package com.thoughtworks.archguard.dependence.infrastructure.module

import com.thoughtworks.archguard.dependence.domain.module.Dependency
import com.thoughtworks.archguard.dependence.domain.module.LogicModule
import com.thoughtworks.archguard.dependence.domain.module.LogicModuleRepository
import com.thoughtworks.archguard.dependence.domain.module.LogicModuleStatus
import com.thoughtworks.archguard.dependence.domain.module.ModuleDependency
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository

@Repository
class LogicModuleRepositoryImpl : LogicModuleRepository {

    @Autowired
    lateinit var jdbi: Jdbi

    override fun getAllByShowStatus(isShow: Boolean): List<LogicModule> {
        if (isShow) {
            return this.getAll().filter { it.status == LogicModuleStatus.NORMAL }
        }
        return this.getAll().filter { it.status == LogicModuleStatus.HIDE }
    }

    override fun getAll(): List<LogicModule> {
        val modules = jdbi.withHandle<List<LogicModuleDTO>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(LogicModuleDTO::class.java))
            it.createQuery("select id, name, members, status from logic_module")
                    .mapTo(LogicModuleDTO::class.java)
                    .list()
        }
        return modules.map { LogicModule(it.id, it.name, it.members.split(',').sorted(), it.status) }
    }

    override fun update(id: String, logicModule: LogicModule) {
        jdbi.withHandle<Int, Nothing> {
            it.createUpdate("update logic_module set " +
                    "name = '${logicModule.name}', " +
                    "members = '${logicModule.members.joinToString(",")}', " +
                    "status = '${logicModule.status}' " +
                    "where id = '${logicModule.id}'")
                    .execute()
        }
    }

    override fun create(logicModule: LogicModule) {
        jdbi.withHandle<Int, Nothing> { handle ->
            handle.execute("insert into logic_module (id, name, members, status) values (?, ?, ?, ?)",
                    logicModule.id, logicModule.name, logicModule.members.joinToString(","), logicModule.status)
        }
    }

    override fun delete(id: String) {
        jdbi.withHandle<Int, Nothing> { handle ->
            handle.execute("delete from logic_module where id = '$id'")
        }
    }

    override fun deleteAll() {
        jdbi.withHandle<Int, Nothing> { handle ->
            handle.execute("delete from logic_module")
        }
    }

    override fun saveAll(logicModules: List<LogicModule>) {
        logicModules.forEach {
            jdbi.withHandle<Int, Nothing> { handle ->
                handle.execute("insert into logic_module (id, name, members, status) values (?, ?, ?, ?)",
                        it.id, it.name, it.members.joinToString(","), it.status)
            }
        }
    }

    override fun updateAll(logicModules: List<LogicModule>) {
        logicModules.forEach {
            update(it.id.toString(), it)
        }
    }

    override fun getDependence(caller: String, callee: String): List<ModuleDependency> {
        val callerTemplate = defineTableTemplate(getMembers(caller))
        val calleeTemplate = defineTableTemplate(getMembers(callee))

        val sql = "select a.module caller, a.clzname callerClass, a.name callerMethod, b.module callee, b.clzname calleeClass, b.name calleeMethod from ($callerTemplate) a, ($calleeTemplate) b, _MethodCallees mc where a.id = mc.a and b.id = mc.b"

        return jdbi.withHandle<List<ModuleDependency>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(ModuleDependency::class.java))
            it.createQuery(sql)
                    .mapTo(ModuleDependency::class.java)
                    .list()
        }

    }

    override fun getAllClassDependency(members: List<String>): List<Dependency> {
        val tableTemplate = defineTableTemplate(members)

        val sql = "select concat(concat(a.module, '.'), a.clzname) caller, " +
                "concat(concat(b.module, '.'), b.clzname) callee " +
                "from ($tableTemplate) a, ($tableTemplate) b,  _MethodCallees mc " +
                "where a.id = mc.a and b.id = mc.b"
        return jdbi.withHandle<List<Dependency>, Nothing> {
            it.registerRowMapper(ConstructorMapper.factory(Dependency::class.java))
            it.createQuery(sql)
                    .mapTo(Dependency::class.java)
                    .list()
                    .filter { it -> it.caller != it.callee }
        }
    }

    fun getMembers(name: String): List<String> {
        val sql = "select members from logic_module where name = '$name'"
        val members = jdbi.withHandle<String, Nothing> {
            it.createQuery(sql)
                    .mapTo(String::class.java)
                    .first()
        }

        return members.split(',')
    }

    override fun getParentClassId(id: String): List<String> {
        val sql = "select b from _ClassParent where a = '$id'"
        return jdbi.withHandle<List<String>, Nothing> {
            it.createQuery(sql)
                    .mapTo(String::class.java)
                    .list()
        }
    }

    fun defineTableTemplate(members: List<String>): String {
        var tableTemplate = "select * from JMethod where ("
        val filterConditions = ArrayList<String>()
        members.forEach { s ->
            val member = s.split('.')
            if (member.size == 1) {
                filterConditions.add("module = '${member[0]}'")
            }
            if (member.size > 1) {
                filterConditions.add("(module = '${member[0]}' and clzname like '${member.subList(1, member.size).joinToString(".") + "."}%')")
                filterConditions.add("(module = '${member[0]}' and clzname='${member.subList(1, member.size).joinToString(".")}')")
            }
        }
        tableTemplate += filterConditions.joinToString(" or ")
        tableTemplate += ")"
        println(tableTemplate)
        return tableTemplate
    }

}

