package uber

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit._

import org.openqa.selenium.{WebElement, WebDriver, By}
import org.openqa.selenium.firefox.{FirefoxProfile, FirefoxDriver}
import org.openqa.selenium.interactions.Actions

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Author: Kul
 */
case class UberInteractor(userName: String, password: String, downloadDir: String) {

  val driver = new FirefoxDriver(firefoxProfile(downloadDir))
  driver.manage().timeouts().implicitlyWait(3, SECONDS)

  driver.get("https://login.uber.com/login")
  login()

  def trips(from: LocalDate, to: LocalDate) = {
    val tripTable = driver.findElement(By.id("trips-table")).findElement(By.tagName("tbody"))
    val trips = tripTable.findElements(By.cssSelector("tr.trip-expand__origin")).asScala
    trips.map(buildTrip)
      .filter(filterOutCanceledTrips)
      .filter(trip => trip.date.isAfter(from) && trip.date.isBefore(to))
  }

  def downloadInvoices(trips: Seq[Trip]) {
    trips.foreach(downloadInvoice)
    driver.quit()
  }

  private def firefoxProfile(receiptDownloadDir: String) = {
    val profile = new FirefoxProfile()
    profile.setPreference("browser.download.folderList", 2)
    profile.setPreference("browser.download.dir", receiptDownloadDir)
    profile.setPreference("browser.download.manager.showWhenStarting", false)
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf")
    profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream")
    profile
  }

  private def login() {
    driver.findElement(By.id("email")).sendKeys(userName)
    driver.findElement(By.id("password")).sendKeys(password)
    driver.findElement(By.cssSelector("button[type='submit']")).click()
    println("Logged in")
  }

  private def tripIds() = {
    val trips = driver.findElement(By.id("trips-table"))
    val rows = trips.findElements(By.className("trip-expand__origin")).asScala
    rows.map(r => r.getAttribute("data-target")).map(_.drop(6))
  }

  def filterOutCanceledTrips(trip: Trip) = !trip.fare.contains("Canceled")

  private def buildTrip(row: WebElement) = {
    val tripId = row.getAttribute("data-target").drop(6)
    val columns = row.findElements(By.tagName("td")).asScala.toList
    val date = LocalDate.parse(columns(1).getText, DateTimeFormatter.ofPattern("MM/dd/yy"))
    val fare = columns(3).getText
    val city = columns(5).getText
    val paymentMethod = columns(6).getText

    Trip(tripId, date, fare, city, paymentMethod)
  }

  private def downloadInvoice(trip: Trip) {
    val tripId = trip.id
    driver.get(s"https://riders.uber.com/trips/$tripId")
    val downloadButton = driver.findElements(By.xpath("//button[text()='Download Invoice']")).asScala

    if(downloadButton.nonEmpty) {
      val act = new Actions(driver)
      act.moveToElement(downloadButton.head)
      downloadButton.head.click()
      println(s"Downloaded for $tripId")
    }
  }
}