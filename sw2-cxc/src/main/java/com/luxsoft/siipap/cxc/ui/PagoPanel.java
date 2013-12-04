package com.luxsoft.siipap.cxc.ui;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.service.ServiceLocator2;


/**
 * Panel base para el mantenimiento de Pagos
 * 
 * @author Ruben Cancino
 *
 */
public class PagoPanel extends AbonoPanel{

	public PagoPanel(PagoFormModel model) {
		super(model);
	}
	
	public PagoFormModel getPagoModel(){
		return (PagoFormModel)getAbonoModel();
	}
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		builder.append("Cobrador",getControl("cobrador"));
		builder.append("Enviado",getControl("enviado"));		
		builder.append("Origen",getControl("origen"));
		builder.append("Anticipo",getControl("anticipo"),true);
		
	}
	

	@Override
	protected JComponent createCustomComponent(String property) {
		if("cobrador".equals(property)){
			SelectionInList sl=new SelectionInList(ServiceLocator2.getCXCManager().getCobradores(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("enviado1".equals(property)){
			JCheckBox box=BasicComponentFactory.createCheckBox(model.getModel(property), "");
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("origen".equals(property)){
			SelectionInList sl=new SelectionInList(OrigenDeOperacion.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}
		return super.createCustomComponent(property);
	}
}
