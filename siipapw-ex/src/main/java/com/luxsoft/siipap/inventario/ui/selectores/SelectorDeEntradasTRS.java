package com.luxsoft.siipap.inventario.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
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

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.model.Periodo;
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
public  class SelectorDeEntradasTRS extends AbstractSelector<MovimientoDet>{
	
	protected Periodo periodo=Periodo.getPeriodoConAnteriroridad(2);
	
	
	private SelectorDeEntradasTRS() {
		super(MovimientoDet.class, "Traslados pendientes de importar");
		//setModal(false);
	}
	
	@Override
	protected TextFilterator<MovimientoDet> getBasicTextFilter() {
		return GlazedLists.textFilterator(new String[]{"documento","clave","descripcion","concepto"});
	}

	@Override
	protected TableFormat<MovimientoDet> getTableFormat() {
		String props[]={"sucursal.nombre","documento","fecha","producto.linea.nombre","producto.clase.nombre","clave","descripcion","cantidad","concepto","renglon"};
		String labels[]={"Suc","Docto","Fecha","Linea","Clase","Prod","Desc","Cantidad","Tipo","Rngl"};
		return GlazedLists.tableFormat(MovimientoDet.class,props,labels);
	}
	
	private HeaderPanel header=new HeaderPanel("Transformaciones pendientes de importar","");
	
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
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar pendientes en otro periodo");
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
		header.setDescription("Periodo: "+periodo.toString());
	}

	@Override
	protected List<MovimientoDet> getData() {
		String sql="from MovimientoDet d where d.fecha between ? and ? and d.concepto in(\'TRS\',\'REC\',\'REF\',\'RAU\',\'TRV\') order by d.sucursal.nombre,d.documento,d.renglon";
		return ServiceLocator2.getHibernateTemplate().find(sql, new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}	
	
	
	public void setPeriodo(Periodo periodo) {
		this.periodo = periodo;
		updatePeriodoLabel();
	}
	
	
	
	public static List<MovimientoDet> buscar(final Periodo periodo){
		SelectorDeEntradasTRS selector=new SelectorDeEntradasTRS();
		selector.setPeriodo(periodo);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		final List<MovimientoDet> res=new ArrayList<MovimientoDet>();
		if(!selector.hasBeenCanceled()&& (!selector.getSelectedList().isEmpty())){
			res.addAll(selector.getSelectedList());	
		}		
		return res;
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				buscar(Periodo.getPeriodoEnUnMes(5, 2009));
				System.exit(0);
			}
		});
		
	}

}
