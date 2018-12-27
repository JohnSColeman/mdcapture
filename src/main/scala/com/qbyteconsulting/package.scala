package com

import org.slf4j.Logger

import scala.util.{Failure, Try}

package object qbyteconsulting {

  def LogTry[A](block: => Unit)(implicit log: Logger): Try[Unit] = {
    Try(block) recoverWith {
      case e: Throwable =>
        log.error("Try error:", e)
        Failure(e)
    }
  }
}
