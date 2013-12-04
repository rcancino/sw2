package com.luxsoft.sw3.ventas.dao;

import com.luxsoft.siipap.dao.BaseDaoTestCase;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentasBaseDaoTest extends BaseDaoTestCase{
	
	
	/**
     * Sets AutowireMode to AUTOWIRE_BY_NAME and configures all context files needed to tests DAOs.
     * @return String array of Spring context files.
     */
    protected String[] getConfigLocations() {
        setAutowireMode(AUTOWIRE_BY_NAME);
        return new String[] {
                "classpath:spring/sw3-ctx-db.xml" //For DAOS
                ,"classpath*:spring/sw3-applicationContext.xml" // for modular projects
            };
    }

}
