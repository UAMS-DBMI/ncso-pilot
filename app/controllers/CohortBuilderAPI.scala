package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.{Json => SJson, JsValue}
import actions.WithCors
import play.libs.Json
import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode
import models.{SesameSparql2Json, SPARQLBuilder}
import scala.collection.immutable.Map
import play.api.libs.json._


/**
 * Created by joshhanna on 3/16/14.
 */
object CohortBuilderAPI extends Controller {
  val sesamePrefixes = "PREFIX dc:<http://purl.org/dc/elements/1.1/>\nPREFIX PATO:<http://purl.org/obo/owl/PATO#>\nPREFIX :<http://www.ifomis.org/bfo/1.1#>\nPREFIX ro:<http://www.obofoundry.org/ro/ro.owl#>\nPREFIX protege:<http://protege.stanford.edu/plugins/owl/protege#>\nPREFIX ncso2:<http://www.semanticweb.org/semanticweb.org/ncso/>\nPREFIX UO:<http://purl.org/obo/owl/UO#>\nPREFIX ncso3:<http://purl.obolibrary.org/obo/ncso/dev/ncso.owl/>\nPREFIX snap:<http://www.ifomis.org/bfo/1.1/snap#>\nPREFIX bfo:<http://www.ifomis.org/bfo/1.1#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX obo:<http://purl.obolibrary.org/obo/>\nPREFIX obo2:<http://purl.obolibrary.org/obo#>\nPREFIX psys:<http://proton.semanticweb.org/protonsys#>\nPREFIX ncso:<http://www.semanticweb.org/ncso/>\nPREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX pext:<http://proton.semanticweb.org/protonext#>\nPREFIX OBO_REL:<http://purl.org/obo/owl/OBO_REL#>\nPREFIX oboInOwl:<http://www.geneontology.org/formats/oboInOwl#>\nPREFIX span:<http://www.ifomis.org/bfo/1.1/span#>  "


  def getCohort(filterData: String) = WithCors("GET") {
    Action {

      val json = Json.parse(filterData)

      val data = json.get("data")
      val anthro = json.get("anthro")
      val nicotine = json.get("nicotine")

      println(data)

      Ok("Hello")
    }
  }

  def getAll(data: String) = WithCors("GET") {
    Action {
      val json = Json.parse(data)
      val checked = (json.get("list").iterator() collect {
        case item: JsonNode if(item.has("isChecked") && item.get("isChecked").asBoolean() == true) => item.get("id").toString
      }).toTraversable.toList.map(_.replace("\"", ""))

      val query = SPARQLBuilder.buildQueryForAll(checked)

      val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + query.replace("\u00A0", " "))

      //accidentally used Java Json; convert back later
      Ok(SJson.toJson(
        Map(
          "sparqlQuery" -> SJson.toJson(query),
          "sparqlResults" -> SJson.toJson(resultRows)
        )
      ))
    }
  }

}
