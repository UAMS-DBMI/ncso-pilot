package models

import org.openrdf.repository.Repository
import org.openrdf.repository.http.HTTPRepository
import scala.collection.immutable.{HashMap, Map}
import org.openrdf.query.QueryLanguage
import collection.JavaConversions._
import play.api.libs.json.JsValue
import play.api.libs.json._

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
    repository = new HTTPRepository(serverUrl, repositoryID)
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
    val result = tupleQuery.evaluate
    val bindingNames : Seq[String] = result.getBindingNames
    var resultList : List[Map[String, String]] = List[HashMap[String, String]]()

    // Create a list of rows from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      val rowMap: scala.collection.mutable.Map[String, String] = scala.collection.mutable.HashMap[String, String]()
      for (name: String <- bindingNames) {
        rowMap(name) = next.getValue(name).stringValue()
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

  /**
   *  Execute a sparql query on the server and receive the rows
   *
   * @param sparqlQuery The sparql query to execute on the server and retrieve the rows.
   * @param parentJsonObject Optional root JsObject to append the results to.
   * @return JsObject of all of the rows
   */
  //TODO: Change internal datastructs to use JsObjects so I don't have to convert it at the end
  /*def getResultRowsInJsonFromSPARQLQuery (sparqlQuery: String, parentJsonObject: JsObject = null) : JsObject  = {
    val con = repository.getConnection
    val tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQuery)
    val result = tupleQuery.evaluate
    val bindingNames : Seq[String] = result.getBindingNames
    var resultList : List[Map[String, String]] = List[HashMap[String, String]]()

    // Create a list of rows from the result bindingNames and values
    while(result.hasNext) {
      val next = result.next
      val rowMap: scala.collection.mutable.Map[String, String] = scala.collection.mutable.HashMap[String, String]()
      for (name: String <- bindingNames) {
        rowMap(name) = next.getValue(name).stringValue()
      }
      resultList = rowMap.toMap :: resultList
    }
    con.close()
    if (parentJsonObject == null) {
      return Json.toJson(resultList)
    }
    parentJsonObject ++ resultList
  }*/









}
