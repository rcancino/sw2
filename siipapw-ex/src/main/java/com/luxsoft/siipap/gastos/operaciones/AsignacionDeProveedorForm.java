package com.luxsoft.siipap.gastos.operaciones;

import java.math.BigDecimal;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;

public class AsignacionDeProveedorForm extends AbstractForm{

	public AsignacionDeProveedorForm(IFormModel model) {
		super(model);
		setTitle("Asignaci—n de Proveedor-Factura");
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,4dlu,70dlu, 2dlu," +
				"p,4dlu,70dlu, 2dlu," +
				"p,4dlu,70dlu:g " 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Factura", getControl("facturaRembolso"));
		builder.append("Proveedor", getControl("proveedorRembolso"),5);
		return builder.getPanel();
	}
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedorRembolso".equals(property)){
			ProveedorControl control=new ProveedorControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			control.setEnabled(model.getValue("id")==null);
			return control;
		}
		return null;
	}
	
	public static GCompraDet showForm(){
		GCompraDet det=new GCompraDet();
		det.setCantidad(BigDecimal.ONE);
		det.setPrecio(BigDecimal.ONE);
		det.setProducto(new GProductoServicio());
		DefaultFormModel model=new DefaultFormModel(det);
		AsignacionDeProveedorForm form=new AsignacionDeProveedorForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (GCompraDet)model.getBaseBean();
		}
		return null;
	}

}
