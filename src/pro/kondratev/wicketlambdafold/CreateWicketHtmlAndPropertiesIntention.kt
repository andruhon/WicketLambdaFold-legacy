package pro.kondratev.wicketlambdafold

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class CreateWicketHtmlAndPropertiesIntention : CreateWicketFileIntention() {

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return(
            this.hasNoResourceFile(".html", project, editor, element) &&
            this.hasNoResourceFile(".properties", project, editor, element)
        )
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        this.createHtmlFile(project, editor, element)
        this.createPropertiesFile(project, editor, element)
    }

}