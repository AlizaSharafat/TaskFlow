package com.example.taskflow.model

import java.io.Serializable
import java.util.UUID


data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    var isDone: Boolean = false
) : Serializable

data class Attachment(
    val fileName: String,
    val styleIndex: Int = 0
) : Serializable

data class Project(
    val id: String = UUID.randomUUID().toString(),
    var tag: String,
    var name: String,
    var description: String,
    var attachments: MutableList<Attachment> = mutableListOf(),
    var tasks: MutableList<TaskItem> = mutableListOf(),
    var commentCount: Int = 0
) : Serializable {

    fun doneTaskCount(): Int = tasks.count { it.isDone }

    fun totalTaskCount(): Int = tasks.size
}