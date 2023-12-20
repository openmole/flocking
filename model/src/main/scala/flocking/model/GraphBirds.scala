package flocking.model

import flocking.model.datatypes._

object GraphBirds {
  def apply(birds: Seq[Bird], vision: Double, distFunc: (Point, Point) => Double): GraphBirds = {
    val mates =
      for {u <- 0 until birds.size}
        yield for {v <- 0 until birds.size if ((v != u) && distFunc(birds(u).position,birds(v).position) <= vision)}
          yield v

    new GraphBirds(
      birds = birds,
      flockmates = mates,
      nearestNeighbour =
        for {u <- 0 until birds.size}
          yield
            mates(u).size match {
              case 0 => None
              case _ => Some(closestFlockmate(birds, u, mates(u), distFunc))
            }
    )
  }

  def closestFlockmate(birds: Seq[Bird], u: Int, flockmates:Seq[Int], distFunc: (Point, Point) => Double): Int =
    flockmates.reduce((a,b) => if (distFunc(birds(u).position,birds(a).position) < distFunc(birds(u).position,birds(b).position)) a
    else b)
}

case class GraphBirds(birds: Seq[Bird], flockmates: Seq[Seq[Int]], nearestNeighbour: Seq[Option[Int]]) {
  def areFlockmates(b1: Int, b2: Int) = flockmates(b1).contains(b2)
  override def toString = "Birds: " ++ birds.toString ++ "\nFlockmates: " ++ flockmates.toString ++ "\nNearestNeighbours: " ++ nearestNeighbour.toString
}
