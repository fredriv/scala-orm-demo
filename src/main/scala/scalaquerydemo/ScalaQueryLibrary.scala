package scalaquerydemo

// Import the session management, including the implicit threadLocalSession
import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession

import org.scalaquery.ql.basic.{BasicTable => Table}
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

case class Author(val id: Int, val firstName: String, val lastName: String, val email: Option[String])

case class Book(val id: Int, val title: String, val authorId: Int)

object ScalaQueryLibrary {

  val db = Database.forURL("jdbc:derby:memory:library;create=true", driver = "org.apache.derby.jdbc.EmbeddedDriver")

  val Authors = new Table[Author]("AUTHORS") {
    def id = column[Int]("id", O PrimaryKey)
    def firstName = column[String]("firstname")
    def lastName = column[String]("lastname")
    def email = column[Option[String]]("email")
    def * = id ~ firstName ~ lastName ~ email <> (Author, Author.unapply _)
  }

  val Books = new Table[Book]("BOOKS") {
    def id = column[Int]("id", O PrimaryKey)
    def title = column[String]("title")
    def authorId = column[Int]("author_id")
    def * = id ~ title ~ authorId <> (Book, Book.unapply _)
    def author = foreignKey("author_fk", authorId, Authors)(_.id)
    def titleIdx = index("titleIdx", title, unique = true)
  }

  def init = {
    db withSession {
      (Authors.ddl ++ Books.ddl) create
    }      
  }

  def findAuthorByName(firstName: String, lastName: String): Option[Author] =
    db withSession { (for (a <- Authors
			   if a.firstName === firstName && a.lastName === lastName) 
		      yield a).firstOption }

  def findAuthorByEmail(email: String): Option[Author] =
    db withSession { (for (a <- Authors if a.email === email) yield a).firstOption }

  def findBooksByAuthor(author: Author): List[Book] =
    db withSession { (for (b <- Books if b.authorId === author.id) yield b).list }

}
