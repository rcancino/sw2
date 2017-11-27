package com.luxsoft.sw2.replica.valida;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import com.luxsoft.siipap.service.ServiceLocator2;


public class SincronizadorDeClientesCredito {
	


	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public SincronizadorDeClientesCredito(){
		   
	}

	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public SincronizadorDeClientesCredito addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}


	
	public void actualizarClientesCredito(){
		for(Long sucursalId:sucursales){
		System.out.println("Sincronizando clientes credito para "+ sucursalId);
			actualizarClientesCredito(sucursalId );
			
		}
	}

	public void actualizarClientesCredito(Long sucursalId){
		
	
		JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);

		String sqlSucursales="SELECT NOMBRE FROM SW_SUCURSALES WHERE SUCURSAL_ID  IN (2,3,5,6,9) ";
		
		List<Map<String,Object>> sucs=ServiceLocator2.getJdbcTemplate().queryForList(sqlSucursales);
		



		String sql="SELECT CREDITO_ID,CLAVE,LINEA,SALDO,ATRASO_MAX FROM SX_CLIENTES_CREDITO";
	
		List<Map<String,Object>> rows=template.queryForList(sql);
	
		for(Map<String,Object> row:rows){
			try {
				Long cliente=(Long)row.get("CREDITO_ID");
				String clave=(String)row.get("CLAVE");
				BigDecimal linea=(BigDecimal)row.get("LINEA");
				BigDecimal saldo=(BigDecimal)row.get("SALDO");
				Integer atraso=(Integer)row.get("ATRASO_MAX");
				
				
				String sqlLocal="SELECT CREDITO_ID,CLAVE,LINEA,SALDO,ATRASO_MAX FROM SX_CLIENTES_CREDITO WHERE CREDITO_ID=?";
				Object[] argumentos=new Object[]{cliente};

				
				Map<String,Object> rowOfi=ServiceLocator2.getJdbcTemplate().queryForMap(sqlLocal, argumentos);
				
				Long clienteOfi=(Long)rowOfi.get("CREDITO_ID");
				String claveOfi=(String)rowOfi.get("CLAVE");
				BigDecimal lineaOfi=(BigDecimal)rowOfi.get("LINEA");
				BigDecimal saldoOfi=(BigDecimal)rowOfi.get("SALDO");
				Integer atrasoOfi=(Integer)rowOfi.get("ATRASO_MAX");
				
					
					if(!linea.equals(lineaOfi) || !saldo.equals(saldoOfi) || !atraso.equals(atrasoOfi)) {					
						System.out.println("Actualizando Cliente:  " +claveOfi);
						
					
						
					
						for(Map<String,Object> sucursal:sucs){

							String sucur=((String)sucursal.get("NOMBRE"));
							
							sucur=sucur=="CALLE4" ? "CALLE 4" : sucur;

							String inserAuditUp="INSERT INTO AUDIT_LOG (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)" +
									"  VALUES (?,\'ClienteCredito\',\'UPDATE\',\'SX_CLIENTES_CREDITO\',\'10.10.1.9\',\'OFICINAS\',?,now(),now(),null,\'SincronizadorDeClientesCredito\',0)";
							Object[] argsAuditUp=new Object[]{cliente,sucur};
							ServiceLocator2.getJdbcTemplate().update(inserAuditUp, argsAuditUp);
							
						}

					}
				
			} catch (Exception e) {
				System.out.println("Error importando exis: "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
				e.printStackTrace();
			}

		}
		//exportarFaltantes(rows, fecha, sucursalId);

	}
	
	
	
	
	
	public static void main(String[] args) {
		new SincronizadorDeClientesCredito()
		.addSucursal(2L,3L,5L,6L,9L).actualizarClientesCredito();
		//.addSucursal(2L).actualizarClientesCredito();

	}
	

}
