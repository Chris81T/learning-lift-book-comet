package code.comet

import net.liftweb._
import http._
import common._

import code.model._
import js.JE
import json.JsonAST.{JArray, JObject}
import json.JsonDSL._

class KnockoutActor extends CometActor with CometListener {

  var book: Box[Book] = Empty

  override def name: Box[String] = book.map(_.id)

  def registerWith = KnockoutServer

  override def lowPriority = {
    case incomingBook: Book => setupIncomingBook(incomingBook)
    case KnockoutMessage(history, book) => handleIncomingMessage(history, book)
    case HelloKnockoutActor(message) => println("KO ACTOR >> >> >> received welcome message from KNOCKOUT-SERVER = " + message)
  }

  private def generateJSON(history: ComplexHistory) : JObject = {
    val json = (
        ("message" -> history.message) ~
        ("username" -> history.username) ~
        ("timestamp" -> history.timestamp.toDate.toString)
    )

    println("create for given history = " +
      history +
      " an appropriate JSON object = " +
      json)

    json
  }
  private def setupIncomingBook(incomingBook: Book) {
    println("KO ACTOR >> >> incoming book " +
      incomingBook +
      " te setup this history-actor = " +
      toString)

    book = Full(incomingBook)

    /*
     * get all appropriate history messages, create JSON objects and send them to the client, so the knockoutJS will
     * do the rest
     */
    val histories = BookStorage.complexHistoryEntriesFor(incomingBook)
    val historiesJSONArray = for (history <- histories) yield generateJSON(history)
    partialUpdate(JE.Call("initHistories", JArray(historiesJSONArray)).cmd)
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
          partialUpdate(JE.Call("addHistory", generateJSON(history)).cmd)
        } else {
          println("KO ACTOR >> >> Incoming message is not for this actor = " + toString)
        }
      case _ => println("KO ACTOR >> >> No book registered in this actor = " + toString + ". Do nothing with incoming message!")
    }

  }

  def render = {
    // TODO how to ignore it instead of using a fake-id ?!
    println("KO ACTOR >> >> RENDERING IS NOT REQUIRED !!!")
    "fake" #> ""
  }
}