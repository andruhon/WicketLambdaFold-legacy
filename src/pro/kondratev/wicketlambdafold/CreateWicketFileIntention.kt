package pro.kondratev.wicketlambdafold

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.XmlElementFactory

/**
 * Create a resource file for Wicket Panel or Page
 */
abstract class CreateWicketFileIntention : BaseElementAtCaretIntentionAction() {

    val PANEL = "org.apache.wicket.markup.html.panel.Panel";
    val PAGE = "org.apache.wicket.Page"
    val APPLICABLE_CLASSES = listOf(PANEL, PAGE)

    /**
     * Used as a criteria for availability of this action
     */
    fun hasNoResourceFile(ofExtension: String, project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.parent !is PsiClass) return false
        val psiClass = element.parent as PsiClass
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
        return c.supers.any { APPLICABLE_CLASSES.contains(it.qualifiedName) || isSubclassOfApplicableClasses(it) }
    }

    fun getApplicableSuperclassFQN(c: PsiClass?): String? {
        if (c == null) return null
        // XXX maybe add a fuse for very long inheritance chains
        return c.supers.fold<PsiClass?, String?>(null) { acc, pc ->
            if (acc != null) return acc
            if (pc == null) return null
            if (APPLICABLE_CLASSES.contains(pc.qualifiedName)) {
                return pc.qualifiedName
            } else {
                return getApplicableSuperclassFQN(pc)
            }
        }
    }

    override fun getText(): String {
        return WicketLambdaFoldBundle.message(javaClass.simpleName+".name")
    }

    override fun getFamilyName(): String {
        return WicketLambdaFoldBundle.defaultableMessage("intention.category.wicket.html."+javaClass.simpleName)
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
            PANEL -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText("<wicket:panel> </wicket:panel>"))
            PAGE -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText("<wicket:extend> </wicket:extend>"))
            else -> throw IllegalStateException("unexpected supers: "+psiClass.supers)
        }
        OpenFileAction.openFile(newFile.virtualFile, project)
    }

}