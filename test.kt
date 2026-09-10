import org.json.JSONArray
import org.json.JSONObject

fun main() {
    val arr = JSONArray("[{\"size\":\"100ml\",\"level\":75}]")
    val obj = arr.getJSONObject(0)
    println(obj.getString("size"))
    println(obj.getInt("level"))
}
