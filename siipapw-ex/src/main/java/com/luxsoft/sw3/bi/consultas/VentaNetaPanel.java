package com.luxsoft.sw3.bi.consultas;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;

import org.acegisecurity.providers.dao.salt.SystemWideSaltSource;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.PredicateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.aspectj.apache.bcel.generic.NEWARRAY;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.ui.DateCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.cxc.model.AntiguedadDeSaldo;
import com.luxsoft.siipap.cxc.ui.selectores.DevolucionRow;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Linea;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.RangoMatcherEditor;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.alcances.Alcance;
import com.luxsoft.sw3.bi.model.VentaNetaAcumuladalRow;
import com.luxsoft.sw3.bi.model.VentaNetaMensualRow;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoPendiente;
import com.luxsoft.sw3.ventas.ui.consultas.ReporteMensualCFD;

/**
 * Panel para el mantenimiento y control de los procesos de Caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentaNetaPanel extends FilteredBrowserPanel<VentaNetaMensualRow>{
	
	//private VentaNetaAcumuladaPanel ventaNetaAcumuladaBrowser;
	
	
	private Date fecha;
	public static String seleccionado;
	public static String seleccionTipo;
	public static String seleccionTipoProd;
	public static String fechaInicial;
	public static String fechaFinal;
	public static int yearStr;
	public static int mesStr;
	public static String descripcionPanel;
	private RangoMatcherEditor<VentaNetaMensualRow> menorAEditor;
	

	
	
	public VentaNetaPanel() {
		super(VentaNetaMensualRow.class);
	}
	
	JTextField descripcionField;
	JTextField tipoField;
	
	protected void init(){

		
		addProperty("indice","periodo","origenId","descripcion","ventaNeta","kilos","precio_kilos","porcentajePartVN","costo","costoKilos","importeUtilidad","porcentajeUtilidad","porcentajeAportacion","inventarioCosteado","kilosInv");
		addLabels("No.","Periodo","OrigenId","Descripcion","Vta. Neta","Kilos","Precio/K","% Part.VN","Costo","Costo Kg.","Imp. Ut.","% Ut.","% Part.U","Inv. Costo","Inv. Kilos");
		manejarPeriodo();
		descripcionField=new JTextField();
		tipoField=new JTextField();
		installTextComponentMatcherEditor("Descripcion",descripcionField, "descripcion");
	//	installTextComponentMatcherEditor("Tipo Prod.",tipoField, "nacional");
	//  installCustomComponentsInFilterPanel(filterPanelBuilder);
		menorAEditor=new RangoMatcherEditor<VentaNetaMensualRow>(){
			public boolean evaluar(VentaNetaMensualRow item) {
				return item.getIndice()<=getDoubleValue();
			}
		};
		installCustomMatcherEditor("Numero <= a", menorAEditor.getField(), menorAEditor);	
		
			
	}
	
	@Override
	protected void installEditors(EventList editors) {
		//super.installEditors(editors);
	}
	
	@Override
	protected JComponent buildContent() {
		//ventaNetaAcumuladaBrowser=new VentaNetaAcumuladaPanel();
	//	ventaNetaAcumuladaBrowser.getControl();
		JComponent parent=super.buildContent();
		
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		//sp.setBottomComponent(buildVentaNetaAcumuladaPanel());
		return sp;
	}
	
	

	
/*	protected JComponent buildVentaNetaAcumuladaPanel(){		
		//JXTable grid=ventaNetaAcumuladaBrowser.getGrid();
		JScrollPane sp=new JScrollPane(grid);
		ToolBarBuilder builder=new ToolBarBuilder();
	
		
		SimpleInternalFrame frame=new SimpleInternalFrame("Venta Neta Acumulada", null, sp);
		
		return frame;
	}*/
	
	public JPanel getFilterPanel() {
		if(filterPanel==null){
				
			
			filterPanel=new JPanel(new VerticalLayout());
						
			filterPanel.add(getFilterPanelBuilder().getPanel());
			
		//	filterPanel.add(ventaNetaAcumuladaBrowser.getFilterPanel());
	
			installFilters(filterPanelBuilder);
		}
		return filterPanel;
	}
	
	protected void installFilters(final DefaultFormBuilder builder){
		builder.appendSeparator("Para Generar Consulta");
		builder.append("Selector", createSeleComboBox());
	    builder.append("Procedencia", createTipoProductoComboBox());
		builder.append("Venta", createTipoVentaComboBox());
		builder.appendSeparator("Filtros");
		super.installFilters(builder);
		//builder.appendSeparator("Filtro Venta Neta Acumulada");
		
	}
	
	
	
	@Override
	protected void doSelect(Object bean) {
		VentaNetaMensualRow vent=(VentaNetaMensualRow) bean;
		
		 String consulta="";
		 descripcionPanel= vent.getDescripcion();
		if (seleccionado.equals("LINEA"))
			 consulta=" AND D.LINEA_ID= '"+ vent.getOrigenId()+"' ";
		if(seleccionado.equals("CLIENTE")){
			consulta=" AND D.CLIENTE_ID= '"+ vent.getOrigenId()+"' ";
		}
		if(seleccionado.equals("PRODUCTO")){
			consulta=" AND D.PRODUCTO_ID= '"+ vent.getOrigenId()+"' ";
		}
		if(seleccionado.equals("SUCURSAL")){
			consulta=" AND D.SUCURSAL_ID= '"+ vent.getOrigenId()+"' ";
		}
		if(seleccionado.equals("VENTA")){
			consulta=" AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END)= '"+ vent.getDescripcion()+"' ";
		}
		if(seleccionado.equals("MES")){
			consulta=" AND MONTH(D.FECHA)= '"+ vent.getOrigenId()+"' ";
		}
		String venta="";
		if(seleccionTipo.equals("TODOS")){
			venta="";
		}
		if(seleccionTipo.equals("CONTADO")){
			venta=" AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CONTADO') ";
		}
		
		if(seleccionTipo.equals("CREDITO")){
			venta=" AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CREDITO') ";
		}
		
		ProductoCosteadoPanel.show(periodo.getFechaInicial(),periodo.getFechaFinal(),venta,consulta);
	}

	@Override
	public Action[] getActions() {
		if(actions==null){
			
			//Action buscarAction=addAction("buscar.id","buscar", "Buscar");
			//buscarAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/page_find.png"));
			List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
			//actions.add(buscarAction);
			actions.add(getLoadAction());
			actions.add(CommandUtils.createPrintAction(this, "imprimir"));
			this.actions=actions.toArray(new Action[actions.size()]);
		}
		return actions;
		
	}
	
	
	
	@Override
	protected List<Action> createProccessActions() {		
		
		//Protecte our selves of null actions return by super class action factory methods
		List<Action> actions=ListUtils.predicatedList(new ArrayList<Action>(), PredicateUtils.notNullPredicate());
	/*	actions.add(addRoleBasedContextAction(null, POSRoles.CAJERO.name(),controller, "corteDeCaja", "Corte"));		
		actions.add(addAction("", "reporteFacturasPendientesCamioneta", "Facturas pendientes (CAM)"));
		*/
		
		
		 
		return actions;
	}

	public void buscar(){
		if(periodo==null)
			periodo=Periodo.getPeriodoDelMesActual();
		cambiarPeriodo();
	}
	
	/**
	 * Necesario para evitar NPE por la etiqueta de periodo
	 */
	protected void manejarPeriodo(){
		//periodo=Periodo.getPeriodoEnUnMes(-1);
		periodo=Periodo.periodoDeloquevaDelMes();
			
	}
	
	
	/*protected void adjustMainGrid(final JXTable grid){
		grid.getColumnExt("Fecha").setCellRenderer(new DateCellRenderer(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss")));
	}*/
	

	public void cambiarPeriodo(){
		
		ValueHolder yearModel=new ValueHolder(Periodo.obtenerYear(periodo.getFechaInicial()));
		ValueHolder mesModel=new ValueHolder(Periodo.obtenerMes(periodo.getFechaFinal()));
		
		AbstractDialog dialog=Binder.createSelectorMesYear(yearModel, mesModel);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			int year=(Integer)yearModel.getValue();
			int mes=(Integer)mesModel.getValue();
			periodo=Periodo.getPeriodoEnUnMes(mes-1, year);
			
			DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
			fechaInicial=df.format(periodo.getFechaInicial());
			fechaFinal=df2.format(periodo.getFechaFinal());
			Calendar fecha=Calendar.getInstance();
			fecha.setTime(periodo.getFechaInicial());
			yearStr= year; //fecha.get(Calendar.YEAR);
			mesStr= mes; //fecha.get(Calendar.MONTH);
			updatePeriodoLabel();
			//nuevoPeriodo(periodo);
			load();
		}
	}
	
	public void load(){
		
		super.load();
	//	ventaNetaAcumuladaBrowser.load();
		
		
	}
	
	protected void beforeLoad(){
		super.beforeLoad();
		
		logger.info("Cargando pendientes...");
		
	}

	@Override
	protected List<VentaNetaMensualRow> findData() {
		
		String sql3=ventaNeta();
		
		return ServiceLocator2.getJdbcTemplate().query(sql3, new BeanPropertyRowMapper(VentaNetaMensualRow.class));
		
		
	}
	
	@Override
	protected void afterLoad() {
		super.afterLoad();
		logger.info("Load completed");
		if(sortedSource.size()>0){
			//mostrarAdvertencia();
		}
	}
	
	public String  ventaNeta(){
		
		if(yearStr==0 || mesStr==0){
			mesStr=Periodo.obtenerMes(periodo.getFechaFinal())+1;
			yearStr=Periodo.obtenerYear(periodo.getFechaFinal());
			
		}
		
		String sql=SQLUtils.loadSQLQueryFromResource("sql/bi/VentaNetaMensual.sql");
				
		
	
		DateFormat df=new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		DateFormat df2=new SimpleDateFormat("yyyy-MM-dd 23:00:00");
		sql=sql.replaceAll("@YEAR", yearStr+"");
		sql=sql.replaceAll("@MES", mesStr+"");
		sql=sql.replaceAll("@FECHA_INI", df.format(periodo.getFechaInicial()));
		sql=sql.replaceAll("@FECHA_FIN", df2.format(periodo.getFechaFinal()));
		sql=sql.replaceAll("@MES", mesStr+"");
		// Seleccion de campos a pintar por el sql segun selector		
		if(seleccionado.equals("LINEA")){
			sql=sql.replaceAll("@DESCRIPCION",  "'LIN' AS TIPO,D.LINEA_ID AS origenId,D.LINEA");
			sql=sql.replaceAll("@INVENTARIO", "'LIN' AS TIPO,L.LINEA_ID AS origenId,L.NOMBRE");
		}
		if(seleccionado.equals("CLIENTE")){
			sql=sql.replaceAll("@DESCRIPCION",  "'EXI' AS TIPO,D.CLIENTE_ID AS origenId,(CASE WHEN D.CLIENTE_ID=8 THEN '1 MOSTRADOR' ELSE D.CLIENTE END)");	
			sql=sql.replaceAll("@INVENTARIO", "'EXI' AS TIPO,8 AS origenId,'1 MOSTRADOR'");
		}
		if(seleccionado.equals("PRODUCTO")){
			sql=sql.replaceAll("@DESCRIPCION",  "'PRD' AS TIPO,D.PRODUCTO_ID AS origenId,CONCAT(D.CLAVE,' ',D.DESCRIPCION)");	
			sql=sql.replaceAll("@INVENTARIO", "'PRD' AS TIPO,D.PRODUCTO_ID AS origenId,CONCAT(D.CLAVE,' ',P.DESCRIPCION)");
		}
		if(seleccionado.equals("SUCURSAL")){
			sql=sql.replaceAll("@DESCRIPCION",  "'SUC' AS TIPO,D.SUCURSAL_ID AS origenId,D.SUC");
			sql=sql.replaceAll("@INVENTARIO", "'SUC' AS TIPO,D.SUCURSAL_ID AS origenId,(SELECT S.NOMBRE FROM SW_SUCURSALES S WHERE S.SUCURSAL_ID=D.SUCURSAL_ID)");
		}
		if(seleccionado.equals("VENTA")){
			sql=sql.replaceAll("@DESCRIPCION", "'EXI' AS TIPO,1 AS origenId,(CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END)");	
			sql=sql.replaceAll("@INVENTARIO", "'EXI' AS TIPO,1 AS origenId,'CREDITO'");
		}
		if(seleccionado.equals("MES")){
			sql=sql.replaceAll("@DESCRIPCION", "'MES' AS TIPO,MONTH(D.FECHA) AS origenId,(SELECT M.MES_NOMBRE FROM sx_meses M WHERE M.MES=MONTH(D.FECHA))");
			sql=sql.replaceAll("@INVENTARIO", "'MES' AS TIPO,D.MES AS origenId,(SELECT M.MES_NOMBRE FROM sx_meses M WHERE M.MES=MONTH(D.FECHA))");
		}
		// Envio de Parametros para el sql de Tipo de Venta
		if(seleccionTipo.equals("TODOS")){
			sql=sql.replaceAll("@VENTA", "");
		}
		if(seleccionTipo.equals("CREDITO")){
			sql=sql.replaceAll("@VENTA", " AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CREDITO') ");
		}
		if(seleccionTipo.equals("CONTADO")){
			sql=sql.replaceAll("@VENTA", " AND (CASE WHEN D.ORIGEN='CRE' THEN 'CREDITO' ELSE 'CONTADO' END) IN('CONTADO') ");
		}
		//Envio de paramentros al sql para tipo de Producto, NAL, IMP y TOD
		
		
		if(seleccionTipoProd.equals("TODOS")){
			sql=sql.replaceAll("@TIPO_PROD", "");
		}
		if(seleccionTipoProd.equals("NACIONAL")){
			sql=sql.replaceAll("@TIPO_PROD", " AND  D.NACIONAL IS TRUE  ");
		}
		if(seleccionTipoProd.equals("IMPORTADO")){
			sql=sql.replaceAll("@TIPO_PROD", " AND  D.NACIONAL IS FALSE ");
		}
		
		
		return sql;
	}
	
	
	/** Reportes ***/
	
	/*public void reporteFacturasPendientesCamioneta(){
		FacturasPendientesCamioneta.run();
	}
	*/
	private JComboBox selectorBox;
	
	private JComboBox createSeleComboBox () {
		String[] data={"LINEA","CLIENTE","PRODUCTO","SUCURSAL","VENTA","MES"};
		selectorBox= new JComboBox(data);
		selectorBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				JComboBox combo = (JComboBox) evt.getSource() ;

			       seleccionado =(String) combo.getSelectedItem() ;
				
			}
		});
		if(seleccionado == null)
			seleccionado=(String) selectorBox.getSelectedItem();
		return selectorBox;
	}
	
	
	private JComboBox tipoVentaBox;
	
	private JComboBox createTipoVentaComboBox () {
		String[] data={"TODOS","CREDITO","CONTADO"};
		tipoVentaBox = new JComboBox(data);
		
			
		tipoVentaBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				JComboBox combo = (JComboBox) evt.getSource() ;

			       seleccionTipo =(String) combo.getSelectedItem() ;
				
			}
		});
		if(seleccionTipo == null)
			seleccionTipo=(String) tipoVentaBox.getSelectedItem();
		return tipoVentaBox;
		
			
	}
	
	

	
	private JComboBox tipoProducto;
	
	private JComboBox createTipoProductoComboBox () {
		String[] data={"TODOS","IMPORTADO","NACIONAL"};
		tipoProducto = new JComboBox(data);
		
			
		tipoProducto.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent evt) {
				JComboBox combo = (JComboBox) evt.getSource() ;

			       seleccionTipoProd =(String) combo.getSelectedItem() ;
				
			}
		});
		if(seleccionTipoProd == null)
			seleccionTipoProd=(String) tipoProducto.getSelectedItem();
		return tipoProducto;
	}
	
	
	
	public VentaNetaMensualRow getSelectedVentanNetaMensualRow(){
		return (VentaNetaMensualRow)getSelectedObject();
	}
	
	public void imprimir(){
		java.util.Map map=new HashMap();
		map.put("SELECTOR",(String)selectorBox.getSelectedItem());
		map.put("VENTA", (String)tipoVentaBox.getSelectedItem());
		map.put("FECHA_INI", periodo.getFechaInicial());
		map.put("FECHA_FIN", periodo.getFechaFinal());
		map.put("DESCRIPCION",descripcionField.getText());
		map.put("TIPO_PROD", tipoProducto.getSelectedItem());
		map.put("FORMATO", "VENTA NETA (MENSUAL)");
		map.put("REGISTROS", menorAEditor.getDoubleValue());
		ReportUtils.viewReport(ReportUtils.toReportesPath("bi/VentaNetaMensual.jasper"), map,grid.getModel());
	}
	//public VentaNetaAcumuladalRow getSelectedVentaneNetaAcumuladalRow(){
//		return (VentaNetaAcumuladalRow)this.ventaNetaAcumuladaBrowser.getSelectedObject();
//	}
	
private TotalesPanel totalPanel=new TotalesPanel();
	
	public JPanel getTotalesPanel(){
		return (JPanel)totalPanel.getControl();
	}
	
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel ventaNeta=new JLabel();
		private JLabel costo=new JLabel();
		private JLabel impUt=new JLabel();
		private JLabel porcUt=new JLabel();
		private JLabel invCost=new JLabel();
		private JLabel kilos=new JLabel();
		private JLabel precioKgVta=new JLabel();
		private JLabel costoKgVta=new JLabel();
		private JLabel inventarioKilos=new JLabel();
		private JLabel costoKg=new JLabel();
		
		

		@Override
		protected JComponent buildContent() {
			
			JPanel panel=new JPanel(new BorderLayout());
			final FormLayout layout=new FormLayout("p,1dlu,f:max(80dlu;p)","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaNeta.setHorizontalAlignment(SwingConstants.RIGHT);
			costo.setHorizontalAlignment(SwingConstants.RIGHT);
			impUt.setHorizontalAlignment(SwingConstants.RIGHT);
			porcUt.setHorizontalAlignment(SwingConstants.RIGHT);
			invCost.setHorizontalAlignment(SwingConstants.RIGHT);
			kilos.setHorizontalAlignment(SwingConstants.RIGHT);
			precioKgVta.setHorizontalAlignment(SwingConstants.RIGHT);
			costoKgVta.setHorizontalAlignment(SwingConstants.RIGHT);
			inventarioKilos.setHorizontalAlignment(SwingConstants.RIGHT);
			costoKg.setHorizontalAlignment(SwingConstants.RIGHT);
			
			
			builder.appendSeparator("Venta Neta");
			builder.nextColumn();
			builder.append("Importe",ventaNeta);
			builder.append("Kilos", kilos);
			builder.append("Precio Kg ", precioKgVta);
			
			builder.appendSeparator("Costo Venta");
			builder.nextLine();
			
			builder.append("Importe",costo);
			builder.append("Precio Kg ", costoKgVta);
			
			builder.appendSeparator("Utilidad");
			builder.nextLine();
			
			builder.append("Importe",impUt);
			builder.append("Porcentaje",porcUt);
			
			builder.appendSeparator("Inventario");
			builder.nextLine();
			
			builder.append("Costo",invCost);			
			builder.append("Kilos", inventarioKilos);
			builder.append("Costo Kg.", costoKg);
			
			panel.add(builder.getPanel(),BorderLayout.NORTH);
			
		//	panel.add(ventaNetaAcumuladaBrowser.getTotalesPanel1(),BorderLayout.SOUTH);

			
	
			
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			
			updateTotales();
			
			return panel;
			//return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
			BigDecimal ventaNeta=BigDecimal.ZERO;
			BigDecimal costo=BigDecimal.ZERO;
			BigDecimal impUt=BigDecimal.ZERO;
			BigDecimal porcUt=BigDecimal.ZERO;
			BigDecimal invCost=BigDecimal.ZERO;
			BigDecimal kilos=BigDecimal.ZERO;
			BigDecimal precioKgVta=BigDecimal.ZERO;
			BigDecimal precioKgCosto=BigDecimal.ZERO;
			BigDecimal inventarioKilos=BigDecimal.ZERO;
			BigDecimal costoKg=BigDecimal.ZERO;
			
			
			int indice=1;
			for(Object  r:getFilteredSource()){
				VentaNetaMensualRow an=(VentaNetaMensualRow)r;
				an.setIndice(indice++);
			}
					
			for(Object  r:getFilteredSource()){
				VentaNetaMensualRow an=(VentaNetaMensualRow)r;
				
			    
				ventaNeta=ventaNeta.add(new BigDecimal(an.getVentaNeta()));
				costo=costo.add(new BigDecimal(an.getCosto()));
				impUt=impUt.add(new BigDecimal(an.getImporteUtilidad()));
				//porcUt=porcUt.add(new BigDecimal(an.getPorcentajeUtilidad()));
				if(! new BigDecimal(an.getImporteUtilidad()).equals(BigDecimal.ZERO))
				porcUt=(impUt.divide(ventaNeta,3,RoundingMode.HALF_EVEN)).multiply(new BigDecimal(100));
				invCost=invCost.add(new BigDecimal(an.getInventarioCosteado()));
				
				kilos=kilos.add(an.getKilos());
				inventarioKilos=inventarioKilos.add(an.getKilosInv());
				
				//if(!kilos.equals(BigDecimal.ZERO)){
				if(kilos.doubleValue()!=0.00){

					precioKgVta=ventaNeta.divide(kilos,3,RoundingMode.HALF_EVEN);
					precioKgCosto=costo.divide(kilos,3,RoundingMode.HALF_EVEN);	
					
				}
				
				if(!inventarioKilos.equals(BigDecimal.ZERO)){
					costoKg=invCost.divide(inventarioKilos,2,RoundingMode.HALF_EVEN);
				}
				
				
				
				
			}
			
			
				
			
			
			
			for(Object  r:getFilteredSource()){
				
				BigDecimal porcentajeAp=BigDecimal.ZERO;
				BigDecimal porcentajePartVN=BigDecimal.ZERO;
				VentaNetaMensualRow an=(VentaNetaMensualRow)r;
				
				 if(! new BigDecimal(an.getImporteUtilidad()).equals(BigDecimal.ZERO))
				      porcentajeAp=new BigDecimal(an.getImporteUtilidad()).divide(impUt,3,RoundingMode.HALF_EVEN).multiply(new BigDecimal(100));
				 
			  an.setPorcentajeAportacion(porcentajeAp);
			  
				 if(! new BigDecimal(an.getVentaNeta()).equals(BigDecimal.ZERO))
				      porcentajePartVN=new BigDecimal(an.getVentaNeta()).divide(ventaNeta,3,RoundingMode.HALF_EVEN).multiply(new BigDecimal(100));
							 
			  an.setPorcentajePartVN(porcentajePartVN);	
			  
			  
			}
			
			String pattern="{0}";
			
			this.ventaNeta.setText(MessageFormat.format(pattern, ventaNeta));
			this.costo.setText(MessageFormat.format(pattern, costo));
			this.impUt.setText(MessageFormat.format(pattern, impUt));
			this.porcUt.setText(MessageFormat.format(pattern, porcUt));
			this.invCost.setText(MessageFormat.format(pattern, invCost));
			this.kilos.setText(MessageFormat.format(pattern, kilos));
			this.precioKgVta.setText(MessageFormat.format(pattern, precioKgVta));
			this.costoKgVta.setText(MessageFormat.format(pattern,precioKgCosto));
			this.inventarioKilos.setText(MessageFormat.format(pattern, inventarioKilos));
			this.costoKg.setText(MessageFormat.format(pattern, costoKg));
			
		}
		
		
		
	}
	

	
	
		
	
}
