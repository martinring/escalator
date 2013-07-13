package models

import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.JLineCompletion
import scala.tools.nsc.interpreter.Results
import scala.tools.nsc.Properties
import scala.tools.nsc.Settings

class Console {
  
  lazy val versionMsg = "Scala compiler " +
    Properties.versionString + " -- " +
    Properties.copyrightString
  
  lazy val comPath = try {
    jarPathOf("scala.tools.nsc.Interpreter")
  } catch { case e: Throwable =>
    throw new RuntimeException("Unable lo load scala interpreter from classpath (scala-compiler jar is missing?)", e)
  }
  
  lazy val libPath = try {
    jarPathOf("scala.ScalaObject")
  } catch { case e: Throwable =>
    throw new RuntimeException("Unable to load scala base object from classpath (scala-library jar is missing?)", e)
  }
  
  lazy val interpreter = {
    val settings = new Settings
    settings.classpath.value += java.io.File.pathSeparator + comPath
    settings.classpath.value += java.io.File.pathSeparator + libPath
    new IMain(settings, outWriter)
  }
  
  lazy val completer = {
    val completion = new JLineCompletion(interpreter)
    completion.completer()
  }
  
  //todo: security
  //todo: timeout
  def interpret(input: String, limit: Long = 5000): ConsoleResult = {
    interpreter.interpret(input) match {
      case Results.Success =>
        outWriter.flush()
        val output = outBuffer.mkString
        outBuffer.clear()
        Success(output)
      case Results.Incomplete =>
        Incomplete
      case Results.Error =>
        outWriter.flush()
        val output = outBuffer.mkString
        outBuffer.clear()
        Error(output)
    }
  }
  
  def complete(input: String, pos: Int): ConsoleResult = {
    val candidates = completer.complete(input, pos)
    Completion(candidates)
  }
  
  def reset(): Unit = {
    outWriter.flush()
    outBuffer.clear()
    interpreter.reset()
  }
  
  private val outBuffer = new StringBuilder
  private val outWriter = new java.io.PrintWriter(
    new java.io.OutputStream {
      def write(b: Int): Unit = outBuffer += b.toChar
    }
  )
  
  private def jarPathOf(className: String) = {
    val resource = className.split('.').mkString("/", "/", ".class")
    val path = getClass.getResource(resource).getPath
    val indexOfFile = path.indexOf("file:") + 5
    val indexOfSeparator = path.lastIndexOf('!')
    path.substring(indexOfFile, indexOfSeparator)
  }
}