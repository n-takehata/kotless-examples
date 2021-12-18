package com.example.kotless

import com.example.kotless.TweetTable.getTweetListByMonthDay
import com.example.kotless.TweetTable.putTweetList
import io.kotless.dsl.lang.http.Get

// TODO 関数名の変更

@Get("/find")
fun find(month: Int, day: Int) = getTweetListByMonthDay(month, day)

@Get("/tweet")
fun tweet() = putTweetList()