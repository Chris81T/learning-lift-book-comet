package code.snippet

import net.liftweb._
import util.Helpers._


import code.model._

object BooksSnippet {

  def render = {
    "li *" #> BookStorage.books.map(book =>
      "a [href]" #> BookSnippet.menu.calcHref(book) &
        "@bookName" #> book.name
    )
  }

}