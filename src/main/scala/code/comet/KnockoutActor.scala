package code.comet

import net.liftweb._
import http._
import common._

import code.model._
import js.JE

class KnockoutActor extends CometActor with CometListener {

  var book: Box[Book] = Empty

  override def name: Box[String] = book.map(_.id)

  def registerWith = KnockoutServer

  override def lowPriority = {
    case incomingBook: Book => setupIncomingBook(incomingBook)
    case KnockoutMessage(history, book) => handleIncomingMessage(history, book)
    case HelloKnockoutActor(message) => println("KO ACTOR >> >> >> received welcome message from KNOCKOUT-SERVER = " + message)
  }

  private def setupIncomingBook(incomingBook: Book) {
    println("KO ACTOR >> >> incoming book " +
      incomingBook +
      " te setup this history-actor = " +
      toString)

    book = Full(incomingBook)

    /*
     * re-render the complete content while originally the render method is
     * called before this method (see logs)
     */
    reRender()
  }

  private def handleIncomingMessage(history: ComplexHistory, book: Book) {
    println("KO ACTOR >> >> incoming knockout message - history = " +
      history +
      " according to book = " +
      book +
      " will be handled by the actor = " +
      toString)

    this.book match {
      case Full(knownBook) =>
        println("KO ACTOR >> >> Check, if given book equals to known registered book. Else do nothing with incoming message!")
        if (knownBook.equals(book)) {
          println("KO ACTOR >> >> Okay, this actor = " + toString + " has to handle the incoming message. So do it and prepare some " +
            "knockoutJS operations : )")
          // TODO partialUpdate(JE.Call("partialUpdate", message).cmd)
        } else {
          println("KO ACTOR >> >> Incoming message is not for this actor = " + toString)
        }
      case _ => println("KO ACTOR >> >> No book registered in this actor = " + toString + ". Do nothing with incoming message!")
    }

  }

  def render = {
    println("KO ACTOR >> >> Start to initially render all existing messages for given book = " + book)
    book match {
      case Full(book) =>
        "*" #> ""
      case _ => "ul" #> "No book is known by the knockout-actor"
    }
  }
}