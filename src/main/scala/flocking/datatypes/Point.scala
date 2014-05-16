package flocking.datatypes

import flocking.tools._

case class Point(val x: Double, val y: Double) {
    override def toString: String = super.toString ++ s":($x, $y)"
}

trait PointBoundsKeeper {
  val lowBound: Point
  val highBound: Point
  val bkAbscissa = DoubleBoundsKeeper(lowBound.x, highBound.x)
  val bkOrdinate = DoubleBoundsKeeper(lowBound.y, highBound.y)
  def apply(p:Point): Point = new Point(bkAbscissa(p.x), bkOrdinate(p.y))
}
object PointBoundsKeeper {
  def apply(_lowBound: Point, _highBound: Point) = new {
    val lowBound = _lowBound
    val highBound = _highBound
  } with PointBoundsKeeper
}
