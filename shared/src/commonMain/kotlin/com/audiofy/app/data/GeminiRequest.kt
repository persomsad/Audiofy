package com.audiofy.app.data

import kotlinx.serialization.Serializable

/**
 * Gemini API Request/Response Models
 * Based on Gemini REST API specification
 * https://ai.google.dev/api/rest/v1beta/models/generateContent
 */

/**
 * Request body for Gemini API
 */
@Serializable
data class GeminiRequest(
    val contents: List<Content>,
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String = "user",
)

@Serializable
data class Part(
    val text: String,
)

/**
 * Response from Gemini API
 */
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
)

/**
 * Translation stages
 */
enum class TranslationStage {
    STAGE_1_TRANSLATE,  // Accurate translation
    STAGE_2_POLISH,     // Polish to natural Chinese
}

/**
 * Translation progress state
 */
data class TranslationProgress(
    val stage: TranslationStage,
    val message: String,
    val intermediateResult: String? = null,
)
