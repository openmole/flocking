package flocking.environment


import flocking.tools._
import flocking.datatypes._
import scala.reflect.ClassTag

trait Environment[T] {
  val nCellsWide: Int
  val nCellsHigh: Int
  val width: Double
  val height: Double
  var pixels: Array[T]
  val emptySpace: T
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

  def addDisc(x:Double,y:Double,r:Double, fill: T) = {
    val center = Point(x,y)
    for (i <- 0 until nCellsWide; j<- 0 until nCellsHigh) {
      if (Distance.torus(width, height)(center, Point(i2x(i),j2y(j))) <= r) {
        set(i,j,fill) 
      }
    }
  }

}

object Environment {
  def empty[T:ClassTag](f:T, _nCellsWide: Int, _nCellsHigh: Int, _width: Double, _height: Double): Environment[T] = new Environment[T] {
    val nCellsWide = _nCellsWide
    val nCellsHigh = _nCellsHigh
    val width = _width
    val height = _height
    var pixels = Array.fill(nCellsHigh * nCellsWide)(f)
    val emptySpace = f
  }

  // def from[T:ClassTag](data: Array[Array[T]], _nCellsWide: Int, _nCellsHigh: Int, _width: Double, _height: Double) = new Environment[T] {
  //   val nCellsWide = _nCellsWide
  //   val nCellsHigh = _nCellsHigh
  //   val width = _width
  //   val height = _height
  //   var pixels = Array.tabulate(nCellsHigh)(j => 
  //     Array.tabulate(nCellsWide)(i => 
  //       data(j)(i)
  //       ))
  // }
}

