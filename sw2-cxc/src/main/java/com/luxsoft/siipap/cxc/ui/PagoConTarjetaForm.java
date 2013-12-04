package com.luxsoft.siipap.cxc.ui;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.swing.binding.Bindings;

/**
 * Forma para el mantenimiento de pagos con tarjetas de credito/debito
 * 
 * @author Ruben Cancino
 *
 */
public class PagoConTarjetaForm extends PagoPanel{

	public PagoConTarjetaForm(PagoConTarjetaFormModel model) {
		super(model);
		
	}
	
	protected PagoConTarjetaFormModel getPagoConTarjetaModel(){
		return (PagoConTarjetaFormModel)getPagoModel();
	}
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		super.instalarAntesDeComentario(builder);
		//getControl("fecha").setEnabled(true);
		builder.appendSeparator("Tarjeta");
		builder.append("Nombre",getControl("tarjeta"));
		builder.append("Esquema",getControl("esquema"));
		builder.append("Comisión",addReadOnly("comisionBancaria"));
		builder.append("Aut Bancaria",getControl("autorizacionBancaria"));
		builder.append("Cuenta Destino",getControl("cuenta"));
		getControl("sucursal").setEnabled(true);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tarjeta".equals(property)){
			List list=getPagoConTarjetaModel().getTarjetas();
			SelectionInList sl=new SelectionInList(list,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("esquema".equals(property)){
			List list=getPagoConTarjetaModel().getEsquemas();
			SelectionInList sl=new SelectionInList(list,getPagoConTarjetaModel().getEsquemaHolder());
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("cuenta".equals(property)){
			JComboBox box=Bindings.createCuentasBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}
	

	public static PagoConTarjeta showForm(final PagoConTarjetaFormModel model){
		PagoConTarjetaForm dialog=new PagoConTarjetaForm(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return (PagoConTarjeta)dialog.getPagoModel().getPago();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				
				PagoConTarjetaFormModel model=new PagoConTarjetaFormModel(new PagoConTarjeta(),false);
				//model.loadClientes();				
				showForm(model);
				showObject(model.getAbono());
			}
			
		});
	}

}
