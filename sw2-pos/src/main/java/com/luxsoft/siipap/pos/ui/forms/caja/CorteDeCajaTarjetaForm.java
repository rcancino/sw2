package com.luxsoft.siipap.pos.ui.forms.caja;

import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Nueva version para el corte de caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeCajaTarjetaForm extends AbstractForm {

	public CorteDeCajaTarjetaForm(CorteDeCajaTarjetaFormModel model) {
		super(model);
		setTitle("Corte de caja : "+model.getCaja().getTipo());
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
		builder.nextLine();
		
		builder.append("Pagos Registrados",addReadOnly("pagos"));
		builder.append("Cortes Acumulados",addReadOnly("cortesAcumulados"));
		
		
		builder.nextLine();
		builder.append("Importe",addReadOnly("importe"));
		builder.append("Disponible",addReadOnly("disponible"));
		
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
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
		}
		return super.createCustomComponent(property);
	}
	
	public static Caja registrarCorte(){
		final CorteDeCajaTarjetaFormModel model=new CorteDeCajaTarjetaFormModel();
		
		final CorteDeCajaTarjetaForm form=new CorteDeCajaTarjetaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Caja bean=model.commit();
			bean=Services.getInstance().getCorteDeCajaManager().registrarCorteDeCajaTarjeta(bean);		
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
