package com.luxsoft.siipap.cxc.ui.analisis;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.rules.CXCUtils;
import com.luxsoft.siipap.cxc.ui.consultas.FacturaForm;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.reports.FacturasPendientesCamioneta;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Venta;


public class CxC_CamionetaPanel extends FilteredBrowserPanel<Venta>{

	public CxC_CamionetaPanel() {
		super(Venta.class);
	}
	
	protected void init(){
		String[] props=new String[]{
				"sucursal.nombre"
				,"pedido.entrega"				
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				,"pagos"
				,"saldoCalculado"
				,"pedido.pagoContraEntrega.autorizo"
				,"facturista"
				,"cancelado"
				};
		String[] names=new String[]{				
				"Sucursal"
				,"Entrega"				
				,"Docto"
				,"N.Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				,"Pagos"
				,"Saldo"
				,"Aut (PCE)"
				,"Facturista"
				,"Cancelado"
				};
		addProperty(props);
		addLabels(names);
		installTextComponentMatcherEditor("Sucursal", "sucursal");
		installTextComponentMatcherEditor("Documento", "documento");
		installTextComponentMatcherEditor("N.Fiscal", "numeroFiscal");
		installTextComponentMatcherEditor("Cliente", "nombre","clave");
		manejarPeriodo();
	}
	
	@Override
	protected List<Venta> findData() {
		Date fecha=ServiceLocator2.obtenerFechaDelSistema();
		String hql="from Venta v left join fetch v.pedido p where " +
				" p.pagoContraEntrega is not null" +
				" and (v.total-v.aplicado)>1" +
				" and v.fecha<?";
		List<Venta> res= ServiceLocator2
			.getHibernateTemplate().find(hql,fecha);
		
		String hql2="from Venta v left join fetch v.pedido p where " +
		" p.pagoContraEntrega is not null" +
		" and v.fecha=?";
		List<Venta> res2= ServiceLocator2
		.getHibernateTemplate().find(hql2,fecha);
		
		res.addAll(res2);
		return res;
	}

	@Override
	public Action[] getActions() {		
		if(actions==null){
			actions=new Action[]{
				getLoadAction()
				,getViewAction()
				,addAction("", "reporteFacturasPendientesCamioneta", "Facturas pendientes (CAM)")
				};
		}
		return actions;
	}
	
	protected Venta getFactura(String id){
		return ServiceLocator2.getVentasManager().get(id);
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
	
	public void reporteFacturasPendientesCamioneta(){
		FacturasPendientesCamioneta.run();
	}
	
	private TotalesPanel totalPanel;
	
	public JPanel getTotalesPanel(){
		if(totalPanel==null){
			totalPanel=new TotalesPanel();
		}
		return (JPanel)totalPanel.getControl();
	}
	
	private class TotalesPanel extends AbstractControl implements ListEventListener{
		
		private JLabel saldoTotal=new JLabel();
		

		@Override
		protected JComponent buildContent() {
			final FormLayout layout=new FormLayout("p,2dlu,f:p:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			saldoTotal.setHorizontalAlignment(SwingConstants.RIGHT);
			
			builder.append("Saldo",saldoTotal);
			
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
			CantidadMonetaria saldo=CXCUtils.calcularSaldo(getFilteredSource());			
			String pattern="{0}  ({1})";
			saldoTotal.setText(MessageFormat.format(pattern, saldo.amount(),part(saldo,saldo)));
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

}
	

