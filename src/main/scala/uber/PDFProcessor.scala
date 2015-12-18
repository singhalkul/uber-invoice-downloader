package uber

import java.io.{File, FileInputStream}
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.{PDFMergerUtility, PDFTextStripper}

import scala.util.{Failure, Success, Try}

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
    val oldDateFormat = tripDateOldFormat(content)
    oldDateFormat match {
      case Success(date) => date
      case _ => newDateFormat(content).get
    }
  }

  private def newDateFormat(content: String): Try[LocalDate] = {
    val pattern = ".*Invoice Date: (\\d+ \\w+ \\d+).*".r
    val date = pattern.findFirstIn(content)
    date match {
      case Some(pattern(date)) => Success(LocalDate.parse(date, DateTimeFormatter.ofPattern("d MMMM yyyy")))
      case None => Failure(new RuntimeException("Could not parse date in format d MMMM yyyy"))
    }
  }

  private def tripDateOldFormat(content: String): Try[LocalDate] = {
    val pattern = ".*Invoice Date: (\\w+ \\d+, \\d+).*".r
    val date = pattern.findFirstIn(content)
    date match {
      case Some(pattern(date)) => Success(LocalDate.parse(date, DateTimeFormatter.ofPattern("MMMM d, yyyy")))
      case None => Failure(new RuntimeException("Could not parse date in format MMMM d, yyyy"))
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
