package com.luxsoft.siipap.gastos.catalogos;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

/**
 * Forma para el mantenimiento de instancias de {@link GTipoProveedor}
 * 
 * @author Ruben Cancino
 *
 */
public class ProveedorDeGastosForm extends GenericAbstractForm<GProveedor>{
	
	private JTabbedPane tabPanel;

	public ProveedorDeGastosForm(IFormModel model) {
		super(model);
		setTitle("Catálogo de proveedores");
		
		model.getComponentModel("personaFisica").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				sincronizarNombre();
			}			
		});
		model.addBeanPropertyChangeListener(new ValidationHelper());	
		
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Proveedor ","De bienes y servicios");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("General", buildGeneralForm());
		tabPanel.addTab("Dirección", buildSecondaryForm());
		tabPanel.addTab("Cuenta", buildCuentaForm());
		sincronizarNombre();
		return tabPanel;
	}
	
	private JComponent buildGeneralForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;70dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos principales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		builder.append("Persona Física",addMandatory("personaFisica"),true);
		builder.append("Razón Social",getControl("nombre"),5);
		setInitialComponent(getControl("nombre"));
		builder.append("Apellido P",getControl("apellidoP"));
		builder.append("Apellido M",getControl("apellidoM"));
		builder.append("Nombre(s)",getControl("nombres"),5);
		builder.append("RFC",getControl("rfc"),true);
		builder.append("Tipo",getControl("tipo"),true);
		builder.append("Contacto 1",getControl("contacto1"),5);
		builder.append("Contacto 2",getControl("contacto2"),5);
		
		builder.append("Telefono 1",getControl("telefono1"));
		builder.append("Telefono 2",getControl("telefono2"));
		
		builder.append("Telefono 3",getControl("telefono3"),true);
		builder.append("Email-1",getControl("email1"));
		builder.append("Email-2",getControl("email2"));
		builder.append("http://www:",getControl("www"),5);
		
		return builder.getPanel();
	}
	
	private JComponent buildSecondaryForm(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Direccion");		
		builder.append(getControl("direccion"),7);
		return builder.getPanel();		
	}
	
	private JComponent buildCuentaForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;70dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos de crédito");
		builder.append("Lína de Crédito",getControl("credito"),true);
		builder.append("Plazo",getControl("plazo"),true);
		builder.append("Nacional",getControl("nacional"),true);	
		builder.append("T.Entrega(Días)",getControl("tiempoDeEntrega"),true);
		return builder.getPanel();
	}
	
	private JFormattedTextField tfRfc;
	
	MaskFormatter formatter;
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			JComboBox box= Bindings.createTipoTipoDeProveedorBinding(model.getModel("tipo"));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("rfc".equals(property)){
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
				
				model.getComponentModel("personaFisica").addPropertyChangeListener(new PropertyChangeListener(){
					public void propertyChange(PropertyChangeEvent evt) {
						sincronizarRfcFormatter();
					}					
				});
								
				return tfRfc;
			} catch (Exception e) {
				
				return null;
				
			}
			
		}else if("nombre".equals(property) || "nombres".equals(property) || "apellidoP".equals(property) || "apellidoM".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
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
	
	
	/**
	 * Valida que la razon social no exista
	 *
	 */
	private void validarNombre(){
		final String nombre=(String)model.getValue("nombre");
		if(nombre==null || nombre.isEmpty()) 
			return;  //NO VALIDAMOS		
		if(model.getValue("personaFisica").equals(Boolean.TRUE)){
			return ;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("LostFocus: Validando que el nombre no exista en la base de datos");
		}
		/*
		GProveedor found=GastosModel.instance().buscarProveedorPorNombre(nombre);
		if(found==null)
			return;
		if(nombre.equalsIgnoreCase(found.getNombre())){
			MessageUtils.showMessage(getContentPane(),"El nombre ya esta registrado", "Validando");
			model.setValue("nombre", "");
			getControl("nombre").requestFocusInWindow();
		}*/
						
	}
	
	/**
	 * Valida que el RFC no exista ya en la base de datos
	 * 
	 *
	 */
	private void validarRfc(){		
		
		
		final String rfc=(String)model.getValue("rfc");
		if(rfc==null || rfc.isEmpty()) 
			return;  //NO VALIDAMOS
		if(logger.isDebugEnabled()){
			logger.debug("LostFocus: Validando que el RFC no exista en la base de datos");
		}
		/*
		GProveedor found=GastosModel.instance().buscarProveedorPorRfc(rfc);
		if(found==null)
			return;
		if(rfc.equalsIgnoreCase(found.getNombre())){
			MessageUtils.showMessage(getContentPane(),"El RFC ya esta registrado para el proveedor: "+found.getNombre(), "Validando");
			model.setValue("rfc", "");
			getControl("rfc").requestFocusInWindow();
		}
		*/
	}
	
	private void sincronizarNombre(){
		
		Boolean val=(Boolean)model.getValue("personaFisica");
		model.getComponentModel("nombres").setEnabled(val);
		model.getComponentModel("apellidoP").setEnabled(val);
		model.getComponentModel("apellidoM").setEnabled(val);		
		model.getComponentModel("nombre").setEnabled(!val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("nombres"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("apellidoP"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("apellidoM"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("nombre"),!val);
		//updateComponentTreeMandatoryAndSeverity();
		
		if(val){			
			//model.setValue("nombre", "");
			getControl("nombre").requestFocusInWindow();
		}
		else{
			//model.setValue("nombres", "");
			//model.setValue("apellidoM", "");
			//model.setValue("apellidoP", "");			
			getControl("apellidoP").requestFocusInWindow();
		}
		
		
	}
	
	private class ValidationHelper implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			if("nombre".equals(evt.getPropertyName())){
				validarNombre();
			}else if("rfc".equals(evt.getPropertyName())){
				validarRfc();
			}
			
		}
		
	}
	
	
	
	public static GProveedor showForm(GProveedor bean){
		return showForm(bean,false);
	}
	
	public static GProveedor showForm(GProveedor bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ProveedorDeGastosForm form=new ProveedorDeGastosForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (GProveedor)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new GProveedor());
		ProveedorDeGastosForm.showObject(bean);
		System.exit(0);
	}

}
