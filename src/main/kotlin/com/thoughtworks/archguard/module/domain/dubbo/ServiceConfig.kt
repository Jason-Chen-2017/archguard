package com.thoughtworks.archguard.module.domain.dubbo

data class ServiceConfig(val id: String, val interfaceName: String, val ref: String, val version: String?,
                         val group: String?, val subModule: SubModuleDubbo)