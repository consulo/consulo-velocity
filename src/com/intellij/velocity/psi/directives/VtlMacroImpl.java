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
package com.intellij.velocity.psi.directives;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.velocity.VtlIcons;
import com.intellij.velocity.VelocityBundle;
import com.intellij.velocity.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Alexey Chmutov
 */
public class VtlMacroImpl extends VtlPresentableNamedElement implements VtlDirective, VtlMacro {

    public VtlMacroImpl(@NotNull final ASTNode node) {
        super(node);
    }

    public boolean processDeclarations(@NotNull final PsiScopeProcessor processor, @NotNull final ResolveState state, final PsiElement lastParent,
                                       @NotNull final PsiElement place) {
        if (!super.processDeclarations(processor, state, lastParent, place)) {
            return false;
        }
        for (final VtlVariable declaration : getParameters()) {
            if (!processor.execute(declaration, state)) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    protected PsiElement getNameElement() {
        return findHeaderOfDirective().findChildByType(VtlElementTypes.IDENTIFIER);
    }

    @Nullable
    public TextRange getNameElementRange() {
        PsiElement nameElement = getNameElement();
        return nameElement == null ? null : nameElement.getTextRange();
    }

    @NotNull
    public String getPresentableName() {
        return "macro '" + getName() + "'";
    }

    @NotNull
    public VtlParameterDeclaration[] getParameters() {
        return findHeaderOfDirective().findChildrenByClass(VtlParameterDeclaration.class);
    }

    public boolean isDeprecated() {
        return false;
    }

    public int getFoldingStartOffset() {
        return getNode().getTextRange().getStartOffset() + "#macro".length();
    }

    public int getFoldingEndOffset() {
        return getNode().getTextRange().getEndOffset() - "#end".length();
    }

    public boolean needsClosing() {
        return true;
    }

    public String getTypeName() {
        return VelocityBundle.message("type.name.macro");
    }

    public Icon getIcon() {
        return VtlIcons.SHARP_ICON;
    }
}