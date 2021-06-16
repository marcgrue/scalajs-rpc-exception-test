package domain

import scala.concurrent.Future

trait FooProxy {

  // Foo is implemented on JS and JVM platforms
  def callDivide(n: Int): Future[Int] = Foo.callDivide(n)
}
