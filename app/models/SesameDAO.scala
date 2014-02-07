package models

import org.openrdf.repository.Repository
import org.openrdf.repository.http.HTTPRepository
import org.openrdf.query.QueryLanguage
import scala.collection.immutable.HashMap
import scala.collection.immutable.Map

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

  // Returns a list that represents the rows within a result table.
  def getResultRowsFromSPARQLQuery (sparqlQuery: String) : List[Map[String, String]]  = {
    val con = repo.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    val result = tupleQuery.evaluate
    val bindingNames : Seq[String] = result.getBindingNames
    var resultList : List[Map[String, String]] = List[HashMap[String, String]]()

    // Create a list of rows from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      var rowMap: scala.collection.mutable.Map[String, String] = scala.collection.mutable.HashMap[String, String]()
      for (name: String <- bindingNames) {
        rowMap(name) = next.getValue(name).stringValue();
      }
      resultList = rowMap.toMap :: resultList
    }
    con.close()
    resultList
  }


  // Returns a map where each output column name is mapped to a list of the column contents
  def getResultColumnMapFromSPARQLQuery (sparqlQuery: String) : Map[String, List[String]]  = {
    val con = repo.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    val result = tupleQuery.evaluate
    val bindingNames : Seq[String] = result.getBindingNames
    val resultMap : scala.collection.mutable.Map[String, List[String]] = new scala.collection.mutable.HashMap[String, List[String]]

    // Create a map of columns from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      for (name: String <- bindingNames) {
        resultMap(name) = next.getValue(name).stringValue() ::  resultMap.getOrElse(name , List[String]())
      }
    }
    con.close()
    resultMap.toMap
  }
}

