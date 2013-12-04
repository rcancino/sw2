package com.luxsoft.sw3.maquila.ui.selectores;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.collections.ListUtils;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;


/**
 * Selector de {@link EntradaDeMaquila} disponibles de ser atendidos
  * 
 * @author Ruben Cancino
 *
 */
public class SelectorDeMaqs extends AbstractSelector<EntradaDeMaquila>{
	
	private Producto producto;

	public SelectorDeMaqs() {
		super(EntradaDeMaquila.class, "Entradas de material cortado al punto de venta");
	}

	@Override
	protected List<EntradaDeMaquila> getData() {
		if(getProducto()==null){
			String hql="from EntradaDeMaquila e " +
					" where e.cantidad-e.atendido-e.atendidoDirecto>0 " +
					" and e.recepcion is not null" +
					" order by e.fecha desc";
			return ServiceLocator2.getHibernateTemplate().find(hql);
		}else{
			String hql="from EntradaDeMaquila e " +
					" where e.producto.id=? " +
					"   and e.cantidad-e.atendido-e.atendidoDirecto>0 " +
					"  and e.recepcion is not null" +
					" order by e.fecha desc";
			return ServiceLocator2.getHibernateTemplate().find(hql,getProducto().getId());
		}		
	}

	@Override
	protected TableFormat<EntradaDeMaquila> getTableFormat() {
		String[] props={
				"sucursal.nombre","documento","clave","descripcion"
				,"cantidad"
				,"atendido"
				,"atendidoDirecto"
				,"pendiente"
				,"comentario"
				};
		String[] names={
				"Sucursal","Docto","Producto","Descripción"
				,"Cantidad"
				,"Atendido"
				,"Aten (Directo)"
				,"Pendiente"
				,"Comentario"
				};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,names);
	}
	

	public Producto getProducto() {
		return producto;
	}

	public void setProducto(Producto producto) {
		this.producto = producto;
	}	

	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(700,400));
	}
	
	public static List<EntradaDeMaquila> seleccionar(){
		SelectorDeMaqs selector=new SelectorDeMaqs();
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<EntradaDeMaquila> res=new ArrayList<EntradaDeMaquila>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static List<EntradaDeMaquila> seleccionar(Producto producto){
		SelectorDeMaqs selector=new SelectorDeMaqs();
		selector.setProducto(producto);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<EntradaDeMaquila> res=new ArrayList<EntradaDeMaquila>(selector.getSelectedList());
			return res;
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static EntradaDeMaquila find(){
		List<EntradaDeMaquila> res=seleccionar();
		if(!res.isEmpty())
			return res.get(0);
		else
			return null;
	}
	
	public static EntradaDeMaquila find(Producto prod){
		List<EntradaDeMaquila> res=seleccionar(prod);
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
				Producto producto=ServiceLocator2.getProductoManager().buscarPorClave("SBSB17112");
				List data=seleccionar(producto);
				System.out.println(data);
				System.exit(0);
			}

		});
	}
}
