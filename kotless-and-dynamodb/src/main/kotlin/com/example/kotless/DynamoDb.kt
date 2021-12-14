package com.example.kotless

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import dev.akkinoc.util.YamlResourceBundle
import twitter4j.Query
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.ResourceBundle

const val TIME_FORMAT = "\"%d-%02d-%02d_%02d:%02d:%02d_JST\""
const val TABLE_DATE_FORMAT = "%d-%02d-%02d"
const val TABLE_TIME_FORMAT = "%02d:%02d:%02d"

val twitterConfig = ResourceBundle.getBundle("twitter", YamlResourceBundle.Control)

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
        .setOAuthConsumerKey(twitterConfig.getString("consume_key"))
        .setOAuthConsumerSecret(twitterConfig.getString("consume_secret"))
        .setOAuthAccessToken(twitterConfig.getString("access_token"))
        .setOAuthAccessTokenSecret(twitterConfig.getString("access_token_secret"))
    val tf = TwitterFactory(cb.build())

    val twitter = tf.instance
    val accountName = twitterConfig.getString("account_name")

    val lastDate = LocalDateTime.now(ZoneId.of("Asia/Tokyo")).minusDays(1)
    val year = lastDate.year
    val month = lastDate.month.value
    val day = lastDate.dayOfMonth

    val since = TIME_FORMAT.format(year, month, day, 0, 0, 0)
    val until = TIME_FORMAT.format(year, month, day, 23, 59, 59)

    val query = Query("from:$accountName since:$since until:$until")
    val queryResults = twitter.search(query).tweets

    val list = queryResults.map {
        Tweet(it.id, LocalDateTime.ofInstant(it.createdAt.toInstant(), ZoneId.systemDefault()), it.text)
    }
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    list.forEach {
        val time = it.time
        val values = mapOf(
            "id" to AttributeValue().withN(it.id.toString()),
            "tweet_date" to AttributeValue().withS(TABLE_DATE_FORMAT.format(time.year, time.month.value, time.dayOfMonth)),
            "tweet_time" to AttributeValue().withS(TABLE_TIME_FORMAT.format(time.hour, time.minute, time.second)),
            "tweet_text" to AttributeValue().withS(it.text)
        )
        val request = PutItemRequest().withItem(values).withTableName("Tweet")
        client.putItem(request)
    }

    return list
}

fun getTweetListByMonthDay(month: Int, day: Int): Map<Int, List<Tweet>> {
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    val dynamoDb = DynamoDB(client)
    val table = dynamoDb.getTable("Tweet")
    val index = table.getIndex("datetime-index")

    val cb = ConfigurationBuilder()
    cb.setDebugEnabled(true)
        .setOAuthConsumerKey(twitterConfig.getString("consume_key"))
        .setOAuthConsumerSecret(twitterConfig.getString("consume_secret"))
        .setOAuthAccessToken(twitterConfig.getString("access_token"))
        .setOAuthAccessTokenSecret(twitterConfig.getString("access_token_secret"))
    val tf = TwitterFactory(cb.build())

    val twitter = tf.instance
    val twitterUser = twitter.showUser(twitterConfig.getString("account_name"))

    val startYear = LocalDateTime.ofInstant(twitterUser.createdAt.toInstant(), ZoneId.systemDefault()).year
    val currentYear = LocalDateTime.now().year

    val tweetMap = mutableMapOf<Int, List<Tweet>>()
    for (year in startYear..currentYear) {
        val date = "$year-$month-$day"
        val since = "00:00:00"
        val until = "23:59:59"

        val query = QuerySpec()
            .withProjectionExpression("id, tweet_date, tweet_time, tweet_text")
            .withKeyConditionExpression("tweet_date = :v_date and tweet_time between :v_since and :v_until")
            .withValueMap(ValueMap().withString(":v_date", date).withString(":v_since", since).withString(":v_until", until))

        val queryResults = index.query(query)

        tweetMap[year] = queryResults.map {
            Tweet(it.getLong("id"), LocalDateTime.now(), it.getString("tweet_text"))
        }
    }

    return tweetMap
}