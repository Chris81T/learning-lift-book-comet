package code.comet

import net.liftweb.actor._
import net.liftweb._
import http._

import code.model._
import collection.mutable.{ListBuffer, Map}

case class IncomingActor(actor: CometActor, book: Book)
case class LeavingActor(actor: CometActor, book: Book)

case class IncomingMessage(message: String, book: Book)

object HistoryServer extends LiftActor {

  private var actors = Map[Book, ListBuffer[CometActor]]()

  def messageHandler = {
    case IncomingActor(actor, book) => addActor(actor, book)
    case LeavingActor(actor, book) => remActor(actor, book)
    case IncomingMessage(message, book) => broadcastMessage(message, book)
  }

  private def printActors = print("known actors are " + actors)

  private def addActor(actor: CometActor, book: Book) {
    println("Incoming actor = " +
      actor +
      " according to book = " +
      book)
    actors.get(book) match {
      case Some(knownActors) => {
        knownActors.append(actor)
      }
      case None => {
        val newList: ListBuffer[CometActor] = new ListBuffer[CometActor]()
        newList.append(actor)
        actors += (book -> newList)
      }
    }
    printActors
  }

  private def remActor(actor: CometActor, book: Book) {
    println("Leaving actor = " +
      actor +
      " according to book = " +
      book)
    actors.get(book) match {
      case Some(knownActors) => {
        actors += (book -> knownActors.filter(!_.equals(actor)))
      }
      case None => println("Something went wrong. No actors for given book " +
                     book +
                     " are known by this server.")
    }
    printActors
  }

  private def broadcastMessage(message: String, book: Book) {
    println("Incoming message = " +
      message +
      " according to book = " +
      book)
    actors.get(book) match {
      case Some(knownActors) => knownActors.foreach(_ ! IncomingMessage(message, book))
      case None => println("Could not send the given message = " +
        message +
        " to any actors, while no actors are known for this book = " +
        book)
    }
  }
}