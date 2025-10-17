package com.audiofy.app.service

/**
 * 智能文本分片工具
 * 用于将长文本按句子边界切割成多个小片段，每片不超过maxLength字符
 */
object TextChunker {
    /**
     * 智能切割文本
     *
     * @param text 待切割的文本
     * @param maxLength 每片最大长度(默认600，Qwen3 TTS Flash限制)
     * @return 文本片段列表
     *
     * 算法:
     * 1. 优先在句号、问号、感叹号、省略号、换行符等标点处切割
     * 2. 尽量保持句子完整性
     * 3. 单个句子超长时按固定长度硬切分
     */
    fun smartChunk(text: String, maxLength: Int = 600): List<String> {
        if (text.length <= maxLength) {
            return listOf(text)
        }

        // 使用正则分割，保留标点符号
        // 中文标点: 。？！……
        // 英文标点: . ? !
        // 换行符: \n
        val sentenceRegex = Regex("(?<=[。？！……?!\n])")
        val sentences = text.split(sentenceRegex).filter { it.isNotBlank() }

        val chunks = mutableListOf<String>()
        val currentChunk = StringBuilder()

        for (sentence in sentences) {
            val trimmedSentence = sentence.trim()

            // 计算添加这个句子后的总长度（包括可能的空格）
            val spaceLength = if (currentChunk.isNotEmpty()) 1 else 0
            val totalLength = currentChunk.length + spaceLength + trimmedSentence.length

            // 如果超过限制
            if (totalLength > maxLength) {
                // 保存当前chunk
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }

                // 处理超长句子(硬切分)
                if (trimmedSentence.length > maxLength) {
                    chunks.addAll(hardChunk(trimmedSentence, maxLength))
                } else {
                    currentChunk.append(trimmedSentence)
                }
            } else {
                // 句子不超长，直接追加
                if (currentChunk.isNotEmpty()) {
                    currentChunk.append(" ")
                }
                currentChunk.append(trimmedSentence)
            }
        }

        // 保存最后的chunk
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return chunks
    }

    /**
     * 硬切分(当单个句子超过maxLength时)
     * 尝试在逗号、顿号等次要标点处切割
     */
    private fun hardChunk(text: String, maxLength: Int): List<String> {
        if (text.length <= maxLength) {
            return listOf(text)
        }

        // 尝试在次要标点处切割(逗号、分号、顿号)
        val subSentenceRegex = Regex("(?<=[，,;；、])")
        val subSentences = text.split(subSentenceRegex).filter { it.isNotBlank() }

        val chunks = mutableListOf<String>()
        val currentChunk = StringBuilder()

        for (subSentence in subSentences) {
            val totalLength = currentChunk.length + subSentence.length
            
            if (totalLength > maxLength) {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }

                // 还是太长，直接按固定长度切割
                if (subSentence.length > maxLength) {
                    chunks.addAll(subSentence.chunked(maxLength))
                } else {
                    currentChunk.append(subSentence)
                }
            } else {
                currentChunk.append(subSentence)
            }
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return chunks
    }

    /**
     * 获取分片统计信息(用于日志和调试)
     */
    fun getChunkStats(text: String, maxLength: Int = 600): String {
        val chunks = smartChunk(text, maxLength)
        val totalChars = text.length
        val chunkCount = chunks.size
        val avgChunkSize = chunks.map { it.length }.average().toInt()
        val maxChunkSize = chunks.maxOfOrNull { it.length } ?: 0
        val minChunkSize = chunks.minOfOrNull { it.length } ?: 0

        return """
            |文本分片统计:
            |  总字符数: $totalChars
            |  分片数量: $chunkCount
            |  平均片长: $avgChunkSize 字符
            |  最长片段: $maxChunkSize 字符
            |  最短片段: $minChunkSize 字符
        """.trimMargin()
    }
}
