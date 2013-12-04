package com.luxsoft.siipap.cxc.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class PagoConChequeForm extends PagoPanel{
	
	public PagoConChequeForm(PagoConChequeFormModel model) {
		super(model);
		setTitle("Registro de pago (CHEQUE)");
		model.getModel("cliente").addValueChangeListener(new ClienteHandler());
		model.getModel("postFechado")
		.addValueChangeListener(new PostFechadoHandler());
		
	}
	
	public PagoConChequeFormModel getPagoConChequeModel(){
		return (PagoConChequeFormModel)model;
	}
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		super.instalarAntesDeComentario(builder);
		builder.appendSeparator("Cheque");
		builder.append("Numero",getControl("numero"));
		builder.append("Banco",getControl("banco"));
		builder.append("Vencimiento",getControl("vencimiento"));
		builder.append("Post-Fechado",getControl("postFechado"));		
		builder.append("Cuenta",getControl("cuentaDelCliente"),true);
		builder.append("Nombre",getControl("cuentaHabiente"),5);		
		getControl("postFechado").setEnabled(false);
		getControl("vencimiento").setEnabled(false);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("banco".equals(property)){			
			SelectionInList sl=new SelectionInList(getPagoConChequeModel().getBancos(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("cuentaHabiente".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("cuentaDelCliente".equals(property)){
			JComboBox box=createCuentasBox(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		else
			return super.createCustomComponent(property);
	}
	
	protected JComboBox createCuentasBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=getPagoConChequeModel().getCuentas();		
		AutoCompleteSupport support = AutoCompleteSupport.install(box, source);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        //support.setStrict(true);
        support.setSelectsTextOnFocusGain(true);
        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
        model.addListDataListener(new Bindings.WeakListDataListener(vm));
        box.setSelectedItem(vm.getValue());
		return box;
	}
	

	public static PagoConCheque showForm(final PagoConChequeFormModel model){
		PagoConChequeForm dialog=new PagoConChequeForm(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return (PagoConCheque)dialog.getPagoModel().getPago();
		}
		return null;
	}
	
	
	private class ClienteHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Cliente c=(Cliente)evt.getNewValue();
			if(c!=null){
				if(c.getCredito()!=null){
					getControl("postFechado").setEnabled(c.getCredito().isChequePostfechado());
				}else
					getControl("postFechado").setEnabled(false);
					
			}
		}
	}
	
	/**
	 * Habilita el componente de vencimiento en funcion de que
	 * se permita el manejo de post fechados
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class PostFechadoHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			Boolean res=(Boolean)evt.getNewValue();
			getControl("vencimiento").setEnabled(res);
		}
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				PagoConChequeFormModel model=new PagoConChequeFormModel(new PagoConCheque(),false);
				model.loadClientes();				
				showForm(model);
				showObject(model.getAbono());
				
			}
			
		});
		
	}

}
