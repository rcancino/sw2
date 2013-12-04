package com.luxsoft.sw3.maquila.ui.selectores;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.maquila.model.Almacen;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;

/**
 * Selector para Ordenes de cortes pendientes
 * entidades de tipo {@link OrdenDeCorteDet}
 * 
 * @author Ruben Cancino
 *
 */
public class SelectorDeCortesPendientes extends AbstractSelector<OrdenDeCorteDet>{
	
	private Almacen almacen;

	public SelectorDeCortesPendientes() {
		super(OrdenDeCorteDet.class, "Ordenes de corte pendientes");
	}

	@Override
	protected List<OrdenDeCorteDet> getData() {
		if(getAlmacen()==null){
			String hql="from OrdenDeCorteDet o left join fetch o.almacen a" +
			" where o.recibido=0 " +
			" order by o.orden.id";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}else{
			String hql="from OrdenDeCorteDet o left join fetch o.almacen a" +
			" where o.recibido=0 and a.id=?" +
			" order by o.orden.id";
			return ServiceLocator2.getHibernateTemplate().find(hql,getAlmacen().getId());
		}		
	}

	@Override
	protected TableFormat<OrdenDeCorteDet> getTableFormat() {
		String[] props={
				"orden.id"
				,"fecha"
				,"almacen.nombre"
				,"entradaDeMaquilador"
				,"origen.clave"
				,"destino.clave"
				,"destino.descripcion"
				,"kilos"
				,"metros2"
				,"millaresEstimados"
				,"comentario"
				,"recibido"
				};
		String[] names={
				"Orden"
				,"Fecha"
				,"Almacen"
				,"Entrada (Maq)"
				,"Bobina"
				,"Producto"
				,"Descripción"
				,"Kg"
				,"M2"
				,"Estimado(MIL)"
				,"Comentario"
				,"Recibido"
		};
		return GlazedLists.tableFormat(OrdenDeCorteDet.class,props, names);
	}
	
	

	/*@Override
	protected TextFilterator<OrdenDeCorteDet> getBasicTextFilter() {
		return GlazedLists.textFilterator("comentario");
	}*/

	public static List<OrdenDeCorteDet> seleccionar(){
		SelectorDeCortesPendientes selector=new SelectorDeCortesPendientes();
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<OrdenDeCorteDet> res=new ArrayList<OrdenDeCorteDet>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<OrdenDeCorteDet> seleccionar(Almacen almacen){
		SelectorDeCortesPendientes selector=new SelectorDeCortesPendientes();
		selector.open();
		selector.setAlmacen(almacen);
		if(!selector.hasBeenCanceled()){
			List<OrdenDeCorteDet> res=new ArrayList<OrdenDeCorteDet>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	
	public Almacen getAlmacen() {
		return almacen;
	}

	public void setAlmacen(Almacen almacen) {
		this.almacen = almacen;
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
				List data=seleccionar();
				System.out.println(data);
				System.exit(0);
			}

		});
	}
}
