package controllers

import java.io.File
import java.io.OutputStream
import java.io.PrintWriter
import scala.annotation.Annotation
import scala.reflect.internal.util.SourceFile
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results
import play.api._
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import scala.tools.nsc.reporters.AbstractReporter
import scala.reflect.internal.util.Position
import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc.interpreter.JLineCompletion
import java.io.FileDescriptor
import java.security.Permission

object REPLSecurityManager extends SecurityManager {
  override def checkExit(status: Int) { throw new SecurityException(); }
  override def checkCreateClassLoader() { }  
  override def checkRead(fd: FileDescriptor) { }           
  override def checkRead(file: String) { }
  override def checkRead(file: String, context: Object) {}         
  override def checkPropertiesAccess() {}
  override def checkPropertyAccess(key: String) {} 
  override def checkPermission(perm: Permission) {}
}

object Scala extends Controller {
  def session = WebSocket.using[String] { request =>
    val console = new models.Console
    val (out, channel) = Concurrent.broadcast[String]
    val in = Iteratee.foreach[String]{ msg =>
      println(msg)
      val (mode, code) = msg.splitAt(1)
      mode match {
        case "+" =>
          System.setSecurityManager(REPLSecurityManager)
          val lines = new StringBuilder
          code.split('\n').foreach { line =>
            if (lines.isEmpty) lines.append(line) else lines.append("\n"+line)
            println("interpreting: '"+lines+"'")
            try {
              val result = console.interpret(lines.mkString)
              result pushToStr channel
            } catch {
              case e: SecurityException =>
                channel.push("-Very funny! Don't try that again!")
            }
          }
          lines.clear()
        case "-" => 
        case "m" => 
        case "?" =>
          val (pos, buf) = code.span(_ != ':')
          val result = console.complete(buf.tail, Integer.parseInt(pos))
          result pushToStr channel
      }
    } 
    (in,out)
  }
}