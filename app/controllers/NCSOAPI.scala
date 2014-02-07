package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._

import models.SesameDAO
import play.mvc.With
import actions.WithCors
import scala.collection.immutable.Map

object NCSOAPI extends Controller {
    val list_of_APIS = List("getcurrentapis", "testsesameconnection")

    def listCurrentAPIS = WithCors("GET", "POST") {
      Action {
        Ok(Json.toJson(Map("currentAPIs" -> Json.toJson(list_of_APIS))))
      }
    }


    def sesameConnectionTest = WithCors("GET", "POST") {
      Action {
        val sparqlQuery = "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count"
        val sqlQuery = "SELECT * FROM FAKE_TABLE fk1 INNER JOIN FAKER_TABLE fk2 ON fk1.FakeID = fk2.FakeID"

        SesameDAO.initializeRepo()
        val resultRows : List[List[Map[String, String]]] = SesameDAO.getResultRowsFromSPARQLQuery(sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameDAO.getResultColumnMapFromSPARQLQuery(sparqlQuery)
        SesameDAO.closeRepo()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery)
          )
        ))
      }
    }
}
