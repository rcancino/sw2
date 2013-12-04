package com.luxsoft.sw2.replica.valida;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeSolicitudesDeDepositos implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	SimpleJdbcInsert insert;
	
	
	public ImportadorCentralDeSolicitudesDeDepositos(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_SOLICITUDES_DEPOSITO");
		
	}
	
	public void importarFaltantes(){
		importarFaltantes(new Periodo(new Date()));
	}
	
	public void importarFaltantes(String f1,String f2){
		importarFaltantes(new Periodo(f1,f2));
	}
	
	public void importarFaltantes(Periodo periodo){
		
		List<Date> dias=periodo.getListaDeDias();
		for(Date dia:dias){
			importarFaltates(dia);
		}
	}
	
	public void importarFaltates(Date dia){
		
		
		for(Long sucursalId:sucursales){
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
			importarFaltantes(dia,template,sucursalId);
		}
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		
		System.out.println("Importando Solicitudes de Deposito del dia " + fecha +" De la sucursal: " +sucursalId);
		String sql="select * from SX_SOLICITUDES_DEPOSITO where date(modificado)=?";
		List<Map<String,Object>> registrosEnSucursal=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		List<Map<String,Object>> registrosEnOficinas=ServiceLocator2.getJdbcTemplate().queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		SimpleJdbcInsert sucursalInsert=new SimpleJdbcInsert(template).withTableName("SX_SOLICITUDES_DEPOSITO");
		for(final Map<String,Object> solSucursal:registrosEnSucursal){
			//Date modificadoOficina=(Date)solSucursal.get("MODIFICADO");
			Date modificadoSucursal=(Date)solSucursal.get("MODIFICADO");
			final String solId=(String)solSucursal.get("SOL_ID");
			Map<String,Object> solOficinas=(Map<String,Object>)CollectionUtils.find(registrosEnOficinas, new Predicate() {
				public boolean evaluate(Object object) {
					Map<String,Object> sol=(Map<String,Object>)object;
					String id=(String)sol.get("SOL_ID");
					return id.equals(solId);
				}
			});
			if(solOficinas!=null){
				try {
					//Date modificadoSucursal=(Date)solOficinas.get("MODIFICADO");
					Date modificadoOficina=(Date)solOficinas.get("MODIFICADO");
					int res=modificadoSucursal.compareTo(modificadoOficina);
					if(res>0){
						// El de la sucursal es mas nuevo asi que insertamos en oficinas
						String abonoId=(String)solOficinas.get("ABONO_ID");
						if(abonoId==null){
							ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_SOLICITUDES_DEPOSITO WHERE SOL_ID=?", new Object[]{solId});
							insert.execute(solSucursal);
						}
					}else if(res<0){
						//El de las oficinas es mas nuevo asi que insertamos en sucursales
						template.update("DELETE FROM SX_SOLICITUDES_DEPOSITO WHERE SOL_ID=?", new Object[]{solId});
						sucursalInsert.execute(solOficinas);
					}
				} catch (Exception e) {
					logger.error("Error insertando/borrando registro causa: "+ExceptionUtils.getRootCauseMessage(e));
				}				
			}else{
				// Sol faltante en oficinas insertnadolo
				try {
					int res=insert.execute(solSucursal);
					if(res>0)
						logger.info("Sol nuevo importado: "+solSucursal);
				} catch (Exception e) {
					logger.error("Error insertando registro: "+solSucursal+"  causa: "+ExceptionUtils.getRootCauseMessage(e));
				}
				
			}
		}
	}

	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public ImportadorCentralDeSolicitudesDeDepositos addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}
	
	public static void main(String[] args) {
		new ImportadorCentralDeSolicitudesDeDepositos().addSucursal(2L,3L,5L,6L,9L,11L).importarFaltantes();
	}

}
