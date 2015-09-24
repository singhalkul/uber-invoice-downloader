Utility to download invoices from uber trips in India.

Also allows to return sum of the fare of all the trips.

Requirements:

Java 8, Scala 2.11.*

Usage:
``` scala
  private val from = LocalDate.parse("01/09/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val to = LocalDate.parse("05/09/2015", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val paymentMethod = "payt"

  val uberInteractor = UberInteractor(username, password, receiptDownloadDir)
  val trips = uberInteractor.trips(from, to, paymentMethod)
  uberInteractor.downloadInvoices(trips)

  val files = new File(receiptDownloadDir).listFiles.toList

  PDFProcessor.mergeFiles(files, mergedReceiptFile)

  val totalFare = files.map(PDFProcessor.getGrossFare).foldLeft(0.00)(_+_)
```
