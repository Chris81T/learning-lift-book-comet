package code.snippet

import net.liftweb._
import http._
import js.{JsCmds, JsCmd}
import JsCmds._
import util.Helpers._
import common._
import sitemap._

import code.model._
import code.comet._

object BookSnippet {

  lazy val menu = Menu.param[Book]("Book",
    "Watching a book",
    id => Full(BookStorage.getBook(id)),
    book => book.id) / "book" / *

}

class BookSnippet(book: Book) {

  def render = {
    "@bookName" #> book.name &
      "@bookContent" #> book.content
  }

  def prepareComet = {
    S.session match {
      case Full(session) =>
        println("found a session. Prepare comet and markup")
        session.setupComet("HistoryActor", Full(book.id), book)
        "#messages [data-lift]" #> ("comet?type=HistoryActor;name=" + book.id)
      case _ =>
        println("No session available!")
        "header h2" #> "Needs a session to run properly!" &
          "#comet" #> ""
    }
  }

  def postSection = {
    var newMessage = ""

    def postNewMessage(): JsCmd = {
      println("try to post new message = " + newMessage)
      if (!newMessage.isEmpty) {
        BookStorage.createHistory(newMessage, book)
        HistoryServer ! IncomingMessage(newMessage, book)
        SetValById("new", "")
      } else {
        println("While empty message exsists, do nothing")
        // TODO create a cool message box on the page, which will be hidden after few seconds!
        Noop
      }
    }

    "#new" #> SHtml.ajaxText(newMessage, {
      newMessage = _
    }) &
      "@postButton [onclick]" #> SHtml.ajaxInvoke(postNewMessage)
  }
}