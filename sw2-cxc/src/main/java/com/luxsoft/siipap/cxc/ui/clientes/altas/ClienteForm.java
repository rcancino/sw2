package com.luxsoft.siipap.cxc.ui.clientes.altas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.util.LookupUtils;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;


/**
 * Forma para el mantenimiento de Clientes desde el punto de venta
 * 
 * @author Ruben Cancino
 *
 */
public class ClienteForm extends AbstractForm{
	
	

	public ClienteForm(IFormModel model) {
		super(model);
		setTitle("Catálogo de clientes");
	}

	@Override
	protected JComponent buildHeader() {
		if(model.getValue("id")!=null)
			return new HeaderPanel("Registro de cliente nuevo ","");
		return null;
	}
	
	private void initComponents(){
		ValueModel vm=model.getModel("personaFisica");
		vm.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				Boolean val=(Boolean)evt.getNewValue();
				getControl("apellidoP").setEnabled(val);
				getControl("apellidoM").setEnabled(val);
				getControl("nombres").setEnabled(val);
				getControl("nombre").setEnabled(!val);
				
			}
		});
		Boolean val=(Boolean)vm.getValue();
		getControl("apellidoP").setEnabled(val);
		getControl("apellidoM").setEnabled(val);
		getControl("nombres").setEnabled(val);
		getControl("nombre").setEnabled(!val);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		initComponents();
		final FormLayout layout=new FormLayout(
				"max(p;50dlu),2dlu,max(p;100dlu), 2dlu," +
				"max(p;50dlu),2dlu,max(p;100dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos generales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		builder.append("RFC",getControl("rfc"));
		builder.append("Persona Física",getControl("personaFisica"));
		
		builder.append("Razón Social",getControl("nombre"),5);
		setInitialComponent(getControl("rfc"));
		builder.append("Apellido P",getControl("apellidoP"));
		builder.append("Apellido M",getControl("apellidoM"));
		builder.append("Nombre(s)",getControl("nombres"),5);
		builder.append("Email",getControl("email"));		
		builder.append("http://www:",getControl("www"),true);
		builder.append("Telefono",getControl("telefono1"));
		builder.append("Telefono 2",getControl("telefono2"));
		builder.append("Fax ",getControl("fax"),true);

		
		addDireccionFields(builder);
		return builder.getPanel();
	}
	
	
	protected void addDireccionFields(final DefaultFormBuilder builder){
		PresentationModel dm=new PresentationModel(model.getValue("direccionFiscal"));
		
		builder.appendSeparator("Dirección Fiscal");
		builder.append("Calle",Binder.createMayusculasTextField(dm.getModel("calle")),5);
		builder.append("Numero Ext",BasicComponentFactory.createTextField(dm.getModel("numero")));
		builder.append("Numero Int",BasicComponentFactory.createTextField(dm.getModel("numeroInterior")));
		builder.append("Colonia",Binder.createMayusculasTextField(dm.getModel("colonia")),5);
		builder.append("Del/Mpio",buildMunicipioBox(dm.getModel("municipio")),5);
		builder.append("Entidad",buildEstadosBox(dm.getModel("estado")));
		builder.append("C.P.",BasicComponentFactory.createTextField(dm.getModel("cp")));
		if(model.isReadOnly()){
			ComponentUtils.disableComponents(builder.getPanel());
		}
	}
	

	
	@Override
	protected JComponent createCustomComponent(String property) {		
		if("rfc".equals(property)){
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
			
		}else if("nombre".equals(property) || "nombres".equals(property) || "apellidoP".equals(property) || "apellidoM".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}
		return null;
	}
	
	protected JComponent cobradorComponent(String property){
		if("cobrador".equals(property)){
		
		}
		return null;
	}
	
	
	private JComboBox buildMunicipioBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(LookupUtils.getMunicipios());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setSelectsTextOnFocusGain(true);
        box.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				vm.setValue(sel);	
			}
        });       
        if(vm.getValue()!=null)
        	box.setSelectedItem(vm.getValue());
		return box;
	}
	
	
	
	private JComboBox buildEstadosBox(final ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(LookupUtils.getEstados());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setSelectsTextOnFocusGain(true);
        box.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				vm.setValue( sel);	
			}
        });        		
        if(vm.getValue()!=null)
        	box.setSelectedItem(vm.getValue());
		return box;
	}
	
	

}
