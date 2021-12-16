package com.example.kotless

import com.example.kotless.TweetTable.getTweetListByMonthDay
import com.example.kotless.TweetTable.putTweetList
import io.kotless.dsl.lang.http.Get

@Get("/find")
fun find() = getTweetListByMonthDay(12, 13)

@Get("/tweet")
fun tweet() = putTweetList()