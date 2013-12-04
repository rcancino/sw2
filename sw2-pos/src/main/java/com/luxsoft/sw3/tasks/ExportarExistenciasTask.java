package com.luxsoft.sw3.tasks;

import java.util.List;
import java.util.Map;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.replica.aop.ExportadorManager;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Exporta las existencias a SIIPAP DBF
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ExportarExistenciasTask {
	
	
	
	public void exportarConExisLog(long sucursalId,int year,int mes){
		String db=POSDBUtils.getAplicationDB_URL();
		if(!db.endsWith("produccion")){
			throw new RuntimeException("Generacion de archivos apuntando a base de datos incorrecta: "+db);
		}	
		String sql="select * from sx_existencias_log where SUCURSAL_ID=? and year=? and mes=?";
		Object[] args={sucursalId,year,mes};
		List<Map<String,Object>> rows=Services.getInstance()
			.getJdbcTemplate().queryForList(sql,args);
		for(Map<String,Object> row:rows){
			try {
				String id=(String)row.get("INVENTARIO_ID");
				Number cantidad=(Number)row.get("CANTIDAD");
				actualizarExistencia(id,cantidad.doubleValue());
				//eliminarLog(id);
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		int deleted=Services.getInstance().getJdbcTemplate().update("delete from sx_existencias_log");
		System.out.println("Eliminadas: "+deleted);
	}
	
	public void execute(){
		String sql="select * from sx_existencias where SUCURSAL_ID=5 and year=2010 and mes=1";
		List<Map<String,Object>> rows=Services.getInstance().getJdbcTemplate().queryForList(sql);
		for(Map<String,Object> row:rows){
			try {
				String id=(String)row.get("INVENTARIO_ID");
				Number cantidad=(Number)row.get("CANTIDAD");
				actualizarExistencia(id,cantidad.doubleValue());
				//eliminarLog(id);
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private void actualizarExistencia(String id,double cantidad){
		System.out.println("Actualizando :"+id+ "  Cantidad: "+cantidad);
		Existencia exis=(Existencia)Services.getInstance().getUniversalDao().get(Existencia.class, id);
		exis.setCantidad(cantidad);
		ExportadorManager.getInstance().exportarExistencia(exis, null);
	}
	
	

	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		ExportarExistenciasTask task=new ExportarExistenciasTask();
		task.execute();
		//task.exportarConExisLog(3l, 2010, 1);
		
	}

}
