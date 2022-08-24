package com.thoughtworks.archguard.insights.application

import org.archguard.domain.insight.*

fun <T> postFilter(query: Query, models: List<T>, filter: (data: T, condition: Either<RegexQuery, VersionQuery>) -> Boolean): List<T> {
    val postqueries = query.postqueries
    if (postqueries.isEmpty()) {
        return models
    } else if (postqueries.size == 1) {
        return models.filter {
            val regexQuery = postqueries[0]
            filter(it, regexQuery)
        }
    } else {
        var finalResult = models
        for (condition in postqueries) {
            val relation = if (condition.isLeft) {
                condition.getLeftOrNull()!!.relation
            } else {
                condition.getRightOrNull()!!.relation
            }

            finalResult = when (relation) {
                null, CombinatorType.And -> {
                    // first one and all `and`s
                    finalResult.filter {
                        filter(it, condition)
                    }
                }
                CombinatorType.Or -> {
                    val newOnes = models.filter {
                        filter(it, condition)
                    }

                    (finalResult union newOnes).toList()
                }
                else -> {
                    throw java.lang.RuntimeException("Invalid relation in the query")
                }
            }
        }
        return finalResult
    }
}