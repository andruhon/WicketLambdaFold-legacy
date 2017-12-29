package pro.kondratev.wicketlambdafold.codeInspection

import com.intellij.codeInsight.daemon.GroupNames
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.source.PsiImmediateClassType
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl
import com.intellij.psi.util.PropertyUtilBase
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.siyeh.ig.psiutils.ImportUtils
import org.jetbrains.annotations.NotNull
import pro.kondratev.wicketlambdafold.WicketLambdaFoldBundle.*


class PropertyModelInspection : AbstractBaseJavaLocalInspectionTool() {

    private val message = message(javaClass.simpleName)

    @NotNull
    override fun getDisplayName(): String {
        return message
    }

    @NotNull
    override fun getShortName(): String {
        return PROPERTY_MODEL_NAME
    }

    @NotNull
    override fun getGroupDisplayName(): String {
        return GroupNames.MATURITY_GROUP_NAME
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return if (!PsiUtil.isLanguageLevel8OrHigher(holder.file)) {
            PsiElementVisitor.EMPTY_VISITOR
        } else object : JavaElementVisitor() {
            override fun visitExpression(expression: PsiExpression?) {
                when (expression) {
                    is PsiNewExpression -> if (PROPERTY_MODEL_FQN == expression.classReference?.qualifiedName) {
                        holder.registerProblem(expression, message, PropertyModelLocalQuickFix())
                    }
                    is PsiMethodCallExpression -> {
                        val q = expression.methodExpression.qualifier
                        if (q is PsiReferenceExpression && PROPERTY_MODEL_FQN == q.qualifiedName) {
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
            return defaultableMessage(this@PropertyModelInspection.javaClass.simpleName + "." + this.javaClass.simpleName)
        }

        override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
            val element = descriptor.psiElement
            when (element) {
                is PsiNewExpression -> {
                    fixPropertyModelNew(element, project)
                }
                is PsiMethodCallExpression -> {
                    // TODO PropertyModel.of
                }
                else -> return
            }
        }

        private fun fixPropertyModelNew(element: PsiNewExpression, project: Project) {
            val factory = JavaPsiFacade.getElementFactory(project)
            val resolveHelper = JavaPsiFacade.getInstance(project).resolveHelper
            ImportUtils.addImportIfNeeded(resolveHelper.resolveReferencedClass(LAMBDA_MODEL_FQN, element)!!, element)
            val args = element.children.find { it is PsiExpressionList } as PsiExpressionList
            val (modelArg, propNameArg) = args.expressions
            assert(propNameArg is PsiLiteralExpressionImpl)
            val genericParam = (GenericsUtil.getVariableTypeByExpressionType(modelArg.type) as PsiImmediateClassType).parameters[0]
            val modelObjectClass = PsiTypesUtil.getPsiClass(if (genericParam is PsiWildcardType) genericParam.bound else genericParam)!!
            val propName = (propNameArg as PsiLiteralExpressionImpl).innerText!!
            val getter: PsiMethod? = PropertyUtilBase.findPropertyGetter(modelObjectClass, propName, false, true)
            val setter: PsiMethod? = PropertyUtilBase.findPropertySetter(modelObjectClass, propName, false, true)
            if (getter != null) {
                val methodQualifierPrefix = modelObjectClass.qualifiedName + "::"
                val newArgs = mutableListOf<String>((modelArg as PsiReferenceExpressionImpl).qualifiedName, methodQualifierPrefix + getter.name)
                if (setter != null) {
                    newArgs.add(methodQualifierPrefix + setter.name)
                }
                val lambdaModelExpression = factory.createExpressionFromText(LAMBDA_MODEL_NAME + ".of(" + newArgs.joinToString(", ") + ")", element)
                element.replace(lambdaModelExpression)
            }
        }

    }

}