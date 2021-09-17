package ru.hse.plugin

import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.caches.project.NotUnderContentRootModuleInfo.project
import org.jetbrains.kotlin.psi.KtFunction

class MethodAction : AnAction() {
    companion object {
        val notificationGroup: NotificationGroup =
            NotificationGroupManager.getInstance().getNotificationGroup("Task Notification Group")
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document: Document = editor.document
        val ktFunction: KtFunction = findKtFunction(e) ?: return
        val functionTextRange: TextRange = ktFunction.textRange
        val functionLinesNumber: Int = ReadAction.compute<Int, Nothing> {
            document.getLineNumber(functionTextRange.endOffset) - document.getLineNumber(functionTextRange.startOffset) + 1
        }
        val message = """
            Name: ${ktFunction.name}
            Number of lines: $functionLinesNumber
        """.trimIndent()
        if (showDialogWithMessage(message) == 1) {
            if (!document.isWritable) {
                notificationGroup.createNotification(
                    "Can't add method information: file is read-only",
                    NotificationType.ERROR
                ).notify(project)
                return
            }
            val spacesBeforeMethodStart: Int = ReadAction.compute<Int, Nothing> {
                val methodStartLineNumber: Int = document.getLineNumber(functionTextRange.startOffset)
                functionTextRange.startOffset - document.getLineStartOffset(methodStartLineNumber)
            }
            ApplicationManager.getApplication().invokeLater {
                WriteAction.run<Nothing> {
                    CommandProcessor.getInstance().executeCommand(
                        project,
                        {
                            document.insertString(
                                functionTextRange.startOffset,
                                "// Method '${ktFunction.name}' takes $functionLinesNumber lines\n"
                                    + " ".repeat(spacesBeforeMethodStart)
                            )
                        },
                        "Insert Comment",
                        this.javaClass
                    )
                }
            }
        }
    }

    private fun findKtFunction(e: AnActionEvent): KtFunction? {
        val project: Project = e.project ?: return null
        val editor: Editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val file: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        val psiFile: PsiFile = PsiManager.getInstance(project).findFile(file) ?: return null
        val element: PsiElement = psiFile.findElementAt(editor.caretModel.offset) ?: return null
        return PsiTreeUtil.getParentOfType(element, KtFunction::class.java)
    }

    private fun showDialogWithMessage(message: String): Int {
        return Messages.showDialog(
            project,
            message,
            "Current Method Information",
            arrayOf(Messages.getCancelButton(), "Add method info"),
            1,
            null
        )
    }

    override fun update(e: AnActionEvent) {
        val file: VirtualFile? = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabled = file != null && file.extension == "kt"
    }
}