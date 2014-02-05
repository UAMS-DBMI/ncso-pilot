package models

import org.openrdf.repository.Repository
import org.openrdf.repository.http.HTTPRepository
import org.openrdf.query.QueryLanguage
import scala.collection.mutable.HashMap
import scala.collection.mutable.Map

import scala.collection.mutable.LinkedList
import scala.collection.mutable
import org.openrdf.model.{Literal, URI}
import org.openrdf.model.vocabulary.{RDFS, RDF}
import collection.JavaConversions._
import java.io.{ByteArrayOutputStream, OutputStream}
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import play.api.libs.json._


object SesameDAO {
  val mainRepo = "NCSOD"

  //DB details
  val sesameServer = "http://144.30.12.10:8080/openrdf-sesame"
  val repositoryID = mainRepo
  val repo: Repository = new HTTPRepository(sesameServer, repositoryID)

  def initializeRepo = {
    repo.initialize()
  }

  def closeRepo = {
    repo.shutDown()
  }

  def getResultsFromSPARQLQuery (query: String) : String  = {
    val con = repo.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query)
    var out = new ByteArrayOutputStream
    var writer = new SPARQLResultsJSONWriter(out);
    tupleQuery.evaluate(writer)
    con.close
    var result = out.toString
    out.close
    result
  }
}
