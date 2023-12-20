
package flocking.model

import scala.math._
import scala.util.Random
import flocking._
import flocking.model.tools._
import datatypes._

object Model {
  def distanceBetween(width: Double, height: Double, p1: Point, p2: Point): Double = torusDistance(width, height)(p1,p2)

  def run(model: Model, iterations: Int, g: GraphBirds): GraphBirds =
    if (iterations <= 0) g
    else run(model, iterations - 1, oneStep(model, g))

  def oneStep(model: Model, g: GraphBirds): GraphBirds = model.buildGraph(model.updateBirds(g))

  def randomInit(model: Model, environment: Environment[Int], random: Random): GraphBirds =
    def randomBird(id: Int): Bird =
      Bird(
        id,
        Position(model.worldHeight, model.worldWidth, random.nextDouble() * model.worldWidth, random.nextDouble() * model.worldHeight),
        Heading(random.nextDouble() * 2*Pi),
        environment,
        model.vision // vision obstacles
      )

    val birds =  (0 to model.populationSize - 1).zipWithIndex.map( (id, _) => randomBird(id) )

    GraphBirds(
      birds,
      model.vision,
      distanceBetween(model.worldWidth, model.worldHeight, _, _)
    )


  def start(model: Model, environment: Environment[Int], iterations: Int, random: Random) = run(model, iterations, randomInit(model, environment, random))


}

case class Model(
  worldWidth: Double,
  worldHeight: Double,
  populationSize: Int,
  vision: Double,
  minimumSeparation: Double,
  maxAlignTurn: Angle,
  maxCohereTurn: Angle,
  maxSeparateTurn: Angle,
  stepSize: Double) {



//  def forEachState[T](maxiter: Int, f: (Model, Int, GraphBirds) => T): Seq[T] = forEachState(0, maxiter, f, randomInit)
//  def forEachState[T](i: Int, maxiter: Int, f: (Model, Int, GraphBirds) => T, state: GraphBirds): List[T] =
//    f(this, i, state) :: (if (i < maxiter) forEachState(i + 1, maxiter, f, oneStep(state)) else List())

  def updateBirds(graph: GraphBirds): Seq[Bird] = {
    graph.birds.map(_.update(graph, this))
//    (0 until graph.birds.size).map(
//      b =>
//        graph.birds(b).update(
//          graph.flockmates(b).map(i => (graph.birds(i), distanceBetween(graph.birds(i).position, graph.birds(b).position))),
//          graph.nearestNeighbour(b).map(i => (graph.birds(i), distanceBetween(graph.birds(i).position, graph.birds(b).position))),
//          this)
//    )
  }

  def buildGraph(birds: Seq[Bird]): GraphBirds = GraphBirds(birds, vision, distanceBetween)

  def distanceBetween(p1: Point, p2: Point): Double = torusDistance(worldWidth,worldHeight)(p1,p2)


}

def torusDistance(width:Double, height:Double)(p1: Point, p2: Point): Double = sqrt(pow(min(abs(p2.x - p1.x), width - abs(p2.x - p1.x)), 2) + pow(min(abs(p2.y - p1.y), height - abs(p2.y - p1.y)),2))







//package flocking.model
//
//import scala.math._
//import scala.util.Random
//import java.awt.Color
//
//import flocking._
//import flocking.model.datatypes._
//import flocking.model.tools._
//
//case class Model(
//  worldWidth: Double,
//  worldHeight: Double,
//  envDivsHorizontal: Int,
//  envDivsVertical: Int,
//  populationSize: Int,
//  vision: Double,
//  visionObstacle: Double,
//  minimumSeparation: Double,
//  maxAlignTurn: Angle,
//  maxCohereTurn: Angle,
//  maxSeparateTurn: Angle,
//  stepSize: Double) {
//
//  def emptySpace: Int = new Color(0,0,0).getRGB()
//
//  lazy val env = Environment.empty(emptySpace, envDivsHorizontal, envDivsVertical, worldWidth, worldHeight)
//
//  def randomBird(id: Int): Bird = Bird(id, Position(Random.nextDouble() * worldWidth, Random.nextDouble() * worldHeight),
//    Heading.fromDouble(Random.nextDouble() * 2*Pi))
//  def randomInit: GraphBirds = GraphBirds((0 until populationSize).map( i => randomBird(i) ), vision, distanceBetween)
//
//  def start(iterations: Int): GraphBirds = run(iterations, randomInit)
//
//  def run(iterations: Int, g: GraphBirds): GraphBirds =
//    if (iterations <= 0) g
//    else run(iterations - 1, oneStep(g))
//
//  def oneStep(g: GraphBirds): GraphBirds = buildGraph(updateBirds(g))
//
//  def forEachState[T](maxiter: Int, f: (Model, Int, GraphBirds) => T): Seq[T] = forEachState(0, maxiter, f, randomInit)
//  def forEachState[T](i: Int, maxiter: Int, f: (Model, Int, GraphBirds) => T, state: GraphBirds): List[T] =
//    f(this, i, state) :: (if (i < maxiter) forEachState(i + 1, maxiter, f, oneStep(state)) else List())
//
//  def updateBirds(graph:GraphBirds): Seq[Bird] = graph.birds.indices.map((b: Int) => graph.birds(b).update(graph, this))
//
//  def buildGraph(birds: Seq[Bird]): GraphBirds = GraphBirds(birds, vision, distanceBetween)
//
//  def distanceBetween(p1: Point, p2: Point): Double = Distance.torus(worldWidth,worldHeight)(p1,p2)
//
//  object Position {
//    val pChecker = PointBoundsKeeper(Point(0,0), Point(worldWidth, worldHeight))
//    def apply(x: Double, y: Double): Point = {
//      val pChecked = pChecker(Point(x,y))
//      Point(pChecked.x, pChecked.y)
//    }
//  }
//}
//
//class ModelIterator(val model: Model) {
//  var currentState = model.randomInit
//  def step = {
//    currentState = model.oneStep(currentState)
//  }
//}
