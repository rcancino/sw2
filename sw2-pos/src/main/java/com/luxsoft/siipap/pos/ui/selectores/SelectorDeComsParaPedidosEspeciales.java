package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Selector de entradas por compra para analizar flete
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeComsParaPedidosEspeciales extends AbstractSelector<EntradaPorCompra>{
	
	String clave;
	
	public SelectorDeComsParaPedidosEspeciales(String clave) {
		super(EntradaPorCompra.class, "Entradas  ");
		this.clave=clave;
		
	}

	@Override
	protected List<EntradaPorCompra> getData() {
		String hql="from EntradaPorCompra e left join fetch e.recepcion r " +
		"  where e.clave=? " +
		"  and e.producto.medidaEspecial=true" +
		"  and e.recepcion is not null " 
		+"  and e.id != any( select p.entrada from PedidoDet  p where p.producto=e.producto)"
		//+"  and e.id=\'8a8a81b5-2eba9935-012e-bb23636c-000a\'"
		;
		return ServiceLocator2.getHibernateTemplate().find(hql,new Object[]{clave});
	}
	
	public void adjustGrid(final JXTable grid){
	}
	
	
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		builder.add(buildFilterPanel());
		addButton(builder);
		return builder.getToolBar();
	}
	
	
	
	
	
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,400));
	}

	@Override
	protected TableFormat<EntradaPorCompra> getTableFormat() {
		
		String[] props={
				"sucursal.nombre"
				,"recepcion.documento"
				,"recepcion.remision"
				,"renglon"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"kilosCalculados"
				,"costoFlete"
				//,"costoCorte"
				//,"costoMateria"
				,"comentario"
				};
		String[] names={
				"Sucursal"
				,"COM"
				,"Remisión"
				,"Rngl"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Kilos"
				,"Flete"
				//,"Hojeo","Costo M.P.","Comentario"
				};
		return GlazedLists.tableFormat(EntradaPorCompra.class, props,names);
	}

	@Override
	protected TextFilterator<EntradaPorCompra> getBasicTextFilter() {
		return GlazedLists.textFilterator("recepcion.remision","documento","producto.clave","producto.descripcion");
	}
	
	
	public static List<EntradaPorCompra> seleccionar(String clave){
		SelectorDeComsParaPedidosEspeciales selector=new SelectorDeComsParaPedidosEspeciales(clave);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<EntradaPorCompra>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static EntradaPorCompra find(String clave){
		SelectorDeComsParaPedidosEspeciales selector=new SelectorDeComsParaPedidosEspeciales(clave);		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			EntradaPorCompra selected=selector.getSelected();
			return selected;
		}
		return null;
	}
	
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				DBUtils.whereWeAre();
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();								
				System.out.println(ToStringBuilder.reflectionToString(seleccionar("SBS1537218")));
				System.exit(0);
			}
		});
	}

}
