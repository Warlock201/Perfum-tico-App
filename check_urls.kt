import java.net.HttpURLConnection
import java.net.URL

fun main() {
    val urls = listOf(
        "Maçã" to "https://images.unsplash.com/photo-1560806887-1e4cd0b6fac6?w=200&h=200&fit=crop",
        "Lavanda" to "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?w=200&h=200&fit=crop",
        "Melancia" to "https://images.unsplash.com/photo-1582281267840-8024921616c0?w=200&h=200&fit=crop",
        "Cedro" to "https://images.unsplash.com/photo-1448375240586-882707db888b?w=200&h=200&fit=crop",
        "Âmbar" to "https://images.unsplash.com/photo-1610260799636-6db27f804dc4?w=200&h=200&fit=crop",
        "Sândalo" to "https://images.unsplash.com/photo-1629858348981-d419a4e4d5fb?w=200&h=200&fit=crop"
    )
    for ((name, u) in urls) {
        try {
            val conn = URL(u).openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            println("$name: ${conn.responseCode}")
        } catch(e: Exception) {
            println("$name: Error ${e.message}")
        }
    }
}
