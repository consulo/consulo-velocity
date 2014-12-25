/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.velocity.editorActions;

import gnu.trove.THashSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.daemon.impl.quickfix.CreateFromUsageUtils;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.velocity.psi.VtlDirectiveHeader;
import com.intellij.velocity.psi.VtlInterpolation;
import com.intellij.velocity.psi.VtlLoopVariable;
import com.intellij.velocity.psi.VtlMethodCallExpression;
import com.intellij.velocity.psi.VtlVariable;
import com.intellij.velocity.psi.directives.VtlAssignment;
import com.intellij.velocity.psi.reference.VelocityNamingUtil;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;

/**
 * @author Alexey Chmutov
 */
public class TypeInferenceUtil {
    @NotNull
    public static Collection<String> inferVariableType(@NotNull final VtlVariable variable) {
        return inferVariableType(ReferencesSearch.search(variable));
    }

    @NotNull
    public static Collection<String> inferVariableType(final Query<PsiReference> query) {
        final Ref<Set<String>> intersection = Ref.create(null);
        query.forEach(new Processor<PsiReference>() {
            private boolean myIterableType = false;

            public boolean process(final PsiReference psiReference) {
                if (!(psiReference instanceof VtlReferenceExpression)) {
                    return true;
                }
                final VtlReferenceExpression reference = (VtlReferenceExpression) psiReference;
                final VtlReferenceExpression parentReference = reference.getParentReferenceExpression();

                if (!myIterableType && parentReference != null && StringUtil.isNotEmpty(parentReference.getReferenceName())) {
                    final Set<String> classes = suggestClassesWithMember(parentReference);
                    intersect(classes, intersection);
                    return true;
                }
                PsiElement ancestor = reference.getParent();
                if (!(ancestor instanceof VtlInterpolation)) {
                    return true;
                }
                ancestor = ancestor.getParent();
                if (!(ancestor instanceof VtlDirectiveHeader)) {
                    return true;
                }
                final VtlLoopVariable loopVariable = ((VtlDirectiveHeader) ancestor).findChildByClass(VtlLoopVariable.class);
                if (loopVariable == null) {
                    return true;
                }
                final Set<String> typeNames = new THashSet<String>();
                for (final String typeName : inferVariableType(loopVariable)) {
                    typeNames.addAll(Arrays.asList(VtlLoopVariable.getVelocityIterables(typeName)));
                }
                if (!myIterableType && typeNames.size() > 0) {
                    myIterableType = true;
                    if(!intersection.isNull()) {
                        intersection.set(null);
                    }
                }

                intersect(typeNames, intersection);
                return true;
            }
        });
        return intersection.isNull() ? Collections.<String>emptySet() : intersection.get();
    }

    public static Set<String> suggestClassesWithMember(final VtlReferenceExpression reference) {
        final PsiFile file = reference.getElement().getContainingFile();
        final String referenceName = reference.getReferenceName();
        final THashSet<String> classes = new THashSet<String>();
        PsiElement parent = reference.getParent();
        if (parent instanceof VtlMethodCallExpression) {
            CreateFromUsageUtils.addClassesWithMember(referenceName, file, classes, true, false);
            CreateFromUsageUtils.addClassesWithMember(referenceName, file, classes, true, true);
            return classes;
        }
        String[] accessors;
        if (parent instanceof VtlAssignment) {
            accessors = VelocityNamingUtil.suggestSetterNames(referenceName);
        } else if (parent instanceof VtlInterpolation) {
            accessors = VelocityNamingUtil.suggestGetterNames(referenceName);
        } else {
            return classes;
        }
        for (String accessor : accessors) {
            CreateFromUsageUtils.addClassesWithMember(accessor, file, classes, true, false);
            CreateFromUsageUtils.addClassesWithMember(accessor, file, classes, true, true);
        }
        return classes;
    }

    private static void intersect(final Set<String> classes, final Ref<Set<String>> to) {
        if (to.isNull()) {
            to.set(classes);
        } else {
            to.get().retainAll(classes);
        }
    }

}
