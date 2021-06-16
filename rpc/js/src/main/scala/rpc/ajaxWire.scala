package rpc

import java.nio.ByteBuffer
import boopickle.Default._
import cats.implicits._
import domain.Api
import org.scalajs.dom.ext.{Ajax, AjaxException}
import sloth._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.typedarray.{ArrayBuffer, TypedArrayBuffer}


object ajaxWire extends Serializations {

  val api: Api = Client[ByteBuffer, Future, ClientException] {
    (req: Request[ByteBuffer]) => {
      Ajax.post(
        url = "http://localhost:8080/ajax/" + req.path.mkString("/"),
        data = Pickle.intoBytes(req.payload), // Param values
        responseType = "arraybuffer",
        headers = Map("Content-Type" -> "application/octet-stream"),
      ).recover {
        case AjaxException(xhr) =>
          val msg    = xhr.status match {
            case 0 => s"Ajax call failed: server not responding."
            case n => s"Ajax call failed: XMLHttpRequest.status = $n."
          }
          println(msg)
          xhr
      }.map { req =>
        TypedArrayBuffer.wrap(req.response.asInstanceOf[ArrayBuffer])
      }
    }
  }.wire[Api]
}