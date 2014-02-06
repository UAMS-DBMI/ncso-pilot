package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._

import models.SesameDAO

object NCSOAPI extends Controller {

    def listCurrentAPIS = TODO

    def sesameConnectionTest = Action {
      val sparqlQuery = "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count"
      val sqlQuery = "SELECT * FROM FAKE_TABLE fk1 INNER JOIN FAKER_TABLE fk2 ON fk1.FakeID = fk2.FakeID"

      SesameDAO.initializeRepo
      val resultRows : List[List[String]] = SesameDAO.getResultRowsFromSPARQLQuery(sparqlQuery)

      //If you want the columns uncomment the line below:
      val resultCols : Map[String, List[String]] = SesameDAO.getResultColumnMapFromSPARQLQuery(sparqlQuery).toMap
      SesameDAO.closeRepo

      var jsonOutput : Map[String, JsValue] = Map[String, JsValue]()

      // Place sparqlQuery in Json
      jsonOutput = Map("sparqlQuery" -> Json.toJson(sparqlQuery))

      // Place sparqlQuery output in Json
      jsonOutput = jsonOutput ++ Map("sparqlResults" -> Json.toJson(resultRows))

      // Place sqlQuery in Json
      jsonOutput = jsonOutput ++ Map("sqlQuery" -> Json.toJson(sqlQuery))

      Ok(Json.toJson(jsonOutput))
    }
}
