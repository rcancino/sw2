package com.luxsoft.sw2.replica.parches;

import java.util.List;
import java.util.Map;

import net.sf.cglib.core.Predicate;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw2.replica.valida.ConnectionServices;

public class ReclasificacionDeProductos {
	
	public void run(){
		String sql="SELECT CLAVE,PRODUCTO_ID FROM SX_PRODUCTOS where year(modificado)=2012 order by CLAVE desc";
		List<Map<String,Object>> produtosOficinas=ServiceLocator2.getJdbcTemplate().queryForList(sql);
		System.out.println("Productos: "+produtosOficinas.size());
		Long[] sucursales={2L,3L,5L,6L,9L};
		for(Long sucursalId:sucursales){
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			for(final Map<String,Object> row: produtosOficinas){
				List<Map<String,Object>> list=template.queryForList("SELECT CLAVE,PRODUCTO_ID FROM SX_PRODUCTOS WHERE CLAVE=?",new Object[]{row.get("CLAVE")});
				if(list.isEmpty()){
					System.out.println("Error producto : "+row.get("CLAVE")+" En sucursal: "+sucursalId);
					continue;
				}
				Map<String,Object> found=(Map<String,Object>)list.get(0);
				if(!found.get("CLAVE").equals(row.get("CLAVE"))){
					System.out.println("PRoducto con id incorrecto: "+found + " Debe ser: "+row+ " Sucursal: "+sucursalId);
				}
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		new ReclasificacionDeProductos().run();
	}

}
