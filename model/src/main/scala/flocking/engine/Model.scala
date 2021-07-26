package flocking.engine

import scala.math._
import scala.util.Random
import java.awt.Color

import flocking._
import flocking.datatypes._
import flocking.environment._
import flocking.tools._
import flocking.birds._
import flocking.interactions._
import flocking.birds._

case class Model(
  worldWidth: Double,
  worldHeight: Double,
  envDivsHorizontal: Int,
  envDivsVertical: Int,
  populationSize: Int,
  vision: Double,
  visionObstacle: Double,
  minimumSeparation: Double,
  maxAlignTurn: Angle,
  maxCohereTurn: Angle,
  maxSeparateTurn: Angle,
  stepSize: Double) {

  def emptySpace: Int = new Color(0,0,0).getRGB()

  lazy val env = Environment.empty(emptySpace, envDivsHorizontal, envDivsVertical, worldWidth, worldHeight)

  def randomBird(id: Int): Bird = Bird(id, Position(Random.nextDouble() * worldWidth, Random.nextDouble() * worldHeight),
    Heading.fromDouble(Random.nextDouble() * 2*Pi))
  def randomInit: GraphBirds = GraphBirds((0 until populationSize).map( i => randomBird(i) ), vision, distanceBetween)

  def start(iterations: Int): GraphBirds = run(iterations, randomInit)

  def run(iterations: Int, g: GraphBirds): GraphBirds =
    if (iterations <= 0) g
    else run(iterations - 1, oneStep(g))

  def oneStep(g: GraphBirds): GraphBirds = buildGraph(updateBirds(g))

  def forEachState[T](maxiter: Int, f: (Model, Int, GraphBirds) => T): Seq[T] = forEachState(0, maxiter, f, randomInit)
  def forEachState[T](i: Int, maxiter: Int, f: (Model, Int, GraphBirds) => T, state: GraphBirds): List[T] =
    f(this, i, state) :: (if (i < maxiter) forEachState(i + 1, maxiter, f, oneStep(state)) else List())

  def updateBirds(graph:GraphBirds): Seq[Bird] = graph.birds.indices.map((b: Int) => graph.birds(b).update(graph, this))

  def buildGraph(birds: Seq[Bird]): GraphBirds = GraphBirds(birds, vision, distanceBetween)

  def distanceBetween(p1: Point, p2: Point): Double = Distance.torus(worldWidth,worldHeight)(p1,p2)

  object Position {
    val pChecker = PointBoundsKeeper(Point(0,0), Point(worldWidth, worldHeight))
    def apply(x: Double, y: Double): Point = {
      val pChecked = pChecker(Point(x,y))
      Point(pChecked.x, pChecked.y)
    }
  }
}

class ModelIterator(val model: Model) {
  var currentState = model.randomInit
  def step = {
    currentState = model.oneStep(currentState)
  }
}
