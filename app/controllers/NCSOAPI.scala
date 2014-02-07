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
    val repoID = "NCSOD"
    val list_of_APIS = List("getcurrentapis", "testsesameconnection")

    def listCurrentAPIS = WithCors("GET", "POST") {
      Action {
        Ok(Json.toJson(Map("currentAPIs" -> Json.toJson(list_of_APIS))))
      }
    }

    def sesameConnectionTest = WithCors("GET", "POST") {
      Action {
        val sparqlQuery = "SELECT  ?p (COUNT(DISTINCT ?o ) AS ?count ) { ?s ?p ?o } GROUP BY ?p ORDER BY ?count"
        val sqlQuery = "SELECT OwnerName, SUM(AmountPaid) AS Paid, SUM(AmountOwedComplete) AS Owed, SUM(AmountOwedThisMonth) AS OwedMonth,\n\tSUM(PaidForPast) AS PaidPast, SUM(PaidForPresent) AS PaidPresent, SUM((AmountPaid - PaidForPast - PaidForPresent)) AS PaidFuture, [Description] FROM (\n\tSELECT OwnerName, AmountPaid, AmountOwedComplete, AmountOwedThisMonth, PaidForPast, [Description],\n\t\t(SELECT CASE WHEN (AmountPaid - PaidForPast) < ABS(AmountOwedThisMonth) THEN AmountPaid - PaidForPast\n\t\t\tELSE ABS(AmountOwedThisMonth) END) AS PaidForPresent\n\tFROM (\n\t\tSELECT OwnerName, AmountPaid, AmountOwedTotal - AmountPaid AS AmountOwedComplete,\n\t\t\tAmountOwedThisMonth, \n\t\t\t(SELECT CASE WHEN (AmountPaid < ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth)\n\t\t\t\tTHEN AmountPaid ELSE ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth END) AS PaidForPast,\t\n\t\t\tDescription, TransactionDate\n\t\t FROM (\n\t\t\tSELECT DISTINCT t.TenantName, p.PropertyName, ISNULL(p.OwnerName, 'Uknown') AS OwnerName, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\tAmount > 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t\t\tAND TenantID = t.ID AND TransactionCode = trans.TransactionCode\n\t\t\t) AS AmountPaid, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\ttblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t)  AS AmountOwedTotal, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE  tblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t\t\tAND Amount < 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t) AS AmountOwedThisMonth, code.Description, trans.TransactionDate FROM tblTransaction trans \n\t\t\tLEFT JOIN tblTenantTransCode code ON code.ID = trans.TransactionCode\n\t\t\tLEFT JOIN tblTenant t ON t.ID = trans.TenantID\n\t\t\tLEFT JOIN tblProperty p ON t.PropertyID  = p.ID\n\t\t\tWHERE trans.TransactionDate >= @StartDate AND trans.TransactionDate <= @EndDate AND trans.Amount > 0\n\t\t) q\n\t) q2\n)q3\nGROUP BY OwnerName, Description"
        val explanation = "Let P be a connected, weighted graph. At every iteration of Prim's algorithm, an edge must be found that connects a vertex in a subgraph to a vertex outside the subgraph. Since P is connected, there will always be a path to every vertex. The output Y of Prim's algorithm is a tree, because the edge and vertex added to tree Y are connected. Let Y1 be a minimum spanning tree of graph P. If Y1=Y then Y is a minimum spanning tree. Otherwise, let e be the first edge added during the construction of tree Y that is not in tree Y1, and V be the set of vertices connected by the edges added before edge e. Then one endpoint of edge e is in set V and the other is not. Since tree Y1 is a spanning tree of graph P, there is a path in tree Y1 joining the two endpoints. As one travels along the path, one must encounter an edge f joining a vertex in set V to one that is not in set V. Now, at the iteration when edge e was added to tree Y, edge f could also have been added and it would be added instead of edge e if its weight was less than e, and since edge f was not added, we conclude that\nw(f)\\geq w(e).\nLet tree Y2 be the graph obtained by removing edge f from and adding edge e to tree Y1. It is easy to show that tree Y2 is connected, has the same number of edges as tree Y1, and the total weights of its edges is not larger than that of tree Y1, therefore it is also a minimum spanning tree of graph P and it contains edge e and all the edges added before it during the construction of set V. Repeat the steps above and we will eventually obtain a minimum spanning tree of graph P that is identical to tree Y. This shows Y is a minimum spanning tree."

        SesameSparql2Json.openConnection(sesameUrl, repoID)
        val resultRows : List[Map[String, String]] = SesameSparql2Json.getResultRowsFromSPARQLQuery(sparqlQuery)
        val resultCols : Map[String, List[String]] = SesameSparql2Json.getResultColumnMapFromSPARQLQuery(sparqlQuery)
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
