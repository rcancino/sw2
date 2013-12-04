package com.luxsoft.siipap.gastos.catalogos;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.gastos.Consignatario;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;

/**
 * Forma para el mantenimiento de instancias de {@link Consignatario}
 * 
 * @author Ruben Cancino
 *
 */
public class ConsignatarioForm extends GenericAbstractForm<Consignatario>{
	

	public ConsignatarioForm(IFormModel model) {
		super(model);
		setTitle("Consignatario de Activo Fijo");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Consignatario","Responsable del Activos");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu, 2dlu," +
				"p,2dlu,70dlu","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		builder.append("Apellido Pat",getControl("apellidoP"),5);
		builder.append("Apellido Mat",getControl("apellidoM"),5);
		builder.append("Nombre(s)",getControl("nombres"),5);
		
		builder.append("Sucursal",getControl("sucursal"));
		builder.append("Departamento",getControl("departamento"));
		
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombres".equals(property) || "apellidoP".equals(property) || "apellidoM".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property));
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
		}
		return null;
	}
	
	public static Consignatario showForm(Consignatario bean){
		return showForm(bean,false);
	}
	
	public static Consignatario showForm(Consignatario bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ConsignatarioForm form=new ConsignatarioForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Consignatario)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Consignatario());
		ConsignatarioForm.showObject(bean);
		System.exit(0);
	}

}
