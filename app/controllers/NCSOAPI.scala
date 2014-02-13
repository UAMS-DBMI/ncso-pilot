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
    val mapOfAPIs = Map(//"Simple Test Query" -> "testsesameconnection",
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
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n} LIMIT 10".replace("\u00A0", " ")
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query gets the ID, the BMI, the weight, and the height of all participants in the study."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID ;\n                 ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n     \n    FILTER NOT EXISTS { #filter out those who are have been in smoker households\n        ?participant ^obo:IAO_0000136 [\n            rdf:type [\n                rdfs:subClassOf [\n                    owl:onProperty obo:OBI_0001927 ;\n                    owl:someValuesFrom obo:NCSO_00000050\n                ]\n            ]     \n        ]\n    }\n} LIMIT 10".replace("\u00A0", " ")
        val sqlQuery = "select A.* , \n       B.smoke \"bv1.1_smoke\", \n       C.smoke \"bv1.2_smoke\", \n       D.smoke \"bv1.3_smoke\", \n       E.smoke \"bv1.4_smoke\", \n       F.smoke \"bvli.1_smoke\",\n       G.smoke \"bvli.2_smoke\", \n       H.smoke \"cqs.1_smoke\",\n       H.smoke_home \"cqs.1_smoke_home\",\n       H.smoke_rules \"cqs.1_smoke_rules\",\n       H.smoke_other \"cqs.1_smoke_other\",\n       I.smoke \"cqs.2_smoke\",\n       I.smoke_home \"cqs.2_smoke_home\",\n       I.smoke_rules \"cqs.2_smoke_rules\",\n       I.smoke_other \"cqs.2_smoke_other\", \n       J.R_SMOKE \"18mm.1_r_smoke\", \n       J.NUM_SMOKER \"18mm.1_num_smoker\", \n       J.smoke_rules \"18mm.1_smoke_rules\", \n       J.smoke_other \"18mm.1_smoke_other\",\n       M.smoke_hours \"18mm.1_smoke_hours\",\n       K.R_SMOKE \"18mm.2_r_smoke\", \n       K.NUM_SMOKER \"18mm.2_num_smoker\", \n       K.smoke_rules \"18mm.2_smoke_rules\", \n       K.smoke_other \"18mm.2_smoke_other\",\n       N.smoke_hours \"18mm.2_smoke_hours\",\n       L.R_SMOKE \"18mm.3_r_smoke\", \n       L.NUM_SMOKER \"18mm.3_num_smoker\", \n       L.smoke_rules \"18mm.3_smoke_rules\", \n       L.smoke_other \"18mm.3_smoke_other\",\n       O.smoke_hours \"18mm.3_smoke_hours\", \n       P.R_SMOKE \"18mm.4_r_smoke\", \n       P.NUM_SMOKER \"18mm.4_num_smoker\", \n       P.smoke_rules \"18mm.4_smoke_rules\", \n       P.smoke_hours \"18mm.4_smoke_hours\",\n       Q.cig_now \"fat_pv1.1_cig_now\",\n       R.cig_now \"fat_pv1.2_cig_now\", \n       S.CIG_PAST \"pv1saq.1_cig_past\", \n       S.CIG_PAST_FREQ \"pv1saq.1_cig_past_freq\",  \n       S.CIG_PAST_NUM \"pv1saq.1_cig_past_num\", \n       S.CIG_NOW \"pv1saq.1_cig_now\",  \n       S.CIG_NOW_FREQ \"pv1saq.1_cig_now_freq\",  \n       S.CIG_NOW_NUM \"pv1saq.1_cig_now_num\",  \n       T.CIG_PAST \"pv1saq.2_cig_past\", \n       T.CIG_PAST_FREQ \"pv1saq.2_cig_past_freq\",  \n       T.CIG_PAST_NUM \"pv1saq.2_cig_past_num\", \n       T.CIG_NOW \"pv1saq.2_cig_now\",  \n       T.CIG_NOW_FREQ \"pv1saq.2_cig_now_freq\",  \n       T.CIG_NOW_NUM \"pv1saq.2_cig_now_num\",  \n       U.CIG_PAST \"pv1saq.3_cig_past\", \n       U.CIG_PAST_FREQ \"pv1saq.3_cig_past_freq\",  \n       U.CIG_PAST_NUM \"pv1saq.3_cig_past_num\", \n       U.CIG_NOW \"pv1saq.3_cig_now\",  \n       U.CIG_NOW_FREQ \"pv1saq.3_cig_now_freq\",  \n       U.CIG_NOW_NUM \"pv1saq.3_cig_now_num\", \n       V.CIG_PAST \"pv1saq.4_cig_past\", \n       V.CIG_PAST_FREQ \"pv1saq.4_cig_past_freq\",  \n       V.CIG_PAST_NUM \"pv1saq.4_cig_past_num\", \n       V.CIG_NOW \"pv1saq.4_cig_now\",  \n       V.CIG_NOW_FREQ \"pv1saq.4_cig_now_freq\",  \n       V.CIG_NOW_NUM \"pv1saq.4_cig_now_num\", \n       W.CIG_PAST \"pv1saq.5_cig_past\", \n       W.CIG_PAST_FREQ \"pv1saq.5_cig_past_freq\",  \n       W.CIG_PAST_NUM \"pv1saq.5_cig_past_num\", \n       W.CIG_NOW \"pv1saq.5_cig_now\",  \n       W.CIG_NOW_FREQ \"pv1saq.5_cig_now_freq\",  \n       W.CIG_NOW_NUM \"pv1saq.5_cig_now_num\",      \n       X.CIG_NOW \"pvli.1_cig_now\",  \n       X.CIG_NOW_FREQ \"pvli.1_cig_now_freq\",  \n       X.CIG_NOW_NUM \"pvli.1_cig_now_num\",\n       Y.CIG_NOW \"pvli.1_cig_now\",  \n       Y.CIG_NOW_FREQ \"pvli.1_cig_now_freq\",  \n       Y.CIG_NOW_NUM \"pvli.1_cig_now_num\", \n       Z.CIG_NOW \"6mm.1_cig_now\", \n       Z.NUM_SMOKER \"6mm.1_num_smoker\", \n       Z.SMOKE_INSIDE \"6mm.1_smoke_inside\", \n       Z.SMOKE_RULES \"6mm.1_smoke_rules\", \n       Z.SMOKE_HOURS \"6mm.1_smoke_hours\",\n       AA.SMOKE_HOURS \"6mm.1_smoke_hours_other\",\n       AB.CIG_NOW \"6mm.2_cig_now\", \n       AB.NUM_SMOKER \"6mm.2_num_smoker\", \n       AB.SMOKE_INSIDE \"6mm.2_smoke_inside\", \n       AB.SMOKE_RULES \"6mm.2_smoke_rules\", \n       AB.SMOKE_HOURS \"6mm.2_smoke_hours\", \n\t   AE.CIG_NOW \"6mm.3_cig_now\", \n       AE.NUM_SMOKER \"6mm.3_num_smoker\", \n       AE.SMOKE_INSIDE \"6mm.3_smoke_inside\", \n       AE.SMOKE_RULES \"6mm.3_smoke_rules\", \n       AE.SMOKE_HOURS \"6mm.3_smoke_hours\", \n       AF.CIG_NOW \"12mm.1_cig_now\",  \n       AF.NUM_SMOKER \"12mm.1_num_smoker\", \n       AF.SMOKE_RULES \"12mm.1_smoke_rules\",  \n       AG.smoke_hours \"12mm.1_smoke_hours\",\n       AH.CIG_NOW \"12mm.2_cig_now\",  \n       AH.NUM_SMOKER \"12mm.2_num_smoker\", \n       AH.SMOKE_RULES \"12mm.2_smoke_rules\",  \n       AI.smoke_hours \"12mm.2_smoke_hours\",\n       AJ.CIG_NOW \"12mm.3_cig_now\",  \n       AJ.NUM_SMOKER \"12mm.3_num_smoker\", \n       AJ.SMOKE_RULES \"12mm.3_smoke_rules\",  \n       AK.smoke_hours \"12mm.3_smoke_hours\", \n       AL.R_SMOKE \"24mm.1_r_smoke\", \n       AL.NUM_SMOKER \"24mm.1_num_smoker\",        \n       AL.SMOKE_RULES \"24mm.1_smoke_rules\",         \n       AM.smoke_hours \"24mm.1_smoke_hours\",\n       AN.R_SMOKE \"24mm.2_r_smoke\", \n       AN.NUM_SMOKER \"24mm.2_num_smoker\",        \n       AN.SMOKE_RULES \"24mm.2_smoke_rules\",         \n       AO.smoke_hours \"24mm.2_smoke_hours\", \n       AP.R_SMOKE \"24mm.3_r_smoke\", \n       AP.NUM_SMOKER \"24mm.3_num_smoker\",        \n       AP.SMOKE_RULES \"24mm.3_smoke_rules\",         \n       AQ.smoke_hours \"24mm.3_smoke_hours\",        \n       AT.R_SMOKE \"24mm.4_r_smoke\", \n       AT.NUM_SMOKER \"24mm.4_num_smoker\",        \n       AT.SMOKE_RULES \"24mm.4_smoke_rules\",         \n       AT.smoke_hours \"24mm.4_smoke_hours\"    \nFrom \n(select p_id From birth_visit\nunion\nselect p_id From birth_visit_2\nunion\nselect p_id From birth_visit_3\nunion\nselect p_id From birth_visit_4\nunion \nselect p_id From birth_visit_li\nunion \nselect p_id From birth_visit_li_2\nunion \nselect p_id From core_quest_smoke\nunion \nselect p_id From core_quest_smoke_2\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_2\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_3\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_4\nunion \nselect p_id From FATHER_PV1\nunion \nselect p_id From FATHER_PV1_2\nunion \nselect p_id From PREG_VISIT_1_SAQ\nunion \nselect p_id From PREG_VISIT_1_SAQ_2\nunion \nselect p_id From PREG_VISIT_1_SAQ_3\nunion \nselect p_id From PREG_VISIT_1_SAQ_4\nunion \nselect p_id From PREG_VISIT_1_SAQ_5\nunion \nselect p_id From PREG_VISIT_LI\nunion \nselect p_id From PREG_VISIT_LI_2\nunion \nselect p_id From SIX_MTH_MOTHER\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL\nunion \nselect p_id From SIX_MTH_MOTHER_2\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL_2\nunion \nselect p_id From SIX_MTH_MOTHER_3\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL_3\nunion \nselect p_id From TWELVE_MTH_MOTHER\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL\nunion \nselect p_id From TWELVE_MTH_MOTHER_2\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL_2\nunion \nselect p_id From TWELVE_MTH_MOTHER_3\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_2\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_2\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_4\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_4) A\nleft outer join \n(select * From birth_visit) B on A.p_id=B.p_id\nleft outer join \n(select * From birth_visit_2) C on A.p_id=C.p_id\nleft outer join \n(select * From birth_visit_3) D on A.p_id=D.p_id\nleft outer join \n(select * From birth_visit_4) E on A.p_id=E.p_id\nleft outer join \n(select * From birth_visit_li) F on A.p_id=E.p_id\nleft outer join \n(select * From birth_visit_li_2) G on A.p_id=G.p_id\nleft outer join \n(select * From core_quest_smoke) H on A.p_id=H.p_id\nleft outer join \n(select * From core_quest_smoke_2) I on A.p_id=I.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER) J on A.p_id=J.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_2) K on A.p_id=K.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_3) L on A.p_id=L.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS) M on A.p_id=M.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_2) N on A.p_id=N.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_3) O on A.p_id=O.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_4) P on A.p_id=P.p_id\nleft outer join \n(select * From FATHER_PV1) Q on A.p_id=Q.p_id\nleft outer join \n(select * From FATHER_PV1_2) R on A.p_id=R.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ) S on A.p_id=S.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_2) T on A.p_id=T.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_3) U on A.p_id=U.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_4) V on A.p_id=V.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_5) W on A.p_id=W.p_id\nleft outer join \n(select * From PREG_VISIT_LI) X on A.p_id=X.p_id\nleft outer join \n(select * From PREG_VISIT_LI_2) Y on A.p_id=Y.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER) Z on A.p_id=Z.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL) AA on A.p_id=AA.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_2) AB on A.p_id=AB.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL_2) AC on A.p_id=AC.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_3) AD on A.p_id=AD.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL_3) AE on A.p_id=AE.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER) AF on A.p_id=AF.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL) AG on A.p_id=AG.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_2) AH on A.p_id=AH.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL_2) AI on A.p_id=AI.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_3) AJ on A.p_id=AJ.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL_3) AK on A.p_id=AK.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER) AL on A.p_id=AL.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS) AM on A.p_id=AM.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_2) AN on A.p_id=AN.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_2) AO on A.p_id=AO.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_3) AP on A.p_id=AP.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_3) AQ on A.p_id=AQ.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_4) AR on A.p_id=AR.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_4) AT on A.p_id=AT.p_id\n"
        val explanation = "This SPARQL query returns all participants who are in a household that contains no smokers.  The SQL query displayed returns much of the data about nicotene exposure, but does not filter for those who do not live in a smoker household.  To do so would require checking each data element against the corresponding code list and manually filtering."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID ;\n                 ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n} LIMIT 10"
        val sqlQuery = "select A.* , \n       B.smoke \"bv1.1_smoke\", \n       C.smoke \"bv1.2_smoke\", \n       D.smoke \"bv1.3_smoke\", \n       E.smoke \"bv1.4_smoke\", \n       F.smoke \"bvli.1_smoke\",\n       G.smoke \"bvli.2_smoke\", \n       H.smoke \"cqs.1_smoke\",\n       H.smoke_home \"cqs.1_smoke_home\",\n       H.smoke_rules \"cqs.1_smoke_rules\",\n       H.smoke_other \"cqs.1_smoke_other\",\n       I.smoke \"cqs.2_smoke\",\n       I.smoke_home \"cqs.2_smoke_home\",\n       I.smoke_rules \"cqs.2_smoke_rules\",\n       I.smoke_other \"cqs.2_smoke_other\", \n       J.R_SMOKE \"18mm.1_r_smoke\", \n       J.NUM_SMOKER \"18mm.1_num_smoker\", \n       J.smoke_rules \"18mm.1_smoke_rules\", \n       J.smoke_other \"18mm.1_smoke_other\",\n       M.smoke_hours \"18mm.1_smoke_hours\",\n       K.R_SMOKE \"18mm.2_r_smoke\", \n       K.NUM_SMOKER \"18mm.2_num_smoker\", \n       K.smoke_rules \"18mm.2_smoke_rules\", \n       K.smoke_other \"18mm.2_smoke_other\",\n       N.smoke_hours \"18mm.2_smoke_hours\",\n       L.R_SMOKE \"18mm.3_r_smoke\", \n       L.NUM_SMOKER \"18mm.3_num_smoker\", \n       L.smoke_rules \"18mm.3_smoke_rules\", \n       L.smoke_other \"18mm.3_smoke_other\",\n       O.smoke_hours \"18mm.3_smoke_hours\", \n       P.R_SMOKE \"18mm.4_r_smoke\", \n       P.NUM_SMOKER \"18mm.4_num_smoker\", \n       P.smoke_rules \"18mm.4_smoke_rules\", \n       P.smoke_hours \"18mm.4_smoke_hours\",\n       Q.cig_now \"fat_pv1.1_cig_now\",\n       R.cig_now \"fat_pv1.2_cig_now\", \n       S.CIG_PAST \"pv1saq.1_cig_past\", \n       S.CIG_PAST_FREQ \"pv1saq.1_cig_past_freq\",  \n       S.CIG_PAST_NUM \"pv1saq.1_cig_past_num\", \n       S.CIG_NOW \"pv1saq.1_cig_now\",  \n       S.CIG_NOW_FREQ \"pv1saq.1_cig_now_freq\",  \n       S.CIG_NOW_NUM \"pv1saq.1_cig_now_num\",  \n       T.CIG_PAST \"pv1saq.2_cig_past\", \n       T.CIG_PAST_FREQ \"pv1saq.2_cig_past_freq\",  \n       T.CIG_PAST_NUM \"pv1saq.2_cig_past_num\", \n       T.CIG_NOW \"pv1saq.2_cig_now\",  \n       T.CIG_NOW_FREQ \"pv1saq.2_cig_now_freq\",  \n       T.CIG_NOW_NUM \"pv1saq.2_cig_now_num\",  \n       U.CIG_PAST \"pv1saq.3_cig_past\", \n       U.CIG_PAST_FREQ \"pv1saq.3_cig_past_freq\",  \n       U.CIG_PAST_NUM \"pv1saq.3_cig_past_num\", \n       U.CIG_NOW \"pv1saq.3_cig_now\",  \n       U.CIG_NOW_FREQ \"pv1saq.3_cig_now_freq\",  \n       U.CIG_NOW_NUM \"pv1saq.3_cig_now_num\", \n       V.CIG_PAST \"pv1saq.4_cig_past\", \n       V.CIG_PAST_FREQ \"pv1saq.4_cig_past_freq\",  \n       V.CIG_PAST_NUM \"pv1saq.4_cig_past_num\", \n       V.CIG_NOW \"pv1saq.4_cig_now\",  \n       V.CIG_NOW_FREQ \"pv1saq.4_cig_now_freq\",  \n       V.CIG_NOW_NUM \"pv1saq.4_cig_now_num\", \n       W.CIG_PAST \"pv1saq.5_cig_past\", \n       W.CIG_PAST_FREQ \"pv1saq.5_cig_past_freq\",  \n       W.CIG_PAST_NUM \"pv1saq.5_cig_past_num\", \n       W.CIG_NOW \"pv1saq.5_cig_now\",  \n       W.CIG_NOW_FREQ \"pv1saq.5_cig_now_freq\",  \n       W.CIG_NOW_NUM \"pv1saq.5_cig_now_num\",      \n       X.CIG_NOW \"pvli.1_cig_now\",  \n       X.CIG_NOW_FREQ \"pvli.1_cig_now_freq\",  \n       X.CIG_NOW_NUM \"pvli.1_cig_now_num\",\n       Y.CIG_NOW \"pvli.1_cig_now\",  \n       Y.CIG_NOW_FREQ \"pvli.1_cig_now_freq\",  \n       Y.CIG_NOW_NUM \"pvli.1_cig_now_num\", \n       Z.CIG_NOW \"6mm.1_cig_now\", \n       Z.NUM_SMOKER \"6mm.1_num_smoker\", \n       Z.SMOKE_INSIDE \"6mm.1_smoke_inside\", \n       Z.SMOKE_RULES \"6mm.1_smoke_rules\", \n       Z.SMOKE_HOURS \"6mm.1_smoke_hours\",\n       AA.SMOKE_HOURS \"6mm.1_smoke_hours_other\",\n       AB.CIG_NOW \"6mm.2_cig_now\", \n       AB.NUM_SMOKER \"6mm.2_num_smoker\", \n       AB.SMOKE_INSIDE \"6mm.2_smoke_inside\", \n       AB.SMOKE_RULES \"6mm.2_smoke_rules\", \n       AB.SMOKE_HOURS \"6mm.2_smoke_hours\", \n\t   AE.CIG_NOW \"6mm.3_cig_now\", \n       AE.NUM_SMOKER \"6mm.3_num_smoker\", \n       AE.SMOKE_INSIDE \"6mm.3_smoke_inside\", \n       AE.SMOKE_RULES \"6mm.3_smoke_rules\", \n       AE.SMOKE_HOURS \"6mm.3_smoke_hours\", \n       AF.CIG_NOW \"12mm.1_cig_now\",  \n       AF.NUM_SMOKER \"12mm.1_num_smoker\", \n       AF.SMOKE_RULES \"12mm.1_smoke_rules\",  \n       AG.smoke_hours \"12mm.1_smoke_hours\",\n       AH.CIG_NOW \"12mm.2_cig_now\",  \n       AH.NUM_SMOKER \"12mm.2_num_smoker\", \n       AH.SMOKE_RULES \"12mm.2_smoke_rules\",  \n       AI.smoke_hours \"12mm.2_smoke_hours\",\n       AJ.CIG_NOW \"12mm.3_cig_now\",  \n       AJ.NUM_SMOKER \"12mm.3_num_smoker\", \n       AJ.SMOKE_RULES \"12mm.3_smoke_rules\",  \n       AK.smoke_hours \"12mm.3_smoke_hours\", \n       AL.R_SMOKE \"24mm.1_r_smoke\", \n       AL.NUM_SMOKER \"24mm.1_num_smoker\",        \n       AL.SMOKE_RULES \"24mm.1_smoke_rules\",         \n       AM.smoke_hours \"24mm.1_smoke_hours\",\n       AN.R_SMOKE \"24mm.2_r_smoke\", \n       AN.NUM_SMOKER \"24mm.2_num_smoker\",        \n       AN.SMOKE_RULES \"24mm.2_smoke_rules\",         \n       AO.smoke_hours \"24mm.2_smoke_hours\", \n       AP.R_SMOKE \"24mm.3_r_smoke\", \n       AP.NUM_SMOKER \"24mm.3_num_smoker\",        \n       AP.SMOKE_RULES \"24mm.3_smoke_rules\",         \n       AQ.smoke_hours \"24mm.3_smoke_hours\",        \n       AT.R_SMOKE \"24mm.4_r_smoke\", \n       AT.NUM_SMOKER \"24mm.4_num_smoker\",        \n       AT.SMOKE_RULES \"24mm.4_smoke_rules\",         \n       AT.smoke_hours \"24mm.4_smoke_hours\"    \nFrom \n(select p_id From birth_visit\nunion\nselect p_id From birth_visit_2\nunion\nselect p_id From birth_visit_3\nunion\nselect p_id From birth_visit_4\nunion \nselect p_id From birth_visit_li\nunion \nselect p_id From birth_visit_li_2\nunion \nselect p_id From core_quest_smoke\nunion \nselect p_id From core_quest_smoke_2\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_2\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_3\nunion \nselect p_id From EIGHTEEN_MTH_MOTHER_4\nunion \nselect p_id From FATHER_PV1\nunion \nselect p_id From FATHER_PV1_2\nunion \nselect p_id From PREG_VISIT_1_SAQ\nunion \nselect p_id From PREG_VISIT_1_SAQ_2\nunion \nselect p_id From PREG_VISIT_1_SAQ_3\nunion \nselect p_id From PREG_VISIT_1_SAQ_4\nunion \nselect p_id From PREG_VISIT_1_SAQ_5\nunion \nselect p_id From PREG_VISIT_LI\nunion \nselect p_id From PREG_VISIT_LI_2\nunion \nselect p_id From SIX_MTH_MOTHER\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL\nunion \nselect p_id From SIX_MTH_MOTHER_2\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL_2\nunion \nselect p_id From SIX_MTH_MOTHER_3\nunion \nselect p_id From SIX_MTH_MOTHER_DETAIL_3\nunion \nselect p_id From TWELVE_MTH_MOTHER\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL\nunion \nselect p_id From TWELVE_MTH_MOTHER_2\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL_2\nunion \nselect p_id From TWELVE_MTH_MOTHER_3\nunion \nselect p_id From TWELVE_MTH_MOTHER_DETAIL_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_2\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_2\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_3\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_4\nunion\nselect p_id from TWENTY_FOUR_MTH_MOTHER_HABITS_4) A\nleft outer join \n(select * From birth_visit) B on A.p_id=B.p_id\nleft outer join \n(select * From birth_visit_2) C on A.p_id=C.p_id\nleft outer join \n(select * From birth_visit_3) D on A.p_id=D.p_id\nleft outer join \n(select * From birth_visit_4) E on A.p_id=E.p_id\nleft outer join \n(select * From birth_visit_li) F on A.p_id=E.p_id\nleft outer join \n(select * From birth_visit_li_2) G on A.p_id=G.p_id\nleft outer join \n(select * From core_quest_smoke) H on A.p_id=H.p_id\nleft outer join \n(select * From core_quest_smoke_2) I on A.p_id=I.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER) J on A.p_id=J.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_2) K on A.p_id=K.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_3) L on A.p_id=L.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS) M on A.p_id=M.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_2) N on A.p_id=N.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_3) O on A.p_id=O.p_id\nleft outer join \n(select * From EIGHTEEN_MTH_MOTHER_HABITS_4) P on A.p_id=P.p_id\nleft outer join \n(select * From FATHER_PV1) Q on A.p_id=Q.p_id\nleft outer join \n(select * From FATHER_PV1_2) R on A.p_id=R.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ) S on A.p_id=S.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_2) T on A.p_id=T.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_3) U on A.p_id=U.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_4) V on A.p_id=V.p_id\nleft outer join \n(select * From PREG_VISIT_1_SAQ_5) W on A.p_id=W.p_id\nleft outer join \n(select * From PREG_VISIT_LI) X on A.p_id=X.p_id\nleft outer join \n(select * From PREG_VISIT_LI_2) Y on A.p_id=Y.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER) Z on A.p_id=Z.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL) AA on A.p_id=AA.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_2) AB on A.p_id=AB.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL_2) AC on A.p_id=AC.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_3) AD on A.p_id=AD.p_id\nleft outer join \n(select * From SIX_MTH_MOTHER_DETAIL_3) AE on A.p_id=AE.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER) AF on A.p_id=AF.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL) AG on A.p_id=AG.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_2) AH on A.p_id=AH.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL_2) AI on A.p_id=AI.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_3) AJ on A.p_id=AJ.p_id\nleft outer join \n(select * From TWELVE_MTH_MOTHER_DETAIL_3) AK on A.p_id=AK.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER) AL on A.p_id=AL.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS) AM on A.p_id=AM.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_2) AN on A.p_id=AN.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_2) AO on A.p_id=AO.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_3) AP on A.p_id=AP.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_3) AQ on A.p_id=AQ.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_4) AR on A.p_id=AR.p_id\nleft outer join \n(select * From TWENTY_FOUR_MTH_MOTHER_HABITS_4) AT on A.p_id=AT.p_id\n"
        val explanation = "This SPARQL query returns all participants who are in a household that contains at least one smoker.  The SQL query displayed returns much of the data about nicotene exposure, but does not filter for those who live in a smoker household.  To do so would require checking each data element against the corresponding code list and manually filtering."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n    ?participant ^obo:IAO_0000136 ?VS .\n    ?VS rdf:type [\n        rdfs:subClassOf [\n            owl:onProperty obo:OBI_0001927 ;\n            owl:someValuesFrom obo:NCSO_00000051\n        ] ;\n        rdfs:label ?VSLabel\n    ]\n} LIMIT 10"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns the BMI, weight, and height of all participants who are in a household with no smokers."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n    ?participant ^obo:IAO_0000136 [\n        rdf:type [\n            rdfs:subClassOf [\n                owl:onProperty obo:OBI_0001927 ;\n                owl:someValuesFrom obo:NCSO_00000050\n            ]\n        ]\n    ] .\n} LIMIT 10"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns the BMI, weight, and height of all participants that are in a household that contains at least one smoker."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID ?bmi ?weight ?height\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n   \n    ?lengthData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000012 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?lengthValue ;\n                    obo:IAO_0000039 [ rdfs:label ?lengthUnitLabel ] ;\n                ] .\n   \n    ?weightData obo:IAO_0000136 ?participant ;\n                rdf:type obo:NCSO_00000024 ;\n                obo:OBI_0001938 [\n                    obo:OBI_0001937 ?weightValue ;\n                    obo:IAO_0000039 [ rdfs:label ?weightUnitLabel ] ;\n                ] .\n   \n    ?bmiData obo:IAO_0000136 ?weightData ;\n             obo:IAO_0000136 ?heightData ;\n             obo:OBI_0001938 [ obo:OBI_0001937 ?bmi ] .\n   \n    bind(concat(?weightValue, ' ', ?weightUnitLabel, 's') AS ?weight) #pretty printing of the weight\n    bind(concat(?lengthValue, ' ', ?lengthUnitLabel, 's') AS ?height) #pretty printing of the height\n     \n     \n    ?participant ^obo:IAO_0000136 [\n        rdf:type [\n            rdfs:subClassOf [\n                owl:onProperty obo:OBI_0001927 ;\n                owl:someValuesFrom obo:NCSO_00000051\n            ]\n        ]\n    ] .\n   \n    FILTER NOT EXISTS { #filter out those who are have been in smoker households\n        ?participant ^obo:IAO_0000136 [\n            rdf:type [\n                rdfs:subClassOf [\n                    owl:onProperty obo:OBI_0001927 ;\n                    owl:someValuesFrom obo:NCSO_00000050\n                ]\n            ]\n        ]\n    }\n   \n    FILTER(xsd:float(?bmi) < 15) #filter out those with a bmi > 15        \n} LIMIT 10"
        val sqlQuery = "SELECT noreally.id FROM TODO as noreally inner join thisIsAPlaceHolder ifyouhaventnoticedyet on noreally.id = thisIsAPlaceHolder.id"
        val explanation = "This query returns all participants who have a BMI that is less than 15 and live in a household which contains at least one smoker."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID ?surDataLabel ?surrogateValue ?surrogateLabel\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n     \n    <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .\n    ?surDataClass rdfs:label ?surDataLabel .\n    ?surrogateData rdf:type ?surDataClass ;\n                   obo:OBI_0001938/obo:OBI_0001937 ?surrogateValue ;\n                   obo:IAO_0000136 ?participant\n    OPTIONAL { ?surrogateData obo:OBI_0001938/obo:IAO_0000039/rdfs:label ?surrogateLabel  }\n   \n} LIMIT 10"
        val sqlQuery = "We know of no equivalent SQL query."
        val explanation = "This query returns all data that is annotated as being a surrogate for general health."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
        val sparqlQuery = "select distinct ?participantID ?surDataLabel\n{\n    ?participant rdf:type <http://www.semanticweb.org/semanticweb.org/ncso/NCSO_00000085> ;\n                 rdfs:label ?participantID .\n     \n    <http://purl.obolibrary.org/obo/NCSO.owl/EXPOSD_00000002> owl:equivalentClass/owl:unionOf/rdf:first*/rdf:rest*/rdf:first/owl:someValuesFrom ?surDataClass .\n    ?surDataClass rdfs:label ?surDataLabel .\n     \n} LIMIT 10"
        val sqlQuery = "We know of no equivalent SQL query."
        val explanation = "This query returns all participants that have data associated with them that is annotated as being a surrogate for general health, along with the actual type of the data."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sesamePrefixes + sparqlQuery.replace("\u00A0", " "))
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
