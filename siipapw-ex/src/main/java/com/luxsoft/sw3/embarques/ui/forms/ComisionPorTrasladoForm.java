package com.luxsoft.sw3.embarques.ui.forms;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;

public class ComisionPorTrasladoForm extends AbstractForm{
	
	private boolean porFactura=false;

	public ComisionPorTrasladoForm(ComisionPorTrasladoFormModel model) {
		super(model);
		setTitle("Comisión para Chofer");
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,2dlu,p:g(.5) 3dlu" +
				",p,2dlu,p:g(.5)","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(isPorFactura())
			builder.append("Comisión",getControl("comision"));
		else
			builder.append("Precio (TON)",getControl("precioPorTonelada"));		
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentario".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return null;
	}
	
	

	public boolean isPorFactura() {
		return porFactura;
	}

	public void setPorFactura(boolean porFactura) {
		this.porFactura = porFactura;
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();				
				ComisionPorTrasladoFormModel model=new ComisionPorTrasladoFormModel();
				ComisionPorTrasladoForm form=new ComisionPorTrasladoForm(model);
				form.open();
				System.exit(0);
			}

		});
	}
	
	

}
