package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.UniqueList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matchers;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.sw3.services.Services;

/**
 * Selector de compras pendientes de entrega
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeComprasPendientes extends AbstractSelector<Compra2>{
	
	
	private Sucursal sucursal;
	
	private SelectorDeComprasPendientes() {
		super(Compra2.class, "");
		sucursal=Services.getInstance().getConfiguracion().getSucursal();
		setTitle("Compras pendientes"+ "     ["+sucursal.getNombre()+"]");
		
	}
	@Override
	protected TableFormat<Compra2> getTableFormat() {
		String props[]={"sucursal.nombre","folio","fecha","nombre","entrega","consolidada","importacion","comentario"};
		String labels[]={"Sucursal","Folio","Fecha","Nombre","Entrega","Consolidada","Importación","Comentario"};
		return GlazedLists.tableFormat(Compra2.class,props,labels);
	}
	
	public void initGlazedLists(){
		source=GlazedLists.threadSafeList(new BasicEventList<Compra2>());
		source=new UniqueList<Compra2>(source,GlazedLists.beanPropertyComparator(Compra2.class,"id"));
		sortedSource=new SortedList<Compra2>(buildFilterList(),getComparator());		
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){		
		header=new HeaderPanel("Compras pendientes de entrega","");
		return header;
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
	
	@Override
	protected void onWindowOpened() {
		load();
	}

	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,450));
	}

	@Override
	protected List<Compra2> getData() {
		
		String hql="select cu.compra from CompraUnitaria cu " +
				" where cu.solicitado-cu.recibido>0" +
				" and cu.depuracion is null" +
				" and cu.compra.sucursal.id in(?,?)";
		
		//String hql="from Compra2 c where c.sucursal.id=? and c.depuracion=null ";
		return Services.getInstance().getHibernateTemplate()
			.find(hql,new Object[]{ sucursal.getId(),new Long(1)});
	}
	
	public void clean(){		
		source.clear();
	}
	
	
	
	@Override
	protected TextFilterator<Compra2> getBasicTextFilter() {
		return GlazedLists.textFilterator("folio","nombre","sucursal.nombre");
	}
	public static Compra2 seleccionar(){
		SelectorDeComprasPendientes selector=new SelectorDeComprasPendientes();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			
			Compra2 selected=selector.getSelected();
			selector.clean();
			return Services.getInstance().getComprasManager().buscarInicializada(selected.getId());
		}		
		return null;
	}
	
	
	/**
	public static buscarVenta(final Cliente c){
		
	}
	**/

	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				Compra2 res=seleccionar();
				System.out.println(res);
				System.exit(0);
			}
			
		});
		
	}

}
