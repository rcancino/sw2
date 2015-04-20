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

import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;

public class SelectorDeSMQ extends AbstractSelector<MovimientoDet>{
	
	
	
	public SelectorDeSMQ() {
		super(MovimientoDet.class, "Salidas al Maquilador");
		
	}

	@Override
	protected List<MovimientoDet> getData() {
		String hql="from MovimientoDet m where  m.movimiento.concepto=\'SMQ\' and m not in (select d.movimientoDet from  EntradaDeMaterialDet d where d.movimientoDet is not null )  ";
		return ServiceLocator2.getHibernateTemplate().find(hql);
		
	}
	
	public void adjustGrid(final JXTable grid){		
		/*grid.getColumnExt("Cortado (Kgs)").setVisible(false);
		grid.getColumnExt("Maqs (Kgs)").setVisible(false);
		grid.getColumnExt("Cortado (M2)").setVisible(false);
		grid.getColumnExt("Maqs (M2)").setVisible(false);*/
	}
	protected void setPreferedDimension(JComponent gridComponent){
		//gridComponent.setPreferredSize(new Dimension(850,400));
	}

	@Override
	protected TableFormat<MovimientoDet> getTableFormat() {
		String[] props={
				"documento","sucursal.nombre","producto.clave","producto.descripcion","cantidad"
				};
		String[] names={"Documento","Sucursal","Clave","Descripcion","Cantidad"
				};
		return GlazedLists.tableFormat(MovimientoDet.class,props, names);
	}

	@Override
	protected TextFilterator<MovimientoDet> getBasicTextFilter() {
		return GlazedLists.textFilterator("documento","sucursal.nombre","producto.clave","producto.descripcion");
	}
	
	public static List<MovimientoDet> seleccionar(){
		SelectorDeSMQ selector=new SelectorDeSMQ();
		selector.open();		
		if(!selector.hasBeenCanceled()){
			return new ArrayList<MovimientoDet>(selector.getSelectedList());
		}
		return ListUtils.EMPTY_LIST;
	}

	public static MovimientoDet find(){
		SelectorDeSMQ selector=new SelectorDeSMQ();		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);		
		selector.open();		
		if(!selector.hasBeenCanceled()){
			MovimientoDet selected=selector.getSelected();
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
