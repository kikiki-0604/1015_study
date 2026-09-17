package com.example.menumaker.service

import com.example.menumaker.client.BedrockClient
import com.example.menumaker.dto.AiCandidate
import com.example.menumaker.dto.Candidate
import com.example.menumaker.dto.RecipeLink
import com.example.menumaker.dto.SuggestionResponse
import com.example.menumaker.exception.AiResponseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class SuggestionService(
    private val bedrockClient: BedrockClient,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(SuggestionService::class.java)

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
                recipeLinks = buildRecipeLinks(candidate.name)
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
            log.warn("failed to fetch/parse AI candidates", e)
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
        val text = node.at("/content/0/text").asText()
        return stripCodeFence(text)
    }

    /**
     * Claudeはプロンプトで禁止していても```json ... ```のようにコードフェンスで
     * 応答を囲むことがあるため、JSON配列の実体部分だけを取り出す。
     */
    private fun stripCodeFence(text: String): String {
        val trimmed = text.trim()
        val start = trimmed.indexOf('[')
        val end = trimmed.lastIndexOf(']')
        if (start == -1 || end == -1 || end < start) {
            return trimmed
        }
        return trimmed.substring(start, end + 1)
    }

    private fun buildPrompt(ingredients: List<String>): String {
        val joined = ingredients.joinToString(", ")
        return "食材: $joined。この食材で作れる料理を5つ提案してください。" +
            "出力は説明文を一切含めず、name・description・calories・fat・sugar・carbsのみを持つJSON配列のみにしてください。" +
            "calories(1人前のkcal)・fat(脂質g)・sugar(糖質g)・carbs(炭水化物g)はおおよその概算値を整数で入れてください。" +
            "例: [{\"name\":\"料理名\",\"description\":\"簡単な説明\",\"calories\":450,\"fat\":15,\"sugar\":40,\"carbs\":55}]"
    }

    /**
     * 特定レシピへの直接リンクは公式APIがないため、料理名で複数の
     * 大手レシピサイトのサイト内検索に飛ばす。1サイトの検索精度に
     * 依存しないよう候補を複数用意する。
     */
    private fun buildRecipeLinks(dishName: String): List<RecipeLink> {
        val encoded = URLEncoder.encode(dishName, StandardCharsets.UTF_8)
        return listOf(
            RecipeLink(
                site = "cookpad",
                label = "クックパッドで探す",
                url = "https://cookpad.com/jp/search/$encoded"
            ),
            RecipeLink(
                site = "kurashiru",
                label = "クラシルで探す",
                url = "https://www.kurashiru.com/search?query=$encoded"
            ),
            RecipeLink(
                site = "rakuten-recipe",
                label = "楽天レシピで探す",
                url = "https://recipe.rakuten.co.jp/search/$encoded/"
            )
        )
    }
}
