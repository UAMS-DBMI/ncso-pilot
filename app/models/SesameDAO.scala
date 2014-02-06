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
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter
import play.api.libs.json._


object SesameDAO {
  val mainRepo = "NCSOD"

  //DB details
  val sesameServer = "http://144.30.12.10:8080/openrdf-sesame"
  val repositoryID = mainRepo
  val repo: Repository = new HTTPRepository(sesameServer, repositoryID)

  def initializeRepo () = {
    repo.initialize()
  }

  def closeRepo () = {
    repo.shutDown()
  }

  def getResultMapFromSPARQLQuery (sparqlQuery: String) : Map[String, List[String]]  = {
    val con = repo.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    val result = tupleQuery.evaluate
    val bindingNames : Seq[String] = result.getBindingNames
    val resultMap : Map[String, List[String]] = new HashMap[String, List[String]]

    // Create a generic map from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      for (name: String <- bindingNames) {
        resultMap(name) = next.getValue(name).stringValue() ::  resultMap.getOrElse(name , List[String]())
      }
    }
    con.close()
    resultMap
  }
}

