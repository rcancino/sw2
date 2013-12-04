package com.luxsoft.sw3.maquila.ui.selectores;

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

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;

public class SelectorDeBobinasParaAnalisis extends AbstractSelector<EntradaDeMaterialDet>{
	
	
	
	public SelectorDeBobinasParaAnalisis() {
		super(EntradaDeMaterialDet.class, "Bobinas disponibles para analisis");
		
	}

	@Override
	protected List<EntradaDeMaterialDet> getData() {
		String hql="from EntradaDeMaterialDet e where  e.analisis is null";
		return ServiceLocator2.getHibernateTemplate().find(hql);
		
	}
	
	public void adjustGrid(final JXTable grid){		
		grid.getColumnExt("Cortado (Kgs)").setVisible(false);
		grid.getColumnExt("Maqs (Kgs)").setVisible(false);
		grid.getColumnExt("Cortado (M2)").setVisible(false);
		grid.getColumnExt("Maqs (M2)").setVisible(false);
	}
	protected void setPreferedDimension(JComponent gridComponent){
		//gridComponent.setPreferredSize(new Dimension(850,400));
	}

	@Override
	protected TableFormat<EntradaDeMaterialDet> getTableFormat() {
		String[] props={
				"recepcion.id"
				,"recepcion.entradaDeMaquilador"
				,"clave"
				,"descripcion"
				,"kilos"
				,"disponibleKilos"
				,"metros2"
				,"disponibleEnM2"
				,"salidaACorteKilos"
				,"salidaAMaqKilos"
				,"salidaACorteM2"
				,"salidaAMaqM2"
				};
		String[] names={"Id","Entrada (Maq)"
				,"Producto"
				,"Descripción"
				,"Kilos"
				,"Disp (Kg)"
				,"M2"
				,"Disp (M2)"
				,"Cortado (Kgs)"
				,"Maqs (Kgs)"
				,"Cortado (M2)"
				,"Maqs (M2)"
				};
		return GlazedLists.tableFormat(EntradaDeMaterialDet.class,props, names);
	}

	@Override
	protected TextFilterator<EntradaDeMaterialDet> getBasicTextFilter() {
		return GlazedLists.textFilterator("id","recepcion.entradaDeMaquilador","producto.clave","producto.descripcion");
	}
	
	public static List<EntradaDeMaterialDet> seleccionar(){
		SelectorDeBobinasParaAnalisis selector=new SelectorDeBobinasParaAnalisis();
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<EntradaDeMaterialDet>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static EntradaDeMaterialDet find(){
		SelectorDeBobinasParaAnalisis selector=new SelectorDeBobinasParaAnalisis();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);		
		selector.open();		
		if(!selector.hasBeenCanceled()){
			EntradaDeMaterialDet selected=selector.getSelected();
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
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();								
				System.out.println(ToStringBuilder.reflectionToString(seleccionar()));
				System.exit(0);
			}
		});
	}

}
