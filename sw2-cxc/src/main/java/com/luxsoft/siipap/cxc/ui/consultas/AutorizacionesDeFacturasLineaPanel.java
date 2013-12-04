package com.luxsoft.siipap.cxc.ui.consultas;

import java.util.List;

import javax.swing.Action;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Panel para la autorizacion de facturas por linea de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AutorizacionesDeFacturasLineaPanel extends FilteredBrowserPanel<Venta>{

	public AutorizacionesDeFacturasLineaPanel() {
		super(Venta.class);
		
	}

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				};
		return actions;
	}

	@Override
	protected void doSelect(Object bean) {
		Venta v=(Venta)getSelectedObject();
		FacturaForm.show(v.getId());
	}

	@Override
	protected List<Venta> findData() {
		String hql="";
		return super.findData();
	}
	
	
	

}
