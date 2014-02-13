package models

import org.openrdf.repository.Repository
import org.openrdf.repository.http.{HTTPQueryEvaluationException, HTTPRepository}
import scala.collection.immutable.{HashMap, Map}
import org.openrdf.query.{TupleQueryResult, QueryLanguage}
import collection.JavaConversions._
import play.api.libs.json.JsValue
import play.api.libs.json._
import com.complexible.stardog.sesame.StardogRepository
import com.complexible.stardog.api.ConnectionConfiguration



object SesameSparql2Json {
  // TODO: try to use something other than null
  private var repository : Repository = null

  /**
    * Initialize the connection with a sesame server connection
    * with the inputted parameters
    *
    * @param serverUrl The url location of the sesame server
    * @param repositoryID The name of the repository
    */
  def openConnection (serverUrl: String, repositoryID: String) = {
    repository = new StardogRepository(ConnectionConfiguration.from("http://localhost/ncso").credentials("admin", "admin"))

    // repository = new HTTPRepository(serverUrl, repositoryID)
    repository.initialize()
  }

  /**
    * Close connection with the sesame server
    */
  def closeConnection () = {
    repository.shutDown()
  }

  /**
   *  Execute a sparql query on the server and receive the rows
   *
   * @param sparqlQuery The sparql query to execute on the server and retrieve the rows.
   * @return List of rows where each row is a hashmap of (columnname -> cellValue)
   */

  def getResultRowsFromSPARQLQuery (sparqlQuery: String) : List[Map[String, String]]  = {
    val con = repository.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    var result: TupleQueryResult  = null
    try {
      result = tupleQuery.evaluate
    } catch {
      case e: HTTPQueryEvaluationException => println("Sparql Query Failed : " + e)
    }
    val bindingNames : Seq[String] = result.getBindingNames
    var resultList : List[Map[String, String]] = List[HashMap[String, String]]()

    // Create a list of rows from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      val rowMap: scala.collection.mutable.Map[String, String] = scala.collection.mutable.HashMap[String, String]()
      for (name: String <- bindingNames) {
        if(next.getValue(name) != null){ //value returned can be null
          rowMap(name) = next.getValue(name).stringValue()
        } else {
          rowMap(name) = ""
        }
      }
      resultList = rowMap.toMap :: resultList
    }
    con.close()
    resultList
  }


  /**
   *  Execute a sparql query on the server and receive the columns
   *
   * @param sparqlQuery The sparql query to execute on the server and retrieve the rows.
   * @return Map of (Columnname -> List[valuesInColumn])
   */
  def getResultColumnMapFromSPARQLQuery (sparqlQuery: String) : Map[String, List[String]]  = {
    val con = repository.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    var result: TupleQueryResult  = null
    try {
      result = tupleQuery.evaluate
    } catch {
      case e: HTTPQueryEvaluationException => println("Sparql Query Failed : " + e)
    }
    val bindingNames : Seq[String] = result.getBindingNames
    val resultMap : scala.collection.mutable.Map[String, List[String]] = new scala.collection.mutable.HashMap[String, List[String]]

    // Create a map of columns from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      for (name: String <- bindingNames) {
        if(next.getValue(name) != null){ //value returned can be null
          resultMap(name) = next.getValue(name).stringValue() ::  resultMap.getOrElse(name , List[String]())
        } else {
          resultMap(name) = "" ::  resultMap.getOrElse(name , List[String]())
        }
      }
    }
    con.close()
    resultMap.toMap
  }
}
