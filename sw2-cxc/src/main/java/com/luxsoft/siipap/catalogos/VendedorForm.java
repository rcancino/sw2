package com.luxsoft.siipap.catalogos;

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
import com.luxsoft.siipap.ventas.model.Vendedor;

public class VendedorForm extends GenericAbstractForm<VendedorForm>{

	private JFormattedTextField tfRfc;
	MaskFormatter formatter;
	
	
	public VendedorForm(IFormModel model) {
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
		builder.setBorder(new TitledBorder("Datos Vendedor"));
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
	
	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Catalogo de Vendedores","Captura de Datos de Vendedor");
	}
	
	
	public static Vendedor showForm(Vendedor bean){
		return showForm(bean,false);
	}
	
	public static Vendedor showForm(Vendedor bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final VendedorForm form=new VendedorForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Vendedor)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Vendedor());
		VendedorForm.showObject(bean);
		System.exit(0);
	}
	
	
	
	
	

}
