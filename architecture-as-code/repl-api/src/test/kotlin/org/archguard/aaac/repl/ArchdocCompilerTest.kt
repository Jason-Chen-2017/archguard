package org.archguard.aaac.repl

import io.kotest.matchers.shouldBe
import org.archguard.aaac.repl.compiler.KotlinReplWrapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DslTest {
    private lateinit var compiler: KotlinReplWrapper


    @BeforeEach
    internal fun setUp() {
        this.compiler = KotlinReplWrapper()
    }

    @Test
    internal fun simple_eval() {
        compiler.eval("val x = 3")
        val res = compiler.eval("x*2")
        res.rawValue shouldBe 6
    }

    @Test
    internal fun local_file() {
        compiler.eval(
            """%use archguard

            var layer = layered {
                prefixId("org.archguard")
                component("controller") dependentOn component("service")
                组件("service") 依赖于 组件("repository")
            }
            """
        )

        val res = compiler.eval("layer.components().size")
        res.rawValue shouldBe 3

        val name = compiler.eval("layer.components()[0].name")
        name.rawValue shouldBe "controller"
    }

}