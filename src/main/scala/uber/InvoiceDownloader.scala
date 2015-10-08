package uber

import java.io.File
import java.nio.file.{FileSystems, Files, Path, StandardCopyOption}
import java.time.LocalDate

/**
 * Author: Kul.
 */
case class InvoiceDownloader(userName: String, password: String, invoiceFileLocation: String, driver: String) {

  private val tempReceiptDownloadDir = Files.createTempDirectory("uber")

  private val provider = driver match {
    case "FIREFOX" => new FireFoxDriverProvider(tempReceiptDownloadDir.toAbsolutePath.toString)
    case "CHROME" => new ChromeDriverProvider(tempReceiptDownloadDir.toAbsolutePath.toString)
  }

  private val uberInteractor = UberInteractor(userName, password, provider)

  def download(from: LocalDate, to: LocalDate, paymentMethod: String) = {

    downloadInvoices(from, to, paymentMethod)
    val files = tempReceiptDownloadDir.toFile.listFiles.toList
    mergeInvoices(files)

    val totalFare = files.map(PDFProcessor.getGrossFare).foldLeft(0.00)(_ + _)
    println("Total fare is: " + totalFare)
  }

  private def downloadInvoices(from: LocalDate, to: LocalDate, paymentMethod: String): Unit = {
    val trips = uberInteractor.trips(from, to, paymentMethod)
    println("Determined trips for which invoices are to be downloaded...")
    uberInteractor.downloadInvoices(trips)
    println("Downloaded invoices for trips...")
  }

  private def mergeInvoices(files: List[File]) {
    val tempFile: Path = Files.createTempFile(tempReceiptDownloadDir, "uber", ".pdf")
    PDFProcessor.mergeFiles(files, tempFile.toFile.getAbsolutePath)
    Files.move(tempFile, FileSystems.getDefault.getPath(invoiceFileLocation), StandardCopyOption.REPLACE_EXISTING)
    println(s"Merged invoice file is available at the following location: $invoiceFileLocation")
  }
}