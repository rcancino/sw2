package com.luxsoft.sw3.cfd.ui.form;

import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.sw3.cfd.model.CertificadoDeSelloDigital;
import com.luxsoft.sw3.cfd.model.FolioFiscal;
import com.luxsoft.sw3.cfd.ui.selectores.SelectorDeCertificados;


public class FolioForm extends AbstractForm{

	public FolioForm(FolioFormModel model) {
		super(model);
	}
	
	public FolioFormModel getFolioModel(){
		return (FolioFormModel)getModel();
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,p, 3dlu" +
				",p,2dlu,120dlu:g,3dlu" +
				",20dlu "
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(model.getValue("id")==null){
			builder.append("Sucursal",getControl("sucursal"));
			builder.append("Serie",getControl("serie"));
			builder.nextLine();
		}else{
			builder.append("Sucursal",addReadOnly("sucursal"));
			builder.append("Serie",addReadOnly("serie"));
			builder.nextLine();
		}
		builder.append("Inicial",getControl("folioInicial"));
		builder.append("Final",getControl("folioFinal"));
		builder.nextLine();
		builder.append("Asignación ",addMandatory("asignacion"));		
		builder.append("No de aprobación",addMandatory("noAprobacion"));
		builder.nextLine();
		builder.append("Año ",getControl("anoAprobacion"),true);
		//builder.append("Certificado",getControl("certificado"),5);
		//builder.append(buildSearchUrlButton());
		builder.nextLine();
		return builder.getPanel();
	}
	
	private JButton buildSearchUrlButton(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "localizarCertificado"));
		return btn;
	}
	
	public void localizarCertificado(){
		CertificadoDeSelloDigital cer=SelectorDeCertificados.seleccionar();
		if(cer!=null)
			model.setValue("certificado", cer);
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("sucursal".equals(property)){
			JComboBox box=Bindings.createSucursalesBinding(getFolioModel().getIdModel().getModel(property));
			boolean val=!model.isReadOnly();
			box.setEditable(val);
			box.setFocusable(val);
			return box;
		}else if("serie".equalsIgnoreCase(property)){
			JTextField tf=Binder.createMayusculasTextField(getFolioModel().getIdModel().getModel(property));
			tf.setEditable(!model.isReadOnly());
			return tf;
		}else if("folioInicial".equals(property) || "folioFinal".equals(property)){
			JFormattedTextField field=BasicComponentFactory.createLongField(model.getModel(property), 0);
			return field;
		}
		else
			return null;
	}

	public static FolioFiscal showForm(){
		final FolioFormModel model=new FolioFormModel();
		final FolioForm form=new FolioForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}else
			return null;
	}
	
	public static FolioFiscal showForm(final FolioFiscal folio){
		final FolioFormModel model=new FolioFormModel(folio,false);
		final FolioForm form=new FolioForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}else
			return null;
	}
	
	public static void view(final FolioFiscal folio){
		final FolioFormModel model=new FolioFormModel(folio,true);
		final FolioForm form=new FolioForm(model);
		form.open();
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
