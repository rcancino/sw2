package com.luxsoft.sw3.maquila.ui.catalogos;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;

import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.maquila.model.Almacen;



/**
 * Forma para el mantenimiento de instancias de {@link Almacen}
 * 
 * @author Ruben Cancino
 *
 */
public class AlmacenForm extends GenericAbstractForm<Almacen>{
	
	

	public AlmacenForm(IFormModel model) {
		super(model);
		setTitle("Mantenimiento de Almacén de maquilador");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Almacén ","Almacén de maquilador");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:150dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("nombre").setEnabled(false);
		}
		builder.append("Nombre",addMandatory("nombre"),true);
		
		builder.append("Maquilador",getControl("maquilador"),true);
		builder.append("Dirección",getControl("direccion"));
		builder.append("Teléfono 1",getControl("telefono1"));
		builder.append("Teléfono 2",getControl("telefono2"));
		builder.append("Fax",getControl("fax"));
		builder.nextLine();
		builder.append("Dirección",getControl("direccion"));
		
		return builder.getPanel();
	}
	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
		if(model.getValue("id")!=null)
			setInitialComponent(getControl("nombre"));
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}else if("maquilador".equals(property)){
			JComboBox box=createProveedorLookup(model.getModel(property));
			if(model.getValue("id")!=null)
				box.setEnabled(false);
			return box;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected  JComboBox createProveedorLookup(final ValueModel vm) {
		final JComboBox box = new JComboBox();		
		final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave", "nombre", "rfc" });
		EventList maquiladores=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find("from Proveedor p where p.maquilador=true"));
		AutoCompleteSupport support = AutoCompleteSupport.install(box,maquiladores, filterator);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		
		return box;
		
	}
	
	public static Almacen showForm(Almacen bean){
		return showForm(bean,false);
	}
	
	public static Almacen showForm(Almacen bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final AlmacenForm form=new AlmacenForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Almacen)model.getBaseBean();
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
				Object bean=showForm(new Almacen());
				AlmacenForm.showObject(bean);
				System.exit(0);
				
			}

		});
	}
	
	

}
