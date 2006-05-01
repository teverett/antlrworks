package org.antlr.works.debugger.parsetree;

import edu.usfca.xj.appkit.gview.GView;
import org.antlr.runtime.Token;
import org.antlr.works.awtree.AWTreePanel;
import org.antlr.works.awtree.AWTreePanelDelegate;
import org.antlr.works.debugger.Debugger;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
/*

[The "BSD licence"]
Copyright (c) 2005-2006 Jean Bovet
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

public class DBParseTreePanel extends JPanel implements DBParseTreeModelListener, AWTreePanelDelegate {

    protected Debugger debugger;
    protected DBParseTreeModel model;
    protected AWTreePanel treePanel;

    public DBParseTreePanel(Debugger debugger) {
        super(new BorderLayout());

        this.debugger = debugger;

        treePanel = new AWTreePanel(new DefaultTreeModel(null));
        treePanel.setDelegate(this);

        add(treePanel, BorderLayout.CENTER);
    }

    public void setModel(DBParseTreeModel model) {
        this.model = model;
        this.model.addListener(this);
    }

    public void clear() {
        model.clear();
    }

    public void selectToken(Token token) {
        DBParseTreeModel.ParseTreeNode root = (DBParseTreeModel.ParseTreeNode) treePanel.getRoot();
        DBParseTreeModel.ParseTreeNode node = root.findNodeWithToken(token);
        if(node != null)
            treePanel.selectNode(node);
    }

    public void updateParseTree(TreeNode selectNode) {
        treePanel.refresh();
        treePanel.scrollNodeToVisible(selectNode);
    }

    public GView getGraphView() {
        return treePanel.getGraphView();
    }

    public void modelChanged(DBParseTreeModel model, TreeNode newNode) {
        TreeNode node = model.peekRule();
        treePanel.setRoot(node);
        updateParseTree(newNode);
    }

    public void modelUpdated(DBParseTreeModel model, TreeNode node) {
        updateParseTree(node);
    }

    public void awTreeDidSelectTreeNode(TreeNode node) {
        DBParseTreeModel.ParseTreeNode n = (DBParseTreeModel.ParseTreeNode) node;
        debugger.selectToken(n.token, n.line, n.pos);
    }

    public JPopupMenu awTreeGetContextualMenu() {
        return debugger.treeGetContextualMenu();
    }

}
