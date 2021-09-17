package ru.hse.plugin

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile

class FileTypeAction : AnAction() {
    companion object {
        val notificationGroup: NotificationGroup =
            NotificationGroupManager.getInstance().getNotificationGroup("Task Notification Group")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val file: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        notificationGroup
            .createNotification("File type: ${file.fileType.name}", NotificationType.INFORMATION)
            .notify(e.project)
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = e.getData(CommonDataKeys.VIRTUAL_FILE) != null
    }
}