Utility to download invoices from uber trips in India.

Also allows to return sum of the fare of all the trips.

Requirements:

Java 8, Scala 2.11.*

Usage:
``` scala
  private val username = ??
  private val password = ??
  private val from = LocalDate.parse("01/09/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val to = LocalDate.parse("30/09/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val paymentMethod = "payt"
  private val invoiceFileLocation = "receipt.pdf"
  private val driver = "FIREFOX" //other one is CHROME

  private val invoiceDownloader = InvoiceDownloader(username, password, invoiceFileLocation, driver)
  invoiceDownloader.download(from, to, paymentMethod)
```