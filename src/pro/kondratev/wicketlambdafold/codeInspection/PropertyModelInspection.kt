package pro.kondratev.wicketlambdafold.codeInspection

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInsight.intention.QuickFixFactory
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.source.PsiImportStatementImpl
import org.jetbrains.annotations.NotNull
import pro.kondratev.wicketlambdafold.WicketLambdaFoldBundle


class PropertyModelInspection : AbstractBaseJavaLocalInspectionTool() {

    private val QUICK_FIX_FACTORY = QuickFixFactory.getInstance()

    private val propertyModelQualifier = "org.apache.wicket.model.PropertyModel"

    private val message = WicketLambdaFoldBundle.message(javaClass.simpleName)

    @NotNull
    override fun getDisplayName(): String {
        return "Deprecated use of PropertyModel, use LambdaModel instead"
    }

    @NotNull
    override fun getShortName(): String {
        return "PropertyModel"
    }

    @NotNull
    override fun getGroupDisplayName(): String {
        return GroupNames.MATURITY_GROUP_NAME
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : JavaElementVisitor() {
            override fun visitExpression(expression: PsiExpression?) {
                when (expression) {
                    is PsiNewExpression -> if (propertyModelQualifier == expression.classReference?.qualifiedName) {
                        holder.registerProblem(expression, message, PropertyModelLocalQuickFix())
                    }
                    is PsiMethodCallExpression -> {
                        val q = expression.methodExpression.qualifier
                        if (q is PsiReferenceExpression && propertyModelQualifier == q.qualifiedName) {
                            holder.registerProblem(expression, message, PropertyModelLocalQuickFix())
                        }
                    }
                }
                super.visitExpression(expression)
            }
        }
    }

    override fun isEnabledByDefault(): Boolean {
        return true
    }

    internal inner class PropertyModelLocalQuickFix : LocalQuickFix {

        override fun getFamilyName(): String {
            return WicketLambdaFoldBundle.defaultableMessage(this@PropertyModelInspection.javaClass.simpleName + "." + this.javaClass.simpleName)
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            val file = element.containingFile as? PsiJavaFile ?: return
            if(file.importList?.findSingleClassImportStatement("org.apache.wicket.model.LambdaModel") == null) {
                val factory = JavaPsiFacade.getElementFactory(project)
                println("found 1")
                // do factory.createImportStatement somehow
                file.importList!!.add(factory.createImportStatementOnDemand("org.apache.wicket.model.LambdaModel"))
            }
            if(file.importList?.importStatements?.any { it.qualifiedName.equals("org.apache.wicket.model.LambdaModel") } != true){
                println("found 2")
            }
            when (element) {
                is PsiNewExpression -> {
                    var params = element.children.find { it is PsiExpressionList } as PsiExpressionList
                    println("PsiNewExpression")
                }
                is PsiMethodCallExpression -> {
                    println("PsiMethodCallExpression")
                }
                else -> return
            }
        }

    }

}