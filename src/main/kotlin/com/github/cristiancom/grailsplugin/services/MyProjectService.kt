package com.github.cristiancom.grailsplugin.services

import com.intellij.openapi.project.Project
import com.github.cristiancom.grailsplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
