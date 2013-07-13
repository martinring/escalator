package models

import play.api.libs.iteratee.Concurrent.Channel
import play.api.libs.json.JsValue
import play.api.libs.json.JsString
import scala.tools.nsc.interpreter.Completion.Candidates

sealed abstract class ConsoleResult {
  def jsMsg: JsValue
  def strMsg: String
  def pushToJs(channel: Channel[JsValue]): Unit = channel.push(jsMsg)
  def pushToStr(channel: Channel[String]): Unit = channel.push(strMsg)
}

case object Incomplete extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = "."
}

case class Success(output: String) extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = "+" + output
}

case class Error(output: String) extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = "-" + output
}

case class Completion(candidates: Candidates) extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = "?" + candidates.cursor + ":" + candidates.candidates.mkString(";")
}

case class Timeout(limit: Long) extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = ""
}

case class Invalid(cause: Throwable) extends ConsoleResult {
  lazy val jsMsg = JsString("")
  lazy val strMsg = ""
}
