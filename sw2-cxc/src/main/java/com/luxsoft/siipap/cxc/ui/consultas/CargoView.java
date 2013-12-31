package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.luxsoft.cfdi.CFDIPrintUI;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;
//import com.luxsoft.siipap.ventas.model.Venta;
//import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Forma que presenta informacion de la factura solo para lectura
 * 
 * @author Ruben Cancino
 *
 */
public class CargoView extends SXAbstractDialog{
	
	protected EventList<Aplicacion> aplicaciones=new BasicEventList<Aplicacion>();;	
	
	private JXTable aplicacionesGrid;
	private JFormattedTextField numero;
	private JFormattedTextField numeroFiscal;
	private JComponent fecha;
	private JFormattedTextField total;
	private JFormattedTextField saldo;
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
	
	private CargoView() {
		super("Consulta de Nota de Cargo");
		model=new PresentationModel(null);
	}

	private void initComponents(){		
		numero=Binder.createNumberBinding(model.getComponentModel("documento"),0);
		numero.setEnabled(false);
		fecha=BasicComponentFactory.createDateField(model.getComponentModel("fecha"));
		fecha.setFocusable(false);
		total=Binder.createMonetariNumberBinding(model.getComponentModel("total"));
		total.setFocusable(false);
		saldo=Binder.createMonetariNumberBinding(model.getComponentModel("saldoCalculado"));
		saldo.setFocusable(false);
		vendedor=new JTextField();
		vendedor.setEnabled(false);
		cortes=Binder.createBigDecimalMonetaryBinding(model.getModel("cargos"));
		cortes.setEnabled(false);
		kilos=new JTextField("N/D");
		kilos.setEnabled(false);
		numeroFiscal=Binder.createNumberBinding(model.getComponentModel("numeroFiscal"),0);
		numeroFiscal.setFocusable(false);
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
		builder.appendSeparator("Aplicaciones");
		builder.nextLine();

		builder.append("Comentario ",comentario,9);
		builder.append("Comentario 2",comentario2,9);
		builder.nextLine();
		builder.append(buildAplicacionesGrid(),11);
		//builder.append(buildPagosGrid(),11);
		JPanel p=builder.getPanel();
		p.setEnabled(false);
		return p;
	}
	
	private JComponent buildAplicacionesGrid(){
		final String[] props={"tipo","detalle.formaDePago","detalle.folio","fecha","descuentoPorNota","importe"};
		final String[] cols={"Tipo","F.P","Folio","Fecha","Desc(%)","importe"};
		final TableFormat<Aplicacion> tf=GlazedLists.tableFormat(Aplicacion.class,props, cols);
		
		final EventTableModel tm=new EventTableModel(aplicaciones,tf);
		aplicacionesGrid=ComponentUtils.getStandardTable();
		aplicacionesGrid.setModel(tm);
		JComponent c=ComponentUtils.createTablePanel(aplicacionesGrid);
		c.setPreferredSize(new Dimension(300,150));
		return c;		
	}
	
	
	protected EventList<NotaDeCargoDet> partidas=new BasicEventList<NotaDeCargoDet>();
	
	private JComponent buildPartidasView(){
		
		final SortedList  sorted=new SortedList(partidas,null);
		final String[] props={"venta.documento","venta.fecha","venta.total","cargo","importe","comentario"};
		final String[] names={"Factura","Fecha(F)","Total(F)","Cargo","Importe","Comentario"};
		final TableFormat tf=GlazedLists.tableFormat(NotaDeCargoDet.class, props,names);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		
		final JXTable grid=ComponentUtils.getStandardTable();
		grid.setModel(tm);
		TableComparatorChooser.install(grid,sorted,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		JScrollPane sp=new JScrollPane(grid);
		return sp;
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
	
	private NotaDeCargo getCargo(){
		return (NotaDeCargo)model.getBean();
	}
	
	public void setCargo(final NotaDeCargo n){		
		model.setBean(n);
		getHeader().setTitle(n.getCliente().getNombreRazon());
		getHeader().setDescription("N.Cargo:"+n.getDocumento()+ "/"+n.getNumeroFiscal());
		partidas.clear();
		partidas.addAll(n.getConceptos());
		
	}
	
	public void cargarAplicaciones(final List<Aplicacion> aps){
		aplicaciones.clear();
		aplicaciones.addAll(aps);
	}
	
	
	final DateFormat df=new SimpleDateFormat("dd/MM/yyyy");
	
	public void load(){
		aplicacionesGrid.packAll();
		recibida.setSelected(getCargo().isRecibidaCXC());
		frecibida.setText(df.format(getCargo().getFechaRecepcionCXC()));
		revisada.setSelected(getCargo().isRevisada());
		frevisada.setText(df.format(getCargo().getFechaRevisionCxc()) );
		if(getCargo()!=null){
			String pattern="{0} ({1})";
			header.setTitle(
					MessageFormat.format(pattern,getCargo().getNombre(),getCargo().getClave()));
			header.setDescription("");
		}
		if(getCargo()!=null){
			partidas.addAll(getCargo().getConceptos());
		}
	}
	
	
	public void print(){
		CFDI cfdi=ServiceLocator2.getCFDIManager().buscarPorOrigen(getCargo().getId());
		if(cfdi!=null){
			NotaDeCargo nota=CXCUIServiceFacade.buscarNotaDeCargoInicializada(getCargo().getId());
			CFDIPrintUI.impripirComprobante(nota, cfdi, "", new Date(), true);
		}else{
			ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(getCargo());
			
			if(cf==null){
				final Map parameters=new HashMap();
				String total=ImporteALetra.aLetra(getCargo().getTotalCM());
				parameters.put("CARGO_ID", String.valueOf(getCargo().getId()));
				parameters.put("IMP_CON_LETRA", total);
				ReportUtils.viewReport(ReportUtils.toReportesPath("cxc/NotaDeCargoCopia.jasper"), parameters);
			}else{
				CFDPrintServicesCxC.imprimirNotaDeCargoElectronica(getCargo().getId());
			}
		}
				
		doClose();
	}
	
	
	public static void show(String cargoId){
		NotaDeCargo v=ServiceLocator2.getVentasManager().buscarNtaDeCargoInicializada(cargoId);
		NotaDeCargo target=(NotaDeCargo)Bean.proxy(NotaDeCargo.class);		
		Bean.normalizar(v, target, new String[]{"partidas"});
		target.setId(v.getId());
		target.getConceptos().addAll(v.getConceptos());
		CargoView form=new CargoView();
		form.setCargo(target);
		form.cargarAplicaciones(ServiceLocator2.getVentasManager().buscarAplicaciones(v.getId()));
		form.open();
	}

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		DBUtils.whereWeAre();
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				SWExtUIManager.setup();
				show("8a8a8189-215fc6d8-0121-5fc8d512-0549");
			}
			
		});
		
		
	}

}
