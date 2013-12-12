package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.math.BigDecimal;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.GradientBackgroundPanel;
import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeAnticiposFacturadosPendientes;
import com.luxsoft.siipap.pos.ui.venta.forms.AplicacionDeDisponibleForm.AnticipoModel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.ventas.Pedido;

/**
 * Forma para la facturacion de pedidos de credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class FacturacionDeCreditoForm extends SXAbstractDialog implements KeyEventPostProcessor{
	
	
	private Pedido pedido;
	private EventList facturas;
	
	private JLabel folioLabel;
	private JLabel clienteLabel;
	private JLabel totalLabel;
	private JLabel fechaLabel;
	private JLabel cargosLabel;
	private JLabel fleteLabel;
	private JLabel origenLabel;
	private JLabel formaDePagoLabel;
	private User user;

	private final Font TABLE_HEADER_FONT=new java.awt.Font("SansSerif", 0, 14);
	private final Font BUTTONS_FONT=new java.awt.Font("SansSerif", 0, 13);
	

	public FacturacionDeCreditoForm(final Pedido pedido,Dialog owner) {
		super(owner,"Facturación de crédito 		"+pedido.getSucursal());
		if(pedido==null)
			throw new IllegalArgumentException("El pedido no puede ser nulo");		
		this.pedido=pedido;
		initComponents();
	}
	
	private void initComponents(){
		folioLabel=createTitleLable("");		
		totalLabel=createTitleLable("");
		clienteLabel=createTitleLable("");
		fechaLabel=createTitleLable("");
		cargosLabel=createTitleLable("");
		fleteLabel=createTitleLable("");
		origenLabel=createTitleLable("");
		formaDePagoLabel=createTitleLable("");
		updatePedido();
	}
	
	private JLabel createTitleLable(String title){
		return UIFactory.createBoldLabel(title,5);
	}
	
	private void updateTitleLabel(JLabel l){
		l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D()+5));
		l.setForeground(UIManager.getColor("TitledBorder.titleColor"));
	}
	
	protected void updatePedido(){
		folioLabel.setText(""+pedido.getId());
		totalLabel.setText(pedido.getTotalMN().toString());
		clienteLabel.setText(pedido.getNombre());
		origenLabel.setText(pedido.getOrigen());
		fechaLabel.setText(DateUtil.convertDateToString(pedido.getFecha()));
		formaDePagoLabel.setText(pedido.getFormaDePago().name());
	}
	
	
	
	@Override
	protected JComponent buildContent() {
		JPanel panel=new GradientBackgroundPanel(true);
		panel.setLayout(new VerticalLayout(0));
		panel.add(buildTopComponent());	
		
		panel.add(buildFacturasPanel());
		JComponent buttonBar=buildButtonBarWithOKCancel();
		buttonBar.setOpaque(false);
		panel.add(buttonBar);		
		return panel;
	}
	
	protected JComponent buildTopComponent(){
		
		final FormLayout layout=new FormLayout(
				"max(p;50dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu)"
				,"");
		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		//JLabel l1=builder.append("Pedido ", folioLabel);
		updateTitleLabel(builder.append("Pedido ", folioLabel));
		updateTitleLabel(builder.append("",clienteLabel));
		updateTitleLabel(builder.append("",origenLabel));
		
		updateTitleLabel(builder.append("Fecha",fechaLabel,5));
		
		updateTitleLabel(builder.append("",formaDePagoLabel));
		
		updateTitleLabel(builder.append("",cargosLabel));
		updateTitleLabel(builder.append("",fleteLabel));
		updateTitleLabel(builder.append("Total",totalLabel));
		
		
		builder.getPanel().setOpaque(false);
		return builder.getPanel();
	}
	
	protected JComponent buildFacturasPanel(){
		String[] props=new String[]{"numeroFiscal","documento","importe","cargos","Flete","impuesto","total"};
		String[] labels=new String[]{"Factura","Docto","Importe","Cargos","Flete","Impuesto","Total"};
		TableFormat tf=GlazedLists.tableFormat(props, labels);
		EventTableModel model=new EventTableModel(getFacturas(),tf);
		JXTable grid=ComponentUtils.getStandardTable();
		grid.getTableHeader().setFont(TABLE_HEADER_FONT);
		grid.setFont(TABLE_HEADER_FONT);
		grid.setColumnControlVisible(false);
		grid.setModel(model);
		JComponent co=ComponentUtils.createTablePanel(grid);
		co.setPreferredSize(new Dimension(600,130));
		co.setOpaque(false);
		co.setBorder(getDialogBorder());
		return co;
	}
	
	 protected JComponent buildButtonBarWithOKCancel() {
		 getOKAction().putValue(Action.NAME, "Facturar [F10]");
		 getOKAction().putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		 getCancelAction().putValue(Action.NAME,"CANCELAR [ESC]");
		 JButton[] buttons=new JButton[]{
				 createAplicarAnticipoButton(),
				 //createCambioDeFormaDePagoButton(),
		         createFacturarButton(),
		         createCancelButton() 
		 };
		 for(JButton btn:buttons){
			 btn.setFont(BUTTONS_FONT);
		 }
	     JPanel bar = ButtonBarFactory.buildRightAlignedBar(buttons);
	     bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
	     return bar;
	 }
	 
	 private JButton createFacturarButton(){
		 JButton btn=new JButton("Facturar [F10]",getIconFromResource("images/file/printview_tsk.gif"));
		 btn.setToolTipText("Generar factura(s)");
		 btn.addActionListener(EventHandler.create(ActionListener.class, this, "facturar"));
		 setDefaultButton(btn);
		 return btn;
	 }
	 
	 private JButton createCambioDeFormaDePagoButton(){
		 JButton btn=new JButton("Forma de Pago",getIconFromResource("images2/creditcards.png"));
		 btn.setToolTipText("Cambiar la forma de pago. Puede modificar los importes");
		 btn.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarFormaDePago"));
		 return btn;
	 }
	 
	 private JButton createAplicarAnticipoButton(){
		 JButton btn=new JButton("Anticipo",getIconFromResource("images2/BLN.PNG"));
		 btn.setToolTipText("Aplicar anticipo facturado");
		 btn.addActionListener(EventHandler.create(ActionListener.class, this, "aplicarAnticipo"));
		 return btn;
	 }

	public void facturar(){
		/*int res=JOptionPane.showConfirmDialog(this, "Prepare su impresor y verifique el consecutivo","Facturando",JOptionPane.OK_CANCEL_OPTION);
		if(JOptionPane.OK_OPTION==res){
			doAccept();
		}*/
	}
	
	AnticipoModel anticipo;
	
	public void aplicarAnticipo(){
		if(!user.hasRole(POSRoles.CONTROLADOR_DE_ANTICIPOS.name())){
			MessageUtils.showMessage("No tiene acceso a esta opción ", "Aplicación de anticipos facturados");
			return;
		}
		Venta anticipo=SelectorDeAnticiposFacturadosPendientes.seleccionar(pedido.getClave());
		if(anticipo!=null){
			BigDecimal total=pedido.getTotal();
			BigDecimal dispo=anticipo.getDisponibleDeAnticipo();
			BigDecimal aplicado=BigDecimal.ZERO;
			if(dispo.compareTo(total)>=0){
				aplicado=total;
			}else{
				aplicado=dispo;
			}
			aplicado=AplicacionDeDisponibleForm.getImporteAplicado(anticipo,aplicado);
			if(aplicado.doubleValue()>0){				
				this.anticipo=new AnticipoModel();
				this.anticipo.setAnticipo(anticipo);
				this.anticipo.setImporte(aplicado);
			}
		}
		/*
		String res=JOptionPane.showInputDialog(this, "Nuevo consecutivo");
		
		if(NumberUtils.isNumber(res)){
			int next=Integer.valueOf(res);
			for(int index=0;index<getFacturas().size();index++){
				Venta v=(Venta)facturas.get(index);
				v.setNumeroFiscal(next++);
				facturas.set(index, v);
			}
		}else{
			JOptionPane.showMessageDialog(this, "Número invalido");
			return;
		}
		*/
	}
	 
	 @Override
	protected void onWindowOpened() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(this);			
	}

	@Override
	public void close() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(this);			
		super.close();
	}
	 
	 public boolean postProcessKeyEvent(final  KeyEvent e) {
		if(KeyEvent.KEY_PRESSED==e.getID()){
			if(KeyStroke.getKeyStroke("F10").getKeyCode()==e.getKeyCode()){
				if(isFocused()){
					e.consume();
					facturar();
					return true;
				}
			}
		}
		return false;
	}
	 
	public EventList getFacturas() {
		return facturas;
	}

	public void setFacturas(EventList facturas) {
		this.facturas = facturas;
	}

	public Pedido getPedido() {
		return pedido;
	}
	public void setPedido(Pedido pedido) {
		if(pedido==null)
			throw new IllegalArgumentException("El pedido no puede ser nulo");
		this.pedido = pedido;
		updatePedido();
	}
	
	

	public AnticipoModel getAnticipo() {
		return anticipo;
	}
	
	

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
				Pedido pedido=new Pedido();
				pedido.setNombre("Union de Credito");
				pedido.setTotal(BigDecimal.valueOf(50000));
				FacturacionDeCreditoForm form=new FacturacionDeCreditoForm(pedido,null);
				form.setFacturas(new BasicEventList());
				form.open();
				System.exit(0);
			}

		});
	}

}
