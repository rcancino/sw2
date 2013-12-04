package com.luxsoft.sw3.solicitudes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.springframework.orm.hibernate3.HibernateTemplate;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.luxsoft.siipap.service.core.ClienteManager;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.UIUtils;


/**
 * Forma para la generacion de solicitudud para modificacion de informacion
 * 
 * @author Ruben Cancino
 *
 */
public class SolicitudDeModificacionForm extends AbstractForm{
	
	private HibernateTemplate hibernateTemplate;
	private ClienteManager clienteManager;
	
	public SolicitudDeModificacionForm(SolicitudDeModificacionFormModel model) {
		super(model);
		setTitle("Solicitud de modificacion");
		model.getModel("modulo").addValueChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				SolicitudDeModificacion.Modulo m=(SolicitudDeModificacion.Modulo)evt.getNewValue();
				tiposList.setList(m.getTipos());
				tiposList.clearSelection();
				
			}
		});
		
	}
	
	public SolicitudDeModificacionFormModel getBaseModel(){
		return (SolicitudDeModificacionFormModel)getModel();
	}
	
	
	
	@Override
	protected JComponent buildFormPanel() {
		
		
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;200dlu):g(.5), 3dlu," +
				"p,2dlu,max(p;200dlu):g(.5)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.setRowGroupingEnabled(true);
		if(model.getValue("id")!=null){
			builder.append("Folio",addReadOnly("folio"),true);
			
		}
		builder.append("Sucursal",addReadOnly("sucursal"));
		builder.append("Estado",getControl("estado"));
		
		builder.append("Fecha",addReadOnly("fecha"),true);
		builder.append("Módulo",getControl("modulo"));
		builder.append("Tipo",getControl("tipo"));
		builder.nextLine();
		
		CellConstraints cc=new CellConstraints();
		//builder.appendRow(builder.getLineGapSpec());
		//builder.appendRow("top:31dlu");
		//builder.nextLine(2);
		builder.append("Descripcion");
		builder.appendRow(new RowSpec("17dlu"));
		builder.add(new JScrollPane(getControl("descripcion")),cc.xywh(builder.getColumn(),builder.getRow(),5,2));
		builder.nextLine(2);
		
		builder.append("Comentario");
		builder.appendRow(new RowSpec("17dlu"));
		builder.add(new JScrollPane(getControl("comentario")),cc.xywh(builder.getColumn(),builder.getRow(),5,2));
		builder.nextLine(2);
		
		if(model.getValue("id")==null){
			builder.append("Password",addMandatory("password"));
		}
		
		builder.append("Usuario",addReadOnly("usuario"));
		builder.nextLine();
		builder.appendSeparator("Documento");
		builder.append("Folio",addMandatory("documento"),true);
		builder.append(addReadOnly("documentoDescripcion"),7);
		
		if(model.getValue("id")!=null && !model.isReadOnly()){
			builder.appendSeparator("Autorizacion");
			JComponent aut=getControl("password");
			builder.append("Password",aut);
			builder.append("Autoriza",addReadOnly("autorizo"));
			builder.append("Comentario",getControl("comentarioAutorizacion"));
			builder.append("Fecha",getControl("autorizacion"));
		}
		
		if(model.isReadOnly()){
			builder.appendSeparator("Autorizó");
			builder.append("Usuario",addReadOnly("autorizo"));
			builder.append("Comentario",addReadOnly("comentarioAutorizacion"));
			builder.appendSeparator("Atendido");
			builder.append("Atendio",addReadOnly("atendio"));
			builder.append("Comentario",addReadOnly("comentarioDeAtencion"));
		}
		
		return builder.getPanel();
	}
	
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if(model.isReadOnly()){
			if("password".equals(property))
				return new JLabel();
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}
		if("descripcion".equals(property) || "comentario".equals(property)){
			JTextArea ta=BasicComponentFactory.createTextArea(model.getModel(property),true);
			ta.setEditable(model.getValue("id")==null);
			return ta;
		}else if("password".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(getBaseModel().getPasswordHolder(),true);
			return c;
		}else if(("modulo").equals(property)){
			if(model.getValue("id")==null){
				SelectionInList sl=new SelectionInList(SolicitudDeModificacion.Modulo.values(),model.getModel(property));
				JComboBox box=BasicComponentFactory.createComboBox(sl);
				box.setEnabled(!getModel().isReadOnly());
				return box;
			}else{
				return BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			}
			
		}else if(("tipo").equals(property)){
			if(model.getValue("id")==null){
				tiposList=new SelectionInList(new Object[0],model.getModel(property));
				JComboBox box=BasicComponentFactory.createComboBox(tiposList);
				box.setEnabled(!getModel().isReadOnly());
				return box;
			}else{
				return BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			}
			
		}else if("usuario".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}else if("sucursal".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}else if("estado".equals(property)){
			if(model.getValue("id")==null){
				JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
				return c;
			}else{
				SelectionInList sl=new SelectionInList(SolicitudDeModificacion.ESTADO.values(),model.getModel(property));
				JComboBox box=BasicComponentFactory.createComboBox(sl);
				return box;
			}
			
		}else if("documento".equals(property)){
			JComponent c=BasicComponentFactory.createTextField(model.getModel(property), true);
			c.setEnabled(model.getValue("id")==null);
			return c;
		}else if("autorizo".equals(property)){
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}else if("comentarioAutorizacion".equals(property)){
			JTextField c=BasicComponentFactory.createTextField(model.getModel(property), true);
			c.setEditable(model.getValue("id")!=null);
			return c;
		}else{
			JComponent c=BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
			return c;
		}
		//return BasicComponentFactory.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
	}
	
	@Override
	protected void onWindowOpened() {
		super.onWindowOpened();
		getControl("password").requestFocusInWindow();
	}
	
	private SelectionInList tiposList;

	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public ClienteManager getClienteManager() {
		return clienteManager;
	}

	public void setClienteManager(ClienteManager clienteManager) {
		this.clienteManager = clienteManager;
	}

	
	

}
