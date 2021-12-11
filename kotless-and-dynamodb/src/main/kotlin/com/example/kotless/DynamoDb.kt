package com.example.kotless

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.GetItemRequest
import io.kotless.AwsResource
import io.kotless.dsl.lang.withKotlessLocal

fun findItem(): String {
    val client = AmazonDynamoDBClientBuilder.defaultClient()
    val request = GetItemRequest()
        .withKey(mapOf("id" to AttributeValue().withN("101")))
        .withTableName("Commodity")
    val result = client.getItem(request).item

    return result["id"].toString()
}
