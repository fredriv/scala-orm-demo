package squeryldemo

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Query
import org.squeryl.Schema
import org.squeryl.Session
import org.squeryl.SessionFactory
import org.squeryl.adapters.DerbyAdapter
import org.squeryl.annotations.Column
import org.squeryl.dsl.OneToMany
import org.squeryl.dsl.ManyToOne

class Author(val id: Int,
	     val firstName: String,
	     val lastName: String,
	     val email: Option[String]) extends KeyedEntity[Int] {

  // no-arg constructor required for classes with Option[] fields
  def this() = this(0, "", "", Some(""))

  lazy val books: OneToMany[Book] = SquerylLibrary.authorToBooks.left(this)
}

class Book(val id: Int,
	   var title: String,
	   @Column(name = "author_id") var authorId: Int) extends KeyedEntity[Int] {

  lazy val author: ManyToOne[Author] = SquerylLibrary.authorToBooks.right(this)
}

object SquerylLibrary extends Schema {

  val authors = table[Author]("SQUERYL_AUTHORS")

  val books = table[Book]("SQUERYL_BOOKS")

  on(authors)(a => declare(
    a.email is (indexed("squerylEmailIdx")),
    a.firstName is (indexed),
    a.lastName is (indexed),
    columns(a.firstName, a.lastName) are (indexed)))

  on(books)(b => declare(
    b.title is (unique, indexed("squerylTitleIdx"), dbType("varchar(255)"))))

  // TODO: Set up relationship between book and author
  val authorToBooks =
    oneToManyRelation(authors, books).via((a, b) => a.id === b.authorId)

  // TODO: Set up query to find author by name
  def findAuthorByName(firstName: String, lastName: String): Option[Author] =
    from(authors)(a => where(a.firstName === firstName and a.lastName === lastName) select (a)).headOption

  // TODO: Set up query to find author by email
  def findAuthorByEmail(email: String): Option[Author] =
    from(authors)(a => where(a.email === Some(email)) select (a)).headOption

  // TODO: Set up query for books by author
  def findBooksByAuthor(author: Author): List[Book] = 
    from(books)(b => where(b.authorId === author.id) select(b)).toList

  def init = {
    Class.forName("org.apache.derby.jdbc.EmbeddedDriver")

    SessionFactory.concreteFactory = Some(() =>
      Session.create(
        java.sql.DriverManager.getConnection("jdbc:derby:memory:library;create=true"),
        new DerbyAdapter()))

    transaction {
      // Library.printDdl
      SquerylLibrary.create
    }
  }

}
