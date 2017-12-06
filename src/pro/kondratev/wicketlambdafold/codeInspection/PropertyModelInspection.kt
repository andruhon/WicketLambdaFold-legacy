package pro.kondratev.wicketlambdafold.codeInspection

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*
import org.jetbrains.annotations.NotNull
import pro.kondratev.wicketlambdafold.WicketLambdaFoldBundle


class PropertyModelInspection: AbstractBaseJavaLocalInspectionTool() {

    private val propertyModelQualifier = "org.apache.wicket.model.PropertyModel"

    private val message = WicketLambdaFoldBundle.message(javaClass.simpleName);

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
                        holder.registerProblem(expression, message)
                    }
                    is PsiMethodCallExpression -> {
                        val q = expression.methodExpression.qualifier
                        if (q is PsiReferenceExpression && propertyModelQualifier == q.qualifiedName) {
                            holder.registerProblem(expression, message)
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

}