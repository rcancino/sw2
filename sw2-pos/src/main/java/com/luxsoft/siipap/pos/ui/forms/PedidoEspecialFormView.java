package com.luxsoft.siipap.pos.ui.forms;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.pos.ui.utils.UIUtils;
import com.luxsoft.siipap.pos.ui.venta.forms.PedidoHeader;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoDet;

/**
 * Forma general para la generacion y  mantenimiento de Pedidos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PedidoEspecialFormView extends AbstractForm {

	private final Font TITLE_LABEL_FONT=new java.awt.Font("SansSerif", 1, 13);
	
	PedidoHeader header;	

	public PedidoEspecialFormView(DefaultFormModel model) {
		super(model);
		setTitle("Pedido  "+model.getValue("folio").toString()+"      ["+model.getValue("sucursal")+"]");
	}
	
	
	@Override
	protected JComponent buildHeader() {		
		header=new PedidoHeader();
		header.setPedido((Pedido)model.getBaseBean());
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
		panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);	
		
		afterMainPanelBuild(panel);
		return panel;
	}

	 protected JComponent buildButtonBarWithOKCancel() {
	        JPanel bar = ButtonBarFactory.buildRightAlignedBar(new JButton[]{	        		
		            createOKButton(false),
		            createCancelButton()	
	        });
	        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
	        return bar;
	}
	
	@Override
	protected JComponent buildFormPanel() {
		
		final FormLayout layout=new FormLayout(
				"p,5dlu,p, 7dlu," +
				"p,5dlu,p, 7dlu," +
				"p,5dlu,p, 7dlu," +
				"p,5dlu,p:g"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append(getTitleLabel("Fecha"),addReadOnly("fecha"));
		builder.append(getTitleLabel("Estado"),addReadOnly("estado"));
		builder.append("Tipo",getControl("tipo"));
		builder.append(getTitleLabel("Pago"),getControl("formaDePago"));
		builder.nextLine();	
		
		builder.append("Entrega",getControl("entrega"),5);
		builder.append("Facturista",getControl("operador"));
		builder.append("Socio",getControl("socio"));
		builder.nextLine();	
		
		builder.append("Dirección",getControl("instruccionDeEntrega"),5);
		builder.append("Comentario",getControl("comentario"),5);			
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
		builder.add(buildTotalesPane_1()  ,cc.xy(3, 1));
		builder.add(buildTotalesPane_2()  ,cc.xy(5, 1));
		builder.add(buildTotalesPane_3()  ,cc.xy(7, 1));
		
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
		builder.append("Bruto ",addReadOnly("importeBruto"),true);
		builder.append("Descuento",addReadOnly("importeDescuento"));
		builder.append("Cortes ",addReadOnly("importeCorte"),true);
		builder.append("Sub Total 1 ",  addReadOnly("subTotal1"),true);
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
		builder.appendSeparator("Cargos");		
		builder.append("Comisiones ",addReadOnly("comisionTarjetaImporte"));
		builder.append("Flete ",        addReadOnly("flete"),true);
		
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
		builder.append("Sub Total 2 ",  addReadOnly("subTotal"));
		builder.append("Impuesto ",     addReadOnly("impuesto"));
		builder.append("A Pagar  ",       addReadOnly("total"));
		return builder.getPanel();
	}
	
	private JLabel getTitleLabel(String text){
		JLabel label=DefaultComponentFactory.getInstance().createTitle(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD,13F));
		return label;
	}
	
	
	
	@Override
	protected JComponent createCustomComponent(String property) {
		return BasicComponentFactory
			.createLabel(model.getModel(property),UIUtils.buildToStringFormat());
	}
	
	
	
	protected void afterMainPanelBuild(JPanel panel){		
		ajustarFont(panel);			
	}	
	
	private void ajustarFont(Container panel){
		UIUtils.increaseFontSize(panel, 1f);
	}
	
	
	protected JPanel buildToolbarPanel(){		
		getViewAction().putValue(Action.NAME, "Consultar");		
		
		
		JButton consolidarButton=new JButton("Consolidar cortes");
		consolidarButton.addActionListener(EventHandler.create(ActionListener.class, this, "consolidarCortes"));
		
		JButton buttons[]={		
				new JButton(getViewAction())				
		};
		return ButtonBarFactory.buildLeftAlignedBar(buttons,true);
	}
	
	
	private JXTable grid;
	private EventSelectionModel<PedidoDet> selectionModel;
	
	protected JComponent buildGridPanel(){
		String[] propertyNames={"clave","descripcion","producto.gramos"
				,"producto.calibre","cantidad","instruccionesDecorte"
				//,"backOrder"
				,"precio","importeBruto","descuento","importeDescuento","cortes","importeCorte","subTotal"};
		
		String[] columnLabels={"Clave","Producto","(g)","cal","Cant","Corte"
				//,"B.O."
				,"Precio","Imp Bruto","Des (%)","Des ($)","Cortes (#)","Cortes ($)","Sub Total"};
		final TableFormat tf=GlazedLists.tableFormat(PedidoDet.class,propertyNames, columnLabels);
		Pedido pedido=(Pedido)model.getBaseBean();
		EventList source=GlazedLists.eventList(pedido.getPartidas());
		source=new SortedList(source,GlazedLists.beanPropertyComparator(PedidoDet.class, "log.creado"));
		final EventTableModel tm=new EventTableModel(source,tf);
		grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<PedidoDet>(source);
		//selectionModel.addListSelectionListener(this);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.decorateActions(grid);
		grid.setEnabled(!model.isReadOnly());
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(850,200));		
		Font font=grid.getFont().deriveFont(grid.getFont().getSize2D()+2);		
		grid.setFont(font);
		grid.setColumnControlVisible(false);
		grid.getColumnExt("Des (%)").setCellRenderer(Renderers.getPorcentageRenderer());
		
		return gridComponent;		
	}

	

	@Override
	protected void onWindowOpened() {
		grid.packAll();
	}
	
	public static void showPedido(final Pedido pedido){
		DefaultFormModel model=new DefaultFormModel(pedido);
		PedidoEspecialFormView form=new PedidoEspecialFormView(model);
		form.open();
	}
	
	public static void showPedido(final String pedidoId){
		Pedido pedido=Services.getInstance()
			.getPedidosManager()
			.get(pedidoId);
		showPedido(pedido);
	}


		

}
