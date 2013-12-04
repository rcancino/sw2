package com.luxsoft.siipap.tesoreria.catalogos;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;


/**
 * Forma para el mantenimiento de Bancos
 * 
 * @author Ruben Cancino
 *
 */
public class CuentaForm extends GenericAbstractForm<Cuenta>{

	public CuentaForm(IFormModel model) {
		super(model);
		setTitle("Cuentas Bancarias");
		//model.getModel("cuentaContable").addValueChangeListener(new CuentaHandler());
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Catálogo de Cuentas","Mantenimiento al catálogo de Cuentas bancarias");
	}

	/*
	 * 
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,70dlu, 2dlu," +
				"p,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		//if(model.getValue("id")!=null)
			//builder.append("Id",getControl("id"),true);
		
		builder.append("Clave",getControl("clave"));
		builder.append("Banco",getControl("banco"));
		
		builder.append("Numero",addMandatory("numero"));
		builder.append("Moneda",getControl("moneda"));
		
		builder.append("Tipo",getControl("tipo"));
		builder.append("Cuenta Contable",getControl("cuentaContable"));
		
		
		builder.append("Descripción",getControl("descripcion"),5);
		if(model.getValue("id")==null)			
			builder.append("Encriptada",getControl("encriptar"),true);
		
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("tipo".equals(property)){
			return createTipoDeCuenta(model.getModel(property));
		}else if("clave".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			ValidationComponentUtils.setMandatory(control,true);
			return control;
		}else if("cuentaContable".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			ValidationComponentUtils.setMandatory(control,true);
			return control;
		}else if("banco".equals(property)){
			return com.luxsoft.siipap.swing.binding.Bindings.createBancosBinding(model.getModel(property));
		}else if("numero".equals(property)){
			return BasicComponentFactory.createLongField(model.getComponentModel(property));			
		}
		return null;
	}
	
	public static  JComboBox createTipoDeCuenta(final ValueModel vm){
		final SelectionInList list=new SelectionInList(Cuenta.Clasificacion.values(),vm);
		final JComboBox box=BasicComponentFactory.createComboBox(list);		
		return box;
	}
	
	
	public static Cuenta showForm(Cuenta bean){
		return showForm(bean,false);
	}
	
	public static Cuenta showForm(Cuenta bean,boolean readOnly){
		CuentaModel model=new CuentaModel(bean);
		model.setReadOnly(readOnly);
		final CuentaForm form=new CuentaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Cuenta)model.getBaseBean();
		}
		return null;
	}
	
	
	
	private static class CuentaModel extends DefaultFormModel{
		
		private List<Cuenta> cuentas;

		public CuentaModel() {
			this(new Cuenta());
		}
		public CuentaModel(Cuenta cta){
			super(cta);
		}
		
		@Override
		protected void init() {
			cuentas=ServiceLocator2.getLookupManager().getCuenta();
		}

		protected void addValidation(PropertyValidationSupport support){
			Cuenta c=buscarCuentaPorClave(getCuenta().getClave());
			if(c!=null){
				support.addError("clave", "Clave ya asignada");
				return;
			}
			c=buscarCuentaPorCuentaContable(getCuenta().getCuentaContable());
			if(c!=null){
				support.addError("cuentaContable", "La cuenta contable ya se ha asignado a la cuenta:\n "+c.getClave());
				return;
			}
		}
		
		private Cuenta getCuenta(){
			return (Cuenta)getBaseBean();
		}
		
		
		public Cuenta buscarCuentaPorClave(final String clave){
			Cuenta c=(Cuenta) CollectionUtils.find(cuentas, new Predicate(){
				public boolean evaluate(Object object) {
					if(object.equals(getCuenta()))
						return false;
					Cuenta c=(Cuenta)object;
					return c.getClave().equalsIgnoreCase(clave);
				}				
			});
			return c;
		}
		public Cuenta buscarCuentaPorCuentaContable(final String cuenta){
			Cuenta c=(Cuenta) CollectionUtils.find(cuentas, new Predicate(){
				public boolean evaluate(Object object) {
					if(object.equals(getCuenta()))
						return false;
					Cuenta c=(Cuenta)object;					
					return c.getCuentaContable().equalsIgnoreCase(cuenta);
				}				
			});
			
			return c;
		}
		
		
	}
	
	public static void main(String[] args) {		
		Object bean=showForm(new Cuenta());
		CuentaForm.showObject(bean);
		System.exit(0);
	}

}
