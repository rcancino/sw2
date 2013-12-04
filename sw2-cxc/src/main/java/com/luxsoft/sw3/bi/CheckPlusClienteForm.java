package com.luxsoft.sw3.bi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;

import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


import com.luxsoft.sw3.ventas.CheckPlusCliente;


/**
 * Forma para el mantenimiento de Clientes tipo CheckPlus
 * 
 * @author Ruben Cancino
 *
 */
public class CheckPlusClienteForm extends AbstractForm{
	
	
	
	public CheckPlusClienteForm(CheckplusClienteFormModel model) {
		super(model);
	}
	
	public CheckplusClienteFormModel getBaseModel(){
		return (CheckplusClienteFormModel)getModel();
	}
	
	@Override
	protected JComponent buildHeader() {
		
		String titulo=getModel().getValue("id")==null?"Alta de cliente en CheckPlus":"";
		String descripcion="Mantenimiento para operar con cheques respaldados por CheckPlus";
		Header header=new Header(titulo, descripcion);
		return header.getHeader();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		JTabbedPane tab=new JTabbedPane();
		tab.addTab("Generales", createGeneralesPanel());
		tab.addTab("Referencias", new CheckPlusReferenciasPanel(getBaseModel()));
		tab.addTab("Documentos", new CheckPlusDocumentoPanel(getBaseModel()));
		return tab;
	}
	
	private JComponent createGeneralesPanel(){
		final FormLayout layout=new FormLayout(
				"p,2dlu,max(p;150dlu):g(.5), 3dlu," +
				"p,2dlu,max(p;150dlu):g(.5), 3dlu," +
				"p,2dlu,p:g(.4)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		if(model.getValue("id")==null)
			builder.append("Cliente",getControl("cliente"),9);
		builder.append("Razón social",addReadOnly("nombre"),9);
		builder.append("RFC",addReadOnly("rfc"));
		builder.append("CURP",getControl("curp"));
		builder.append("Persono física",getControl("personaFisica"));
		
		builder.append("Teléfono 1",addMandatory("telefono1"));
		builder.append("Teléfono 2",addMandatory("telefono2"));
		builder.nextLine();
		builder.append("Fax",addMandatory("fax"));
		builder.append("Email",addMandatory("email"));
		builder.nextLine();
		builder.append("Comentario",addMandatory("comentario"),9);
		builder.nextLine();
		builder.appendSeparator("Dirección");
		builder.append(getControl("direccion"),10);
		builder.append("Crédito solicitado",addMandatory("creditoSolicitado"),true);
		builder.append("Crédito solicitado",addReadOnly("lineaDeCredito"),true);
		
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		
		if("cliente".equals(property)){
			final JComboBox box = new JComboBox();
			final EventList source = GlazedLists.eventList(ServiceLocator2.getHibernateTemplate()
					.find("from Cliente c where c.credito!=null and c.credito.checkplus=false"));
			final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave" ,"nombre"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
			support.setFilterMode(TextMatcherEditor.CONTAINS);
			support.setCorrectsCase(true);
			box.getEditor().addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					Object sel=box.getSelectedItem();
					
					if(sel instanceof Cliente){
						Cliente ss=(Cliente)sel;
						Cliente cliente=ServiceLocator2.getClienteManager().get(ss.getId());
						model.setValue("cliente", cliente);
					}else if(sel instanceof String){
						/*
						String clave=(String)sel;
						if(!StringUtils.isBlank(clave)){
							Producto p=Services.getInstance().getProductosManager().buscarPorClave(clave);
							if(p!=null && p.isActivo() && p.isActivoVentas()){
								model.setValue("producto", p);
							}	
						}*/					
					}
				}
	        });        
	        if(model.getValue("cliente")!=null)
	        	box.setSelectedItem(model.getValue("cliente"));
			 box.setEnabled(!getModel().isReadOnly());
			 return box;
		}else if("tipoDeIdentificacion".equalsIgnoreCase(property)){
			SelectionInList sl=new SelectionInList(new String[]{"IFE","PASAPORTE"},getModel().getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setEnabled(!getModel().isReadOnly());
			return box;
		}if("rfc".equals(property)){
			try {
				MaskFormatter formatter=new MaskFormatter("UUU*-######-AAA"){
					public Object stringToValue(String value) throws ParseException {
						String nval=StringUtils.upperCase(value);
						return super.stringToValue(nval);						
					}
				};
				formatter.setValueContainsLiteralCharacters(false);
				formatter.setPlaceholder(" ");
				formatter.setValidCharacters(" 0123456789abcdfghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
				//formatter.setPlaceholderCharacter('_');
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(true);
				JFormattedTextField tfRfc=BasicComponentFactory.createFormattedTextField(model.getComponentModel(property), formatter);
				
				
				return tfRfc;
			} catch (Exception e) {
				
				return null;
				
			}
			
		}else if("nombre".equals(property) || "curp".equals(property) || "comentario".equals(property) ){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			tf.setEnabled(!getModel().isReadOnly());
			return tf;
		}else if("creditoSolicitado".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(getModel().getModel(property));
			c.setEnabled(!getModel().isReadOnly());
			return c;
		}
		return null;
	}
	
	
	public static CheckPlusCliente showForm(String id){
		CheckPlusCliente ch=ServiceLocator2.getCheckplusManager().buscarCliente(id);
		final CheckplusClienteFormModel model=new CheckplusClienteFormModel(ch);
		final CheckPlusClienteForm form=new CheckPlusClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static CheckPlusCliente showForm(){
		final CheckplusClienteFormModel model=new CheckplusClienteFormModel();
		final CheckPlusClienteForm form=new CheckPlusClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.commit();
		}
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				//Object res=showForm();
				Object res=showForm("8a8a8161-3f348f3c-013f-3490104e-0002");
				if(res!=null)
					showObject(res);
			}
		});
	}

}
