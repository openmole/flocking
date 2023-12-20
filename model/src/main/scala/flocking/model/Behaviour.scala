package flocking.model

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

import java.util.Random

object Behaviour:

  def countGroups(gb: GraphBirds): Int = countGroups(gb, 0, (0 until gb.birds.size).toSet)
  def countGroups(gb: GraphBirds, nclustersFound: Int, remaining: Set[Int]): Int =
    if (remaining.size == 0) nclustersFound
    else countGroups(gb, nclustersFound + 1, remaining -- extractComponent(gb, remaining.head, Set()))

  def extractComponent(gb: GraphBirds, start: Int, visited: Set[Int]): Set[Int] =
    if gb.birds.size == 0
    then Set()
    else
      val neighbours: Seq[Int] = gb.flockmates(start)
      if (neighbours.size == 0) Set(start)
      else neighbours.foldLeft(visited + start)((a:Set[Int], b:Int) => if (!a.contains(b)) extractComponent(gb, b, a) else a)


  def nearestNeighbour(d: DistMatrix)(i: Int, birds: Seq[Bird]): Int =
    birds.indices.minBy(j => if (i != j) d(i, j) else Double.MaxValue)

  def voronoiNeighbours(birds: Seq[Bird], dm: DistMatrix): Seq[Seq[Int]] =
    val nnf = nearestNeighbour(dm)_
    val nn = for {i <- birds.indices} yield nnf(i, birds)
    for {i <- birds.indices} yield voronoiNeighbours(birds, nn, i)

  def voronoiNeighbours(birds: Seq[Bird], nearestNeigh: Seq[Int], i: Int): Seq[Int] =
    for {j <- birds.indices if ((i != j) && nearestNeigh(j) == i)} yield j

  def kNearestNeighbours(k: Int, birds:Seq[Bird], dm: DistMatrix): Seq[Seq[Int]] =
    def insert(x: Int, k: Int, nn: List[Int], distFromI: Int => Double): List[Int] =
      if (k == 0) List()
      else if (nn.size == 0) List(x)
      else if (distFromI(x) < distFromI(nn.head)) (x :: nn) take k
      else nn.head :: insert(x, k - 1, nn.tail, distFromI)

    def knn(i: Int): Seq[Int] =
      birds.indices.foldRight(List[Int]())((j,nn) => if (j == i) nn else insert(j, k, nn, {dm(i,_)}))

    birds.indices.map(knn(_))


  def distBetween(neighbours: Seq[Seq[Int]], dm: DistMatrix): Seq[Seq[Double]] =
    neighbours.indices.map((i: Int) => neighbours(i).map((j: Int) => dm(i,j)))

  def sumOver(is: Range, f: Int => Double): Double = (is map f).sum
  def averageOver(is: Range, f: Int => Double): Double =
    sumOver(is, f) / (is.size: Double)

  def relativeDiffusion(
    neighboursDistAtT1: Seq[Seq[Double]],
    neighboursDistAtT2: Seq[Seq[Double]]): Double =
    averageOver(
      neighboursDistAtT1.indices,
      i =>
        val ni = neighboursDistAtT1(i).size
        (1.0 / ni) *
          sumOver(
            neighboursDistAtT1(i).indices,
            j => 1 - (math.pow(neighboursDistAtT1(i)(j), 2) / math.pow(neighboursDistAtT2(i)(j), 2))
          )
    )


  //abstract class AbstractCollector[S, +T]
//  case class Collector[S, +T](when: Int, f: S => Val[T])  {
//    def collect(modelstate: S): Val[T] = f(modelstate)
//  }
//
//  case class Val[+T](f: T)

  def countGroups(model: Model, state: GraphBirds): Double =
    countGroups(state) / (model.populationSize.toDouble)

//  def countGroupsCollector(step: Int, model: Model): Collector[GraphBirds, Double] =
//    Collector(step, { (s: GraphBirds) => Val(collectCountGroups(model, s)) })

  def relativeDiffusion(model: Model, state1: GraphBirds, state2: GraphBirds): Double =
    val dist1 =
      val dm = DistMatrix(state1.birds.map(_.position), model.distanceBetween)
      val neighbs = kNearestNeighbours(3, state1.birds, dm)
      distBetween(neighbs, dm)

    val dist2 =
      val dm = DistMatrix(state2.birds.map(_.position), model.distanceBetween)
      val neighbs = kNearestNeighbours(3, state2.birds, dm)
      distBetween(neighbs, dm)

    relativeDiffusion(dist1, dist2)


//  def relativeDiffusionCollector(model: Model): Collector[GraphBirds, Double] =
//    Collector(200, { (s1:GraphBirds) =>
//      Collector(300, { (s2: GraphBirds) => Val(collectRelativeDiffusion(model, s1)(s2))})
//    })

  def velocity(model: Model, state1: GraphBirds, state2: GraphBirds): Double =
    (state1.birds.sortBy(_.id) zip state2.birds.sortBy(_.id)).map(x => model.distanceBetween(x._1.position, x._2.position)).sum / state1.birds.size.toDouble

//  def velocityCollector(model: Model): Collector[GraphBirds, Double] =
//    Collector(298, { (s1:GraphBirds) =>
//      Collector(300, { (s2:GraphBirds) => Val(collectVelocity(model, s1)(s2))})
//    })
//
//  def constructDescription(model: Model, collectors: Seq[Collector[GraphBirds, Double]], result: List[Val[GraphBirds, Double]], gb: GraphBirds, maxIter: Int, iter: Int): Seq[Double] =
//    if iter >= maxIter
//    then
//      val updatedCollectors =
//        collectors.map {
//          case x@Collector(i, f) if (i == iter) => f(gb)
//          case x => x
//        }
//      val updatedState = Model.oneStep(model, gb)
//      constructDescription(model, updatedCollectors, updatedState, maxIter, iter + 1)
//    else collectors.collect { case Val(x) => x }
//    //else collectors.map { case Val(x) => x }
//
//  def defaultDescription(model: Model, environment: Environment[Int], maxIter: Int, maxIter: Int, random: Random) =
//    constructDescription(model, List(countGroupsCollector(model), relativeDiffusionCollector(model), velocityCollector(model)), Model.randomInit(model, environment, random), maxIter, 0)

  def computeBehaviour(model: Model, environment: Environment[Int], random: Random) =

    val steps = 500
    val s1 = 449
    val s2 = 499
//    val td = (math.min(model.worldWidth, model.worldHeight) / (4 * model.stepSize)).toInt
//    val tv = (math.min(model.worldWidth, model.worldHeight) / (2 * model.stepSize)).toInt

    val states = Iterator.iterate(Model.randomInit(model, environment, random))(Model.oneStep(model, _)).take(steps).toArray

//    val diffusion =
//      states.map { s =>
//         val s1 = iteration - (s - 1) * td
//         val s2 = iteration - s * td
//        relativeDiffusion(model, states(s1 - 1), states(s2 - 1))
//      }.sum / 5


    val diffusion = relativeDiffusion(model, states(s1), states(s2))

//    val velocity =
//      (1 to 5).map { s =>
//        val s1 = iteration - (s - 1) * tv
//        val s2 = iteration - s * tv
//        Behaviour.velocity(model, states(s1 - 1), states(s2 - 1))
//      }.sum / 5

    val velocity = Behaviour.velocity(model, states(s1), states(s2))

    val bigest = states(s2).flockmates.map(_.size).max.toDouble

    Seq(diffusion, velocity, bigest)


import datatypes.*

import java.util.Random
import scala.util.Random
trait DistMatrix:
  val distances: Vector[Vector[Double]]
  def apply(i: Int,j: Int): Double =
    if (i == j) 0
    else if (i < j) distances(i)(j - i - 1)
    else apply(j,i)


object DistMatrix:
  def apply(points: Seq[Point], distFunc: (Point, Point) => Double): DistMatrix = new DistMatrix {
    val distances: Vector[Vector[Double]] = (for {i <- 0 until (points.size - 1)} yield (for {j <- i+1 until points.size} yield distFunc(points(i), points(j))).toVector).toVector
  }
  def euclidean(p1: Point, p2: Point): Double = math.sqrt(math.pow(p1.x - p2.x, 2) + math.pow(p1.y - p2.y,2))


object BehaviourTest extends App {

  val model =
    Model(
      worldWidth = 1,
      worldHeight = 1,
      populationSize = 200,
      vision = 10 / 70.0,
      minimumSeparation = 1 / 70.0,
      maxAlignTurn = Angle(math.toRadians(5)),
      maxCohereTurn = Angle(math.toRadians(3)),
      maxSeparateTurn = Angle(math.toRadians(1.5)),
      stepSize = 0.2 / 70.0
    )

  val environment = Environment.empty(model.worldWidth, model.worldHeight)

  println(Behaviour.computeBehaviour(model, environment, new java.util.Random(22)))
//  println(Behaviour.defaultDescription(model, environment, 1000, new java.util.Random(100)))


}

//
//def apply(_populationSize : Int,
//          _vision: Double,
//          _minimumSeparation: Double,
//          _stepSize: Double,
//          _maxAlignTurn: Double,
//          _maxCohereTurn: Double,
//          _maxSeparateTurn: Double
//         ) = {
//  new Behaviour {
//    val model = new Model {
//      val worldWidth: Double = 1
//      val worldHeight: Double = 1
//      val populationSize: Int = _populationSize
//      val vision: Double = _vision
//      val minimumSeparation: Double = _minimumSeparation
//      val maxAlignTurn: Angle = toAngle(_maxAlignTurn)
//      val maxCohereTurn: Angle = toAngle(_maxCohereTurn)
//      val maxSeparateTurn: Angle = toAngle(_maxSeparateTurn)
//      val stepSize: Double = _stepSize
//    }
//  }.defaultDescription
//}
