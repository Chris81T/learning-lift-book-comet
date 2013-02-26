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
          "#messages" #> ""
    }
  }

  def postSection = {
    var newMessage = ""

    def postNewMessage(): JsCmd = {
      println("try to post new message = " + newMessage)
      if (!newMessage.isEmpty) {
        BookStorage.createHistory(newMessage, book)
        HistoryServer ! IncomingMessage(newMessage, book)
        newMessage = ""
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

  // ---- ---- ---- ---- ---- KnockoutJS handling ---- ---- ---- ---- ----

  def setupKnockoutComet = {
    S.session match {
      case Full(session) =>
        println("found a session. Prepare knockout comet and markup")
        session.setupComet("KnockoutActor", Full(book.id), book)
        "#ko-messages [data-lift]" #> ("comet?type=KnockoutActor;name=" + book.id)
      case _ =>
        println("KO-JS >> No session available!")
        "header h3" #> "Needs a session to run properly!" &
          "#ko-messages" #> ""
    }
  }

  def postKnockoutMessage = {
    var newMessage = ""
    var username = ""

    def postNewMessage() : JsCmd = {
      println("try to post new knockout message = " +
        newMessage +
        " of user with name = " +
        username)

      if (!newMessage.isEmpty && !username.isEmpty) {
        val result = BookStorage.createComplexHistory(newMessage, username, book)
        KnockoutServer ! KnockoutMessage(result, book)
        newMessage = ""
        username = ""
        SetValById("newMsg", "") & SetValById("username", "")
      } else {
        println("Eighter message or username or both fields are empty. Correct it!")
        // TODO send a cool validation error message
        Noop
      }
    }

    // TODO How to perform following with a JSON_CALL ??? <--------------------
    "#newMsg" #> SHtml.ajaxText(newMessage, {newMessage = _}) &
      "#username" #> SHtml.ajaxText(username, {username = _}) &
      "@postButton [onclick]" #> SHtml.ajaxInvoke(postNewMessage)
  }

}