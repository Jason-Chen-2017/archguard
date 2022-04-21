package org.archguard.scanner.dep.model

/*
 * file for save content ?
 */
class DeclFile(
    val name: String,
    val path: String,
    val content: String,
    val child: List<DeclFile> = listOf()
)

class DepDecl(
    // self name
    val name: String,
    // self version for some cases
    val version: String,
    // like `maven`, `gradle`
    val packageManager: String,
    // requirements in maven
    val dependencies: List<DepDependency>,
    // sub dependencies
    val childrens: List<DepDecl> = listOf()
)

enum class DEP_SCOPE {
    NORMAL,
    TEST
}

class DepDependency(
    // full name groupId:artifactId
    val name: String,
    val group: List<String>,
    val artifact: String,
    val version: String,
    // url: like github, maven
    val scope: DEP_SCOPE = DEP_SCOPE.NORMAL,
    // file: like NPM in local
    val source: DepSource = DepSource(),
    // additional information
    val metadata: DepMetadata = DepMetadata()
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