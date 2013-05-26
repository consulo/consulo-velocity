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