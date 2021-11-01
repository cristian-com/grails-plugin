package com.github.cristiancom.grailsplugin

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.util.ProcessingContext
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.GrThisReferenceResolver

class DomainClassCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {

        val parent = parameters.position.parent
        if (parent is GrReferenceExpression) {
            val cd = GrailsPsiUtil.resolveReference(parent as GrReferenceExpression)
            if (cd) {
                if (GrailsClassHelper.hasMapping(cd)) {
                    if (GrailsClassHelper.inMapping(cd, parent)) {
                        GrailsDomainClassApi.MappingMethods.each { methodName, methodParams ->
                            def m = GrailsMethodUtil.createMethod(cd, methodName, Void.name)
                            methodParams.each { argName, argType ->
                                m.addParameter(argName, argType)
                            }
                            result.addElement(methodLookupElement(m))
                        }
                    }
                    boolean isClass = ResolveUtil.resolvesToClass(parent.firstChild)
                    if (isClass) {
                        GrailsDomainClassApi.staticMethods(cd).each { methodName, methodVariants ->
                            methodVariants.each { result.addElement(methodLookupElement(it)) }
                        }
                    }
                }
            }
        }
    }

    fun resolveReference(expression: GrReferenceExpression) {
        val thisResolved = GrThisReferenceResolver.resolveThisExpression(expression)
        val resolvedClass = thisResolved?.elementAt(0)?.element

        if (resolvedClass == null) {
            val qualifier = expression.qualifierExpression
                    PsiType qualifierType = GroovyCompletionUtil.getQualifierType(qualifier)
            if (qualifierType instanceof PsiClassType) {
                resolvedClass = ((PsiClassType) qualifierType)?.resolve()
            }
        }
        return resolvedClass
    }

}
