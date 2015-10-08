package uber

import java.time.LocalDate

/**
 * Author: Kul
 */
case class Trip(id: String, date: LocalDate, fare: String, city: String, paymentMethod: String)

object Trip {

  implicit object TripDateOrdering extends Ordering[Trip] {
    override def compare(x: Trip, y: Trip) = x.date.compareTo(y.date)

  }
}