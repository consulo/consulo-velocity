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

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.intellij.velocity.VtlIcons;
import com.intellij.velocity.VtlReferenceContributor;
import com.intellij.velocity.psi.PsiUtil;
import com.intellij.velocity.psi.VtlCompositeElementTypes;
import com.intellij.velocity.psi.VtlImplicitVariable;
import com.intellij.velocity.psi.VtlLanguage;
import com.intellij.velocity.psi.directives.VtlDirective;
import com.intellij.velocity.psi.directives.VtlSet;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.reference.VtlReferenceExpression;
import consulo.codeInsight.completion.CompletionProvider;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Alexey Chmutov
 */
public class VtlCompletionContributor extends CompletionContributor
{
	private static final Key<VtlDirective> DIRECTIVE_KEY = Key.create("VtlDirectiveKey");

	public VtlCompletionContributor()
	{
		registerDirectiveNameCompletionProvider();
		registerInferredClassNameCompletionProvider();
		registerWritablePropertyNameCompletionProvider();
	}

	@Override
	public void beforeCompletion(@Nonnull CompletionInitializationContext context)
	{
		int offset = context.getStartOffset();
		if(offset == 0 || !context.getFile().getViewProvider().getLanguages().contains(VtlLanguage.INSTANCE))
		{
			return;
		}
		final CharSequence chars = context.getEditor().getDocument().getCharsSequence();
		char currChar;
		do
		{
			currChar = chars.charAt(--offset);
		}
		while(offset > 0 && (Character.isLetterOrDigit(currChar) || currChar == '-' || currChar == '_'));
		String dummyId;
		if(currChar == '#')
		{
			dummyId = "dummyId()";
		}
		else if(currChar == '{' && offset > 0 && chars.charAt(offset - 1) == '#')
		{
			dummyId = "dummyId}()";
		}
		else
		{
			boolean upperCase = offset + 1 < chars.length() && Character.isUpperCase(chars.charAt(offset + 1));
			dummyId = upperCase ? "DummyId" : "dummyId";
		}
		context.setDummyIdentifier(dummyId);
	}

	private void registerDirectiveNameCompletionProvider()
	{
		final PsiElementPattern.Capture<PsiElement> directiveStartingSharp = psiElement().withText("#").withParent(psiElement(VtlDirective.class)
				.save(DIRECTIVE_KEY));
		final ElementPattern<PsiElement> sharpPattern = psiElement().afterLeaf(directiveStartingSharp);
		final ElementPattern<PsiElement> sharpBracePattern = psiElement().afterLeaf(psiElement().withText("{").afterLeaf(directiveStartingSharp));

		extend(CompletionType.BASIC, sharpPattern, new DirectiveNameCompletionProvider(false));
		extend(CompletionType.BASIC, sharpBracePattern, new DirectiveNameCompletionProvider(true));
	}

	private static class DirectiveNameCompletionProvider implements CompletionProvider
	{
		private final boolean myClosingBraceNeeded;

		public DirectiveNameCompletionProvider(boolean closingBraceNeeded)
		{
			super();
			myClosingBraceNeeded = closingBraceNeeded;
		}

		@Override
		public void addCompletions(
				@Nonnull final CompletionParameters parameters,
				final ProcessingContext context,
				@Nonnull final CompletionResultSet result)
		{
			result.stopHere();
			final VtlDirective directive = context.get(DIRECTIVE_KEY);
			for(final String name : VtlDirective.DIRECTIVE_NAMES)
			{
				if(!VtlDirective.Validator.isAllowed(directive, name))
				{
					continue;
				}
				LookupElement element = LookupElementBuilder.create(name).withIcon(VtlIcons.SHARP_ICON).withBoldness(true);
				result.addElement(TailTypeDecorator.withTail(element, new VtlTailType(myClosingBraceNeeded)
				{
					@Override
					protected boolean openingParenNeeded()
					{
						return VtlDirective.Validator.areParenthesesNeeded(directive, name);
					}
				}));
			}
			PsiElement positionParent = parameters.getPosition().getParent();
			if(positionParent instanceof VtlReferenceExpression)
			{
				for(Object variant : ((VtlReferenceExpression) positionParent).getVariants())
				{
					assert variant instanceof LookupElement;
					result.addElement((LookupElement) variant);
				}
			}
		}
	}

	private void registerInferredClassNameCompletionProvider()
	{
		extend(CompletionType.SMART, VtlReferenceContributor.VTLVARIABLE_COMMENT, new CompletionProvider()
		{
			@Override
			public void addCompletions(
					@Nonnull final CompletionParameters parameters,
					final ProcessingContext context,
					@Nonnull final CompletionResultSet _result)
			{
				final PsiComment element = (PsiComment) parameters.getPosition();
				final String text = element.getText();
				final TextRange typeNameRange = VtlReferenceContributor.findTypeNameRange(text);
				final int offset = parameters.getOffset() - element.getTextRange().getStartOffset();
				if(typeNameRange != null && typeNameRange.contains(offset))
				{
					final CompletionResultSet result = _result.withPrefixMatcher(text.substring(typeNameRange.getStartOffset(), offset));

					final VtlFile originalFile = (VtlFile) parameters.getOriginalFile();
					VtlImplicitVariable originalVariable = ApplicationManager.getApplication().runReadAction(new Computable<VtlImplicitVariable>()
					{
						@Override
						public VtlImplicitVariable compute()
						{
							String[] varNameAndTypeAndScopeFilePath = VtlFile.findVariableNameAndTypeAndScopeFilePath(text);
							return varNameAndTypeAndScopeFilePath == null ? null : originalFile.findImplicitVariable
									(varNameAndTypeAndScopeFilePath[0]);
						}
					});
					assert originalVariable != null;

					Collection<String> types = TypeInferenceUtil.inferVariableType(originalVariable);
					addInferredTypeItems(element, result, types);
				}
			}

			private void addInferredTypeItems(final PsiComment element, final CompletionResultSet result, final Collection<String> typeNames)
			{
				for(final String typeName : typeNames)
				{
					final LookupElement lookupElement = PsiUtil.createPsiTypeLookupElement(element, typeName);
					if(lookupElement == null)
					{
						continue;
					}
					ApplicationManager.getApplication().runReadAction(new Runnable()
					{
						@Override
						public void run()
						{
							result.addElement(lookupElement);
						}
					});
				}
			}
		});
	}

	private void registerWritablePropertyNameCompletionProvider()
	{
		final PsiElementPattern.Capture<PsiElement> propertyToBeSet = psiElement().withParent(psiElement(VtlCompositeElementTypes
				.REFERENCE_EXPRESSION).withParent(VtlSet.class));

		extend(CompletionType.SMART, propertyToBeSet, new CompletionProvider()
		{
			@Override
			public void addCompletions(@Nonnull CompletionParameters parameters, ProcessingContext context, @Nonnull CompletionResultSet result)
			{
				result.stopHere();
				PsiElement positionParent = parameters.getPosition().getParent();
				if(positionParent instanceof VtlReferenceExpression)
				{
					for(Object variant : ((VtlReferenceExpression) positionParent).getVariants(true))
					{
						assert variant instanceof LookupElement;
						result.addElement((LookupElement) variant);
					}
				}
			}
		});
	}


}
