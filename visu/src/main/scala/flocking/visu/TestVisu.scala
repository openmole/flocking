package flocking.visu

import scala.math.toRadians
import flocking.model.*
import flocking.model.datatypes.*

import java.util.Random
/*
 * Copyright (C) 2021 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

object TestVisu extends App {

  val model = Model(
    worldWidth = 100,
    worldHeight = 100,
//    envDivsHorizontal = 100,
//    envDivsVertical = 100,
    populationSize = 300,
    vision = 9,
//    visionObstacle = 5,
    minimumSeparation = 1,
    maxAlignTurn = Angle(toRadians(5)),
    maxCohereTurn = Angle(toRadians(3)),
    maxSeparateTurn = Angle(toRadians(1.5)),
    stepSize = 2
  )


  val environment = Environment.buildEmpty(Visu.backgroundColorRGB, 100, 100, 100, 100)

  // model.env.addDisc(model.worldWidth / 2.0, model.worldHeight / 2.0, 30.0, visu.obstacleColorRGB)

  environment.addDisc(0, 0, 30.0, Visu.obstacleColorRGB)

  val random = new Random(42)

  val visu = Visu(
    model = TestVisu.model,
    environment = environment,
    random = random,
    pixelWidth = 500,
    pixelHeight = 500,
    frameDelay = 1000 / 24,
    birdLength = 0.02 * 500,
    birdWidth = 2
  )

  // val visu = new Visu with Fullscreen {
  //       lazy val model: Model = TestVisu.model
  //       lazy val frameDelay: Int = 1000 / 24
  //       lazy val birdLength:Double = 0.02 * min(pixelWidth, pixelHeight)
  //       lazy val birdWidth: Double = 2
  // }
  visu.Skeleton.startup(null)
}
