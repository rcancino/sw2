package com.luxsoft.sw2.replica.valida;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import com.luxsoft.siipap.service.ServiceLocator2;


public class SincronizadorDeProductos {
	


	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public SincronizadorDeProductos(){
		   
	}

	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public SincronizadorDeProductos addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}


	
	public void actualizarProductos(){
		for(Long sucursalId:sucursales){
		System.out.println("Sincronizando Productos "+ sucursalId);
			actualizarProductos(sucursalId );
			
		}
	}

	public void actualizarProductos(Long sucursalId){
		
	
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);

		String sqlSucursales="SELECT NOMBRE FROM SW_SUCURSALES WHERE SUCURSAL_ID  IN (2,3,5,6,9,11,14) ";
		
		List<Map<String,Object>> sucs=ServiceLocator2.getJdbcTemplate().queryForList(sqlSucursales);
		



		String sql="SELECT PRODUCTO_ID,CLAVE,DESCRIPCION,PRECIOCONTADO,PRECIOCREDITO,ACTIVO FROM SX_PRODUCTOS";
	
		List<Map<String,Object>> rows=template.queryForList(sql);
	
		for(Map<String,Object> row:rows){
			try {
				Long producto=(Long)row.get("PRODUCTO_ID");
				String clave=(String)row.get("CLAVE");
				String descripcion=(String)row.get("DESCRIPCION");
				Double precioCon=(Double)row.get("PRECIOCONTADO");
				Double precioCre=(Double)row.get("PRECIOCREDITO");
				Boolean activo=(Boolean)row.get("ACTIVO");
				
				
				String sqlLocal="SELECT PRODUCTO_ID,CLAVE,DESCRIPCION,PRECIOCONTADO,PRECIOCREDITO,ACTIVO FROM SX_PRODUCTOS WHERE PRODUCTO_ID=?";
				Object[] argumentos=new Object[]{producto};

				
				Map<String,Object> rowOfi=ServiceLocator2.getJdbcTemplate().queryForMap(sqlLocal, argumentos);
				
				Long productoOfi=(Long)rowOfi.get("PRODUCTO_ID");
				String claveOfi=(String)rowOfi.get("CLAVE");
				String descripcionOfi=(String)rowOfi.get("DESCRIPCION");
				Double precioConOfi=(Double)rowOfi.get("PRECIOCONTADO");
				Double precioCreOfi=(Double)rowOfi.get("PRECIOCREDITO");
				Boolean activoOfi=(Boolean)rowOfi.get("ACTIVO");
				
				//System.out.println("Sincronizando el producto "+ claveOfi+" "+descripcionOfi+" "+precioConOfi+" "+precioCreOfi+" - - - - - " +clave+" "+descripcion+" "+precioCon+" "+precioCre +"---**--"+activo+"**"+activoOfi);
					
					if(!descripcion.equals(descripcionOfi) || !precioCon.equals(precioConOfi) || !precioCre.equals(precioCreOfi) || !activo.equals(activoOfi)) {					
						System.out.println("Actualizando Producto:  " +claveOfi );
						//System.out.println("Sincronizando el producto "+ claveOfi+" "+descripcionOfi+" "+precioConOfi+" "+precioCreOfi+" - - - - - " +clave+" "+descripcion+" "+precioCon+" "+precioCre +"---**--"+activo+"**"+activoOfi);
	
						for(Map<String,Object> sucursal:sucs){

							String sucur=((String)sucursal.get("NOMBRE"));
							
							sucur=sucur.equals("CALLE4") ? "CALLE 4" : sucur;

							String inserAuditUp="INSERT INTO AUDIT_LOG (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)" +
									"  VALUES (?,\'Producto\',\'UPDATE\',\'SX_PRODUCTOS\',\'10.10.1.227\',\'OFICINAS\',?,now(),now(),null,\'SincronizadorDeProductos\',0)";
							Object[] argsAuditUp=new Object[]{producto,sucur};
							ServiceLocator2.getJdbcTemplate().update(inserAuditUp, argsAuditUp);
							
						}

					}
				
			} catch (Exception e) {
				//System.out.println("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
				e.printStackTrace();
			}

		}
		//exportarFaltantes(rows, fecha, sucursalId);

	}
	
	
	
	
	
	public static void main(String[] args) {
		new SincronizadorDeProductos()
		.addSucursal(2L,3L,5L,6L,9L,11L,14L).actualizarProductos();
		//.addSucursal(2L).actualizarProductos();
		//
		
	}
	

}
