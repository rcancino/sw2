package com.luxsoft.sw3.contabilidad.ui.reportes;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
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

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.DockingUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;

/**
 * 
 */
public class ReportTaskPanel extends JPanel{
	
	protected JXTaskPaneContainer taskContainer;	
	protected JXTaskPane reportes;
	protected JXTaskPane filtros;
	
	public ReportTaskPanel() {
		super(new BorderLayout());
	}
	
	@Override
	public void addNotify() {
		super.addNotify();
		init();
	}

	private void init(){		
		add(buildContent(),BorderLayout.CENTER);
		AnalisisDeDIOTPanel diodPanel=new AnalisisDeDIOTPanel(2012, 1);
		View view=new View("Análisis DIOD", null, diodPanel.getControl());
		addView(view, view.hashCode());
	}

	
	protected JComponent buildContent() {
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
		reportes=createTaskPanel("Consultas", "");
		reportes.setSpecial(true);		
		filtros=createTaskPanel("Filtros", "images2/page_find.png");
		instalarReportes();
		taskContainer.add(reportes);
		taskContainer.add(filtros);
		return new JScrollPane(taskContainer);
	}
	
	protected void instalarReportes(){
		reportes.add(addAction(null, "informeIva", "Informe de IVA"));
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
		pane.setIcon(ResourcesUtils.getIconFromResource(iconPath));
		
		return pane;
	}
	
	public Action addAction(String actionId,String method,String label){
		Action a=new DispatchingAction(this,method);
		configAction(a, actionId,label);
		if(StringUtils.isBlank(actionId))
			return a;  //No requiere seguridad
		if(KernellSecurity.instance().isActionGranted(a))
			return a;
		return null;
	}
	
	private ViewMap viewMap;
	protected RootWindow rootWindow;
	private DockingWindowListener dockingHandler;
	
	/**
	 * Crea el panel de documentos o vistas internas, 
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
	
	
	public void addView(final View view,int id){
		DockingUtil.addWindow(view,rootWindow);
		viewMap.addView(id,view);
		view.restoreFocus();
		view.addListener(dockingHandler);
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
			v.removeListener(dockingHandler);
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
