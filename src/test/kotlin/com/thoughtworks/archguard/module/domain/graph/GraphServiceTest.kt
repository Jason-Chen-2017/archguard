package com.thoughtworks.archguard.module.domain.graph

import com.thoughtworks.archguard.module.domain.LogicModuleRepository
import com.thoughtworks.archguard.module.domain.dependency.DependencyService
import com.thoughtworks.archguard.module.domain.model.Dependency
import com.thoughtworks.archguard.module.domain.model.JClassVO
import com.thoughtworks.archguard.module.domain.model.LogicComponent
import com.thoughtworks.archguard.module.domain.model.LogicModule
import com.thoughtworks.archguard.module.domain.plugin.PluginManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GraphServiceTest {
    @MockK
    lateinit var logicModuleRepository: LogicModuleRepository

    @MockK
    lateinit var dependencyService: DependencyService

    @MockK
    lateinit var pluginManager: PluginManager

    private lateinit var service: GraphService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        service = GraphService(logicModuleRepository, dependencyService, pluginManager)
    }

    @Test
    fun `should get graph of all logic modules dependency`() {
        val logicModule1 = LogicModule.createWithOnlyLeafMembers("id1", "module1", listOf(LogicComponent.createLeaf("submodule1.class")))
        val logicModule2 = LogicModule.createWithOnlyLeafMembers("id2", "module2", listOf(LogicComponent.createLeaf("submodule2.class")))
        val logicModule3 = LogicModule.createWithOnlyLeafMembers("id3", "module3", listOf(LogicComponent.createLeaf("submodule3.class")))
        val logicModules = listOf(logicModule1, logicModule2, logicModule3)

        val dependency1 = Dependency(JClassVO("class","submodule1"), JClassVO("class", "submodule2"))
        val dependency2 = Dependency(JClassVO("class","submodule1"), JClassVO("class", "submodule3"))
        val dependency3 = Dependency(JClassVO("class","submodule2"), JClassVO("class", "submodule3"))
        val dependencies = listOf(dependency1, dependency2, dependency3)

        every { pluginManager.getPlugins() } returns emptyList()
        every { logicModuleRepository.getAllByShowStatus(true) } returns logicModules
        every { dependencyService.getAllClassDependencies() } returns dependencies

        // when
        val moduleGraph = service.getLogicModuleGraph()

        // then
        assertEquals(3, moduleGraph.nodes.size)
        assertEquals(3, moduleGraph.edges.size)
    }

}
