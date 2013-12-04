package com.luxsoft.siipap.dao;

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

import java.util.List;

/**
 * Lookup Data Access Object (GenericDao) interface.  This is used to lookup values in
 * the database (i.e. for drop-downs).
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public interface LookupDao extends UniversalDao {
    //~ Methods ================================================================

    /**
     * Returns all Roles ordered by name
     * @return populated list of roles
     */
    List<Role> getRoles();
    
    /**
     * Regresa las empresas registradas
     * 
     * @return
     */
    List<Empresa> getEmpresas();
    
    /**
     * Regresa las sucursales registradas
     * 
     * @return
     */
    List<Sucursal> getSucursales();
    
    /**
     * Regresa las sucursales operativas registradas
     * 
     * @return
     */
    List<Sucursal> getSucursalesOperativas();
    
    /**
     * Regresa las departamentos registradas
     * 
     * @return
     */
    List<Departamento> getDepartamentos();
    
    /**
     * Regresa los conceptos de ingreso/egreso
     * 
     * @return
     */
    List<Concepto> getConceptosDeIngreoEgreso();
    
    /**
     * Regresa los bancos del sistema
     * 
     * @return
     */
    List<Banco> getBancos();    
    
    
    /**
     * Regresa las cuentas de banco registradas
     * 
     * @return
     */
    List<Cuenta> getCuentas();
    
    /**
     * Regresa la lista de las clasificaciones para bienes
     * y servicios existentes
     * 
     * @return
     */
    List<ConceptoDeGasto> getClasificaciones();
    
    /**
     * Regresa la lista de productos y servicios registrados
     * 
     * @return
     */
    List<GProductoServicio> getProductos();
    
    /**
     * REgresa la lista de proveedores registrados
     * 
     * @return
     */
    List<GProveedor> getProveedores();
    
    List<INPC> getINPCs();
     
}
