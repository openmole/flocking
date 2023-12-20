//package flocking
//
//import scala.math._
//import scala.collection._
//import scala.util.Random
//
//object Flocking {
//
//  //---- Base types ----//
//  trait DoubleBoundsKeeper {
//    val lowBound: Double
//    val highBound: Double
//
//    def keepInBounds(x: Double): Double =
//      if (x >= highBound) ((x - lowBound) % (highBound - lowBound)) + lowBound
//      else if (x < lowBound) highBound - ((highBound - x) % (highBound - lowBound))
//          else x
//
//    def apply(x: Double): Double = keepInBounds(x)
//  }
//
//  object DoubleBoundsKeeper {
//    def apply(_lowBound: Double, _highBound: Double) = new DoubleBoundsKeeper {
//      val lowBound = _lowBound
//      val highBound = _highBound
//    }
//  }
//
//  case class Point(val x: Double, val y: Double) {
//      override def toString: String = super.toString ++ s":($x, $y)"
//  }
//
//  trait PointBoundsKeeper {
//    val lowBound: Point
//    val highBound: Point
//    val bkAbscissa = DoubleBoundsKeeper(lowBound.x, highBound.x)
//    val bkOrdinate = DoubleBoundsKeeper(lowBound.y, highBound.y)
//    def apply(p:Point): Point = new Point(bkAbscissa(p.x), bkOrdinate(p.y))
//  }
//  object PointBoundsKeeper {
//    def apply(_lowBound: Point, _highBound: Point) = new {
//      val lowBound = _lowBound
//      val highBound = _highBound
//    } with PointBoundsKeeper
//  }
//
//  object HeadingBoundsKeeper {
//    val hbk = DoubleBoundsKeeper(0,2 * Pi)
//    def apply(x:Double) = hbk(x)
//  }
//
//  case class Heading(value: Double) {
//    def -(a: Angle): Heading = toHeading(value - a.value)
//    def +(a: Angle): Heading = toHeading(value + a.value)
//    def *(d: Double): Heading = toHeading(value * d)
//    def /(d: Double): Heading = toHeading(value / d)
//    def angleTo(h:Heading): Angle = {
//      if (h.value > value)
//        if (abs(h.value - value) < abs(h.value - 2*Pi - value)) toAngle(h.value - value)
//        else toAngle(h.value - 2*Pi - value)
//      else
//      if (abs(h.value + 2*Pi - value) < abs(h.value - value)) toAngle(h.value + 2*Pi - value)
//      else toAngle(h.value - value)
//    }
//  }
//  def toHeading(value: Double): Heading = Heading(HeadingBoundsKeeper(value))
//  def toDouble(heading: Heading): Double = heading.value
//
//  // object AngleBoundsKeeper extends DoubleBoundsKeeper {
//  //   val lowBound: Double = -Pi
//  //   val highBound: Double = Pi
//  // }
//  case class Angle(value: Double) {
//    def unary_- = toAngle(-value)
//    def abs = toAngle(value.abs)
//    def <(a: Angle): Boolean = value < a.value
//    def <=(a: Angle): Boolean = value <= a.value
//    def >(a: Angle): Boolean = value > a.value
//    def >=(a: Angle): Boolean = value >= a.value
//    def *(x: Double): Angle = toAngle(value * x)
//    def +(a: Angle): Angle = toAngle(value + a.value)
//    def -(a: Angle): Angle = toAngle(value - a.value)
//  }
//  def toAngle(value: Double): Angle = Angle(value)
//  def toDouble(a: Angle): Double = a.value
//
//  trait GraphBirds {
//    val birds: Seq[Bird]
//    val flockmates: Seq[Seq[Int]]
//    val nearestNeighbour: Seq[Option[Int]]
//
//    def areFlockmates(b1: Int, b2: Int) = flockmates(b1).contains(b2)
//
//    override def toString = "Birds: " ++ birds.toString ++ "\nFlockmates: " ++ flockmates.toString ++ "\nNearestNeighbours: " ++ nearestNeighbour.toString
//  }
//
//  object GraphBirds {
//      def apply(_birds: Seq[Bird], vision: Double, distFunc: (Point, Point) => Double): GraphBirds = {
//
//        new GraphBirds {
//          val birds: Seq[Bird] = _birds
//          val flockmates: Seq[Seq[Int]] =
//            for {u <- 0 until birds.size}
//            yield for {v <- 0 until birds.size if ((v != u) && distFunc(birds(u).position,birds(v).position) <= vision)}
//                  yield v
//          val nearestNeighbour: Seq[Option[Int]] =
//            for {u <- 0 until birds.size}
//            yield (flockmates(u).size match {case 0 => None
//                                            case _ => Some(closestFlockmate(birds, u,flockmates(u), distFunc))})
//        }
//      }
//
//      def closestFlockmate(birds: Seq[Bird], u: Int, flockmates:Seq[Int], distFunc: (Point, Point) => Double): Int =
//        flockmates.reduce((a,b) => if (distFunc(birds(u).position,birds(a).position) < distFunc(birds(u).position,birds(b).position)) a
//                                   else b)
//  }
//
//  trait Bird {
//    val position: Point
//    val heading: Heading
//
//    def update(flockmates: Seq[(Bird, Double)],
//               nearestNeighbour: Option[(Bird, Double)],
//               model: Model): Bird = {
//
//        val newHeading = updateHeading(flockmates.map(_._1), nearestNeighbour,
//                         model.minimumSeparation,model.maxSeparateTurn,model.maxAlignTurn,model.maxCohereTurn)
//        val newPosition = model.Position(position.x + model.stepSize * cos(toDouble(newHeading)),
//                                          position.y + model.stepSize * sin(toDouble(newHeading)))
//        new Bird {
//          val heading = newHeading
//          val position = newPosition
//        }
//    }
//
//    /** Bird orientation, returns the new heading of the bird
//      */
//    def updateHeading(flockmates: Seq[Bird],
//                      nearestNeighbourAndDist: Option[(Bird, Double)],
//                      minimumSeparation: Double,
//                      maxSeparateTurn: Angle,
//                      maxAlignTurn: Angle,
//                      maxCohereTurn: Angle): Heading = {
//    //   find-flockmates
//    //   if any? flockmates
//    //     [ find-nearest-neighbor
//    //       ifelse distance nearest-neighbor < minimum-separation
//    //         [ separate ]
//    //         [ align
//    //           cohere ] ]
//      nearestNeighbourAndDist match {
//        case None => heading
//        case Some((nearestNeighbour: Bird, nnDistance: Double)) =>
//          if (nnDistance < minimumSeparation) separate(nearestNeighbour.heading, maxSeparateTurn)
//          else alignAndCohere(flockmates, maxAlignTurn, maxCohereTurn)
//      }
//    }
//
//    def separate(nearestNeighbourHeading: Heading,
//                 maxSeparateTurn: Angle): Heading = {
//         // turn-away ([heading] of nearest-neighbor) max-separate-turn
//      val angle: Angle = heading angleTo nearestNeighbourHeading
//      val newHeading: Heading = heading - angle
//      heading + turnAtMost(newHeading, maxSeparateTurn)
//    }
//
//    def alignAndCohere(flockmates: Seq[Bird], maxAlignTurn: Angle, maxCohereTurn: Angle): Heading = {
//    // turn-towards average-flockmate-heading max-align-turn
//    // turn-towards average-heading-towards-flockmates max-cohere-turn
//      heading + turnAtMost(averageFlockmateHeading(flockmates), maxAlignTurn) + turnAtMost(averageHeadingTowardsFlockmates(flockmates), maxCohereTurn)
//    }
//
//    def averageFlockmateHeading(flockmates: Seq[Bird]): Heading = {
//    // ;; We can't just average the heading variables here.
//    // ;; For example, the average of 1 and 359 should be 0,
//    // ;; not 180.  So we have to use trigonometry.
//    // let x-component sum [dx] of flockmates
//    // let y-component sum [dy] of flockmates
//    // ifelse x-component = 0 and y-component = 0
//    //   [ report heading ]
//    //   [ report atan x-component y-component ]
//      val xComponent: Double = flockmates.map((b:Bird) => cos(toDouble(b.heading))).sum
//      val yComponent: Double = flockmates.map((b:Bird) => sin(toDouble(b.heading))).sum
//      if (xComponent == 0 && yComponent == 0) heading
//      else toHeading(atan2(yComponent, xComponent))
//    }
//
//    def averageHeadingTowardsFlockmates(flockmates: Seq[Bird]): Heading = {
//    // // ;; "towards myself" gives us the heading from the other turtle
//    // // ;; to me, but we want the heading from me to the other turtle,
//    // // ;; so we add 180
//    // // let x-component mean [sin (towards myself + 180)] of flockmates
//    // // let y-component mean [cos (towards myself + 180)] of flockmates
//    // // ifelse x-component = 0 and y-component = 0
//    // //   [ report heading ]
//    // //   [ report atan x-component y-component ]
//      val xComponent: Double = flockmates.map((b:Bird) => b.position.x - position.x).sum / flockmates.size
//      val yComponent: Double = flockmates.map((b:Bird) => b.position.y - position.y).sum / flockmates.size
//      if (xComponent == 0 && yComponent == 0) heading
//      else toHeading(atan2(yComponent, xComponent))
//    }
//
//    def turnAtMost(newHeading: Heading, maxTurn: Angle): Angle = {
//    // // ifelse abs turn > max-turn
//    // //   [ ifelse turn > 0
//    // //       [ rt max-turn ]
//    // //       [ lt max-turn ] ]
//    // //   [ rt turn ]
//      val turn: Angle = heading angleTo newHeading
//      if (turn.abs < maxTurn) turn
//      else if (turn >= toAngle(0)) maxTurn else -maxTurn
//    }
//
//    override def toString = s"Bird(x=${position.x}, y=${position.y}, heading=$heading)"
//  }
//
//  object Bird {
//    def apply(_p: Point, _heading: Heading): Bird =
//      new Bird {
//        val position: Point = _p
//        val heading: Heading = _heading
//      }
//  }
//
//  trait Model {
//    def worldWidth: Double
//    def worldHeight: Double
//    def populationSize: Int
//    def vision: Double
//    def minimumSeparation: Double
//    def maxAlignTurn: Angle
//    def maxCohereTurn: Angle
//    def maxSeparateTurn: Angle
//    def stepSize: Double
//
//    def randomBird: Bird = Bird(Position(Random.nextDouble() * worldWidth, Random.nextDouble() * worldHeight),
//                                Heading(Random.nextDouble() * 2*Pi))
//    def randomInit: GraphBirds = GraphBirds((1 to populationSize).map( _ => randomBird ))
//
//    def start(iterations: Int): GraphBirds = run(iterations, randomInit)
//
//    def run(iterations: Int, g: GraphBirds): GraphBirds =
//      if (iterations <= 0) g
//      else run(iterations - 1, oneStep(g))
//
//    def oneStep(g: GraphBirds): GraphBirds = buildGraph(updateBirds(g))
//
//    def forEachState[T](maxiter: Int, f: (Model, Int, GraphBirds) => T): Seq[T] = forEachState(0, maxiter, f, randomInit)
//    def forEachState[T](i: Int, maxiter: Int, f: (Model, Int, GraphBirds) => T, state: GraphBirds): List[T] =
//      f(this, i, state) :: (if (i < maxiter) forEachState(i + 1, maxiter, f, oneStep(state)) else List())
//
//    def updateBirds(graph: GraphBirds): Seq[Bird] =
//      (0 until graph.birds.size).map((b: Int) => graph.birds(b).update(
//        graph.flockmates(b).map((i:Int) => (graph.birds(i), distanceBetween(graph.birds(i).position, graph.birds(b).position))),
//        graph.nearestNeighbour(b).map((i:Int) => (graph.birds(i), distanceBetween(graph.birds(i).position, graph.birds(b).position))),
//        this))
//
//    def buildGraph(birds: Seq[Bird]): GraphBirds = GraphBirds(birds)
//
//    object Bird {
//      def apply(_position: Point, _heading: Heading) = new Flocking.Bird {
//        val position: Point = _position
//        val heading: Heading = _heading
//      }
//    }
//
//    object GraphBirds {
//      def apply(birds: Seq[Bird]) = Flocking.GraphBirds(birds, vision, distanceBetween)
//    }
//
//    def distanceBetween(p1: Point, p2: Point): Double = torusDistance(worldWidth,worldHeight)(p1,p2)
//
//    object Position {
//        val pChecker = PointBoundsKeeper(Point(0,0), Point(worldWidth, worldHeight))
//        def apply(x: Double, y: Double): Point = {
//          val pChecked = pChecker(Point(x,y))
//          Point(pChecked.x, pChecked.y)
//        }
//    }
//  }
//
//  def torusDistance(width:Double, height:Double)(p1: Point, p2: Point): Double = sqrt(pow(min(abs(p2.x - p1.x), width - abs(p2.x - p1.x)), 2) + pow(min(abs(p2.y - p1.y), height - abs(p2.y - p1.y)),2))
//
//  class ModelIterator(model: Model) {
//    var currentState = model.randomInit
//    def step = {
//      currentState = model.oneStep(currentState)
//    }
//  }
//
//  trait Behaviour {
//    // type NGroups = Int
//    // type AvgVelocity = Double
//    // type RelativeDiffusion = Double
//    // type B = ([NGroups], [AvgVelocity], [RelativeDiffusion])
//
//    val model: Model
//
//    def countGroups(gb: GraphBirds): Int = countGroups(gb, 0, (0 until gb.birds.size).toSet)
//    def countGroups(gb: GraphBirds, nclustersFound: Int, remaining: Set[Int]): Int = {
//      if (remaining.size == 0) nclustersFound
//      else countGroups(gb, nclustersFound + 1, remaining -- extractComponent(gb, remaining.head, Set()))
//    }
//    def extractComponent(gb: GraphBirds, start: Int, visited: Set[Int]): Set[Int] = {
//      if (gb.birds.size == 0) Set()
//      else {
//        val neighbours: Seq[Int] = gb.flockmates(start)
//        if (neighbours.size == 0) Set(start)
//        else neighbours.foldLeft(visited + start)((a:Set[Int], b:Int) => if (!a.contains(b)) extractComponent(gb, b, a) else a)
//      }
//    }
//
//    def nearestNeighbour(d: DistMatrix)(i: Int, birds: Seq[Bird]): Int = {
//      birds.indices.minBy(j => if (i != j) d(i, j) else Double.MaxValue)
//    }
//
//    def voronoiNeighbours(birds: Seq[Bird], dm: DistMatrix): Seq[Seq[Int]] = {
//      val nnf = nearestNeighbour(dm)_
//      val nn = for {i <- birds.indices} yield nnf(i, birds)
//      for {i <- birds.indices} yield voronoiNeighbours(birds, nn, i)
//    }
//    def voronoiNeighbours(birds: Seq[Bird], nearestNeigh: Seq[Int], i: Int): Seq[Int] =
//      for {j <- birds.indices if ((i != j) && nearestNeigh(j) == i)} yield j
//
//    def kNearestNeighbours(k: Int, birds:Seq[Bird], dm: DistMatrix): Seq[Seq[Int]] = {
//      def insert(x: Int, k: Int, nn: List[Int], distFromI: Int => Double): List[Int] =
//        if (k == 0) List()
//        else if (nn.size == 0) List(x)
//        else if (distFromI(x) < distFromI(nn.head)) (x :: nn) take k
//        else nn.head :: insert(x, k - 1, nn.tail, distFromI)
//      def knn(i: Int): Seq[Int] =
//        birds.indices.foldRight(List[Int]())((j,nn) => if (j == i) nn else insert(j, k, nn, {dm(i,_)}))
//      birds.indices.map(knn(_))
//    }
//
//    def distBetween(neighbours: Seq[Seq[Int]], dm: DistMatrix): Seq[Seq[Double]] =
//      neighbours.indices.map((i: Int) => neighbours(i).map((j: Int) => dm(i,j)))
//
//    def sumOver(is: Range, f: Int => Double): Double = (is map f).sum
//    def averageOver(is: Range, f: Int => Double): Double =
//      sumOver(is, f) / (is.size: Double)
//
//    def relativeDiffusion(neighboursDistAtT1: Seq[Seq[Double]],
//                          neighboursDistAtT2: Seq[Seq[Double]]): Double = {
//      averageOver(neighboursDistAtT1.indices, {i => {
//          val ni: Double = neighboursDistAtT1(i).size
//          (1 / ni) * sumOver(neighboursDistAtT1(i).indices, {j =>
//            1 - (pow(neighboursDistAtT1(i)(j), 2) / pow(neighboursDistAtT2(i)(j), 2))
//            })
//          }
//        })
//    }
//
//    abstract class AbstractCollector[S, +T]
//    case class Collector[S, +T](when: Int, f: S => AbstractCollector[S,T]) extends AbstractCollector[S, T] {
//      def collect(modelstate: S): AbstractCollector[S, T] = f(modelstate)
//    }
//    case class Val[S,+T](f: T) extends AbstractCollector[S, T]
//
//    def collectCountGroups(state: GraphBirds): Double =
//      countGroups(state) / (model.populationSize: Double)
//    val countGroupsCollector: Collector[GraphBirds, Double] =
//      Collector(300, { (s: GraphBirds) => Val(collectCountGroups(s)) })
//
//    def collectRelativeDiffusion(state1: GraphBirds)(state2: GraphBirds): Double = {
//      val dm = DistMatrix(state1.birds.map(_.position), model.distanceBetween)
//      val neighbs = kNearestNeighbours(3,state1.birds, dm)
//      val dist1 = distBetween(neighbs, dm)
//      relativeDiffusion(dist1, distBetween(neighbs, DistMatrix(state2.birds.map(_.position), model.distanceBetween)))
//    }
//    val relativeDiffusionCollector: Collector[GraphBirds, Double] =
//      Collector(200, { (s1:GraphBirds) =>
//        Collector(300, { (s2: GraphBirds) => Val(collectRelativeDiffusion(s1)(s2))})
//        })
//
//    def collectVelocity(state1: GraphBirds)(state2: GraphBirds): Double =
//      (state1.birds zip state2.birds).map(x => model.distanceBetween(x._1.position, x._2.position)).sum / (state1.birds.size: Double)
//    val velocityCollector: Collector[GraphBirds, Double] =
//      Collector(298, { (s1:GraphBirds) =>
//        Collector(300, { (s2:GraphBirds) => Val(collectVelocity(s1)(s2))})
//      })
//
//    def constructDescription(collectors: Seq[AbstractCollector[GraphBirds, Double]], gb: GraphBirds, iter: Int): Seq[Double] =
//      if (collectors.exists(x => x match {case Collector(_,_) => true
//                                          case Val(_) => false })) {
//        val updatedCollectors: Seq[AbstractCollector[GraphBirds, Double]] = collectors.map(x => x match {case Collector(i,f) => if (i == iter) f(gb) else x
//                                                             case Val(_) => x})
//        val updatedState = model.oneStep(gb)
//        constructDescription(updatedCollectors, updatedState, iter + 1)
//      }
//      else collectors.map(_ match { case Val(x) => x } )
//
//    def defaultDescription = constructDescription(List(countGroupsCollector, relativeDiffusionCollector, velocityCollector), model.randomInit, 0)
//  }
//
//  trait DistMatrix {
//    val distances: Vector[Vector[Double]]
//    def apply(i: Int,j: Int): Double =
//      if (i == j) 0
//      else if (i < j) distances(i)(j - i - 1)
//           else apply(j,i)
//  }
//  object DistMatrix {
//    def apply(points: Seq[Point], distFunc: (Point, Point) => Double): DistMatrix = new DistMatrix {
//      val distances: Vector[Vector[Double]] = (for {i <- 0 until (points.size - 1)} yield (for {j <- i+1 until points.size} yield distFunc(points(i), points(j))).toVector).toVector
//    }
//    def euclidean(p1: Point, p2: Point): Double = sqrt(pow(p1.x - p2.x, 2) + pow(p1.y - p2.y,2))
//  }
//
//  def apply(_populationSize : Int,
//            _vision: Double,
//            _minimumSeparation: Double,
//            _stepSize: Double,
//            _maxAlignTurn: Double,
//            _maxCohereTurn: Double,
//            _maxSeparateTurn: Double
//            ) = {
//    new Behaviour {
//      val model = new Model {
//      val worldWidth: Double = 1
//      val worldHeight: Double = 1
//      val populationSize: Int = _populationSize
//      val vision: Double = _vision
//      val minimumSeparation: Double = _minimumSeparation
//      val maxAlignTurn: Angle = toAngle(_maxAlignTurn)
//      val maxCohereTurn: Angle = toAngle(_maxCohereTurn)
//      val maxSeparateTurn: Angle = toAngle(_maxSeparateTurn)
//      val stepSize: Double = _stepSize
//      }
//    }.defaultDescription
//  }
//}
//
