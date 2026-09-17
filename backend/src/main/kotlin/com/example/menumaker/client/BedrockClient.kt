package com.example.menumaker.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest

/**
 * AWS Bedrock (Claude) の呼び出しをラップするクライアント。
 * 他のクラスからはAWS SDKへの依存を隠す。
 */
@Component
class BedrockClient(
    @Value("\${aws.region}") private val region: String,
    @Value("\${aws.bedrock.model-id}") private val modelId: String
) {
    private val client: BedrockRuntimeClient by lazy {
        BedrockRuntimeClient.builder()
            .region(Region.of(region))
            .build()
    }

    /**
     * プロンプトを渡してBedrockを呼び出し、レスポンスのJSON文字列全体を返す。
     */
    fun invoke(requestBodyJson: String): String {
        val request = InvokeModelRequest.builder()
            .modelId(modelId)
            .contentType("application/json")
            .accept("application/json")
            .body(SdkBytes.fromUtf8String(requestBodyJson))
            .build()

        val response = client.invokeModel(request)
        return response.body().asUtf8String()
    }
}
