package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.FormatUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.POSDBUtils;

/**
 * Nueva version para el corte de caja para cheques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeCajaChequeForm extends AbstractForm {

	public CorteDeCajaChequeForm(CorteDeCajaChequeFormModel model) {
		super(model);
		setTitle("Corte de caja : CHEQUE");
	}
	
	public CorteDeCajaChequeFormModel getCorteModel(){
		return (CorteDeCajaChequeFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;100dlu),3dlu,p,2dlu,max(p;100dlu):g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Hora",addReadOnly("hora"));
		
		builder.nextLine();
		builder.append("Origen",getControl("origen"));
		builder.append("Cuenta",addReadOnly("cuenta"));
		builder.nextLine();
		
		builder.append("Pagos Registrados",addReadOnly("pagos"));
		builder.append("Cortes Acumulados",addReadOnly("cortesAcumulados"));
		builder.nextLine();
		
		builder.append("Cambios de cheque",addReadOnly("cambiosDeCheque"));
		builder.nextLine();
		
		builder.append("Importe",addReadOnly("importe"));
		builder.append("Disponible",addReadOnly("disponible"));
		builder.nextLine();
		
		builder.appendSeparator("Cheques");		
		builder.append("Mismo Banco",addReadOnly("chequesMismoBanco"));
		builder.append("$",addReadOnly("importeChequesMismoBanco"));
		builder.nextLine();
		
		builder.append("Otros",addReadOnly("chequesOtrosBancos"));
		builder.append("$",addReadOnly("importeChequesOtrosBancos"));
		builder.nextLine();
		builder.append("Total",addReadOnly("cheques"));
		builder.append("$",addReadOnly("importeCheques"));
		
		builder.appendSeparator("Fichas");		
		builder.append("Mismo Banco",addReadOnly("fichasMismoBanco"));
		builder.append("$",addReadOnly("importeFichasMismoBanco"));
		builder.nextLine();
		
		builder.append("Otros",addReadOnly("fichasOtrosBancos"));
		builder.append("$",addReadOnly("importeFichasOtrosBancos"));
		builder.nextLine();
		builder.append("Total",addReadOnly("fichas"));
		builder.append("$",addReadOnly("totalFichas"));
		//builder.append("Comentario",addReadOnly("comentario"),5);
		return builder.getPanel();
	}
	
	protected JComponent buildButtonBarWithOKCancel() {
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(
        		createMostrarButton(),
        		createBuscarButton(),
        		createOKButton(true),
        		createCancelButton()
            	);
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        return bar;
    }
		

	@Override
	protected JComponent createCustomComponent(String property) {
		if("origen".equals(property)){
			Object[] data={OrigenDeOperacion.CAM,OrigenDeOperacion.MOS};
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}else if(property.startsWith("impor")|| "pagos".equals(property)
				|| "cortesAcumulados".equals(property)|| "disponible".equals(property)
				||"cambiosDeCheque".endsWith(property)){
			return Binder.createBigDecimalForMonyBinding(model.getModel(property));
		}else if("cuenta".equals(property)){
			return BasicComponentFactory.createLabel(model.getModel(property),FormatUtils.getToStringFormat());
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}
	
	
	public JButton createBuscarButton(){
		JButton btn=new JButton("Seleccionar pagos");
		btn.addActionListener(EventHandler.create(ActionListener.class, getCorteModel(), "registrarPagos"));
		return btn;
	}
	
	public JButton createMostrarButton(){
		JButton btn=new JButton("Mostrar pagos");
		btn.addActionListener(EventHandler.create(ActionListener.class, getCorteModel(), "mostrarPagos"));
		return btn;
	}
	
	public static Caja registrarCorte(){
		final CorteDeCajaChequeFormModel model=new CorteDeCajaChequeFormModel();		
		final CorteDeCajaChequeForm form=new CorteDeCajaChequeForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Caja res=model.persist();
			MessageUtils.showMessage("Corte registrado: "+res.getDeposito()
					+ "  \n Fichas generadas:"+res.fichasParaCheques.size()
					, "Corte de Caja (CHEQUE)");
			return res;
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
				POSDBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				registrarCorte();
				System.exit(0);
			}

		});
	}
	

}
