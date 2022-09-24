package paint

import CentralControlComputer
import Clock
import infrastructure.Direction
import infrastructure.container.Transit
import infrastructure.World
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt



object MapPainter {

    private class WorldPainter(world: World) {

        private enum class Paint(val color: Color) {
            Station(Color.CYAN), Railway(Color.LIGHT_GRAY), Train(Color.RED)
        }

        val width = 2000
        val height = 2000
        val buffer = height / 10
        val stations = world.stations
        val railways = world.railways
        val originY = stations.minBy { it.coord.first }.coord.first
        val originX = stations.minBy { it.coord.second }.coord.second
        val rangeY = stations.maxBy { it.coord.first }.coord.first - originY
        val rangeX = stations.maxBy { it.coord.second }.coord.second - originX
        val unitY = (height - buffer * 2.0) / rangeY.toDouble()
        val unitX = (width - buffer * 2.0) / rangeX.toDouble()

        val fontSizeStd = 30f
        val strokeStd = 10f
        val stdSize = 40

        private fun getY(y: Int) = ((y - originY) * unitY).roundToInt() + buffer
        private fun getX(x: Int) = ((x - originX) * unitX).roundToInt() + buffer

        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics: Graphics2D = img.createGraphics()

        fun draw(): BufferedImage {
            drawDefaultComponents()
            drawRailways()
            drawStations()
            graphics.dispose()
            return img
        }

        private fun drawDefaultComponents() {
            graphics.apply {
                this.font = this.font.deriveFont(Font.TRUETYPE_FONT, fontSizeStd*3)
                color = Color.BLACK
                drawString("Train Simulator Map", width / 2 - buffer*2, buffer / 2)
                this.font = this.font.deriveFont(Font.TRUETYPE_FONT, fontSizeStd*1.5f)
                drawString(CentralControlComputer.money.toString() + "$", buffer,buffer/2)
                val nextTask = CentralControlComputer.nextTask ?: return
                val task = "${nextTask.first}$ from ${nextTask.second.first.name} to ${nextTask.second.second.name}"
                drawString(task,buffer,buffer*3/4)
            }
        }

        private fun drawRailways(){

            for (railway in railways) {
                val yStart = getY(railway.start.coord.first) + stdSize
                val xStart = getX(railway.start.coord.second) + stdSize
                val yEnd = getY(railway.end.coord.first) + stdSize
                val xEnd = getX(railway.end.coord.second) + stdSize
                graphics.apply {
                    color = Paint.Railway.color
                    stroke = BasicStroke(strokeStd)
                    drawLine(xStart,yStart,xEnd,yEnd)
                }
                val y = yStart-yEnd
                val x = xStart-xEnd
                drawTrains(y,x,railway.waiting,yStart,xStart)
            }
        }

        private fun drawTrains(y : Int, x : Int, trains : List<Transit>, yStart : Int, xStart : Int){
            for (train in trains) {
                if(train.first.first > Clock.time)continue
                var progress = Clock.time / (train.first.first + train.first.second).toFloat()
                progress = if(train.direction == Direction.Up) progress else abs(1-progress)
                val xPos = (x * progress).roundToInt()
                val yPos = (y * progress).roundToInt()
                graphics.apply {
                    color = Paint.Train.color
                    drawOval(xStart - xPos - stdSize,yStart - yPos - stdSize, stdSize*2,  stdSize*2)
                }
            }
        }

        private fun drawStations() {

                for (station in stations) {
                    val y = getY(station.coord.first)
                    val x = getX(station.coord.second)
                    graphics.apply {
                        color = Paint.Station.color
                        fillOval(x, y, stdSize * 2, stdSize * 2)
                        color = Color.BLACK
                        this.font = this.font.deriveFont(Font.BOLD, fontSizeStd * 1.5f)
                        drawString(station.name, x - stdSize, y + stdSize * 3)
                    }
                    drawDepartureChart(station.waiting, x, y)
            }

        }

        private fun drawDepartureChart(waiting: List<Transit>, x: Int, y: Int) {
            val departures = waiting.sortedBy { it.first.second }
                .map { "${Clock.time - it.first.second} |RE " + it.second.target }

            graphics.apply {
                this.font = this.font.deriveFont(Font.ITALIC, fontSizeStd * 1.2f)
                if (departures.size <= 5) {
                    for ((idx, train) in departures.withIndex()) {
                        drawString(train, x - stdSize, y + stdSize * 4 + idx * (fontSizeStd * 1.4f).toInt())
                    }
                } else {
                    for ((idx, train) in (departures.take(5) + "...").withIndex()) {
                        drawString(train, x - stdSize, y + stdSize * 4 + idx * (fontSizeStd * 1.4f).toInt())
                    }
                }
            }
        }

    }

    fun draw(world: World, id : Int) {
        val img = WorldPainter(world).draw()
        val file = File("tests/$id.png")
        file.createNewFile()
        ImageIO.write(img, "png", file)
        //Desktop.getDesktop().open(file)
    }


}