import scala.collection.Seq
import scala.math._
import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import ImplicitArbitrary._

import flocking._

object Compare {
  val doubleTolerance: Double = 0.00000001
  def doubles(a:Double, b:Double): Prop = s"$a != $b" |: abs(a - b) < doubleTolerance
  def headings(a: Heading, b: Heading): Prop = {
    val aval = a.toDouble
    val bval = b.toDouble
    s"$a != $b" |: List((aval,bval), (aval-2*Pi,bval), (aval,bval-2*Pi)).map(t => abs(t._1 - t._2)).min < doubleTolerance
  }
  def angles(a: Angle, b: Angle): Prop = {
    val aval: Double = a.toDouble
    val bval: Double = b.toDouble
    s"$a != $b" |: List((aval,bval), (aval-2*Pi,bval), (aval,bval-2*Pi)).map(t => abs(t._1 - t._2)).min  < doubleTolerance
  }
}

object Generate {
  def bird(worldWidth:Double, worldHeight:Double) =
    for {x:Double <- Gen.choose(0,worldWidth)
         y:Double <- Gen.choose(0,worldHeight)
         h:Double <- Gen.choose(0:Double,2*Pi:Double)
    } yield Bird(Point(x,y), Heading.fromDouble(h))

  def seqBirdsInModel(model: Model): Gen[Seq[Bird]] =
    for {
      birds: Seq[Bird] <- Gen.containerOf[List, Bird](
        for {x:Double <- Gen.choose(0,model.worldWidth)
             y:Double <- Gen.choose(0,model.worldHeight)
             h:Double <- Gen.choose(0:Double,2*Pi:Double)
        } yield model.Bird(model.Position(x,y), Heading.fromDouble(h)))
    } yield birds

  def model = for {
    _worldWidth <- worldWidthOrHeight
    _worldHeight <- worldWidthOrHeight
    _populationSize <-  Gen.choose(0:Int,100:Int)
    _vision <- Gen.choose(0:Double, 10:Double)
    _minimumSeparation <- Gen.choose(0:Double,100:Double)
    _maxAlignTurn <- angle(0:Double,Pi:Double)
    _maxCohereTurn <- angle(0:Double,Pi:Double)
    _maxSeparateTurn <- angle(0:Double,Pi:Double)
    _stepSize <- Gen.choose(0:Double,100:Double)
  } yield new Model {
      val worldWidth: Double = _worldWidth
      val worldHeight: Double = _worldHeight
      val populationSize: Int = _populationSize
      val vision: Double  = _vision
      val minimumSeparation: Double = _minimumSeparation
      val maxAlignTurn: Angle = _maxAlignTurn
      val maxCohereTurn: Angle = _maxCohereTurn
      val maxSeparateTurn: Angle = _maxSeparateTurn
      val stepSize: Double = _stepSize
    }: Model

  def modelIteration = Gen.choose(0,100)

  def heading = for {v <- Gen.choose(0:Double,2*Pi:Double)} yield Heading.fromDouble(v)

  def position(m: Model) = for {
    x <- Gen.choose(0.0, m.worldWidth)
    y <- Gen.choose(0.0, m.worldHeight)
  } yield m.Position(x, y)

  def smallInt = Gen.choose(-10,10)

  def worldWidthOrHeight = Gen.choose(0.0, 1000.0)

  def angle(min: Double, max: Double) = for {x <- Gen.choose(min, max)} yield Angle(x)

  def falseEnvironment = for {
    ncwide <- Gen.choose(0, 100)
    nchigh <- Gen.choose(0,100)
    width <- Gen.choose(0.0, 100.0)
    height <- Gen.choose(0.0, 100.0)
    } yield Environment.fill(false, ncwide, nchigh, width, height)

  def randomBoolEnvironment = for {
    ncwide <- Gen.choose(0,100)
    nchigh <- Gen.choose(0,100)
    width <- Gen.choose(0.0, 100.0)
    height <- Gen.choose(0.0, 100.0)
    data <- Gen.containerOfN[Array,Array[Boolean]](nchigh, Gen.containerOfN[Array,Boolean](ncwide, arbitrary[Boolean]))
  } yield Environment.from(data, ncwide, nchigh, width, height)

  
}

object ImplicitArbitrary {
  implicit def arbModel: Arbitrary[Model] = Arbitrary(Generate.model)
  implicit def arbHeading: Arbitrary[Heading] = Arbitrary(Generate.heading)
  implicit def arbAngle: Arbitrary[Angle] = Arbitrary(Generate.angle(-Pi, Pi))
}

object PointSpecification extends Properties("Point") {
  property("create") = forAll { (x: Double, y: Double) =>
    val p = Point(x, y)

    (p.x ?=x) && (p.y ?= y)
  }
}

object DoubleBoundsKeeperSpecification extends Properties("DoubleBoundsKeeper") {
  property("create") =
    forAll {(lowBound: Double) =>
      forAll(Gen.choose(lowBound, Double.MaxValue)) { (highBound: Double) =>
        val dbk = DoubleBoundsKeeper(lowBound, highBound)

        (dbk.lowBound ?= lowBound) && (dbk.highBound ?= highBound)
      }
    }

  property("keeps bounds") =
    forAll (Gen.choose(-1000.0, 1000.0)) {(lowBound: Double) =>
      forAll(Gen.choose(lowBound, 1000.0)) { (highBound: Double) =>
        val dbk = DoubleBoundsKeeper(lowBound, highBound)

        forAll (Gen.choose(lowBound, highBound)) { (x : Double) =>
          forAll (Generate.smallInt) {(dx: Int) =>
            Compare.doubles(dbk(x + dx * (highBound - lowBound)), x)
          }
        }
      }
    }
}

object PointBoundsKeeperSpecification extends Properties("PointBoundsKeeper") {
  property("create") =
    forAll { (lowBoundX : Double, lowBoundY: Double) =>
      forAll (Gen.choose(lowBoundX, Double.MaxValue), Gen.choose(lowBoundY, Double.MaxValue)) {
        (highBoundX: Double, highBoundY: Double) =>
          val pbk = PointBoundsKeeper(Point(lowBoundX, lowBoundY), Point(highBoundX, highBoundY))

          all("lowBoundX" |: (pbk.lowBound.x ?= lowBoundX),
              "lowBoundY" |: (pbk.lowBound.y ?= lowBoundY),
              "highBoundX" |: (pbk.highBound.x ?= highBoundX),
              "highBoundY" |: (pbk.highBound.y ?= highBoundY))

      }
    }

  property("keep bounds") =
    forAll (Gen.choose(-100000.0, 100000.0), Gen.choose(-100000.0,100000.0)) { (lowBoundX : Double, lowBoundY: Double) =>
      forAll (Gen.choose(lowBoundX, 100000.0), Gen.choose(lowBoundY, 100000.0)) {
        (highBoundX: Double, highBoundY: Double) =>
          val pbk = PointBoundsKeeper(Point(lowBoundX, lowBoundY), Point(highBoundX, highBoundY))

          forAll (Gen.choose(lowBoundX, highBoundX), Gen.choose(lowBoundY, highBoundY)) {(x: Double, y: Double) =>
            forAll (Generate.smallInt, Generate.smallInt) {(dx: Int, dy:Int) =>
              val checkedPoint = pbk(Point(x + dx * (highBoundX - lowBoundX), y + dy * (highBoundY - lowBoundY)))

              all("x" |: Compare.doubles(checkedPoint.x, x),
                  "y" |: Compare.doubles(checkedPoint.y, y)
                 )
            }
          }

      }
    }
}

object HeadingSpecification extends Properties("Heading") {
  property("+") = forAll { (h: Heading) => 
    forAll (Generate.angle(-Pi,Pi)) {(a: Angle) =>
      val aval = a.toDouble
      val hval = h.toDouble
      if (aval < 0)
        if (aval.abs <= hval) Compare.headings(h + a, Heading.fromDouble(hval + aval))
        else Compare.headings(h + a, Heading.fromDouble(hval + aval + 2*Pi))
      else //aval >= 0
        if (aval.abs < 2 * Pi - hval) Compare.headings(h + a, Heading.fromDouble(hval + aval))
        else Compare.headings(h + a, Heading.fromDouble(hval + aval - 2*Pi)) 
    }
  }

  property("-") = forAll { (h: Heading) => 
    forAll (Generate.angle(-Pi,Pi)) {(a: Angle) =>
      val aval = a.toDouble
      val hval = h.toDouble
      if (aval >= 0)
        if (aval.abs <= hval) Compare.headings(h - a, Heading.fromDouble(hval - aval))
        else Compare.headings(h - a, Heading.fromDouble(hval - aval + 2*Pi))
      else //aval >= 0
        if (aval.abs < 2 * Pi - hval) Compare.headings(h - a, Heading.fromDouble(hval - aval))
        else Compare.headings(h - a, Heading.fromDouble(hval - aval - 2*Pi)) 
    }
  }

  property("*") = forAll { (h: Heading) => 
    forAll (Gen.choose(0.0,6 * Pi / h.toDouble)) {(m: Double) =>
      val hval = h.toDouble
      if (m < 2 * Pi / h.toDouble) Compare.headings(h * m, Heading.fromDouble(hval * m))
      else if (m < 4 * Pi / h.toDouble) Compare.headings(h * m, Heading.fromDouble(hval * m - 2*Pi))
      else if (m < 6 * Pi / h.toDouble) Compare.headings(h * m, Heading.fromDouble(hval * m - 4*Pi))
      else /*m == 6 * Pi / h.toDouble*/ Compare.headings(h * m, Heading.fromDouble(0))

    }
  }

  property("/") = forAll { (h: Heading) => 
    forAll (Gen.choose(0.0, Double.MaxValue)) {(d: Double) => (d > 0) ==>
      Compare.headings(h / d, Heading.fromDouble(h.toDouble / d))
    }
  }

  property("angleTo") = forAll { 
    (h: Heading, a: Angle) =>
      Compare.angles(h angleTo (h + a), a) 
    }
}

object AngleSpecification extends Properties("Angle") {

}


object GraphBirdsSpecification extends Properties("GraphBirds") {
  property("construct empty graph") =
    forAll {
      (model: Model) => 
      val gb = GraphBirds(List(), model.vision, model.distanceBetween)
      gb.birds.size ?= 0
    }

  property("construct relations") =
    forAll (Generate.worldWidthOrHeight, Generate.worldWidthOrHeight) {
      (worldWidth: Double, worldHeight: Double) =>
        forAll (Generate.bird(worldWidth, worldHeight)) {
          (b1) =>
            forAll (Gen.choose(0.0, min(worldWidth, worldHeight)), //vision
                    Gen.choose(0.0,min(worldWidth, worldHeight) / 4), //r1
                    Gen.choose(min(worldWidth, worldHeight) / 4,min(worldWidth, worldHeight) / 2), //r2
                    Gen.choose(0.0, 2*Pi), //a1
                    Gen.choose(0.0, 2*Pi), //a2
                    Gen.choose(0.0, 2*Pi), //heading1
                    Gen.choose(0.0, 2*Pi)  //heading2
                    ) {
              (vision: Double, r1:Double, r2:Double, a1: Double, a2: Double, heading1:Double, heading2: Double) =>
                val b2 = Bird(Point(b1.position.x + r1 * cos(a1), b1.position.y + r1 * sin(a1)), Heading.fromDouble(heading1))
                val b3 = Bird(Point(b1.position.x + r2 * cos(a2), b1.position.y + r2 * sin(a2)), Heading.fromDouble(heading2))
                val distFunc = Distance.torus(worldWidth,worldHeight)_
                val gb = GraphBirds(List(b1,b2,b3), vision, distFunc)

                all("distances incorrect" |: (Compare.doubles(distFunc(b1.position,b2.position), r1) &&
                                              Compare.doubles(distFunc(b1.position,b3.position), r2)),
                    if (vision >= r2) "both flockmates" |: ((s"flockmate 1 not found, vision=$vision, r1=$r1, r2=$r2\n$gb" |: gb.flockmates(0).contains(1))
                                                          && (s"flockmate 2 not found, vision=$vision, r1=$r1, r2=$r2\n$gb" |: gb.flockmates(0).contains(2))
                                                          && (s"self found as flockmate, vision=$vision, r1=$r1, r2=$r2\n$gb" |: !gb.flockmates.contains(0))
                                                          && (s"wrong nearestNeighbour, vision=$vision, r1=$r1, r2=$r2\n$gb}" |: gb.nearestNeighbour(0) == Some(1)))
                    else if ((vision < r2) && (vision >= r1)) "one flockmate" |:
                                                            ((s"flockmate 1 not found, vision=$vision, r1=$r1, r2=$r2\n$gb}" |: gb.flockmates(0).contains(1))
                                                          && (s"bird 2 found as flockmate, vision=$vision, r1=$r1, r2=$r2\n$gb" |: !gb.flockmates(0).contains(2))
                                                          && (s"self found as flockmate, vision=$vision, r1=$r1, r2=$r2\n$gb" |: !gb.flockmates.contains(0))
                                                          && (s"wrong nearestNeighbour, vision=$vision, r1=$r1, r2=$r2\n$gb" |: gb.nearestNeighbour(0) == Some(1)))
                    else "no flockmate" |: (!gb.flockmates(0).contains(1) && !gb.flockmates(0).contains(2) && !gb.flockmates.contains(0)
                                       && gb.nearestNeighbour(0) == None)
                )
            }
        }
    }
}

object BirdSpecification extends Properties("Bird") {
  property("updateHeading") =
    forAll (Generate.worldWidthOrHeight, Generate.worldWidthOrHeight) {
      (worldWidth:Double, worldHeight: Double) =>
        forAll (Generate.bird(worldWidth, worldHeight)) {
          (b1) =>
            forAll (Gen.choose(0.0,min(worldWidth, worldHeight) / 4), //r1
                    Gen.choose(min(worldWidth, worldHeight) / 4,min(worldWidth, worldHeight) / 2), //r2
                    Gen.choose(0.0, 2*Pi), //a1
                    Gen.choose(0.0, 2*Pi), //a2
                    Gen.choose(0.0, 2*Pi), //heading1
                    Gen.choose(0.0, 2*Pi)  //heading2
                    ) {
              (r1: Double, r2: Double, a1: Double, a2: Double, heading1: Double, heading2: Double) =>
                forAll (Generate.angle(-Pi, Pi), //maxSeparateTurn
                        Generate.angle(-Pi, Pi), //maxAlignTurn
                        Generate.angle(-Pi, Pi) //maxCohereTurn
                        ) {
                  (maxSeparateTurn: Angle, maxAlignTurn: Angle, maxCohereTurn: Angle) =>
                    all(
                      forAll(Gen.choose(0.0, r1), //vision
                             Gen.choose(0.0, min(worldWidth, worldHeight)) //minimumSeparation
                             ) {
                        (vision: Double, minimumSeparation: Double) => (vision < r1) ==> {
                          val b2 = Bird(Point(b1.position.x + r1 * cos(a1), b1.position.y + r1 * sin(a1)), Heading.fromDouble(heading1))
                          val b3 = Bird(Point(b1.position.x + r2 * cos(a2), b1.position.y + r2 * sin(a2)), Heading.fromDouble(heading2))
                          val distFunc = Distance.torus(worldWidth,worldHeight)_
                          val gb = GraphBirds(List(b1,b2,b3), vision, distFunc)

                          Compare.headings(b1.updateHeading(flockmates = gb.flockmates(0).map(gb.birds(_)),
                                                           nearestNeighbourAndDist = gb.nearestNeighbour(0).map((b:Int) => (gb.birds(b), distFunc(b1.position,b2.position))),
                                                           minimumSeparation = minimumSeparation,
                                                           maxSeparateTurn = maxSeparateTurn,
                                                           maxAlignTurn = maxAlignTurn,
                                                           maxCohereTurn = maxCohereTurn),
                                          b1.heading)
                        }
                      },
                      forAll (Gen.choose(r1, min(worldWidth, worldHeight))) { (vision: Double) =>
                        all(
                          forAll (Gen.choose(0.0, r1)) {
                            (minimumSeparation: Double) => (r1 >= minimumSeparation) ==> {
                              val b2 = Bird(Point(b1.position.x + r1 * cos(a1), b1.position.y + r1 * sin(a1)), Heading.fromDouble(heading1))
                              val b3 = Bird(Point(b1.position.x + r2 * cos(a2), b1.position.y + r2 * sin(a2)), Heading.fromDouble(heading2))
                              val distFunc = Distance.torus(worldWidth,worldHeight)_
                              val gb = GraphBirds(List(b1,b2,b3), vision, distFunc)

                              Compare.headings(b1.updateHeading(flockmates = gb.flockmates(0).map(gb.birds(_)),
                                                                nearestNeighbourAndDist = gb.nearestNeighbour(0).map((b:Int) => (gb.birds(b), distFunc(b1.position,b2.position))),
                                                                minimumSeparation = minimumSeparation,
                                                                maxSeparateTurn = maxSeparateTurn,
                                                                maxAlignTurn = maxAlignTurn,
                                                                maxCohereTurn = maxCohereTurn),
                                              b1.alignAndCohere(gb.flockmates(0).map(gb.birds(_)), maxAlignTurn, maxCohereTurn))
                            }
                          },
                          forAll(Gen.choose(r1, min(worldWidth, worldHeight))) {
                            (minimumSeparation: Double) => (r1 < minimumSeparation) ==> {
                              val b2 = Bird(Point(b1.position.x + r1 * cos(a1), b1.position.y + r1 * sin(a1)), Heading.fromDouble(heading1))
                              val b3 = Bird(Point(b1.position.x + r2 * cos(a2), b1.position.y + r2 * sin(a2)), Heading.fromDouble(heading2))
                              val distFunc = Distance.torus(worldWidth,worldHeight)_
                              val gb = GraphBirds(List(b1,b2,b3), vision, distFunc)

                              Compare.headings(b1.updateHeading(flockmates = gb.flockmates(0).map(gb.birds(_)),
                                                                nearestNeighbourAndDist = gb.nearestNeighbour(0).map((b:Int) => (gb.birds(b), distFunc(b1.position,b2.position))),
                                                                minimumSeparation = minimumSeparation,
                                                                maxSeparateTurn = maxSeparateTurn,
                                                                maxAlignTurn = maxAlignTurn,
                                                                maxCohereTurn = maxCohereTurn),
                                             b1.separate(b2.heading, maxSeparateTurn))
                            }
                          }
                        )
                      }
                    )
                }
            }
        }
    }

  property("separate") =
    forAll (Generate.worldWidthOrHeight, Generate.worldWidthOrHeight) {
      (worldWidth:Double, worldHeight: Double) =>
        forAll (Generate.bird(worldWidth, worldHeight)) {
          (b1) =>
            forAll (Generate.angle(-Pi, Pi), Generate.angle(0.0, Pi)) {
              (angleval: Angle, maxSeparateTurn: Angle) =>
                Compare.headings(b1.separate(b1.heading + angleval, maxSeparateTurn),
                                 b1.heading + b1.turnAtMost(b1.heading - angleval, maxSeparateTurn))
            }
        }
    }

  property("turnAtMost") =
    forAll (Generate.worldWidthOrHeight, Generate.worldWidthOrHeight) {
      (worldWidth:Double, worldHeight: Double) =>
        forAll (Generate.bird(worldWidth, worldHeight)) {
          (b1) =>
            forAll (Generate.angle(-Pi, Pi)) {
              (angle: Angle) => {
                all(
                  forAll (Generate.angle(angle.abs.toDouble, 2 * Pi)) {
                    (maxTurn: Angle) =>
                      s"angle <= maxTurn, angle=$angle, maxTurn=$maxTurn" |: Compare.angles(b1.turnAtMost(b1.heading + angle, maxTurn),
                                     angle)
                  },
                  "angle >= maxTurn" |: forAll (Generate.angle(0.0, angle.abs.toDouble)) {
                    (maxTurn: Angle) =>
                      Compare.angles(b1.turnAtMost(b1.heading + angle, maxTurn),
                                     (if (angle >= Angle(0)) maxTurn else -maxTurn))
                  }
                )
              }
            }
        }
    }

  property("averageFlockmateHeading") =
    forAll (Generate.worldWidthOrHeight, Generate.worldWidthOrHeight) {
      (worldWidth:Double, worldHeight: Double) =>
        forAll (Generate.bird(worldWidth, worldHeight)) {
          (b1: Bird) =>
            all(
              forAll (Generate.bird(worldWidth, worldHeight)) {
                (b2: Bird) =>
                  s"Just one flockmate: b1=$b1, b2=$b2" |: Compare.headings(b1.averageFlockmateHeading(List(b2)),
                                                                            b2.heading)
              },
              forAll (Generate.bird(worldWidth, worldHeight)) {
                (b2:Bird) =>
                  forAll (Generate.angle(max(0 - b2.heading.toDouble, -Pi/2.0),min(2*Pi - b2.heading.toDouble - 0.000001, Pi/2)),
                          Gen.choose(0.0, worldWidth),
                          Gen.choose(0.0, worldHeight)) {
                    (a3: Angle, x3: Double, y3: Double) =>
                      val distFunc = Distance.torus(worldWidth,worldHeight)_
                      val b3 = Bird(Point(x3,y3),b2.heading + a3)
                      s"Two flockmates, easy angles: b1=$b1, b2=$b2, a3=$a3" |: Compare.headings(b1.averageFlockmateHeading(List(b2,b3)),
                                       Heading.fromDouble(b2.heading.toDouble + (a3.toDouble / 2.0)))
                  }
              },
              forAll (Generate.bird(worldWidth, worldHeight)) {
                (b2:Bird) =>
                  forAll (Generate.angle(-Pi, Pi),
                          Gen.choose(0.0, worldWidth),
                          Gen.choose(0.0, worldHeight)) {
                    (a3: Angle, x3: Double, y3: Double) =>
                      val distFunc = Distance.torus(worldWidth,worldHeight)_
                      val b3 = Bird(Point(x3,y3),b2.heading + a3)
                      s"Two flockmates, any angles: b1=$b1, b2=$b2, a3=$a3" |: Compare.headings(b1.averageFlockmateHeading(List(b2,b3)),
                                       Heading.fromDouble(b2.heading.toDouble + (a3.toDouble / 2.0)))
                  }
              }
              // forAll (Generate.bird(worldWidth, worldHeight),
              //                                       Generate.heading,
              //                                       Gen.choose(0.0, worldWidth),
              //                                       Gen.choose(0.0, worldHeight),
              //                                       Gen.choose(0.0, worldWidth),
              //                                       Gen.choose(0.0, worldHeight)) {
              //   (b2: Bird, afh: Heading, x3: Double, y3: Double, x4: Double, y4: Double) =>
              //     forAll (Generate.angle(0.0, toDouble((b2.heading angleTo afh) * 3))) { (a3: Angle) =>
              //       val h3: Heading = b2.heading + a3
              //       val b3 = Bird(x3, y3, h3, worldWidth, worldHeight)
              //       val a4: Angle = ((b2.heading angleTo afh) * 3) - a3
              //       val h4: Heading = b2.heading + a4
              //       val b4 = Bird(x4, y4, h4, worldWidth, worldHeight)

              //     "3 flockmates, any angles" |: Compare.headings(b1.averageFlockmateHeading(List(b2,b3,b4)),
              //                      afh)
              //     }
              // }
            )
        }
    }
}

object ModelSpecification extends Properties("Model") {
  property("create position") =
    forAll { (model: Model) =>
      forAll (Gen.choose(0:Double, model.worldWidth), Gen.choose(0:Double, model.worldHeight)) {
        (x: Double, y:Double) =>
          forAll (Generate.smallInt, Generate.smallInt){ (dx: Int, dy: Int) =>
            val pos = model.Position(x + model.worldWidth * dx, y + model.worldHeight * dy)

            all("x" |: Compare.doubles(pos.x, x),
                "y" |: Compare.doubles(pos.y, y))
          }
    }
  }

  property("distanceBetween 2 positions") =
    forAll {
      (model: Model) =>
        forAll (Generate.position(model)) {
          (p1: Point) =>
            forAll (Gen.choose(-model.worldWidth / 2, model.worldWidth / 2), Gen.choose(-model.worldHeight / 2, model.worldHeight / 2)) {
              (distx: Double, disty: Double) => {
                forAll (Generate.smallInt, Generate.smallInt) {
                  (multdistx: Int, multdisty: Int) => {
                    val p2 = model.Position(p1.x + distx + model.worldWidth * multdistx,
                                  p1.y + disty + model.worldHeight * multdisty)
                    Compare.doubles(model.distanceBetween(p1,p2), sqrt(pow(distx,2) + pow(disty,2)))
                  }
                }
              }
            }
        }
    }


  property("random bird") = forAll {
    (model: Model) => {
      val bird1 = model.randomBird
      val bird2 = model.randomBird

      (bird1.heading != bird2.heading) :| "birds headings randomly different" &&
        (bird1.position != bird2.position) :| "birds positions randomly different" &&
        ((bird1.heading.value < 2*Pi) && (bird1.heading.value >= 0)) :| "bird heading in [0;2*Pi[" &&
        ((bird1.position.x >= 0) && (bird1.position.x < model.worldWidth)) :| "bird x position in bounds" &&
        ((bird1.position.y >= 0) && (bird1.position.y < model.worldHeight)) :| "bird y position in bounds"
    }
  }

  property("random init") = forAll {
    (model: Model) => model.randomInit.birds.size == model.populationSize
  }

  property("construct graph") =
    forAll { (model: Model) =>
      forAll (Generate.seqBirdsInModel(model)) { (birds: Seq[Bird]) => {
      val graph = model.buildGraph(birds)

      ("flocking distance" |: all((for (u <- 0 until birds.size; v <- 0 until birds.size)
                                 yield (u != v && model.distanceBetween(graph.birds(v).position, graph.birds(u).position) < model.vision)
                                        =? graph.areFlockmates(u,v)): _*)) &&
      ("nearest neighbour" |: all((0 until birds.size).map(u =>
        (graph.flockmates(u).size match {
          case 0 => None
          case _ => Some(
            (0 until birds.size).diff(u until (u + 1)).reduce((a: Int, b: Int) =>
              if (model.distanceBetween(graph.birds(u).position, graph.birds(a).position)
                  < model.distanceBetween(graph.birds(u).position, graph.birds(b).position)) a
              else b))}) =? graph.nearestNeighbour(u) ): _*))
      }
    }
  }

  // always passes unless the model crashes
  property("run") = forAll (Generate.model, Generate.modelIteration) {
    (model: Model, iterations: Int) => { model.start(iterations); true }
  }

}

// object BehaviourSpecification extends Properties("Behaviour") {

//   /* generate clusters of points s.t. two points are in the same clusters if their
//    * are at most at a distance d from each other. npoints >= nclusters*/
//   def genPointsClusters(npoints: Int, nclusters: Int, d: Double): Gen[Seq[(Double, Double)]] = 
//     nclusters match {
//       case 0 => List()
//       case 1 => genOneCluster(npoints, d, 0, 0)
//       case _ => for { npointsThisCluster <- Gen.choose(1, npoints - nclusters + 1)
//                       recPoints <- genPointsClusters(npoints - npointsThisCluster, nclusters - 1, d)
//                       newCluster <- genOneCluster(npointsThisCluster, 
//                                                   d, 
//                                                   recPoints.unzip._1.max + 2*d + npointsThisCluster * d, 
//                                                   0)
//                 } yield recPoints ++ newCluster
//     }

//   /* generate a cluster of points forming a connected component of neighbours */
//   def genOneCluster(npoints: Int, d: Double, originx: Double, originy: Double): Gen[Seq[(Double, Double)]] = 
//     npoints match {
//       case 0 => List()
//       case 1 => for {theta: Double <- Gen.choose(0.0, 2*Pi)
//                      r: Double <- Gen.choose(0.0, d)
//                 } yield List((originx + r * cos(theta), originy + r * sin(theta)))
//       case _ => for { recCluster: Seq[(Double, Double)] <- genOneCluster(npoints - 1, d, originx, originy)
//             theta: Double <- Gen.choose(0.0, 2*Pi)
//             r: Double <- Gen.choose(0.0, d)
//             parent: Int <- Gen.choose(0, recCluster.size - 1)
//       } yield recCluster :+ (recCluster(parent)._1 + r * cos(theta), recCluster(parent)._2 + r * sin(theta))
//     }    
              
//   property("extractComponent") =
//     forAll(Gen.choose(0.1, 10.0)) {
//       (d: Double) =>
//         forAll(Gen.choose(0, 50)) {
//           (npoints: Int) => 
//             forAllNoShrink(genOneCluster(npoints, d, 0, 0), genOneCluster(npoints, d, 2 * (npoints - 1) * d + 2 * d, 0), Gen.containerOfN[List, Heading](npoints * 2, Generate.heading)) {
//               (points1: Seq[(Double, Double)], points2: Seq[(Double, Double)], headings: Seq[Heading]) =>
//                 val worldWidth = 4 * (npoints - 1) * d + 4 * d
//                 val worldHeight = (npoints - 1) * d + 2 * d
//                 val distFunc = Distance.torus(worldWidth, worldHeight)_
//                 val pbk = PointBoundsKeeper(Point(0,0), Point(worldWidth, worldHeight))
//                 val gb: GraphBirds = 
//                   GraphBirds(
//                     ((points1 ++ points2) zip headings).map((x: ((Double,Double), Heading)) => Bird(pbk(Point(x._1._1,x._1._2)),x._2)),
//                     d,
//                     distFunc)
//                 new Behaviour {
//                   val model = null
//                   }.extractComponent(gb, 0, Set()) ?= (0 until npoints).toSet
//             }
//         }
//     }

//   property("countGroups") = 
//     forAll(Gen.choose(0, 5), Gen.choose(0.1, 10.0)) {
//       (ngroups: Int, d: Double) =>
//         forAll(Gen.choose(ngroups, ngroups + 50)) {
//           (npoints: Int) => 
//             forAllNoShrink(genPointsClusters(npoints, ngroups, d), Gen.containerOfN[List, Heading](npoints, Generate.heading)) {
//               (points: Seq[(Double, Double)], headings: Seq[Heading]) => 
//                 val minmaxbounds = points.foldLeft((Double.MaxValue,Double.MaxValue,Double.MinValue,Double.MinValue))(
//                                       (bounds,a) => (if (a._1 < bounds._1) a._1 else bounds._1,
//                                                       if (a._2 < bounds._2) a._2 else bounds._2,
//                                                       if (a._1 > bounds._3) a._1 else bounds._3,
//                                                       if (a._2 > bounds._4) a._2 else bounds._4))
//                 val xmin = minmaxbounds._1
//                 val ymin = minmaxbounds._2
//                 val xmax = minmaxbounds._3
//                 val ymax = minmaxbounds._4
//                 val worldWidth = xmax - xmin + 2 * d
//                 val worldHeight = ymax - ymin + 2 * d
//                 val distFunc = Distance.torus(worldWidth, worldHeight)_
//                 val pbk = PointBoundsKeeper(Point(0,0), Point(worldWidth, worldHeight))
//                 val gb: GraphBirds = 
//                   GraphBirds(
//                     (points zip headings).map((x: ((Double,Double), Heading)) => Flocking.Bird(pbk(Point(x._1._1,x._1._2)),x._2)),
//                     d,
//                     distFunc)
//                 new Behaviour {
//                   val model = null
//                   }.countGroups(gb) ?= ngroups
//             }
//         }
//     }

//   property("relativeDiffusion easy") = 
//     all(Compare.doubles(new Behaviour {
//                   val model = null
//                   }.relativeDiffusion(List(List(1.0), List(1.0)), List(List(1.0), List(1.0))), 0),
//         Compare.doubles(new Behaviour {
//                   val model = null
//                   }.relativeDiffusion(List(List(0.0),List(0.0)), List(List(1.0),List(1.0))), 1),
//         Compare.doubles(new Behaviour {
//                   val model = null
//                   }.relativeDiffusion(List(List(0.0,0.0), List(0.0,0.0), List(0.0,0.0)), 
//                                       List(List(1.0,1.0), List(1.0,1.0), List(1.0,1.0))), 1)
//     )

//   // property("constructDescription") = 
//   //   forAll (Generate.model, Gen.choose(0,50), Gen.choose(1,10))
//   //     {(model: Model, itermax: Int, every: Int) =>
//   //       val desc: Seq[(Int, GraphBirds)] = Behaviour.constructDescription(
//   //                     model,
//   //                     {(pastbehaviour: Seq[(Int, GraphBirds)], state: GraphBirds, iteration: Int) => 
//   //                       if (iteration >= itermax) None
//   //                       else if (iteration % every == 0) Some(pastbehaviour :+ (iteration, state))
//   //                            else Some(pastbehaviour)},
//   //                     List()
//   //                  )
        
//   //       desc.unzip._1 ?= (0 until itermax by every).toList
//   //     }
// }

object DistMatrixSpecification extends Properties("DistMatrix") {

  property("create easy") = {
    val dm = DistMatrix(List(Point(0.0,0.0), Point(0.0, 1.0)), Distance.euclidean)
    Compare.doubles(dm(0,1), 1)
  }


  property("create") = 
    forAll(Gen.choose(0,50)) {
      (ndists: Int) =>
        forAll(Gen.containerOfN[List, Double](ndists, Gen.choose(0.0, 1000.0)),
               Gen.containerOfN[List, Double](ndists, Gen.choose(0.0, 2 * Pi))) {
          (radii: List[Double], theta: List[Double]) => 
            val pointsAround0 = ((radii zip theta) map (_ match {case (r,t) => Point(r * cos(t), r * sin(t))}))
            def chainOfPoints(p: Point, ps: Seq[Point]): List[Point] = 
              if (ps.size == 0) List(p)
              else {
                val h = ps.head
                val t = ps.tail
                p :: chainOfPoints(Point(h.x + p.x, h.y + p.y), t)
              }
            val dm = DistMatrix(chainOfPoints(Point(0,0), pointsAround0), Distance.euclidean)
            all((1 until ndists + 1) map (i => (dm(i,i) ?= 0) && Compare.doubles(dm(i-1,i), radii(i - 1)) && (dm(i,i-1) ?= dm(i-1,i))): _*)
        }
    }
}

object EnvironmentSpecification extends Properties("Environment") {
  property("int2Double") =
    forAll(Generate.randomBoolEnvironment) {
      env: Environment[Bool] =>
        forAll(Gen.choose(0,env.nCellsWide), Gen.choose(0,env.nCellsHigh)) {
          (i: Int, j: Int)
        }

          )      
    }

}