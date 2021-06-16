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

  val respond: String => Route = (method: String) => post {
    extractRequest { req =>
      req.entity match {
        case strict: HttpEntity.Strict =>
          val path       = List("Api", method)
          val args       = Unpickle.apply[ByteBuffer].fromBytes(strict.data.asByteBuffer)
          val callResult = router.apply(Request[ByteBuffer](path, args))
          val futResult  = callResult.toEither match {
            case Right(byteBufferResultFuture) =>
              byteBufferResultFuture
                .map { byteBufferResult =>
                  val dataAsByteArray = Array.ofDim[Byte](byteBufferResult.remaining())
                  byteBufferResult.get(dataAsByteArray)
                  dataAsByteArray
                }
                .recover { case exc: Throwable =>
                  println("---- Recovering in AjaxResponder ---------------------\n" + exc)
                  println(exc.getStackTrace.mkString("\n"))
                  val t: Throwable    = exc
                  val tb              = Pickle.intoBytes(t)
                  val dataAsByteArray = Array.ofDim[Byte](tb.remaining())
                  tb.get(dataAsByteArray)

                  // Bytes of ArithmeticException("/ by zero") where the first byte (15) is
                  // the number of the fifteenth exception in Boopickles Composite[Throwable]
                  // See https://github.com/suzaku-io/boopickle/blob/master/boopickle/shared/src/main/scala/boopickle/CompositePicklers.scala#L110
                  // 15, 9, 47, 32, 98, 121, 32, 122, 101, 114, 111
                  //        /       b   y        z    e    r    o
                  // Why is only this first byte (15) deserialized on the client?
                  println(dataAsByteArray.mkString(", "))

                  // Why is the Future containing this exception successful?
                  dataAsByteArray
                }

            case Left(err) => {
              println(s"ServerFailure: " + err)
              err match {
                case PathNotFound(path: List[String]) =>
                  Future.failed(new RuntimeException(s"PathNotFound($path)"))

                case HandlerError(exc: Throwable) =>
                  println(exc.getStackTrace.mkString("\n"))
                  Future.failed(exc)

                case DeserializerError(exc: Throwable) => Future.failed(exc)
              }
            }
          }
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
