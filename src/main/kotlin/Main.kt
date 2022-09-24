import java.io.File


fun main(){

    val file = File("Main.txt")
    CentralControlComputer.parse(file)
    CentralControlComputer.run()

}
