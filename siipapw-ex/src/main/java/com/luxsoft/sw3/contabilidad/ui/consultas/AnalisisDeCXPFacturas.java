package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.Dimension;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.cxp.service.FacturaManager;
import com.luxsoft.siipap.inventario.InventariosActions;
import com.luxsoft.siipap.inventario.ui.reports.FacturasPorProveedorForm;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.reports.DiarioDeEntradas;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;





public class AnalisisDeCXPFacturas extends AbstractMasterDatailFilteredBrowserPanel<CXPFactura, CXPAnalisisDet>{
	
	private Date fecha=new Date();
	//private String proveedor="";
	private Proveedor proveedor;
	

	public AnalisisDeCXPFacturas() {
		super(CXPFactura.class);
		setTitle("Facturas analizadas");
	}
	
	public void init(){
		addProperty(
				//"id"
				//,"tipoDeFactura"
				//"fecha"
				//,"clave"
				"nombre"
				,"documento"
				,"fecha"
				,"vencimiento"
				,"moneda"
				,"tc"
				,"total"
				,"totalAnalisis"
				,"bonificado"
				,"importeDescuentoFinanciero"
				,"pagos"
				//,"saldoCalculado"
				//,"porRequisitar"
				//,"requisitado"
				//,"comentario"
				);
		addLabels(
				//"CXP_ID"
				//,"Tipo"
				//"Fecha"
				//"Prov"
				"Nombre"
				,"Docto"
				,"F Docto"
				,"Vto"
				,"Mon"
				,"TC"
				,"Facturado"
				,"Analizado"
				,"Bonificado"
				,"D.F."
				,"Pagos"
				//,"Saldo"
				//,"Por Requisitar"
				//,"Requisitado"
				//,"Comentario"
				);
		
		installTextComponentMatcherEditor("Proveedor", "nombre");
		installTextComponentMatcherEditor("Factura", "documento");
		installTextComponentMatcherEditor("Analisis", "id");
		
	}
	
	
	
	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"factura.id"
				,"factura.documento"
				,"entrada.sucursal.nombre"
				,"entrada.remision"
				,"entrada.fechaRemision"
				,"entrada.fecha"
				,"entrada.documento"
				,"entrada.clave"
				,"entrada.descripcion"
				,"entrada.cantidad"
				,"cantidad"
				,"precio"
				,"costo"
				,"Importe"
				};
		String[] labels={
				"CXP_ID"
				,"Factura"
				,"Sucursal"
				,"Remisión"
				,"F.Remisión"
				,"Entrada"
				,"COM"
				,"Producto"
				,"Descripción"
				,"Recibido"
				,"Analizado"
				,"Precio"
				,"CostoU"
				,"Importe"
				};
		return GlazedLists.tableFormat(CXPAnalisisDet.class, props,labels);
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				};
		return actions;
	}
	
	@Override
	protected Model<CXPFactura, CXPAnalisisDet> createPartidasModel() {
		Model<CXPFactura, CXPAnalisisDet> model=new Model<CXPFactura,CXPAnalisisDet>(){
			public List<CXPAnalisisDet> getChildren(CXPFactura parent) {
				try {
					return getManager().buscarAnalisis(parent);
				} catch (Exception e) {					
					return new ArrayList<CXPAnalisisDet>();
				}
				
			}
		};
		return model;
	}
	

	private FacturaManager getManager(){
		return CXPServiceLocator.getInstance().getFacturasManager();
	}
	

	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("reporteDeAnalisis", "printReport", "Imprimir"));
		procesos.add(addAction("diarioDeEntradas", "reporteDiarioDeEntradas", "Diario de entradas"));
		procesos.add(addAction(InventariosActions.ConsultaDeCostosDeInventario.getId(), "facturasAnalizadas", "Facturas Analizadas"));
		return procesos;
	}

	@Override
	protected List<CXPFactura> findData() {
		
		String hql="from CXPFactura f where f.id in(select d.factura.id from CXPAnalisisDet d left join d.entrada e where date(e.fecha) =?)";
		Object[] params=new Object[]{getFecha()};
		if(getProveedor()!=null){
			hql+=" and f.proveedor.id=?";
			params=new Object[]{getFecha(),getProveedor().getId()};
		}
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
	}
	
	public void printReport(){
		if(getSelectedObject()!=null)
			print((CXPFactura)getSelectedObject());
	}
	
	public void print(final CXPFactura bean){
		if(confirmar("Desea imprimir Análisis?")){
			Map params=new HashMap();
			params.put("NUMERO", bean.getId());
			Currency moneda=bean.getMoneda();
			if(!moneda.equals(MonedasUtils.PESOS)){
				if(confirmar("En moneda nacional"))
					moneda=MonedasUtils.PESOS;
			}
			params.put("MONEDA", moneda.getCurrencyCode());
			String path=ReportUtils.toReportesPath("cxp/AnalisisDeFactura.jasper");
			if(ReportUtils.existe(path))
				ReportUtils.viewReport(path, params);
			else
				JOptionPane.showMessageDialog(this.getControl()
						,MessageFormat.format("El reporte:\n {0} no existe",path),"Reportes",JOptionPane.ERROR_MESSAGE);
		}
	}

	
	public void reporteDiarioDeEntradas(){
		DiarioDeEntradas d=new DiarioDeEntradas();
		d.actionPerformed(null);
	}
	public void facturasAnalizadas(){
		FacturasPorProveedorForm.run();
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	public void open(){
		load();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		
		private JLabel total1=new JLabel();
		private JLabel total2=new JLabel();
		private JLabel total3=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			total1.setHorizontalAlignment(SwingConstants.RIGHT);
			total2.setHorizontalAlignment(SwingConstants.RIGHT);
			total3.setHorizontalAlignment(SwingConstants.RIGHT);
			builder.appendSeparator("Resumen ");
			builder.append("Total",total1);
			//builder.append("Ventas (Prom)",total2);
			//builder.append("Por Pedir",total3);
			
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
			double valorTotal1=0;
			
			for(Object obj:getFilteredSource()){
				CXPFactura a=(CXPFactura)obj;
				valorTotal1+=a.getTotal().doubleValue();
				
			}
			total1.setText(nf.format(valorTotal1));
			
		}
		
		private NumberFormat nf=NumberFormat.getNumberInstance();
		
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	
	@Override
	protected void executeLoadWorker(SwingWorker worker) {
		TaskUtils.executeSwingWorker(worker);
	}
	

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public static void show(String dateAsString){
		Date fecha=DateUtil.toDate(dateAsString);
		show(fecha,"");
	}
		
	//private static ;
	
	public static void show(final Date fecha,String claveProveedor){
		final AnalisisDeCXPFacturas browser=new AnalisisDeCXPFacturas();
		Proveedor proveedor=ServiceLocator2.getProveedorManager().buscarPorClave(claveProveedor);
		browser.setFecha(fecha);
		browser.setProveedor(proveedor);
		FilterBrowserDialog dialog=new FilterBrowserDialog(browser);
		dialog.setModal(false);
		dialog.setSize(new Dimension(800,650));
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
				show("01/06/2011");
				//System.exit(0);
			}

		});
	}
	
	
}
