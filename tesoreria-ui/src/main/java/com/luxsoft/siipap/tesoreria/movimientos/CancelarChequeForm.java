package com.luxsoft.siipap.tesoreria.movimientos;

import java.math.BigDecimal;
import java.util.Date;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Autorizacion;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class CancelarChequeForm extends AbstractForm{

	public CancelarChequeForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,p","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cuenta",addMandatory("cuenta"));
		builder.append("Numero",addMandatory("referencia"));
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuenta".equals(property)){
			JComponent c=Bindings.createCuentasBinding(model.getModel("cuenta"));
			return c;
		}
		return null;
	}
	
	public static CargoAbono showForm(){
		CargoAbono pago=CargoAbono.crearCargoAbono(BigDecimal.ZERO, new Date(), "CANCELADO");
		pago.setAFavor("CANCELADO");
		pago.setFormaDePago(FormaDePago.CHEQUE);
		pago.setOrigen(Origen.TESORERIA);
		pago.setAutorizacion(new Autorizacion());		
		DefaultFormModel model=new DefaultFormModel(pago);
		final CancelarChequeForm form=new CancelarChequeForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (CargoAbono)form.getModel().getBaseBean();
		}
		return null;
	}
	
	public static void main(String[] args) {
		CargoAbono pago=showForm();
		System.out.println(pago);
	}

}
