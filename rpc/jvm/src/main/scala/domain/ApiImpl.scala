package domain

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ApiImpl extends Api {
  def divide(n: Int): Future[Int] = Future(6 / n)
}