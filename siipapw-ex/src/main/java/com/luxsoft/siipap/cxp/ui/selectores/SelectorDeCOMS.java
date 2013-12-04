package com.luxsoft.siipap.cxp.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.springframework.util.Assert;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.cxp.service.CXPServiceLocator;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Selector de facturas para un proveedor, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeCOMS extends AbstractSelector<EntradaPorCompra>{
	
	protected Periodo periodo=Periodo.getPeriodoConAnteriroridad(2);
	protected Proveedor proveedor;
	
	private SelectorDeCOMS() {
		super(EntradaPorCompra.class, "Entradas pendientes de analizar");
		//setModal(false);
	}
	
	@Override
	protected TextFilterator<EntradaPorCompra> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"remision","documento","compra","descripcion","clave"});
	}

	@Override
	protected TableFormat<EntradaPorCompra> getTableFormat() {
		String props[]={"remision","fechaRemision","compra","fechaCompra","documento","fecha","clave","descripcion","cantidad","analizado","pendienteDeAnalisis"};
		String labels[]={"Remisión","F.Remisión","Compra","compra F.","Com","Entrada","Producto","Descripcion","Ingresado","Analizado","Por analizar"};
		return GlazedLists.tableFormat(EntradaPorCompra.class,props,labels);
	}
	
	private HeaderPanel header=new HeaderPanel("","");
	
	protected JComponent buildHeader(){
		updatePeriodoLabel();
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(810,500));
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();			
			load();
			updatePeriodoLabel();
		}
	}
	
	public void open(){
		super.open();
		load();
	}

	protected void updatePeriodoLabel(){
		header.setDescription("Entradas pendientes de analizar para el periodo: "+periodo.toString());
	}

	@Override
	protected List<EntradaPorCompra> getData() {
		return CXPServiceLocator.getInstance().getEntradaPorCompraDao()
		.buscarAnalisisPendientes(proveedor, periodo);
	}
	
	

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;	
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}
	
	
	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
		updatePeriodoLabel();
	}
	
	
	public static EntradaPorCompra buscarEntrada(final Proveedor p){
		SelectorDeCOMS selector=new SelectorDeCOMS();
		selector.setProveedor(p);
		selector.open();
		if(!selector.hasBeenCanceled()){
			
			return selector.getSelected();
		}		
		return null;
	}
	
	public static List<EntradaPorCompra> buscarEntradas(final Proveedor p){
		Assert.notNull(p,"Se requiere el proveedor");
		SelectorDeCOMS selector=new SelectorDeCOMS();
		selector.setProveedor(p);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<EntradaPorCompra> entradas=new ArrayList<EntradaPorCompra>();
			entradas.addAll(selector.getSelectedList());			
			return entradas;
		}		
		return new ArrayList<EntradaPorCompra>(0);
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				buscarEntradas(ServiceLocator2.getProveedorManager().buscarPorClave("I001"));
				System.exit(0);
			}
			
		});
		
	}

}
