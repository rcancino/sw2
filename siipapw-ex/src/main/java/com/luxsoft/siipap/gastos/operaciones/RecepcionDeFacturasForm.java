package com.luxsoft.siipap.gastos.operaciones;

import javax.swing.JComponent;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.binding.CantidadMonetariaControl;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Permite registrar una facturas a una compra
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeFacturasForm extends AbstractForm{

	public RecepcionDeFacturasForm(RecepcionDeFacturasFormModel model) {
		super(model);
		
	}
	
	@SuppressWarnings("unused")
	private RecepcionDeFacturasFormModel getMainModel(){
		return (RecepcionDeFacturasFormModel)model;
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout("50dlu,2,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Orden de Compra");
		builder.append("Compra",addReadOnly("compra"));
		builder.append("Proveedor",addReadOnly("proveedor"));
		builder.append("Total ",addReadOnly("totalPedido"));
		builder.append("Pendiente ",addReadOnly("pendientePedido"));
		
		builder.appendSeparator("CXPFactura");
		builder.append("Fecha",getControl("fecha"));
		builder.append("Documento",getControl("documento"));
		builder.append("Importe F",getControl("total"));
		builder.append("A Pagar",getControl("apagar"));
		return builder.getPanel();
	}
	
	public static GCompra showForm(final GCompra compra){
		final GFacturaPorCompra fac=compra.crearCuentaPorPagar();
		if(fac==null)
			return null;
		fac.setCompra(compra);
		//fac.setTotal(compra.getSaldoPorRevisar());
		final RecepcionDeFacturasFormModel model=new RecepcionDeFacturasFormModel(fac);
		RecepcionDeFacturasForm form=new RecepcionDeFacturasForm(model);
		try {
			form.open();
			if(!form.hasBeenCanceled()){
				//fac.actualizarSaldo();
				compra.agregarFactura(fac);
				return compra;
			}
			form=null;
			return null;
			
		} catch (Exception e) {
			MessageUtils.showError("Error registrando",e);
			return null;
		}
		
		
	}
	
	private JTextField idField;
	
	
	@Override
	protected JComponent createCustomComponent(String property) {		
		if("compra".equals(property)){
			idField=new JTextField();
			idField.setHorizontalAlignment(JTextField.RIGHT);
			idField.setText(String.valueOf(model.getValue("compra.id")));
			return idField;
		}else if("total".equals(property)|| "apagar".equals(property)){
			CantidadMonetariaControl control=(CantidadMonetariaControl)Bindings.createCantidadMonetariaBinding(model.getComponentModel(property));
			control.enableMonedaSelector(false);
			return control;
		}
		return super.createCustomComponent(property);
	}
	
	

	public static void main(String[] args) {
		final GCompra compra=ServiceLocator2.getGCompraDao().get(6l);
		showForm(compra);
		System.exit(0);
	}

}
