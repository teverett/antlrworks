package org.antlr.works.grammar.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.Tool;
import org.antlr.v4.tool.Grammar;
import org.antlr.works.ate.syntax.misc.ATEToken;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngine;
import org.antlr.works.grammar.antlr.ANTLRGrammarEngineImpl;
import org.antlr.works.grammar.antlr.GrammarResult;
import org.antlr.works.grammar.element.ElementAction;
import org.antlr.works.grammar.element.ElementBlock;
import org.antlr.works.grammar.element.ElementGrammarName;
import org.antlr.works.grammar.element.ElementGroup;
import org.antlr.works.grammar.element.ElementImport;
import org.antlr.works.grammar.element.ElementReference;
import org.antlr.works.grammar.element.ElementRule;
import org.antlr.works.grammar.syntax.GrammarSyntaxEngine;

/*

[The "BSD licence"]
Copyright (c) 2005-07 Jean Bovet
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
public class GrammarEngineImpl implements GrammarEngine {
   private GrammarEngineDelegate delegate;
   private GrammarEngine parent;
   private final List<GrammarEngine> importedEngines = new ArrayList<GrammarEngine>();
   private final GrammarProperties properties = new GrammarPropertiesImpl();
   private final ANTLRGrammarEngine antlrEngine = new ANTLRGrammarEngineImpl();
   private final GrammarSyntaxEngine syntaxEngine = new GrammarSyntaxEngine();

   public GrammarEngineImpl(GrammarEngineDelegate delegate) {
      this.delegate = delegate;
      properties.setGrammarEngine(this);
      properties.setSyntaxEngine(syntaxEngine);
      properties.setAntlrEngine(antlrEngine);
      antlrEngine.setGrammarEngine(this);
   }

   @Override
   public void setParent(GrammarEngine parent) {
      // System.out.println(getGrammarName()+" is child of "+parent.getGrammarName());
      this.parent = parent;
   }

   @Override
   public GrammarEngine getParent() {
      return parent;
   }

   @Override
   public GrammarEngine getRootEngine() {
      if (parent == null) {
         return this;
      }
      return parent.getRootEngine();
   }

   @Override
   public void close() {
      antlrEngine.close();
      syntaxEngine.close();
   }

   @Override
   public GrammarProperties getGrammarProperties() {
      return properties;
   }

   @Override
   public ANTLRGrammarEngine getANTLRGrammarEngine() {
      return antlrEngine;
   }

   @Override
   public GrammarSyntaxEngine getSyntaxEngine() {
      return syntaxEngine;
   }

   @Override
   public int getType() {
      return properties.getType();
   }

   @Override
   public ElementGrammarName getElementName() {
      return properties.getElementName();
   }

   @Override
   public String getGrammarName() {
      return properties.getName();
   }

   @Override
   public String getGrammarLanguage() {
      try {
         antlrEngine.createGrammars();
         Grammar g = antlrEngine.getParserGrammar();
         if (g == null) {
            g = antlrEngine.getLexerGrammar();
         }
         if (g != null) {
            return (String) g.getOption("language");
         }
      } catch (Exception e) {
         delegate.reportError(e);
      }
      return null;
   }

   @Override
   public List<ElementRule> getRules() {
      return properties.getRules();
   }

   @Override
   public ElementRule getRuleWithName(String name) {
      return properties.getRuleWithName(name);
   }

   @Override
   public List<ElementRule> getDuplicateRules() {
      return properties.getDuplicateRules();
   }

   @Override
   public ElementRule getRuleAtIndex(int index) {
      return properties.getRuleAtIndex(index);
   }

   @Override
   public List<String> getRuleNames() {
      return properties.getRuleNames();
   }

   @Override
   public List<ElementReference> getReferences() {
      return properties.getReferences();
   }

   @Override
   public List<ElementReference> getUndefinedReferences() {
      return properties.getUndefinedReferences();
   }

   @Override
   public List<ElementImport> getImports() {
      return properties.getImports();
   }

   @Override
   public List<ElementImport> getUndefinedImports() {
      List<ElementImport> undefinedImports = new ArrayList<ElementImport>();
      for (ElementImport i : getImports()) {
         if (!isEngineExisting(i.getName())) {
            undefinedImports.add(i);
         }
      }
      return undefinedImports;
   }

   private boolean isEngineExisting(String grammarName) {
      for (GrammarEngine e : importedEngines) {
         if (e.getGrammarName() == null)
            continue;
         if (e.getGrammarName().equals(grammarName))
            return true;
      }
      return false;
   }

   @Override
   public List<ElementAction> getActions() {
      return properties.getActions();
   }

   @Override
   public List<ElementGroup> getGroups() {
      return properties.getGroups();
   }

   @Override
   public List<ElementBlock> getBlocks() {
      return properties.getBlocks();
   }

   @Override
   public List<ATEToken> getDecls() {
      return properties.getDecls();
   }

   @Override
   public int getNumberOfLines() {
      return syntaxEngine.getMaxLines();
   }

   @Override
   public int getNumberOfRules() {
      return properties.getRules().size();
   }

   @Override
   public int getNumberOfErrors() {
      return properties.getNumberOfErrors();
   }

   @Override
   public String getTokenVocab() {
      return properties.getTokenVocab();
   }

   @Override
   public List<String> getAllGeneratedNames() throws Exception {
      return properties.getAllGeneratedNames();
   }

   /**
    * Returns true if the grammar type needs a suffix for the generated class files. Only combined grammars need a suffix.
    *
    * @return true if the grammar generated files need a suffix
    */
   private boolean hasSuffix() {
      return isCombinedGrammar();
   }

   private String getSuffix(int type) {
      if (hasSuffix()) {
         switch (type) {
            case ElementGrammarName.LEXER:
               return "Lexer";
            case ElementGrammarName.PARSER:
               return "Parser";
         }
      }
      return "";
   }

   @Override
   public String getGeneratedClassName(int type) throws Exception {
      String name = null;
      antlrEngine.createGrammars();
      if (type == ElementGrammarName.LEXER) {
         Grammar g = antlrEngine.getLexerGrammar();
         if (g == null)
            return null;
         name = g.name + getSuffix(type);
      } else if (type == ElementGrammarName.PARSER) {
         Grammar g = antlrEngine.getParserGrammar();
         if (g == null)
            return null;
         name = g.name + getSuffix(type);
      } else if (type == ElementGrammarName.TREEPARSER) {
         Grammar g = antlrEngine.getParserGrammar();
         if (g == null)
            return null;
         if (!isTreeParserGrammar())
            return null;
         name = g.name + getSuffix(type);
      }
      return name;
   }

   @Override
   public int getFirstDeclarationPosition(String name) {
      return properties.getFirstDeclarationPosition(name);
   }

   /**
    * Returns the list of grammars that overrides the rule specified in parameter. Overrides has the same meaning than in Java: the rule of a parent grammar is declared again in one or more child
    * grammar.
    */
   @Override
   public List<String> getGrammarsOverriddenByRule(String name) {
      List<String> grammars = new ArrayList<String>();
      for (GrammarEngine child : importedEngines) {
         for (ATEToken decl : child.getDecls()) {
            if (decl.getAttribute().equals(name)) {
               grammars.add(child.getGrammarName());
               break;
            }
         }
         grammars.addAll(child.getGrammarsOverriddenByRule(name));
      }
      return grammars;
   }

   /**
    * Returns the list of grammars that this rule overrides.
    */
   @Override
   public List<String> getGrammarsOverridingRule(String name) {
      List<String> grammars = new ArrayList<String>();
      if (parent != null) {
         for (ATEToken decl : parent.getDecls()) {
            if (decl.getAttribute().equals(name)) {
               grammars.add(parent.getGrammarName());
               break;
            }
         }
         grammars.addAll(parent.getGrammarsOverridingRule(name));
      }
      return grammars;
   }

   @Override
   public List<ATEToken> getTokens() {
      return syntaxEngine.getTokens();
   }

   @Override
   public void updateHierarchy(Map<String, GrammarEngine> engines, Set<GrammarEngine> alreadyVisitedEngines) {
      importedEngines.clear();
      // traverse all the imports for this grammar
      for (ElementImport element : properties.getImports()) {
         GrammarEngine d = engines.get(element.getName());
         if (d == null)
            continue;
         if (alreadyVisitedEngines.contains(d))
            continue;
         // add the engine that is visited
         alreadyVisitedEngines.add(d);
         if (parent != d) {
            d.setParent(this);
         }
         importedEngines.add(d);
         d.updateHierarchy(engines, alreadyVisitedEngines);
         // remove the engine that was visited - so each branch
         // of the tree is checked separately
         alreadyVisitedEngines.remove(d);
      }
      resetRules();
   }

   @Override
   public GrammarResult analyze() throws Exception {
      return antlrEngine.analyze();
   }

   @Override
   public void cancelAnalyze() {
      antlrEngine.cancel();
   }

   @Override
   public void computeRuleErrors(ElementRule rule) {
      antlrEngine.computeRuleErrors(rule);
   }

   @Override
   public void parserCompleted() {
      properties.parserCompleted();
   }

   @Override
   public void updateAll() {
      properties.updateAll();
   }

   @Override
   public void markDirty() {
      antlrEngine.markDirty();
      if (parent != null) {
         parent.markDirty();
      }
   }

   @Override
   public void reset() {
      properties.reset();
   }

   @Override
   public boolean isCombinedGrammar() {
      return properties.isCombinedGrammar();
   }

   @Override
   public boolean isTreeParserGrammar() {
      return properties.isTreeParserGrammar();
   }

   @Override
   public void antlrGrammarEngineAnalyzeCompleted() {
      delegate.engineAnalyzeCompleted();
   }

   @Override
   public String getGrammarFileName() {
      return delegate.getGrammarFileName();
   }

   @Override
   public String getGrammarText() {
      return delegate.getGrammarText();
   }

   @Override
   public String getTokenVocabFile(String name) {
      return delegate.getTokenVocabFile(name);
   }

   @Override
   public Tool getANTLRTool() {
      return delegate.getANTLRTool();
   }

   @Override
   public void reportError(String error) {
      delegate.reportError(error);
   }

   @Override
   public void gotoToRule(String grammar, String name) {
      delegate.gotoToRule(grammar, name);
   }

   private void resetRules() {
      for (ElementRule r : properties.getRules()) {
         r.resetHierarchy();
      }
   }
}
