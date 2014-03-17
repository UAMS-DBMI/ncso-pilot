package models

/**
 * Created by joshhanna on 3/16/14.
 */
object SPARQLBuilder {
  def buildQueryForAll(dataType: List[String]): String = {

    select distinct ?participantID ?surDataLabel ?surrogateValue ?bmi ?weight ?height
      {
        ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;
        rdfs:label ?participantID .

        <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .
        ?surDataClass rdfs:label ?surDataLabel .
      ?surrogateData rdf:type ?surDataClass ;
        obo:OBI_0001938/obo:OBI_0001937 ?surrogateValue ;
        obo:IAO_0000136 ?participant .

        ?lengthData obo:IAO_0000136 ?participant ;
        rdf:type obo:NCSO_00000012 ;
        obo:OBI_0001938 [
          obo:OBI_0001937 ?lengthValue ;
        obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;
        ] .

        ?weightData obo:IAO_0000136 ?participant ;
        rdf:type obo:NCSO_00000024 ;
        obo:OBI_0001938 [
          obo:OBI_0001937 ?weightValue ;
        obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;
        ] .

        ?bmiData obo:IAO_0000136 ?weightData ;
        obo:IAO_0000136 ?heightData ;
        obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .

        bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight
        bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height

        OPTIONAL { ?surrogateData obo:OBI_0001938/obo:IAO_0000039/rdfs:label ?surrogateLabel  }

      }

    select distinct ?participantID ?householdType
      {
        ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;
        rdfs:label ?participantID ;
        ^obo:IAO_0000136 ?VS .
        ?VS rdf:type [
        rdfs:subClassOf [
          owl:onProperty obo:OBI_0001927 ;
        owl:someValuesFrom ?household
        ] ;
        rdfs:label ?VSLabel
        ] .

        ?household rdfs:label ?householdType .
        FILTER (?household = obo:NCSO_00000051 || ?household = obo:NCSO_00000050)
      }

  }

}
