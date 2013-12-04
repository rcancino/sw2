package com.luxsoft.sw3.maquila.ui.selectores;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;

public class SelectorDeBobinasDisponibles extends AbstractSelector<EntradaDeMaterialDet>{
	
	private  Almacen almacen;
	
	public SelectorDeBobinasDisponibles() {
		this(null);
	}

	public SelectorDeBobinasDisponibles(final Almacen almacen) {
		super(EntradaDeMaterialDet.class, "Bobinas disponibles");
		this.almacen=almacen;
	}

	@Override
	protected List<EntradaDeMaterialDet> getData() {
		if(getAlmacen()!=null){
			String hql="from EntradaDeMaterialDet e where e.recepcion.almacen.id=? and e.disponibleKilos>0";
			return ServiceLocator2.getHibernateTemplate().find(hql,almacen.getId());
		}else{
			String hql="from EntradaDeMaterialDet e where  e.disponibleKilos>0";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}
		
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
	
	
	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		this.almacen = almacen;
	}

	public static EntradaDeMaterialDet seleccionar(final Almacen almacen){
		SelectorDeBobinasDisponibles selector=new SelectorDeBobinasDisponibles(almacen);		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.setAlmacen(almacen);
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
				//Almacen almacen=(Almacen)ServiceLocator2.getUniversalDao().get(Almacen.class, 1l);
				EntradaDeMaterialDet res=seleccionar(null);
				System.out.println(ToStringBuilder.reflectionToString(res));
				System.exit(0);
			}
		});
	}

}
