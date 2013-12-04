package com.luxsoft.siipap.swing.reports;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.ViewMap;

import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.DockingUtils;

public class ReportView extends AbstractView{
	
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
		tree.setCellRenderer(new FileRenderer());
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
		String dirPath="Z:\\Reportes_MySQL\\";
		File dir=new File(dirPath);
		Assert.isTrue(dir.isDirectory(),"No encontro el directorio: "+dirPath);
		visitDir(dir,rootNode);
	}
	
	private void visitDir(File dir,DefaultMutableTreeNode parent){
		File[] files=dir.listFiles();
		for(int i=0;i<files.length;i++){
			File f=files[i];
			if(f.isDirectory()){
				DefaultMutableTreeNode subDir=new DefaultMutableTreeNode(f,true);
				parent.add(subDir);
				visitDir(f, subDir);
			}else{
				if(parent==rootNode)
					continue;
				
				if(f.getName().endsWith(".jasper")){
					DefaultMutableTreeNode child=new DefaultMutableTreeNode(f,false);				
					parent.add(child);
				}
				
			}
		}
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
		if(node.getUserObject() instanceof File){
			File f=(File)node.getUserObject();
			if(f.isFile()){
				logger.info("Ejecutando reporte: "+f.getAbsolutePath());
				String forma=toSimpleString(f)+".class";
				logger.info("Forma a ejecutar  "+ forma );
				//DefaultResourceLoader loader=new DefaultResourceLoader();
				PathMatchingResourcePatternResolver loader=new PathMatchingResourcePatternResolver();
				try {
					Resource[] resources = loader.getResources("classpath*:/com/**/"+forma);
					logger.info("Recurso para ejecutar el reporte "+ resources);
					if(resources.length==1)
						System.out.println("Recurso encontrado: "+resources[0]);
					if(resources.length>1){
						logger.info("Existe mas de una forma con el nombre: "+forma+ "utilizando: "+resources[0].getFile().getAbsolutePath());
					}
					
					if(resources.length>=1){
						Resource res=resources[0];
						if(res.exists()){
							//ClassLoader cl=Thread.currentThread().getContextClassLoader();
							//
							URL url=res.getURL();
							String formClassName=url.getPath();
							if(ResourceUtils.isJarURL(url)){
								formClassName=StringUtils.substringAfter(formClassName, "!");
								formClassName=StringUtils.substringAfter(formClassName, "/");
								formClassName=StringUtils.substringBefore(formClassName, ".class");
								formClassName=ClassUtils.convertResourcePathToClassName(formClassName);
								System.out.println("\n\nEncontre la forma: "+formClassName);
							}else{
								formClassName=res.getFile().getAbsolutePath();
								formClassName=formClassName.replace('\\', '.');
								int index=formClassName.indexOf(".com");
								int lastIndex=formClassName.lastIndexOf(".class");
								formClassName=formClassName.substring(index+1, lastIndex);
								System.out.println("Encontre la forma: "+formClassName);
							}
							try {
								Class formClass=ClassUtils.forName(formClassName);
								ReportForm dialog=(ReportForm)formClass.newInstance();
								dialog.open();
								if(!dialog.hasBeenCanceled()){
									Map params=dialog.getParametros();
								}
							} catch (Exception  e) {
								e.printStackTrace();
							}
							
						}else{
							System.out.println("No existe una forma apropiada para:"+forma);
						}
					}
					
				} catch (IOException e1) {					
					e1.printStackTrace();
				}
				
				
			}
		}
	}
	
	private class SelectionAdapter extends MouseAdapter{
		
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2){
				ejecutarReporte();
			}
		}
	}
	
	private String toSimpleString(File f){
		String name=f.getName();
		int endIndex=name.lastIndexOf('.');
		name=name.substring(0, endIndex);
		return name;
	}
	
	
	private class FileRenderer extends DefaultTreeCellRenderer{

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
					row, hasFocus);
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)value;
			if(node.getUserObject() instanceof File){
				
				File f=(File)node.getUserObject();
				if(f.isFile()){
					String name=f.getName();
					int endIndex=name.lastIndexOf('.');
					name=name.substring(0, endIndex);
					setText(name);
				}
				
			}
			return this;
		}
		
	}

}
