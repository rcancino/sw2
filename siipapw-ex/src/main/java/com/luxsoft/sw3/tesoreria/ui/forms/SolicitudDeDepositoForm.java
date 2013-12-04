package com.luxsoft.sw3.tesoreria.ui.forms;

import java.awt.Container;
import java.awt.Font;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.UIUtils;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * Forma para el mantenimiento de Pagos con deposito
 * 
 * @author Ruben Cancino Ramos
 * 
 */
public class SolicitudDeDepositoForm extends AbstractForm implements ActionListener{

	private Header header;
	
	private boolean paraAutoriza=false;
	private boolean paraCancelar=false;
	
	public SolicitudDeDepositoForm(final DefaultFormModel model) {
		super(model);
		if(!model.isReadOnly()){
			model.getModel("cliente").addValueChangeListener(new PropertyChangeListener(){			
				public void propertyChange(PropertyChangeEvent evt) {
					updateHeader();
				}			
			});
		}		
	}

	public JComponent buildHeader() {
		if (header == null) {
			header = new Header("Seleccione un cliente", "");
			header
					.getHeader()
					.setIcon(
							ResourcesUtils
									.getIconFromResource("images/siipapwin/ventas64.gif"));
			header.setDescRows(4);			
		}
		updateHeader();
		return header.getHeader();
	}
	
	private void updateHeader() {
		if(model.getValue("cliente")!=null){
			Cliente c=(Cliente)model.getValue("cliente");
			String pattern="{0} " +
					//"\n{1}" +
					"";
			String msg=MessageFormat.format(pattern
					,c.getDireccionAsString()
					//,c.getTelefonosRow()
					);
			header.setTitulo(c.getNombreRazon());
			header.setDescripcion(msg);
		}else{
			header.setTitulo("Seleccione un cliente");
		}
	}

	@Override
	protected JComponent buildFormPanel() {
		getOKAction().putValue(Action.NAME, "Salvar F10");
		FormLayout layout = new FormLayout("p,2dlu,100dlu:g(.5), 3dlu,p,2,150dlu:g:(.5)",
				"");
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		
		builder.append("Fecha", getControl("fecha"));
		if (model.getValue("id") != null)
			builder.append("Folio", addReadOnly("documento"));
		else{
			builder.nextLine();
			builder.append("Cliente",getControl("cliente"),5);
				
		}
		builder.nextLine();		
		builder.append("Efectivo", getControl("efectivo"));
		builder.append("Cheque", getControl("cheque"));
		builder.nextLine();
		builder.append("Transferencia", getControl("transferencia"));
		builder.nextLine();
		
		builder.append("Banco", getControl("bancoOrigen"), 5);
		builder.append("Cuenta(Dest)", getControl("cuentaDestino"),5);
		
		builder.nextLine();
		builder.append("Fecha Dep", getControl("fechaDeposito"));
		builder.append("Referencia", getControl("referenciaBancaria"));
		builder.nextLine();
		
		builder.append("Anticipo", getControl("anticipo"),true);
		builder.append("Solicita", getControl("solicita"));
		
		builder.nextLine();
		builder.append("Abono",addReadOnly("pagoInfo"),5);
		
		if(isParaAutoriza()){
			builder.append("Comentario",getControl("comentario"),5);
			builder.append("Autorizar",getControl("autorizar"));
			builder.append("Sbc",getControl("salvoBuenCobro"));
		}
		if(isParaCancelar()){
			builder.append("Cancelación",getControl("comentarioCancelacion"),5);
		}
		JPanel panel= builder.getPanel();
		ajustarFont(panel);
		return panel;
		
	}
	private final Font TITLE_LABEL_FONT=new java.awt.Font("SansSerif", 1, 13);
	
	private void ajustarFont(Container panel){
		//UIUtils.increaseFontSize(panel, 1f);
		UIUtils.updateContainerFont(panel, TITLE_LABEL_FONT.deriveFont(Font.BOLD));
	}
	
	

	/**
	 * Creea el panel principal. Normalmente no requiere ser modificado
	 * 
	 * @return
	 */
	protected JComponent buildMainPanel(){
		final FormLayout layout=new FormLayout("p:g(.7)","p,2dlu,80dlu:g(.3)");
		final PanelBuilder builder=new PanelBuilder(layout);
		final CellConstraints cc=new CellConstraints();
		builder.add(buildFormPanel(),cc.xy(1, 1));
		if(!model.isReadOnly())
			builder.add(buildValidationPanel(),cc.xy(1,3));
		model.validate();
		updateComponentTreeMandatoryAndSeverity(builder.getPanel());
		return builder.getPanel();
	}
	
	protected JComponent createNewComponent(final String property){
		JComponent res=super.createNewComponent(property);
		if(res instanceof JLabel)
			return res;
		if(model.isReadOnly() || isParaAutoriza())			
			//res.setEnabled(false);
			decorateReadOnly(res);
		if(property.equals("comentario")){
			res.setEnabled(true);
			((JTextField)res).setEditable(true);
			res.setFocusable(true);
		}
		if("autorizar".equals(property)){
			res.setEnabled(true);
			res.setFocusable(true);
		}if("salvoBuenCobro".equals(property)){
			res.setEnabled(true);
			res.setFocusable(true);
		}
		return res;
	}	
	
	private void decorateReadOnly(JComponent c){
		c.setEnabled(false);
		c.setFocusable(false);
		c.setFont(c.getFont().deriveFont(Font.BOLD));
		if(c instanceof JTextField){
			((JTextField)c).setEditable(false);
			c.setEnabled(true);
		}if(c instanceof JComboBox){
			System.out.println("Decoracion especial para JComboBox");
			JComboBox box=(JComboBox)c;
			
			box.getEditor().getEditorComponent().setFont(TITLE_LABEL_FONT.deriveFont(Font.BOLD));
		}
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if ("fecha".equals(property)) {
			JLabel ln = BasicComponentFactory.createLabel(model
					.getModel(property), new SimpleDateFormat(
					"dd/MM/yyyy ' 'KK:mm:ss:a "));
			//ln.setFont(ln.getFont().deriveFont(Font.BOLD));
			//ln.setEnabled(false);
			return ln;
		} else if ("bancoOrigen".equals(property)) {
			if(model.isReadOnly() || isParaAutoriza()){
				return BasicComponentFactory.createLabel(model.getModel(property), UIUtils.buildToStringFormat());
			}
			String hql = "from Banco b  ";
			List data = ServiceLocator2.getHibernateTemplate().find(hql);
			SelectionInList sl = new SelectionInList(data, model
					.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		} else if ("cuentaDestino".equals(property)) {
			if(model.isReadOnly() || isParaAutoriza()){
				return BasicComponentFactory.createLabel(model.getModel(property), UIUtils.buildToStringFormat());
			}
			String hql = "from Cuenta c where c.activoEnVentas=true";
			List data = ServiceLocator2.getHibernateTemplate().find(hql);
			SelectionInList sl = new SelectionInList(data, model
					.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		} else if ("efectivo".equals(property) || "cheque".equals(property)
				|| "transferencia".equals(property)) {
			return Binder.createBigDecimalForMonyBinding(model
					.getModel(property));
		} else if("cliente".equals(property)){
			return buildClienteControl();
		} else if("solicita".equals(property)){
			/*EventList<String> data=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find("select firstName + \' \' + lastName from User u "));
			JComboBox box=Binder.createBindingBox(model.getModel(property), data, new StringTextFilterator());
			return box;*/
			if(model.isReadOnly() || isParaAutoriza()){
				return BasicComponentFactory.createLabel(model.getModel(property), UIUtils.buildToStringFormat());
			}
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			return c;
		}else if("comentario".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			return c;
		}else if("fechaDeposito".equals(property)){
			if(model.isReadOnly() || isParaAutoriza()){
				return BasicComponentFactory.createLabel(model.getModel(property), new SimpleDateFormat(
				"dd / MMMM  / yyyy"));
			}
			return super.createCustomComponent(property);
		}else
			return super.createCustomComponent(property);
	}

	private JTextField nombreField = new JTextField(20);

	private JComponent buildClienteControl() {

		FormLayout layout = new FormLayout("p,2dlu,p", "");
		nombreField.addActionListener(this);
		ComponentUtils.addF2Action(nombreField, getLookupAction());
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		final UIFButton btn = new UIFButton(getLookupAction());
		btn.setFocusable(false);
		builder.append(nombreField, btn);
		return builder.getPanel();
	}
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"selecctionarCliente");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
			lookupAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F2"));
			
		}
		return lookupAction;
	}
	
	public void selecctionarCliente(){
		Cliente c=SelectorDeClientes.seleccionar(nombreField.getText());
		if(c!=null){			
			model.setValue("cliente", c);
			nombreField.setText(c.getClave());
		}else
			nombreField.setText("");
	}

	
	
	/**
	 * Implementacion local de {@link ActionListener} para  buscar el cliente por clave
	 * 
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==nombreField){
			if(!StringUtils.isBlank(nombreField.getText())){
				Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(nombreField.getText());
				model.setValue("cliente", c);
			}else
				model.setValue("cliente", null);
		}
	}
	
	KeyHandler keyHandler;
	
	public void open(){
		keyHandler=new KeyHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		super.open();
		
	}
	public void close(){		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		super.close();		
	}

	public boolean isParaAutoriza() {
		return paraAutoriza;
	}

	public void setParaAutoriza(boolean paraAutoriza) {
		this.paraAutoriza = paraAutoriza;
	}
	
	

	public boolean isParaCancelar() {
		return paraCancelar;
	}

	public void setParaCancelar(boolean paraCancelar) {
		this.paraCancelar = paraCancelar;
	}



	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			if(KeyEvent.KEY_PRESSED==e.getID()){
				//System.out.println("Key Text: "+KeyEvent.getKeyText(e.getKeyCode())+ "  Event type: "+e.getKeyCode()+ " KeyCode:"+e.getKeyCode());
				if(KeyStroke.getKeyStroke("F2").getKeyCode()==e.getKeyCode()){
					if(isFocused() && (model.getValue("id")==null)){
						e.consume();
						selecctionarCliente();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("F10").getKeyCode()==e.getKeyCode()){
					if(isFocused() && getOKAction().isEnabled() ){
						e.consume();
						doAccept();
						return true;
					}
				}
				
			}								
			return false;
		}
	}
	
	public static void consultar(SolicitudDeDeposito solicitud) {
		DefaultFormModel model = new DefaultFormModel(solicitud,true);
		SolicitudDeDepositoForm form = new SolicitudDeDepositoForm(model);
		form.open();
	}
	
	public static SolicitudDeDeposito modificar(SolicitudDeDeposito solicitud) {
		DefaultFormModel model = new DefaultFormModel(solicitud);
		SolicitudDeDepositoForm form = new SolicitudDeDepositoForm(model);
		form.open();
		if (!form.hasBeenCanceled()) {
			return (SolicitudDeDeposito) model.getBaseBean();
		}
		return null;
	}
	
	public static SolicitudDeDeposito modificarParaAutorizar(SolicitudDeDeposito solicitud) {
		Toolkit.getDefaultToolkit().beep();
		DefaultFormModel model = new DefaultFormModel(solicitud){
			@Override
			protected void addValidation(PropertyValidationSupport support) {
				SolicitudDeDeposito sol=(SolicitudDeDeposito)getBaseBean();
				Cliente c=ServiceLocator2.getClienteManager().buscarPorClave(sol.getCliente().getClave());
				if(!c.getComentarios().isEmpty()){
					if(c.getComentarios().containsKey("BLOQUEO_CONTABILIDAD")){
						support.addError("autorizar", "Bloqueo por contabilidad: "+c.getComentarios().get("BLOQUEO_CONTABILIDAD"));
					}
				}
				//support.addError("", "Debe registrar por lo menos una partida");
			}
		};
		SolicitudDeDepositoForm form = new SolicitudDeDepositoForm(model);
		form.setParaAutoriza(true);
		form.open();
		if (!form.hasBeenCanceled()) {
			return (SolicitudDeDeposito) model.getBaseBean();
		}
		return null;
	}
	
	public static SolicitudDeDeposito generar(OrigenDeOperacion origen) {
		SolicitudDeDeposito sol=new SolicitudDeDeposito();
		sol.setOrigen(origen);
		sol.setSucursal(ServiceLocator2.getConfiguracion().getSucursal());
		sol.setFecha(ServiceLocator2.obtenerFechaDelSistema());
		
		DefaultFormModel model = new DefaultFormModel(sol);
		SolicitudDeDepositoForm form = new SolicitudDeDepositoForm(model);
		form.open();
		if (!form.hasBeenCanceled()) {
			return (SolicitudDeDeposito) model.getBaseBean();
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
				//SolicitudDeDeposito sol=generar(OrigenDeOperacion.MOS);				
				
				//sol=Services.getInstance().getSolicitudDeDepositosManager().save(sol);				
				//System.out.println(sol);
				
				String hql="from SolicitudDeDeposito s left join fetch s.cliente c" +
				" left join fetch s.cuentaDestino c" +
				" left join fetch s.bancoOrigen b" +
				" left join fetch s.pago p " +
				" where s.id=?";
				List<SolicitudDeDeposito> res=ServiceLocator2.getHibernateTemplate().find(hql,"8a8a81e7-298e6e55-0129-8e8a3de5-0041");
				//consultar(res.get(0));
				@SuppressWarnings("unused")
				SolicitudDeDeposito sol=modificarParaAutorizar(res.get(0));
				System.exit(0);
			}

		});
	}

}
