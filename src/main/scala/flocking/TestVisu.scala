package flocking

import scala.math._
import flocking.visu._
import flocking.engine._
import flocking.datatypes._

object TestVisu extends App {

  val model = new Model {
    val worldWidth: Double = 100
    val worldHeight: Double = 100
    val envDivsHorizontal: Int = 100
    val envDivsVertical: Int = 100
    val populationSize: Int = 300
    val vision: Double = 9
    val visionObstacle: Double = 5
    val minimumSeparation: Double = 1
    val maxAlignTurn: Angle = Angle(toRadians(5))
    val maxCohereTurn: Angle = Angle(toRadians(3))
    val maxSeparateTurn: Angle = Angle(toRadians(1.5))
    val stepSize: Double = 1.5
  }


  val visu = new Visu {
        lazy val model: Model = TestVisu.model
        lazy val pixelWidth: Int = 500
        lazy val pixelHeight: Int = 500
        lazy val frameDelay: Int = 1000 / 24
        lazy val birdLength:Double = 0.02 * min(pixelWidth, pixelHeight)
        lazy val birdWidth: Double = 2
  } 

  // model.env.addDisc(model.worldWidth / 2.0, model.worldHeight / 2.0, 30.0, visu.obstacleColorRGB)
  model.env.addDisc(0, 0, 30.0, visu.obstacleColorRGB)

  // val visu = new Visu with Fullscreen {
  //       lazy val model: Model = TestVisu.model
  //       lazy val frameDelay: Int = 1000 / 24
  //       lazy val birdLength:Double = 0.02 * min(pixelWidth, pixelHeight)
  //       lazy val birdWidth: Double = 2
  // } 
  visu.Skeleton.startup(null)
}