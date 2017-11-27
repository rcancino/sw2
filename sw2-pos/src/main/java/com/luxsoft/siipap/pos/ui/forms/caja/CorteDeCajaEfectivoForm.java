package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.Checkbox;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Nueva version para el corte de caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeCajaEfectivoForm extends AbstractForm {
	


	public CorteDeCajaEfectivoForm(CorteDeCajaEfectivoFormModel model) {
		super(model);
		
		final CorteDeCajaEfectivoFormModel mod=model; 
		setTitle("Corte de caja : "+model.getCaja().getTipo());
		
		model.getModel("cierre").addValueChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
				
			}
			
		});
		
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;100dlu),3dlu,p,2dlu,max(p;100dlu):g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Fecha",getControl("fecha"));
		builder.append("Hora",addReadOnly("hora"));
		
		/*builder.nextLine();
		builder.append("Origen",getControl("origen"));*/
		builder.nextLine();
		
		builder.append("Pagos Registrados",addReadOnly("pagos"));
		builder.append("Cortes Acumulados",addReadOnly("cortesAcumulados"));
		
		builder.append("Cambios de cheque",addReadOnly("cambiosDeCheque"));
		builder.nextLine();
		
		//builder.append("Importe",addReadOnly("importe"));
		
		builder.append("Importe",getControl("importe"));

		builder.append("Disponible",addReadOnly("disponible"));
		
		
		
		builder.nextLine();
		
		builder.append("Cierre Monedas",getControl("cierre"));
		
		builder.append("Anticipo Corte",getControl("anticipoCorte"));
		
		builder.nextLine();
		
		
		builder.append("Comentario",getControl("comentario"));
		
		
		
	
		return builder.getPanel();
		
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if("origen".equals(property)){
			Object[] data={OrigenDeOperacion.CAM,OrigenDeOperacion.MOS};
			SelectionInList sl=new SelectionInList(data,model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}else if("importe".equals(property)|| "pagos".equals(property)
				|| "cortesAcumulados".equals(property)|| "disponible".equals(property)){
			return Binder.createBigDecimalForMonyBinding(model.getModel(property));
		}else if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}else if("cierre".equals(property)){
			JComponent c=BasicComponentFactory.createCheckBox(model.getModel(property), "Cierre");
			//c.setEnabled(!model.isReadOnly());
			return c;
		}else if("anticipoCorte".equals(property)){
			JComponent c=BasicComponentFactory.createCheckBox(model.getModel(property), "anticipoCorte");
			//c.setEnabled(!model.isReadOnly());
			return c;
		}
		return super.createCustomComponent(property);
	}
	
	public static Caja registrarCorte(){
		final CorteDeCajaEfectivoFormModel model=new CorteDeCajaEfectivoFormModel();
		model.getCaja().setTipo(Caja.Tipo.EFECTIVO);
		model.getCaja().setOrigen(OrigenDeOperacion.MOS);
	
		
		final CorteDeCajaEfectivoForm form=new CorteDeCajaEfectivoForm(model);
		
		form.open();
		if(!form.hasBeenCanceled()){
			Caja bean=model.commit();
			bean=Services.getInstance().getCorteDeCajaManager().registrarCorteDeCaja(bean);
			MessageUtils.showMessage("Corte registrado por: "+bean.getDeposito()
					+"  "+"\nFicha de deposito generada: "+bean.getFolio(), "Corte de efectivo");
			return bean;
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
