fun main() {
    val prompt = "apple fruit ingredient macro photography dark background"
    val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
    val seed = 123
    println("https://image.pollinations.ai/prompt/$encodedPrompt?width=200&height=200&nologo=true&seed=$seed")
}
