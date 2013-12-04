package com.luxsoft.siipap.inventario.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swx.binding.ProductoControl;

/**
 * Forma del Detalle De Maquila
 * 
 * @author OCTAVIO
 *
 */

public class OrdenDeMaquilaDetForm extends AbstractForm{

	public OrdenDeMaquilaDetForm(IFormModel model) {
		super(model);
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto", getControl("producto"));
		builder.append("Cantidad",getControl("cantidad"));
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			
			return control;
		}
		return super.createCustomComponent(property);
	}
	
	public static EntradaDeMaquila showForm(EntradaDeMaquila det){
		return showForm(det,false);
	}
	
	public static EntradaDeMaquila showForm(EntradaDeMaquila det,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(det,readOnly);
		OrdenDeMaquilaDetForm form=new OrdenDeMaquilaDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (EntradaDeMaquila)model.getBaseBean();
		}
		return null;
	}
	
	
	

}
