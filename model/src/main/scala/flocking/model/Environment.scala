package flocking.model

import flocking.model.datatypes._
import flocking.model.tools._

import scala.reflect.ClassTag


case class Environment[T](
  nCellsWide: Int,
  nCellsHigh: Int,
  width: Double,
  height: Double,
  var pixels: Array[T],
  val emptySpace: T) {

  lazy val widthBoundsKeeper = DoubleBoundsKeeper(0, width)
  lazy val heightBoundsKeeper = DoubleBoundsKeeper(0, height)

  def set(i: Int, j: Int, v: T) = pixels(j * nCellsWide + i) = v
  def get(i: Int, j: Int): T = pixels(j * nCellsWide + i)
  // def get(x: Double, y: Double): T = {
  //   val i = if (x == width) nCellsWide - 1 else x2i(x)
  //   val j = if (y == height) nCellsWide - 1 else y2j(y)
  //   get(i,j)
  // }
  def set(x: Double, y: Double, v:T): Unit =
    set(x2i(widthBoundsKeeper(x)),y2j(heightBoundsKeeper(y)), v)

  def get(x: Double, y: Double): T =
    get(x2i(widthBoundsKeeper(x)),y2j(heightBoundsKeeper(y)))
  //def getTorus(x: Double, y: Double): T =
  // def getNormal(x: Double, y:Double): T = {
  //   val i = if (x == 1.0) nCellsWide - 1 else x2i(x)
  //   val j = if (y == 1.0) nCellsWide - 1 else y2j(y)
  //   get(i,j)
  // }

  /** returns the discreete coordinate (cell position) corresponding to the real coordinate given */
  def x2i(x: Double): Int = ((x / width) * nCellsWide).toInt
  def y2j(y: Double): Int = ((y / height) * nCellsHigh).toInt

  def i2x(i: Int): Double = (((i:Double) / nCellsWide) * width) + (width / nCellsWide / 2.0)
  def j2y(j: Int): Double = (((j:Double) / nCellsHigh) * height) + (height / nCellsHigh / 2.0)

  def addDisc(x:Double, y:Double, r:Double, fill: T) = {
    val center = Point(x,y)
    for (i <- 0 until nCellsWide; j<- 0 until nCellsHigh) {
      if (Distance.torus(width, height)(center, Point(i2x(i),j2y(j))) <= r) {
        set(i,j,fill)
      }
    }
  }

}

object Environment {

  def isEmpty[T](environment: Environment[T], x: Double, y: Double) = Environment.get(environment, x, y) == emptySpace(environment)
  def emptySpace[T](environment: Environment[T]) = environment.emptySpace
  def get[T](environment: Environment[T], x: Double, y: Double) = environment.get(x, y)


  def empty(width: Double, height: Double) =
    buildEmpty[Int](0, 1, 1, width, height)

  def buildEmpty[T: ClassTag](f :T, _nCellsWide: Int, _nCellsHigh: Int, _width: Double, _height: Double) = Environment[T](
    nCellsWide = _nCellsWide,
    nCellsHigh = _nCellsHigh,
    width = _width,
    height = _height,
    pixels = Array.fill(_nCellsHigh * _nCellsWide)(f),
    emptySpace = f
  )

}

