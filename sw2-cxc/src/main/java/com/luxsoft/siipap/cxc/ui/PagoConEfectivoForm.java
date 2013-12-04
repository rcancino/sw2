package com.luxsoft.siipap.cxc.ui;

import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class PagoConEfectivoForm extends PagoPanel{
	
	public PagoConEfectivoForm(PagoFormModel model) {
		super(model);
		setTitle("Registro de pago (EFECTIVO)");
	}
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		builder.append("Cobrador",getControl("cobrador"));
		builder.append("Enviado",getControl("enviado"));		
		builder.append("Origen",getControl("origen"));
		builder.append("Anticipo",getControl("anticipo"));
		
	}

	public static PagoConEfectivo showForm(final PagoFormModel model){
		PagoConEfectivoForm dialog=new PagoConEfectivoForm(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return (PagoConEfectivo)dialog.getPagoModel().getPago();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				PagoFormModel model=new PagoFormModel(Bean.proxy(PagoConEfectivo.class),false);
				model.loadClientes();				
				showForm(model);
				showObject(model.getAbono());
				
			}
			
		});
		
	}

}
