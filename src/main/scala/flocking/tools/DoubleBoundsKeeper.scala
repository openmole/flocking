package flocking.tools

//---- Base types ----//
trait DoubleBoundsKeeper {
  val lowBound: Double
  val highBound: Double

  def keepInBounds(x: Double): Double =
    if (x >= highBound) ((x - lowBound) % (highBound - lowBound)) + lowBound
    else if (x < lowBound) highBound - ((highBound - x) % (highBound - lowBound))
        else x

  def apply(x: Double): Double = keepInBounds(x)
}

object DoubleBoundsKeeper {
  def apply(_lowBound: Double, _highBound: Double) = new DoubleBoundsKeeper {
    val lowBound = _lowBound
    val highBound = _highBound
  }
}
