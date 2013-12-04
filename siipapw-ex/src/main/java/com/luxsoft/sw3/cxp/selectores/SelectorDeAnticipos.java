package com.luxsoft.sw3.cxp.selectores;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Predicate;

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
import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeAnticipos extends AbstractSelector<AnticipoDeCompra>{
	
	
	protected Proveedor proveedor;
	protected Currency moneda;
	
	private SelectorDeAnticipos(Proveedor proveedor,Currency moneda) {
		super(AnticipoDeCompra.class, "Anticipos para :"+proveedor);
		this.proveedor=proveedor;
		this.moneda=moneda;
		
	}
	
	@Override
	protected TextFilterator<AnticipoDeCompra> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"id","documento"});
	}

	@Override
	protected TableFormat<AnticipoDeCompra> getTableFormat() {
		String[] props={
				"id"
				,"proveedor.nombre"
				,"fecha"
				,"importe"
				,"aplicado"				
				,"disponible"
				,"diferencia"
				,"factura.documento"
				,"factura.pagos"
				,"descuento"
				,"descuentoFinanciero"
				,"factura.saldoCalculado"
				,"descuentoNota.documento"
				,"nota.documento"
				,"comentario"
		};
		String[] names={
				"Id"
				,"Proveedor"
				,"Fecha"
				,"Importe"				
				,"Aplicado"				
				,"Disponible"
				,"Dif"
				,"Factura"
				,"Abonos"
				,"Desco C."
				,"D.F"
				,"Saldo"				
				,"Nota Dscto"
				,"Nota D.F."
				,"Comentario"
		};
		return GlazedLists.tableFormat(AnticipoDeCompra.class,props,names);
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
		//Action a=CommandUtils.createLoadAction(this, "load");
		//a.putValue(Action.NAME, "Buscar");
		//a.putValue(Action.SHORT_DESCRIPTION, "Buscar analisis en otro periodo");
		//builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	protected void updateHeader(){
		header.setDescription("Anticipos pendientes ");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	@Override
	protected List<AnticipoDeCompra> getData() {
		final String hql="from AnticipoDeCompra a " +
				" where a.proveedor.id=?" +				
				"  and a.moneda=?"; 
				
				;
		List<AnticipoDeCompra> res= ServiceLocator2.getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery(hql)				
				.setLong(0, proveedor.getId())
				.setParameter(1, moneda,Hibernate.CURRENCY)
				.list();
			}
		});
		res=(List<AnticipoDeCompra>) CollectionUtils.filter(res, new Predicate() {
			public boolean evaluate(Object arg) {
				AnticipoDeCompra aa=(AnticipoDeCompra)arg;
				return aa.getDisponible().doubleValue()>0;
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
		
	
	public static AnticipoDeCompra buscarAnticipo(final Proveedor p,Currency moneda){
		SelectorDeAnticipos selector=new SelectorDeAnticipos(p,moneda);//getInstance();
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			AnticipoDeCompra a=(AnticipoDeCompra)selector.getSelectedList().get(0);
			return a;
		}		
		return null;
		
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscarAnticipo(ServiceLocator2.getProveedorManager().buscarPorClave("I024"),MonedasUtils.PESOS);
				
				System.exit(0);
			}
			
		});
		
	}

}
