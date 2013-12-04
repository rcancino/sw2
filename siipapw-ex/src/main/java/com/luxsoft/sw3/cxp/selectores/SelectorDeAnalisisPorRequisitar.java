package com.luxsoft.sw3.cxp.selectores;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeAnalisisPorRequisitar extends AbstractSelector<AnalisisDeFactura>{
	
	
	protected Proveedor proveedor;
	protected Currency moneda;
	
	private SelectorDeAnalisisPorRequisitar() {
		super(AnalisisDeFactura.class, "Analisis pendientes de pago");
		
	}
	
	@Override
	protected TextFilterator<AnalisisDeFactura> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"id","factura.documento"});
	}

	@Override
	protected TableFormat<AnalisisDeFactura> getTableFormat() {
		String props[]={
				"id"
				,"fecha"
				,"importe"
				,"factura.documento"
				,"factura.fecha"
				,"factura.vencimiento"
				,"factura.moneda"
				,"factura.total"
				//,"factura.importeDescuentoFinanciero"
				//,"factura.requisitado"
				//,"factura.porRequisitar"
				//,"factura.pagos"
				//,"factura.totalAnalisis"
				//,"factura.saldo"
				//,"factura.saldoCalculado"
				,"factura.saldoReal"				
				};
		String labels[]={
				"Análisis"
				,"Fecha"
				,"Analizado"
				,"Factura"
				,"Fecha (Fac)"
				,"Vto"
				,"Mon"
				,"Total(Fac)"
				//,"factura.importeDescuentoFinanciero"
				//,"factura.requisitado"
				//,"factura.porRequisitar"
				//,"factura.pagos"
				//,"factura.totalAnalisis"
				//,"Saldo"
				//,"Saldo C"
				,"Saldo"				
				
		};
		//String labels[]={"Analisis","Factura","Fecha","Vencimiento","Moneda","Total","Dcto F","Requisitado","Por Requisitar","Pagos","Analizado","Saldo","Saldo C","Saldo N"};
		return GlazedLists.tableFormat(AnalisisDeFactura.class,props,labels);
	}
	
	private HeaderPanel header=new HeaderPanel("","");
	
	protected JComponent buildHeader(){
		updateHeader();
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(810,500));
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createLoadAction(this, "load");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar analisis en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	protected void updateHeader(){
		header.setDescription("Análisis pendientes de requisitar");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	@Override
	protected List<AnalisisDeFactura> getData() {
		final String hql="from AnalisisDeFactura a " +
				" where a.factura.proveedor.id=?" +
				"  and  a.factura.moneda=?" 
				+"  and a.requisicionDet is null" 
				+" and a.fecha>?"
				;
		List<AnalisisDeFactura> res= ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery(hql)				
				.setLong(0, proveedor.getId())
				.setParameter(1, moneda,Hibernate.CURRENCY)
				.setParameter(2,DateUtil.toDate("01/10/2011"),Hibernate.DATE )
				.list();
			}
		});
		return res;
	}
	
	

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
		
	
	public static List<AnalisisDeFactura> buscarFacturas(final Proveedor p,Currency moneda){
		SelectorDeAnalisisPorRequisitar selector=new SelectorDeAnalisisPorRequisitar();//getInstance();
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<AnalisisDeFactura> analisis=new ArrayList<AnalisisDeFactura>();
			analisis.addAll(selector.getSelectedList());
			return analisis;
		}		
		return new ArrayList<AnalisisDeFactura>(0);
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscarFacturas(ServiceLocator2.getProveedorManager().buscarPorClave("C003"),MonedasUtils.PESOS);
				/*final String hql="from AnalisisDeFactura a " +
						" where a.factura.proveedor.clave=\'C003\'" 
						//+"  and  a.factura.moneda=?"
						+" and a.fecha>?"
						+"  and a.requisicionDet is null";
				List data=ServiceLocator2.getHibernateTemplate().find(hql,DateUtil.toDate("01/10/2011"));
				System.out.println("Analisis: "+data.size());
				*/
				
				System.exit(0);
			}
			
		});
		
	}

}
