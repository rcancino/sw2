package com.luxsoft.siipap.cxp.ui.selectores;

import java.awt.Dimension;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
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
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
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
public  class SelectorDeFacturasPorRequisitar extends AbstractSelector<CXPFactura>{
	
	
	protected Proveedor proveedor;
	protected Currency moneda;
	
	private SelectorDeFacturasPorRequisitar() {
		super(CXPFactura.class, "Facturas analizadas pendientes de pago");
		
	}
	
	@Override
	protected TextFilterator<CXPFactura> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"id","documento"});
	}

	@Override
	protected TableFormat<CXPFactura> getTableFormat() {
		String props[]={"id","documento","fecha","vencimiento","moneda","total","importeDescuentoFinanciero","requisitado","porRequisitar","pagos","totalAnalisis","saldo","saldoCalculado","saldoReal"};
		String labels[]={"Analisis","Factura","Fecha","Vencimiento","Moneda","Total","Dcto F","Requisitado","Por Requisitar","Pagos","Analizado","Saldo","Saldo C","Saldo N"};
		return GlazedLists.tableFormat(CXPFactura.class,props,labels);
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
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	protected void updateHeader(){
		header.setDescription("Facturas analizadas pendientes de pago  ");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	@Override
	protected List<CXPFactura> getData() {
		
		final String hql="from CXPFactura f " +
				" where f.proveedor.id=? " 
				+" and f.saldoReal>0 " 
				+" and f.moneda=?" +
				" and f.fecha>=?" +
				" order by f.vencimiento asc"
				;
		return ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery(hql)
				//.setEntity(0, proveedor)
				.setLong(0, proveedor.getId())
				.setParameter(1, moneda,Hibernate.CURRENCY)
				.setParameter(2, DateUtil.toDate("01/01/2008"),Hibernate.DATE)
				.list();
			}
			
		});
		/*
		List<CXPFactura> res= CXPServiceLocator.getInstance()
				.getFacturasManager()
				.buscarFacturasPorRequisitar(proveedor, moneda);
		CollectionUtils.filter(res, new Predicate(){
			public boolean evaluate(Object object) {
				CXPFactura fac=(CXPFactura)object;
				return fac.getPorRequisitar().amount().doubleValue()>0;
			}
		});
		return res;*/
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
	

	private static SoftReference<SelectorDeFacturasPorRequisitar> INSTANCE;
	
	public static SelectorDeFacturasPorRequisitar getInstance(){
		if(INSTANCE==null){
			INSTANCE=new SoftReference<SelectorDeFacturasPorRequisitar>(new SelectorDeFacturasPorRequisitar());
		}
		return INSTANCE.get();
	}
	
	public static List<CXPFactura> buscarFacturas(final Proveedor p,Currency moneda){
		SelectorDeFacturasPorRequisitar selector=new SelectorDeFacturasPorRequisitar();//getInstance();
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<CXPFactura> facturas=new ArrayList<CXPFactura>();
			facturas.addAll(selector.getSelectedList());
			return facturas;
		}		
		return new ArrayList<CXPFactura>(0);
		
	}
	
	public static List<CXPFactura> buscarFacturasParaRequisitar(final Proveedor p,Currency moneda){
		SelectorDeFacturasPorRequisitar selector=new SelectorDeFacturasPorRequisitar(){
			protected List<CXPFactura> getData() {
				/*
				final String hql="from CXPFactura f " +
						" where f.proveedor.id=? " 
						+" and f.saldoReal>0 " 
						+" and f.moneda=?" +
						" and f.fecha>=?" +
						" order by f.vencimiento asc"
						;*/
				
				final String hql="from CXPFactura f " +
						" where f.proveedor.id=? " 
						+" and f.saldoReal>0 " 
						+" and f.moneda=?" +
						" and f.fecha>=?" +
						" and f.anticipo is null" +
						" order by f.vencimiento asc"
						;
				List<CXPFactura> res= ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
					public Object doInHibernate(Session session)throws HibernateException, SQLException {
						return session.createQuery(hql)
						.setLong(0, proveedor.getId())
						.setParameter(1, moneda,Hibernate.CURRENCY)
						.setParameter(2, DateUtil.toDate("01/06/2011"),Hibernate.DATE)
						.list();
					}
				});
				return res;
			}
		};
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<CXPFactura> facturas=new ArrayList<CXPFactura>();
			facturas.addAll(selector.getSelectedList());
			return facturas;
		}		
		return new ArrayList<CXPFactura>(0);
		
	}
	
	public static List<CXPFactura> buscarFacturasParaAnticipo(final Proveedor p,Currency moneda){
		SelectorDeFacturasPorRequisitar selector=new SelectorDeFacturasPorRequisitar(){
			protected List<CXPFactura> getData() {
				final String hql="from CXPFactura f " +
						" where f.proveedor.id=? " 
						+" and f.importe=0 " 
						+" and f.moneda=?" +
								" and f.fecha>=?" +
								" and f.anticipo is null" +
								" order by f.vencimiento asc"
						;
				List<CXPFactura> res= ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
					public Object doInHibernate(Session session)throws HibernateException, SQLException {
						return session.createQuery(hql)
						.setLong(0, proveedor.getId())
						.setParameter(1, moneda,Hibernate.CURRENCY)
						.setParameter(2, DateUtil.toDate("01/01/2011"),Hibernate.DATE)
						.list();
					}
				});
				return res;
			}
		};
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<CXPFactura> facturas=new ArrayList<CXPFactura>();
			facturas.addAll(selector.getSelectedList());
			return facturas;
		}		
		return new ArrayList<CXPFactura>(0);
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscarFacturasParaRequisitar(ServiceLocator2.getProveedorManager()
						.buscarPorClave("I024")
						,MonedasUtils.PESOS);
				System.exit(0);
			}
			
		});
		
	}

}
