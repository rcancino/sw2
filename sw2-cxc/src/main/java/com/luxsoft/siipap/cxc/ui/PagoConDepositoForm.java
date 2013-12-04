package com.luxsoft.siipap.cxc.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;

/**
 * Forma para el registro y mantenimiento pagos mediante transferencia o deposito
 * en cuenta.
 * 
 * @author Ruben Cancino
 *
 */
public class PagoConDepositoForm extends PagoPanel{

	public PagoConDepositoForm(PagoConDepositoFormModel model) {
		super(model);
		setTitle("Registro de pago (DEPOSITO/TRANSFERENCIA)");
	}
	
	protected PagoConDepositoFormModel getPagoConDepositoModel(){
		return (PagoConDepositoFormModel)getPagoModel();
	}
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		super.instalarAntesDeComentario(builder);
		builder.appendSeparator("Deposito/Transferencia");
		builder.append("Cuenta",getControl("cuenta"),5);
		builder.append("Banco",getControl("banco"),true);
		builder.append("Transferencia",getControl("transferencia"),true);
		builder.append("Efectivo",getControl("efectivo"),true);
		builder.append("Cheque",getControl("cheque"),true);
		builder.append("Referencia",getControl("referenciaBancaria"),5);	
		getControl("total").setEnabled(false);
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cuenta".equals(property)){
			JComboBox box=Bindings.createCuentasBinding(model.getModel(property));
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("banco".equals(property)){
			List<Banco> bancos=ServiceLocator2.getLookupManager().getBancos();
			List<String> bb=new ArrayList<String>();
			for(Banco cc:bancos){
				bb.add(cc.getClave());
			}
			SelectionInList sl=new SelectionInList(bb,model.getModel(property));
			final JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}
		else
			return super.createCustomComponent(property);
	}

	public static PagoConDeposito showForm(final PagoConDepositoFormModel model){
		PagoConDepositoForm dialog=new PagoConDepositoForm(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			return (PagoConDeposito)dialog.getPagoModel().getPago();
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				//SWExtUIManager.setup();
				PagoConDepositoFormModel model=new PagoConDepositoFormModel(new PagoConDeposito(),false);
				model.loadClientes();				
				showForm(model);
				showObject(model.getAbono());
			}
			
		});
	}

}
