package com.luxsoft.siipap.gastos.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Selector de instancias de gastos GCompraDet 
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturasDeGastos extends AbstractSelector<GCompraDet>{
	
	
	
	protected Periodo periodo=Periodo.getPeriodoDelMesActual();
	
	public SelectorDeFacturasDeGastos() {
		super(GCompraDet.class, "Activos pendientes");
		
	}
	
	protected JTextField docField=new JTextField(30);
	
	@Override
	protected void installEditors(EventList<MatcherEditor<GCompraDet>> editors) {
		textFilter=new JTextField(10);
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(docField
				,GlazedLists.textFilterator(new String[]{"factura","compra.proveedor.nombreRazon","producto.descripcion"}));
		editors.add(docEditor);
	}

	protected JComponent buildFilterPanel(){
		ButtonBarBuilder builder=ButtonBarBuilder.createLeftToRightBuilder();		
		//builder.addUnrelatedGap();
		
		ActionLabel al=new ActionLabel("Cambiar periodo");
		al.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		builder.addGridded(al);
		builder.addRelatedGap();
		
		builder.addGridded(new JLabel("Filtrar"));
		builder.addRelatedGap();
		builder.addGridded(docField);
		builder.addGlue();
		return builder.getPanel();
	}

	@Override
	protected TableFormat<GCompraDet> getTableFormat() {
		final String[] props={
				"compra.id"
				,"factura"
				,"facturacion.fecha"
				,"compra.proveedor"
				,"rubro.descripcion"
				,"producto.descripcion"
				,"cantidad"
				,"precio"
				,"importe"				
				};
		final String[] names={
				"Compra"
				,"Factura"
				,"Fecha fac"
				,"Proveedor"
				,"Concepto"
				,"Descripción"
				,"Cantidad"
				,"Precio"
				,"Importe"				
				};
		
		return GlazedLists.tableFormat(GCompraDet.class, props, names);
	}
	
	private HeaderPanel header=new HeaderPanel("","");
	
	protected JComponent buildHeader(){
		updateHeader();
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(810,500));
	}
	
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		this.periodo=(Periodo)holder.getValue();
		updateHeader();
		load();
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createLoadAction(this, "load");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar Otra compra");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	protected void updateHeader(){
		header.setDescription("Activos ");
		header.setTitle("Pendientes por trasladar");
	}

	@Override
	protected List<GCompraDet> getData() {
		String hql="from GCompraDet f where  f.producto.inversion=true" +
				" and f not in(select a.compraDeGastoDet from ActivoFijo a)";
		return ServiceLocator2.getHibernateTemplate().find(hql);
	}
	
	public void clean(){
		source.clear();
	}	
	
	public static GCompraDet buscar(){
		List<GCompraDet> facturas=new ArrayList<GCompraDet>();
		SelectorDeFacturasDeGastos selector=new SelectorDeFacturasDeGastos();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			facturas.addAll(selector.getSelectedList());
		}		
		return facturas.isEmpty()?null:facturas.get(0);
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscar();
				System.exit(0);
			}
			
		});
		
	}

}
