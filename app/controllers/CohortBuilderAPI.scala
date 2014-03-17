package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.json.JsValue
import actions.WithCors
import play.libs.Json
import scala.collection.JavaConversions._
import com.fasterxml.jackson.databind.JsonNode
import models.SPARQLBuilder

/**
 * Created by joshhanna on 3/16/14.
 */
object CohortBuilderAPI extends Controller {

  def getCohort(filterData: String) = WithCors("GET") {
    Action {

      val json = Json.parse(filterData)

      val data = json.get("data")
      val anthro = json.get("anthro")
      val nicotine = json.get("nicotine")

      anthro.get("params").iterator().foreach(param => {
        if(param.has("isChecked") && param.get("isChecked").asBoolean() == true){
          println(param.get("id") + " " +  param.get("comparisonOperator").toString + " " + param.get("value") )
        }
      })

      Ok("Hello")
    }
  }

  def getAll(data: String) = WithCors("GET") {
    Action {
      val json = Json.parse(data)
      val checked = (json.get("list").iterator() collect {
        case item: JsonNode if(item.has("isChecked") && item.get("isChecked").asBoolean() == true) => item.get("id").toString
      }).toTraversable.toList
      println(checked)

      SPARQLBuilder.buildQueryForAll(checked)

      Ok("Hello")
    }
  }

}
