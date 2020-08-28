package com.thoughtworks.archguard.clazz.domain.service

import com.thoughtworks.archguard.clazz.domain.JClass
import com.thoughtworks.archguard.clazz.domain.JClassRepository
import org.springframework.stereotype.Service

@Service
class ClassDependencerService(val repo: JClassRepository) {

    fun findDependencers(target: JClass, deep: Int): JClass {
        buildDependencers(listOf(target), deep)
        return target
    }

    private fun buildDependencers(target: List<JClass>, deep: Int): List<JClass> {
        val container = ArrayList<JClass>()
        doBuildDependencers(target, deep, container)
        return target
    }

    private fun doBuildDependencers(target: List<JClass>, deep: Int, container: MutableList<JClass>) {
        var pendingClasses = target.filterNot { container.contains(it) }
        if (pendingClasses.isEmpty() || deep == 0) {
            container.addAll(pendingClasses)
        } else {
            pendingClasses.forEach { it.dependencers = repo.findDependencers(it.id) }
            container.addAll(pendingClasses)
            doBuildDependencers(pendingClasses.flatMap { it.dependencers }, deep - 1, container)
        }
    }

}
