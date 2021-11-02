package com.github.cristiancom.grailsplugin

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.util.MethodParenthesesHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import icons.JetgroovyIcons
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import org.jetbrains.plugins.groovy.lang.completion.GroovyCompletionUtil
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrThisReferenceResolver
import org.jetbrains.plugins.groovy.lang.psi.impl.synthetic.GrLightMethodBuilder
import java.lang.reflect.Modifier

class DomainClassCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val parent: PsiElement = parameters.position.parent
        if (parent is GrReferenceExpression) {
            val cd = resolveReference(parent)
            if (cd != null) {
                if (hasMapping(cd)) {
                    if (inMapping(cd, parent)) {
                            val m = createMethod(cd, "hellobebe", "void")
                                m.addParameter("untest", "String")
                            result.addElement(methodLookupElement(m))
                    }
                }
            }
        }
    }

    private fun methodLookupElement(method: PsiMethod): LookupElement {
        var mb = LookupElementBuilder.createWithSmartPointer(method.name, method).
        withTypeText(method.getReturnType()?.getPresentableText())
        if (method is GrLightMethodBuilder) {
            mb = mb.withTailText("vamos")
        }
        mb = mb.appendTailText(" via grails", true).
        withBoldness(true).
        withCaseSensitivity(true).
        withIcon(JetgroovyIcons.Groovy.Method).
        withInsertHandler(MethodParenthesesHandler(method, false))
        return mb
    }

    private fun inMapping(clazz: PsiClass, element: PsiElement): @NotNull Boolean {
        val field: @Nullable PsiField? = clazz.findFieldByName("mapping", false)
        return PsiTreeUtil.isAncestor(field, element, true)
    }

    private fun hasMapping(clazz: PsiClass): @NotNull Boolean {
        val field: @Nullable PsiField? = clazz.findFieldByName("mapping", false)
        return field != null && field.hasModifierProperty("static")
    }

    private fun createMethod(clazz:PsiClass, methodName: String, returnType: String): GrLightMethodBuilder {
        val mb = GrLightMethodBuilder(clazz.manager, methodName)
        mb.setModifiers(Modifier.PUBLIC)
        mb.setReturnType(returnType, GlobalSearchScope.allScope(clazz.getProject()))
        return mb
    }

    private fun resolveReference(expression: GrReferenceExpression): @Nullable PsiClass? {
        val thisResolved = GrThisReferenceResolver.resolveThisExpression(expression)

        val qualifier = expression.qualifierExpression

        val qualifierType = GroovyCompletionUtil.getQualifierType(qualifier)
        if (qualifierType is PsiClassType) {
            return qualifierType.resolve()
        }

        return null
    }

}
