package com.thoughtworks.archguard.insights.application

import org.archguard.domain.insight.FieldFilter
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper
import org.springframework.stereotype.Repository

@Repository
class ScaInsightRepositoryImpl(val jdbi: Jdbi) : ScaInsightRepository {
    override fun filterByConditionWithSystemId(id: Long, models: List<FieldFilter>): List<ScaModelDto> {
        var sql =
            "select dep_artifact, dep_group, dep_version, dep_name" +
                    " from project_composition_dependencies where system_id = :id "

        val additionCondition: String = FieldFilter.toQuery(models)
        if (additionCondition.isNotEmpty()) {
            sql += additionCondition
        }

        return jdbi.withHandle<List<ScaModelDto>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(ScaModelDto::class.java))
            it.createQuery(sql)
                .bind("id", id)
                .mapTo(ScaModelDto::class.java)
                .list()
        }
    }

    override fun filterByCondition(models: List<FieldFilter>): List<ScaModelDto> {
        var sql =
            "select dep_artifact, dep_group, dep_version, dep_name" +
                    " from project_composition_dependencies "

        val additionCondition: String = FieldFilter.toQuery(models)
        if (additionCondition.isNotEmpty()) {
            sql += "where $additionCondition"
        }

        return jdbi.withHandle<List<ScaModelDto>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(ScaModelDto::class.java))
            it.createQuery(sql)
                .mapTo(ScaModelDto::class.java)
                .list()
        }
    }
}
