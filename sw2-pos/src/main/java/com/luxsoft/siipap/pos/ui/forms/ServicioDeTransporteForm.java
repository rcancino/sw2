package com.luxsoft.siipap.pos.ui.forms;

import javax.swing.JComponent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.embarques.ServicioDeTransporte;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.services.Services;

/**
 * Forma para el mantenimiento de servicios de transportes
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ServicioDeTransporteForm extends AbstractForm{
	
	

	public ServicioDeTransporteForm(IFormModel model) {
		super(model);
		
	}

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				"p,2dlu,max(100dlu;p), 3dlu ," +
				"p,2dlu,max(100dlu;p):g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Nombre",getControl("nombre"),5);
		builder.append("RFC",getControl("rfc"));
		builder.nextLine();
		builder.append("Tel 1",getControl("telefono1"));
		builder.append("Tel 2",getControl("telefono2"));
		builder.nextLine();
		builder.append("Fax ",getControl("fax"));
		builder.nextLine();
		builder.appendSeparator("Dirección");
		
		builder.append("Calle",addMandatory("calle"),5);
		builder.append("Numero Ext",addMandatory("numero"));
		builder.append("Numero Int",getControl("numeroInterior"));
		builder.append("Colonia",getControl("colonia"),5);
		builder.append("Del/Mpio",getControl("municipio"),5);
		builder.append("Ciudad",getControl("ciudad"));
		builder.append("Estado",getControl("estado"));
		builder.append("C.P.",getControl("cp"));
		
		
		return builder.getPanel();
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			return Binder.createMayusculasTextField(model.getModel(property));
		}
		return super.createCustomComponent(property);
	}

	public static ServicioDeTransporte showForm(){
		ServicioDeTransporte s=new ServicioDeTransporte();
		final DefaultFormModel model=new DefaultFormModel(s);
		ServicioDeTransporteForm form=new ServicioDeTransporteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			ServicioDeTransporte res=(ServicioDeTransporte)model.getBaseBean();
			return (ServicioDeTransporte)Services.getInstance().getUniversalDao().save(res);
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
