package com.audiofy.app.service

/**
 * 智能文本分片工具
 * 用于将长文本按句子边界切割成多个小片段，每片不超过maxBytes字节
 * 
 * ⚠️ 重要：Qwen3 TTS Flash限制是600字节（UTF-8），不是600字符！
 * - 英文: 1字符 = 1字节 → 最多600字符
 * - 中文: 1字符 = 3字节 → 最多200字符
 */
object TextChunker {
    /**
     * 智能切割文本
     *
     * @param text 待切割的文本
     * @param maxBytes 每片最大字节数(默认600，Qwen3 TTS Flash限制)
     * @return 文本片段列表
     *
     * 算法:
     * 1. 优先在句号、问号、感叹号、省略号、换行符等标点处切割
     * 2. 尽量保持句子完整性
     * 3. 单个句子超长时按固定字节长度硬切分
     * 4. ⚠️ 使用UTF-8字节数计算长度，而非字符数
     */
    fun smartChunk(text: String, maxBytes: Int = 600): List<String> {
        if (text.encodeToByteArray().size <= maxBytes) {
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

            // ⚠️ 关键修改：按UTF-8字节数计算，而非字符数
            val spaceBytes = if (currentChunk.isNotEmpty()) 1 else 0
            val currentBytes = currentChunk.toString().encodeToByteArray().size
            val sentenceBytes = trimmedSentence.encodeToByteArray().size
            val totalBytes = currentBytes + spaceBytes + sentenceBytes

            // 如果超过字节限制
            if (totalBytes > maxBytes) {
                // 保存当前chunk
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }

                // 处理超长句子(硬切分)
                if (sentenceBytes > maxBytes) {
                    chunks.addAll(hardChunk(trimmedSentence, maxBytes))
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
     * 硬切分(当单个句子超过maxBytes时)
     * 尝试在逗号、顿号等次要标点处切割
     * ⚠️ 按UTF-8字节数计算
     */
    private fun hardChunk(text: String, maxBytes: Int): List<String> {
        if (text.encodeToByteArray().size <= maxBytes) {
            return listOf(text)
        }

        // 尝试在次要标点处切割(逗号、分号、顿号)
        val subSentenceRegex = Regex("(?<=[，,;；、])")
        val subSentences = text.split(subSentenceRegex).filter { it.isNotBlank() }

        val chunks = mutableListOf<String>()
        val currentChunk = StringBuilder()

        for (subSentence in subSentences) {
            val currentBytes = currentChunk.toString().encodeToByteArray().size
            val subSentenceBytes = subSentence.encodeToByteArray().size
            val totalBytes = currentBytes + subSentenceBytes
            
            if (totalBytes > maxBytes) {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }

                // 还是太长，直接按字节切割
                if (subSentenceBytes > maxBytes) {
                    chunks.addAll(hardChunkByBytes(subSentence, maxBytes))
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
     * 按字节数硬切分文本
     * 确保每个片段的UTF-8字节数不超过maxBytes
     */
    private fun hardChunkByBytes(text: String, maxBytes: Int): List<String> {
        val chunks = mutableListOf<String>()
        var currentChunk = ""
        
        for (char in text) {
            val testChunk = currentChunk + char
            if (testChunk.encodeToByteArray().size > maxBytes) {
                // 当前chunk已满，保存并开始新chunk
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk)
                }
                currentChunk = char.toString()
            } else {
                currentChunk += char
            }
        }
        
        // 保存最后的chunk
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk)
        }
        
        return chunks
    }

    /**
     * 获取分片统计信息(用于日志和调试)
     * ⚠️ 显示字节数信息（UTF-8编码）
     */
    fun getChunkStats(text: String, maxBytes: Int = 600): String {
        val chunks = smartChunk(text, maxBytes)
        val totalChars = text.length
        val totalBytes = text.encodeToByteArray().size
        val chunkCount = chunks.size
        val avgChunkBytes = chunks.map { it.encodeToByteArray().size }.average().toInt()
        val maxChunkBytes = chunks.maxOfOrNull { it.encodeToByteArray().size } ?: 0
        val minChunkBytes = chunks.minOfOrNull { it.encodeToByteArray().size } ?: 0
        val maxChunkChars = chunks.maxOfOrNull { it.length } ?: 0
        val minChunkChars = chunks.minOfOrNull { it.length } ?: 0

        return """
            |文本分片统计:
            |  总字符数: $totalChars (${totalBytes}字节)
            |  分片数量: $chunkCount
            |  平均片长: $avgChunkBytes 字节
            |  最长片段: $maxChunkBytes 字节 ($maxChunkChars 字符)
            |  最短片段: $minChunkBytes 字节 ($minChunkChars 字符)
        """.trimMargin()
    }
}
