package com.luxsoft.siipap.pos.ui.forms.caja;

import java.awt.Font;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.core.Cliente;

import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.catalogos.ClienteController;
import com.luxsoft.sw3.ui.catalogos.Mostrador;

/**
 * Forma para el mantenimiento de Pagos con deposito
 * 
 * @author Ruben Cancino Ramos
 * 
 */
public class PagoConDepositoForm extends AbstractForm implements ActionListener{

	private Header header;
	
	//private ClienteController clienteController;

	public PagoConDepositoForm(final PagoconDepositoFormModel model) {
		super(model);
		model.getModel("cliente").addValueChangeListener(new PropertyChangeListener(){			
			public void propertyChange(PropertyChangeEvent evt) {
				if(header!=null)
					updateHeader();
			}			
		});
		
		model.getModel("cliente").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				Cliente c=(Cliente)evt.getNewValue();
				if(c!=null){
					if("1".equals(c.getClave())){
						Mostrador m=ClienteController.getInstance().getMostrador();
						if(m!=null)
							model.setValue("nombre", m.toString());
					}
				}
				
			}
		});
	}

	public JComponent buildHeader() {
		if (header == null) {
			header = new Header("", "");
			header
					.getHeader()
					.setIcon(
							ResourcesUtils
									.getIconFromResource("images/siipapwin/ventas64.gif"));
		}
		updateHeader();
		return header.getHeader();
	}
	
	private void updateHeader() {
		if(model.getValue("nombre")!=null){
			header.setDescripcion(model.getValue("nombre").toString());
		}else{
			header.setDescripcion("Seleccione un cliente");
		}
	}

	@Override
	protected JComponent buildFormPanel() {
		getOKAction().putValue(Action.NAME, "Salvar F10");
		FormLayout layout = new FormLayout("p,2dlu,100dlu:g(.5), 3dlu,p,2,150dlu:g:(.5)",
				"");
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		builder.append("Fecha", addReadOnly("fecha"));
		if (model.getValue("id") != null)
			builder.append("Folio", addReadOnly("folio"));
		else
			builder.append("Cliente",getControl("cliente"));		
		builder.nextLine();
		
		builder.append("Moneda", addReadOnly("moneda"));
		builder.append("T.C.", addReadOnly("tc"));
		builder.nextLine();
		builder.append("Efectivo", getControl("efectivo"));
		builder.append("Cheque", getControl("cheque"));
		builder.nextLine();
		builder.append("Transferencia", getControl("transferencia"));
		builder.nextLine();
		
		builder.append("Banco", getControl("banco"), 5);
		builder.append("Cuenta(Dest)", getControl("cuenta"));
		
		builder.nextLine();
		builder.append("Fecha Dep", getControl("fechaDeposito"));
		builder.append("Referencia", getControl("referenciaBancaria"));
		builder.nextLine();
		
		builder.append("Anticipo", getControl("anticipo"),true);
		builder.append("Solicita", getControl("solicito"));
		return builder.getPanel();
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if ("fecha".equals(property)) {
			JLabel ln = BasicComponentFactory.createLabel(model
					.getModel(property), new SimpleDateFormat(
					"dd / MMMM  / yyyy"));
			ln.setFont(ln.getFont().deriveFont(Font.BOLD));
			ln.setEnabled(false);
			return ln;
		} else if ("banco".equals(property)) {
			String hql = "select b.nombre from Banco b  ";
			List data = Services.getInstance().getHibernateTemplate().find(hql);
			SelectionInList sl = new SelectionInList(data, model
					.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		} else if ("cuenta".equals(property)) {
			String hql = "from Cuenta c where c.activoEnVentas=true";
			List data = Services.getInstance().getHibernateTemplate().find(hql);
			SelectionInList sl = new SelectionInList(data, model
					.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		} else if ("efectivo".equals(property) || "cheque".equals(property)
				|| "transferencia".equals(property)) {
			return Binder.createBigDecimalForMonyBinding(model
					.getModel(property));
		} else if("cliente".equals(property)){
			return buildClienteControl();
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
				Cliente c=Services.getInstance().getClientesManager().buscarPorClave(nombreField.getText());
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
	
	public static PagoConDeposito modificar(PagoConDeposito deposito) {
		//DefaultFormModel model = new DefaultFormModel(deposito);
		PagoconDepositoFormModel model=new PagoconDepositoFormModel(deposito);
		PagoConDepositoForm form = new PagoConDepositoForm(model);
		form.open();
		if (!form.hasBeenCanceled()) {
			return (PagoConDeposito) model.getBaseBean();
		}
		return null;
	}
	
	public static PagoConDeposito generar() {
		PagoConDeposito pago=new PagoConDeposito();
		pago.setOrigen(OrigenDeOperacion.MOS);
		pago.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		pago.setFecha(Services.getInstance().obtenerFechaDelSistema());
		//pago.setFechaDeposito(pago.getFecha());
		//DefaultFormModel model = new DefaultFormModel(pago);
		PagoconDepositoFormModel model=new PagoconDepositoFormModel(pago);
		PagoConDepositoForm form = new PagoConDepositoForm(model);
		
		form.open();
		if (!form.hasBeenCanceled()) {
			return (PagoConDeposito) model.getBaseBean();
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
				/*
				 * PagoConDeposito
				 * deposito=(PagoConDeposito)Services.getInstance()
				 * .getPagosManager
				 * ().getAbono("8a8a81c7-2556092b-0125-560b4f4a-0001");
				 * showObject(modificar(deposito));
				 */
				
				showObject(generar());
				System.exit(0);
			}

		});
	}

}
