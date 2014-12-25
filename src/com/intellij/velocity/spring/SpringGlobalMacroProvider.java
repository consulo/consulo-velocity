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

package com.intellij.velocity.spring;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaPackage;
import com.intellij.velocity.VtlGlobalMacroProvider;
import com.intellij.velocity.psi.VtlMacro;
import com.intellij.velocity.psi.files.VtlFile;

/**
 * @author Alexey Chmutov
 */
public class SpringGlobalMacroProvider extends VtlGlobalMacroProvider {
    private static final String SPRING_VM_PACKAGE = "org.springframework.web.servlet.view.velocity";
    private static final String SPRING_VM = "spring.vm";

    @NotNull
    public Collection<VtlMacro> getGlobalMacros(@NotNull final VtlFile file) {
        final PsiJavaPackage aPackage = JavaPsiFacade.getInstance(file.getProject()).findPackage(SPRING_VM_PACKAGE);
        if (aPackage != null) {
            for (final PsiDirectory directory : aPackage.getDirectories()) {
                final PsiFile springVmFile = directory.findFile(SPRING_VM);
                if (springVmFile instanceof VtlFile) {
                    return ((VtlFile) springVmFile).getDefinedMacros();
                }
            }
        }
        return Collections.emptySet();
    }
}
