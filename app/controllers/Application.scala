package controllers

import play.api._
import play.api.mvc._
import scala.util.Random

object Application extends Controller {
  val adjectives = List(
    "Hungry", "Creepy", "Horrible", "Slippery", "Dirty",
    "Pretty", "Sleepy", "Fantastic", "Deaf", "Green",
    "Red", "Blue", "Cosy", "Burning", "Stinky")

  val nouns = List(
    "Tiger", "Duck", "Elephant", "Carrot", "Horse",
    "Zebra", "Whale", "Turtle", "Dog", "Cat", "Monkey",
    "Pirate", "Sausage", "Furball", "Parrot", "Teapot")

  def index = Action {
    //val name = Random.shuffle(adjectives).head +
    //           Random.shuffle(nouns).head
    Ok(views.html.main(f"//start here\nHello\nworld"))
  }
  
  // -- Javascript routing
  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("routes")(
        Scala.session
      )
    ).as("text/javascript") 
  }
}