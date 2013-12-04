package com.luxsoft.siipap.gastos.operaciones;

import org.jdesktop.swingx.JXTaskPaneContainer;

import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

/**
 * Consulta de mantenimiento de pagos
 * 
 * @author Ruben Cancino
 *
 */
public class PagosView extends DefaultTaskView{
	
	private InternalTaskTab chequesTab;
	private ChequesPanel chequesPanel;
	
	
	
	@Override
	public void open() {
		mostrarPagos();
		//chequesPanel.load();
	}
	
	@Override
	public void close() {
		if(chequesPanel!=null)
			chequesPanel.close();
	}

	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		this.taskContainer.remove(procesos); //We don't need this
		this.detalles.setTitle("Resumen");
		this.detalles.setExpanded(true);
	}
	
	
	
	public void mostrarPagos(){
		if(chequesPanel==null){
			chequesPanel=new ChequesPanel();
			InternalTaskAdapter adapter=new InternalTaskAdapter(chequesPanel);
			chequesTab=new InternalTaskTab(adapter);
		}
		addTab(chequesTab);
	}
	
	
	

}
