package com.github.kaperd.cognitivecomplexityplugintask.services

import com.intellij.openapi.project.Project
import com.github.kaperd.cognitivecomplexityplugintask.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
