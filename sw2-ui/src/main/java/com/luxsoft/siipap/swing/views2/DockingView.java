package com.luxsoft.siipap.swing.views2;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.tabbedpanel.titledtab.TitledTab;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.beans.BeanUtils;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.DockingUtils;

/**
 * Mejoramiento a DefaultTaskView 
 * 
 * @author Ruben Cancino
 * 
 * TODO Hacer una Interfaz de las altas bajas y cambios
 *
 */
public class DockingView extends AbstractView{
	
	
	protected RootWindow rootWindow;
	private ViewMap viewMap;
	protected JXTaskPane consultas;	
	protected JXTaskPane operaciones;
	protected JXTaskPane procesos;
	protected JXTaskPane filtros;
	protected JXTaskPane detalles;
	protected JXTaskPaneContainer taskContainer;
	private double resizeWeightForView=.10;
	
	
	@Override
	protected JComponent buildContent() {
		JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,createTaskPanel()
				,createDocumentPanel());
		sp.setResizeWeight(getResizeWeightForView());
		sp.setOneTouchExpandable(true);
		sp.setBorder(null);
		return sp;
	}
	
	

	/**
	 * Crea el JXTaskPaneContainer que es accesible para las sub-clases
	 * mediante la variable de instancia taskContainer
	 * @return
	 */
	protected JComponent createTaskPanel(){
		taskContainer=new JXTaskPaneContainer();
		
		consultas=createTaskPanel(getConsultasLabel(), "");
		consultas.setSpecial(true);		
		operaciones=createTaskPanel("Operaciones", "");
		procesos=createTaskPanel("Procesos", "images2/overlays.png");
		filtros=createTaskPanel("Filtros", "images2/page_find.png");
		detalles=createTaskPanel("Detalles", "images2/information.png");
		detalles.setExpanded(false);
		
		instalarTaskElements();
		
		taskContainer.add(consultas);
		taskContainer.add(operaciones);
		taskContainer.add(procesos);			
		taskContainer.add(filtros);		
		taskContainer.add(detalles);
		
		return new JScrollPane(taskContainer);
	}
	
	
	/**
	 * Crea un JXTaskPanel
	 * @param title
	 * @param iconPath
	 * @return
	 */
	protected JXTaskPane createTaskPanel(final String title, final String iconPath){
		final JXTaskPane pane=ComponentUtils.createStandarJXTaskPane();
		pane.setTitle(title);
		pane.setLayout(new VerticalLayout(5));
		pane.setIcon(getIconFromResource(iconPath));
		
		return pane;
	}
	
	private DockingViewAdapter dockingHandler;
	
	/**
	 * Crea el panel de documentos o vistas internas, Esta implementacion crea un TabbedPanel
	 * que es accesible para las sub-clases mediante la  variable tabPanel
	 * 
	 * @return
	 */
	protected JComponent createDocumentPanel(){
		viewMap=new ViewMap();
		rootWindow=new RootWindow(viewMap);
		
		dockingHandler=new DockingViewAdapter(this);
		DockingUtils.configRootWindow(rootWindow);
		DockingUtils.configTabWindowProperties(rootWindow);
		rootWindow.addListener(dockingHandler);
		return rootWindow;
	}
	
	
	/**
	 * Cierra las vistas internas 
	 */
	public void close(){		
		closeAllViews();
		rootWindow.close();
		logger.info("Cerrando las vistas internas para :"+getId());
	}
	
	private List<View> getAllViews(){
		List<View> views=new ArrayList<View>();
		int windows=viewMap.getViewCount();
		for(int i=0;i<windows;i++){
			View v=viewMap.getViewAtIndex(i);
			views.add(v);
		}
		return  views;
	}
	
	private void closeAllViews(){		
		for(View v:getAllViews()){
			v.close();
		}		
	}
	
	/**
	 * Template method para configurar acciones de la consulta
	 * 
	 */
	protected void configAction(final Action a,String id){
		CommandUtils.configAction(a, id, null);
	}
	
	protected void configAction(final Action a,String id,String label){
		CommandUtils.configAction(a, id, null);
		a.putValue(Action.NAME, label);
	}
	
	/*** Manejo de FilterBrower Panels ***/
	
	private List panels=new ArrayList();
	
	protected Action defaultBrowserAction;
	
	/**
	 * Instala los BrowserPanels existentes
	 * 
	 */
	protected void instalarTaskElements(){
		for(Object obj:panels){
			if(obj instanceof FilteredBrowserPanel){
				FilteredBrowserPanel fp=(FilteredBrowserPanel)obj;
				boolean res=KernellSecurity.instance().isConsultaGranted(fp.getSecurityId());
				if(res){
					Action a=new LoadFilterPanelAction(fp);
					configAction(a, fp.getTitle(),fp.getTitle());
					consultas.add(a);
					if(fp.isDefaultPanel())
						defaultBrowserAction=a;
				}
			}else if (obj instanceof String){
				consultas.add(new JSeparator());
			}
			
			
		}
	}
	
	@Override
	public void open() {
		super.open();
		if(defaultBrowserAction!=null)
			defaultBrowserAction.actionPerformed(null);
	}
	
	
	public void removerTaskPanel(final JXTaskPane panel){
		this.taskContainer.remove(panel);
	}
	
	public JXTaskPane getConsultasTaskPanel(){
		return this.consultas;
	}
	
	
	
	public void setBrowser(final FilteredBrowserPanel browser){		
		operaciones.removeAll();
		procesos.removeAll();
		filtros.removeAll();
		detalles.removeAll();
		if(browser==null)
			return;
		if(browser!=null){
			for(Action a:browser.getActions()){
				if(a!=null)
					operaciones.add(a);
			}
			if(browser.getPeriodo()!=null){
				operaciones.add(browser.getPeriodoLabel());
			}
			for(JComponent c:browser.getOperacionesComponents()){
				operaciones.add(c);
			}
		}
		if(browser.getProccessActions()!=null){
			for(Object o:browser.getProccessActions()){
				Action a=(Action)o;
				procesos.add(a);
			}
		}
		filtros.add(browser.getFilterPanel());
		Method method=BeanUtils.findDeclaredMethodWithMinimalParameters(browser.getClass(), "getTotalesPanel");
		if(method!=null){
			JComponent res;
			try {
				res = (JComponent)method.invoke(browser,new Object[0]);
				detalles.add(res);
				detalles.setExpanded(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * BrowerPanels disponibles
	 * 
	 * 
	 * @return
	 */
	public List getPanels() {
		return panels;
	}

	public void setPanels(List panels) {
		this.panels = panels;
	}
	
	public class LoadFilterPanelAction extends AbstractAction{
		
		private final FilteredBrowserPanel fp;

		public LoadFilterPanelAction(final FilteredBrowserPanel fp) {
			this.fp = fp;
		}

		public void actionPerformed(ActionEvent e) {			
			View view=viewMap.getView(fp.getControl().hashCode());
			if(view==null){				
				//view=new View(fp.getTitle(),null,fp.getControl());
				view=fp.getView();
				view.putClientProperty(FilteredBrowserPanel.CLIENTE_PROPERTY_ID, fp);
				
				viewMap.addView(fp.getControl().hashCode(), view);
			}
			//System.out.println("Vista localizada: "+view.getTitle());
			DockingUtil.addWindow(view,rootWindow);
			view.restoreFocus();
		}		
	}
	
	private String consultasLabel="Consultas";


	public String getConsultasLabel() {
		return consultasLabel;
	}
	public void setConsultasLabel(String consultasLabel) {
		this.consultasLabel = consultasLabel;
	}

	public double getResizeWeightForView() {
		return resizeWeightForView;
	}

	public void setResizeWeightForView(double resizeWeightForView) {
		this.resizeWeightForView = resizeWeightForView;
	}
	
}
