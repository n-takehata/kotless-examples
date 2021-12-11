package com.example.kotless

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import com.amazonaws.services.dynamodbv2.model.PutItemResult
import io.kotless.AwsResource
import io.kotless.dsl.lang.withKotlessLocal

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
