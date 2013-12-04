package com.luxsoft.siipap.gastos;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.binding.beans.Model;
import com.luxsoft.siipap.dao.gastos.GProductoServicioDao;
import com.luxsoft.siipap.dao.gastos.GProveedorDao;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Punto central de acceso a informacion para la capa de presentacion
 * para el modulo de Gastos
 * 
 * En ocaciones funciona como Facade para la capa de persistencia ej: ServiceManagers 
 * 
 * @author Ruben Cancino
 *
 */
public class GastosModel extends Model{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private static GastosModel INSTANCE;
	
	private GastosModel(){
		init();
	}
	
	private void init(){
		
	}
	
	/**
	 * Regresa la lista de clasificaciones pero como un {@link EventList}
	 * @return
	 */
	public EventList<ConceptoDeGasto> getClasificaciones(){
		final EventList<ConceptoDeGasto> source=GlazedLists.threadSafeList(new BasicEventList<ConceptoDeGasto>());
		source.addAll(ServiceLocator2.getLookupManager().getClasificaciones());
		return source;
	}
	
	public GProveedor buscarProveedorPorNombre(final String nombre){
		GProveedorDao dao=(GProveedorDao)ServiceLocator2.instance().getContext().getBean("proveedorDao");
		try {
			return dao.buscarPorNombre(nombre);
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}		
	}
	
	public GProveedor buscarProveedorPorRfc(final String rfc){
		GProveedorDao dao=(GProveedorDao)ServiceLocator2.instance().getContext().getBean("proveedorDao");
		try {
			return dao.buscarPorRfc(rfc);
		} catch (Exception ex) {
			logger.error(ex);
			return null;
		}		
	}
	
	public GProductoServicio buscarProducto(final String clave){
		GProductoServicioDao dao=(GProductoServicioDao)ServiceLocator2.instance().getContext().getBean("productoServicioDao");
		try {
			return dao.buscarPorClave(clave);
		} catch (Exception ex) {
			//logger.error(ex);
			return null;
		}	
	}
	
	
	
	
	/**
	 * Acceso publico al Singleton de esta clase
	 * 
	 * @return El GastoModel 
	 */
	public static synchronized  GastosModel instance() {
		if(INSTANCE==null){
			INSTANCE=new GastosModel();
		}
		return INSTANCE;
	}

}
