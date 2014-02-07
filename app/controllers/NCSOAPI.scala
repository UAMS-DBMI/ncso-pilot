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
        val sqlQuery = "SELECT OwnerName, SUM(AmountPaid) AS Paid, SUM(AmountOwedComplete) AS Owed, SUM(AmountOwedThisMonth) AS OwedMonth,\n\tSUM(PaidForPast) AS PaidPast, SUM(PaidForPresent) AS PaidPresent, SUM((AmountPaid - PaidForPast - PaidForPresent)) AS PaidFuture, [Description] FROM (\n\tSELECT OwnerName, AmountPaid, AmountOwedComplete, AmountOwedThisMonth, PaidForPast, [Description],\n\t\t(SELECT CASE WHEN (AmountPaid - PaidForPast) < ABS(AmountOwedThisMonth) THEN AmountPaid - PaidForPast\n\t\t\tELSE ABS(AmountOwedThisMonth) END) AS PaidForPresent\n\tFROM (\n\t\tSELECT OwnerName, AmountPaid, AmountOwedTotal - AmountPaid AS AmountOwedComplete,\n\t\t\tAmountOwedThisMonth, \n\t\t\t(SELECT CASE WHEN (AmountPaid < ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth)\n\t\t\t\tTHEN AmountPaid ELSE ABS((AmountOwedTotal - AmountPaid)) + AmountOwedThisMonth END) AS PaidForPast,\t\n\t\t\tDescription, TransactionDate\n\t\t FROM (\n\t\t\tSELECT DISTINCT t.TenantName, p.PropertyName, ISNULL(p.OwnerName, 'Uknown') AS OwnerName, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\tAmount > 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t\t\tAND TenantID = t.ID AND TransactionCode = trans.TransactionCode\n\t\t\t) AS AmountPaid, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE \n\t\t\t\t\ttblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t)  AS AmountOwedTotal, (\n\t\t\t\tSELECT SUM(Amount) FROM tblTransaction WHERE  tblTransaction.TransactionCode = trans.TransactionCode AND tblTransaction.TenantID = t.ID\n\t\t\t\t\tAND Amount < 0 AND TransactionDate >= @StartDate AND TransactionDate <= @EndDate\n\t\t\t) AS AmountOwedThisMonth, code.Description, trans.TransactionDate FROM tblTransaction trans \n\t\t\tLEFT JOIN tblTenantTransCode code ON code.ID = trans.TransactionCode\n\t\t\tLEFT JOIN tblTenant t ON t.ID = trans.TenantID\n\t\t\tLEFT JOIN tblProperty p ON t.PropertyID  = p.ID\n\t\t\tWHERE trans.TransactionDate >= @StartDate AND trans.TransactionDate <= @EndDate AND trans.Amount > 0\n\t\t) q\n\t) q2\n)q3\nGROUP BY OwnerName, Description"

        SesameDAO.initializeRepo()
        val resultRows : List[Map[String, String]] = SesameDAO.getResultRowsFromSPARQLQuery(sparqlQuery)
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
