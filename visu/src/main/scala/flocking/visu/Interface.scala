package flocking.visu

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

object Interface {
  // var model: Model = null

  // def runDefaultModel(outputResolution: (Int,Int),
  //                     environmentResolution: (Int,Int),
  //                     framesPerSec: Int,
  //                     birdsSpeed: Double,
  //                     _popSize:Int,
  //                     _vision: Double,
  //                     _visionObstacle: Double,
  //                     _minimumSeparation: Double,
  //                     _maxAlignTurn: Double,
  //                     _maxCohereTurn: Double,
  //                     _maxSeparationTurn: Double,
  //                     _birdWidth: Double,
  //                     _birdLength: Double): Visu = {
  //     model = new Model {
  //     val envDivsHorizontal: Int = environmentResolution._1
  //     val envDivsVertical: Int = environmentResolution._2
  //     val worldWidth: Double = outputResolution._1.toDouble
  //     val worldHeight: Double = outputResolution._2.toDouble
  //     val populationSize: Int = _popSize
  //     val vision: Double = _vision * min(outputResolution._1, outputResolution._2) / 70.0
  //     val visionObstacle: Double = _visionObstacle * min(outputResolution._1, outputResolution._2) / 70.0
  //     val minimumSeparation: Double = _minimumSeparation * min(outputResolution._1, outputResolution._2) / 70.0
  //     val maxAlignTurn: Angle = Angle(toRadians(_maxAlignTurn))
  //     val maxCohereTurn: Angle = Angle(toRadians(_maxCohereTurn))
  //     val maxSeparateTurn: Angle = Angle(toRadians(_maxSeparationTurn))
  //     val stepSize: Double = (birdsSpeed * min(outputResolution._1, outputResolution._2) / 70.0 ) // / (framesPerSec.toDouble)
  //   }

  //   val visu = new {
  //       val model: Model = Interface.model
  //       val pixelWidth: Int = outputResolution._1
  //       val pixelHeight: Int = outputResolution._2
  //       val frameDelay: Int = 1000 / framesPerSec
  //       val birdLength:Double = _birdLength * min(outputResolution._1, outputResolution._2) / 70.0
  //       val birdWidth: Double = _birdWidth
  //       override val gc = Interface.gc
  //   } with Visu
  //   visu.Skeleton.startup(null)
  //   visu
  // }

  // val ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
  // val gd = ge.getDefaultScreenDevice();
  // val gc = gd.getDefaultConfiguration();


  // def main(args: Array[String]): Unit = {
  //   val inputMatrixSize = (args(0).split("x")(0).toInt,args(0).split("x")(1).toInt)
  //   val outputResolution = if (args(1) == "fullscreen") {
  //                             (gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight())
  //                         } else (args(1).split("x")(0).toInt,args(1).split("x")(1).toInt)
  //   val environmentResolution = (args(2).split("x")(0).toInt,args(2).split("x")(1).toInt)
  //   val framesPerSecBirds = args(3).toInt
  //   val framesPerSecInput = args(4).toInt
  //   val birdsSpeed = args(5).toDouble
  //   val dataFile = args(6)
  //   val vision = args(7).toDouble
  //   val visionObstacle = args(8).toDouble
  //   val minimumSeparation = args(9).toDouble
  //   val maxAlignTurn = args(10).toDouble
  //   val maxCohereTurn = args(11).toDouble
  //   val maxSeparateTurn = args(12).toDouble
  //   val birdWidth = args(13).toDouble
  //   val birdLength = args(14).toDouble
  //   val popSize = args(15).toInt

  //   val backgroundColorRGB:Int = new Color(0,0,0).getRGB()
  //   val obstacleColorRGB:Int = new Color(0,0,255).getRGB()

  //   // start swing thread
  //   val visu = runDefaultModel(outputResolution,
  //                              environmentResolution,
  //                              framesPerSecBirds,
  //                              birdsSpeed,
  //                              popSize,
  //                              vision ,
  //                              visionObstacle ,
  //                              minimumSeparation ,
  //                              maxAlignTurn ,
  //                              maxCohereTurn ,
  //                              maxSeparateTurn,
  //                              birdWidth,
  //                              birdLength)
  //   if (args(1) == "fullscreen") {
  //     gd.setFullScreenWindow(visu.Skeleton.f.peer);
  //   }

  //   // start data input thread
  //   val odl = new ObstacleDataListener(1000 / framesPerSecInput, dataFile, inputMatrixSize, (model.envDivsHorizontal, model.envDivsVertical),
  //     (i,j,b) => {
  //       model.env.set(i, j ,if (b == (0:Byte)) backgroundColorRGB else obstacleColorRGB)
  //     } )

  //   odl.start

  //   // Runtime.getRuntime.addShutdownHook(new Thread() {
  //   //   override def run() {
  //   //     println("QUIT!")
  //   //     odl.quit
  //   //   }
  //   // } )

  // }
}
