package com.example.menumaker.service

import com.example.menumaker.client.BedrockClient
import com.example.menumaker.exception.AiResponseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * SuggestionService のテスト。
 *
 * Bedrock(AI)への実通信はテストでは使わない。BedrockClient をモックにして、
 * 「AIがこう返してきたら、Serviceはこう動く」を確認する。
 *
 * 使うもの(spring-boot-starter-test に同梱済み):
 *   JUnit5 / Mockito / AssertJ(assertThat)
 */
class SuggestionServiceTest {

    private val bedrockClient: BedrockClient = Mockito.mock(BedrockClient::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val service = SuggestionService(bedrockClient, objectMapper)

    // ---- テスト用ヘルパー --------------------------------------------------

    /** Bedrockが返す形式(content[0].text にモデルの出力が入る)のJSONを作る */
    private fun bedrockResponse(text: String): String =
        objectMapper.writeValueAsString(
            mapOf("content" to listOf(mapOf("type" to "text", "text" to text)))
        )

    /** AIが返す「料理5件のJSON配列」のサンプル */
    private val fiveDishesJson = """
        [
          {"name":"オムライス","description":"卵とご飯","calories":450,"fat":15,"sugar":40,"carbs":55},
          {"name":"玉ねぎのスープ","description":"甘いスープ","calories":120,"fat":3,"sugar":15,"carbs":18},
          {"name":"卵焼き","description":"定番","calories":150,"fat":10,"sugar":2,"carbs":3},
          {"name":"チャーハン","description":"手早く","calories":500,"fat":14,"sugar":60,"carbs":70},
          {"name":"親子丼","description":"甘辛","calories":600,"fat":16,"sugar":75,"carbs":85}
        ]
    """.trimIndent()

    /** invoke が何を渡されても、指定した応答を返すようにする */
    private fun stubBedrock(vararg responses: String) {
        var stubbing = Mockito.`when`(bedrockClient.invoke(Mockito.anyString()))
        for (response in responses) {
            stubbing = stubbing.thenReturn(response)
        }
    }

    private fun encode(text: String): String = URLEncoder.encode(text, StandardCharsets.UTF_8)

    // ---- テスト本体 --------------------------------------------------------

    @Test
    fun `正常系_AIの応答から5件の候補が返る`() {
        stubBedrock(bedrockResponse(fiveDishesJson))

        val result = service.suggest(listOf("卵", "玉ねぎ"))

        assertThat(result.candidates).hasSize(5)
        assertThat(result.candidates[0].name).isEqualTo("オムライス")
        assertThat(result.candidates[0].calories).isEqualTo(450)
        assertThat(result.candidates.map { it.name })
            .containsExactly("オムライス", "玉ねぎのスープ", "卵焼き", "チャーハン", "親子丼")
    }

    @Test
    fun `レシピリンク_3サイト分が料理名をURLエンコードして作られる`() {
        stubBedrock(bedrockResponse(fiveDishesJson))

        val links = service.suggest(listOf("卵")).candidates[0].recipeLinks

        val encoded = encode("オムライス")
        assertThat(links.map { it.site }).containsExactly("cookpad", "kurashiru", "rakuten-recipe")
        assertThat(links.map { it.url }).containsExactly(
            "https://cookpad.com/jp/search/$encoded",
            "https://www.kurashiru.com/search?query=$encoded",
            "https://recipe.rakuten.co.jp/search/$encoded/"
        )
    }

    @Test
    fun `コードフェンス付きの応答でもパースできる`() {
        stubBedrock(bedrockResponse("```json\n$fiveDishesJson\n```"))

        val result = service.suggest(listOf("卵"))

        assertThat(result.candidates).hasSize(5)
    }

    @Test
    fun `前置きの文章付きの応答でもパースできる`() {
        stubBedrock(bedrockResponse("以下が候補です。\n$fiveDishesJson\n以上です。"))

        val result = service.suggest(listOf("卵"))

        assertThat(result.candidates).hasSize(5)
    }

    @Test
    fun `1回目が壊れたJSONでも2回目で成功すれば結果を返す`() {
        stubBedrock(
            bedrockResponse("これはJSONではありません"),
            bedrockResponse(fiveDishesJson)
        )

        val result = service.suggest(listOf("卵"))

        assertThat(result.candidates).hasSize(5)
        Mockito.verify(bedrockClient, Mockito.times(2)).invoke(Mockito.anyString())
    }

    @Test
    fun `2回とも壊れたJSONならAiResponseExceptionを投げる`() {
        stubBedrock(bedrockResponse("これはJSONではありません"))

        assertThrows<AiResponseException> {
            service.suggest(listOf("卵"))
        }
        Mockito.verify(bedrockClient, Mockito.times(2)).invoke(Mockito.anyString())
    }

    @Test
    fun `プロンプトに入力した食材が含まれる`() {
        // Kotlinでは ArgumentCaptor.capture() が null を返して非null引数に渡せないため、
        // スタブの Answer の中で、渡された引数を自分で保存する。
        var requestBody = ""
        Mockito.`when`(bedrockClient.invoke(Mockito.anyString())).thenAnswer { invocation ->
            requestBody = invocation.getArgument<String>(0)
            bedrockResponse(fiveDishesJson)
        }

        service.suggest(listOf("卵", "玉ねぎ"))

        val prompt = objectMapper.readTree(requestBody).at("/messages/0/content").asText()
        assertThat(prompt).contains("卵").contains("玉ねぎ")
    }

    // ---- 現在の挙動を固定するテスト(仕様として許容している内容) -------------------

    @Test
    fun `AIが5件未満を返してもそのまま返す`() {
        val threeDishes = """
            [
              {"name":"A","description":"a","calories":1,"fat":1,"sugar":1,"carbs":1},
              {"name":"B","description":"b","calories":2,"fat":2,"sugar":2,"carbs":2},
              {"name":"C","description":"c","calories":3,"fat":3,"sugar":3,"carbs":3}
            ]
        """.trimIndent()
        stubBedrock(bedrockResponse(threeDishes))

        val result = service.suggest(listOf("卵"))

        // 現状は件数を検証していない。5件固定にしたい場合は実装とこのテストを変える。
        assertThat(result.candidates).hasSize(3)
    }

    @Test
    fun `caloriesが数値でない応答はパース失敗としてリトライされ最終的に例外になる`() {
        val invalid = """[{"name":"A","description":"a","calories":"約450","fat":1,"sugar":1,"carbs":1}]"""
        stubBedrock(bedrockResponse(invalid))

        assertThrows<AiResponseException> {
            service.suggest(listOf("卵"))
        }
        Mockito.verify(bedrockClient, Mockito.times(2)).invoke(Mockito.anyString())
    }
}
