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
import com.luxsoft.sw3.tesoreria.model.ComisionBancaria;


public class ComisionBancariaForm extends AbstractForm{
	

	
	public ComisionBancariaForm(ComisionBancariaFormModel model) {
		super(model);
		setTitle("Registro de inversión");
	}
	private ComisionBancariaFormModel getController(){
		return (ComisionBancariaFormModel)getModel();
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
		
		builder.nextLine();
		
		builder.append("Cuenta",addMandatory("cuenta"),9);
		builder.nextLine();
		
		builder.append("Comisión",getControl("comision"));		
		builder.append("IVA",getControl("impuesto"),true);
		
		builder.append("Referencia",getControl("referenciaOrigen"),5);		
		builder.nextLine();		
		builder.append("Comentario",getControl("comentario"),9);
		

		
		
		
		model.setValue("moneda", MonedasUtils.PESOS);//Set initial value
		
		return builder.getPanel();
	}
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuenta".equals(property)){
			 JComboBox box= createCuentaOrigenBox(model.getModel(property));
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
	
	

	
	
	
	public static ComisionBancaria showForm(){
		ComisionBancariaFormModel model=new ComisionBancariaFormModel();
		final ComisionBancariaForm form=new ComisionBancariaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			ComisionBancaria target=model.commit();
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
