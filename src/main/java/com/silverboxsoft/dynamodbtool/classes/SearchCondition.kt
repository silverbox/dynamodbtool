package com.silverboxsoft.dynamodbtool.classes

import lombok.Data

@Data
class SearchCondition(val searchWord: String, val onlySelectedColumn: Boolean = false
                      , val caseSensitive: Boolean = false, val searchAsRegEx: Boolean = false) {

    override fun toString(): String {
        return "word=[%s], onlySelCol=%s, caseSensitive=%s, regEx=%s".format(searchWord, onlySelectedColumn, caseSensitive, searchAsRegEx)
    }
}
