import java.time.LocalDate
import java.time.format.DateTimeFormatter

import uber.InvoiceDownloader

/**
 * Author: Kul.
 */
object Main extends App {

  private val username = ???
  private val password = ???
  private val from = LocalDate.parse("15/10/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val to = LocalDate.parse("30/10/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val paymentMethod = "payt"
  private val invoiceFileLocation = ??? // path wih file name
  private val driver = "CHROME" //other one is FIREFOX

  private val invoiceDownloader = InvoiceDownloader(username, password, invoiceFileLocation, driver)
  invoiceDownloader.download(from, to, paymentMethod)
}
