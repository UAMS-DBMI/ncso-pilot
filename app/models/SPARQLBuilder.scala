package models

/**
 * Created by joshhanna on 3/16/14.
 */
object SPARQLBuilder {

  val surrogateDataQuery =
    """
      |    #surrogate data
      |    <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .
      |    ?surDataClass rdfs:label ?surDataType .
      |    ?surrogateData rdf:type ?surDataClass ;
      |    obo:OBI_0001938/obo:OBI_0001937 ?surDataValue ;
      |    obo:IAO_0000136 ?participant . """.stripMargin
  val anthroDataQuery =
    """
      |    #anthro data
      |    ?lengthData obo:IAO_0000136 ?participant ;
      |    rdf:type obo:NCSO_00000012 ;
      |    obo:OBI_0001938 [
      |      obo:OBI_0001937 ?lengthValue ;
      |      obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;
      |    ] .
      |
      |    ?weightData obo:IAO_0000136 ?participant ;
      |      rdf:type obo:NCSO_00000024 ;
      |      obo:OBI_0001938 [
      |        obo:OBI_0001937 ?weightValue ;
      |        obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;
      |      ] .
      |
      |    ?bmiData obo:IAO_0000136 ?weightData ;
      |    obo:IAO_0000136 ?heightData ;
      |    obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .
      |
      |    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight
      |    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height""".stripMargin
  val nicotineExposureDataQuery =
    """
      |    #nicotine exposure data
      |    ?participant ^obo:IAO_0000136 ?VS .
      |    ?VS rdf:type [
      |      rdfs:subClassOf [
      |        owl:onProperty obo:OBI_0001927 ;
      |        owl:someValuesFrom ?household
      |      ] ;
      |      rdfs:label ?VSLabel
      |    ] .
      |
      |    ?household rdfs:label ?participantType .
      |    FILTER (?household = obo:NCSO_00000051 || ?household = obo:NCSO_00000050)""".stripMargin

  val nicotineExposureHeader: String = "?participantType"
  val surrogateDataHeader: String = "?surDataType ?surDataValue"
  val anthroHeader: String = "?weight ?height"

  val smokingFilter = "    FILTER (?household = obo:NCSO_00000050)"
  val nonsmokingFilter = "    FILTER (?household = obo:NCSO_00000051)"

  val bmiFilter = "     FILTER(xsd:float(?bmi) %s %s) ."
  val lengthFilter = "    FILTER(xsd:float(?lengthValue) %s %s)"
  val weightFilter = "    FILTER(xsd:float(?weightValue) %s %s)"

  def buildQueryForAll(dataType: List[String]): String = {
    var headers = List[String]()
    var body = List[String]()

    if(dataType.contains("participantID")){
      headers ::= "?participantID"
    }
    if(dataType.contains("anthroData")){
      headers ::= anthroHeader
      body ::= anthroDataQuery
    }
    if(dataType.contains("generalHealthData")){
      headers ::= surrogateDataHeader
      body ::= surrogateDataQuery
    }
    if(dataType.contains("nicotineData")){
      headers ::= nicotineExposureHeader
      body ::= nicotineExposureDataQuery
    }


    val query =
      """select distinct """ + headers.mkString(" ") +
      """|  {
         |    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;
         |      rdfs:label ?participantID . """.stripMargin + body.mkString("\n") + "\n} LIMIT 10"

    return query
  }

  def buildCohortQuery(anthro: List[Map[String, String]], smoking: Option[String], data: List[String]): String = {
    var headers = List[String]()
    var body = Set[String]()
    var filters = Set[String]()

    //always want participant ID
    headers ::= "?participantID"

    //handling which data the user wants to return
    if(data.contains("anthroData")){
      headers ::= anthroHeader
      body += anthroDataQuery
    }
    if(data.contains("generalHealthData")){
      headers ::= surrogateDataHeader
      body += surrogateDataQuery
    }
    if(data.contains("nicotineData")){
      headers ::= nicotineExposureHeader
      body += nicotineExposureDataQuery
    }

    //handling smoking household filter
    if(smoking.nonEmpty){
      if(smoking.get == "smokingHousehold"){
        //need exposure data for filter to work
        body += nicotineExposureDataQuery
        filters += smokingFilter
      } else if(smoking.get == "nonSmokingHousehold") {
        //need exposure data for filter to work
        body += nicotineExposureDataQuery
        filters += nonsmokingFilter
      }
    }

    //handling anthro filter
    anthro.foreach(filter => {
      val filterID = filter("id")
      val operator = filter("operator")
      val value = filter("value")

      if(filterID == "bmi"){
        //need anthro data for filter to work
        body += anthroDataQuery
        filters += bmiFilter.format(operator, value)
      }

      if(filterID == "length"){
        //need anthro dat for filter to work
        body += anthroDataQuery
        filters += lengthFilter.format(operator, value)
      }

      if(filterID == "weight"){
        //need anthro dat for filter to work
        body += anthroDataQuery
        filters += weightFilter.format(operator, value)
      }

    })

    //building query based on options
    val query =
      """select distinct """ + headers.mkString(" ") +
      """|  {
         |    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;
         |      rdfs:label ?participantID . """.stripMargin +
        body.mkString("\n") + "\n" +
        filters.mkString("\n") + "\n} LIMIT 10"

    return query
  }
}



