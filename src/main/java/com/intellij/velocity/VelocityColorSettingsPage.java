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

package com.intellij.velocity;

import static com.intellij.velocity.psi.files.VtlSyntaxHighlighter.*;

import java.util.Map;

import javax.annotation.Nonnull;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.velocity.psi.files.VtlSyntaxHighlighter;

/**
 * @author Alexey Chmutov
 */
public class VelocityColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRS;

    static {
        ATTRS = new AttributesDescriptor[]{
                createAttributesDescriptor("velocity.color.dot", VELOCITY_DOT),
                createAttributesDescriptor("velocity.color.parenths", VELOCITY_PARENTHS),
                createAttributesDescriptor("velocity.color.brackets", VELOCITY_BRACKETS),
                createAttributesDescriptor("velocity.color.braces", VELOCITY_BRACES),
                createAttributesDescriptor("velocity.color.operation.sign", VELOCITY_OPERATION_SIGN),
                createAttributesDescriptor("velocity.color.string", VELOCITY_STRING),
                createAttributesDescriptor("velocity.color.escape", VELOCITY_ESCAPE),
                createAttributesDescriptor("velocity.color.number", VELOCITY_NUMBER),
                createAttributesDescriptor("velocity.color.keyword", VELOCITY_KEYWORD),
                createAttributesDescriptor("velocity.color.comma", VELOCITY_COMMA),
                createAttributesDescriptor("velocity.color.semicolon", VELOCITY_SEMICOLON),
                createAttributesDescriptor("velocity.color.directive", VELOCITY_DIRECTIVE),
                createAttributesDescriptor("velocity.color.reference", VELOCITY_REFERENCE),
                createAttributesDescriptor("velocity.color.comment", VELOCITY_COMMENT),
                createAttributesDescriptor("velocity.color.bad.character", VELOCITY_BAD_CHARACTER),
                createAttributesDescriptor("velocity.color.scripting.background", VELOCITY_SCRIPTING_BACKGROUND),
        };
    }

    private static AttributesDescriptor createAttributesDescriptor(String displayNameKey, TextAttributesKey textAttributesKey) {
        return new AttributesDescriptor(VelocityBundle.message(displayNameKey), textAttributesKey);
    }

    @Nonnull
    public String getDisplayName() {
        return "Velocity";
    }

    @Nonnull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @Nonnull
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Nonnull
    public SyntaxHighlighter getHighlighter() {
        return new VtlSyntaxHighlighter();
    }

    @Nonnull
    public String getDemoText() {
        return "#* comment  \n" +
                "  comment *#\n" +
                "$reference.method('string').property\n" +
                "## another comment\n" +
                "#foreach($loopvar in [-4..77])\n" +
                "  #set($var = {1:\"double-quoted ${loopvar.toString()}\", 2:false #fff})\n" +
                "#{end}\n\n" +
                "#{macro}(m1 $p1, $p2 $p3 $)\n" +
                "  #if($p1 * ${p2} eq $p3)\n" +
                "#templatetext\n" +
                "  #{else}template text\n" +
                "  #end\n" +
                "#end\n" +
                "#m1(4 5 7)\n" +
                "#{m1}(8, 7, 6)\n";
    }

    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }
}
