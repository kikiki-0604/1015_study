package com.example.menumaker.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class RecordRequest(
    @field:NotBlank(message = "dishName is required")
    val dishName: String,

    @field:NotBlank(message = "recipeUrl is required")
    @field:Pattern(regexp = "^https?://.+", message = "recipeUrl must be a valid URL")
    val recipeUrl: String,

    @field:NotBlank(message = "cookedOn is required")
    @field:Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "cookedOn must be YYYY-MM-DD")
    val cookedOn: String
)

data class RecordResponse(
    val id: Long,
    val dishName: String,
    val recipeUrl: String,
    val cookedOn: String
)

data class CreateRecordResponse(val id: Long)
