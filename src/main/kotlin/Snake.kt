import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class App : Application() {

    companion object {
        const val WIDTH = 500.0
        const val HEIGHT = 500.0
    }

    override fun start(primaryStage: Stage) {
        primaryStage.width = WIDTH
        primaryStage.height = HEIGHT

        val (canvas, scene, graphics) = initGame(primaryStage)

        val random = Random()
        val snake = initSnake()
        val apples = initApples(random)
        var score = 0


        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                graphics.clearRect(0.0, 0.0, canvas.width, canvas.height)
                snake.move()
                val eat = snake.eat(apples)
                if (eat) {
                    apples.grow()
                    score++
                }

                snake.head.let { graphics.fillText("Q", it.x.toDouble(), it.y.toDouble()) }
                snake.body.forEach { graphics.fillText("o", it.x.toDouble(), it.y.toDouble()) }
                apples.points.forEach {
                    graphics.fillText("*", it.x.toDouble(), it.y.toDouble())
                    graphics.fillText("*", it.x.toDouble() - 2, it.y.toDouble())
                    graphics.fillText("*", it.x.toDouble(), it.y.toDouble() - 2)
                    graphics.fillText("*", it.x.toDouble() - 2, it.y.toDouble() - 2)
                }
                if (snake.isDied()) {
                    timer.cancel()
                    graphics.fillText("Your score is $score !!", 200.0, 200.0)
                }
            }
        }, 0, 100)

        primaryStage.setOnCloseRequest {
            timer.cancel()
        }

        scene.setOnKeyPressed {
            val direction = Direction.getDirection(it.code)
            snake.turn(direction)
        }
    }

    private fun initApples(random: Random): Apples {
        val points = HashSet<Point>()
        points.add(Point(random.nextInt(WIDTH.toInt()), random.nextInt(HEIGHT.toInt())))
        return Apples(points)
    }

    private fun initSnake(): Snake {
        val points = ArrayList<Point>()
        points.add(Point(130, 100))
        points.add(Point(120, 100))
        points.add(Point(110, 100))
        points.add(Point(100, 100))
        return Snake(points)
    }

    private fun initGame(primaryStage: Stage): Triple<Canvas, Scene, GraphicsContext> {
        val canvas = Canvas()
        canvas.height = primaryStage.height
        canvas.width = primaryStage.width
        val group = Group()
        group.children.add(canvas)
        val scene = Scene(group, primaryStage.width, primaryStage.height)
        primaryStage.scene = scene
        primaryStage.show()

        val graphics = canvas.graphicsContext2D
        return Triple(canvas, scene, graphics)
    }
}

class Snake(points: MutableList<Point>) {
    var head = points[0]
    var body = points.subList(1, points.size)

    private var direction = Direction.RIGHT

    fun move() {
        val old = head
        val x = if (old.x > App.WIDTH) 0 else old.x + direction.x
        val y = if (old.y > App.HEIGHT) 0 else old.y + direction.y
        head = Point(x, y)
        body.add(0, old)
        body.removeAt(body.size - 1)
    }

    fun turn(direction: Direction) {
        if (this.direction.x + direction.x != 0
                || this.direction.y + direction.y != 0) {
            this.direction = direction
        }
    }

    fun eat(apples: Apples): Boolean {
        val tail = body[body.size - 1]
        val eat = apples.points.removeIf {
            Math.abs(head.x - it.x) < 10 && Math.abs(head.y - it.y) < 10
        }
        if (eat) {
            body.add(Point(tail.x - direction.x, tail.y - direction.y))
        }
        return eat
    }

    fun isDied(): Boolean {
        return body.any { it.x == head.x && it.y == head.y }
    }
}

class Apples(val points: MutableSet<Point>) {
    private val random = Random()

    fun grow() {
        if (points.size > 10) return
        var i = random.nextInt(3)
        while (i-- >= 0) {
            points.add(Point(random.nextInt(App.WIDTH.toInt()), random.nextInt(App.HEIGHT.toInt())))
        }
    }

}

data class Point(val x: Int, val y: Int)

enum class Direction(val x: Int, val y: Int, private val keys: List<KeyCode>) {
    LEFT(-10, 0, listOf(KeyCode.LEFT)),
    RIGHT(10, 0, listOf(KeyCode.RIGHT)),
    UP(0, -10, listOf(KeyCode.UP)),
    DOWN(0, 10, listOf(KeyCode.DOWN));

    companion object {
        fun getDirection(keyCode: KeyCode): Direction {
            return values().first { it.keys.contains(keyCode) }
        }
    }
}

fun main(args: Array<String>) {
    Application.launch(App::class.java)
}