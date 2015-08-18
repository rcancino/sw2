package com.luxsoft.sw3.crm.catalogos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.gastos.GTipoProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
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
		setTitle("Catlogo de Clientes");
		
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
		tabPanel.addTab("Credito", buildCuentaForm());
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
		builder.append("Persona Fsica",getControl("personaFisica"),true);
		builder.append("Razn Social",getControl("nombre"),5);
		//setInitialComponent(getControl("nombre"));
		builder.append("Apellido P",getControl("apellidoP"));
		builder.append("Apellido M",getControl("apellidoM"));
		builder.append("Nombre(s)",getControl("nombres"),5);
		builder.append("RFC",getControl("rfc"),true);
		//builder.append("Tipo",getControl("tipo"),true);
		
		builder.append("Email",getControl("email"),5);
		setInitialComponent(getControl("email"));
		builder.append("http://www:",getControl("www"),5);
		builder.append("Cdula",getControl("cedula"),true);
		builder.append("Suspendido",getControl("suspendido"),true);
		builder.append("Permitir cheque",addReadOnly("permitirCheque"),true);
		builder.appendSeparator("Direccin Fiscal");
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
		builder.appendSeparator("Direccin de Entrega");
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
		builder.appendSeparator("Lnea autorizada");
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
		builder.append("Lnea de Crdito",creditoLinea);
		builder.append("Plazo",creditoPlazo);
		//builder.nextLine();
		//builder.append("Suspendido",creditoSuspendido);
		//builder.append("Cheque",chequePostFechado);
		builder.nextLine();
		builder.append("Descuento Fijo",descuentoFijo);
		builder.append("Atraso Mximo",atrasoMaximo,true);
		
		builder.appendSeparator("A revisin y cobro");
		creditoRevision=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("revision"), "");
		creditoVencimientoFactura=BasicComponentFactory.createCheckBox(getClienteModel().getCreditoModel().getComponentModel("vencimientoFactura"), "Fecha factura");
		creditoVencimientoFactura.setEnabled(false);
		creditoDiaRevision=Binder.createDiaDeLaSemanaBinding(getClienteModel().getCreditoModel().getComponentModel("diarevision"));
		creditoDiaCobro=Binder.createDiaDeLaSemanaBinding(getClienteModel().getCreditoModel().getComponentModel("diacobro"));
		builder.append("Mandar a revisin",creditoRevision);
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
	
	private class CreditoHandler implements ActionListener{

		public void actionPerformed(ActionEvent e) {
			if(creditBox.isSelected())
				getClienteModel().getCliente().habilitarCredito();
			else
				getClienteModel().cancelarCredito();
			enableCreditoPanel(creditBox.isSelected());
			
		}
		
	}
	
	private void enableCreditoPanel(boolean val){
		//creditoLinea.setEnabled(val);
		//creditoPlazo.setEnabled(val);
		//creditoSuspendido.setEnabled(val);
		creditoRevision.setEnabled(val);
		creditoDiaRevision.setEnabled(val);
		creditoDiaCobro.setEnabled(val);
		//creditoVencimientoFactura.setEnabled(val);
		operadorField.setEnabled(val);
		chequePostFechado.setEnabled(val);
		cobradorBox.setEditable(val);
		vendedorBox.setEditable(val);
		//descuentoFijo.setEnabled(val);
		//getControl("cobrador").setEnabled(false);
		//getControl("vendedor").setEnabled(false);
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
