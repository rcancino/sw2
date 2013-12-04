package com.luxsoft.siipap.dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

/**
 * 
 * @author Ruben Cancino
 *
 */
public class LocalidadesDaoImpl implements LocalidadesDao {
	
	 

	
	/* (non-Javadoc)
	 * @see com.luxsoft.siipap.dao.LocalidadesDao#getEstados()
	 */
	public List<String> getEstados(){
		final List<String> estados=new ArrayList<String>();
		try {
			final DefaultResourceLoader loader=new DefaultResourceLoader();
			Resource rs=loader.getResource("META-INF/estados.txt");
			Reader ri=new InputStreamReader(rs.getInputStream());
			BufferedReader reader=new BufferedReader(ri);
			String edo=null;
			do{
				edo=reader.readLine();
				if(edo!=null)
					estados.add(edo.trim());				
			}while(edo!=null);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return estados;
		
	}
	

}
