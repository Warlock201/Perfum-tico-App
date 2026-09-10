fun main() {
    val notes = "Maçã, Lavanda | Melancia, Cedro | Âmbar, Sândalo"
    val parts = notes.split(",", "|").map { it.trim() }.filter { it.isNotBlank() }
    println(parts)
}
