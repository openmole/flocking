package flocking.model.datatypes

import flocking.model.tools._

case class Point(val x: Double, val y: Double):
  override def toString: String = super.toString ++ s":($x, $y)"


case class PointBoundsKeeper(lowBound: Point, highBound: Point):
  val bkAbscissa = DoubleBoundsKeeper(lowBound.x, highBound.x)
  val bkOrdinate = DoubleBoundsKeeper(lowBound.y, highBound.y)
  def apply(p:Point): Point = new Point(bkAbscissa(p.x), bkOrdinate(p.y))
