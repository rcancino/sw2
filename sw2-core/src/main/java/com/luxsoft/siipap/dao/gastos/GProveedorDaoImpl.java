package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.gastos.GProveedor;

/**
 * Implementacion de {@link GProveedorDao}

 * @author Ruben Cancino
 *
 */
@SuppressWarnings("unchecked")
public class GProveedorDaoImpl extends GenericDaoHibernate<GProveedor, Long> implements GProveedorDao{

	public GProveedorDaoImpl() {
		super(GProveedor.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public GProveedor buscarPorNombre(String clave) {
		List<GProveedor> l=getHibernateTemplate()
		.find("from GProveedor p where p.nombre=?", clave);
		return l.isEmpty()?null:l.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public GProveedor buscarPorRfc(String rfc) {
		List<GProveedor> l=getHibernateTemplate()
		.find("from GProveedor p where p.rfc=?", rfc);
		return l.isEmpty()?null:l.get(0);
	}
	
	

}
