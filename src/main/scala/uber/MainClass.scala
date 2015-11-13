package uber

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
  * Created by prashant on 13/11/15.
  */
object MainClass {

  private val username = "kalkar.prashant@gmail.com"
  private val password = "prash1prash"
  private val from = LocalDate.parse("01/11/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val to = LocalDate.parse("13/11/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val paymentMethod = "payt"
  private val invoiceFileLocation = "receipt.pdf"
  private val driver = "FIREFOX" //other one is CHROME

  private val invoiceDownloader = InvoiceDownloader(username, password, invoiceFileLocation, driver)

  def main(args: Array[String]) {
    invoiceDownloader.download(from, to, paymentMethod)
  }
}
