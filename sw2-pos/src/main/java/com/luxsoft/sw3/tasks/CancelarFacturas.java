package com.luxsoft.sw3.tasks;

import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;

import ca.odell.glazedlists.ListSelection;

import com.luxsoft.siipap.cxc.model.AutorizacionParaCargo;
import com.luxsoft.siipap.cxc.model.CancelacionDeCargo;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturas2;
import com.luxsoft.siipap.security.CancelacionDeCargoForm;
import com.luxsoft.siipap.security.CancelacionDeCargoFormModel;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.AutorizacionesFactory;

public class CancelarFacturas extends AbstractAction{
	

	public void actionPerformed(ActionEvent e) {
		SelectorDeFacturas2 selector=new SelectorDeFacturas2();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Venta cargo=selector.getSelected();
			Date fecha=Services.getInstance().obtenerFechaDelSistema();
			CancelacionDeCargoFormModel model=new CancelacionDeCargoFormModel();
			model.setHibernateTemplate(Services.getInstance().getHibernateTemplate());
			CancelacionDeCargoForm form=new CancelacionDeCargoForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				Venta factura=Services.getInstance().getFacturasManager().cancelarFactura(
						cargo.getId()
						, fecha
						,model.getCancelacion().getUsuario()
						,model.getCancelacion().getComentario()
						);
				MessageUtils.showMessage("Factura cancelada:\n"+factura, "Cancelacion de ventas");
				
			}
		}
		
	}
	
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				new CancelarFacturas().actionPerformed(null);
				System.exit(0);
			}

		});
	}

	

}
