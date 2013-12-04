package com.luxsoft.sw3.bi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swing.utils.UIUtils;
import com.luxsoft.sw3.solicitudes.SolicitudDeModificacion;

public class AtenecionDeModificacionForm extends AbstractForm{

	public AtenecionDeModificacionForm() {
		super(new AtencionFormModel());
	}
	
	public Modificacion getModificacion(){
		return ((AtencionFormModel)getModel()).getModificacion();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;200dlu):g(.5), 3dlu," +
				"p,2dlu,max(p;200dlu):g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.append("Password",addMandatory("password"));
		builder.append("Atiende",addReadOnly("atendio"));
		builder.append("Estado",getControl("estado"),true);
		
		
		CellConstraints cc=new CellConstraints();
		builder.append("Comentario");
		builder.appendRow(new RowSpec("17dlu"));
		builder.add(new JScrollPane(getControl("comentarioDeAtencion")),cc.xywh(builder.getColumn(),builder.getRow(),5,2));
		builder.nextLine(2);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("comentarioDeAtencion".equals(property)){
			JTextArea ta=BasicComponentFactory.createTextArea(model.getModel(property),true);
			return ta;
		}else if("atendio".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}else if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(getModel().getModel(property),true);
			return c;
		}else if("estado".equals(property)){
			Object[] data=SolicitudDeModificacion.ESTADO.getSistemas();
			SelectionInList sl=new SelectionInList(data,getModel().getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}
		return super.createCustomComponent(property);
	}
	public static class AtencionFormModel extends DefaultFormModel{
		public AtencionFormModel() {
			super(Bean.proxy(Modificacion.class));
			getModel("password").addValueChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					resolveUser(evt.getNewValue());
				}
			});
		}
		
		public Modificacion getModificacion(){
			return (Modificacion)getBaseBean();
		}
		private void resolveUser(Object newValue) {
			String s=(String)newValue;
			setValue("atendio", KernellSecurity.instance().findUser(s,ServiceLocator2.getHibernateTemplate()));
			validate();
		}
		protected void addValidation(PropertyValidationSupport support){
			if(getModificacion().getAtendio()==null){
				support.addError("Atendio", "Digite su password");
			}if(getModificacion().getEstado()==null){
				support.addError("Estado", "Defina el estado de la modificación");
			}
		}
	}
	
	public static class Modificacion {
		private User atendio;
		private String comentarioDeAtencion;
		private String password;
		private SolicitudDeModificacion.ESTADO estado;
		public User getAtendio() {
			return atendio;
		}
		public void setAtendio(User atendio) {
			this.atendio = atendio;
		}
		public String getComentarioDeAtencion() {
			return comentarioDeAtencion;
		}
		public void setComentarioDeAtencion(String comentarioDeAtencion) {
			this.comentarioDeAtencion = comentarioDeAtencion;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public SolicitudDeModificacion.ESTADO getEstado() {
			return estado;
		}
		public void setEstado(SolicitudDeModificacion.ESTADO estado) {
			this.estado = estado;
		}
		
	}
	
	public static void main(String[] args) throws Exception{
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				AtenecionDeModificacionForm form=new AtenecionDeModificacionForm();
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(form.getModificacion());
				}
			}
		});
	}
}
