package code.model

case class Book(name: String, content: String, id: String)

case class History(message: String, book: Book, id: String)

/**
 * Mockup for some backend storage like a database...
 */
object BookStorage {

  private val lorem = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna..."

  val books = Book("Lift in Action", lorem, "12345") ::
    Book("Programming in Scala", lorem, "67890") ::
    Book("JavaScript: The definitive guide", lorem, "46385") ::
    Nil

  var history: List[History] = Nil

  def historyEntriesFor(book: Book): List[History] = history.filter(_.book.equals(book))

  def getBook(id: String): Book = books.filter(_.id.equals(id)).first

  def createHistory(message: String, book: Book) {
    println("create a new history with given message = " +
      message +
      " for according to book = " +
      book)
    history ::= History(message, book, history.size.toString)
  }
}