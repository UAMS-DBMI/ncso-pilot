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
      var resultMap : Map[String, List[String]] = SesameDAO.getResultMapFromSPARQLQuery(sparqlQuery).toMap
      SesameDAO.closeRepo
      var jsonOutput : Map[String, JsValue] = Map[String, JsValue]()

      // place sparqlQuery in Json
      jsonOutput = Map("sparqlQuery" -> Json.toJson(sparqlQuery))

      // place sparqlQuery output in Json
      var sparqlOutputList : List[Map[String, List[String]]] = List[Map[String, List[String]]]()

      for (key: String <- resultMap.keys) {
        sparqlOutputList = Map(key -> resultMap.getOrElse(key, List("None"))) :: sparqlOutputList
      }
      jsonOutput = jsonOutput ++ Map("sparqlResults" -> Json.toJson(sparqlOutputList))

      // place sqlQuery in Json
      jsonOutput = jsonOutput ++ Map("sqlQuery" -> Json.toJson(sqlQuery))

      Ok(Json.toJson(jsonOutput))
    }
}
