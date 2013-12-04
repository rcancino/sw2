package com.luxsoft.siipap.gastos.operaciones;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.ActivoFijo;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para el mantenimiento manual de instancias de {@link ActivoFijo}
 * 
 * @author Ruben Cancino
 *
 */
public class AF_ManualForm extends GenericAbstractForm<ActivoFijo>{
	
	
	public AF_ManualForm(IFormModel model) {
		super(model);
		setTitle("Inventario de activo fijo");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Activo Fijo","Lista de activos registrados");
	}	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;100dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos principales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Departamento",getControl("departamento"));
		
		
		builder.append("Proveedor",getControl("proveedor"),5);
		builder.append("Producto",getControl("producto"),5);
		
		builder.append("MOI",getControl("moi"));
		builder.append("INPC",getControl("inpcOriginal"));
		
		builder.append("Fecha Adq",getControl("fechaDeAdquisicion"));
		builder.append("Documento",getControl("documento"));
		
		builder.append("Tasa (Dep)",getControl("tasaDepreciacion"),true);
		builder.append("Clasificación",getControl("clasificacion"),5);
		/*
		builder.appendSeparator("Ultima Actualización ");
		builder.append("Depreciación (Acu)",getControl("depreciacionAcumulada"));
		builder.append("Ultimo INPC",getControl("ultimoINPC"));
		builder.append("Fecha (Act)",getControl("fechaActualizacion"),true);
		*/
		builder.appendSeparator();		
		builder.append("Responsable",getControl("consignatario"),5);
		builder.append("Comentario",getControl("comentario"),5);
		
		return builder.getPanel();
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("proveedor".equals(property)){
			ProveedorControl box=new ProveedorControl(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("producto".equals(property)){
			ProductoControl control=new ProductoControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("departamento".equals(property)){
			JComboBox box=Bindings.createDepartamentosBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("inpcOriginal".equals(property) || "ultimoINPC".equals(property)){
			INPCControl control=new INPCControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("clasificacion".equals(property)){
			JComboBox box=Bindings.createClasificacionDeActivoFijoBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("consignatario".equals(property)){
			ConsignatarioControl control=new ConsignatarioControl(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}
		return null;
	}
	
		
	
	public static ActivoFijo showForm(ActivoFijo bean){
		return showForm(bean,false);
	}
	
	public static ActivoFijo showForm(ActivoFijo bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final AF_ManualForm form=new AF_ManualForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ActivoFijo)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new ActivoFijo());
		AF_ManualForm.showObject(bean);
		System.exit(0);
	}

}
