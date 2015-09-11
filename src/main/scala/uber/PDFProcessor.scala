package uber

import java.io.{File, FileInputStream}

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
    files.foreach(utility.addSource)
    utility.mergeDocuments()
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
    val cos = parser.getDocument
    val data = pdfStripper.getText(new PDDocument(cos))
    data
  }
}
