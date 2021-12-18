package com.example.kotless

import com.example.kotless.TweetTable.getTweetListByMonthDay
import com.example.kotless.TweetTable.putTweetList
import io.kotless.dsl.lang.http.Get
import io.kotless.dsl.lang.http.Post

// TODO 関数名の変更

@Get("/find")
fun find(month: Int, day: Int) = getTweetListByMonthDay(month, day)

@Post("/tweet")
fun tweet(): List<Tweet> {
    val accountName = twitterConfig.getString("account_name")
    val since = TIME_FORMAT.format(2021, 12, 10, 0, 0, 0)
    val until = TIME_FORMAT.format(2021, 12, 17, 23, 59, 59 )
    return putTweetList(accountName, since, until)
}