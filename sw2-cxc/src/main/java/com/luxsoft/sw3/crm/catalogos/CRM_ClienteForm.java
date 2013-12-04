package com.luxsoft.sw3.crm.catalogos;

import java.text.ParseException;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.DireccionControl;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Forma para el mantenimiento de instancias de {@link GTipoProveedor}
 * 
 * @author Ruben Cancino
 *
 */
public class CRM_ClienteForm extends GenericAbstractForm<Cliente>{
	
	private JTabbedPane tabPanel;

	public CRM_ClienteForm(CRM_ClienteFormModel model) {
		super(model);
		setTitle("Catálogo de Clientes");
		
	}
	
	private CRM_ClienteFormModel getClienteModel(){
		return (CRM_ClienteFormModel)getModel();
	}
	

	@Override
	protected JComponent buildHeader() {		
		return new HeaderPanel("Clientes ","Mantenimiento de clientes");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent buildFormPanel() {
		tabPanel=new JTabbedPane();
		tabPanel.addTab("General", buildGeneralForm());
		tabPanel.addTab("Contactos", buildContactosPanel());
		tabPanel.addTab("Comentarios", buildComentariosPanel());		
		tabPanel.addTab("Otros", buildOtrosPanel());
		
		return tabPanel;
	}
	
	private JComponent buildGeneralForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;70dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Datos principales");
		if(model.isReadOnly()){
			builder.append("Id",getControl("id"),true);
		}
		builder.append("Persona Física",getControl("personaFisica"),true);
		builder.append("Razón Social",getControl("nombre"),5);
		//setInitialComponent(getControl("nombre"));
		builder.append("Apellido P",getControl("apellidoP"));
		builder.append("Apellido M",getControl("apellidoM"));
		builder.append("Nombre(s)",getControl("nombres"),5);
		builder.append("RFC",getControl("rfc"),true);
		//builder.append("Tipo",getControl("tipo"),true);
		
		builder.append("Email",getControl("email"),5);
		setInitialComponent(getControl("email"));
		builder.append("http://www:",getControl("www"),5);
		builder.append("Cédula",getControl("cedula"),true);
		builder.append("Suspendido",getControl("suspendido"),true);
		builder.append("Permitir cheque",addReadOnly("permitirCheque"),true);
		builder.appendSeparator("Dirección Fiscal");
		builder.append(getControl("direccionFiscal"),7);
		getControl("direccionFiscal").setEnabled(!model.isReadOnly());
		return builder.getPanel();
	}
	
	private JComponent buildOtrosPanel(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Telefonos");
		CRM_ClienteTelefonosPanel panel=new CRM_ClienteTelefonosPanel(getClienteModel().getCliente());
		panel.setEnabled(!model.isReadOnly());
		builder.append(panel,7);
		DireccionControl control=new DireccionControl(getClienteModel().getComponentModel("direccionDeEntrega"));
		control.setEnabled(!model.isReadOnly());
		builder.appendSeparator("Dirección de Entrega");
		builder.append(control,7);
		return builder.getPanel();		
	}
	
	private JComponent buildComentariosPanel(){
		final CRM_ClienteComentariosPanel panel=new CRM_ClienteComentariosPanel(getClienteModel());
		panel.setEnabled(!model.isReadOnly());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComponent buildContactosPanel(){
		final CRM_ClienteContactosPanel panel=new CRM_ClienteContactosPanel(getClienteModel().getCliente());
		panel.setEnabled(!model.isReadOnly());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComboBox cobradorBox;
	private JComboBox vendedorBox;
	
	
	
	
	private JFormattedTextField tfRfc;
	
	MaskFormatter formatter;
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("cobrador".equals(property)){
			List data=ServiceLocator2.getCXCManager().getCobradores();
			SelectionInList sl=new SelectionInList(data,model.getModel("cobrador"));
			cobradorBox=BasicComponentFactory.createComboBox(sl);
			cobradorBox.setEnabled(!model.isReadOnly());
			return cobradorBox;
		}else if("vendedor".equals(property)){
			List data=ServiceLocator2.getCXCManager().getVendedores();
			SelectionInList sl=new SelectionInList(data,model.getModel("vendedor"));
			vendedorBox=BasicComponentFactory.createComboBox(sl);
			vendedorBox.setEnabled(!model.isReadOnly());
			return vendedorBox;
		}if("rfc1".equals(property)){
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
	
	
	
	public static Cliente showForm(Cliente bean){
		return showForm(bean,false);
	}
	
	public static Cliente showForm(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		return showForm(c,false);
	}
	
	public static Cliente showForm(Cliente bean,boolean readOnly){
		CRM_ClienteFormModel model=new CRM_ClienteFormModel(bean);
		model.setReadOnly(readOnly);
		final CRM_ClienteForm form=new CRM_ClienteForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return (Cliente)model.getBaseBean();
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
				DBUtils.whereWeAre();
				showForm("P080970");
				System.exit(0);
			}

		});
	}

}
