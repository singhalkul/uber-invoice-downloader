package uber

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit._

import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.{ExpectedConditions, WebDriverWait}
import org.openqa.selenium.{By, WebElement}
import uber.Trip.TripDateOrdering

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * Author: Kul
 */
case class UberInteractor(userName: String, password: String, driverProvider: DriverProvider) {

  val driver = driverProvider.driver

  driver.manage().timeouts().implicitlyWait(3, SECONDS)

  driver.get("https://login.uber.com/login")
  login()

  def trips(from: LocalDate, to: LocalDate, paymentMethod: String) = {
    val trips = getTripsWithinDateRange(1, from, to, Nil)
    trips.filter(filterOutCanceledTrips).filter(filterBasedOnPaymentMethod(_, paymentMethod))
  }

  def downloadInvoices(trips: Seq[Trip]) {
    trips.foreach(downloadInvoice)
    driver.quit()
  }

  private def getTripsWithinDateRange(page: Int, from: LocalDate, to: LocalDate, previousTrips: Seq[Trip]): Seq[Trip] = {
    gotoPage(page)
    val pageTrips = tripsFromPage
    val lastTripOnPage = tripsFromPage.last
    val filteredTripsFromPage = pageTrips.filter(t => dateBetween(t.date, from, to))
    val allTrips = (filteredTripsFromPage ++ previousTrips).sorted
    if(!lastTripOnPage.date.isBefore(from)) getTripsWithinDateRange(page + 1, from, to, allTrips) else allTrips
  }

  private def tripsFromPage = {
    val tripTable = driver.findElement(By.id("trips-table")).findElement(By.tagName("tbody"))
    val trips = tripTable.findElements(By.cssSelector("tr.trip-expand__origin")).asScala.map(buildTrip)
    trips
  }

  private def dateBetween(date: LocalDate, from: LocalDate, to: LocalDate) = date.isEqual(from) || date.isEqual(to) || (date.isAfter(from) && date.isBefore(to))

  private def gotoPage(page: Int): Unit = {
    if (page > 1) {
      driver.findElement(By.className("pagination__next")).click()
      while (driver.findElement(By.id("trips-next-loader")).getAttribute("style").nonEmpty) {
        Thread.sleep(100)
      }
    }
  }

  private def login() {
    driver.findElement(By.id("email")).sendKeys(userName)
    driver.findElement(By.id("password")).sendKeys(password)
    driver.findElement(By.cssSelector("button[type='submit']")).click()
  }
  
  private def filterOutCanceledTrips(trip: Trip) = !trip.fare.contains("Canceled")

  private def filterBasedOnPaymentMethod(trip: Trip, paymentMethod: String) = trip.paymentMethod.contains(paymentMethod) 
  
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
    val downloadButton = driver.findElement(By.xpath("//button[text()='Download Invoice']"))
    val buttonVisibleTry = Try { new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOf(downloadButton)) }
    buttonVisibleTry match {
      case Success(_) =>
        val act = new Actions (driver)
        act.moveToElement (downloadButton)
        downloadButton.click ()
      case Failure(_) => println("Could not download invoice for trip: " + s"https://riders.uber.com/trips/$tripId")
    }
  }
}