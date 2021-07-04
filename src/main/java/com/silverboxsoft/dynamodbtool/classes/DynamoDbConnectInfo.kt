package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbConnectInfo(val connectType: DynamoDbConnectType, val endpointUrl: String)