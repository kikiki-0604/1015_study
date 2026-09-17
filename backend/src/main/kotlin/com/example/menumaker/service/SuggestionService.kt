package com.example.menumaker.service

import com.example.menumaker.client.BedrockClient
import com.example.menumaker.dto.AiCandidate
import com.example.menumaker.dto.Candidate
import com.example.menumaker.dto.SuggestionResponse
import com.example.menumaker.exception.AiResponseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class SuggestionService(
    private val bedrockClient: BedrockClient,
    private val objectMapper: ObjectMapper
) {

    fun suggest(ingredients: List<String>): SuggestionResponse {
        val prompt = buildPrompt(ingredients)
        val aiCandidates = tryFetchCandidates(prompt) ?: tryFetchCandidates(prompt)
            ?: throw AiResponseException("failed to parse AI response after retry")

        val candidates = aiCandidates.map { candidate ->
            Candidate(
                name = candidate.name,
                description = candidate.description,
                calories = candidate.calories,
                fat = candidate.fat,
                sugar = candidate.sugar,
                carbs = candidate.carbs,
                recipeUrl = buildSearchUrl(candidate.name)
            )
        }
        return SuggestionResponse(candidates)
    }

    private fun tryFetchCandidates(prompt: String): List<AiCandidate>? {
        return try {
            val requestBody = buildRequestBody(prompt)
            val rawResponse = bedrockClient.invoke(requestBody)
            val text = extractText(rawResponse)
            objectMapper.readValue<List<AiCandidate>>(text)
        } catch (e: Exception) {
            null
        }
    }

    private fun buildRequestBody(prompt: String): String {
        val body = mapOf(
            "anthropic_version" to "bedrock-2023-05-31",
            "max_tokens" to 1024,
            "messages" to listOf(
                mapOf("role" to "user", "content" to prompt)
            )
        )
        return objectMapper.writeValueAsString(body)
    }

    private fun extractText(rawResponse: String): String {
        val node = objectMapper.readTree(rawResponse)
        return node.at("/content/0/text").asText()
    }

    private fun buildPrompt(ingredients: List<String>): String {
        val joined = ingredients.joinToString(", ")
        return "食材: $joined。この食材で作れる料理を5つ提案してください。" +
            "出力は説明文を一切含めず、name・description・calories・fat・sugar・carbsのみを持つJSON配列のみにしてください。" +
            "calories(1人前のkcal)・fat(脂質g)・sugar(糖質g)・carbs(炭水化物g)はおおよその概算値を整数で入れてください。" +
            "例: [{\"name\":\"料理名\",\"description\":\"簡単な説明\",\"calories\":450,\"fat\":15,\"sugar\":40,\"carbs\":55}]"
    }

    private fun buildSearchUrl(dishName: String): String {
        val encoded = URLEncoder.encode(dishName, StandardCharsets.UTF_8)
        return "https://cookpad.com/search/$encoded"
    }
}
