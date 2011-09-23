package scalaquerydemo

import org.scalatest.Assertions
import org.scalatest.FlatSpec
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import org.scalaquery.session._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.basic.{BasicTable => Table}
import org.scalaquery.ql.basic.BasicDriver.Implicit._
import org.scalaquery.ql.TypeMapper._
import org.scalaquery.ql._

import scalaquerydemo.ScalaQueryLibrary._

@RunWith(classOf[JUnitRunner])
class ScalaQueryLibraryTest extends FlatSpec with ShouldMatchers with Assertions {

  ScalaQueryLibrary.init

  "A library" should "register authors" in {
    db withSession {
      Authors.insert(Author(1, "Fredrik", "Testesen", Some("fvr@knowit.no")))
      Authors.insert(Author(2, "Johannes Stamnes", "Vraalsen", None))
      Authors.insert(Author(3, "Wil", "Wheaton", Some("wil@wilwheaton.net")))
      Authors.insert(Author(4, "Neil", "Gaiman", None))
    }
  }

  it should "register books to authors" in {
    db withSession {
      Books.insert(Book(1, "Memories of the Future", 3))
      Books.insert(Book(2, "Coraline", 4))
      Books.insert(Book(3, "American Gods", 4))
    }
  }

  it should "not register multiple books with the same title" in {
    evaluating {
      db withSession {
        Books.insert(Book(4, "Coraline", 3))
      }
    } should produce[Exception]
  }

  it should "not register books to authors that do not exist" in {
    evaluating {
      db withSession {
        Books.insert(Book(5, "Test", 0))
      }
    } should produce[Exception]
  }

  it should "find author by e-mail" in {
    db withSession {
      val a = findAuthorByEmail("wil@wilwheaton.net")
      a should not equal(None)
      a.get.firstName should equal("Wil")
      a.get.lastName should equal("Wheaton")
      a.get.email should equal(Some("wil@wilwheaton.net"))
    }
  }

  it should "find author by name" in {
    db withSession {
      val a = findAuthorByName("Neil", "Gaiman")
      a should not be (None)
      a.get.firstName should equal("Neil")
      a.get.lastName should equal("Gaiman")
      a.get.email should equal(None)
    }
  }
  
  it should "not find authors that do not exist" in {
    db withSession {
      findAuthorByEmail("fredrik@vraalsen.no") should equal(None)
      findAuthorByName("Fredrik", "Vraalsen") should equal(None)
    }
  }

  it should "find books for authors" in {
    db withSession {
      val a = findAuthorByName("Neil", "Gaiman")
      a should not be (None)
      val books = findBooksByAuthor(a.get)
      books find(_.title == "Coraline") should not equal(None)
    }
  }

  it should "find all books for all authors" in {
    db withSession {
      val authors = (for (a <- Authors) yield a).list

      val booksPerAuthor: List[(Author, List[Book])] = 
	for (a <- authors) yield {
	  val books = (for (b <- Books 
			    if b.authorId === a.id) 
		       yield b).list
	  (a, books)
	}

      booksPerAuthor should have length 4
    }
  }

  it should "find all books for all authors 2" in {
    db withSession {
      val authors = (for (a <- Authors) yield a).list
      val allBooks = (for (b <- Books) yield b).list

      val booksPerAuthor: List[(Author, List[Book])] = 
	for (a <- authors) yield {
	  val books = for (b <- allBooks 
			   if b.authorId == a.id) 
		      yield b
	  (a, books)
	}

      booksPerAuthor should have length 4
    }
  }

}
