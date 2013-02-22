package code.comet

import net.liftweb._
import http._
import common._

import code.model._
import js.JE

class HistoryActor extends CometActor {

  var book: Box[Book] = Empty

  override def name: Box[String] = book.map(_.id)

  def registerWith = HistoryServer

  override def lowPriority = {
    case incomingBook: Book => setupIncomingBook(incomingBook)
    case IncomingMessage(message, book) => handleIncomingMessage(message, book)
  }

  private def setupIncomingBook(incomingBook: Book) {
    println("incoming book " +
      incomingBook +
      " te setup this history-actor = " +
      toString)

    if (book.isEmpty) {
      println("It seem's to be the first setup of this actor. Say 'hello' to the HistoryServer")
      HistoryServer ! IncomingActor(this, incomingBook)
    } else {
      println("It is not neccessary to send the HistoryServer an IncomingActor message")
    }

    book = Full(incomingBook)

    /*
     * re-render the complete content while originally the render method is
     * called before this method (see logs)
     */
    reRender()
  }

  private def handleIncomingMessage(message: String, book: Book) {
    println("incoming message = " +
      message +
      " according to book = " +
      book +
      " will be handled by the actor = " +
      toString)

    this.book match {
      case Full(b) =>
        if (b.equals(book)) {
          println("Invoke JavaSript function for partial update the appropriate pages / tabs for viewed book = " + book)
          partialUpdate(JE.Call("partialUpdate", message).cmd)
        } else println("Cannot handle incoming message, while given incoming book = " +
                 book +
                 " is not equal to the known book = " +
                 b +
                 " of this actor = " +
                 toString)
      case _ => println("SORRY, could not handle this message, while no book is setup'd to this actor = " +
                  toString)
    }
  }

  override def localSetup() {
    println("local setup for history-actor")
    super.localSetup()
  }

  override def localShutdown() {
    println("local shutdown for history-actor")
    super.localShutdown()
  }

  def render = {
    println("Start to initially render all existing messages for given book = " + book)
    book match {
      case Full(b) =>
        val entries = BookStorage.historyEntriesFor(b)
        if (!entries.isEmpty) "li *" #> entries.map(entry => "@msg" #> entry.message)
        else "ul" #> "No comments for this book available."
      case _ => "ul" #> "No book is known by the history-actor"
    }
  }
}