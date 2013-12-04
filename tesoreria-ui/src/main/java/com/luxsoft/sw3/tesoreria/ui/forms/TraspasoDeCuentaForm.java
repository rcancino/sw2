package com.luxsoft.sw3.tesoreria.ui.forms;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.tesoreria.model.TraspasoDeCuenta;

public class TraspasoDeCuentaForm extends AbstractForm{
	
	
	
	public TraspasoDeCuentaForm(TraspasoDeCuentaFormModel model) {
		super(model);
		setTitle("Traspaso entre cuentas");
	}
	private TraspasoDeCuentaFormModel getController(){
		return (TraspasoDeCuentaFormModel)getModel();
	}
	

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.5), 2dlu," +
				"p,2dlu,p:g(.5)","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Id",addReadOnly("id"));
		builder.append("Fecha",getControl("fecha"));
		builder.nextLine();
		
		builder.append("Origen",addMandatory("cuentaOrigen"),5);
		builder.append("Destino",getControl("cuentaDestino"),5);
				
		builder.append("Importe",getControl("importe"));
		builder.append("Moneda",addReadOnly("moneda"));
		
		builder.append("Comisión",getControl("comision"));
		builder.append("IVA",getControl("impuesto"));
		
		builder.append("Referencia",getControl("referenciaOrigen"),5);
		builder.append("Comentario",getControl("comentario"),5);
		
		
		model.setValue("moneda", MonedasUtils.PESOS);//Set initial value
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuentaOrigen".equals(property)){
			 JComboBox box= createCuentaOrigenBox(model.getModel(property));
			 box.setEnabled(!model.isReadOnly());
			 return box;
		}else if("cuentaDestino".equals(property)){
			JComboBox box= createCuentaDestinoBox(model.getModel(property));
			 box.setEnabled(!model.isReadOnly());
			 return box;
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return null;
	}
	
	private JComboBox createCuentaOrigenBox(ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=getController().getCuentas();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","banco","numero"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;		
	}
	
	private JComboBox createCuentaDestinoBox(ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=getController().getCuentas();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","banco","numero"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;		
	}
	
	


	public static TraspasoDeCuenta showForm(){
		TraspasoDeCuentaFormModel model=new TraspasoDeCuentaFormModel();
		final TraspasoDeCuentaForm form=new TraspasoDeCuentaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			TraspasoDeCuenta target=model.commit();
			return target;
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				Object res=showForm();
				if(res!=null)
					showObject(res);
			}
		});
	}

}
