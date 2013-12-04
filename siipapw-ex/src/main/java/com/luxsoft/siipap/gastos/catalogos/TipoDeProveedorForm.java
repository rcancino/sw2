package com.luxsoft.siipap.gastos.catalogos;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;

import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;

/**
 * Forma para el mantenimiento de instancias de {@link GTipoProveedor}
 * 
 * @author Ruben Cancino
 *
 */
public class TipoDeProveedorForm extends GenericAbstractForm<GTipoProveedor>{
	
	private JCheckBox root;

	public TipoDeProveedorForm(IFormModel model) {
		super(model);
		setTitle("Clasificación de Proveedores");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Clasificación","Clasificación de proveedores");
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
		root=new JCheckBox("",false);
		root.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				JComboBox box=(JComboBox)getControl("parent");
				box.setEnabled(!root.isSelected());
				if(root.isSelected())
					box.setSelectedItem(null);
				
			}			
		});
		root.setSelected(model.getValue("parent")==null);
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		
		builder.append("Clave",addMandatory("clave"));
		builder.append("Primer nivel",root);
		
		builder.append("Padre",getControl("parent"),5);
		builder.append("Descripción",addMandatory("descripcion"),5);
		
		getControl("clave").addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				validarClave();
			}			
		});
		return builder.getPanel();
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}else if("parent".equals(property)){
			return Bindings.createTipoProveedorBinding(model.getModel(property));
		}
		return null;
	}
	
	private void validarClave(){
		if(model.getValue("clave")==null) return;
		if(model.getValue("id")!=null) return;
		String clave=model.getValue("clave").toString();
		JComboBox box=(JComboBox)getControl("parent");
		for(int i=0;i<box.getModel().getSize();i++){
			GTipoProveedor g=(GTipoProveedor)box.getModel().getElementAt(i);
			if(clave.equalsIgnoreCase(g.getClave())){
				MessageUtils.showMessage(getContentPane(),"La clace ya esta registrada", "Validando");
				model.setValue("clave", null);
				getControl("clave").requestFocusInWindow();
			}
		}		
	}
	
	
	public static GTipoProveedor showForm(GTipoProveedor bean){
		return showForm(bean,false);
	}
	
	public static GTipoProveedor showForm(GTipoProveedor bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final TipoDeProveedorForm form=new TipoDeProveedorForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (GTipoProveedor)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {		
		Object bean=showForm(new GTipoProveedor());
		TipoDeProveedorForm.showObject(bean);
		System.exit(0);
	}

}
