package com.luxsoft.sw3.embarques.ui.catalogos;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.embarque.Chofer;

/**
 * Forma para el mantenimiento de instancias de {@link Chofer}
 * 
 * @author Ruben Cancino
 *
 */
public class ChoferForm extends GenericAbstractForm<Chofer>{
	
	private JTabbedPane tabPanel;

	public ChoferForm(IFormModel model) {
		super(model);
		setTitle("Administración de Choferes");
	}

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Embarques","Resumen de chofer");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("General", buildGeneralForm());
		tabPanel.addTab("Comentarios", buildObservacionesPanel());
		return tabPanel;
	}
	
	private JComponent buildGeneralForm(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,p:g(.5), 2dlu," +
				"p,2dlu,p:g(.5)","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos generales");
		if(model.getValue("id")!=null){
			builder.append("Id",addReadOnly("id"),true);
			builder.append("Nombre",addReadOnly("nombre"),5);
		}else{
			builder.append("Nombre",addMandatory("nombre"),5);			
		}
		builder.append("Suspendido",getControl("suspendido"));
		builder.append("Susp (Fecha)",addReadOnly("suspendidoFecha"));
		
		builder.append("Radio",getControl("radio"),5);
		builder.append("Email-1",getControl("email1"),5);
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		return builder.getPanel();
	}
	
	private JComponent buildObservacionesPanel(){
		final ChoferObservacionesPanel panel=new ChoferObservacionesPanel((DefaultFormModel)model);
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("nombre".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),false);
			return tf;
		}
		return null;
	}
	
	
	
	public static Chofer showForm(Chofer bean){
		return showForm(bean,false);
	}
	
	public static Chofer showForm(Chofer bean,boolean readOnly){
		DefaultFormModel model=new DefaultFormModel(bean);
		model.setReadOnly(readOnly);
		final ChoferForm form=new ChoferForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Chofer)model.getBaseBean();
		}
		return null;
	}	
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		Object bean=showForm(new Chofer());
		ChoferForm.showObject(bean);
		System.exit(0);
	}

}
