package com.miguelrodriguez19.mindmaster.models.structures

data class TranslationResponse(
    val translations: List<TranslatedText>
)

data class TranslatedText(
    val detected_source_language: String,
    val text: String
)
