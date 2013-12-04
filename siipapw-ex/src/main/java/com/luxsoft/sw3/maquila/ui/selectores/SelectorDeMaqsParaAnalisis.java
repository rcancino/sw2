package com.luxsoft.sw3.maquila.ui.selectores;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.util.DBUtils;


public class SelectorDeMaqsParaAnalisis extends AbstractSelector<EntradaDeMaquila>{
	
	private boolean flete=true;
	
	public SelectorDeMaqsParaAnalisis() {
		super(EntradaDeMaquila.class, "Entradas de maquila (MAQ) disponibles para analisis gastos");
		
	}

	@Override
	protected List<EntradaDeMaquila> getData() {
		if(flete){
			String hql="from EntradaDeMaquila e left join fetch e.recepcion r " +
			"  where e.analisisFlete is null " +
			"    and e.recepcion is not null";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}else{
			String hql="from EntradaDeMaquila e left join fetch e.recepcion r " +
			"  where e.analisisHojeo is null " +
			"    and e.recepcion is not null";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}
		
		
	}
	
	public void adjustGrid(final JXTable grid){		
		
	}
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(750,400));
	}

	@Override
	protected TableFormat<EntradaDeMaquila> getTableFormat() {
		
		String[] props={"sucursal.nombre","recepcion.documento","recepcion.remision","renglon","clave","descripcion","cantidad","kilosCalculados","costoFlete","costoCorte","costoMateria","comentario"};
		String[] names={"Sucursal","Maq","Remisión","Rngl","Producto","Descripción","Cantidad","Kilos","Flete","Hojeo","Costo M.P.","Comentario"};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,names);
	}

	@Override
	protected TextFilterator<EntradaDeMaquila> getBasicTextFilter() {
		return GlazedLists.textFilterator("recepcion.remision","documento","producto.clave","producto.descripcion");
	}
	
	
	
	public boolean isFlete() {
		return flete;
	}

	public void setFlete(boolean flete) {
		this.flete = flete;
	}

	public static List<EntradaDeMaquila> seleccionar(boolean flete){
		SelectorDeMaqsParaAnalisis selector=new SelectorDeMaqsParaAnalisis();
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.setFlete(flete);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<EntradaDeMaquila>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static EntradaDeMaquila find(boolean flete){
		SelectorDeMaqsParaAnalisis selector=new SelectorDeMaqsParaAnalisis();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.setFlete(flete);
		selector.open();		
		if(!selector.hasBeenCanceled()){
			EntradaDeMaquila selected=selector.getSelected();
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
				System.out.println(ToStringBuilder.reflectionToString(seleccionar(true)));
				System.exit(0);
			}
		});
	}

}
