package org.antlr.works.grammar.engine;

import java.io.IOException;
/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.antlr.v4.tool.Grammar;
import org.antlr.works.ate.syntax.generic.ATESyntaxLexer;
import org.antlr.works.ate.syntax.generic.ATESyntaxParser;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.element.ElementAction;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementGroup;
import org.antlr.works.grammar.element.ElementImport;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;
import org.antlr.works.grammar.syntax.GrammarSyntaxLexer;
import org.antlr.works.grammar.syntax.GrammarSyntaxParser;
import org.antlr.xjlib.foundation.XJUtils;

public class GrammarPropertiesImpl implements GrammarProperties {
   private ElementGrammarName name;
   private final List<ElementRule> rules = new ArrayList<ElementRule>();
   private final List<ElementGroup> groups = new ArrayList<ElementGroup>();
   private final List<ElementBlock> blocks = new ArrayList<ElementBlock>();
   private final List<ElementAction> actions = new ArrayList<ElementAction>();
   private final List<ElementReference> references = new ArrayList<ElementReference>();
   private final List<ElementImport> imports = new ArrayList<ElementImport>();
   private final List<ATEToken> decls = new ArrayList<ATEToken>();
   private final List<ElementRule> duplicateRules = new ArrayList<ElementRule>();
   private final List<ElementReference> undefinedReferences = new ArrayList<ElementReference>();
   private final Set<String> tokenVocabNames = new HashSet<String>();
   private String tokenVocabName;
   private GrammarEngine engine;
   private GrammarSyntaxEngine syntaxEngine;
   private ANTLRGrammarEngine antlrEngine;

   public GrammarPropertiesImpl() {
   }

   @Override
   public void setGrammarEngine(GrammarEngine engine) {
      this.engine = engine;
   }

   @Override
   public void setSyntaxEngine(GrammarSyntaxEngine syntaxEngine) {
      this.syntaxEngine = syntaxEngine;
   }

   @Override
   public void setAntlrEngine(ANTLRGrammarEngine antlrEngine) {
      this.antlrEngine = antlrEngine;
   }

   @Override
   public List<ElementRule> getRules() {
      return rules;
   }

   @Override
   public ElementRule getRuleWithName(String name) {
      List<ElementRule> rules = getRules();
      for (ElementRule r : rules) {
         if (r.name.equals(name))
            return r;
      }
      return null;
   }

   @Override
   public List<ElementGroup> getGroups() {
      return groups;
   }

   @Override
   public List<ElementBlock> getBlocks() {
      return blocks;
   }

   @Override
   public List<ElementAction> getActions() {
      return actions;
   }

   @Override
   public List<ElementReference> getReferences() {
      return references;
   }

   @Override
   public List<ElementImport> getImports() {
      return imports;
   }

   @Override
   public List<ATEToken> getDecls() {
      return decls;
   }

   @Override
   public ElementGrammarName getElementName() {
      return name;
   }

   @Override
   public String getName() {
      if (name == null) {
         return null;
      } else {
         return name.getName();
      }
   }

   @Override
   public int getType() {
      if (name == null) {
         return -1;
      } else {
         return name.getType();
      }
   }

   @Override
   public boolean isParserGrammar() {
      return getType() == ElementGrammarName.PARSER;
   }

   @Override
   public boolean isLexerGrammar() {
      return getType() == ElementGrammarName.LEXER;
   }

   @Override
   public boolean isCombinedGrammar() {
      return getType() == ElementGrammarName.COMBINED;
   }

   @Override
   public boolean isTreeParserGrammar() {
      return getType() == ElementGrammarName.TREEPARSER;
   }

   private List<String> getDeclaredTokenNames() {
      List<String> names = new ArrayList<String>();
      if (blocks != null) {
         for (ElementBlock block : blocks) {
            if (block.isTokenBlock) {
               names.addAll(block.getDeclaredTokensAsString());
            }
         }
      }
      return names;
   }

   private List<String> getPredefinedReferences() {
      return GrammarSyntaxParser.predefinedReferences;
   }

   @Override
   public synchronized String getTokenVocab() {
      if (blocks == null)
         return null;
      for (ElementBlock block : blocks) {
         if (block.isOptionsBlock)
            return block.getTokenVocab();
      }
      return null;
   }

   @Override
   public synchronized List<String> getRuleNames() {
      List<String> names = new ArrayList<String>();
      if (rules != null) {
         for (ElementRule rule : rules) {
            names.add(rule.name);
         }
      }
      return names;
   }

   @Override
   public synchronized ElementRule getRuleAtIndex(int index) {
      if (index < 0 || index >= rules.size())
         return null;
      else
         return rules.get(index);
   }

   @Override
   public int getNumberOfRulesWithErrors() {
      int count = 0;
      if (getRules() != null) {
         for (ElementRule rule : getRules()) {
            if (rule.hasErrors())
               count++;
         }
      }
      return count;
   }

   @Override
   public int getNumberOfErrors() {
      int count = 0;
      if (getRules() != null) {
         for (ElementRule rule : getRules()) {
            if (rule.hasErrors())
               count += rule.getErrors().size();
         }
      }
      return count;
   }

   @Override
   public List<ElementRule> getDuplicateRules() {
      // Return a new array to prevent concurrent modification
      return new ArrayList<ElementRule>(duplicateRules);
   }

   @Override
   public List<ElementReference> getUndefinedReferences() {
      // Return a new array to prevent concurrent modification
      return new ArrayList<ElementReference>(undefinedReferences);
   }

   @Override
   public void reset() {
      resetTokenVocab();
   }

   private void resetTokenVocab() {
      tokenVocabName = null;
      tokenVocabNames.clear();
   }

   private Set<String> getTokenVocabNames() {
      String tokenVocab = getTokenVocab();
      if (tokenVocab == null) {
         tokenVocabName = null;
         tokenVocabNames.clear();
         return tokenVocabNames;
      }
      if (tokenVocabName != null && tokenVocabName.equals(tokenVocab))
         return tokenVocabNames;
      tokenVocabName = tokenVocab;
      tokenVocabNames.clear();
      try {
         String file = engine.getTokenVocabFile(tokenVocabName + ".tokens");
         if (file != null) {
            readTokenVocabFromFile(file, tokenVocabNames);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return tokenVocabNames;
   }

   public static boolean readTokenVocabFromFile(String filePath, Set<String> tokenNames) throws IOException {
      // Read the tokens from the file if it exists
      List<ATEToken> tokens = parsePropertiesString(XJUtils.getStringFromFile(filePath));
      // Add each token name to the list of tokenVocabNames
      for (ATEToken t : tokens) {
         tokenNames.add(t.getAttribute());
      }
      return true;
   }

   private static List<ATEToken> parsePropertiesString(final String content) {
      class ParseProperties extends ATESyntaxParser {
         public List<ATEToken> propertiesTokens;

         @Override
         public void parseTokens() {
            propertiesTokens = new ArrayList<ATEToken>();
            while (nextToken()) {
               if (T(0).type == ATESyntaxLexer.TOKEN_ID) {
                  if (isChar(1, "=") || isChar(1, "\n"))
                     propertiesTokens.add(T(0));
               }
            }
         }
      }
      GrammarSyntaxLexer lexer = new GrammarSyntaxLexer();
      lexer.tokenize(content);
      ParseProperties parser = new ParseProperties();
      parser.parse(lexer.getTokens());
      return parser.propertiesTokens;
   }

   private void rebuildHasLeftRecursionRulesList() {
      if (getRules() == null)
         return;
      for (ElementRule r : getRules()) {
         // hasLeftRecursion has a side-effect to analyze the rule
         r.hasLeftRecursion();
      }
   }

   private void rebuildDuplicateRulesList() {
      List<ElementRule> rules = getRules();
      if (rules == null)
         return;
      List<ElementRule> sortedRules = Collections.list(Collections.enumeration(rules));
      Collections.sort(sortedRules);
      Iterator<ElementRule> iter = sortedRules.iterator();
      ElementRule currentRule = null;
      duplicateRules.clear();
      while (iter.hasNext()) {
         ElementRule nextRule = iter.next();
         if (currentRule != null && currentRule.name.equals(nextRule.name) && !duplicateRules.contains(currentRule)) {
            duplicateRules.add(currentRule);
            duplicateRules.add(nextRule);
         }
         currentRule = nextRule;
      }
   }

   private void rebuildUndefinedReferencesList() {
      List<String> existingReferences = getRuleNames();
      existingReferences.addAll(getDeclaredTokenNames());
      existingReferences.addAll(getPredefinedReferences());
      Set<String> tokenVocabNames = getTokenVocabNames();
      existingReferences.addAll(tokenVocabNames);
      syntaxEngine.resolveReferencesWithExternalNames(tokenVocabNames);
      undefinedReferences.clear();
      List<ElementReference> references = getReferences();
      if (references == null)
         return;
      for (ElementReference ref : references) {
         if (existingReferences.contains(ref.token.getAttribute()))
            continue;
         if (!engine.getGrammarsOverriddenByRule(ref.token.getAttribute()).isEmpty())
            continue;
         // also check from the root grammar
         if (!engine.getRootEngine().getGrammarsOverriddenByRule(ref.token.getAttribute()).isEmpty())
            continue;
         undefinedReferences.add(ref);
      }
   }

   @Override
   public void updateAll() {
      rebuildDuplicateRulesList();
      rebuildUndefinedReferencesList();
      rebuildHasLeftRecursionRulesList();
   }

   @Override
   public void parserCompleted() {
      update((GrammarSyntaxParser) syntaxEngine.getParser());
      resetTokenVocab();
   }

   private void update(GrammarSyntaxParser parser) {
      rules.clear();
      rules.addAll(parser.rules);
      groups.clear();
      groups.addAll(parser.groups);
      blocks.clear();
      blocks.addAll(parser.blocks);
      actions.clear();
      actions.addAll(parser.actions);
      references.clear();
      references.addAll(parser.references);
      imports.clear();
      imports.addAll(parser.imports);
      decls.clear();
      decls.addAll(parser.decls);
      this.name = parser.getName();
      for (ElementRule r : rules) {
         r.setEngine(engine);
      }
   }

   @Override
   public List<String> getAllGeneratedNames() throws Exception {
      List<String> names = new ArrayList<String>();
      Grammar g = antlrEngine.getDefaultGrammar();
      if (g != null) {
         names.add(g.getRecognizerName());
         for (Grammar gd : g.getDelegates()) {
            names.add(gd.getRecognizerName());
         }
      }
      Grammar lexer = antlrEngine.getLexerGrammar();
      if (lexer != null) {
         names.add(lexer.getRecognizerName());
         for (Grammar gd : lexer.getDelegates()) {
            names.add(gd.getRecognizerName());
         }
      }
      return names;
   }

   @Override
   public int getFirstDeclarationPosition(String name) {
      ATEToken token = getFirstDeclaration(name);
      if (token != null) {
         return token.start;
      } else {
         return -1;
      }
   }

   private ATEToken getFirstDeclaration(String name) {
      for (ATEToken decl : getDecls()) {
         if (decl.getAttribute().equals(name)) {
            return decl;
         }
      }
      return null;
   }
}