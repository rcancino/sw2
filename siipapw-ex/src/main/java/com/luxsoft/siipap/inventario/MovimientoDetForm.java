package com.luxsoft.siipap.inventario;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swx.binding.ProductoControl;

public class MovimientoDetForm extends AbstractForm{

	public MovimientoDetForm(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto", getControl("producto"));
		builder.append("Cantidad", getControl("cantidad"));
		builder.append("Comentario",getControl("comentario"));
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
	
	public static MovimientoDet showForm(MovimientoDet det){
		return showForm(det,false);
	}
	
	public static MovimientoDet showForm(MovimientoDet det,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(det,readOnly);
		MovimientoDetForm form=new MovimientoDetForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (MovimientoDet)model.getBaseBean();
		}
		return null;
	}
	
	
	

}
