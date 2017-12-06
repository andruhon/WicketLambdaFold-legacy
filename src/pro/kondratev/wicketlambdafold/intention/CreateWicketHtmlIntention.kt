package pro.kondratev.wicketlambdafold.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement


class CreateWicketHtmlIntention : CreateWicketFileIntention() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return this.hasNoResourceFile(".html", project, editor, element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        this.createHtmlFile(project, editor, element)
    }

}