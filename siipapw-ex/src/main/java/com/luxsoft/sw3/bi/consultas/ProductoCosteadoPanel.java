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
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.bi.model.ProductoCosteadoRow;
import com.luxsoft.sw3.bi.model.VentaNetaMensualRow;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.contabilidad.ui.consultas.FilterBrowserDialog;
import com.mysql.jdbc.Util;



public class ProductoCosteadoPanel extends FilteredBrowserPanel<ProductoCosteadoRow>{
	
	private  String origenId="";
	private Date fechaInicial=new Date();
	private Date fechaFinal=new Date();
	private String venta;
	private String consulta;
	public static String clave;
	public static String descripcion;
//	private String sucursal;

	public ProductoCosteadoPanel() {
		super(ProductoCosteadoRow.class);
		setTitle("Producto Costeado "+VentaNetaPanel.seleccionado+": "+VentaNetaPanel.descripcionPanel+" Tipo de Venta: "+VentaNetaPanel.seleccionTipo  );
	}
	
	protected void init(){
		String[] props=new String[]{"nacional","linea","clave","descripcion","ventaNeta","kilos","precio_kilos","costo","costo_kilos","importeUtilidad","porcentajeUtilidad","clase","marca","kilosMillar","gramos","calibre","caras","deLinea"};
		String[] names=new String[]{"Proced.","Linea","Clave","Descripcion","Vta. Neta","Kilos","Precio/K","Costo","Costo/K","Imp. Ut.","% Ut.","Clase","Marca","KxMll","Grs","Cal.","Caras","De Linea"};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Producto", "linea","clave","descripcion");
		installTextComponentMatcherEditor("Procedencia", "nacional");
		installTextComponentMatcherEditor("Clase", "clase");
		installTextComponentMatcherEditor("Marca", "marca");
		installTextComponentMatcherEditor("Kilos", "kilosMillar");
		installTextComponentMatcherEditor("Gramos", "gramos");
		installTextComponentMatcherEditor("Calibre", "calibre");
		installTextComponentMatcherEditor("Caras", "caras");
		
		//installTextComponentMatcherEditor("Total", "total");
		/*
		CheckBoxMatcher<ProductoCosteadoRow> m1=new CheckBoxMatcher<ProductoCosteadoRow>(){
			@Override
			protected Matcher<ProductoCosteadoRow> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(ProductoCosteadoRow.class, "cancelado", Boolean.TRUE);
			}
			
		};
		installCustomMatcherEditor("Canceladas", m1.getBox(), m1);*/
		//manejarPeriodo();
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
		/*BuscadorDeCargos ba=new BuscadorDeCargos();
		ba.putValue(Action.NAME, "Buscar factura");
		procesos.add(addAction("","reporteDeVentasDiarias", "Ventas Diarias"));
		procesos.add(addAction("","reporteDeFacturasCanceladas", "Facturas Canceladas"));
		procesos.add(ba);*/
		return procesos;
	}
	
	
	


	@Override
	protected List<ProductoCosteadoRow> findData() {
		String sql=SQLUtils.loadSQLQueryFromResource("sql/bi/ProductoCosteado.sql");
		
		
		
		sql=sql.replaceAll("@FECHA_INI",VentaNetaPanel.fechaInicial);
		sql=sql.replaceAll("@FECHA_FIN",VentaNetaPanel.fechaFinal);
		sql=sql.replaceAll("@ORIGEN_ID", getConsulta());
		sql=sql.replaceAll("@VENTA", getVenta());
		sql=sql.replaceAll("@MES", VentaNetaPanel.mesStr+"");
		
		System.out.println(sql);
		
		return ServiceLocator2.getJdbcTemplate().query(sql, new BeanPropertyRowMapper(ProductoCosteadoRow.class));
		
	}
	
	

	@Override
	protected void doSelect(Object bean) {
		ProductoCosteadoRow prod=(ProductoCosteadoRow) bean;
		clave=prod.getClave();
		descripcion=prod.getDescripcion();
		ProductoCosteadoDetPanel.show(getVenta(), getConsulta(),clave);
		
	}
	
	public void open(){
		load();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public void imprimir(){
	
	}
	
	


	public Date getFechaInicial() {
		return fechaInicial;
	}

	public void setFechaInicial(Date fecha) {
		this.fechaInicial = fechaInicial;
	}

	public Date getFechaFinal() {
		return fechaInicial;
	}

	public void setFechaFinal(Date fecha) {
		this.fechaInicial = fechaInicial;
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
				ProductoCosteadoRow v=(ProductoCosteadoRow)o;
				ventaNeta=ventaNeta.add(new BigDecimal(v.getVentaNeta()));
				costo=costo.add(new BigDecimal(v.getCosto()));
				utilidad=utilidad.add(new BigDecimal(v.getImporteUtilidad()));
				
				if(!ventaNeta.equals(BigDecimal.ZERO)){
					porcUtilidad=utilidad.multiply(new BigDecimal(100)).divide(ventaNeta,3,RoundingMode.HALF_EVEN);	
				}
				
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
	public static void show(final Date fechaInicial,final Date fechaFinal,final String venta,final String consulta){
		final ProductoCosteadoPanel browser=new ProductoCosteadoPanel();
	    
		browser.setVenta(venta);
		
		browser.setConsulta(consulta);
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
	

}
