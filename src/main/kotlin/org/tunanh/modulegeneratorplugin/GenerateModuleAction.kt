package org.tunanh.modulegeneratorplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileFactory

class GenerateModuleAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val view = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val dir = view.orChooseDirectory ?: return
        val virtualDir = dir.virtualFile

        val moduleName = Messages.showInputDialog(
            project,
            "Enter module name (e.g., Abc):",
            "Generate Module Files",
            Messages.getQuestionIcon()
        )?.trim()?.replaceFirstChar { it.uppercase() } ?: return

        val packageName = getPackageName(virtualDir, project)

        WriteCommandAction.runWriteCommandAction(project) {
            val factory = PsiFileFactory.getInstance(project)

            val files = mapOf(
                "${moduleName}Api.kt" to Templates.api(moduleName, packageName),
                "${moduleName}CommonConfig.kt" to Templates.commonConfig(moduleName, packageName),
                "${moduleName}UiConfig.kt" to Templates.uiConfig(moduleName, packageName),
                "${moduleName}CallbackConfig.kt" to Templates.callbackConfig(moduleName, packageName),
                "${moduleName}ModuleConfig.kt" to Templates.moduleConfig(moduleName, packageName),
                "${moduleName}ApiImpl.kt" to Templates.apiImpl(moduleName, packageName),
                "${moduleName}ModuleEntry.kt" to Templates.entry(moduleName, packageName)
            )

            files.forEach { (fileName, content) ->
                val file = factory.createFileFromText(fileName, PlainTextLanguage.INSTANCE, content)
                dir.add(file)
            }
        }
    }

    private fun getPackageName(file: VirtualFile, project: Project): String {
        val srcIndex = file.path.indexOf("/src/")
        if (srcIndex == -1) return "com.generated.unknown"

        val relative = file.path.substringAfter("/src/").substringAfter("/java/").replace("/", ".")
        return relative.trim('.')
    }
}