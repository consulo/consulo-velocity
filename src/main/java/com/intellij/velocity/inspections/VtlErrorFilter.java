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

package com.intellij.velocity.inspections;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.files.VtlFileViewProvider;

/**
 * @author Alexey Chmutov
 */
public class VtlErrorFilter extends HighlightErrorFilter {

  public boolean shouldHighlightErrorElement(@Nonnull final PsiErrorElement element) {
    return !value(element);
  }

  public static boolean value(final PsiErrorElement psiErrorElement) {
    final PsiFile file = psiErrorElement.getContainingFile();
    return file.getViewProvider() instanceof VtlFileViewProvider && !(file instanceof VtlFile);
  }
}
