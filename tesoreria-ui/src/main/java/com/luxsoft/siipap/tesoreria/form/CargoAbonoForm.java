package com.luxsoft.siipap.tesoreria.form;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto.Clase;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.model.tesoreria.Concepto.Tipo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Fomra para el mantenimiento de {@link CargoAbono}
 * @author Ruben Cancino Ramos
 *
 */
public class CargoAbonoForm extends AbstractForm{
	
	

	public CargoAbonoForm(CargoAbonoFormModel model) {
		super(model);
		setTitle("Retiro bancario");
		if(model.isDeosito())
			setTitle("Deposito bancario");
		
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,p:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Sucursal",getControl("sucursal"));
		if(model.getValue("id")!=null){
			builder.append("Folio",addReadOnly("id"));
		}
		builder.nextLine();
		builder.append("Fecha",addMandatory("fecha"));
		builder.append("Concepto",getControl("concepto"));
				
		builder.append("Importe",getControl("importe"));
		builder.append("Moneda",getControl("moneda"));
		
		builder.append("Tipo de Cambio",getControl("tc"));
		builder.append("Tipo",getControl("formaDePago"));
		
		builder.append("Referencia",getControl("referencia"),5);
		builder.append("Cuenta",getControl("cuenta"),5);
		
		builder.append("Comentario",getControl("comentario"),5);		
		builder.append("Origen",addReadOnly("origen"),true);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("formaDePago".equals(property)){
			return Bindings.createFormasDeCargoAbonoBinding(model.getModel(property));
		}else if("concepto".equals(property)){
			Tipo tipo=getBaseModel().isDeosito()?Tipo.ABONO:Tipo.CARGO;
			JComboBox box= Bindings.createConceptosDeAbonoBinding(model.getModel(property),tipo,Clase.TESORERIA1);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("cuenta".equals(property)){
			JComboBox box= Bindings.createCuentasBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("comentario".equals(property)){
			return BasicComponentFactory.createTextField(model.getComponentModel(property), true);
		}else if("sucursal".equals(property)){
			JComboBox box= Bindings.createSucursalesBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("origen".equals(property)){
			SelectionInList sl=new SelectionInList(Origen.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	
	public CargoAbonoFormModel getBaseModel(){
		return (CargoAbonoFormModel)model;
	}
	
	
	public static CargoAbono showForm(CargoAbono ca){
		final CargoAbonoFormModel model=new CargoAbonoFormModel(ca);
		final CargoAbonoForm form=new CargoAbonoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return  model.commit();
		}
		return null;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				DBUtils.whereWeAre();
				CargoAbono ca=ServiceLocator2.getCargoAbonoDao().get(759179L);
				System.out.println("Cuenta: "+ca.getCuenta());
				showForm(ca);
				System.exit(0);
			}

		});
	}


}
