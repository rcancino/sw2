package com.luxsoft.siipap.cxp.ui;

import javax.swing.Action;

import org.jdesktop.swingx.JXTaskPaneContainer;

import com.luxsoft.siipap.cxp.ui.consultas.FacturasAnalizadas;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class CXPView extends DefaultTaskView{
	
	private FacturasAnalizadas facturasPanel;
	private InternalTaskTab facturasTab;
	
	protected void instalarTaskElements(){
		Action facAction=new DispatchingAction(this,"mostrarFacturas");
		facAction.putValue(Action.NAME, "Facturas");
		consultas.add(facAction);
	}
	
	protected void instalarTaskPanels(final JXTaskPaneContainer container){
		
	}
	
	public void mostrarFacturas(){
		if(facturasTab==null){
			InternalTaskAdapter adapter=new InternalTaskAdapter(getFacturasPanel()){
				
			};
			adapter.setTitle("Facturas");
			facturasTab=new InternalTaskTab(adapter);
		}
		addTab(facturasTab);
	}
	

	@Override
	public void close() {
		super.close();
		getFacturasPanel().close();
	}

	@Override
	public void open() {
		
	}

	public FacturasAnalizadas getFacturasPanel() {
		return facturasPanel;
	}

	public void setFacturasPanel(FacturasAnalizadas facturasPanel) {
		this.facturasPanel = facturasPanel;
	}
	
	

}
