package com.luxsoft.sw3.embarques.ui.forms;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.embarque.ClientePorTonelada;
import com.luxsoft.sw3.embarques.ui.selectores.SelectorDeClientes;

/**
 * Forma para el mantenimiento de clientes con tarifa especial para la comision
 * de embarque
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ClientePorToneladaForm extends AbstractForm{
	
	

	public ClientePorToneladaForm(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout("p,3dlu,120dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")!=null)
			builder.append("Id",addReadOnly("id"),true);
		builder.append("Precio (TON)",getControl("precio"));
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"));
		return builder.getPanel();
	}
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			Cliente c=(Cliente)model.getValue("cliente");
			header=new Header(c.getNombreRazon(),c.getDireccionAsString());
			
		}
		return header.getHeader();
	}
	
	public static ClientePorTonelada showForm(){
		Cliente c=SelectorDeClientes.seleccionar();
		if(c!=null){
			
			ClientePorTonelada bean=new ClientePorTonelada(c);
			DefaultFormModel model=new DefaultFormModel(bean);
			ClientePorToneladaForm form=new ClientePorToneladaForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				return (ClientePorTonelada)model.getBaseBean();
			}
		}
		return null;
	}
	
	public static ClientePorTonelada showForm(ClientePorTonelada bean,boolean readOnly){		
		DefaultFormModel model=new DefaultFormModel(bean,readOnly);
		ClientePorToneladaForm form=new ClientePorToneladaForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (ClientePorTonelada)model.getBaseBean();
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				showObject(showForm());
				System.exit(0);
			}

		});
	}
	
}
