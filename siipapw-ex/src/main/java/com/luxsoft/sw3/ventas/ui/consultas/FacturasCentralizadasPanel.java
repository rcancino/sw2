package com.luxsoft.sw3.ventas.ui.consultas;

import java.text.MessageFormat;
import java.text.NumberFormat;
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
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.reports.ClientesNuevosBI;
import com.luxsoft.siipap.reports.FacturasCanceladasBi;
import com.luxsoft.siipap.reports.VentasDiariasBI;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.CFDPrintServicesCxC;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;



public class FacturasCentralizadasPanel extends FilteredBrowserPanel<Venta>{
	
	

	public FacturasCentralizadasPanel() {
		super(Venta.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"sucursal.nombre"
				,"origen"
				,"pedidoFormaDeEntrega"				
				,"pedidoFolio"
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"pagos"
				,"saldoCalculado"
				,"facturista"
				,"cancelado"
				,"cancelacion.log.createUser"
				,"cancelacion.log.creado"
				};
		String[] names=new String[]{				
				"Suc"
				,"Ori"
				,"Entrega"
				,"Pedido"
				,"Folio"
				,"N.Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Pagos"
				,"Saldo"
				,"Facturista"
				,"Cancelado"
				,"Canceló"
				,"Fecha Cancel"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Origen", "origen");
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		installTextComponentMatcherEditor("Total", "total");
		CheckBoxMatcher<Venta> m1=new CheckBoxMatcher<Venta>(){
			@Override
			protected Matcher<Venta> getSelectMatcher(Object... obj) {
				return Matchers.beanPropertyMatcher(Venta.class, "cancelado", Boolean.TRUE);
			}
			
		};
		installCustomMatcherEditor("Canceladas", m1.getBox(), m1);
		manejarPeriodo();
	}
	
	protected void manejarPeriodo(){
		periodo=Periodo.getPeriodo(-3);
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
		procesos.add(addAction("","reporteClientesNuevos", "Clientes Nuevos"));
		procesos.add(ba);
		return procesos;
	}
	
	
	
	public void reporteDeVentasDiarias(){
		VentasDiariasBI.run();
	}
	
	public void reporteDeFacturasCanceladas(){
		FacturasCanceladasBi.run();
	}
	
	public void reporteClientesNuevos(){
		ClientesNuevosBI.run();
	}

	@Override
	protected List<Venta> findData() {
		return ServiceLocator2.getHibernateTemplate().find(
				"from Venta v   " +
				"where v.fecha between ? and ?" 
				,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}

	@Override
	protected void doSelect(Object bean) {
		Venta venta=(Venta)bean;
		FacturaForm.show(venta.getId());
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
	
private TotalesPanel totalPanel;
	
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel ventaTotal=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			ventaTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			
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
			CantidadMonetaria saldo=calcuarTotalVentas(getFilteredSource());			
			String pattern="{0}  ({1})";
			ventaTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
		}
		
		private NumberFormat nf=NumberFormat.getPercentInstance();
		
		private String part(final CantidadMonetaria total,final CantidadMonetaria part){
			
			double res=0;
			if(total.amount().doubleValue()>0){
				res=part.divide(total.amount()).amount().doubleValue();
			}
			return StringUtils.leftPad(nf.format(res),5);
		}
		
	}
	
	public static CantidadMonetaria calcuarTotalVentas(final List<Venta> ventas){
		CantidadMonetaria total=CantidadMonetaria.pesos(0);
		for(Venta c:ventas){
			total=total.add(c.getTotalCM());
		}
		return total;
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

}
