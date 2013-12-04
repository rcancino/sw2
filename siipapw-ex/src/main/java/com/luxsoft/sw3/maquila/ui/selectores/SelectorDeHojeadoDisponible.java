package com.luxsoft.sw3.maquila.ui.selectores;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;

/**
 * Selector para Hojeado disponible
 * entidades de tipo {@link RecepcionDeCorteDet}
 * 
 * @author Ruben Cancino
 *
 */
public class SelectorDeHojeadoDisponible extends AbstractSelector<RecepcionDeCorteDet>{
	
	private Producto producto;

	public SelectorDeHojeadoDisponible() {
		super(RecepcionDeCorteDet.class, "Inventario Hojeado");
	}

	@Override
	protected List<RecepcionDeCorteDet> getData() {
		if(getProducto()==null){
			String hql="from RecepcionDeCorteDet r " +
					" left join fetch r.almacen a" +
					" left join fetch r.origen o" +
			" where r.disponible>0 " +
			" order by r.id";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}else{
			String hql="from RecepcionDeCorteDet r " +
					" left join fetch r.almacen a" +
					" left join fetch r.origen o " +
			" where r.disponible>0 " +
			" and r.producto.id=?" +
			" order by r.id";
			return ServiceLocator2.getHibernateTemplate().find(hql,getProducto().getId());
		}		
	}

	@Override
	protected TableFormat<RecepcionDeCorteDet> getTableFormat() {
		String[] props={
				"fecha"
				,"almacen.nombre"
				,"origen.entradaDeMaquilador"
				,"producto.clave"
				,"producto.descripcion"
				,"disponible"
				,"costo"
				,"comentario"
				};
		String[] names={
				"Fecha"
				,"Almacén"
				,"Entrada (Maq)"
				,"Producto"
				,"Descripción"
				,"Disponible"
				,"Costo"
				,"Comentario"
		};
		return GlazedLists.tableFormat(RecepcionDeCorteDet.class,props, names);
	}
	

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}

	/*@Override
	protected TextFilterator<OrdenDeCorteDet> getBasicTextFilter() {
		return GlazedLists.textFilterator("comentario");
	}*/

	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(700,400));
	}
	
	public static List<RecepcionDeCorteDet> seleccionar(){
		SelectorDeHojeadoDisponible selector=new SelectorDeHojeadoDisponible();
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<RecepcionDeCorteDet> res=new ArrayList<RecepcionDeCorteDet>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<RecepcionDeCorteDet> seleccionar(Producto producto){
		SelectorDeHojeadoDisponible selector=new SelectorDeHojeadoDisponible();
		selector.setProducto(producto);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<RecepcionDeCorteDet> res=new ArrayList<RecepcionDeCorteDet>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static RecepcionDeCorteDet find(){
		List<RecepcionDeCorteDet> res=seleccionar();
		if(!res.isEmpty())
			return res.get(0);
		else
			return null;
	}
	
	public static RecepcionDeCorteDet find(Producto prod){
		List<RecepcionDeCorteDet> res=seleccionar(prod);
		if(!res.isEmpty())
			return res.get(0);
		else
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
				Producto producto=ServiceLocator2.getProductoManager().buscarPorClave("SBS16012");
				List data=seleccionar(producto);
				System.out.println(data);
				System.exit(0);
			}

		});
	}
}
