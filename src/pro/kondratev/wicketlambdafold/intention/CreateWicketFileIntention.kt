package pro.kondratev.wicketlambdafold.intention

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementFactory
import pro.kondratev.wicketlambdafold.WicketLambdaFoldBundle

/**
 * Create a resource file for Wicket Panel or Page
 */
abstract class CreateWicketFileIntention : BaseElementAtCaretIntentionAction() {

    private val panelQualifier = "org.apache.wicket.markup.html.panelQualifier.Panel";
    private val pageQualifier = "org.apache.wicket.Page"
    private val applicableClasses = listOf(panelQualifier, pageQualifier)

    /**
     * Used as a criteria for availability of this action
     */
    fun hasNoResourceFile(ofExtension: String, project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.parent !is PsiClass) return false
        val psiClass = element.parent as PsiClass
        if (psiClass.containingFile == null || psiClass.containingFile.containingDirectory == null) {
            // Do not show intent, if file or directory is not available
            return false
        }
        return (
            isSubclassOfApplicableClasses(psiClass) &&
            psiClass.containingFile.containingDirectory.findFile(psiClass.name.plus(ofExtension)) == null
        )
    }

    /**
     * Find recursively if the class ends up extending one of applicable classes
     */
    fun isSubclassOfApplicableClasses(c: PsiClass?): Boolean {
        if (c == null) return false
        // XXX maybe add a fuse for very long inheritance chains
        return c.supers.any { applicableClasses.contains(it.qualifiedName) || isSubclassOfApplicableClasses(it) }
    }

    fun getApplicableSuperclassFQN(c: PsiClass?): String? {
        if (c == null) return null
        // XXX maybe add a fuse for very long inheritance chains
        return c.supers.fold<PsiClass?, String?>(null) { acc, pc ->
            if (acc != null) return acc
            if (pc == null) return null
            if (applicableClasses.contains(pc.qualifiedName)) {
                return pc.qualifiedName
            } else {
                return getApplicableSuperclassFQN(pc)
            }
        }
    }

    override fun getText(): String {
        return WicketLambdaFoldBundle.message(javaClass.simpleName + ".name")
    }

    override fun getFamilyName(): String {
        return WicketLambdaFoldBundle.defaultableMessage("intention.category.wicket.html." + javaClass.simpleName)
    }

    fun createPropertiesFile(project: Project, editor: Editor?, element: PsiElement) {
        assert(element.parent is PsiClass)
        val psiClass = element.parent as PsiClass;
        val newFile = psiClass.containingFile.containingDirectory.createFile(psiClass.name.plus(".properties"))
        OpenFileAction.openFile(newFile.virtualFile, project)
    }

    fun createHtmlFile(project: Project, editor: Editor?, element: PsiElement) {
        assert(element.parent is PsiClass)
        val psiClass = element.parent as PsiClass;
        val newFile = psiClass.containingFile.containingDirectory.createFile(psiClass.name.plus(".html"))
        when (getApplicableSuperclassFQN(psiClass)) {
            panelQualifier -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText("<wicket:panelQualifier> </wicket:panelQualifier>"))
            pageQualifier -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText("<wicket:extend> </wicket:extend>"))
            else -> throw IllegalStateException("unexpected supers: "+psiClass.supers)
        }
        OpenFileAction.openFile(newFile.virtualFile, project)
    }

}