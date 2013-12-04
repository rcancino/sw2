package com.luxsoft.siipap.pos.ui.venta.forms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ConverterFactory;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.ActionLabel;
import com.jgoodies.uifextras.util.UIFactory;
import com.jgoodies.validation.view.ValidationResultViewFactory;
import com.luxsoft.siipap.pos.ui.utils.UIUtils;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.UpperCaseField;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.ui.selectores.SelectorDeDescuento;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Forma general para la generacion y  mantenimiento de Pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoForm_bak extends AbstractForm implements ActionListener,ListSelectionListener{

	private final Font TITLE_LABEL_FONT=new java.awt.Font("SansSerif", 1, 13);
	
	PedidoHeader header;
	
	final BeanHandler beanHandler;
	
	private final KeyEventPostProcessor keyHandler;

	public PedidoForm_bak(final PedidoController controller) {
		super(controller);
		setTitle("Pedido    ["+model.getValue("sucursal")+"]");
		beanHandler=new BeanHandler();		
		model.addBeanPropertyChangeListener(beanHandler);
		model.getModel("entrega").addValueChangeListener(new PropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent evt) {
				
				if(Pedido.FormaDeEntrega.ENVIO_CARGO.equals(evt.getNewValue())){
					if(!getController().getPedido().isMismaDireccion())
						updateFleteControl();
					
				}
					
				
			}
			
		});
		model.getModel("mismaDireccion").addValueChangeListener(new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				
					//getControl("entrega").setEnabled(false);
					entregaLabel.setEnabled(!getController().getPedido().isMismaDireccion());
			}
		});
		keyHandler=new KeyHandler();
	}
	
	private void updateFleteControl(){
		//boolean res=getController().getPedido().getEntrega().equals(Pedido.FormaDeEntrega.ENVIO_CARGO);
		// getControl("flete").setEnabled(res);
	}
	
	public PedidoController getController(){
		return (PedidoController)getModel();
	}
	
	 
	
	@Override
	protected JComponent buildHeader() {		
		header=new PedidoHeader();
		header.setPedido(getController().getPedido());
		if(model.getValue("id")!=null)
			header.updateHeader();
		return header;
	}	
	
	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new VerticalLayout(8));		
		panel.add(buildFormPanel());				
		//Agregamos el grid
		panel.add(buildGridPanel());		
		//Agregamos el Panel de botones (Toolbar)
		panel.add(buildToolbarPanel());		
		//Agrgamos panel de validaciones/totales
		panel.add(buildSouthPanel());		
		// Agregamos la seccion de Salvar/Cancelar
		if(model.isReadOnly()){
			panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);			
		}else
			panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		
		afterMainPanelBuild(panel);
		return panel;
	}

	 protected JComponent buildButtonBarWithOKCancel() {
	        JPanel bar = ButtonBarFactory.buildRightAlignedBar(new JButton[]{
	        		//new JButton(getAutorizarAction()),
	        		//new JButton(getFacturarAction()),
		            createOKButton(false),
		            createCancelButton()	
	        });
	        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
	        return bar;
	}
	 
	 final ActionLabel entregaLabel=new ActionLabel("Dirección");
	
	@Override
	protected JComponent buildFormPanel() {
		//Maestro de la forma
		final FormLayout layout=new FormLayout(
				"p,2dlu,150dlu, 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu), 2dlu," +
				"max(p;50dlu),2dlu,max(p;90dlu)"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		if(getController().getPedido().getId()==null){
			builder.append(getTitleLabel("&Clave"),buildClienteControl());
		}
		builder.append(getTitleLabel("Fecha"),addReadOnly("fecha"));
		builder.append(getTitleLabel("Estado"),addReadOnly("estado"));
		builder.append("Anticipo",getControl("anticipo"));
		builder.nextLine();		
		
		builder.append("Tipo",getControl("tipo"));
		builder.append(getTitleLabel("Pago"),getControl("formaDePago"));
		//addReadOnly("descripcionFormaDePago").setBorder(null);
		builder.append("Opción",getControl("checkplusOpcion"));
		builder.append("Socio",getControl("socio"));
		builder.nextLine();
		//CellConstraints cc=new CellConstraints();
		
		
		builder.append("Comentario",getControl("comentario"));
		builder.append("Puesto",getControl("puesto"));
		builder.append("Entre" +
				"ga",getControl("entrega"));
		
		
		entregaLabel.setFont(TITLE_LABEL_FONT);
		entregaLabel.addActionListener(EventHandler.create(ActionListener.class, getController(), "definirFormaDeEntrega"));
		builder.append(entregaLabel,getControl("mismaDireccion"));
		
		builder.append("Comprador",getControl("comprador")).setToolTipText("Nombre de la persona que realiza el pedido");
		builder.append("Comprador",getControl("mismoComprador"));
		builder.append("Facturista",getControl("facturista"));
		builder.append("",addReadOnly("operador"));
		
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		if(getController().getPedido().getId()==null)
			ComponentUtils.decorateTabFocusTraversal(claveField);
		return builder.getPanel();
	}
	
	/**
	 * Fabrica la parte sur de la forma
	 * @return
	 */
	private JComponent buildSouthPanel(){
		FormLayout layout=new FormLayout("p:g,3dlu,l:p,3dlu,r:p,3dlu,r:p"
				,"t:p,3dlu");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.add(buildValidationPanel(),cc.xy(1, 1));
		//builder.add(buildTotalesPane_1(),cc.xy(3, 1));
		builder.add(buildTotalesPane_2(),cc.xy(5, 1));
		builder.add(buildTotalesPane_3(),cc.xy(7, 1));
		
		return builder.getPanel();
	}
	
	/**
	 * Primera seccion del panel de totales
	 * 
	 * @return
	 */
	private JComponent buildTotalesPane_1(){
		FormLayout layout=new FormLayout(
				"p,2dlu,80dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.appendSeparator("Importes ");		
		//builder.append("Bruto ",addReadOnly("importeBruto"),true);
		//builder.append("Descuento",addReadOnly("importeDescuento"));
		//builder.append("Cortes ",addReadOnly("importeCorte"),true);
		//builder.append("Sub Total 1 ",  addReadOnly("subTotal1"),true);
		return builder.getPanel();
	}
	/**
	 * Segunda sección del panel de totales
	 * @return
	 */
	private JComponent buildTotalesPane_2(){
		FormLayout layout=new FormLayout(
				"p,2dlu,80dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.appendSeparator("Cargos (Informativos)");
		builder.append("Cortes ",addReadOnly("importeCorte"),true);
		builder.append("Maniobra T ",addReadOnly("comisionTarjetaImporte"));
		builder.append("Maniobra F", getControl("flete"),true);
		updateFleteControl();
		
		
		return builder.getPanel();
	}
	/**
	 * Tercera  sección del panel de totales
	 * @return
	 */
	private JComponent buildTotalesPane_3(){
		FormLayout layout=new FormLayout(
				"p,2dlu,80dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.appendSeparator("Total");
		builder.append("Bruto ",addReadOnly("importeBruto"),true);
		builder.append("Descuento",addReadOnly("importeDescuento"),true);
		builder.append("Sub Total ",  addReadOnly("subTotal"));
		builder.append("Impuesto ",     addReadOnly("impuesto"));
		builder.append("A Pagar  ",       addReadOnly("total"));
		return builder.getPanel();
	}
	
	protected JComponent buildValidationPanel(){
		Color color=UIManager.getColor("Label.background");
		JComponent c=ValidationResultViewFactory.createReportList(model.getValidationModel(),color);
		c.setBorder(null);
		JScrollPane sp=(JScrollPane)c;
		sp.setBorder(BorderFactory.createTitledBorder(Borders.EMPTY_BORDER,"Validación"));		
		JList list=(JList)sp.getViewport().getView();
		list.setFont(new java.awt.Font("SansSerif", 1, 13));
		return c;
	}
	
	
	private JLabel getTitleLabel(String text){
		JLabel label=DefaultComponentFactory.getInstance().createTitle(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD,13F));
		return label;
	}
	
	private JTextField claveField;	
	//private JTextField rfcField;
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("clave".equals(property)){
			//claveField=Binder.createMayusculasTextField(model.getModel(property));
			claveField=new UpperCaseField();
			claveField.addActionListener(this);
			claveField.getActionMap().put("insert", getInsertAction());
			claveField.getInputMap().put(KeyStroke.getKeyStroke("INSERT"), "insert");
			
			return claveField;
		}else if("tipo".equals(property)){
			final SelectionInList sl=new SelectionInList(Pedido.Tipo.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setPrototypeDisplayValue("M O S T R A D O R");
			return box;
		}else if("formaDePago".equals(property)){
			final SelectionInList sl=new SelectionInList(getController().getFormasDePago(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			box.setPrototypeDisplayValue("TARJETA DE CREDITO");
			return box;
		}else if("entrega".equals(property)){
			final SelectionInList sl=new SelectionInList(Pedido.FormaDeEntrega.values(),model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}else if(property.startsWith("importe")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if(property.startsWith("impu") || property.startsWith("total") 
				|| property.startsWith("flete") || property.equals("comisionTarjetaImporte")
				|| property.startsWith("sub") 
				)
		{
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property),true);
			return c;
		}else if("fecha".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createFormattedTextField(model.getModel(property), new SimpleDateFormat("dd/MM/yyyy"));
			tf.setEditable(false);
			//tf.setFocusable(false);
			return tf;
		}else if("instruccionDeEntrega".equals(property)){
			JTextArea ta=UIFactory.createMultilineLabel("<h3>Dirección</h3>");
			ta.setBorder(BorderFactory.createEtchedBorder());
			ValueModel vm=ConverterFactory.createStringConverter(model.getModel(property),new  InstruccionDeEntrega.StringFormat());
			//BasicComponentFactory.createLabel(valueModel, format)
			Bindings.bind(ta, vm);
			return UIFactory.createStrippedScrollPane(ta);
		}else if("socio".equals(property)){
			ActionLabel label=new ActionLabel(new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					getController().modificarSocio();
				}
			});
			ValueModel vm=ConverterFactory.createStringConverter(model.getModel(property), UIUtils.buildToStringFormat());
			Bindings.bind(label, vm);
			return label;
		}else if("mismaDireccion".equals(property)){
			JComponent c=BasicComponentFactory.createCheckBox(model.getModel(property), "Misma");
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("comprador".equals(property)){
			JComponent c=Binder.createMayusculasTextField(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("mismoComprador".equals(property)){
			JComponent c=BasicComponentFactory.createCheckBox(model.getModel(property), "Mismo");
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("facturista".equals(property)){
			JComponent c=BasicComponentFactory.createPasswordField(getController().getPasswordHolder(),true);
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("operador".equals(property)){
			return BasicComponentFactory.createLabel(getController().getFacturistaHolder());
		}else if("checkplusOpcion".equals(property)){
			ActionLabel label=new ActionLabel(new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					getController().modificarCheckplus();
				}
			});
			ValueModel vm=ConverterFactory.createStringConverter(model.getModel(property), UIUtils.buildToStringFormat());
			Bindings.bind(label, vm);
			return label;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent buildClienteControl(){
		FormLayout layout=new FormLayout("70dlu:g,2dlu,p,2dlu,p","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		JButton bt1=new JButton(getLookupAction());
		bt1.setFocusable(false);
		JButton bt2=new JButton(getInsertCliente());
		bt2.setFocusable(false);
		builder.append(getControl("clave"),bt1,bt2);
		return builder.getPanel();
	}
	
	protected void afterMainPanelBuild(JPanel panel){
		model.validate();
		ajustarActions(panel);
		//header.updateHeader();
		ajustarFont(panel);	
		
	}	
	
	private void ajustarFont(Container panel){
		UIUtils.increaseFontSize(panel, 1f);
	}
	
	
	protected JPanel buildToolbarPanel(){
		getInsertAction().putValue(Action.NAME, "Agregar [INS]");
		getInsertAction().putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("INSERT"));
		getInsertAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/cart_add.png"));
		getDeleteAction().putValue(Action.NAME, "Eliminar [SUP]");
		getEditAction().putValue(Action.NAME, "Editar [F11]");
		getViewAction().putValue(Action.NAME, "Consultar");
		
		JButton insertButton=new JButton(getInsertAction());
		setDefaultButton(insertButton);
		
		JButton consolidarButton=new JButton("Consolidar cortes");
		consolidarButton.addActionListener(EventHandler.create(ActionListener.class, this, "consolidarCortes"));
		
		JButton buttons[]={				
				insertButton
				,new JButton(getDeleteAction())
				,new JButton(getEditAction())
				//,new JButton(getImprimirAction())
				,new JButton(getDescuentoEspecialAction())
				,new JButton(getPrecioEspecialAction())
				,consolidarButton
		};
		return ButtonBarFactory.buildLeftAlignedBar(buttons,true);
	}
		
	private Action lookupAction;
	private Action insertCliente;
	private Action imprimirAction;
	private Action descuentoEspecialAction;	
	private Action facturarAction;
	private Action precioEspecialAction;	
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"seleccionarCliente");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}
	
	public Action getInsertCliente(){
		if(insertCliente==null){
			insertCliente=new DispatchingAction(getController(),"insertarClienteNuevo");
			insertCliente.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/user_add.png"));
			insertCliente.putValue(Action.NAME, "F7");			
		}
		return insertCliente;
	}
	
	public Action getImprimirAction(){
		if(imprimirAction==null){
			imprimirAction=CommandUtils.createPrintAction(this, "imprimir");
			imprimirAction.putValue(Action.NAME, "Imprimir [F12]");
		}
		return imprimirAction;
	}
	public Action getDescuentoEspecialAction(){
		if(descuentoEspecialAction==null){
			descuentoEspecialAction=new DispatchingAction(this, "solicitarDescuentoEspecial");
			descuentoEspecialAction.putValue(Action.NAME, "Descto Especial");
			descuentoEspecialAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_delete.png"));
		}
		return descuentoEspecialAction;
	}
	
	public Action getPrecioEspecialAction(){
		if(precioEspecialAction==null){
			precioEspecialAction=new DispatchingAction(this, "solicitarPrecioEspecial");
			precioEspecialAction.putValue(Action.NAME, "Precio Especial");
			//precioEspecialAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_delete.png"));
			precioEspecialAction.setEnabled(!getModel().isReadOnly());
		}	
		return precioEspecialAction;
	}	
	public Action getFacturarAction(){
		if(facturarAction==null){
			facturarAction=new DispatchingAction(this, "facturar");
			facturarAction.putValue(Action.NAME, "Facturar");
			facturarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/money_dollar.png"));
		}
		return facturarAction;
	}
	
	protected void ajustarActions(JPanel panel){
		getOKAction().putValue(Action.NAME, "Salvar [F10]");
		getOKAction().putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/edit/save_edit.gif"));
		getCancelAction().putValue(Action.NAME, "Cancelar");
		ComponentUtils.addAction(panel, new AbstractAction(){			
			public void actionPerformed(ActionEvent e) {
				if(getOKAction().isEnabled())
					getOKAction().actionPerformed(null);
			}
		}, 
		KeyStroke.getKeyStroke("F10"), JComponent.WHEN_IN_FOCUSED_WINDOW);
		ComponentUtils.addInsertAction(panel, getInsertAction());
	}
	
	
	private JXTable grid;
	private EventSelectionModel<PedidoDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"clave","descripcion","producto.gramos"
				,"producto.calibre","cantidad"
				//,"backOrder"
				,"precio"
				//,"precioEspecial"
				,"importeBruto","descuento"
				,"importeDescuento"
				,"subTotal"
				,"descripcionCorte"
				,"cortes"
				,"importeCorte"
				,"log.creado"
				};
		
		String[] columnLabels={"Clave","Producto","(g)","cal","Cant"
				//,"B.O."
				,"Precio"
				//,"Precio Esps"
				,"Imp Bruto","Des (%)"
				,"Des ($)"
				,"Sub Total"
				,"Corte"
				,"Cortes (#)"
				,"Cortes ($)"
				,"Creado"
				
				};
		final TableFormat tf=GlazedLists.tableFormat(PedidoDet.class,propertyNames, columnLabels);
		final EventTableModel tm=new EventTableModel(getController().getPartidasSource(),tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<PedidoDet>(getController().getPartidasSource());
		selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		ComponentUtils.addInsertAction(grid,getInsertAction());
		ComponentUtils.addDeleteAction(grid, getDeleteAction());
		
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		
		gridComponent.setPreferredSize(new Dimension(850,300));		
		Font font=grid.getFont().deriveFont(grid.getFont().getSize2D()+2);		
		grid.setFont(font);
		grid.setColumnControlVisible(false);
		grid.getColumnExt("Des (%)").setCellRenderer(Renderers.getPorcentageRenderer());
		
		return gridComponent;		
	}
	
	public void insertPartida(){
		getController().insertarPartida();		
		((JXTable)grid).packAll();
		grid.requestFocusInWindow();
	}
	
	
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			PedidoDet selected=(PedidoDet)selectionModel.getSelected().get(0);
			getController().editar(selected);
		}
	}

	public void deletePartida(){
		if(!selectionModel.isSelectionEmpty()){
			for(int index=selectionModel.getMinSelectionIndex();index<=selectionModel.getMaxSelectionIndex();index++){
				getController().eliminarPartida(index);
			}
		}
	}
	
	/**
	 * Implementacion local de {@link ActionListener} para  buscar el cliente por clave
	 * TODO Mover al controlador o LookupUtils
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==claveField){
			String clave=claveField.getText();
			//if(StringUtils.isBlank(clave))
				claveField.transferFocus();
			getController().asignarCliente(clave);
		}
	}
	
	/**
	 * Implementacion de {@link ListSelectionListener} para habilitar o no las acciones
	 * relacionadas con partidas
	 * 
	 * @param e
	 */
	public void valueChanged(ListSelectionEvent e) {
		boolean val=!selectionModel.isSelectionEmpty();
		if(model.isReadOnly()){
			val=false;
		}
		getEditAction().setEnabled(val);
		getDeleteAction().setEnabled(val);
		getViewAction().setEnabled(!selectionModel.isSelectionEmpty());
		
	}
	
	public void solicitarDescuentoEspecial(){
		if(getController().calificaParaDescuentoEspecial()){
			double desc=SelectorDeDescuento.seleccionarEnBase100();
			if(desc>0){
				getController().aplicarDescuentoEspecial(desc);
			}
		}else{
			MessageUtils.showMessage("Este pedido no califica para Descuento especial por:\n" +
					" SER MODIFICABLE O SER DE CREDITO  O NO CONTENER EXCLUSIVAMENTE PRODUCTOS DE PRECIO BRUTO "
					, "Descuento Especial");
		}		
	}
	
	public void solicitarPrecioEspecial(){
		if(getController().getPedido().isFacturado())
			return;
		if(selectionModel.isSelectionEmpty())
			return;
		PedidoDet selected=(PedidoDet)selectionModel.getSelected().get(0);
		BigDecimal precio=selected.getPrecio();
		String res=JOptionPane.showInputDialog(this, "Nuevo precio: ",precio.toString());
		if(NumberUtils.isNumber(res)){
			BigDecimal nuevoPrecio=new BigDecimal(res);			
			getController().asignarPrecioEspecial(selected,nuevoPrecio);
		}
	}

	@Override
	protected void onWindowOpened() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		if(claveField!=null)
			claveField.requestFocusInWindow();
	}

	@Override
	public void close() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventPostProcessor(keyHandler);
		model.removeBeanPropertyChangeListener(beanHandler);
		super.close();
	}
	
	public void seleccionarCliente(){
		getController().seleccionarCliente(this);
	}
	
	public void consolidarCortes(){
		JOptionPane.showMessageDialog(getContentPane(), "EN DESARROLLO");
	}

	/**
	 * Detecta los cambios en el pedido
	 * 
	 * @author Ruben Cancino Ramos
	 */
	private class BeanHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			//logger.info("Propiedad actualizada: "+evt.getPropertyName()+ "Value: "+evt.getNewValue());
			header.updateHeader();
			if(evt.getPropertyName().equals("tipo")){				
				if(Pedido.Tipo.CREDITO.equals(evt.getNewValue())){
					//getControl("formaDePago").setEnabled(false);
				}else
					getControl("formaDePago").setEnabled(true);
			}
		}
	}
	
	/**
	 * Garantiza la ejecucion de ciertas tareas mediante teclas de acceso 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class KeyHandler implements KeyEventPostProcessor{
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rápido
		 * 
		 */
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			if(KeyEvent.KEY_PRESSED==e.getID()){
				//System.out.println("Key Text: "+KeyEvent.getKeyText(e.getKeyCode())+ "  Event type: "+e.getKeyCode()+ " KeyCode:"+e.getKeyCode());
				if(KeyStroke.getKeyStroke("F2").getKeyCode()==e.getKeyCode()){
					if(getController().getPedido().getId()==null){
						if(isFocused()){
							e.consume();
							seleccionarCliente();
							return true;
						}
					}
					
				}else if(KeyStroke.getKeyStroke("F7").getKeyCode()==e.getKeyCode()){
					if(getController().getPedido().getId()==null){
						if(isFocused()){
							e.consume();
							getController().insertarClienteNuevo();
							return true;
						}
					}
					
				}else if(KeyStroke.getKeyStroke("INSERT").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						insertPartida();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("DELETE").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						deletePartida();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("F11").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						edit();
						return true;
					}
				}else if(KeyStroke.getKeyStroke("F3").getKeyCode()==e.getKeyCode()){
					if(isFocused()){
						e.consume();
						claveField.requestFocusInWindow();
						return true;
					}
				}
				
			}								
			return false;
		}
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
				PedidoController model=new PedidoController();
				PedidoForm_bak form=new PedidoForm_bak(model);
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(model.getBaseBean());					
					model.persist();
				}
				System.exit(0);
			}

		});
	}
	

}
