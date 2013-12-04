package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Date;
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
import com.luxsoft.siipap.util.DateUtil;


/**
 * Consulta de Entradas por compra
 * 
 * @author Ruben Cancino 
 *
 */
public  class AnalisisDeEntradasPorCompra extends AbstractSelector<EntradaPorCompra>{
	
	private Periodo periodo;
	private Proveedor proveedor;
	
	private AnalisisDeEntradasPorCompra() {
		super(EntradaPorCompra.class, "Entradas al almacén");
		setModal(false);
	}
	
	@Override
	protected TextFilterator<EntradaPorCompra> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"remision","documento","compra","descripcion"});
	}

	@Override
	protected TableFormat<EntradaPorCompra> getTableFormat() {
		String props[]={
				"sucursal.nombre"
				,"compra"
				,"fechaCompra"
				,"documento"
				,"fecha"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"costoEntrada"
				,"pendienteDeAnalisis"
				};
		String labels[]={
				"Sucursal"
				,"Compra"
				,"Compra F."
				,"Com"
				,"Entrada"
				,"Producto"
				,"Descripcion"
				,"Ingresado"
				,"Costo"
				,"Por analizar"
				};
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
		header.setDescription("Entradas del periodo: "+periodo.toString());
	}

	@Override
	protected List<EntradaPorCompra> getData() {
		String hql="from EntradaPorCompra e where e.proveedor.id=? and date(e.fecha)=?";
		Object[] params=new Object[]{proveedor.getId(),periodo.getFechaFinal()};
		return ServiceLocator2.getHibernateTemplate().find(hql,params);
		/*
		return CXPServiceLocator.getInstance().getEntradaPorCompraDao()
		.buscarAnalisisPendientes(proveedor, periodo);
		*/
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
		AnalisisDeEntradasPorCompra selector=new AnalisisDeEntradasPorCompra();
		selector.setProveedor(p);
		selector.open();
		if(!selector.hasBeenCanceled()){
			
			return selector.getSelected();
		}		
		return null;
	}
	
	public static List<EntradaPorCompra> buscarEntradas(final String claveProveedor,Date fecha){
		Proveedor proveedor=ServiceLocator2.getProveedorManager().buscarPorClave(claveProveedor);
		return buscarEntradas(proveedor,fecha);
	}
	
	public static List<EntradaPorCompra> buscarEntradas(final Proveedor p,Date fecha){
		Assert.notNull(p,"Se requiere el proveedor");
		AnalisisDeEntradasPorCompra selector=new AnalisisDeEntradasPorCompra();
		selector.setProveedor(p);
		selector.setPeriodo(new Periodo(fecha));
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<EntradaPorCompra> entradas=new ArrayList<EntradaPorCompra>();
			//entradas.addAll(selector.getSelectedList());			
			return entradas;
		}		
		return new ArrayList<EntradaPorCompra>(0);
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				buscarEntradas("A001",DateUtil.toDate("01/08/2011"));
				//System.exit(0);
			}
			
		});
		
	}

}
