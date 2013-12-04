package com.luxsoft.siipap;

import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;


/**
 * Forma para el mantenimiento de instancias de {@link Empresa}
 * 
 * @author Ruben Cancino
 *
 */
public class EmpresaForm extends GenericAbstractForm<Empresa>{
	
	

	public EmpresaForm(IFormModel model) {
		super(model);
		setTitle("Empresa");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Empresa","Mantenimiento de datos para la empresa");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Clave",getControl("clave"),true);
		builder.append("Nombre",getControl("nombre"),5);
		builder.append("Descripción",getControl("descripcion"),5);
		builder.append("RFC",getControl("rfc"),true);
		builder.append("Dirección",getControl("direccion"),5);
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)||"nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}else if("rfc".equals(property)){
			try {
				MaskFormatter formatter=new MaskFormatter("UUU-######-AA#"){
					public Object stringToValue(String value) throws ParseException {
						String nval=StringUtils.upperCase(value);						
						return super.stringToValue(nval);						
					}
					
				};
				formatter.setValueContainsLiteralCharacters(false);
				formatter.setPlaceholderCharacter('_');
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(true);
				return BasicComponentFactory.createFormattedTextField(model.getComponentModel(property), formatter);
			} catch (Exception e) {
				logger.error(e);
				return null;
			}
		}
		return null;
	}
	
	
	
	public static Empresa showForm(Empresa bean){
		return showForm(bean,false);
	}
	
	public static Empresa showForm(Empresa bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final EmpresaForm form=new EmpresaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Empresa)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new Empresa());
		EmpresaForm.showObject(bean);
		System.exit(0);
	}

}
