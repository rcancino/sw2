package com.luxsoft.siipap.cxp.ui.selectores;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeContraRecibos extends AbstractSelector<ContraReciboDet>{
	
	
	protected Proveedor proveedor;
	protected ContraReciboDet.Tipo tipo=ContraReciboDet.Tipo.FACTURA;
	
	
	private SelectorDeContraRecibos() {
		super(ContraReciboDet.class, "Facturas por contrarecibo pendientes de analizar");
		
	}
	
	@Override
	protected TextFilterator<ContraReciboDet> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"id","documento","recibo.id"});
	}

	@Override
	protected TableFormat<ContraReciboDet> getTableFormat() {
		String props[]={"recibo.id","documento","tipo","fecha","tc","total","moneda"};
		String labels[]={"Recibo","Docto","Tipo","Fecha","TC","Total","Mon"};
		return GlazedLists.tableFormat(ContraReciboDet.class,props,labels);
	}
	
	private HeaderPanel header=new HeaderPanel("","");
	
	protected JComponent buildHeader(){
		updateHeader();
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(650,500));
	}

	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createLoadAction(this, "load");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar recibos en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	protected void updateHeader(){
		header.setDescription("Documentos recibidos y pendientes de analisis");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	protected List<ContraReciboDet> getData() {
		final Periodo periodo=Periodo.getPeriodoConAnteriroridad(-12);
		String hql="from ContraReciboDet d where d.recibo.proveedor.id=? and d.recibo.fecha between ? and ? " +
				" and d.cargoAbono is null and d.tipo=\'@DAT\'" +
				" and d.requisicion is null" ;
		hql=hql.replaceAll("@DAT", ContraReciboDet.Tipo.FACTURA.name());
		Object params[]=new Object[]{proveedor.getId(),periodo.getFechaInicial(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
		
	}
	
	public void clean(){
		proveedor=null;
		source.clear();
	}	

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	
	public void setTipo(ContraReciboDet.Tipo tipo) {
		this.tipo = tipo;
	}
	
	
	/**
	 * Busca la lista de facturas pendientes de pago
	 * 
	 * @param p
	 * @return
	 */
	public static List<String> buscarFacturasRecibidasPendientes(final Proveedor p){
		List<String> docs=new ArrayList<String>();
		SelectorDeContraRecibos selector=new SelectorDeContraRecibos();//getInstance();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setProveedor(p);
		selector.open();
		if(!selector.hasBeenCanceled()){			
			for(ContraReciboDet det:selector.getSelectedList()){
				docs.add(det.getDocumento());
			}
		}		
		return docs;
	}

	public static ContraReciboDet buscarReciboDeFacturas(final Proveedor p){
		SelectorDeContraRecibos selector=new SelectorDeContraRecibos();//getInstance();
		selector.setProveedor(p);		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){			
			return selector.getSelected();
		}		
		return null;
	}
	
	public static ContraReciboDet buscarReciboDeFacturas(){
		final Periodo periodo=Periodo.getPeriodoConAnteriroridad(-2);
		SelectorDeContraRecibos selector=new SelectorDeContraRecibos(){
			@Override
			protected List<ContraReciboDet> getData() {
				String hql="from ContraReciboDet d where d.recibo.fecha between ? and ? and d.cargoAbono is null and d.tipo=\'@DAT\'";
				hql=hql.replaceAll("@DAT", ContraReciboDet.Tipo.FACTURA.name());
				
				return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
				//return CXPServiceLocator.getInstance().getRecibosManager().buscarRecibosPendientes(ContraReciboDet.Tipo.FACTURA);
			}
		};				
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){			
			return selector.getSelected();
		}		
		return null;
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				//Object res=buscarReciboDeFacturas(ServiceLocator2.getProveedorManager().buscarPorClave("C003"));
				List<String> res=buscarFacturasRecibidasPendientes(ServiceLocator2.getProveedorManager().buscarPorClave("G012"));
				System.out.println(res);
				System.exit(0);
			}
			
		});
		
	}

}
