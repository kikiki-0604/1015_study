package com.example.menumaker.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class SuggestionRequest(
    @field:NotEmpty(message = "ingredients is required")
    @field:Size(max = 10, message = "ingredients must be 10 or fewer")
    val ingredients: List<@Size(min = 1, max = 50, message = "each ingredient must be 1-50 characters") String>
)

data class AiCandidate(
    val name: String,
    val description: String,
    val calories: Int,
    val fat: Int,
    val sugar: Int,
    val carbs: Int
)

data class Candidate(
    val name: String,
    val description: String,
    val calories: Int,
    val fat: Int,
    val sugar: Int,
    val carbs: Int,
    val recipeUrl: String
)

data class SuggestionResponse(
    val candidates: List<Candidate>
)
