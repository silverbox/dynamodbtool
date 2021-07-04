package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbConnectInfo(private val connectType: DynamoDbConnectType, private val endpointUrl: String)