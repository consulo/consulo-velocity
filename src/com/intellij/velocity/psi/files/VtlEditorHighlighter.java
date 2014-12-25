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
package com.intellij.velocity.psi.files;

import static com.intellij.velocity.psi.VtlElementTypes.TEMPLATE_TEXT;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;

/**
 * @author Alexey Chmutov
 */
public class VtlEditorHighlighter extends LayeredLexerEditorHighlighter
{
	public VtlEditorHighlighter(@Nullable final Project project, @Nullable final VirtualFile virtualFile, @NotNull final EditorColorsScheme colors)
	{
		super(new VtlSyntaxHighlighter(), colors);
		final SyntaxHighlighter highlighter = getTemplateDataLanguageHighlighter(project, virtualFile);
		registerLayer(TEMPLATE_TEXT, new LayerDescriptor(new SyntaxHighlighter()
		{
			@Override
			@NotNull
			public Lexer getHighlightingLexer()
			{
				return highlighter.getHighlightingLexer();
			}

			@Override
			@NotNull
			public TextAttributesKey[] getTokenHighlights(final IElementType tokenType)
			{
				return highlighter.getTokenHighlights(tokenType);
			}
		}, ""));
	}

	@NotNull
	private static SyntaxHighlighter getTemplateDataLanguageHighlighter(final Project project, final VirtualFile virtualFile)
	{
		final FileType type = project == null || virtualFile == null ? null : VtlFileViewProvider.getTemplateDataLanguage(virtualFile,
				project).getAssociatedFileType();
		final FileType fileType = type == null ? PlainTextFileType.INSTANCE : type;
		final SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(fileType, project, virtualFile);
		assert highlighter != null;
		return highlighter;
	}

}

