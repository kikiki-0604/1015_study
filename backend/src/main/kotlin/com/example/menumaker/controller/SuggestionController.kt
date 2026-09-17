package com.example.menumaker.controller

import com.example.menumaker.dto.SuggestionRequest
import com.example.menumaker.dto.SuggestionResponse
import com.example.menumaker.service.SuggestionService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/suggestions")
class SuggestionController(private val suggestionService: SuggestionService) {

    @PostMapping
    fun suggest(@Valid @RequestBody request: SuggestionRequest): SuggestionResponse {
        return suggestionService.suggest(request.ingredients)
    }
}
