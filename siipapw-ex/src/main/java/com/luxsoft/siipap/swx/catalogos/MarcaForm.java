package com.luxsoft.siipap.swx.catalogos;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Marca;
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
public class MarcaForm extends GenericAbstractForm<Marca>{
	
	

	public MarcaForm(IFormModel model) {
		super(model);
		setTitle("Mantenimiento de Marcas");
	}
	
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Marca","Clasificación de productos mediante Marcas ");
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
	
	
	public static Marca showForm(Marca bean){
		return showForm(bean,false);
	}
	
	public static Marca showForm(Marca bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final MarcaForm form=new MarcaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Marca)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Marca());
		MarcaForm.showObject(bean);
		System.exit(0);
	}

}
