package com.audiofy.app.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextChunkerTest {

    @Test
    fun testShortTextNoChunking() {
        val text = "这是一段短文本。"
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertEquals(1, chunks.size)
        assertEquals(text, chunks[0])
    }

    @Test
    fun testLongTextChunking() {
        // 构建一个1000字的文本
        val sentence = "这是一个测试句子。"
        val text = sentence.repeat(100) // 约1000字
        
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertTrue(chunks.size >= 2, "文本应该被分成至少2片")
        chunks.forEach { chunk ->
            assertTrue(chunk.length <= 600, "每片长度不应超过600字符")
        }
    }

    @Test
    fun testChunkingAtSentenceBoundary() {
        val text = "第一句话。第二句话。第三句话。第四句话。第五句话。"
        val chunks = TextChunker.smartChunk(text, maxLength = 20)
        
        assertTrue(chunks.size >= 2, "应该在句子边界处分片")
        chunks.forEach { chunk ->
            assertTrue(chunk.contains("。"), "每片应该至少包含一个完整句子")
        }
    }

    @Test
    fun testVeryLongSingleSentence() {
        // 构建一个超长的单个句子
        val text = "这" + "是".repeat(700) + "一个超长句子。"
        
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertTrue(chunks.size >= 2, "超长句子应该被硬切分")
        chunks.forEach { chunk ->
            assertTrue(chunk.length <= 600, "每片长度不应超过600字符")
        }
    }

    @Test
    fun testMixedPunctuation() {
        val text = "第一句话。第二句话？第三句话！第四句话……第五句话\n第六句话。"
        val chunks = TextChunker.smartChunk(text, maxLength = 30)
        
        assertTrue(chunks.size >= 2, "应该在多种标点处分片")
        
        // 验证分片保留了标点符号
        val joinedText = chunks.joinToString(" ")
        assertTrue(joinedText.contains("。"))
        assertTrue(joinedText.contains("？"))
        assertTrue(joinedText.contains("！"))
    }

    @Test
    fun testEmptyText() {
        val text = ""
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertTrue(chunks.isEmpty() || chunks.size == 1, "空文本应该返回空列表或单个空片段")
    }

    @Test
    fun testExactlyMaxLength() {
        val text = "这".repeat(600) // 恰好600字
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertEquals(1, chunks.size)
        assertEquals(600, chunks[0].length)
    }

    @Test
    fun testChunkStats() {
        val text = "这是测试文本。".repeat(100)
        val stats = TextChunker.getChunkStats(text, maxLength = 600)
        
        assertTrue(stats.contains("总字符数"))
        assertTrue(stats.contains("分片数量"))
        assertTrue(stats.contains("平均片长"))
    }

    @Test
    fun testChinesePunctuation() {
        val text = "第一句，包含逗号。第二句，也包含逗号；还有分号。第三句？问号结尾。"
        val chunks = TextChunker.smartChunk(text, maxLength = 30)
        
        assertTrue(chunks.size >= 1, "应该能正确处理中文标点")
        chunks.forEach { chunk ->
            assertTrue(chunk.length <= 30, "每片长度不应超过限制")
        }
    }

    @Test
    fun testEnglishText() {
        val text = "This is the first sentence. This is the second sentence. This is the third sentence."
        val chunks = TextChunker.smartChunk(text, maxLength = 40)
        
        assertTrue(chunks.size >= 2, "英文文本也应该被正确分片")
        chunks.forEach { chunk ->
            assertTrue(chunk.length <= 40, "每片长度不应超过限制")
        }
    }

    @Test
    fun testRealWorldExample1000Chars() {
        // 模拟1000字的真实场景
        val text = """
            人工智能（Artificial Intelligence，AI）是计算机科学的一个分支，它企图了解智能的实质，
            并生产出一种新的能以人类智能相似的方式做出反应的智能机器。该领域的研究包括机器人、
            语言识别、图像识别、自然语言处理和专家系统等。人工智能从诞生以来，理论和技术日益成熟，
            应用领域也不断扩大。可以设想，未来人工智能带来的科技产品，将会是人类智慧的"容器"。
            人工智能可以对人的意识、思维的信息过程进行模拟。人工智能不是人的智能，但能像人那样思考、
            也可能超过人的智能。
        """.trimIndent().repeat(3) // 约900字
        
        val chunks = TextChunker.smartChunk(text, maxLength = 600)
        
        assertTrue(chunks.size >= 2, "1000字文本应该被分成至少2片")
        assertTrue(chunks.size <= 3, "1000字文本应该被分成不超过3片")
        
        // 验证合并后的文本长度与原文本相近（允许因空格产生的小差异）
        val totalLength = chunks.sumOf { it.length }
        assertTrue(totalLength >= text.length * 0.95, "分片后总长度不应显著减少")
    }
}

