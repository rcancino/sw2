package com.luxsoft.siipap.swing.reports;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.ViewMap;

import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.utils.DockingUtils;

public class ReportView2 extends AbstractView{
	
	private RootWindow rootWindow;
	private ViewMap viewMap;

	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		sp.setLeftComponent(buildReportTree());
		sp.setRightComponent(buildReportPanel());
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.20);
		return sp;
	}
	
	private JTree tree;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode;
	
	
	private JComponent buildReportTree(){
		JPanel panel=new JPanel(new BorderLayout());
		
		rootNode=new DefaultMutableTreeNode("Reportes",true);
		load();
		treeModel=new DefaultTreeModel(rootNode,true);
		tree=new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new SelectionAdapter());
		tree.setCellRenderer(new ActionRenderer());
		JScrollPane scroll=new JScrollPane(tree);
		panel.add(scroll,BorderLayout.CENTER);
		return panel;
	}
	
	private JComponent buildReportPanel(){
		viewMap=new ViewMap();
		rootWindow=new RootWindow(viewMap);
		DockingUtils.configRootWindow(rootWindow);
		DockingUtils.configTabWindowProperties(rootWindow);
		return rootWindow;
	}
	
	private void load(){
		buildTree(rootNode);
	}
	
	protected void buildTree(DefaultMutableTreeNode root){
		
	}
	
	/**
	 * Cerramos todas las posibles vistas de reportes
	 * 
	 */
	public void close(){
		if(viewMap.getViewCount()>0){
			for(int index=0;index<=viewMap.getViewCount();index++){
				View v=viewMap.getViewAtIndex(index);
				v.close();
				rootWindow.removeView(v);
			}
		}
	}
	
	protected DefaultMutableTreeNode buildActionNode(String label,String methodName){
		DispatchingAction action=new DispatchingAction(this, methodName);
		action.putValue(Action.SHORT_DESCRIPTION, label);
		DefaultMutableTreeNode node=new DefaultMutableTreeNode(action,false);
		return node;
	}
	   
	
	/**
	 * Cargamos el arbol de reportes
	 * 
	 */
	public void open(){
		//load();
	}
	
	/**
	 * Ejecuta el reporte de la seleccion actual
	 * 
	 * 
	 */
	private void ejecutarReporte(){
		Object obj=tree.getLastSelectedPathComponent();
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)obj;
		if(node.getUserObject() instanceof AbstractAction){
			Action action=(Action)node.getUserObject();
			action.actionPerformed(null);
		}
	}
	
	private class SelectionAdapter extends MouseAdapter{
		
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2){
				ejecutarReporte();
			}
		}
	}
	
	
	
	
	private class ActionRenderer extends DefaultTreeCellRenderer{

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) 
		{
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
			if(node.getUserObject() instanceof AbstractAction){
				AbstractAction a=(AbstractAction)node.getUserObject();
				setText(a.getValue(AbstractAction.SHORT_DESCRIPTION).toString());
				
			}
			return this;
		}
		
	}

}
