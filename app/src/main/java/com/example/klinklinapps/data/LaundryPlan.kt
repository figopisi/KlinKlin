package com.example.klinklinapps.data

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class LaundryPlan(
    val id: String = "",
    val userId: String = "",
    val eventName: String = "",
    val eventDate: Timestamp? = null,
    val serviceType: String = "",
    val computedDeadline: Timestamp? = null
) {
    fun getEventLocalDate(): LocalDate {
        return eventDate?.let {
            Instant.ofEpochSecond(it.seconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } ?: LocalDate.now()
    }

    fun getDeadlineLocalDate(): LocalDate {
        return computedDeadline?.let {
            Instant.ofEpochSecond(it.seconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        } ?: LocalDate.now()
    }
}
