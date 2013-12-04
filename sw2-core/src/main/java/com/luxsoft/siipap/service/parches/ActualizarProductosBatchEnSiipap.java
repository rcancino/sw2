package com.luxsoft.siipap.service.parches;

import java.util.List;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;

public class ActualizarProductosBatchEnSiipap {
	
	
	public void execute(){
		List<Producto> prods=ServiceLocator2.getUniversalDao().getAll(Producto.class);
		for(Producto p:prods){
			String update="UPDATE SW_ARTICULOS SET LINEA=? " +
					"WHERE CLAVE=?" ;
			ServiceLocator2.getAnalisisJdbcTemplate().setFetchSize(10);
			int res=ServiceLocator2.getAnalisisJdbcTemplate().update(update, new Object[]{p.getLinea().getNombre(),p.getClave()});
			System.out.println("Actualizado en Oracle: "+res+" Prod: "+p);
		}
		ServiceLocator2.close();
	}
	
	
	public static void main(String[] args) {
		new ActualizarProductosBatchEnSiipap().execute();
	}

}
