package com.example.menumaker.repository

import com.example.menumaker.entity.CookingRecord
import org.springframework.data.jpa.repository.JpaRepository

interface CookingRecordRepository : JpaRepository<CookingRecord, Long> {
    fun findAllByOrderByCookedOnDesc(): List<CookingRecord>
}
