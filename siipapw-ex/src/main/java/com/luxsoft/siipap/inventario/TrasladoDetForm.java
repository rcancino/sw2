package com.luxsoft.siipap.inventario;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


import com.luxsoft.siipap.inventarios.model.TrasladoDet;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swx.binding.ProductoControl;

public class TrasladoDetForm extends AbstractForm{

	public TrasladoDetForm(IFormModel model) {
		super(model);		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto", getControl("producto"));
		builder.append("Cantidad", getControl("solicitado"));
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else
			return super.createCustomComponent(property);
	}
	
	public static TrasladoDet showForm(TrasladoDet det){
		return showForm(det,false);
	}
	
	public static TrasladoDet showForm(TrasladoDet det,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(det,readOnly);
		TrasladoDetForm form=new TrasladoDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (TrasladoDet)model.getBaseBean();
		}
		return null;
	}
	
	
	

}
