package com.luxsoft.siipap.dao;

import java.util.List;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Cuenta;

/**
 * This class tests the current LookupDao implementation class
 * @author mraible
 */
public class LookupDaoTest extends BaseDaoTestCase {
    private LookupDao dao;
    
    public void setLookupDao(LookupDao dao) {
        this.dao = dao;
    }

    public void testGetRoles() {
        List roles = dao.getRoles();
        log.debug(roles);
        assertTrue(roles.size() > 0);
    }
    
    public void testGetEmpresas(){
    	List<Empresa> empresas=dao.getEmpresas();
    	log.debug(empresas);
    	assertTrue(empresas.size()>0);
    }
    
    public void testGetSucursales(){
    	List<Sucursal> beans = dao.getSucursales();
        log.debug(beans);
        assertTrue(beans.size() > 0);
    }
    
    public void testConceptosIngresoEgreso(){
    	List<Concepto> beans = dao.getConceptosDeIngreoEgreso();
        log.debug(beans);
        assertTrue(beans.size() > 0);
    }
    
    public void testBancos(){
    	List<Banco> beans = dao.getBancos();
        log.debug(beans);
        assertTrue(beans.size() > 0);
    }
    
    public void testGetCuentas(){
    	List<Cuenta> beans = dao.getCuentas();
        log.debug(beans);
        assertTrue(beans.size() > 0);
    }
   
}
