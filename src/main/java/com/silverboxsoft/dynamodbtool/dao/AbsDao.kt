package com.silverboxsoft.dynamodbtool.dao

import kotlin.Throws
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectInfo
import java.net.URISyntaxException
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import com.silverboxsoft.dynamodbtool.classes.DynamoDbConnectType

import software.amazon.awssdk.regions.Region
import java.net.URI

open class AbsDao(private val connInfo: DynamoDbConnectInfo) {
    @get:Throws(URISyntaxException::class)
    protected val dbClient: DynamoDbClient
        protected get() = if (connInfo.connectType == DynamoDbConnectType.AWS) {
            DynamoDbClient.builder().region(region).build()
        } else {
            DynamoDbClient.builder().endpointOverride(URI(connInfo.endpointUrl)).region(region).build()
        }

    companion object {
        private val region = Region.AP_NORTHEAST_1
    }
}