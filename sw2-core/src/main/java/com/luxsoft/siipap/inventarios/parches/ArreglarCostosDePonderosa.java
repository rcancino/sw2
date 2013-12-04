package com.luxsoft.siipap.inventarios.parches;

import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.springframework.jdbc.core.SqlParameterValue;

import com.luxsoft.siipap.inventarios.dao.CostoPromedioDao;
import com.luxsoft.siipap.inventarios.service.CostoPromedioManagerImpl;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Parche para arreglar del costo promedio de productos comprados a ponderosa
 * incorporando el costo del flete 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ArreglarCostosDePonderosa {
	
	
	public void execute() throws ParseException{
		String sql="SELECT CLAVE FROM sx_inventario_com I " +
				" WHERE I.FECHA>? AND I.PROVEEDOR_ID=140 GROUP BY CLAVE";
		SqlParameterValue p1=new SqlParameterValue(Types.DATE
				,new SimpleDateFormat("yyyy/MM/dd").parseObject("2008/12/31"));		
		Object[] args=new SqlParameterValue[]{p1};
		List<String> claves=ServiceLocator2.getJdbcTemplate().queryForList(sql,args,String.class);
		CostoPromedioDao dao=(CostoPromedioDao)ServiceLocator2.instance().getContext().getBean("costoPromedioDao");
		CostoPromedioManagerImpl cm=new CostoPromedioManagerImpl(dao);
		cm.setHibernateTemplate(ServiceLocator2.getHibernateTemplate());
		/*for(String clave:claves){
			for(int i=1;i<10;i++){
				System.out.println("Procesando clave: "+clave);
				ServiceLocator2.getCostosServices().actualizarCostosPromedio(clave, 2009, i);
			}
			cm.backwardCosto(2009,clave);
		}*/
		for(String clave:claves){
			for(int i=1;i<10;i++){
				System.out.println("Procesando clave: "+clave);				
				ServiceLocator2.getCostosServices().actualizarMovimientosPromedio(clave, 2009, i);
			}
		}
		System.out.println("Total de claves: "+claves.size());
	}
	
	public static void main(String[] args) throws ParseException {
		new ArreglarCostosDePonderosa().execute();
	}

}
