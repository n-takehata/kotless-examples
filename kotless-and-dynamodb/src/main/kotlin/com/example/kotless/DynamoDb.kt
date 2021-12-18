package com.example.kotless

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import dev.akkinoc.util.YamlResourceBundle
import io.kotless.PermissionLevel
import io.kotless.dsl.lang.DynamoDBTable
import io.kotless.dsl.lang.event.Scheduled
import twitter4j.Query
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.ResourceBundle

const val TIME_FORMAT = "\"%d-%02d-%02d_%02d:%02d:%02d_JST\""
const val TABLE_DATE_FORMAT = "%d-%02d-%02d"
const val TABLE_TIME_FORMAT = "%02d:%02d:%02d"

val twitterConfig = ResourceBundle.getBundle("twitter", YamlResourceBundle.Control)

@DynamoDBTable("Tweet", PermissionLevel.ReadWrite)
object TweetTable {
    private val twitterClient = TwitterFactory(
        ConfigurationBuilder().setDebugEnabled(true)
            .setOAuthConsumerKey(twitterConfig.getString("consume_key"))
            .setOAuthConsumerSecret(twitterConfig.getString("consume_secret"))
            .setOAuthAccessToken(twitterConfig.getString("access_token"))
            .setOAuthAccessTokenSecret(twitterConfig.getString("access_token_secret"))
            .build()
    ).instance

    @Scheduled("0 0 1/1 * ? *")
    private fun putTweetList() {
        val accountName = twitterConfig.getString("account_name")

        val lastDate = LocalDateTime.now(ZoneId.of("Asia/Tokyo")).minusDays(1)
        val year = lastDate.year
        val month = lastDate.month.value
        val day = lastDate.dayOfMonth

        val since = TIME_FORMAT.format(year, month, day, 0, 0, 0)
        val until = TIME_FORMAT.format(year, month, day, 23, 59, 59)

        putTweetList(accountName, since, until)
    }

    fun putTweetList(accountName: String, since: String, until: String): List<Tweet> {
        val query = Query("from:$accountName since:$since until:$until")
        val queryResults = twitterClient.search(query).tweets

        val list = queryResults.map {
            Tweet(it.id, LocalDateTime.ofInstant(it.createdAt.toInstant(), ZoneId.systemDefault()), it.text)
        }
        val client = AmazonDynamoDBClientBuilder.defaultClient()
        list.forEach {
            val time = it.time
            val values = mapOf(
                "id" to AttributeValue().withN(it.id.toString()),
                "tweet_date" to AttributeValue().withS(
                    TABLE_DATE_FORMAT.format(
                        time.year,
                        time.month.value,
                        time.dayOfMonth
                    )
                ),
                "tweet_time" to AttributeValue().withS(TABLE_TIME_FORMAT.format(time.hour, time.minute, time.second)),
                "tweet_text" to AttributeValue().withS(it.text)
            )
            val request = PutItemRequest().withItem(values).withTableName("Tweet")
            client.putItem(request)
        }

        return list
    }

    fun getTweetListByMonthDay(month: Int, day: Int): Map<Int, List<GetTweetListResponse>> {
        val client = AmazonDynamoDBClientBuilder.defaultClient()
        val dynamoDb = DynamoDB(client)
        val table = dynamoDb.getTable("Tweet")
        val index = table.getIndex("datetime-index")

        val twitterUser = twitterClient.showUser(twitterConfig.getString("account_name"))

        val startYear = LocalDateTime.ofInstant(twitterUser.createdAt.toInstant(), ZoneId.systemDefault()).year
        val currentYear = LocalDateTime.now().year

        val tweetMap = mutableMapOf<Int, List<GetTweetListResponse>>()
        for (year in startYear..currentYear) {
            val date = "$year-$month-$day"
            val since = "00:00:00"
            val until = "23:59:59"

            val query = QuerySpec()
                .withProjectionExpression("id, tweet_date, tweet_time, tweet_text")
                .withKeyConditionExpression("tweet_date = :v_date and tweet_time between :v_since and :v_until")
                .withValueMap(
                    ValueMap().withString(":v_date", date).withString(":v_since", since).withString(":v_until", until)
                )

            val queryResults = index.query(query)

            tweetMap[year] = queryResults.map {
                GetTweetListResponse(
                    it.getLong("id"),
                    "${it.getString("tweet_date")} ${it.getString("tweet_time")}",
                    it.getString("tweet_text")
                )
            }
        }

        return tweetMap
    }
}