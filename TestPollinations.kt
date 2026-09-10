fun main() {
    val englishName = "lemon fruit"
    val prompt = "A single $englishName, centered, isolated on a solid dark gray background, macro photography, soft studio lighting, highly detailed"
    val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8").replace("+", "%20")
    val seed = kotlin.math.abs(englishName.hashCode())
    println("https://image.pollinations.ai/prompt/$encodedPrompt?width=200&height=200&nologo=true&seed=$seed")
}
