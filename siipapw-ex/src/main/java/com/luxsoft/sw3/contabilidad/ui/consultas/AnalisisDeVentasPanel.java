package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.text.MessageFormat;
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
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;



public class AnalisisDeVentasPanel extends FilteredBrowserPanel<Venta>{	
	
	private Date fecha=new Date();
	private String origen="MOS";
	private String sucursal;
	//private Date fecha1= AnalisisDeVentasPanel.

	public AnalisisDeVentasPanel() {
		super(Venta.class);
		setTitle("Ventas");
	}
	
	protected void init(){
		String[] props=new String[]{
				"sucursal.nombre"
				,"origen"
				//,"pedidoFormaDeEntrega"				
				//,"pedidoFolio"
				,"documento"
				//,"numeroFiscal"				
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"pagos"
				,"saldoCalculado"
				//,"facturista"
				,"cancelado"
				//,"cancelacion.log.createUser"
				//,"cancelacion.log.creado"
				};
		String[] names=new String[]{				
				"Suc"
				,"Ori"
				//,"Entrega"
				//,"Pedido"
				,"Folio"
				//,"N.Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Pagos"
				,"Saldo"
				//,"Facturista"
				,"Cancelado"
				//,"Canceló"
				//,"Fecha Cancel"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");		
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		CheckBoxMatcher<Venta> m1=new CheckBoxMatcher<Venta>(){
			@Override
			protected Matcher<Venta> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(Venta.class, "cancelado", Boolean.TRUE);
			}
			
		};
		installCustomMatcherEditor("Canceladas", m1.getBox(), m1);
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
		BuscadorDeCargos ba=new BuscadorDeCargos();
		ba.putValue(Action.NAME, "Buscar factura");
		procesos.add(addAction("","reporteDeVentasDiarias", "Ventas Diarias"));
		procesos.add(addAction("","reporteDeFacturasCanceladas", "Facturas Canceladas"));
		procesos.add(ba);
		return procesos;
	}
	
	
	
	public void reporteDeVentasDiarias(){
		VentasDiariasBI.run();
	}
	
	public void reporteDeFacturasCanceladas(){
		FacturasCanceladasBi.run();
	}

	@Override
	protected List<Venta> findData() {
		String hql="from Venta v " +				
		"    where v.origen=@ORIGEN " +
		"    and v.fecha=?" +
		"";
		
		hql=hql.replace("@ORIGEN", "\'"+getOrigen()+"\'");
		Object[] params=new Object[]{getFecha()
			};
		if(StringUtils.isNotBlank(sucursal)){
			hql+=" and v.sucursal.nombre=?";
			params=new Object[]{getFecha(),getSucursal()};
		}
		return ServiceLocator2.getHibernateTemplate()
		.find(hql, params);
	}

	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
		
		
	}
	
	public void open(){
		load();
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}

	public void imprimir(){
		Venta factura=(Venta)getSelectedObject();
		if(factura!=null){
			factura=ServiceLocator2.getVentasManager().buscarVentaInicializada(factura.getId());
			ComprobanteFiscal cf=ServiceLocator2.getCFDManager().cargarComprobante(factura);
			if(cf==null){
				final Map parameters=new HashMap();
				String total=ImporteALetra.aLetra(factura.getTotalCM());
				parameters.put("CARGO_ID", String.valueOf(factura.getId()));
				parameters.put("IMP_CON_LETRA", total);
				ReportUtils.viewReport(ReportUtils.toReportesPath("ventas/FacturaCopia.jasper"), parameters);
			}else
				CFDPrintServicesCxC.impripirComprobante(factura, cf,null, true);
		}
	}
	
	
	
public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}



private TotalesPanel totalPanel;
	
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel ventaTotal=new JLabel();
		private JLabel importeTotal=new JLabel();
		private JLabel ivaTotal=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Importe",importeTotal);
			builder.append("IVA",ivaTotal);
			builder.append("Total",ventaTotal);
			
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
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			CantidadMonetaria impuesto=CantidadMonetaria.pesos(0);
			CantidadMonetaria total=CantidadMonetaria.pesos(0);
			for(Object o:getFilteredSource()){
				Venta v=(Venta)o;
				total=total.add(v.getTotalCM());
				importe=importe.add(v.getImporteCM());
				impuesto=impuesto.add(v.getImpuestoCM());
			}			
			String pattern="{0}";
			importeTotal.setText(MessageFormat.format(pattern, importe.amount()));
			ivaTotal.setText(MessageFormat.format(pattern, impuesto.amount()));
			ventaTotal.setText(MessageFormat.format(pattern, total.amount()));
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
	
	public static void show(String dateAsString,final String origen){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen);
	}
	
	
	public static void show(final Date fecha,final String origen){
		final AnalisisDeVentasPanel browser=new AnalisisDeVentasPanel();
		browser.setFecha(fecha);
		browser.setOrigen(origen);
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setModal(false);
		dialog.open();
	}
	
	public static void show(String dateAsString,final String origen,String sucursal){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha, origen,sucursal);
	}
	
	public static void show(final Date fecha,final String origen,final String sucursal){
		final AnalisisDeVentasPanel browser=new AnalisisDeVentasPanel();
		browser.setFecha(fecha);
		browser.setOrigen(origen);
		browser.setSucursal(sucursal);
		final FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setModal(false);
		dialog.open();
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
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				show("14/04/2011", "CAM","ANDRADE");
				//System.exit(0);
			}

		});
	}
	

}
