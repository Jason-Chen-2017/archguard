package org.archguard.scanner.core.sca

/*
 * file for save content ?
 */
class DeclFileTree(
    val filename: String,
    val path: String,
    val content: String,
    var childrens: List<DeclFileTree> = listOf(),
    val name: String = ""
)

class PackageDependencies(
    // self name
    val name: String,
    // self version for some cases
    val version: String,
    // like `maven`, `gradle`
    val packageManager: String,
    // requirements in maven
    val dependencies: List<DependencyEntry>,
    // file path
    val path: String,
    // child dependencies
    val childrens: List<PackageDependencies> = listOf(),
)

enum class DEP_SCOPE {
    NORMAL,
    RUNTIME,
    TEST,
    OPTIONAL,
    DEV;

    companion object {
        fun from(str: String): DEP_SCOPE {
            return when (str) {
                "test" -> {
                    TEST
                }
                else -> {
                    NORMAL
                }
            }
        }
    }
}

class DependencyEntry(
    // full name groupId:artifactId
    val name: String,
    val group: String,
    val artifact: String,
    val version: String,
    // url: like github, maven
    val scope: DEP_SCOPE = DEP_SCOPE.NORMAL,
    // file: like NPM in local
    val source: DepSource = DepSource(),
    // additional information
    val metadata: DepMetadata = DepMetadata(),
)

// like GitHub link,
class DepSource(
    val type: String = "",
    val url: String = "",
    val branch: String = "",
    val ref: String = "",
)

class DepMetadata(
    val packagingType: String = "",
    val propertyName: String = "",
)
