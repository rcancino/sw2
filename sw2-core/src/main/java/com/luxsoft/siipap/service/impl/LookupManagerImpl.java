package com.luxsoft.siipap.service.impl;

import com.luxsoft.siipap.dao.LookupDao;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.LabelValue;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GProductoServicio;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.gastos.INPC;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.LookupManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;


/**
 * Implementation of LookupManager interface to talk to the persistence layer.
 *
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class LookupManagerImpl extends UniversalManagerImpl implements LookupManager {
    private LookupDao dao;

    /**
     * Method that allows setting the DAO to talk to the data store with.
     * @param dao the dao implementation
     */
    public void setLookupDao(LookupDao dao) {
        super.dao = dao;
        this.dao = dao;
    }

    /**
     * {@inheritDoc}
     */
    public List<LabelValue> getAllRoles() {
        List<Role> roles = dao.getRoles();
        List<LabelValue> list = new ArrayList<LabelValue>();

        for (Role role1 : roles) {
            list.add(new LabelValue(role1.getName(), role1.getName()));
        }

        return list;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Empresa> getEmpresas(){
    	return dao.getEmpresas();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Sucursal> getSucursales(){
    	return dao.getSucursales();
    }
    
    /**
     * Regresa las sucursales operativas registradas
     * 
     * @return
     */
    public List<Sucursal> getSucursalesOperativas(){
    	return dao.getSucursalesOperativas();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Departamento> getDepartamentos() {
		return dao.getDepartamentos();
	}

	/**
     * {@inheritDoc}
     */
    public List<Concepto> getConceptosDeIngreoEgreso(){
    	return dao.getConceptosDeIngreoEgreso();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Banco> getBancos(){
    	return dao.getBancos();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public List<Cuenta> getCuenta(){
    	return dao.getCuentas();
    }

	public List<ConceptoDeGasto> getClasificaciones() {
		return dao.getClasificaciones();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<GProductoServicio> getProductos() {
		return dao.getProductos();
	}

	public List<GProveedor> getProveedores() {
		return dao.getProveedores();
	}

	public List<String> getEstados() {
		final List<String> estados=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/estados.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null){
					estados.add(edo.trim().toUpperCase());
					
				}
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return estados;
	}

	public List<String> getCiudades() {
		final List<String> ciudades=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/ciudades.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null){
					
					ciudades.add(edo.trim().toUpperCase());	
				}
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ciudades;
	}

	public List<String> getMunicipios() {
		final List<String> municipios=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/municipios.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null)
					municipios.add(edo.trim().toUpperCase());				
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return municipios;
	}

	public List<INPC> getINPCs() {
		return dao.getINPCs();
	}
	
	
	
	
}
