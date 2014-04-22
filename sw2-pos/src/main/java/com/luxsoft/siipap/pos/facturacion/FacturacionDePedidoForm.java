package com.luxsoft.siipap.pos.facturacion;

import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.consultas.caja.CajaController;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeAbonosDisponibles;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeDisponibles;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;

public class FacturacionDePedidoForm extends AbstractForm {

	private final PresentationModel pedidoModel;
	private KeyHandler keyHandler;
	private CajaController cajaController;

	public FacturacionDePedidoForm(IFormModel model) {
		super(model);
		pedidoModel = new PresentationModel(model.getModel("pedido"));
		setTitle("Facturación de contado");
		model.getModel("totalPagos").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if(getFacturacionModel().getPedido().getFormaDePago().equals(FormaDePago.DEPOSITO)
					||getFacturacionModel().getPedido().getFormaDePago().equals(FormaDePago.TRANSFERENCIA)	){
					BigDecimal val=(BigDecimal)evt.getNewValue();
					if(val==null)val=BigDecimal.ZERO;
					cobrarButton.setEnabled(val.doubleValue()>0);
				}
			}
		});
		model.getModel("cobrado").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
			if(!getFacturacionModel().getCobrado().equals(BigDecimal.ZERO)){
				createCustomComponent("cobrado").requestFocusInWindow();
				calcularCambio();
				}
			}
		});
	}

	PedidoHeader header;
	
	private FacturacionModel getFacturacionModel(){
		return (FacturacionModel)model.getBaseBean();
	}

	@Override
	protected JComponent buildHeader() {
		header = new PedidoHeader(pedidoModel);
		return header.getControl();
	}

	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout = new FormLayout(
				"p,2dlu,p:g(.5), 3dlu, p,2dlu,p:g(.5)", "");
		final DefaultFormBuilder builder = new DefaultFormBuilder(layout);				
		builder.setLineGapSize(Sizes.DLUX6);
		ActionLabel facturasLabel=new ActionLabel("Facturas [F6]");
		facturasLabel.addActionListener(EventHandler.create(ActionListener.class, this, "mostrarFacturas"));
		ComponentUtils.toTitleLabel(facturasLabel, 3f);
		
		ActionLabel pagosLabel=new ActionLabel("Pagos     [F7]");
		pagosLabel.addActionListener(EventHandler.create(ActionListener.class, this, "mostrarPagos"));
		ComponentUtils.toTitleLabel(pagosLabel, 3f);
		
		builder.append(facturasLabel, addReadOnly("numeroDeFacturas"));
		updateTitleLabel(builder.append("Total facturado", addReadOnly("totalFacturas")));
		builder.append(pagosLabel, addReadOnly("numeroDePagos"));
		updateTitleLabel(builder.append("Pagos", addReadOnly("totalPagos")));
		builder.nextLine();
		updateTitleLabel(builder.append("Por cobrar", addReadOnly("porPagar")));
		
		updateTitleLabel(builder.append("Cobrado", createCustomComponent("cobrado")));
	
		updateTitleLabel(builder.append("Cambio", addReadOnly("cambio"),5));
		
	
		
		if(getFacturacionModel().getPedido().isContraEntrega()){
			builder.nextLine();
			updateTitleLabel(builder.append("PAGO CONTRA ENTREGA"));
		}
		return builder.getPanel();

	}
	
	private void updateTitleLabel(JLabel l){
		ComponentUtils.toTitleLabel(l, 3f);
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		if (property.startsWith("total") || "porPagar".equals(property) || "cobrado".equals(property)  || "cambio".equals(property) ) {
			return Binder.createBigDecimalForMonyBinding(model
					.getModel(property));
		}
		
		
		
		return super.createCustomComponent(property);
	}

	protected JComponent buildButtonBarWithOKCancel() {
		getOKAction().putValue(Action.NAME, "Facturar");
		getOKAction().putValue(Action.SMALL_ICON,getIconFromResource("images/file/printview_tsk.gif"));
		getOKAction().putValue(Action.MNEMONIC_KEY, KeyEvent.VK_F);
		
		formaDePagoButton=createFormaDePagoButton();
		JButton[] buttons = new JButton[] {
				formaDePagoButton
				,createDisponibles()
				,createCobrar()
				,createOKButton(false), createCancelButton() 
				};
		JPanel bar = ButtonBarFactory.buildRightAlignedBar(buttons);
		bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
		return bar;
	}

	UIFButton cobrarButton;	
	
	protected UIFButton createCobrar() {
		UIFButton btn = new UIFButton("Cobrar",
				getIconFromResource("images2/money_add.png"));
		btn.setToolTipText("Generar factura(s)");
		btn.addActionListener(EventHandler.create(ActionListener.class, this,
				"cobrar"));
		btn.setMnemonic(KeyEvent.VK_C);
		if(getFacturacionModel().getPedido().isContraEntrega())
			btn.setEnabled(false);
		FormaDePago formaDePago=getFacturacionModel().getPedido().getFormaDePago();
		switch (formaDePago) {
		case DEPOSITO:
		case TRANSFERENCIA:
			if(getFacturacionModel().getPagos().isEmpty())
				btn.setEnabled(false);
			else
				btn.setEnabled(true);
			break;
		default:
			break;
		}
		cobrarButton=btn;
		return btn;
	}
	
	protected UIFButton createDisponibles() {
		UIFButton btn = new UIFButton("Disponibles"
				//,getIconFromResource("images2/money_add.png")
				);
		btn.setToolTipText("Depositos disponibles para aplicar");
		btn.addActionListener(EventHandler.create(ActionListener.class, this,
				"seleccionarDisponible"));
		btn.setMnemonic(KeyEvent.VK_D);
		if(getFacturacionModel().getPedido().isContraEntrega())
			btn.setEnabled(false);
		return btn;
	}
	
	UIFButton formaDePagoButton;
	
	protected UIFButton createFormaDePagoButton() {
		UIFButton btn = new UIFButton("F.P."
				//,getIconFromResource("images2/money_add.png")
				);
		btn.setToolTipText("Cambiar la forma de pago");
		btn.addActionListener(EventHandler.create(ActionListener.class, this,
				"cambiarFormaDePago"));
		btn.setMnemonic(KeyEvent.VK_P);
		if(getFacturacionModel().getPedido().isContraEntrega())
			btn.setEnabled(false);
		return btn;
	}
	
	public void mostrarFacturas(){
		EventList source=getFacturacionModel().getFacturas();
		String[] props={"documento","numeroFiscal","total"};
		String[] names={"Docto","Fac","total"};
		TableFormat tf=GlazedLists.tableFormat(props, names);
		EventTableModel tm=new EventTableModel(source,tf);
		final JXTable grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		grid.setColumnControlVisible(false);
		grid.packAll();
		final SXAbstractDialog dialog=new SXAbstractDialog("Facturas"){

			@Override
			protected JComponent buildContent() {
				return ComponentUtils.createTablePanel(grid);
				
			}
			
		};
		dialog.open();
	}
	
	public void mostrarPagos(){
		EventList source=getFacturacionModel().getPagos();
		String[] props={"folio","info","total"};
		String[] names={"Folio","Tipo","total"};
		TableFormat tf=GlazedLists.tableFormat(props, names);
		EventTableModel tm=new EventTableModel(source,tf);
		final JXTable grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		grid.setColumnControlVisible(false);
		grid.packAll();
		final SXAbstractDialog dialog=new SXAbstractDialog("Pagos registrados"){

			@Override
			protected JComponent buildContent() {
				return ComponentUtils.createTablePanel(grid);
				
			}
			
		};
		dialog.open();
	}
	
	public void cobrar(){
		getCajaController().registrarPago(getFacturacionModel());
	}
	
	public void seleccionarDisponible(){
		//Pago pago=SelectorDeDisponibles.buscarPago(getFacturacionModel().getPedido().getCliente());
		Abono pago=SelectorDeAbonosDisponibles.buscarPago(getFacturacionModel().getPedido().getCliente());
		
		if(pago!=null){
			pago=Services.getInstance().getPagosManager().getAbono(pago.getId());
			try {
				getFacturacionModel().agregarPago(pago);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(getContentPane(), ExceptionUtils.getRootCauseMessage(e));
			}
		}
	}
	
	public void cambiarFormaDePago(){
		List<FormaDePago> data=new ArrayList<FormaDePago>();
		data.add(FormaDePago.EFECTIVO);
		data.add(FormaDePago.DEPOSITO);
		if(getFacturacionModel().getPagos().isEmpty()){
			data.add(FormaDePago.TARJETA_CREDITO);
			data.add(FormaDePago.TARJETA_DEBITO);
		}
		data.add(FormaDePago.CHEQUE);
		FormaDePago origen=getFacturacionModel().getPedido().getFormaDePago();
		FormaDePago res=(FormaDePago)JOptionPane.showInputDialog(this, "Forma de Pago:", "Cambio de forma de pago", JOptionPane.INFORMATION_MESSAGE, null, data.toArray(), origen);
		if(origen!=null){
			if(!origen.equals(res)){
				formaDePagoButton.setEnabled(false);
				getFacturacionModel().modificarFormaDePago(res);
				
			}
		}
	}
	
	public void open(){
		keyHandler=new KeyHandler();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		super.open();
	}
	public void close(){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		super.close();
	}
	
	
	
	@Override
	public void doAccept() {
		JOptionPane.showMessageDialog(this, "Prepare su impresora");
		super.doAccept();
	}

	public CajaController getCajaController() {
		return cajaController;
	}

	public void setCajaController(CajaController cajaController) {
		this.cajaController = cajaController;
	}



	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			
			if(KeyEvent.KEY_PRESSED==e.getID()){
				if(KeyStroke.getKeyStroke("F5").getKeyCode()==e.getKeyCode()){
					System.out.println(e);
				}else if(KeyStroke.getKeyStroke("F6").getKeyCode()==e.getKeyCode()){
					System.out.println(e);
				}else if(KeyStroke.getKeyStroke("F7").getKeyCode()==e.getKeyCode()){
					System.out.println(e);
				}else if(KeyStroke.getKeyStroke("F8").getKeyCode()==e.getKeyCode()){
					System.out.println(e);
				}else if(KeyStroke.getKeyStroke("F9").getKeyCode()==e.getKeyCode()){
					System.out.println(e);
				}else if(KeyStroke.getKeyStroke("ESCAPE").getKeyCode()==e.getKeyCode()){
					System.out.println("Ejecutando escape");
				}
			}
			return false;
		}
	}
	
	
	private void calcularCambio() {
		BigDecimal camb= BigDecimal.ZERO;
		camb=getFacturacionModel().getCobrado().subtract(getFacturacionModel().getTotalPagos());
		model.setValue("cambio", camb);

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
				Pedido pedido = new Pedido();
				pedido.setCliente(new Cliente("U050008","UNION DE CREDITO"));
				pedido.setTotal(BigDecimal.valueOf(23000.00));
				DefaultFormModel model = new DefaultFormModel(FacturacionModel
						.getModel(pedido));
				final FacturacionDePedidoForm form = new FacturacionDePedidoForm(
						model);
				form.open();
				System.exit(0);
			}

		});
	}

}
