package com.luxsoft.sw3.bi.form;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.ui.form.ProductoControl;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.bi.SimuladorDePreciosPorClienteDet;


public class SimuladorDePreciosClienteDetForm extends GenericAbstractForm<SimuladorDePreciosPorClienteDet>{

	public SimuladorDePreciosClienteDetForm(IFormModel model) {
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
		builder.append("Producto", getControl("producto"),true);
		builder.append("Precio",getControl("precio"),true);		
		builder.appendSeparator("Descuentos");
		builder.append("Descuento",getControl("descuento"),true);

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
	
	public static SimuladorDePreciosPorClienteDet showForm(final SimuladorDePreciosPorClienteDet det){
		return showForm(det,false);
	}
	
	public static SimuladorDePreciosPorClienteDet showForm(final SimuladorDePreciosPorClienteDet det, boolean readOnly){
	
		final DefaultFormModel model=new DefaultFormModel(det,readOnly);
		final SimuladorDePreciosClienteDetForm form=new SimuladorDePreciosClienteDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return det;
		}
		return null;
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		final SimuladorDePreciosPorClienteDet lp=new SimuladorDePreciosPorClienteDet();
		Object res=showForm(lp);
		showObject(res);		
	}
	
	
	 
	

}