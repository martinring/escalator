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
    val (out,channel) = Concurrent.broadcast[String]
    val settings = new Settings
    settings.bootclasspath.value += scala.tools.util.PathResolver.Environment.javaBootClassPath + File.pathSeparator + "lib/scala-library-2.10.0.jar"
    val output = new StringBuilder
    val interpreter = new IMain(settings, new PrintWriter(new OutputStream {
      def write(b: Int): Unit = {
        output += b.toChar
      }
    }))    
    val completer = new JLineCompletion(interpreter).completer()
    val in = Iteratee.foreach[String]{ msg => 
      println(msg)
      val (mod,code) = msg.splitAt(1)
      mod match {
        case "+" =>
          System.setSecurityManager(REPLSecurityManager)          
          val lines = new StringBuilder
          code.split('\n').foreach { line =>
            if (lines.isEmpty) lines.append(line) else lines.append("\n"+line)
            println("interpreting: '"+lines+"'")
            try {
              val ir = interpreter.interpret(lines.mkString)
              ir match {
                case Results.Error =>
                  println("-" + output.mkString)
                  channel.push("-" + output.mkString)
                  output.clear()
                  lines.clear()
                case Results.Incomplete => 
                  println(".")
                  channel.push(".")                
                case Results.Success => 
                  println("+"+ output.mkString)
                  channel.push("+" + output.mkString)
                  output.clear()
                  lines.clear()
              }
            } catch {
              case e: SecurityException =>                
                channel.push("-Very funny! Don't try that again!")
            }	            	            
          }
          lines.clear()
        case "-" => 
        case "m" => 
        case "?" =>
          val (pos,buf) = code.span(_ != ':')
          val candidates = completer.complete(buf.tail, Integer.parseInt(pos))
          channel.push("?" + candidates.cursor + ":" + candidates.candidates.mkString(";"))
      }       
    } 
    (in,out)
  }
}