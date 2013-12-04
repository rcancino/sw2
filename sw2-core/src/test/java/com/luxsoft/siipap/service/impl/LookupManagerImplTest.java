package com.luxsoft.siipap.service.impl;

import com.luxsoft.siipap.dao.LookupDao;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Role;
import com.luxsoft.siipap.model.LabelValue;
import com.luxsoft.siipap.Constants;
import org.jmock.Mock;

import java.util.ArrayList;
import java.util.List;


public class LookupManagerImplTest extends BaseManagerMockTestCase {
    private LookupManagerImpl mgr = new LookupManagerImpl();
    private Mock lookupDao = null;

    protected void setUp() throws Exception {
        super.setUp();
        lookupDao = new Mock(LookupDao.class);
        mgr.setLookupDao((LookupDao) lookupDao.proxy());
    }

    public void testGetAllRoles() {
        log.debug("entered 'testGetAllRoles' method");

        // set expected behavior on dao
        Role role = new Role(Constants.ADMIN_ROLE);
        List<Role> testData = new ArrayList<Role>();
        testData.add(role);
        lookupDao.expects(once()).method("getRoles").withNoArguments().will(returnValue(testData));

        List<LabelValue> roles = mgr.getAllRoles();
        assertTrue(roles.size() > 0);
    }
    
    public void testGetEmpresas(){
    	log.debug("entered 'testGetEmpresas' method");
    	
    	//Fijar comportamiento esperado
    	Empresa e=new Empresa("","");
    	List<Empresa> testData=new ArrayList<Empresa>();
    	testData.add(e);
    	lookupDao.expects(once()).method("getEmpresas").withNoArguments().will(returnValue(testData));
    	
    	
    }
}
