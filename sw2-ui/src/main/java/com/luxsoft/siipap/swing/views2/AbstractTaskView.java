package com.luxsoft.siipap.swing.views2;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;


import net.infonode.tabbedpanel.TabAdapter;
import net.infonode.tabbedpanel.TabEvent;
import net.infonode.tabbedpanel.TabRemovedEvent;
import net.infonode.tabbedpanel.TabStateChangedEvent;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.titledtab.TitledTab;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;


import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Mejoramiento a DefaultTaskView 
 * 
 * @author Ruben Cancino
 * 
 * TODO Hacer una Interfaz de las altas bajas y cambios
 *
 */
public class AbstractTaskView extends AbstractView{
	
	protected TabbedPanel tabPanel;	
	protected JXTaskPane consultas;
	protected JXTaskPane operaciones;
	protected JXTaskPane procesos;
	protected JXTaskPane filtros;
	protected JXTaskPane detalles;
	protected JXTaskPaneContainer taskContainer;
	
	private boolean procesosTasksVisible=true;
	private boolean detallesTaskVisible=true;
	private double resizeWeightForView=.18;
	
	
	@Override
	protected JComponent buildContent() {
		initActions();
		JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,createTaskPanel()
				,createDocumentPanel());
		sp.setResizeWeight(getResizeWeightForView());
		sp.setOneTouchExpandable(true);
		sp.setBorder(null);
		return sp;
	}
	
	
	public double getResizeWeightForView() {
		return resizeWeightForView;
	}

	public void setResizeWeightForView(double resizeWeightForView) {
		this.resizeWeightForView = resizeWeightForView;
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
		if(isProcesosTasksVisible())
			taskContainer.add(procesos);
		taskContainer.add(filtros);
		if(isDetallesTaskVisible())
			taskContainer.add(detalles);
		instalarTaskPanels(taskContainer);
		return new JScrollPane(taskContainer);
	}
	
	/**
	 * Template method para instalar elementos JXTaskPanel al JXTaxkPaneContainer
	 * , el container pasado es la instancia protected taskContainer.
	 * Este metodo permite la extensibilidad de esta clase
	 *  
	 * @param container
	 */
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		
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
	
	/**
	 * Crea el panel de documentos o vistas internas, Esta implementacion crea un TabbedPanel
	 * que es accesible para las sub-clases mediante la  variable tabPanel
	 * 
	 * @return
	 */
	protected JComponent createDocumentPanel(){
		tabPanel=new TabbedPanel();
		tabPanel.addTabListener(new TabHandler());
		return tabPanel;
	}
	
	/**
	 * Inserta instancias de InternalTaskView envueltas en una
	 * instancia de InternalTaskTab. Las subclases pueden ocupar este metodo
	 * para agregar vistas internas al document panel 
	 *  
	 * @param view
	 */
	protected InternalTaskTab createInternalTaskTab(final InternalTaskView view){
		final InternalTaskTab tab=new InternalTaskTab(view);		
		return tab;
	}
	
	public void addTab(final InternalTaskTab tab){
		tabPanel.addTab(tab);
		tab.setSelected(true);
	}
	
	/**
	 * Template method para inicializar acciones antes de construir el panel
	 * principal de esta vista, las sub clases lo pueden implementar sabiendo
	 * que este metodo se ejecutara solo una vez dentro del cuerpo del metodo
	 * buildContent.
	 * 
	 */
	protected void initActions(){
		
	}	
	
	/************* Metodos para el comportamiento dinamico *******************/
	
	/**
	 *  Metodo ejecutado cuando una vista deja de ser la vista seleccionada
	 *  se utiliza para inicialmente para limiar los taskpanels
	 *  Lo adecuado en esta implementacion es que las sub clases que sobre 
	 *  escriban este metodo lo  manden llamar para garantizar la limpieza
	 *  de los taskpanels operaciones,procesos,filtros y detalle. 
	 * @param tab
	 */
	protected void vistaInternaDeSeleccionada(final TitledTab tab){		
		operaciones.removeAll();
		procesos.removeAll();
		filtros.removeAll();
		detalles.removeAll();
	}
	
	/**
	 * Metodo detonado cuando una vista interna implementada en un TitledTab
	 * es seleccionada. Se utiliza en primera instancia para agregar componentes
	 * a los taskpanels, esto si la vista seleccionada implementa InternalTaskView 
	 * Lo adecuado en esta implementacion es que las sub clases que sobre 
	 *  escriban este metodo lo  manden llamar para garantizar la correcta 
	 *  adicion de acciones y paneles a los taskpanels operaciones,procesos,filtros y detalle.
	 *  
	 * @param tab
	 */
	protected void vistaInternaSeleccionada(final TitledTab tab){
		
		if(tab instanceof InternalTaskTab){
			InternalTaskView s=((InternalTaskTab)tab).getTaskView();
			s.instalOperacionesAction(operaciones);
			s.instalProcesosActions(procesos);
			s.installFiltrosPanel(filtros);
			s.installDetallesPanel(detalles);
		}	
	}
	
	/**
	 * Metodo detonado cuando una vista interna es cerrada, Se utiliz para
	 * detonar el metodo close de la vista si esta implementa InternalTaskView
	 * 
	 * @param tab
	 */
	protected void vistaInternaCerrada(final TitledTab tab){		
		if(tab instanceof InternalTaskView){
			InternalTaskView s=((InternalTaskTab)tab).getTaskView();
			s.close();
		}
		if(tab instanceof InternalTaskTab){
			InternalTaskTab i=(InternalTaskTab)tab;
			i.getTaskView().close();
		}
	}
	
	
	/**
	 * Metodo detonado cuando una vista interna es abierta, Se utiliz para
	 * detonar el metodo open de la vista si esta implementa InternalTaskView
	 * 
	 * @param tab
	 */
	protected void vistaInternaOpen(final TitledTab tab){
		
		//System.out.println("Tab class: "+tab.getClass().getName());
		if(tab instanceof InternalTaskView){
			InternalTaskView s=((InternalTaskTab)tab).getTaskView();
			s.open();
		}	
		if(tab instanceof InternalTaskTab){
			InternalTaskTab i=(InternalTaskTab)tab;
			i.getTaskView().open();
		}
	}
	
	/**
	 * Cierra las vistas internas 
	 */
	public void close(){		
		while(tabPanel.getSelectedTab()!=null){
			InternalTaskTab tab=(InternalTaskTab)tabPanel.getSelectedTab();
			tabPanel.removeTab(tab);
		}
		logger.info("Cerrando las vistas internas para :"+getId());
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


	/**
	 * TabAdapter para controlar la apariencia de el taskpanel
	 * en funcion de la ventana seleccionada 
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public class TabHandler  extends TabAdapter{
		
		@Override
		public void tabSelected(TabStateChangedEvent event) {
			TitledTab tab=(TitledTab)event.getCurrentTab();
			if(tab!=null){
				vistaInternaSeleccionada(tab);			
			}
		}

		@Override
		public void tabDeselected(TabStateChangedEvent event) {
			TitledTab tab=(TitledTab)event.getCurrentTab();
			if(tab!=null){
				vistaInternaDeSeleccionada(tab);			
			}			
		}

		@Override
		public void tabRemoved(TabRemovedEvent event) {
			TitledTab tab=(TitledTab)event.getTab();
			if(tab!=null){
				vistaInternaCerrada(tab);			
			}			
		}

		@Override
		public void tabAdded(TabEvent event) {
			TitledTab tab=(TitledTab)event.getTab();
			if(tab!=null){
				vistaInternaOpen(tab);			
			}	
		}
		
	}
	
	/*** Manejo de FilterBrower Panels ***/
	
	//private List<FilteredBrowserPanel> panels=new ArrayList<FilteredBrowserPanel>();
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
	
	


	/**
	 * Trata de localizar el InternalTaskTab que hospeda el FilterBrowserPanel indicado
	 * 
	 * @param panel
	 * @return El InternalTaskTab del panel o nulo si este no se ha instalado
	 */
	protected InternalTaskTab findTabForBrowser(final FilteredBrowserPanel panel){
		int tabs=this.tabPanel.getTabCount();		
		for(int index=0;index<tabs;index++){
			InternalTaskTab tab=(InternalTaskTab)tabPanel.getTabAt(index);
			if(tab.getContentComponent().equals(panel.getControl()))
				return tab;
		}
		return null;
	}
	
	public void removerTaskPanel(final JXTaskPane panel){
		this.taskContainer.remove(panel);
	}
	
	public JXTaskPane getProcesosTaskPanel(){
		return this.procesos;
	}
	public JXTaskPane getConsultasTaskPanel(){
		return this.consultas;
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
			InternalTaskTab tab=findTabForBrowser(fp);
			if(tab==null){
				InternalTaskAdapter adapter=new InternalTaskAdapter(fp);
				adapter.setTitle(fp.getTitle());
				tab=new InternalTaskTab(adapter);
			}
			addTab(tab);
		}
		
	}
	
	private String consultasLabel="Consultas";



	public String getConsultasLabel() {
		return consultasLabel;
	}
	public void setConsultasLabel(String consultasLabel) {
		this.consultasLabel = consultasLabel;
	}



	public boolean isProcesosTasksVisible() {
		return procesosTasksVisible;
	}



	public void setProcesosTasksVisible(boolean procesosTasksVisible) {
		this.procesosTasksVisible = procesosTasksVisible;
	}



	public boolean isDetallesTaskVisible() {
		return detallesTaskVisible;
	}



	public void setDetallesTaskVisible(boolean detallesTaskVisible) {
		this.detallesTaskVisible = detallesTaskVisible;
	}
	
	
	
	
}
