package com.luxsoft.siipap.dao.gastos;

import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.gastos.GCxP;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;

/**
 *NO USAR USARA {@link GFacturaPorCompra}  
 * @author RUBEN
 *
 */
@Deprecated
public class GCxPDaoImpl extends GenericDaoHibernate<GCxP, Long> implements GCxPDao{

	public GCxPDaoImpl() {
		super(GCxP.class);
	}

	@SuppressWarnings("unchecked")
	public List<GCxP> buscarPorProveedor(GProveedor p) {
		return getHibernateTemplate().find("from GCxP c where c.proveedor=?", p);
	}

}
