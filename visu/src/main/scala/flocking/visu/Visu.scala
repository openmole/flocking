package flocking.visu

import java.awt.Graphics
import java.awt.{Point => JPoint}
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape
import java.awt.geom.Line2D
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.GraphicsEnvironment
import java.awt.GraphicsDevice
import java.awt.GraphicsConfiguration
import javax.imageio.ImageIO
import java.io.File
import java.awt.BasicStroke

import javax.swing.Timer

import scala.swing._
import scala.swing.RichWindow._
import scala.swing.event._
import scala.math._

import flocking._
import flocking.model.datatypes._
import flocking.model._

object Visu:
  def apply(model: Model, environment: Environment, random: java.util.Random, pixelWidth: Int, pixelHeight: Int, frameDelay: Int, birdLength:Double, birdWidth:Double) =
    new Visu(model, environment, random, pixelWidth, pixelHeight, frameDelay, birdLength, birdWidth, fullScreen = false)

  def fullScreen(model: Model, environment: Environment, random: java.util.Random, frameDelay: Int, birdLength:Double, birdWidth:Double) =
    val gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
    val pixelWidth: Int = gd.getDisplayMode().getWidth()
    val pixelHeight: Int = gd.getDisplayMode().getHeight()
    new Visu(model, environment, random, pixelWidth, pixelHeight, frameDelay, birdLength, birdWidth, fullScreen = true)

  class ModelIterator(model: Model, var currentState: GraphBirds):
    def step =
      currentState = Model.oneStep(model, currentState)


  lazy val backgroundColorRGB:Int = new Color(0,0,0).getRGB()
  lazy val obstacleColorRGB:Int = new Color(0,0,255).getRGB()
  lazy val birdColor: Color = new Color(255,255,255)


class Visu(model: Model, environment: Environment, random: java.util.Random, pixelWidth: Int, pixelHeight: Int, frameDelay: Int, birdLength:Double, birdWidth:Double, fullScreen: Boolean) {

  lazy val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
  lazy val gd = ge.getDefaultScreenDevice()
  lazy val gc = gd.getDefaultConfiguration()

  lazy val backgroundImage: BufferedImage = new BufferedImage(environment.nCellsWide, environment.nCellsHigh, BufferedImage.TYPE_INT_RGB)
  lazy val scaleBackgroundImage: AffineTransform = AffineTransform.getScaleInstance(pixelWidth / environment.nCellsWide.toDouble, pixelHeight / environment.nCellsHigh.toDouble)


  lazy val timer = new Timer(frameDelay, Surface)
  lazy val modelStepByStep = new Visu.ModelIterator(model, Model.randomInit(model, environment, random))

  lazy val visuMainFrame =
    if !fullScreen
    then
      new MainFrame(gc):
        preferredSize = new Dimension(pixelWidth, pixelHeight)
        contents = Surface
        background = Color.black
    else
      new MainFrame(gc) with Undecorated:
        preferredSize = new Dimension(pixelWidth, pixelHeight)
        contents = Surface
        background = Color.black
        gd.setFullScreenWindow(this.peer)

  def shapeBird(x: Double,y: Double,heading: Heading): Shape = {
    new Line2D.Double(
      (x / model.worldWidth * pixelWidth + (birdLength / 2) * cos(heading.toDouble)),
      (y / model.worldHeight * pixelHeight + (birdLength / 2) * sin(heading.toDouble)),
      (x / model.worldWidth * pixelWidth - (birdLength / 2) * cos(heading.toDouble)),
      (y / model.worldHeight * pixelHeight - (birdLength / 2) * sin(heading.toDouble))
    )
  }

  def drawBirds(xyheading: Seq[(Double, Double, Heading)]) =
    xyheading.map(t => shapeBird(t._1, t._2, t._3))

  def birdsShapes(): Iterable[Shape] =
    drawBirds(modelStepByStep.currentState.birds.map(b => (b.position.x, b.position.y, b.heading)))

  def updateBackground() = {
    val envWidth: Double = environment.width
    val envHeight: Double = environment.height
    // var i: Int = 0
    // var j: Int = 0
    // i = 0
    // while (i < pixelWidth) {
    //   j = 0
    //   while (j < pixelHeight) {
    //     if (model.env.get((i + 0.5) * envWidth / pixelWidth, (j + 0.5) * envHeight / pixelHeight) != 0) backgroundImage.setRGB(i,j,obstacleColorRGB)
    //     else backgroundImage.setRGB(i,j,backgroundColorRGB)
    //     j += 1
    //   }
    //   i += 1
    // }
    backgroundImage.setRGB(0,0, environment.nCellsWide, environment.nCellsHigh, environment.pixels, 0, environment.nCellsWide)
  }

  object Surface extends Panel with ActionListener  {
    var lastFrameTime: Long = System.currentTimeMillis()
    var framesSinceLastTimeMeasure: Int = 0
    var deltaTime: Long = 0

    override def paintComponent(g: Graphics2D) = {
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
        RenderingHints.VALUE_ANTIALIAS_ON);
      g.setBackground(Color.black)
      g.clearRect(0,0,pixelWidth,pixelHeight)
      g.setColor(Visu.birdColor)
      //for {s <- obstaclesShapes} g.fill(s)
      updateBackground()
      g.drawImage(backgroundImage, scaleBackgroundImage, null)
      g.setStroke(new BasicStroke(birdWidth.toFloat))
      for {shape <- birdsShapes()} g.draw(shape)
      //for {af <- birdsTransform} g.drawImage(birdImage, af, null)
      // val af = new AffineTransform()
      // af.translate(-birdImageWidth / 2.0, -birdImageHeight / 2.0)
      // af.scale(0.5, 0.5)
      // af.translate(birdImageWidth * (1/0.5) / 2.0, birdImageHeight * (1/0.5) / 2.0)
      // af.rotate(Pi/4.0, birdImageWidth / 2.0, birdImageHeight / 2.0)
      // g.drawImage(birdImage, af, null)
      framesSinceLastTimeMeasure += 1
      deltaTime = System.currentTimeMillis() - lastFrameTime
      if (deltaTime >= 1000) {
        print((framesSinceLastTimeMeasure / deltaTime.toDouble) * 1000)
        println(" fps")
        framesSinceLastTimeMeasure = 0
        lastFrameTime = System.currentTimeMillis()
      }
    }

    def actionPerformed(e: ActionEvent) = {
      modelStepByStep.step
      repaint()
    }

    listenTo(keys)
    reactions += {
      case KeyPressed(_, Key.Q, _, _) => {
        Skeleton.quit()
      }
      case KeyPressed(_, Key.Escape, _, _) => {
        Skeleton.quit()
      }
    }
    focusable = true
    requestFocus()
  }


  object Skeleton extends SimpleSwingApplication {
    def top = {
      timer.start()
      visuMainFrame
    }

    override def shutdown() = {
      timer.stop
      super.shutdown()
    }
  }
}
