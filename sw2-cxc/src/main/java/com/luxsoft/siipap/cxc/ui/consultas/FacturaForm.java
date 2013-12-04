package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

/**
 * Forma que presenta informacion de la factura solo para lectura
 * 
 * @author Ruben Cancino
 *
 */
public class FacturaForm extends SXAbstractDialog{
	
	protected EventList<Aplicacion> aplicaciones=new BasicEventList<Aplicacion>();;	
	
	private JXTable aplicacionesGrid;
	private JFormattedTextField numero;
	private JFormattedTextField numeroFiscal;
	private JComponent fecha;
	private JFormattedTextField total;
	final private JFormattedTextField saldo=new JFormattedTextField();
	private JCheckBox recibida=new JCheckBox();
	private JCheckBox pbruto=new JCheckBox();
	private JTextField frecibida=new JTextField(10);
	private JCheckBox revisada=new JCheckBox();
	private JTextField frevisada=new JTextField(10);
	private JTextField vendedor;
	private JTextField cortes;
	private JTextField kilos;
	
	private JTextField comentario;
	private JTextField comentario2;
	
	private final PresentationModel model;
	
	private FacturaForm() {
		super("Consulta de facturas");
		model=new PresentationModel(null);
		
	}

	private void initComponents(){		
		numero=Binder.createNumberBinding(model.getComponentModel("documento"),0);
		numero.setEnabled(false);
		fecha=BasicComponentFactory.createDateField(model.getComponentModel("fecha"));
		fecha.setFocusable(false);
		total=Binder.createMonetariNumberBinding(model.getComponentModel("total"));
		total.setFocusable(false);
		
		saldo.setFocusable(false);
		vendedor=new JTextField();
		vendedor.setEnabled(false);
		cortes=Binder.createBigDecimalMonetaryBinding(model.getModel("cargos"));
		cortes.setEnabled(false);
		kilos=new JTextField("N/D");
		kilos.setEnabled(false);
		numeroFiscal=BasicComponentFactory.createIntegerField(model.getModel("numeroFiscal"),NumberFormat.getIntegerInstance());
		numeroFiscal.setEnabled(false);
		numeroFiscal.setFocusable(true);
		pbruto=BasicComponentFactory.createCheckBox(model.getModel("precioBruto"), "");
		pbruto.setEnabled(false);
		
		String c1=model.getModel("comentario").getValue()!=null?StringUtils.trimToEmpty(model.getModel("comentario").getValue().toString()):"";
		String c2=model.getModel("comentario2").getValue()!=null?StringUtils.trimToEmpty(model.getModel("comentario2").getValue().toString()):"";
		comentario=new JTextField(c1);
		comentario.setEnabled(false);
		comentario2=new JTextField(c2);
		comentario2.setEnabled(false);
	}
	
	protected JComponent buildContentPane(){
		JComponent c=super.buildContentPane();
		c.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.setOpaque(false);
		return c;
	}

	@Override
	protected JComponent buildContent() {
		initComponents();
		JTabbedPane tabPanel=new JTabbedPane();
		
		JPanel panel=new JPanel(new BorderLayout());		
		panel.setLayout(new BorderLayout());
		panel.add(buildFormPanel(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);
		
		tabPanel.addTab("General", panel);
		tabPanel.addTab("Detalle", buildPartidasView());
		return tabPanel;
	}
	
	protected JComponent buildButtonBarWithClose() {
		 JButton print=new JButton(CommandUtils.createPrintAction(this, "print"));
		 
		 print.setText("Imprimir");
		 print.setMnemonic('I');
       JPanel bar = ButtonBarFactory.buildRightAlignedBar(new JButton[]{
       		print
       		,createCloseButton(true)
       		
       });
       bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
       return bar;
   }
	
	protected JComponent buildFormPanel(){
		return buildEditorPanel();		
	}
	
	private JComponent buildEditorPanel(){
		FormLayout layout=new FormLayout(
				"l:40dlu,2dlu,50dlu, 2dlu, " +
				"l:40dlu,2dlu,50dlu, 2dlu, " +
				"l:40dlu,2dlu,50dlu:g"
				,"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.append("Numero",numero);
		builder.append("N.Fiscal",numeroFiscal);
		builder.append("Fecha",fecha);
		
		builder.append("Total",total);
		builder.append("Saldo",saldo,true);
		
		builder.append("Facturista",vendedor);
		builder.append("Kilos",kilos);
		builder.append("Cargos",cortes);		
		
		
		builder.append("Recibida",recibida);
		builder.append("F. Rec",frecibida);
		builder.append("P. Bruto",pbruto);
		
		builder.append("Revisada",revisada);
		builder.append("F. Rev",frevisada);
		builder.nextLine();
		
		builder.append("Comentario ",comentario,9);
		builder.append("Comentario 2",comentario2,9);
		
		builder.appendSeparator("Aplicaciones");
		builder.nextLine();
		builder.append(buildAplicacionesGrid(),11);
		//builder.append(buildPagosGrid(),11);
		JPanel p=builder.getPanel();
		p.setEnabled(false);
		return p;
	}
	
	private JComponent buildAplicacionesGrid(){
		final String[] props={"tipo","detalle.formaDePago","abono.folio","fecha","descuentoPorNota","importe","comentario"};
		final String[] cols={"Tipo","F.P","Folio","Fecha","Desc(%)","importe","Comentario"};
		final TableFormat<Aplicacion> tf=GlazedLists.tableFormat(Aplicacion.class,props, cols);
		
		final EventTableModel tm=new EventTableModel(aplicaciones,tf);
		aplicacionesGrid=ComponentUtils.getStandardTable();
		aplicacionesGrid.setModel(tm);
		JComponent c=ComponentUtils.createTablePanel(aplicacionesGrid);
		c.setPreferredSize(new Dimension(300,150));
		return c;		
	}
	
	
	protected EventList<VentaDet> partidas=new BasicEventList<VentaDet>();
	
	private JComponent buildPartidasView(){
		
		final SortedList<VentaDet> sorted=new SortedList<VentaDet>(partidas,null);
		final String[] props={"clave","descripcion","cantidad","precioLista","precio","descuento","descuentoNota","descuentoOriginal"};
		final String[] names={"Articulo","Desc","Cantidad","Precio L","Precio","Des","Des Nota","Desc Orig"};
		final TableFormat<VentaDet> tf=GlazedLists.tableFormat(VentaDet.class, props,names);
		final EventTableModel<VentaDet> tm=new EventTableModel<VentaDet>(sorted,tf);
		
		final JXTable grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		TableComparatorChooser.install(grid,sorted,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		JScrollPane sp=new JScrollPane(grid);
		return sp;
	}
	
	protected HeaderPanel header;
	
	@Override
	protected JComponent buildHeader() {
		return getHeader();
	}
	
	protected HeaderPanel getHeader() {
		if(header==null){
			header= new HeaderPanel("Cliente"
					,""
					,getIconFromResource("images/siipapwin/cxc64.gif"));
		}		
		return header;
	}
	
	private Venta getFactura(){
		return (Venta)model.getBean();
	}
	
	public void setFactura(final Venta v){
		model.setBean(v);
		getHeader().setTitle(v.getCliente().getNombreRazon());
		String pattern="{0}," +
				"\n Tels: {1}" +
				"\n Venta :{2} / {3}";
		String msg=MessageFormat.format(pattern, v.getCliente().getDireccionAsString(),v.getCliente().getTelefonosRow(),v.getDocumento(),v.getNumeroFiscal());
		getHeader().setDescription(msg);
		partidas.clear();
		partidas.addAll(v.getPartidas());
	}
	
	public void cargarAplicaciones(final List<Aplicacion> aps){
		aplicaciones.clear();
		aplicaciones.addAll(aps);
	}
	
	
	
	public void load(){
		aplicacionesGrid.packAll();
		if(getFactura().getCredito()!=null){
			final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
			recibida.setSelected(getFactura().isRecibidaCXC());
			if(getFactura().isRecibidaCXC())
				frecibida.setText(df.format(getFactura().getFechaRecepcionCXC()));
			revisada.setSelected(getFactura().isRevisada());
			if(getFactura().isRevisada())
				frevisada.setText(df.format(getFactura().getFechaRevisionCxc()) );
		}
		if(getFactura()!=null){
			if(getFactura().getVendedor()!=null)
				vendedor.setText(getFactura().getVendedor().toString());
		}		
		if(getFactura()!=null){
			String pattern="{0} ({1})";
			header.setTitle(
					MessageFormat.format(pattern,getFactura().getNombre(),getFactura().getClave()));
			header.setDescription(getFactura().getCliente().getDireccionAsString());
		}
		if(getFactura()!=null){
			partidas.addAll(getFactura().getPartidas());
			saldo.setText(getFactura().getSaldoCalculado().toString());
		}
		
	}
	
	public void print(){
		ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(getFactura());
		if(cf==null){
			final Map parameters=new HashMap();
			String total=ImporteALetra.aLetra(getFactura().getTotalCM());
			parameters.put("CARGO_ID", String.valueOf(getFactura().getId()));
			parameters.put("IMP_CON_LETRA", total);
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaDigital.jasper"), parameters);
		}else{
			CFDPrintServicesCxC.impripirComprobante(getFactura(), cf, null, true);
		}
		doClose();
	}
	
	
	/**
	 * 
	 * @param ventaId
	 * @return true si la venta se modifico
	 */
	public static boolean show(String ventaId){
		final Venta v=ServiceLocator2.getVentasManager().buscarVentaInicializada(ventaId);
		Venta target=(Venta)Bean.proxy(Venta.class);
		Bean.normalizar(v, target, new String[]{"partidas","pedido"});
		target.getPartidas().addAll(v.getPartidas());
		FacturaForm form=new FacturaForm();
		form.saldo.setText(NumberFormat.getCurrencyInstance().format(v.getSaldoCalculado().doubleValue()));
		form.setFactura(target);
		form.cargarAplicaciones(ServiceLocator2.getVentasManager().buscarAplicaciones(v.getId()));
		form.open();
		/*
		if(v.getNumeroFiscal()!=target.getNumeroFiscal()){
			//System.out.println("Actualizar el numero fiscal Source: "+v.getNumeroFiscal()+" Target: "+target.getNumeroFiscal());
			v.setNumeroFiscal(target.getNumeroFiscal());
			ServiceLocator2.getVentasManager().salvar(v);
			return true;
		}
		*/
		return false;
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		DBUtils.whereWeAre();
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				show("8a8a8189-215f17da-0121-5f251b9e-0a25");
			}
			
		});
		
		
	}

}
