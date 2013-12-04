package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.gastos.GProductoServicio;

/**
 * Implementacion de {@link GProductoServicioDao}
 * 
 * @author Ruben Cancino
 *
 */
public class GProductoServicioDaoImpl extends GenericDaoHibernate<GProductoServicio, Long> implements GProductoServicioDao{

	public GProductoServicioDaoImpl() {
		super(GProductoServicio.class);
		
	}

	@SuppressWarnings("unchecked")
	public GProductoServicio buscarPorClave(String clave) {
		List<GProductoServicio> list=getHibernateTemplate()
			.find("from GProductoServicio g where g.clave=?", clave);
		return list.isEmpty()?null:list.get(0);
	}

}
