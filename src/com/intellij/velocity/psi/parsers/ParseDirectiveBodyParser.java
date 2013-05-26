package com.intellij.velocity.psi.parsers;

import static com.intellij.velocity.psi.VtlElementTypes.DIRECTIVE_PARSE;

import com.intellij.lang.PsiBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class ParseDirectiveBodyParser extends CompositeBodyParser {

    public static final ParseDirectiveBodyParser INSTANCE = new ParseDirectiveBodyParser();

    private ParseDirectiveBodyParser() {}

    public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
        
        parseArgumentList(builder, false);
        bodyMarker.done(DIRECTIVE_PARSE);
    }

}