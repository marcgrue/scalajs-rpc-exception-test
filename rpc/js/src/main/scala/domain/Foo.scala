package domain

import scala.concurrent.Future
import rpc.ajaxWire.api


object Foo extends FooProxy {
  override def callDivide(n: Int): Future[Int] = api.divide(n)
}
