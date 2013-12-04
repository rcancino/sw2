package com.luxsoft.siipap.cxc.ui.form;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.AutorizacionDeAplicacionCxC;
import com.luxsoft.siipap.cxc.model.AutorizacionesCxC;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.Concepto;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion.ModeloDeCalculo;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;

public class NotaDeCreditoBonificacionForm extends NotaDeCreditoForm{

	public NotaDeCreditoBonificacionForm(NotaDeCreditoBonificacionFormModel model) {
		super(model);
		model.getModel("modo").addValueChangeListener(new ModoHandler());
		setTitle("Nota de crédito por bonificación");
	}
	
	public NotaDeCreditoBonificacionFormModel getNotaBonificacionModel(){
		return (NotaDeCreditoBonificacionFormModel)model;
	}
	
	protected JComponent buildAplicacionesPanel(){
		JComponent c=super.buildAplicacionesPanel();
		c.setPreferredSize(new Dimension(800,350));
		return c;
	}
	
	
	protected void instalarAntesDeComentario(final DefaultFormBuilder builder){
		builder.append("Comentario CxC",getControl("comentario2"),5);
		builder.append("Concepto",getControl("concepto"));		
		builder.append("Modelo ",getControl("modo"));
		getControl("impuesto").setEnabled(false);
		getControl("importe").setEnabled(false);
		getControl("total").setEnabled(false);
		
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("concepto".equals(property)){
			SelectionInList sl=new SelectionInList(Concepto.values(),model.getModel("concepto"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("modo".equals(property)){
			SelectionInList sl=new SelectionInList(ModeloDeCalculo.values(),model.getModel("modo"));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!model.isReadOnly());
			return box;
		}else if("cliente".equals(property)){
			if(getNotaBonificacionModel().getNota().getCliente()==null){
				return super.createCustomComponent(property);
			}else{
				JTextField tf=new JTextField(20);
				tf.setText(getNotaBonificacionModel().getNota().getCliente().getNombreRazon());
				tf.setEnabled(false);
				return tf;
			}
		}
		return super.createCustomComponent(property);
	}
	
	@Override
	public void doAccept() {
		final AutorizacionDeAbono aut=AutorizacionesCxC.autorizacionDeNotaDeBonificacion();
		if(aut!=null){
			model.setValue("autorizacion", aut);
			super.doAccept();
		}
	}

	
	
	public static NotaDeCreditoBonificacion showForm(final NotaDeCreditoBonificacionFormModel model,OrigenDeOperacion origen){
		final NotaDeCreditoBonificacionForm form=new NotaDeCreditoBonificacionForm(model);
		form.setOrigen(origen);
		form.open();
		if(!form.hasBeenCanceled()){
			return (NotaDeCreditoBonificacion)model.getNota();
		}
		return null;
	}
	
	
	
	private class ModoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			ModeloDeCalculo m=getNotaBonificacionModel().getNotaBonificacion().getModo();
			getControl("total").setEnabled(m.equals(ModeloDeCalculo.PRORREATAR));
			getControl("descuento").setEnabled(!m.equals(ModeloDeCalculo.PRORREATAR));
		}		
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable(){
 
			public void run() {
				SWExtUIManager.setup();
				NotaDeCreditoBonificacion nota=new NotaDeCreditoBonificacion();
				NotaDeCreditoBonificacionFormModel model=new NotaDeCreditoBonificacionFormModel(nota,false);
				model.loadClientes();				
				showForm(model,null);
				//NotaDeCredito res=ServiceLocator2.getCXCManager().salvarNota(model.getNota());
				showObject(model.getNota());
				System.exit(0);
				
			}
			
		});
		
	}

}
