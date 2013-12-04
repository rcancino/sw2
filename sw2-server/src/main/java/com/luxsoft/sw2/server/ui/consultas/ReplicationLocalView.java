package com.luxsoft.sw2.server.ui.consultas;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.StringViewMap;

import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.utils.DockingUtils;



public class ReplicationLocalView extends AbstractView {
	
	private RootWindow rootWindow;
	private StringViewMap viewMap;
	
	
	

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
		
		rootNode=new DefaultMutableTreeNode("JMS",true);
		
		load();
		treeModel=new DefaultTreeModel(rootNode,true);
		tree=new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addMouseListener(new SelectionAdapter());
		//tree.setCellRenderer(new ReplicadorRenderer());
		JScrollPane scroll=new JScrollPane(tree);
		panel.add(scroll,BorderLayout.CENTER);
		return panel;
	}
	
	private JComponent buildReportPanel(){
		
		viewMap=new StringViewMap();
		rootWindow=new RootWindow(viewMap);
		DockingUtils.configRootWindow(rootWindow);
		DockingUtils.configTabWindowProperties(rootWindow);
		return rootWindow;
		
	}
	
	/**
	 * Cerramos todas las posibles vistas de reportes
	 * 
	 */
	public void close(){
		if(viewMap.getViewCount()>0){
			for(int index=0;index<viewMap.getViewCount();index++){
				View v=viewMap.getViewAtIndex(index);
				v.close();
				rootWindow.removeView(v);
			}
		}
	}
	   
	
	/**
	 * Cargamos el arbol de reportes
	 * 
	 */
	public void open(){
		load();
	}
	
	
	private void load(){
		DefaultMutableTreeNode brokersNode=new DefaultMutableTreeNode("Brokers ",true);
		DefaultMutableTreeNode exportacionNode=new DefaultMutableTreeNode("Topicos ",true);
		rootNode.add(brokersNode);
		rootNode.add(exportacionNode);
		/*
		for(String[] row:LocalServerManager.getInstance().getBrokers()){
			DefaultMutableTreeNode broker=new DefaultMutableTreeNode(row[1],false);
			
			brokersNode.add(broker);
			
		}
		*/
	}
	
	private void mostrarReplicador(){
		Object obj=tree.getLastSelectedPathComponent();
		if(obj==null) return;
		DefaultMutableTreeNode node=(DefaultMutableTreeNode)obj;
		if(node.isLeaf() ){
			//AbstractEnterpriseReplicator replicador=(AbstractEnterpriseReplicator)node.getUserObject();
			/*
			View view=this.viewMap.getView(replicador.getLabel());
			if(view==null){
				view=new View(replicador.getLabel(), null, new ReplicadorPanel(replicador));
				this.viewMap.addView(replicador.getLabel(), view);				
			}
			DockingUtil.addWindow(view, rootWindow);
			*/
		}		
	}
	
	private class SelectionAdapter extends MouseAdapter{
		
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2){
				mostrarReplicador();
			}
		}
	}
	
	/*
	private class ReplicadorRenderer extends DefaultTreeCellRenderer{

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
			if(node.getUserObject() instanceof AbstractEnterpriseReplicator){
				
				AbstractEnterpriseReplicator f=(AbstractEnterpriseReplicator)node.getUserObject();				
				setText(f.getLabel());
				
			}
			return this;
		}		
	}*/

}
