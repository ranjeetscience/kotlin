
description = "Kotlin Android Compat Replacer Compiler Plugin"

apply { plugin("kotlin") }

plugins { java }

dependencies {
    compile(ideaSdkCoreDeps("intellij-core"))
    compile(project(":compiler:util"))
    compile(project(":compiler:plugin-api"))
    compile(project(":compiler:frontend"))
    compile(project(":compiler:frontend.java"))
    compile(project(":compiler:backend"))
}

sourceSets {
    "main" { projectDefault() }
    "test" { projectDefault() }
}

dist()

ideaPlugin()

testsJar {}