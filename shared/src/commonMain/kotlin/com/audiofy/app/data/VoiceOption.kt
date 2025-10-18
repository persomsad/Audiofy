package com.audiofy.app.data

/**
 * 音色选项
 * 参考 Qwen3 TTS API 文档
 */
data class VoiceOption(
    val id: String,           // Cherry, Ethan, Nofish, etc.
    val displayName: String,  // 中文名称
    val description: String,  // 音色描述
    val gender: String,       // 性别：男/女
    val style: String,        // 风格：温柔/活力/专业等
    val previewUrl: String? = null  // 预览音频URL（可选）
)

/**
 * 预定义的音色选项
 * 来源：Qwen3 TTS 官方文档
 */
object VoiceOptions {
    val ALL = listOf(
        VoiceOption(
            id = "Cherry",
            displayName = "樱桃",
            description = "温柔女声，适合情感类内容",
            gender = "女",
            style = "温柔"
        ),
        VoiceOption(
            id = "Ethan",
            displayName = "伊森",
            description = "沉稳男声，适合新闻和专业内容",
            gender = "男",
            style = "专业"
        ),
        VoiceOption(
            id = "Nofish",
            displayName = "诺菲",
            description = "清新女声，适合日常阅读",
            gender = "女",
            style = "清新"
        ),
        VoiceOption(
            id = "Sunny",
            displayName = "阳光",
            description = "活力男声，适合激励类内容",
            gender = "男",
            style = "活力"
        ),
        VoiceOption(
            id = "Serena",
            displayName = "塞琳娜",
            description = "优雅女声，适合文学作品",
            gender = "女",
            style = "优雅"
        )
    )
    
    /**
     * 根据ID获取音色选项
     */
    fun findById(id: String): VoiceOption? {
        return ALL.find { it.id == id }
    }
    
    /**
     * 获取默认音色
     */
    val DEFAULT = ALL.first() // Cherry
}
