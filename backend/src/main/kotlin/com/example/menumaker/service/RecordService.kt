package com.example.menumaker.service

import com.example.menumaker.dto.CreateRecordResponse
import com.example.menumaker.dto.RecordRequest
import com.example.menumaker.dto.RecordResponse
import com.example.menumaker.entity.CookingRecord
import com.example.menumaker.repository.CookingRecordRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RecordService(private val repository: CookingRecordRepository) {

    fun create(request: RecordRequest): CreateRecordResponse {
        val record = CookingRecord(
            dishName = request.dishName,
            recipeUrl = request.recipeUrl,
            cookedOn = LocalDate.parse(request.cookedOn)
        )
        val saved = repository.save(record)
        return CreateRecordResponse(saved.id!!)
    }

    fun list(): List<RecordResponse> {
        return repository.findAllByOrderByCookedOnDesc().map { record ->
            RecordResponse(
                id = record.id!!,
                dishName = record.dishName,
                recipeUrl = record.recipeUrl,
                cookedOn = record.cookedOn.toString()
            )
        }
    }
}
