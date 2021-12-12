package com.example.kotless

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import io.kotless.dsl.lang.event.Scheduled
import twitter4j.Paging
import twitter4j.Query
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.time.LocalDateTime
import java.time.ZoneId

const val TIME_FORMAT = "\"%d-%02d-%02d_%02d:%02d:%02d_JST\""

fun putItem(): String {
    val id = (Math.random() * 1000).toInt().toString()
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    val items = mapOf(
        "id" to AttributeValue().withN(id),
        "name" to AttributeValue().withS("potato"),
        "price" to AttributeValue().withN("1000")
    )
    val request = PutItemRequest().withItem(items).withTableName("Commodity")
    val result = client.putItem(request)

    return id
}

fun findItem(): String {
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    val request = GetItemRequest()
        .withKey(mapOf("id" to AttributeValue().withN("101")))
        .withTableName("Commodity")
    val result = client.getItem(request).item

    return result["id"].toString()
}

//@Scheduled(Scheduled.everyMinute)
private fun putItemEveryMinute() {
    val id = (Math.random() * 1000).toInt().toString()
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    val items = mapOf(
        "id" to AttributeValue().withN(id),
        "name" to AttributeValue().withS("Tomato"),
        "price" to AttributeValue().withN("1000")
    )
    val request = PutItemRequest().withItem(items).withTableName("Commodity")

    client.putItem(request)
}

fun putTweetList(): List<Tweet> {
    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey("XXXXXXXXXXXXXXXXXXX")
        .setOAuthConsumerSecret("XXXXXXXXXXXXXXXXXXX")
        .setOAuthAccessToken("XXXXXXXXXXXXXXXXXXX")
        .setOAuthAccessTokenSecret("XXXXXXXXXXXXXXXXXXX")
    val tf = TwitterFactory(cb.build())

    val twitter = tf.instance
    val accountName = "n_takehata"
    val twitterUser = twitter.showUser(accountName)

    val since = TIME_FORMAT.format(2021, 12, 11, 0, 0, 0)
    val until = TIME_FORMAT.format(2021, 12, 11, 23, 59, 59)

    val query = Query("from:$accountName since:$since until:$until")
    val queryResults = twitter.search(query).tweets

    val list = queryResults.map {
        Tweet(it.id, LocalDateTime.ofInstant(it.createdAt.toInstant(), ZoneId.systemDefault()), it.text)
    }
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    list.forEach {
        val values = mapOf(
            "id" to AttributeValue().withN(it.id.toString()),
            "time" to AttributeValue().withS(it.time.toString()),
            "text" to AttributeValue().withS(it.text)
        )
        val request = PutItemRequest().withItem(values).withTableName("Tweet")
        val result = client.putItem(request)
        println("tweet: $it")
        println("result: $result")
    }

    return list
}

fun getTweetListByMonthDay(accountName: String, month: Int, day: Int): Map<Int, List<Tweet>> {
    val paging = Paging(1, 5)

    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey("XXXXXXXXXXXXXXXXXXX")
        .setOAuthConsumerSecret("XXXXXXXXXXXXXXXXXXX")
        .setOAuthAccessToken("XXXXXXXXXXXXXXXXXXX")
        .setOAuthAccessTokenSecret("XXXXXXXXXXXXXXXXXXX")
    val tf = TwitterFactory(cb.build())

    val twitter = tf.instance
    val twitterUser = twitter.showUser(accountName)

    val startYear = LocalDateTime.ofInstant(twitterUser.createdAt.toInstant(), ZoneId.systemDefault()).year
    val currentYear = LocalDateTime.now().year

    val tweetMap = mutableMapOf<Int, List<Tweet>>()
    for (year in startYear..currentYear) {
        val since = TIME_FORMAT.format(year, month, day, 0, 0, 0)
        val until = TIME_FORMAT.format(year, month, day, 23, 59, 59)

        val query = Query("from:$accountName since:$since until:$until")
        val queryResults = twitter.search(query).tweets

        tweetMap[year] = queryResults.map {
            Tweet(it.id, LocalDateTime.ofInstant(it.createdAt.toInstant(), ZoneId.systemDefault()), it.text)
        }
    }

    return tweetMap
}