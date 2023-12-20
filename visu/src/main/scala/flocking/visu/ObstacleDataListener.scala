package flocking.visu

import java.awt.event._
import java.io._
import java.lang.Thread
import javax.swing.Timer

class ObstacleDataListener(delay: Int, inputPipe: String, inputMatrixSize: (Int, Int), sampleSize: (Int, Int), f: (Int,Int,Byte) => Unit) extends Thread {

  require(inputMatrixSize._1 % sampleSize._1 == 0 && inputMatrixSize._2 % sampleSize._2 == 0)

  //val fi = new File(inputPipe)
  //val fis: FileInputStream = new FileInputStream(inputPipe)
  var fis: FileInputStream = null
  var continue: Boolean = true

  def quit() = {
    continue = false
  }

  override def run() = {
    var i: Int = 0
    var j: Int = 0
    while(continue) {
      //discard present data if any and wait for the next frame
      // skipRead(fis.available())
      // //read and discard until a START tag (int 255) is found
      // while (fis.read() != 255) {}

      fis = new FileInputStream(inputPipe)
      val bytes: Array[Byte] = new Array[Byte](inputMatrixSize._1 * inputMatrixSize._2)
      fis.read(bytes)

      j = 0
      while (j < sampleSize._2) {
        i = 0
        while (i < sampleSize._1) {
          f(i,j, bytes((j * inputMatrixSize._2 / sampleSize._2) * inputMatrixSize._1 + (i * inputMatrixSize._1 / sampleSize._1)))
          i = i + 1
        }
        j = j + 1
      }


      Thread.sleep(delay)
    }
  }

  def skipRead(n: Int) = {
    var skipCpt = 0
    while (skipCpt < n) {
      fis.read()
      skipCpt += 1
    }
  }
}


// class ObstacleDataListener(delay: Int, inputPipe: String, inputMatrixSize: (Int, Int), sampleSize: (Int, Int), f: (Int,Int,Int) => Unit) extends ActionListener {

//   //require(inputMatrixSize._1 % sampleSize._1 == 0 && inputMatrixSize._2 % sampleSize._2 == 0)

//   val timer = new Timer(delay, this)
//   //val fi = new File(inputPipe)
//   val fis = new FileInputStream(inputPipe)

//   def start: Unit = {
//     println("Starting timer")
//     timer.start
//     println("Timer Started")
//   }

//   def actionPerformed(e: ActionEvent) {
//     // println(s"Retrieving input data from $inputPipe")

//     // while(fis.available() > 0)
//     //   println(s"${fis.read()}")
//     //discard present data if any and wait for the next frame
//     skipRead(fis.available())
//     //read and discard until a START tag (int 255) is found
//     while (fis.read() != 255) {}

//     for (j <- 0 until sampleSize._2) {
//       for (i <- 0 until sampleSize._1) {
//         f(i,j, fis.read())
//         skipRead(inputMatrixSize._1 / sampleSize._1 - 1)
//       }
//       skipRead((inputMatrixSize._2 / sampleSize._2 - 1) * inputMatrixSize._1)
//     }
//   }

//   def skipRead(n: Int) {
//     var skipCpt = 0
//     while (skipCpt < n) {
//       fis.read()
//       skipCpt += 1
//     }
//   }
// }
