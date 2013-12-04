package com.luxsoft.siipap.compras.dao;

import java.util.List;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.core.Proveedor;

public class ProveedorDaoImpl extends GenericDaoHibernate<Proveedor, Long> implements ProveedorDao{

	public ProveedorDaoImpl() {
		super(Proveedor.class);
	}
	
	public Proveedor buscarPorClave(String clave) {
		List<Proveedor> provs=getHibernateTemplate().find("from Proveedor p where p.clave=?", clave);
		return provs.isEmpty()?null:provs.get(0);
	}

	public Proveedor buscarPorNombre(String nombre) {
		List<Proveedor> provs=getHibernateTemplate().find("from Proveedor p where p.nombre=?", nombre);
		return provs.isEmpty()?null:provs.get(0);
	}

	public Proveedor buscarPorRfc(String rfc) {
		List<Proveedor> provs=getHibernateTemplate().find("from Proveedor p where p.rfv=?", rfc);
		return provs.isEmpty()?null:provs.get(0);
	}

	
	public Proveedor save(Proveedor p) {
		p.actualizarNombre();
		return super.save(p);
	}

	
	@Override
	public Proveedor get(Long id) {
		List<Proveedor> res=getHibernateTemplate().find("from Proveedor p " +
				"left join fetch p.productos where p.id=?", id);
		if(res.isEmpty()){
			log.warn("Uh oh, '" + Proveedor.class+ "' object with id '" + id + "' not found...");
            throw new ObjectRetrievalFailureException(Proveedor.class, id);
		}
		return res.get(0);
			
	}

	public List<Proveedor> buscarActivos() {
		return getHibernateTemplate().find("from Proveedor p where p.activo=true");
	}

	public List<Proveedor> buscarImportadores() {
		return getHibernateTemplate().find("from Proveedor p where p.activo=true and p.importador=true");
	}

	
	
	

}
