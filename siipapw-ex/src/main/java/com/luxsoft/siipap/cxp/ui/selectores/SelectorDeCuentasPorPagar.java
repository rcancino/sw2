package com.luxsoft.siipap.cxp.ui.selectores;

import java.awt.Dimension;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Currency;
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
import com.luxsoft.siipap.cxp.model.CXPCargo;

import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeCuentasPorPagar extends AbstractSelector<CXPCargo>{
	
	
	protected Proveedor proveedor;
	protected Currency moneda;
	
	private SelectorDeCuentasPorPagar() {
		super(CXPCargo.class, "Cuentas por pagar");
		
	}
	
	@Override
	protected TextFilterator<CXPCargo> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"id","documento"});
	}

	@Override
	protected TableFormat<CXPCargo> getTableFormat() {
		String props[]={"id","documento","fecha","vencimiento","moneda","total","pagos","saldoCalculado"};
		String labels[]={"CargoId","Docto","Fecha","Vencimiento","Moneda","Total","Pagos","Saldo"};
		return GlazedLists.tableFormat(CXPCargo.class,props,labels);
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
		header.setDescription("Cuentas por pagar   ");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	@Override
	protected List<CXPCargo> getData() {
		return CXPServiceLocator.getInstance().getFacturasManager().buscarCuentasPorPagar(proveedor, moneda);
	}
	
	public void clean(){
		proveedor=null;
		source.clear();
	}	

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
	

	private static SoftReference<SelectorDeCuentasPorPagar> INSTANCE;
	
	public static SelectorDeCuentasPorPagar getInstance(){
		if(INSTANCE==null){
			INSTANCE=new SoftReference<SelectorDeCuentasPorPagar>(new SelectorDeCuentasPorPagar());
		}
		return INSTANCE.get();
	}
	
	
	
	public static List<CXPCargo> buscarCuentasPorPagar(final Proveedor p,Currency moneda){
		SelectorDeCuentasPorPagar selector=new SelectorDeCuentasPorPagar();//getInstance();
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<CXPCargo> facturas=new ArrayList<CXPCargo>();
			facturas.addAll(selector.getSelectedList());
			selector.clean();
			return facturas;
		}		
		return new ArrayList<CXPCargo>(0);
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscarCuentasPorPagar(ServiceLocator2.getProveedorManager().buscarPorClave("I001"),MonedasUtils.PESOS);
				System.exit(0);
			}
			
		});
		
	}

}
