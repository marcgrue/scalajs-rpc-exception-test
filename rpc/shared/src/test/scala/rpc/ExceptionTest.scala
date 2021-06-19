package rpc

import domain.FooProxy
import utest._
import scala.concurrent.ExecutionContext.Implicits.global

object ExceptionTest extends TestSuite with FooProxy {

  lazy val tests = Tests {

    "exception test" - {
      for {
        _ <- callDivide(2).map(_ ==> 3)
        _ <- callDivide(0).failed.collect {
          case exc: ArithmeticException => exc.getMessage ==> "/ by zero"
        }
      } yield ()
    }
  }
}
