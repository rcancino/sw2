package com.luxsoft.sw3.replica.tasks;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.services.Services;


/**
 * Genera una tabla de respaldo para las existencias del mes
 * en cada una de las sucursales, y limpia la bitacora de existencias 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class RespaldoDeExistencias {
	
	private Set<Long> sucursales=new HashSet<Long>();
	private Logger logger=Logger.getLogger(getClass());
	
	public RespaldoDeExistencias addSucursales(Long...sucursal){
		for(Long suc:sucursal)
			sucursales.add(suc);
		return this;
	}
	
	/**
	 * Sincroniza las existencias de todas las sucursales.
	 *   a- Actualzia las existencias de cada sucursal en produccion
	 *   
	 *   
	 */
	public void execute(){
		Date fecha=Services.getInstance().obtenerFechaDelSistema();		
		for(Long sucursalId:sucursales){
			try {
				importar(sucursalId,fecha);
			} catch (Exception e) {
				logger.debug("Imposible importar existencias de la sucursal: "+sucursalId,e);
			}
		}
	}
	
	public void importar(Long sucursalId,Date fecha){
		int year=Periodo.obtenerYear(fecha);
		int mes=Periodo.obtenerMes(fecha)+1;
		JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
		String sql="select clave,cantidad from sx_existencias  where year=? and mes=? and sucursal_id=?";
		Object[] args=new Object[]{year,mes,sucursalId};
		List<Map<String,Object>> rows=template.queryForList(sql, args);
		for(Map<String, Object> row:rows){
			Number cantidad=(Number)row.get("CANTIDAD");
			String clave=(String)row.get("CLAVE");
			
			Existencia ex=Services.getInstance().getExistenciasDao().buscar(clave, sucursalId, year, mes);
			if(ex==null){
				System.out.println("No hay registro de existencia para: "+clave);
				ex=Services.getInstance().getExistenciasDao().generar(clave, fecha, sucursalId);
			}
			ex.setCantidad(cantidad.doubleValue());
			Services.getInstance().getExistenciasDao().save(ex);
			//System.out.println("Acutralizando: "+row);
		}
		System.out.println("Rows: "+rows.size());
	}

	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}

	public static void main(String[] args) {
		RespaldoDeExistencias task=new RespaldoDeExistencias();
		task.addSucursales(2L,3L,5L,6L)
		.execute();
	}
	

}
