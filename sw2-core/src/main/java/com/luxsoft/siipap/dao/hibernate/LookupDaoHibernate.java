package com.luxsoft.siipap.dao.hibernate;

import java.util.List;

import com.luxsoft.siipap.dao.LookupDao;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * Hibernate implementation of LookupDao.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
@SuppressWarnings("unchecked")
public class LookupDaoHibernate extends UniversalDaoHibernate implements LookupDao {

    /**
     * {@inheritDoc}
     */    
    public List<Role> getRoles() {
        log.debug("Retrieving all role names...");
        return getHibernateTemplate().find("from Role order by name");
    }
    
    /**
     * {@inheritDoc}
     */
	public List<Empresa> getEmpresas() {
		return getHibernateTemplate().find("from Empresa order by nombre");
	}

    /**
     * {@inheritDoc}
     */
	public List<Banco> getBancos() {
		return getHibernateTemplate().find("from Banco order by clave");
	}

	/**
     * {@inheritDoc}
     */
	public List<Concepto> getConceptosDeIngreoEgreso() {
		return getHibernateTemplate().find("from Concepto order by clave");
	}

	/**
     * {@inheritDoc}
     */
	public List<Cuenta> getCuentas() {
		return getHibernateTemplate().find("from Cuenta order by clave");
	}

	

	/**
     * {@inheritDoc}
     */
	public List<Sucursal> getSucursales() {
		return getHibernateTemplate().find("from Sucursal s where s.habilitada=?", Boolean.TRUE);
	}
	
	/**
     * Regresa las sucursales operativas registradas
     * 
     * @return
     */
    public List<Sucursal> getSucursalesOperativas(){
    	return getHibernateTemplate().find("from Sucursal s where s.habilitada=? and s.clave not in(1,50)", Boolean.TRUE);
    }
	

	public List<Departamento> getDepartamentos() {
		return getHibernateTemplate().find("from Departamento order by clave");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ConceptoDeGasto> getClasificaciones() {
		return getHibernateTemplate().find("from ConceptoDeGasto order by clave");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GProductoServicio> getProductos() {
		return getHibernateTemplate().find("from GProductoServicio g  order by g.clave");
	}
	
	/**
     * {@inheritDoc}
     * 
     */
    public List<GProveedor> getProveedores(){
    	return getHibernateTemplate().find("from GProveedor p order by p.nombre");
    }

	public List<INPC> getINPCs() {
		return getHibernateTemplate().find("from INPC p ");
	}
	
	
}
