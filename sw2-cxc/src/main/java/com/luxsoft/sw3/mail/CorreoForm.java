package com.luxsoft.sw3.mail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.print.PrinterException;
import java.net.URL;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class CorreoForm extends AbstractForm{
	
	JEditorPane editor;

	public CorreoForm(CorreoFormModel model) {
		super(model);
		setTitle("Correo electrónico");
	}
	
	public CorreoFormModel getCorreoModel(){
		return (CorreoFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel(){
		
		final JPanel panel=new JPanel(new BorderLayout());
		
		FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.3), 3dlu," +
				"p,2dlu,p:g(.3), 3dlu," +
				"p,2dlu,p:g(.3)"
				,"");
		
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente: ",addReadOnly("clienteNombre"),9);
		builder.append("De: ",getControl("from"));
		builder.append("Para:",getControl("to"));
		builder.nextLine();
		builder.append("Cc 1",getControl("cc1"));
		builder.append("Cc 2",getControl("cc2"));
		builder.append("Cc 3",getControl("cc3"));
		panel.add(builder.getPanel(),BorderLayout.NORTH); 
		
		
		editor=new JEditorPane();
		editor.setEditable(false);
		editor.setContentType("text/html");
		panel.add(new JScrollPane(editor));		
		panel.setPreferredSize(new Dimension(800,450));
		return panel;
	}
	
	

	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		editor.setText(getCorreoModel().getHtml());
	}

	@Override
	protected void setResizable() {
		setResizable(true);
	}
	
	public void print(){
		try {
			editor.print();
		} catch (PrinterException e) {
			e.printStackTrace();
			MessageUtils.showError("Error imprimiendo",e);
		}
	}
	
	public static void mandarCorreo(Cliente cliente,List<ComprobanteFiscal> cfds){
		final CorreoFormModel model=new CorreoFormModel(cfds);
		model.setValue("cliente", cliente);
		//model.setValue("from", "credito@papelsa.com.mx");
		final CorreoForm form=new CorreoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			model.commit();
		}
		
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
				Cliente c=ServiceLocator2.getClienteManager().buscarPorClave("A050435");
				List<ComprobanteFiscal> cfds=ServiceLocator2.getHibernateTemplate().find("from ComprobanteFiscal c where c.rfc=?","AABM540504E38");
				mandarCorreo(c, cfds);
				System.exit(0);
			}

		});
	}

}
