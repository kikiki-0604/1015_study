package com.example.menumaker.controller

import com.example.menumaker.dto.CreateRecordResponse
import com.example.menumaker.dto.RecordRequest
import com.example.menumaker.dto.RecordResponse
import com.example.menumaker.service.RecordService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/records")
class RecordController(private val recordService: RecordService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: RecordRequest): CreateRecordResponse {
        return recordService.create(request)
    }

    @GetMapping
    fun list(): List<RecordResponse> {
        return recordService.list()
    }
}
