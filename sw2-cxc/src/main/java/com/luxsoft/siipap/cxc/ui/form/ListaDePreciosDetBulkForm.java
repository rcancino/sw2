package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

public class ListaDePreciosDetBulkForm extends GenericAbstractForm<ListaDePreciosClienteDet>{
	
	public ListaDePreciosDetBulkForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		final JPanel panel=new JPanel(new BorderLayout(2,5));
		panel.add(buildForm(),BorderLayout.CENTER);		
		return panel;
	}
	
	protected JComponent buildForm() {
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu,p:g" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		//builder.append("Producto", getControl("producto"),2);
		//builder.append("Precio",getControl("precio"),1);
		builder.appendSeparator("Descuentos");
		builder.append("Descuento",getControl("descuento"),true);

		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
	/*	if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel("producto"));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if(property.startsWith("descuento")){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}else*/
			return null;
	}	
	
	public static ListaDePreciosClienteDet showForm(final ListaDePreciosClienteDet det){
		return showForm(det,false);
	}
	
	public static ListaDePreciosClienteDet showForm(final ListaDePreciosClienteDet det, boolean readOnly){
	
		final DefaultFormModel model=new DefaultFormModel(det,readOnly);
		final ListaDePreciosDetBulkForm form=new ListaDePreciosDetBulkForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return det;
		}
		return null;
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		final ListaDePreciosClienteDet lp=new ListaDePreciosClienteDet();
		Object res=showForm(lp);
		showObject(res);		
	}

}
