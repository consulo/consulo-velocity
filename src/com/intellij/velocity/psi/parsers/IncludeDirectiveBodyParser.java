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

package com.intellij.velocity.psi.parsers;

import static com.intellij.velocity.psi.VtlElementTypes.DIRECTIVE_INCLUDE;

import com.intellij.lang.PsiBuilder;
                        
/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class IncludeDirectiveBodyParser extends CompositeBodyParser {

    public static final IncludeDirectiveBodyParser INSTANCE = new IncludeDirectiveBodyParser();

    private IncludeDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        parseArgumentList(builder, false);
        bodyMarker.done(DIRECTIVE_INCLUDE);
    }

}
