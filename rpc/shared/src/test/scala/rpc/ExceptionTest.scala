package rpc

import domain.FooProxy
import utest._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object ExceptionTest extends TestSuite with FooProxy {

  lazy val tests = Tests {

    "exception test" - {
      for {
        _ <- callDivide(2).map(_ ==> 3)
        _ <- callDivide(0).transform {
          case Success(res) =>
            // Unexpected success
            Try(res ==> "expected a failed Future with the ArithmeticException...")

          case Failure(exc: ArithmeticException) =>
            // Expected failure
            Try(exc.getMessage ==> "/ by zero")

          case Failure(exc) =>
            // Unexpected failure
            Try(exc.getMessage ==> "Unexpected failure!")
        }
      } yield ()
    }


  }
}
