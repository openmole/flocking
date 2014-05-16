package flocking

import flocking.datatypes._
import flocking.birds._

trait GraphBirds {
  val birds: Seq[Bird]
  val flockmates: Seq[Seq[Int]]
  val nearestNeighbour: Seq[Option[Int]]

  def areFlockmates(b1: Int, b2: Int) = flockmates(b1).contains(b2)

  override def toString = "Birds: " ++ birds.toString ++ "\nFlockmates: " ++ flockmates.toString ++ "\nNearestNeighbours: " ++ nearestNeighbour.toString
}


object GraphBirds {
  def apply(_birds: Seq[Bird], vision: Double, distFunc: (Point, Point) => Double): GraphBirds = {

    new GraphBirds {
      val birds: Seq[Bird] = _birds
      val flockmates: Seq[Seq[Int]] =
        for {u <- 0 until birds.size}
        yield for {v <- 0 until birds.size if ((v != u) && distFunc(birds(u).position,birds(v).position) <= vision)}
              yield v
      val nearestNeighbour: Seq[Option[Int]] =
        for {u <- 0 until birds.size}
        yield (flockmates(u).size match {case 0 => None
                                        case _ => Some(closestFlockmate(birds, u,flockmates(u), distFunc))})
    }
  }

  def closestFlockmate(birds: Seq[Bird], u: Int, flockmates:Seq[Int], distFunc: (Point, Point) => Double): Int =
    flockmates.reduce((a,b) => if (distFunc(birds(u).position,birds(a).position) < distFunc(birds(u).position,birds(b).position)) a
                               else b)
}
