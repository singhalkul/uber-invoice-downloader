package uber

import java.io.{File, FileInputStream}
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.{PDFMergerUtility, PDFTextStripper}

/**
 * Author: Kul
 */
object PDFProcessor {

  def mergeFiles(files: List[File], targetFileName: String) {
    val utility = new PDFMergerUtility
    utility.setDestinationFileName(targetFileName)
    val sorted = getInvoicesSortedByTripDate(files)
    sorted.foreach(utility.addSource)
    utility.mergeDocuments()
  }

  private def getInvoicesSortedByTripDate(files: List[File]): List[File] = {
    files.map(f => (f, tripDate(f))).sortWith((l, r) => l._2.isBefore(r._2)).map(_._1)
  }

  private def tripDate(file: File) = {
    val content = readPDFContent(file)
    val pattern = ".*Invoice Date: (\\w+ \\d+, \\d+).*".r
    val date = pattern.findFirstIn(content)
    date match {
      case Some(pattern(date)) => LocalDate.parse(date, DateTimeFormatter.ofPattern("MMMM d, yyyy"))
      case None => throw new RuntimeException("error")
    }
  }

  def getGrossFare(file: File) = {
    val content = readPDFContent(file)
    val pattern = ".*Gross Amount ([0-9.]+) INR.*".r
    val fareOption = pattern.findFirstIn(content)

    fareOption match {
      case Some(pattern(fare)) => fare.toDouble
      case None => throw new RuntimeException("error")
    }
  }

  private def readPDFContent(file: File): String = {
    val pdfStripper = new PDFTextStripper()
    val parser = new PDFParser(new FileInputStream(file))
    parser.parse()
    val cosDoc = parser.getDocument
    val data = pdfStripper.getText(new PDDocument(cosDoc))
    cosDoc.close()
    data
  }
}
