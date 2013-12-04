package com.luxsoft.sw3.cfd.ui.form;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;


public class CertificadoForm extends AbstractForm{

	public CertificadoForm(CertificadoFormModel model) {
		super(model);
		setTitle("Mantenimiento de Certificado de sello digital");
	}
	
	public CertificadoFormModel getFormModel(){
		return (CertificadoFormModel)getModel();
	}
	
	private JTextArea validacionArea;

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,120dlu:g(.5), 3dlu" +
				",p,2dlu,120dlu:g(.5),3dlu" +
				",20dlu "
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Certificado",getControl("certificadoPath"),5);
		builder.append(buildCertSearchButton());
		builder.nextLine();
		builder.append("Número",addReadOnly("numeroDeCertificado"));
		builder.nextLine();
		builder.append("Vigencia Del ",addReadOnly("expedicion"));
		builder.append("Al",addReadOnly("vencimiento"));
		
		builder.nextLine();
		builder.append("Llave privada",getControl("privateKeyPath"),5);
		builder.append(buildPkSearchButton());
		builder.nextLine();
		builder.append("Algoritmo",getControl("algoritmo"));
		builder.nextLine();
		
		builder.appendSeparator("Validación");
		validacionArea=new JTextArea(10,40);
		Bindings.bind(validacionArea, getFormModel().getMessageHolder());
		validacionArea.setEnabled(false);
		builder.append(new JScrollPane(validacionArea),9);
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("certificadoPath".equals(property) || "privateKeyPath".equals(property)){
			JTextField tf=BasicComponentFactory.createTextField(model.getModel(property));
			tf.setEditable(false);
			return tf;
			//return BasicComponentFactory.createLabel(model.getModel(property), UIUtils.buildToStringFormat());
		}else if("algoritmo".equals(property)){
			
			SelectionInList sl=new SelectionInList(CertificadoDeSelloDigital.ALGORITMOS,model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}
		return super.createCustomComponent(property);
	}

	private JButton buildCertSearchButton(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "localizarCertificado"));
		return btn;
	}
	
	public void localizarCertificado(){
		logger.info("Localizando certificado de sello digital");
		JFileChooser chooser=new JFileChooser("/");
		FileNameExtensionFilter filter=new FileNameExtensionFilter("Certificado", "cer");
		chooser.setFileFilter(filter);
		int res=chooser.showDialog(getContentPane(), "Aceptar");
		if(res==JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			if(file!=null){
				model.setValue("certificadoPath", file.getAbsolutePath());
			}
		}
		
	}
	
	private JButton buildPkSearchButton(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "localizarPK"));
		return btn;
	}
	
	public void localizarPK(){
		logger.info("Localizando llave privada ");
		JFileChooser chooser=new JFileChooser("/");
		FileNameExtensionFilter filter=new FileNameExtensionFilter("Private Key", "key");
		chooser.setFileFilter(filter);
		int res=chooser.showDialog(getContentPane(), "Aceptar");
		if(res==JFileChooser.APPROVE_OPTION){
			File file=chooser.getSelectedFile();
			if(file!=null){
				model.setValue("privateKeyPath", file.getAbsolutePath());
			}
		}
		
	}
	
	public static CertificadoDeSelloDigital showForm(){
		
		CertificadoDeSelloDigital cer=(CertificadoDeSelloDigital)ServiceLocator2.getHibernateTemplate().get(CertificadoDeSelloDigital.class,1L);
		if(cer==null)
			cer=new CertificadoDeSelloDigital();
		final CertificadoFormModel model=new CertificadoFormModel(cer);
		final CertificadoForm form=new CertificadoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}else
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
				showForm();
				System.exit(0);
			}

		});
	}

}
