package com.luxsoft.sw3.bi.consultas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.cxc.ui.command.BuscadorDeCargos;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.FacturasCanceladasBi;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel.Predicate;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;

import com.luxsoft.sw3.bi.model.ProductoCosteadoDetRow;
import com.luxsoft.sw3.bi.model.ProductoCosteadoRow;
import com.luxsoft.sw3.bi.model.VentaNetaMensualRow;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;
import com.mysql.jdbc.Util;



public class ProductoCosteadoDetPanel extends FilteredBrowserPanel<ProductoCosteadoDetRow>{
	
	private  String origenId="";
	private Date fechaInicial=new Date();
	private Date fechaFinal=new Date();
	private String venta="";
	private String consulta="";
	private  String clave="";
	private  String descripcion="";


	public ProductoCosteadoDetPanel() {
		super(ProductoCosteadoDetRow.class);
		setTitle("Detalle Producto: " +ProductoCosteadoPanel.clave+" "+ProductoCosteadoPanel.descripcion);
	}
	
	protected void init(){
		String[] props=new String[]{"tipo","cliente","docto","fechad","origen","suc","ventaNeta","costo","importeUtilidad","porcentajeUtilidad","kilos","precio_kilos"};
		String[] names=new String[]{"Tipo","Cliente","Docto","Fecha","Venta","Sucursal","Vta. Neta","Costo","Imp. Ut.","% Ut.","Kilos","Precio/K"};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Tipo", "tipo");
		installTextComponentMatcherEditor("Cliente", "cliente");
		installTextComponentMatcherEditor("Doc", "docto");
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "suc");
		
		
		
	}
	
	

	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction(null,"imprimir", "Imprimir")
				};
		return actions;
	}
	
	@Override
	protected List<Action> createProccessActions() {		
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("","imprimir", "Imprimir"));
	
		return procesos;
	}
	
	
	


	@Override
	protected List<ProductoCosteadoDetRow> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/bi/ProductoCosteadoDet.sql");
		
		
		
		sql=sql.replaceAll("@FECHA_INI",VentaNetaPanel.fechaInicial);
		sql=sql.replaceAll("@FECHA_FIN",VentaNetaPanel.fechaFinal);
		sql=sql.replaceAll("@ORIGEN_ID", getConsulta());
		sql=sql.replaceAll("@VENTA", getVenta());
		sql=sql.replaceAll("@CLAVE", getClave());
		
		return ServiceLocator2.getJdbcTemplate().query(sql, new BeanPropertyRowMapper(ProductoCosteadoDetRow.class));
		
	}
	
	

	@Override
	protected void doSelect(Object bean) {
	ProductoCosteadoDetRow item=(ProductoCosteadoDetRow) bean;
		
		final Map parameters=new HashMap();

		parameters.put("ID", item.getOrigen_id());
		parameters.put("CLAVE",item.getClave());
		
		if( item.getTipo().equals("VTA")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaCosteada.jasper"), parameters);
			//System.out.println("VTA--" +item.getOrigen_id());
		}
		if(item.getTipo().equals("DEV")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/NotaCosteadaDev.jasper"), parameters);
			//System.out.println("DEV--" +item.getOrigen_id());
		}
		if(item.getTipo().equals("BON")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/NotaCosteadaBon.jasper"), parameters);
			//System.out.println("BON--" +item.getOrigen_id());
		}
		
	}
	
	public void open(){
		load();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public void imprimir(){ 
		ProductoCosteadoDetRow item=(ProductoCosteadoDetRow) getSelectedObject();
		
		final Map parameters=new HashMap();

		parameters.put("ID", item.getOrigen_id());
		parameters.put("CLAVE",item.getClave());
		
		if( item.getTipo().equals("VTA")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaCosteada.jasper"), parameters);
			//System.out.println("VTA--" +item.getOrigen_id());
		}
		if(item.getTipo().equals("DEV")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/NotaCosteadaDev.jasper"), parameters);
			//System.out.println("DEV--" +item.getOrigen_id());
		}
		if(item.getTipo().equals("BON")){
			ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/NotaCosteadaBon.jasper"), parameters);
			//System.out.println("BON--" +item.getOrigen_id());
		}
		
		
	}
	
	
	


	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fecha) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaFinal;
	}

	public void setFechaFinal(Date fecha) {
		this.fechaFinal = fechaFinal;
	}
	
	public String getVenta() {
		return venta;
	}

	
	public void setVenta(String venta) {
		this.venta = venta;
	}
	
	public void setOrigenId(String origen) {
		this.origenId = origenId;
	}
	
	public String getOrigenId() {
		return origenId;
	}
	
	public void setConsulta(String consulta) {
		this.consulta = consulta;
	}
	
	public String getConsulta() {
		return consulta;
	}
	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}


private TotalesPanel totalPanel;
	
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{


	
		
		private JLabel ventaNeta=new JLabel();
		private JLabel costo=new JLabel();
		private JLabel utilidad=new JLabel();
		private JLabel porcUtilidad=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaNeta.setHorizontalAlignment(SwingConstants.RIGHT);
			costo.setHorizontalAlignment(SwingConstants.RIGHT);
			utilidad.setHorizontalAlignment(SwingConstants.RIGHT);
			porcUtilidad.setHorizontalAlignment(SwingConstants.RIGHT);
			
			
			builder.appendSeparator("Totales");
			builder.append("Venta Neta",ventaNeta);
			builder.append("Costo",costo);
			builder.append("Utilidad",utilidad);
			builder.append("% Utilidad",porcUtilidad);
			
			builder.getPanel().setOpaque(false);
			getFilteredSource().addListEventListener(this);
			updateTotales();
			return builder.getPanel();
		}
		
		public void listChanged(ListEvent listChanges) {
			if(listChanges.next()){
				
			}
			updateTotales();
		}
		
		public void updateTotales(){
		
			BigDecimal ventaNeta=BigDecimal.ZERO;
			BigDecimal costo=BigDecimal.ZERO;
			BigDecimal utilidad=BigDecimal.ZERO;
			BigDecimal porcUtilidad=BigDecimal.ZERO;
			
			for(Object o:getFilteredSource()){
				ProductoCosteadoDetRow v=(ProductoCosteadoDetRow)o;
				ventaNeta=ventaNeta.add(new BigDecimal(v.getVentaNeta()));
				costo=costo.add(new BigDecimal(v.getCosto()));
				utilidad=utilidad.add(new BigDecimal(v.getImporteUtilidad()));
				porcUtilidad=utilidad.multiply(new BigDecimal(100)).divide(ventaNeta,3,RoundingMode.HALF_EVEN);
			}			
			String pattern="{0}";
			
			this.ventaNeta.setText(MessageFormat.format(pattern, ventaNeta));
			this.costo.setText(MessageFormat.format(pattern, costo));
			this.utilidad.setText(MessageFormat.format(pattern, utilidad));
			this.porcUtilidad.setText(MessageFormat.format(pattern, porcUtilidad));
			
			
		}
	
		
	}
	
	public static class ImprimiblePredicate implements Predicate{
		public boolean evaluate(Object bean) {
			Venta v=(Venta)bean;
			if(v!=null){
				return v.getImpreso()==null;
			}
			return false;
		}
	}
	

	

//******************************************************************************* ESTE ES EL BUENO

		public static void show(final String venta,final String consulta,final String clave){
			final ProductoCosteadoDetPanel browser=new ProductoCosteadoDetPanel();
			
			browser.setVenta(venta);
			browser.setConsulta(consulta);
			browser.setClave(clave);
			final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
			dialog.setModal(false);
			dialog.open();
		}
	
//**************************************************************************************	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
		
				
			}

		});
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	
	

}
