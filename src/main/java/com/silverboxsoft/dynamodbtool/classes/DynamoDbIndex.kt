package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class DynamoDbIndex(val indexName: String, val hashKey: String, val sortKey: String?)
