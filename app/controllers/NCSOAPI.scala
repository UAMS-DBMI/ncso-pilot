package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._

import models.SesameDAO

object NCSOAPI extends Controller {

    def listCurrentAPIS = TODO

    def sesameConnectionTest = Action {
      val query = "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count"
      //val query = "SELECT DISTINCT ?class WHERE { ?s a ?class . }"

      SesameDAO.initializeRepo
      var results = SesameDAO.getResultsFromSPARQLQuery(query)
      SesameDAO.closeRepo
      Ok(results)
    }
}
