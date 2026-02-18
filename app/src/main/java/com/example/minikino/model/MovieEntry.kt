package com.example.minikino.model

import kotlinx.serialization.Serializable

@Serializable
data class MovieEntry(
    val title: String,
    val genre: String,
    val rating: Float
): java.io.Serializable
