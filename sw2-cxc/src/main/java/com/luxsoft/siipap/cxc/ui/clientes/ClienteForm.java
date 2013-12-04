package com.luxsoft.siipap.cxc.ui.clientes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.List;

import javax.swing.JCheckBox;
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
import com.jgoodies.validation.view.ValidationComponentUtils;
import com.luxsoft.siipap.cxc.CXCRoles;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.binding.DireccionControl;
import com.luxsoft.siipap.swing.form2.GenericAbstractForm;

/**
 * Forma para el mantenimiento de instancias de {@link GTipoProveedor}
 * 
 * @author Ruben Cancino
 *
 */
public class ClienteForm extends GenericAbstractForm<Cliente>{
	
	private JTabbedPane tabPanel;

	public ClienteForm(ClienteFormModel model) {
		super(model);
		setTitle("Catálogo de Clientes");
		
		model.getComponentModel("personaFisica").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				sincronizarNombre();
			}			
		});
		model.addBeanPropertyChangeListener(new ValidationHelper());	
		
	}
	
	private ClienteFormModel getClienteModel(){
		return (ClienteFormModel)getModel();
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
		tabPanel.addTab("Credito", buildCuentaForm());
		tabPanel.addTab("Contactos", buildContactosPanel());
		tabPanel.addTab("Comentarios", buildComentariosPanel());		
		tabPanel.addTab("Otros", buildOtrosPanel());
		sincronizarNombre();
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
		builder.append("Persona Física",addReadOnly("personaFisica"),true);
		builder.append("Razón Social",getControl("nombre"),5);
		setInitialComponent(getControl("nombre"));
		builder.append("Apellido P",getControl("apellidoP"));
		builder.append("Apellido M",getControl("apellidoM"));
		builder.append("Nombre(s)",getControl("nombres"),5);
		builder.append("RFC",getControl("rfc"),true);
		builder.append("Email",getControl("email"));
		
		builder.nextLine();
		builder.append("http://www:",getControl("www"),5);
		if(KernellSecurity.instance().hasRole(CXCRoles.GERENCIA_DE_CREDITO.name())){
			builder.append("Suspendido",getControl("suspendido"),true);
			builder.append("Permitir cheque",getControl("permitirCheque"),true);
		}else{
			builder.append("Suspendido",addReadOnly("suspendido"),true);
			builder.append("Permitir cheque",addReadOnly("permitirCheque"),true);
		}
		
		builder.appendSeparator("Dirección Fiscal");
		builder.append(getControl("direccionFiscal"),7);
		getControl("direccionFiscal").setEnabled(!model.isReadOnly());
		
		builder.nextLine();
		builder.appendSeparator("CFD");
		builder.append("Destinatario",getControl("destinatarioCFD"),5);
		builder.append("Email ",getControl("emai3"),5);
		builder.nextLine();
		builder.append("Forma de pago",getControl("formaDePago"));
		builder.append("Cuenta",getControl("cuentaDePago"));
		
		
		return builder.getPanel();
	}
	
	private JComponent buildOtrosPanel(){
		final FormLayout layout=new FormLayout(
				"50dlu,2dlu,70dlu, 2dlu," +
				"50dlu,2dlu,70dlu:g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Telefonos");
		ClienteTelefonosPanel panel=new ClienteTelefonosPanel(getClienteModel());
		panel.setEnabled(!model.isReadOnly());
		builder.append(panel,7);
		DireccionControl control=new DireccionControl(getClienteModel().getComponentModel("direccionDeEntrega"));
		control.setEnabled(!model.isReadOnly());
		builder.appendSeparator("Dirección de Entrega");
		builder.append(control,7);
		return builder.getPanel();		
	}
	
	private JComponent buildComentariosPanel(){
		final ClienteComentariosPanel panel=new ClienteComentariosPanel(getClienteModel());
		panel.setEnabled(!model.isReadOnly());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	private JComponent buildContactosPanel(){
		final ClienteContactosPanel panel=new ClienteContactosPanel(getClienteModel());
		panel.setEnabled(!model.isReadOnly());
		panel.setEnabled(!model.isReadOnly());
		return panel;
	}
	
	
	private JCheckBox creditBox;
	private JComponent creditoLinea;
	private JTextField creditoPlazo;
	private JCheckBox creditoSuspendido;
	private JCheckBox creditoRevision;
	private JComboBox creditoDiaRevision;
	private JComboBox creditoDiaCobro;
	private JCheckBox creditoVencimientoFactura;	
	private JTextField operadorField;
	private JCheckBox chequePostFechado;
	private JComboBox cobradorBox;
	private JComboBox vendedorBox;
	private JComponent descuentoFijo;
	private JComponent atrasoMaximo;
	
	 
	
	private JComponent buildCuentaForm(){
		final FormLayout layout=new FormLayout(
				"max(p;40dlu),2dlu,max(p;70dlu), 2dlu," +
				"max(p;40dlu),2dlu,f:max(p;70dlu):g","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Línea autorizada");
		creditBox=new JCheckBox(" Autorizar"); //
		creditBox.setEnabled(!model.isReadOnly());
		creditBox.addActionListener(new CreditoHandler());
		creditBox.setSelected(getClienteModel().getCliente().isDeCredito());
		
		builder.append("Credito",creditBox,true);
		if(getClienteModel().getCliente().getCredito()!=null)
			//if(getClienteModel().getCliente().getCredito().getId()==null)
				creditBox.setEnabled(false);
		
		creditoLinea=Bindings.createCantidadMonetariaBinding(getClienteModel().getCreditoModel().getComponentModel("linea"));
		creditoLinea.setEnabled(creditBox.isSelected());
		chequePostFechado=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("chequePostfechado"), "Permitir post-fechado");
		creditoPlazo=BasicComponentFactory.createIntegerField(getClienteModel().getCreditoModel().getComponentModel("plazo"));
		creditoPlazo.setEnabled(false);
		creditoSuspendido=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("suspendido"), "Suspender temporalmente");
		descuentoFijo=Bindings.createDescuentoEstandarBinding(getClienteModel().getCreditoModel().getComponentModel("descuentoEstimado"));
		atrasoMaximo=BasicComponentFactory.createIntegerField(getClienteModel().getCreditoModel().getComponentModel("atrasoMaximo"));
		builder.append("Línea de Crédito",creditoLinea);
		builder.append("Plazo",creditoPlazo);
		//builder.nextLine();
		//builder.append("Suspendido",creditoSuspendido);
		//builder.append("Cheque",chequePostFechado);
		builder.nextLine();
		builder.append("Descuento Fijo",descuentoFijo);
		builder.append("Atraso Máximo",atrasoMaximo,true);
		
		builder.appendSeparator("A revisión y cobro");
		creditoRevision=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("revision"), "");
		creditoVencimientoFactura=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("vencimientoFactura"), "Fecha factura");
		creditoDiaRevision=Binder.createDiaDeLaSemanaBinding(getClienteModel().getCreditoModel().getComponentModel("diarevision"));
		creditoDiaCobro=Binder.createDiaDeLaSemanaBinding(getClienteModel().getCreditoModel().getComponentModel("diacobro"));
		builder.append("Mandar a revisión",creditoRevision);
		builder.append("Vencimiento",creditoVencimientoFactura);
		builder.append("Dia Rev",creditoDiaRevision);
		builder.append("Dia Cob",creditoDiaCobro);
		
		builder.append("Cobrador",getControl("cobrador"));
		builder.append("Vendedor",getControl("vendedor"));
		builder.nextLine();
		builder.append("Cuota mensual",getControl("cuotaMensualComision"),true);
		//builder.nextLine();
		builder.append("Cuenta Contable",getControl("cuentaContable"),true);
		
		operadorField=BasicComponentFactory.createIntegerField(getClienteModel().getCreditoModel().getComponentModel("operador"));
		//operadorField.setEnabled(false);
		builder.append("Operador CxC",operadorField,true);
		
		enableCreditoPanel(creditBox.isSelected());
		if(model.isReadOnly()){
			enableCreditoPanel(false);
			
		}
		this.creditoLinea.setEnabled(false);
		this.descuentoFijo.setEnabled(false);
		this.atrasoMaximo.setEnabled(false);
		this.creditoPlazo.setEnabled(false);
		return builder.getPanel();
	}
	
	private JFormattedTextField tfRfc;
	
	MaskFormatter formatter;
	
	@Override
	protected JComponent createCustomComponent(String property) {
		/*if("tipo".equals(property)){
			JComboBox box= Bindings.createTipoTipoDeProveedorBinding(model.getModel("tipo"));
			box.setEnabled(!model.isReadOnly());
			return box;
		}*/
		if("rfc2".equals(property)){
			try {
				formatter=new MaskFormatter("UUU-######-AAA"){
					public Object stringToValue(String value) throws ParseException {
						String nval=StringUtils.upperCase(value);						
						return super.stringToValue(nval);						
					}
					
				};
				formatter.setValueContainsLiteralCharacters(false);
				formatter.setPlaceholderCharacter('_');
				formatter.setAllowsInvalid(false);
				formatter.setCommitsOnValidEdit(false);
				tfRfc=BasicComponentFactory.createFormattedTextField(model.getComponentModel(property), formatter);
				
				model.getComponentModel("personaFisica").addPropertyChangeListener(new PropertyChangeListener(){
					public void propertyChange(PropertyChangeEvent evt) {
						sincronizarRfcFormatter();
					}					
				});
								
				return tfRfc;
			} catch (Exception e) {
				
				return null;
				
			}
			
		}else if("nombre".equals(property) || "nombres".equals(property) || "apellidoP".equals(property) || "apellidoM".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}else if("cobrador".equals(property)){
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
		}else if("formaDePago".equals(property)){
			Object[] data=FormaDePago.values();
			final SelectionInList sl=new SelectionInList(data,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setPrototypeDisplayValue("TARJETA DE CREDITO");
			return box;
		}else if("cuentaDePago".equals(property)){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}
		return null;
	}
	
	private void sincronizarRfcFormatter(){		
		if(model.getValue("personaFisica").equals(Boolean.TRUE)){
			try {
				formatter.setMask("UUUU-######-AAA");
				
			} catch (ParseException e) {				
				e.printStackTrace();
			}
		}else{
			try {
				formatter.setMask("UUU-######-AAA");
			} catch (ParseException e) {				
				e.printStackTrace();
			}
		}		
	}
	
	
	/**
	 * Valida que la razon social no exista
	 *
	 */
	private void validarNombre(){
		final String nombre=(String)model.getValue("nombre");
		if(nombre==null || nombre.isEmpty()) 
			return;  //NO VALIDAMOS		
		if(model.getValue("personaFisica").equals(Boolean.TRUE)){
			return ;
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("LostFocus: Validando que el nombre no exista en la base de datos");
		}				
	}
	
	/**
	 * Valida que el RFC no exista ya en la base de datos
	 * 
	 *
	 */
	private void validarRfc(){		
		
		final String rfc=(String)model.getValue("rfc");
		if(rfc==null || rfc.isEmpty()) 
			return;  //NO VALIDAMOS
		if(logger.isDebugEnabled()){
			logger.debug("LostFocus: Validando que el RFC no exista en la base de datos");
		}		
	}
	
	private void sincronizarNombre(){
		
		Boolean val=(Boolean)model.getValue("personaFisica");
		model.getComponentModel("nombres").setEnabled(val);
		model.getComponentModel("apellidoP").setEnabled(val);
		model.getComponentModel("apellidoM").setEnabled(val);		
		model.getComponentModel("nombre").setEnabled(!val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("nombres"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("apellidoP"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("apellidoM"),val);
		ValidationComponentUtils.setMandatory((JTextField)getControl("nombre"),!val);
		//updateComponentTreeMandatoryAndSeverity();
		
		if(val){			
			//model.setValue("nombre", "");
			getControl("nombre").requestFocusInWindow();
		}
		else{
			//model.setValue("nombres", "");
			//model.setValue("apellidoM", "");
			//model.setValue("apellidoP", "");			
			getControl("apellidoP").requestFocusInWindow();
		}
		
		
	}
	
	private class ValidationHelper implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("nombre".equals(evt.getPropertyName())){
				validarNombre();
			}else if("rfc".equals(evt.getPropertyName())){
				validarRfc();
			}			
		}		
	}
	
	private void enableCreditoPanel(boolean val){
		//creditoLinea.setEnabled(val);
		//creditoPlazo.setEnabled(val);
		//creditoSuspendido.setEnabled(val);
		creditoRevision.setEnabled(val);
		creditoDiaRevision.setEnabled(val);
		creditoDiaCobro.setEnabled(val);
		creditoVencimientoFactura.setEnabled(val);
		operadorField.setEnabled(val);
		chequePostFechado.setEnabled(val);
		cobradorBox.setEditable(val);
		vendedorBox.setEditable(val);
		//descuentoFijo.setEnabled(val);
		//getControl("cobrador").setEnabled(false);
		//getControl("vendedor").setEnabled(false);
	}
	
	
	private class CreditoHandler implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(creditBox.isSelected())
				getClienteModel().getCliente().habilitarCredito();
			else
				getClienteModel().cancelarCredito();
			enableCreditoPanel(creditBox.isSelected());
			
		}
		
	}
	
	
	public static Cliente showForm(Cliente bean){
		return showForm(bean,false);
	}
	
	public static Cliente showForm(String clave ){
		Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(clave);
		return showForm(c,false);
	}
	
	public static Cliente showForm(Cliente bean,boolean readOnly){
		ClienteFormModel model=new ClienteFormModel(bean);
		model.setReadOnly(readOnly);
		final ClienteForm form=new ClienteForm(model);
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
				showForm("U050008");
				System.exit(0);
			}

		});
	}

}
