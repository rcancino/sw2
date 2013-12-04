package com.luxsoft.siipap.compras.ui;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swx.binding.ProductoControl;


public class ListaDePreciosDetBulkForm extends GenericAbstractForm<ListaDePreciosDet>{
	
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
		builder.append("Desc 1",getControl("descuento1"),true);
		builder.append("Desc 2",getControl("descuento2"),true);
		builder.append("Desc 3",getControl("descuento3"),true);
		builder.append("Desc 4",getControl("descuento4"),true);
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel("producto"));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if(property.startsWith("descuento")){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}else
			return null;
	}	
	
	public static ListaDePreciosDet showForm(final ListaDePreciosDet det){
		return showForm(det,false);
	}
	
	public static ListaDePreciosDet showForm(final ListaDePreciosDet det, boolean readOnly){
	
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
		final ListaDePreciosDet lp=new ListaDePreciosDet();
		Object res=showForm(lp);
		showObject(res);		
	}

}
