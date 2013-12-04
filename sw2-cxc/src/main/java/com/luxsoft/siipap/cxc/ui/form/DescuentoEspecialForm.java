package com.luxsoft.siipap.cxc.ui.form;

import java.text.MessageFormat;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.ui.model.DescuentoEspecialFormModel;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;


public class DescuentoEspecialForm extends AbstractForm{

	public DescuentoEspecialForm(DescuentoEspecialFormModel model) {
		super(model);
	}
	
	private DescuentoEspecialFormModel getMainModel(){
		return (DescuentoEspecialFormModel)model;
	}
	
	HeaderPanel header;
	
	protected JComponent buildHeader() {
		header=new HeaderPanel(
				getMainModel().getCargo().getCliente().getNombreRazon(),
				getInfo()
				);
		return header;
	}
	
	private String getInfo(){
		String pattern="CXPFactura: {0} Fecha: {1,date,short} Importe: {2} {3}";
		return MessageFormat.format(pattern
				, getMainModel().getCargo().getDocumento()
				, getMainModel().getCargo().getFecha()
				, getMainModel().getCargo().getTotal()
				, StringUtils.trimToEmpty(getMainModel().getCargo().getComentario())
				);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,p,3dlu,p,2dlu,f:150dlu",
				""
				);
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Descuento",getControl("descuento"),true);
		builder.append("Comentario",getControl("comentarioDescuento"),5);
		builder.appendSeparator("Autorización");
		builder.append("Fecha",addReadOnly("fecha"),true);
		builder.append("Comentario",getControl("comentarioAutorizacion"),5);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if(property.startsWith("comentario")){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;
			
		}else if(property.startsWith("descuento")){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}
		else return null;
	}

}
