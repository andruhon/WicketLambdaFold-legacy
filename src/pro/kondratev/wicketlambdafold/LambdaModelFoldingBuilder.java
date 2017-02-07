package pro.kondratev.wicketlambdafold;

import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.FoldingBuilderEx;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LambdaModelFoldingBuilder extends FoldingBuilderEx {
    private static final String GET_PREFIX = "::get";
    private static final String IS_PREFIX = "::is";
    private static final String SET_PREFIX = "::set";
    private static final String SET_SUFFIX = "/set";

    @NotNull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@NotNull PsiElement root, @NotNull Document document, boolean b) {
        List<FoldingDescriptor> descriptors = new ArrayList<>();

        boolean lambdaModelImported = PsiTreeUtil.findChildrenOfType(root, PsiImportStatement.class).stream()
                .anyMatch(statement -> "org.apache.wicket.model.LambdaModel".equals(statement.getQualifiedName()));

        if (lambdaModelImported) {
            root.accept(new JavaRecursiveElementWalkingVisitor() {

                @Override
                public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                    if ("LambdaModel.of".equals(expression.getMethodExpression().getQualifiedName())) {
                        addLambdaModelFoldDescriptor(expression);
                    }
                    super.visitMethodCallExpression(expression);
                }

                private void addLambdaModelFoldDescriptor(PsiMethodCallExpression expression) {
                    PsiExpression[] args = expression.getArgumentList().getExpressions();
                    // LambdaModel.of with model getter and setter should have exactly 3 params
                    // 2 params is read only and is already short enough
                    if (args.length != 3) {
                        return;
                    }
                    PsiExpression modelDef = args[0];
                    PsiExpression getterDef = args[1];
                    PsiExpression setterDef = args[2];
                    String getterStr = getterDef.getText();
                    String setterStr = setterDef.getText();
                    PsiType modelDefType = modelDef.getType();
                    boolean hasGetterAndSetter =
                            (getterStr.contains(GET_PREFIX) && getterStr.replace(GET_PREFIX, "/").equals(setterStr.replace(SET_PREFIX, "/"))) ||
                                    (getterStr.contains(IS_PREFIX) && getterStr.replace(IS_PREFIX, "/").equals(setterStr.replace(SET_PREFIX, "/")));

                    // First param is assignable to IModel and following two looks like getter and setter
                    if (
                            modelDefType == null ||
                                    !PsiType.getTypeByName("org.apache.wicket.model.IModel", root.getProject(), root.getResolveScope()).isAssignableFrom(modelDefType) ||
                                    !hasGetterAndSetter
                            ) {
                        return;
                    }

                    descriptors.add(
                            new FoldGetSetDescriptor(
                                    expression,
                                    new TextRange(getterDef.getTextRange().getStartOffset(), setterDef.getTextRange().getEndOffset()),
                                    getterStr
                            )
                    );
                }

            });
        }

        return descriptors.toArray(new FoldingDescriptor[descriptors.size()]);
    }

    @Nullable
    @Override
    public String getPlaceholderText(@NotNull ASTNode astNode) {
        return "...";
    }

    @Override
    public boolean isCollapsedByDefault(@NotNull ASTNode astNode) {
        return true;
    }

    static class FoldGetSetDescriptor extends FoldingDescriptor {

        final String getterStr;
        final String prefix;

        FoldGetSetDescriptor(PsiMethodCallExpression expression, @NotNull TextRange range, String getterStr) {
            // Intellij doesn't show collapse/expand icon if the expression was already one-line
            // Will show for >1 liners
            super(expression.getNode(), range);
            this.getterStr = getterStr;
            this.prefix = getterStr.contains(IS_PREFIX) ? IS_PREFIX : GET_PREFIX;
        }

        @Nullable
        @Override
        public String getPlaceholderText() {
            return getterStr.replace(prefix, prefix+SET_SUFFIX);
        }
    }
}

