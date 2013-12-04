package com.luxsoft.siipap.tesoreria.catalogos;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.view.ValidationComponentUtils;

import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;


/**
 * Forma para el mantenimiento de Bancos
 * 
 * @author Ruben Cancino
 *
 */
public class ConceptoForm extends GenericAbstractForm<Concepto>{

	public ConceptoForm(IFormModel model) {
		super(model);
		setTitle("Conceptos de Ingreso/Egreso");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Catálogo de Conceptos","Mantenimiento al catálogo de conceptos de ingreso/egreso");
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Clave",getControl("clave"),true);
		builder.append("Descripción",getControl("descripcion"),5);
		builder.append("Tipo",getControl("tipo"),true);
		builder.append("Clase",getControl("clase"),true);
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			return Bindings.createConceptosDeIngresosEgresosBinding(model.getModel(property));
		}else if("clave".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			ValidationComponentUtils.setMandatory(control,true);
			return control;
		}else if("clase".equals(property)){
			SelectionInList sl=new SelectionInList(Concepto.Clase.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return null;
	}
	
	
	public static Concepto showForm(Concepto bean){
		return showForm(bean,false);
	}
	
	public static Concepto showForm(Concepto bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ConceptoForm form=new ConceptoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Concepto)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Concepto());
		ConceptoForm.showObject(bean);
		System.exit(0);
	}

}
