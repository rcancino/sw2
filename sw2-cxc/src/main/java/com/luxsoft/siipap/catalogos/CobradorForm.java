package com.luxsoft.siipap.catalogos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Cobrador;

public class CobradorForm extends GenericAbstractForm<Cobrador>{

	private JFormattedTextField tfRfc;
	MaskFormatter formatter;
	
	public CobradorForm(IFormModel model) {
		super(model);
	}
	
	@Override
	protected JComponent buildFormPanel() {
		
		return buildForm();
	}

	private JComponent buildForm(){
		FormLayout layout=new FormLayout(
				"40dlu,5dlu,70dlu,10dlu," +
				"5dlu,5dlu,5dlu," +
				"40dlu,5dlu,70dlu,10dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setBorder(new TitledBorder("Datos Cobrador"));
		builder.append("Nombre",getControl("nombres"),9);
		builder.nextLine();
		builder.append("Apellido P",getControl("apellidoP"),2);
		builder.nextColumn(2);
		builder.append("Apellido M",getControl("apellidoM"),2);
		builder.nextLine();
		builder.append("RFC",getControl("rfc"),2);
		builder.nextColumn(2);
		builder.append("CURP",getControl("curp"),2);
		
		return builder.getPanel();
	}
	
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("rfc".equals(property)){
			try {
				formatter=new MaskFormatter("UUU-######-AAA"){
					public Object stringToValue(String value) throws ParseException {
						String nval=StringUtils.upperCase(value);						
						return super.stringToValue(nval);						
					}
					
				};
				formatter.setValueContainsLiteralCharacters(false);
				formatter.setPlaceholderCharacter('_');
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(false);
				tfRfc=BasicComponentFactory.createFormattedTextField(model.getComponentModel(property), formatter);
														
				return tfRfc;
			} catch (Exception e) {
				
				return null;
				
			}
			
		}
		return null;
	}
	
	
	
	
	
	private void sincronizarRfcFormatter(){		
		if(model.getValue("personaFisica").equals(Boolean.TRUE)){
			try {
				formatter.setMask("UUUU-######-AAA");
				
			} catch (ParseException e) {				
				e.printStackTrace();
			}
		}else{
			try {
				formatter.setMask("UUU-######-AAA");
			} catch (ParseException e) {				
				e.printStackTrace();
			}
		}		
	}

	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Catalogo de Cobradores","Captura de Datos de Cobrador");
	}
	
	
	public static Cobrador showForm(Cobrador bean){
		return showForm(bean,false);
	}
	
	public static Cobrador showForm(Cobrador bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final CobradorForm form=new CobradorForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Cobrador)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Cobrador());
		CobradorForm.showObject(bean);
		System.exit(0);
	}

	
	
}
