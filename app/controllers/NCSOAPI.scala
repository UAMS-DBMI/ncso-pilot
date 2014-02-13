package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._

import models.SesameSparql2Json
import play.mvc.With
import actions.WithCors
import scala.collection.immutable.Map
import models.SesameSparql2Json._

object NCSOAPI extends Controller {
    val sesameUrl = "http://144.30.12.10:8080/openrdf-sesame"
    val repoID = "NCSODT"
    val sesamePrefixes = "PREFIX dc:<http://purl.org/dc/elements/1.1/>\nPREFIX PATO:<http://purl.org/obo/owl/PATO#>\nPREFIX :<http://www.ifomis.org/bfo/1.1#>\nPREFIX ro:<http://www.obofoundry.org/ro/ro.owl#>\nPREFIX protege:<http://protege.stanford.edu/plugins/owl/protege#>\nPREFIX ncso2:<http://www.semanticweb.org/semanticweb.org/ncso/>\nPREFIX UO:<http://purl.org/obo/owl/UO#>\nPREFIX ncso3:<http://purl.obolibrary.org/obo/ncso/dev/ncso.owl/>\nPREFIX snap:<http://www.ifomis.org/bfo/1.1/snap#>\nPREFIX bfo:<http://www.ifomis.org/bfo/1.1#>\nPREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\nPREFIX obo:<http://purl.obolibrary.org/obo/>\nPREFIX obo2:<http://purl.obolibrary.org/obo#>\nPREFIX psys:<http://proton.semanticweb.org/protonsys#>\nPREFIX ncso:<http://www.semanticweb.org/ncso/>\nPREFIX xsd:<http://www.w3.org/2001/XMLSchema#>\nPREFIX owl:<http://www.w3.org/2002/07/owl#>\nPREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX pext:<http://proton.semanticweb.org/protonext#>\nPREFIX OBO_REL:<http://purl.org/obo/owl/OBO_REL#>\nPREFIX oboInOwl:<http://www.geneontology.org/formats/oboInOwl#>\nPREFIX span:<http://www.ifomis.org/bfo/1.1/span#>  "
    val mapOfAPIs = Map("Simple Test Query" -> "testsesameconnection",
      "General health metrics for all participants" -> "getGeneralHealthMetricsForAllParticipants",
      "Participants in non-smoking households" -> "getParticipantsInNonSmokingHouseholds",
      "Participants in smoking households" -> "getParticipantsInSmokingHouseholds",
      "General health metrics for non-smoking households" -> "getGeneralHealthMetricsForNonSmokingHouseholds",
      "General health metrics for smoking households" -> "getGeneralHealthMetricsForSmokingHouseHolds",
      "General health metrics for participants with a BMI of less than 15 in smoking households" -> "getGeneralHealthMetricsForParticipantsInSmokingHouseholdsLessThanFifteenBMI",
      "General health surrogate data for all participants" -> "getGeneralHealthAndSurrogateDataForAllParticipants",
      "Participants with general health surrogate data" -> "getGeneralHealthDataForAllParticipants" )

    def listCurrentAPIS = WithCors("GET") {
      Action {
        Ok(Json.toJson(Json.toJson(mapOfAPIs)))
      }
    }

    def sesameConnectionTest = WithCors("GET") {
      Action {
        val sparqlQuery = "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count".replace("\u00A0", " ")
        val sqlQuery = "SELECT OwnerName, SUM(AmountPaid) AS Paid, SUM(AmountOwedComplete) AS Owed, SUM(AmountOwedThisMonth) AS OwedMonth,\n\tSUM(PaidForPast) AS PaidPast, SUM(PaidForPresent) AS PaidPresent, SUM((AmountPaid - PaidForPast - PaidForPresent)) AS PaidFuture, [Description] FROM (\n\tSELECT OwnerName, AmountPaid, AmountOwedComplete, AmountOwedThisMonth, PaidForPast, [Description],\n\t\t(SELECT CASE WHEN (AmountPaid - PaidForPast) < ABS(AmountOwedThisMonth) THEN AmountPaid - PaidForPast\n\t\t\tELSE ABS(AmountOwedThisMonth) END) AS PaidForPresent\n\tFROM (\n\t\tSELECT OwnerName, AmountPaid, AmountOwedTotal - AmountPaid AS AmountOwedComplete,\n\t\t\tAmountOwedThisMonth, \n\t\t\t(SELECT CASE WHEN (AmountPaid < ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth)\n\t\t\t\tTHEN AmountPaid ELSE ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth END) AS PaidForPast,\t\n\t\t\tDescription, TransactionDate\n\t\t FROM (\n\t\t\tSELECT DISTINCT t.TenantName, p.PropertyName, ISNULL(p.OwnerName, 'Uknown') AS OwnerName, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\tAmount > 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t\t\tAND TenantID = t.ID AND TransactionCode = trans.TransactionCode\n\t\t\t) AS AmountPaid, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\ttblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t)  AS AmountOwedTotal, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE  tblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t\t\tAND Amount < 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t) AS AmountOwedThisMonth, code.Description, trans.TransactionDate FROM tblTransaction trans \n\t\t\tLEFT JOIN tblTenantTransCode code ON code.ID = trans.TransactionCode\n\t\t\tLEFT JOIN tblTenant t ON t.ID = trans.TenantID\n\t\t\tLEFT JOIN tblProperty p ON t.PropertyID  = p.ID\n\t\t\tWHERE trans.TransactionDate >= @StartDate AND trans.TransactionDate <= @EndDate AND trans.Amount > 0\n\t\t) q\n\t) q2\n)q3\nGROUP BY OwnerName, Description"
        val explanation = "Let P be a connected, weighted graph. At every iteration of Prim's algorithm, an edge must be found that connects a vertex in a subgraph to a vertex outside the subgraph. Since P is connected, there will always be a path to every vertex. The output Y of Prim's algorithm is a tree, because the edge and vertex added to tree Y are connected. Let Y1 be a minimum spanning tree of graph P. If Y1=Y then Y is a minimum spanning tree. Otherwise, let e be the first edge added during the construction of tree Y that is not in tree Y1, and V be the set of vertices connected by the edges added before edge e. Then one endpoint of edge e is in set V and the other is not. Since tree Y1 is a spanning tree of graph P, there is a path in tree Y1 joining the two endpoints. As one travels along the path, one must encounter an edge f joining a vertex in set V to one that is not in set V. Now, at the iteration when edge e was added to tree Y, edge f could also have been added and it would be added instead of edge e if its weight was less than e, and since edge f was not added, we conclude that\nw(f)\\geq w(e).\nLet tree Y2 be the graph obtained by removing edge f from and adding edge e to tree Y1. It is easy to show that tree Y2 is connected, has the same number of edges as tree Y1, and the total weights of its edges is not larger than that of tree Y1, therefore it is also a minimum spanning tree of graph P and it contains edge e and all the edges added before it during the construction of set V. Repeat the steps above and we will eventually obtain a minimum spanning tree of graph P that is identical to tree Y. This shows Y is a minimum spanning tree."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))
      }
    }

    def getGeneralHealthMetricsForAllParticipants = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n}".replace("\u00A0", " ")
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query gets the ID, the BMI, the weight, and the height of all participants in the study."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getParticipantsInNonSmokingHouseholds = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID ;\n                 ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n     \n    FILTER NOT EXISTS { #filter out those who are have been in smoker households\n        ?participant ^obo:IAO_0000136 [\n            rdf:type [\n                rdfs:subClassOf [\n                    owl:onProperty obo:OBI_0001927 ;\n                    owl:someValuesFrom obo:NCSO_00000050\n                ]\n            ]     \n        ]\n    }\n}".replace("\u00A0", " ")
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all participants who are in a household that contains no smokers."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getParticipantsInSmokingHouseholds = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participant ?participantID\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID ;\n                 ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all participants who are in a household that contains at least one smoker."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getGeneralHealthMetricsForNonSmokingHouseholds = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n    ?participant ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns the BMI, weight, and height of all participants who are in a household with no smokers."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getGeneralHealthMetricsForSmokingHouseHolds = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n    ?participant ^obo:IAO_0000136 [\n        rdf:type [\n            rdfs:subClassOf [\n                owl:onProperty obo:OBI_0001927 ;\n                owl:someValuesFrom obo:NCSO_00000050\n            ]\n        ]\n    ] .\n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns the BMI, weight, and height of all participants that are in a household that contains at least one smoker."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getGeneralHealthMetricsForParticipantsInSmokingHouseholdsLessThanFifteenBMI = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participant ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n     \n    ?participant ^obo:IAO_0000136 [\n        rdf:type [\n            rdfs:subClassOf [\n                owl:onProperty obo:OBI_0001927 ;\n                owl:someValuesFrom obo:NCSO_00000051\n            ]\n        ]\n    ] .\n   \n    FILTER NOT EXISTS { #filter out those who are have been in smoker households\n        ?participant ^obo:IAO_0000136 [\n            rdf:type [\n                rdfs:subClassOf [\n                    owl:onProperty obo:OBI_0001927 ;\n                    owl:someValuesFrom obo:NCSO_00000050\n                ]\n            ]\n        ]\n    }\n   \n    FILTER(xsd:float(?bmi) < 15) #filter out those with a bmi > 15        \n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all participants who have a BMI that is less than 15 and live in a household which contains at least one smoker."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getGeneralHealthAndSurrogateDataForAllParticipants = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID ?surDataLabel ?surrogateValue ?surrogateLabel\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n     \n    <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .\n    ?surDataClass rdfs:label ?surDataLabel .\n    ?surrogateData rdf:type ?surDataClass ;\n                   obo:OBI_0001938/obo:OBI_0001937 ?surrogateValue ;\n                   obo:IAO_0000136 ?participant\n    OPTIONAL { ?surrogateData obo:OBI_0001938/obo:IAO_0000039/rdfs:label ?surrogateLabel  }\n   \n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all data that is annotated as being a surrogate for general health."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }

    def getGeneralHealthDataForAllParticipants = WithCors("GET") {
      Action {
        val sparqlQuery = "select distinct ?participantID ?surDataLabel\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n     \n    <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .\n    ?surDataClass rdfs:label ?surDataLabel .\n     \n}"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all participants that have data associated with them that is annotated as being a surrogate for general health, along with the actual type of the data."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery)
        SesameSparql2Json.closeConnection()

        Ok(Json.toJson(
          Map(
            "sparqlQuery" -> Json.toJson(sparqlQuery),
            "sparqlResults" -> Json.toJson(resultRows),
            "sqlQuery" -> Json.toJson(sqlQuery),
            "explanation" -> Json.toJson(explanation)
          )
        ))

      }
    }
}
