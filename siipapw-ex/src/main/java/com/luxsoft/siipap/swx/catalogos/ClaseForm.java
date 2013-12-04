package com.luxsoft.siipap.swx.catalogos;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Clase;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;



/**
 * Forma para el mantenimiento de instancias de {@link GProductoServicio}
 * 
 * @author Ruben Cancino
 *
 */
public class ClaseForm extends GenericAbstractForm<Clase>{
	
	

	public ClaseForm(IFormModel model) {
		super(model);
		setTitle("Mantenimiento de Clases");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Clase","Clasificación de productos mediante clases ");
	}

	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.swing.form2.AbstractForm#buildFormPanel()
	 */
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;100dlu)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
		}		
		builder.append("Nombre",addMandatory("nombre"),true);
		return builder.getPanel();
	}
	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
		setInitialComponent(getControl("nombre"));
	}
		
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			return control;
		}
		return null;
	}
	
	
	public static Clase showForm(Clase bean){
		return showForm(bean,false);
	}
	
	public static Clase showForm(Clase bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ClaseForm form=new ClaseForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Clase)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Clase());
		ClaseForm.showObject(bean);
		System.exit(0);
	}

}
