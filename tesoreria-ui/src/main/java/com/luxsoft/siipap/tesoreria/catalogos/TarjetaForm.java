package com.luxsoft.siipap.tesoreria.catalogos;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class TarjetaForm extends AbstractForm{

	public TarjetaForm(IFormModel model) {
		super(model);
		setTitle("Catálogo de Productos");
		
	}
	
	private JTabbedPane tabPanel;

	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("General", buildGeneralForm());
		return tabPanel;
	}
	
	private JComponent buildGeneralForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;90dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("");
		if(model.getValue("id")!=null){
			JComponent ct=getControl("id");
			ct.setEnabled(false);
			builder.append("Id",ct,true);
			getControl("nombre").setEnabled(false);
			getControl("nombre").setFocusable(false);
		}
		builder.append("Tarjeta",getControl("nombre"),5);
		builder.append("Banco",getControl("banco"));
		builder.append("Débito",getControl("debito"));
		builder.append("Comisión",getControl("comisionBancaria"));		
		builder.append("Comisión (Venta)",getControl("comisionVenta"));
		
		
		return builder.getPanel();
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Tarjeta de Credito ","Mantenimiento al catalogo de tarjetas");
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("banco".equals(property)){
			JComponent control= com.luxsoft.siipap.swing.binding.Bindings.createBancosBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;
		}else if("nombre".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getComponentModel(property));
			ValidationComponentUtils.setMandatory(control,true);
			return control;
		}
		return null;
	}

	
	@Override
	protected void onWindowOpened() {		
		super.onWindowOpened();
		setInitialComponent(getControl("nombre"));
	}
	
	public static Tarjeta  showForm(){
		return showForm(new Tarjeta(), false);
	}
	
	public static Tarjeta  showForm(Tarjeta tarjeta,boolean readOnly){
		final DefaultFormModel model=new DefaultFormModel(tarjeta);
		model.setReadOnly(readOnly);
		final TarjetaForm form=new TarjetaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Tarjeta)model.getBaseBean();
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				Tarjeta t=showForm();
				showObject(t);
				
			}
			
		});
	}

}
