package com.example.kotless

import io.kotless.dsl.lang.http.Get

@Get("/put")
fun put() = putItem()

@Get("/find")
fun find() = getTweetListByMonthDay(12, 13)

@Get("/tweet")
fun tweet() = putTweetList()