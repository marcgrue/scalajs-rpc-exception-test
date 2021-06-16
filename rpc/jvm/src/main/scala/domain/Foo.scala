package domain

import scala.concurrent.Future

object Foo extends FooProxy {
  override def callDivide(n: Int): Future[Int] = ApiImpl.divide(n)
}
