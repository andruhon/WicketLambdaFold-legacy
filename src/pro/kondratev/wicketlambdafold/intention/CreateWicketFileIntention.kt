package pro.kondratev.wicketlambdafold.intention

import com.intellij.codeInsight.intention.BaseElementAtCaretIntentionAction
import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.diagnostic.Logger
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

    companion object {
        private const val MAX_SUPERCLASS_SCAN_DEPTH = 30
        private const val PANEL_QUALIFIER = "org.apache.wicket.markup.html.panel.Panel"
        private const val PAGE_QUALIFIER = "org.apache.wicket.Page"
        private const val PANEL_CONTENT = "<wicket:panel> </wicket:panel>";
        private const val PAGE_CONTENT = "<wicket:extend> </wicket:extend>";
        private val APPLICABLE_CLASSES = listOf(PANEL_QUALIFIER, PAGE_QUALIFIER)
        private val logger = Logger.getInstance(javaClass.enclosingClass.simpleName)
    }

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

    protected fun isSubclassOfApplicableClasses(c: PsiClass?): Boolean {
        return isSubclassOfApplicableClasses(c, MAX_SUPERCLASS_SCAN_DEPTH)
    }

    /**
     * Find recursively if the class ends up extending one of applicable classes
     */
    protected fun isSubclassOfApplicableClasses(c: PsiClass?, fuse: Int): Boolean {
        if (c == null) return false
        if (fuse <= 0) {
            logger.error(String.format("isSubclassOfApplicableClasses exceeds the MAX_SUPERCLASS_SCAN_DEPTH of %d", MAX_SUPERCLASS_SCAN_DEPTH))
            return false
        }
        // XXX maybe add a fuse for very long inheritance chains
        return c.supers.any { APPLICABLE_CLASSES.contains(it.qualifiedName) || isSubclassOfApplicableClasses(it, fuse-1) }
    }

    protected fun getApplicableSuperclassFQN(c: PsiClass?): String? {
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
        return WicketLambdaFoldBundle.message(javaClass.simpleName + ".name")
    }

    override fun getFamilyName(): String {
        return WicketLambdaFoldBundle.defaultableMessage("intention.category.wicket.html." + javaClass.simpleName)
    }

    fun createPropertiesFile(project: Project, editor: Editor?, element: PsiElement) {
        assert(element.parent is PsiClass)
        val psiClass = element.parent as PsiClass
        val newFile = psiClass.containingFile.containingDirectory.createFile(psiClass.name.plus(".properties"))
        OpenFileAction.openFile(newFile.virtualFile, project)
    }

    fun createHtmlFile(project: Project, editor: Editor?, element: PsiElement) {
        assert(element.parent is PsiClass)
        val psiClass = element.parent as PsiClass
        val newFile = psiClass.containingFile.containingDirectory.createFile(psiClass.name.plus(".html"))
        when (getApplicableSuperclassFQN(psiClass)) {
            PANEL_QUALIFIER -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText(PANEL_CONTENT))
            PAGE_QUALIFIER -> newFile.add(XmlElementFactory.getInstance(project).createTagFromText(PAGE_CONTENT))
            else -> IllegalStateException("unexpected supers: "+psiClass.supers)
        }
        OpenFileAction.openFile(newFile.virtualFile, project)
    }

}