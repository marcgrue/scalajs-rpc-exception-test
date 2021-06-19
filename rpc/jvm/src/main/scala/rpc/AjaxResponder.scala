package rpc

import java.nio.ByteBuffer
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import boopickle.Default._
import cats.implicits._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import domain.{Api, ApiImpl}
import sloth.ServerFailure._
import sloth._
import scala.concurrent.Future
import scala.util.{Failure, Success}


object AjaxResponder extends App with Serializations {
  implicit val system           = ActorSystem(Behaviors.empty, "ajaxSystem")
  implicit val executionContext = system.executionContext

  lazy val router = Router[ByteBuffer, Future].route[Api](ApiImpl)

  lazy val route: Route = cors() {
    path("ajax" / "Api" / Remaining)(respond)
  }

  // Flag for response header to ajaxWire - todo
  var isException = "no"

  val respond: String => Route = (method: String) => post {
    extractRequest { req =>
      req.entity match {
        case strict: HttpEntity.Strict =>
          val path       = List("Api", method)
          val args       = Unpickle.apply[ByteBuffer].fromBytes(strict.data.asByteBuffer)
          val callResult = router.apply(Request[ByteBuffer](path, args))

          val futResult: Future[Array[Byte]] = callResult.toEither match {
            case Right(byteBufferResultFuture) =>
              byteBufferResultFuture
                // Serialize data for successful Future
                .map { bytes =>
                  val dataLength                    = bytes.remaining()
                  val bytesAsByteArray: Array[Byte] = Array.ofDim[Byte](dataLength + 1)

                  // Reserve first byte for exception flag
                  bytes.get(bytesAsByteArray, 1, dataLength)

                  // Set first byte as a flag (0) for no exception thrown
                  bytesAsByteArray.update(0, 0)
                  bytesAsByteArray
                }

                // Serialize exception for failed Future
                .recover { case exc: Throwable =>
                  isException = "yes"
                  val t: Throwable     = exc
                  val bytes            = Pickle.intoBytes(t)
                  val dataLength       = bytes.remaining()
                  val bytesAsByteArray = Array.ofDim[Byte](dataLength + 1)

                  // Reserve first byte for exception flag
                  bytes.get(bytesAsByteArray, 1, dataLength)

                  // Set first byte as a flag (1) for exception thrown
                  bytesAsByteArray.update(0, 1)
                  bytesAsByteArray
                }

            case Left(err) => {
              println(s"ServerFailure: " + err)
              err match {
                case PathNotFound(path: List[String])  => Future.failed(new RuntimeException(s"PathNotFound($path)"))
                case HandlerError(exc: Throwable)      => Future.failed(exc)
                case DeserializerError(exc: Throwable) => Future.failed(exc)
              }
            }
          }

          // todo: send throwable status as response header - this doesn't work for some reason
          //          respondWithHeader(RawHeader("isException", isException)) {
          //            complete(futResult)
          //          }
          // // or
          //          onComplete(futResult) {
          //            case Success(f)   =>
          //              respondWithHeader(RawHeader("isException", isException)) {
          ////                complete(Future.successful(f))
          //                complete(f)
          //              }
          //            case Failure(exc) => complete(exc)
          //          }

          complete(futResult)

        case _ => complete("Ooops, request entity is not strict!")
      }
    }
  }

  Http()
    .newServerAt("localhost", 8080)
    .bind(route)
    .onComplete {
      case Success(b) => println(s"Ajax server is running ${b.localAddress} ")
      case Failure(e) => println(s"there was an error starting the server $e")
    }
}
