package flocking.birds

import flocking.datatypes._
import flocking.engine._

/**
 * Created by guillaume on 11/03/14.
 */
trait Bird {
  val position: Point
  val heading: Heading

  type SensorState

  def update(s: SensorState, m: Model): Bird
}
