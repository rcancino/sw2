package com.luxsoft.siipap.tesoreria.procesos;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class PagoDeRequisicionForm extends AbstractForm{
	
	

	public PagoDeRequisicionForm(PagoDeRequisicionFormModel model) {
		super(model);
	}
	
	public PagoDeRequisicionFormModel getPagoModel(){
		return (PagoDeRequisicionFormModel)model;
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,p,2dlu,p,2dlu,p"
				,"");		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Requisición");
		builder.append("Id",addReadOnly("id"),true);
		builder.append("A Favor",addReadOnly("afavor"),5);
		builder.append("Fecha",addReadOnly("fecha"));
		builder.append("Importe",addReadOnly("total"));
		
		builder.append("Moneda",addReadOnly("moneda"));
		builder.append("TC",addReadOnly("tipoDeCambio"));
		builder.append("Por pagar:",addReadOnly("porPagar"),true);
		
		builder.appendSeparator("Pago");
		if(model.getValue("pago")!=null){
			builder.append("Pago Id",getControl("pago.id"),true);
		}
		builder.append("Cuenta",getControl("cuenta"));
		builder.append("Fecha",getControl("fechaPago"));
		builder.append("Referencia",getControl("referencia"));
		builder.append("F de Pago",getControl("formaDePago"));		
		builder.append("Comentario",getControl("comentarioPago"),5);
		
		return builder.getPanel();
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuenta".equals(property)){
			JComboBox box=Bindings.createCuentasBinding(getPagoModel().getCuentaHolder());
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("fechaPago".equals(property)){
			JComponent c=Binder.createDateComponent(getPagoModel().getFechaPagoHolder());
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("comentarioPago".equals(property)){
			JComponent c=Binder.createMayusculasTextField(getPagoModel().getComentarioHolder());
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("referencia".equals(property)){
			JComponent c=Binder.createMayusculasTextField(getPagoModel().getReferenciaHolder());
			c.setEnabled(!model.isReadOnly());
			return c;
		}else  if("formaDePago".equals(property)){
			JComboBox box=Bindings.createFormasDePagoBinding(model.getComponentModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		else
			return null;
	}
	
	
	public void doApply() {
		super.doApply();
		getPagoModel().aplicarPago();
	}
	

	public static Requisicion showForm(final Requisicion req){
		PagoDeRequisicionFormModel model=new PagoDeRequisicionFormModel(req);
		PagoDeRequisicionForm form=new PagoDeRequisicionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getRequisicion();
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		final Requisicion r=ServiceLocator2.getRequisiciionesManager().get(27l);
		showForm(r);
	}

}
