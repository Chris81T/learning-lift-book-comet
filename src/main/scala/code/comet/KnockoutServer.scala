package code.comet

import net.liftweb.actor._
import net.liftweb._
import http._

import code.model._
import collection.mutable.{ListBuffer, Map}
import org.joda.time.DateTime

case class KnockoutMessage(history: ComplexHistory, book: Book)
case class HelloKnockoutActor(message: String)

object KnockoutServer extends LiftActor with ListenerManager {

  /**
     This method is called when the

     updateListeners()

     method needs a message to send to subscribed Actors. In particular, createUpdate is used to create the first
     message that a newly subscribed CometListener will receive.
   */
  def createUpdate = HelloKnockoutActor("registered at server --> timestamp = " + new DateTime().toDate.toString)

  override def lowPriority = {
    case msg: KnockoutMessage =>
      println("KO SERVER >> incoming knockout message send to all known listeners...")
      updateListeners(msg)
  }
}