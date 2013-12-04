package com.luxsoft.sw3.tesoreria.ui.forms;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.tesoreria.model.Inversion;


public class InversionForm extends AbstractForm{
	
	private boolean registroDeGanancias=false;
	
	public InversionForm(InversionFormModel model) {
		super(model);
		setTitle("Registro de inversión");
	}
	private InversionFormModel getController(){
		return (InversionFormModel)getModel();
	}
	

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;100dlu):g(.3), 3dlu," +
				"p,2dlu,max(p;100dlu):g(.3), 3dlu," +
				"p,2dlu,p:g(.4)","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")!=null)
			builder.append("Folio",addReadOnly("id"));
		builder.append("Fecha",getControl("fecha"));
		getControl("fecha").setEnabled(!isRegistroDeGanancias());
		builder.nextLine();
		
		builder.append("Origen",addMandatory("cuentaOrigen"),9);
		builder.append("Destino",getControl("cuentaDestino"),9);
		builder.nextLine();
		
		builder.append("Importe",getControl("importe"));		
		builder.append("Moneda",addReadOnly("moneda"));
		builder.nextLine();		
		//builder.append("Comisión",getControl("comision"));		
		//builder.append("IVA",getControl("impuesto"),true);
		
		builder.append("Plazo",getControl("plazo"));
		builder.append("Tasa (Anual)",getControl("tasa"),true);			
		builder.append("ISR (%)",getControl("isr"));
		builder.append("ISR ",getControl("importeRealISR"),true);
		
		
		
		builder.append("Referencia",getControl("referenciaOrigen"),5);
		builder.append("Vencimiento",getControl("vencimiento"));
		builder.nextLine();		
		builder.append("Comentario",getControl("comentario"),9);
		getControl("comentario").setEnabled(!isRegistroDeGanancias());

		
		builder.appendSeparator("Rendimiento ");
		
		builder.append("Calculado",addReadOnly("rendimientoCalculado"));
		builder.append("Real",getControl("rendimientoReal"));		
		builder.append("Fecha",getControl("rendimientoFecha"));
		//builder.append("IVA",getControl("rendimientoImpuesto"),true);
		
		model.setValue("moneda", MonedasUtils.PESOS);//Set initial value
		enableComponents();
		return builder.getPanel();
	}
	
	
	public void enableComponents() {		
		
		String[] props={"rendimientoImpuesto","rendimientoFecha","rendimientoReal"};
		for(String s:props){
			getControl(s).setEnabled(registroDeGanancias && !model.isReadOnly());
		}
		String[] props2={"referenciaOrigen","tasa","comision","impuesto","importe","cuentaOrigen","cuentaDestino","vencimiento","plazo"};
		for(String s:props2){
			getControl(s).setEnabled(!registroDeGanancias && !model.isReadOnly());
		}
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
		}else if("tasa".equals(property) || "isr".equals(property)){
			JComponent control=Bindings.createDescuentoEstandarBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("rendimientoReal".equals(property)){
			JComponent control=Bindings.createBigDecimalBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());			
			return control;			
		}else if("importe".equals(property)){
			JComponent control=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());			
			return control;
		}else if("rendimientoReal".equals(property)){
			JComponent control=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());			
			return control;
		}else if("rendimientoCalculado".equals(property)){
			JComponent control=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());			
			return control;
		}else if("importeRealISR".equals(property)){
			JComponent control=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			control.setEnabled(!model.isReadOnly());			
			return control;
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
		EventList source=getController().getCuentas();
		source=new FilterList(source,new Matcher() {
			public boolean matches(Object item) {
				Cuenta c=(Cuenta)item;
				return c.getTipo().equals(Cuenta.Clasificacion.INVERSION);
			}
		});
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","banco","numero"});
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;		
	}

	public boolean isRegistroDeGanancias() {
		return registroDeGanancias;
	}
	public void setRegistroDeGanancias(boolean registroDeGanancias) {
		this.registroDeGanancias = registroDeGanancias;
	}
	
	public static Inversion showForm(boolean ganancias){
		InversionFormModel model=new InversionFormModel();
		final InversionForm form=new InversionForm(model);
		form.setRegistroDeGanancias(ganancias);
		form.open();
		if(!form.hasBeenCanceled()){
			Inversion target=model.commit();
			return target;
		}
		return null;
	}
	
	public static Inversion showForm(){
		InversionFormModel model=new InversionFormModel();
		final InversionForm form=new InversionForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Inversion target=model.commit();
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
