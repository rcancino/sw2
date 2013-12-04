package com.luxsoft.siipap.swing.views2;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.Assert;

import com.luxsoft.siipap.swing.AbstractView;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.DockingUtils;

/**
 * Vista que agrega una seccion de tareas y opciones representados
 * por un JXTaskPaneContainer y algunos JXTaskPane 
 * 
 * @author Ruben Cancino
 * 
 * TODO Hacer una Interfaz de las altas bajas y cambios
 *
 */
public class DefaultTaskDockingView extends AbstractView{
	
		
	protected JXTaskPane consultas;
	protected JXTaskPane operaciones;
	protected JXTaskPane procesos;
	protected JXTaskPane filtros;
	protected JXTaskPane detalles;
	protected JXTaskPaneContainer taskContainer;
	
	private final String name;
	
	
	
	public DefaultTaskDockingView(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}	

	@Override
	protected JComponent buildContent() {
		initActions();
		JSplitPane sp=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,createTaskPanel()
				,createDocumentPanel());
		sp.setResizeWeight(.15);
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
		consultas=createTaskPanel("Consultas", "");
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
	 * Template method para instalar acciones y/o componentes a los task panels
	 * creados. Se tiene acceso por medio de variables protected a los taskpnels
	 * de opreciones,procesos,filtros y detalles
	 * 
	 */
	protected void instalarTaskElements(){
		
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
		pane.setLayout(new VerticalLayout(1));
		pane.setIcon(getIconFromResource(iconPath));
		
		return pane;
	}
	
	private ViewMap viewMap;
	protected RootWindow rootWindow;
	private DockingWindowListener dockingHandler;
	
	/**
	 * Crea el panel de documentos o vistas internas, Esta implementacion crea un TabbedPanel
	 * que es accesible para las sub-clases mediante la  variable tabPanel
	 * 
	 * @return
	 */
	protected JComponent createDocumentPanel(){
		//Configuramos RootWindow
		viewMap=new ViewMap();
		rootWindow=new RootWindow(viewMap);
		dockingHandler=new DockingHandler();
		DockingUtils.configRootWindow(rootWindow);
		DockingUtils.configTabWindowProperties(rootWindow);
		rootWindow.addListener(dockingHandler);
		return rootWindow;
	}
	
	/**
	 * Regresa  una instancia  de {@link View} si ya existe o bien lazy genera una nueva
	 * 
	 * @param panel
	 * @return
	 */
	public View getView(final JComponent panel){
		int id=(Integer)panel.getClientProperty(DockingConstants.DOCKING_ID_KEY);
		Assert.isTrue(id>0,"No ha asignado un Id al panel");		
		//Buscamos una vista ya registrada
		
		View view=viewMap.getView(panel.hashCode());
		if(view!=null){
			view.restoreFocus();
			return view;
		}else{			
			//Generamos un una nueva vista			
			String title=(String)panel.getClientProperty(DockingConstants.DOCKING_TITLE_KEY);
			if(StringUtils.isEmpty(title))
				title="Hash:"+String.valueOf(id);
			Icon icon=(Icon)content.getClientProperty(DockingConstants.DOCKING_ICON_KEY);			
			view=new View(title,icon,panel);
			String tooltip=(String)content.getClientProperty(DockingConstants.DOCKING_TOOLTIP_KEY);
			if(!StringUtils.isBlank(tooltip))
				view.setToolTipText(tooltip);
			
			view.addListener(dockingHandler);
			DockingUtil.addWindow(view,rootWindow);
			return view;
		}
	}
	
	public void addView(final View view,int id){
		DockingUtil.addWindow(view,rootWindow);
		viewMap.addView(id,view);
		view.restoreFocus();
	}
	
	/**
	 * Agrega un panel 
	 * 
	 * @param panel
	 */
	public View addPanel(final JComponent panel){
		final View view=getView(panel);
		int id=(Integer)panel.getClientProperty(DockingConstants.DOCKING_ID_KEY);
		addView(view, id);
		return view;
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
	protected void vistaInternaDeSeleccionada(final View view){		
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
	protected void vistaInternaSeleccionada(final View tab){
		/** TODO Modificar esto para utlizar una Interfaz y el Lookup pathern
		if(tab instanceof InternalTaskTab){
			InternalTaskView s=((InternalTaskTab)tab).getTaskView();
			s.instalOperacionesAction(operaciones);
			s.instalProcesosActions(procesos);
			s.installFiltrosPanel(filtros);
			s.installDetallesPanel(detalles);
		}	
		**/
		System.out.println("Panel seleccionado: "+tab.getName());
	}
	
	public void dispose(){		
		closeAllViews();
		rootWindow.close();
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
			//v.removeListener(dockingListener);
		}		
	}
	
	protected void configAction(final Action a,String id){
		CommandUtils.configAction(a, id, null);
	}
	
	protected void configAction(final Action a,String id,String defaultName){
		CommandUtils.configAction(a, id, null);
		String name=(String)a.getValue(Action.NAME);
		if(StringUtils.isEmpty(name))
			a.putValue(Action.NAME, defaultName);
		
	}

	protected class DockingHandler extends DockingWindowAdapter{

		@Override
		public void viewFocusChanged(View previouslyFocusedView,View focusedView) {
			/*if(focusedView!=null)
				System.out.println("Focus recibido: "+focusedView.getTitle());
				*/	
		}

		@Override
		public void windowAdded(DockingWindow addedToWindow,DockingWindow addedWindow) {
			System.out.println("Window added to: "+addedToWindow.getTitle());
		}

		@Override
		public void windowClosed(DockingWindow window) {
			System.out.println("Window closed: "+window.getTitle());
		}

		@Override
		public void windowClosing(DockingWindow window)throws OperationAbortedException {
			System.out.println("Window closing: "+window.getTitle());
		}

		@Override
		public void windowDocked(DockingWindow window) {
			System.out.println("Window docked: "+window.getTitle());
		}

		@Override
		public void windowDocking(DockingWindow window)throws OperationAbortedException {
			System.out.println("Window docking: "+window.getTitle());
		}

		@Override
		public void windowHidden(DockingWindow window) {
			System.out.println("Window hiden: "+window.getTitle());
		}

		@Override
		public void windowRemoved(DockingWindow removedFromWindow,DockingWindow removedWindow) {
			System.out.println("Window removed fomr: "+removedFromWindow.getTitle());
		}

		@Override
		public void windowRestored(DockingWindow window) {
			System.out.println("Window restored: "+window.getTitle());
		}

		@Override
		public void windowRestoring(DockingWindow window)throws OperationAbortedException {
			System.out.println("Window restoring: "+window.getTitle());
		}

		@Override
		public void windowShown(DockingWindow window) {
			System.out.println("Window shown: "+window.getTitle());
		}
		
	}
	
}
