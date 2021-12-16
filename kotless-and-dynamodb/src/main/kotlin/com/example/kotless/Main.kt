package com.example.kotless

import com.example.kotless.TweetTable.getTweetListByMonthDay
import com.example.kotless.TweetTable.putTweetList
import io.kotless.dsl.lang.http.Get

// TODO 関数名の変更

// TODO Requestでmonthとdayを受け取れるようにする
@Get("/find")
fun find() = getTweetListByMonthDay(12, 13)

@Get("/tweet")
fun tweet() = putTweetList()