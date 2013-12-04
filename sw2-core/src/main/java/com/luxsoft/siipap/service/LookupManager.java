package com.luxsoft.siipap.service;

import java.util.List;

import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.LabelValue;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * Business Service Interface to talk to persistence layer and
 * retrieve values for drop-down choice lists.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public interface LookupManager extends UniversalManager {
    /**
     * Retrieves all possible roles from persistence layer
     * @return List of LabelValue objects
     */
    List<LabelValue> getAllRoles();
    
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
    List<Cuenta> getCuenta();
    
    /**
     * Regresa la lista de las clasificaciones para bienes
     * y servicios existentes
     * 
     * @return
     */
    List<ConceptoDeGasto> getClasificaciones();
    
    /**
     * Regresa una lista de los productos y servicios
     * disponibles
     * 
     * @return
     */
    List<GProductoServicio> getProductos();
    
    //List<Producto> getProductosComercia()
    
    /**
     * REgresa la lista de proveedores registrados
     * 
     * @return
     */
    List<GProveedor> getProveedores();
    
    /**
     * Estados registrados
     * 
     * @return
     */
    List<String> getEstados();
    
    /**
     * Ciudades registradas
     * @return
     */
    List<String> getCiudades();
    
    /**
     * Lista de municipios
     * @return
     */
    List<String> getMunicipios();
    
    /**
     * Lista de INPC
     * 
     * @return
     */
    List<INPC> getINPCs();
    
}
