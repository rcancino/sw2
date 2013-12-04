package com.luxsoft.siipap.ventas;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.ViewControl;
import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;





public class VentasTaskView extends DefaultTaskView{
	
	private InternalTaskTab mainTab;
	private InternalTaskTab resumenTab;
	
	public VentasTaskView(){
		
	}
	
	protected void instalarTaskElements(){
		
		final Action mainTask=new AbstractAction("InventarioTaskView.view1"){
			public void actionPerformed(ActionEvent e) {
				mostrarMovimientos();				
			}
			
		};
		configAction(mainTask, "InventarioTaskView.view1");
		mainTask.putValue(Action.NAME, "Movimientos");
		consultas.add(mainTask);
		
		final Action showResumen=new AbstractAction("Resumen"){
			public void actionPerformed(ActionEvent e) {
				mostrarResumen();				
			}
			
		};
		configAction(showResumen, "InventarioTaskView.view2");
		showResumen.putValue(Action.NAME, "Resumen");
		consultas.add(showResumen);
		
	}
	
	@Override
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //No necesitamos este panel
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	@SuppressWarnings("unchecked")
	private void mostrarMovimientos(){
		if(mainTab==null){
			VentasView panel=new VentasView();
			panel.getControl();//Requreimos inicializar para usar la lista filtrada
			TotalesPanel totPanel=new TotalesPanel(panel.getFilteredSource());
			MainView view=new MainView(panel);
			view.setTotalesPanel(totPanel);
			view.setTitle("Ventas");
			mainTab=new InternalTaskTab(view);
		}
		addTab(mainTab);
		
	}
	
	
	private void mostrarResumen(){
		if(mainTab==null) 
			return;
		//Pendiente por desarrollar resumen
		/*
		if(resumenTab==null){
			//resumenTab=new InternalTaskTab(new ResumenDeGastos(comprasView.getFilterCompras()));
			ComprasResumenView view=new ComprasResumenView();
			view.setTitle("Resumen");
			resumenTab=new InternalTaskTab(view);
			
		}
		addTab(resumenTab);
		*/		
	}
		
	/**
	 * Adapta un {@link FilteredBrowserPanel} para ser usado como
	 * {@link AbstractInternalTaskView}
	 * 
	 * @author Ruben Cancino
	 *
	 */
	@SuppressWarnings("unchecked")
	public static  class MainView extends AbstractInternalTaskView{
		
		private final FilteredBrowserPanel browser;
		private ViewControl totalesPanel;
		
		public MainView(FilteredBrowserPanel browser){
			this.browser=browser;
		}

		public JComponent getControl() {
			return browser.getControl();
		}

		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){				
				operaciones.add(a);
			}
		}

		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {			
			filtros.add(browser.getFilterPanel());
		}		
		@Override
		public void installDetallesPanel(JXTaskPane detalle) {
			if(getTotalesPanel()!=null)
				detalle.add(getTotalesPanel().getControl());
		}
		
		public EventList getEventList(){
			return browser.getSource();
		}

		public ViewControl getTotalesPanel() {
			return totalesPanel;
		}

		public void setTotalesPanel(ViewControl totalesPanel) {
			this.totalesPanel = totalesPanel;
		}

		
		
		
	}

}
