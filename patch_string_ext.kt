import android.content.Context

fun Context.getNoteDrawableResId(noteName: String): Int {
    val norm = noteName.normalizeNoteName().replace("-", "_").replace("(", "").replace(")", "").replace(" ", "")
    return this.resources.getIdentifier("note_$norm", "drawable", this.packageName)
}
