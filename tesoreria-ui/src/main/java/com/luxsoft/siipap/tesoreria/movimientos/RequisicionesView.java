package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;

import org.jdesktop.swingx.JXTaskPane;

import com.luxsoft.siipap.swing.views2.AbstractInternalTaskView;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;
import com.luxsoft.siipap.tesoreria.TesoreriaActions;

public class RequisicionesView extends DefaultTaskView{
	
	private InternalTaskTab reqTab;
	
	public RequisicionesView(){
		
	}
	
	protected void instalarTaskElements(){
		final Action mostrarReqs=new AbstractAction("Requisiciones"){
			public void actionPerformed(ActionEvent e) {
				mostrarRequisiciones();				
			}
			
		};
		configAction(mostrarReqs,TesoreriaActions.ShowRequisicionesView.getId());
		consultas.add(mostrarReqs);
	}
	
	public void mostrarRequisiciones(){
		if(reqTab==null){
			RequiscionesGlobales view=new RequiscionesGlobales();
			view.setTitle("Requisiciones");
			reqTab=new InternalTaskTab(view);
		}
		reqTab.getTaskView().load();
		addTab(reqTab);
	}
	
	
	public static class RequiscionesGlobales extends AbstractInternalTaskView{
		
		RequisicionesBrowser browser;

		public JComponent getControl() {
			if(browser==null){
				browser=new RequisicionesBrowser();
			}
			return browser.getControl();
		}
		
		@Override
		public void instalOperacionesAction(JXTaskPane operaciones) {
			for(Action a:browser.getActions()){
				operaciones.add(a);
			}
		}
		
		@Override
		public void instalProcesosActions(JXTaskPane procesos) {
			//procesos.add(browser.getAutorizarAction());			
			//procesos.add(browser.getCancelraAutorizacion());
			procesos.add(browser.getPagarAction());
			procesos.add(browser.getCancelarPagoAction());
			procesos.add(browser.getImprimirPolizaAction());
			//procesos.add(browser.getImportarReqCompras());
			procesos.add(browser.createActionReqDet());
			procesos.add(browser.createActionReqGral());
		}

		@Override
		public void installFiltrosPanel(JXTaskPane filtros) {
			filtros.add(browser.getFilterPanel());
		}
		
	}

}
