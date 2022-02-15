package com.example.kotless

import java.time.LocalDateTime

data class Tweet(val id: Long, val time: LocalDateTime, val text: String)