package domain

import scala.concurrent.Future

trait Api {

  def divide(n: Int): Future[Int]
}
