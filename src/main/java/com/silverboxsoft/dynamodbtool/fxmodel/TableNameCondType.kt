package com.silverboxsoft.dynamodbtool.fxmodel

import java.util.ArrayList

enum class TableNameCondType(val condition_name: String) {
    PARTIAL_MATCH("Partial match"), HEAD_MATCH("Head match"), TAIL_MATCH("Tail match");

    companion object {
        val titleList: List<String>
            get() {
                val retList: MutableList<String> = ArrayList()
                for (type in values()) {
                    retList.add(type.condition_name)
                }
                return retList
            }

        fun getByName(condition_name: String): TableNameCondType {
            for (type in values()) {
                if (type.condition_name == condition_name) {
                    return type
                }
            }
            return PARTIAL_MATCH
        }
    }
}