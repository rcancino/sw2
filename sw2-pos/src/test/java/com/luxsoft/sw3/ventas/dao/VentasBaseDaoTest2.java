package com.luxsoft.sw3.ventas.dao;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.sql.DataSource;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.SessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import static junit.framework.Assert.*;
/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@ContextConfiguration(locations={
		"classpath:spring/sw3-ctx-db.xml"
		,"classpath:spring/sw3-applicationContext.xml"
		})
public class VentasBaseDaoTest2 extends AbstractTransactionalJUnit4SpringContextTests{
	
	/**
     * Log variable for all child classes. Uses LogFactory.getLog(getClass()) from Commons Logging
     */
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * ResourceBundle loaded from src/test/resources/${package.name}/ClassName.properties (if exists)
     */
    protected ResourceBundle rb;

   
    /**
     * Default constructor - populates "rb" variable if properties file exists for the class in
     * src/test/resources.
     */
    public VentasBaseDaoTest2() {
        // Since a ResourceBundle is not required for each class, just
        // do a simple check to see if one exists
        String className = this.getClass().getName();

        try {
            rb = ResourceBundle.getBundle(className);
        } catch (MissingResourceException mre) {
            //log.warn("No resource bundle found for: " + className);
        }
    }

    /**
     * Utility method to populate a javabean-style object with values
     * from a Properties file
     * @param obj the model object to populate
     * @return Object populated object
     * @throws Exception if BeanUtils fails to copy properly
     */
    protected Object populate(Object obj) throws Exception {
        // loop through all the beans methods and set its properties from its .properties file
        Map<String, String> map = new HashMap<String, String>();

        for (Enumeration<String> keys = rb.getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            map.put(key, rb.getString(key));
        }

        BeanUtils.copyProperties(map, obj);

        return obj;
    }

    /**
     * Create a HibernateTemplate from the SessionFactory and call flush() and clear() on it.
     * Designed to be used after "save" methods in tests: http://issues.appfuse.org/browse/APF-178.
     */
    protected void flush() {
        HibernateTemplate hibernateTemplate =
                new HibernateTemplate((SessionFactory) applicationContext.getBean("sessionFactory"));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }
    
    /**
	 * Inserta informacion en una o mas tablas con ayuda de DBUnit
	 * Util para perarar el estado incial de la base de datos antes de los tests
	 * 
	 * @param path
	 */
	protected void insertDataSet(final String path) throws Exception{
		Resource rs=applicationContext.getResource(path);
		assertTrue("Para insertar los datos debe existir el archivo: "+path,rs.exists());
		logger.info("Insertando entradas de prueba");
		
		Connection con=dataSource.getConnection();
		try {
			IDatabaseConnection icon=new DatabaseConnection(con);			
			DatabaseOperation.INSERT.execute(icon, new FlatXmlDataSet(rs.getInputStream()));
		} finally{
			DataSourceUtils.releaseConnection(con,dataSource);
			logger.info("Finished inserting data");
		}
	}
	
	public String toStringBean(Object bean){
		String m=ToStringBuilder.reflectionToString(bean, ToStringStyle.SHORT_PREFIX_STYLE);
		return m;
	}
	
	@Autowired
	protected DataSource dataSource;

}
