package com.example.menumaker.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "cooking_records")
class CookingRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "dish_name", nullable = false)
    val dishName: String,

    @Column(name = "recipe_url", nullable = false, length = 1024)
    val recipeUrl: String,

    @Column(name = "cooked_on", nullable = false)
    val cookedOn: LocalDate,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
