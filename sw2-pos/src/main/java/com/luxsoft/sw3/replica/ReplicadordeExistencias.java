package com.luxsoft.sw3.replica;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.services.Services;

/**
 * Replicador de existencias q divide su funcionalidad en 2 partes
 *  
 *  Importaciones
 *  
 *  Exportaciones
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class ReplicadordeExistencias {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private Set<Long> sucursales=new HashSet<Long>();
	
	/**
	 * Importa las existencias registradas como pendientes en la bitacora de cada sucursal
	 * (SX_EXISTENCIAS_LOG)
	 */
	public synchronized void importar(){		
		
		for(Long sucursalId:getSucursales()){			
			try {
				importar(sucursalId);
			} catch (Exception e) {
				logger.info("Imposible importar existencias de la sucursal: "+sucursalId,e);
			}
			
			
		}
	}
	
	/**
	 * Importa las exsitencias pendientes de la sucursal indicada
	 * 
	 * Las pendientes son las registradas en la tabla de bitacoras SX_EXISTENCIAS_LOG
	 * 
	 * @param sucursalId
	 */
	public synchronized void importar(Long sucursalId){
		
		Date hoy=new Date();
		int year=Periodo.obtenerYear(hoy);
		int mes=Periodo.obtenerMes(hoy)+1;
		String sql="SELECT * FROM sx_existencias_log " +
		"WHERE YEAR=? " +
		"  AND MES=? " +
		"  AND SUCURSAL_ID=? " +
		"  AND TX_IMPORTADO IS NULL";  //Solo las pendientes
		JdbcTemplate source=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
		List<Map<String,Object>> rows=source.queryForList(sql,new Object[]{year,mes,sucursalId});
		if(rows.size()>0)
			logger.info("Importando "+rows.size()+ " existencias pendientes de la sucursal: "+sucursalId);
		for(Map<String,Object> row:rows){
			Existencia res=importarRegistro(row,true);
			if(res!=null)
				registrarImportacion(row); //Notificamos q ha sido importado
			System.out.print(".");
		}
	}
	
	/**
	 * Importa todas las existencias desde las sucursales.
	 * 
	 * Solo importa la cantidad existente no el Bean Existencia
	 * 
	 */
	public synchronized void importarTodas(){
		for(Long sucursalId:getSucursales()){
			importarTodas(sucursalId);
		}
	}
	
	public synchronized void importarTodas(int year, int mes){
		for(Long sucursalId:getSucursales()){
			importarTodas(sucursalId,year,mes);
		}
	}
	
	/**
	 * Importa las existencias de todos los productos para cada sucursal
	 * 
	 */
	public synchronized void importarTodas(Long sucursalId){
		try {
			logger.info("Importando todas las existencias de la sucursal: "+sucursalId);
			Date hoy=new Date();
			int year=Periodo.obtenerYear(hoy);
			int mes=Periodo.obtenerMes(hoy)+1;
			String sql="SELECT * FROM sx_existencias WHERE YEAR=? AND MES=? AND SUCURSAL_ID=?";
			JdbcTemplate source=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			List<Map<String,Object>> rows=source.queryForList(sql,new Object[]{year,mes,sucursalId});		
			for(Map<String, Object> row:rows){
				importarRegistro(row,false);
				System.out.print(".");
			}
			logger.info("\nExistencias importadas de la sucursal: "+sucursalId);
		} catch (Exception e) {
			logger.error("Imposible importar TODAS las existencias de la sucursal: "+sucursalId,e);
		}
		
	}
	
	/**
	 * Importa las existencias de todos los productos para cada sucursal
	 * 
	 */
	public synchronized void importarTodas(Long sucursalId,int year,int mes){
		try {
			logger.info("Importando todas las existencias de la sucursal: "+sucursalId);
			String sql="SELECT * FROM sx_existencias WHERE YEAR=? AND MES=? AND SUCURSAL_ID=?";
			JdbcTemplate source=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			List<Map<String,Object>> rows=source.queryForList(sql,new Object[]{year,mes,sucursalId});		
			for(Map<String, Object> row:rows){
				importarRegistro(row,false);
				System.out.print(".");
			}
			logger.info("\nExistencias importadas de la sucursal: "+sucursalId);
		} catch (Exception e) {
			logger.error("Imposible importar TODAS las existencias de la sucursal: "+sucursalId,e);
		}
		
	}
	
	
	/**
	 * Importa la existencia desde un registro de la tabla SX_EXISTENCIAS
	 * 
	 * @param row
	 * @return El bean de existencia si se importaron los datos exitosamente de lo contrario nulo
	 */
	private synchronized Existencia importarRegistro(Map<String,Object> row,boolean delLog){
		try {
			String columnaCantidad;
			if(delLog)
				columnaCantidad="EXISTENCIA";
			else
				columnaCantidad="CANTIDAD";
			//Sacamos las propiedades requeridas
			String producto=(String)row.get("CLAVE");
			Number cantidad=(Number)row.get(columnaCantidad);
			Integer year=(Integer)row.get("YEAR");
			Integer mes=(Integer)row.get("MES");
			Long sucursalId=(Long)row.get("SUCURSAL_ID");	
			String inventarioId=(String)row.get("INVENTARIO_ID");
			Number recorte=(Number)row.get("RECORTE");
			
			Existencia e=Services.getInstance().getExistenciasDao().buscar(producto, sucursalId, year, mes);
			if(e==null){
				logger.info("No existe la existencia para el producto: "+producto+ "  Generandola");
				//e=Services.getInstance().getExistenciasDao().generar(producto, sucursalId,year,mes);
				e=(Existencia)ReplicaServices.getInstance().getHibernateTemplate(sucursalId).get(Existencia.class, inventarioId);
				Services.getInstance().getHibernateTemplate().replicate(e, ReplicationMode.OVERWRITE);
				return e;
			}
			e.setCantidad(cantidad.doubleValue());
			e.setRecorte(recorte.doubleValue());
			e.setReplicado(null);
			e.setImportado(new Date());
			return Services.getInstance().getExistenciasDao().save(e);
		} catch (Exception e) {
			logger.error("Imposible importar el registro de existencia: "+row+" Error:"+ExceptionUtils.getRootCauseMessage(e));
			return null;
		}		
	}
	
	/**
	 * Actualiza el campo de importado en un registro de SX_EXISTENCIAS_LOG
	 * 
	 * @param row
	 */
	private synchronized void registrarImportacion(Map<String,Object> row){
		try {
			//Sacamos las propiedades requeridas
			Long id=(Long)row.get("ID");
			Long sucursalId=(Long)row.get("SUCURSAL_ID");			
			String update="UPDATE SX_EXISTENCIAS_LOG SET TX_IMPORTADO=NOW() WHERE ID=?";
			Object[] params={id};
			JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			template.update(update, params);
		} catch (Exception e) {
			logger.error("Imposible importar el registro de existencia: "+row);
		}		
	}
	
	/**** Exportadores ****************************************/
	
	public synchronized void replicar(){
		String sql="SELECT * FROM SX_EXISTENCIAS_LOG WHERE TX_REPLICADO IS NULL";
		List<Map<String,Object>> rows=Services.getInstance().getJdbcTemplate().queryForList(sql);
		if(rows.size()>0)
			logger.info("Replicando existencias "+rows.size()+"  pendientes de replicar");
		for(Map<String,Object> row:rows){
			boolean actualizar=true;
			try {
				//Sacamos las propiedades requeridas
				
				String producto=(String)row.get("CLAVE");
				Number cantidad=(Number)row.get("EXISTENCIA");
				Integer year=(Integer)row.get("YEAR");
				Integer mes=(Integer)row.get("MES");
				Long sucursalId=(Long)row.get("SUCURSAL_ID");	
				Number recorte=(Number)row.get("RECORTE");
				for(Long sucursal:getSucursales()){
					if(sucursal.equals(sucursalId))
						continue; //Excluimos la sucursal origen
					logger.debug("Replicando existencia a la sucursal: "+sucursal);
					try {
						String update="UPDATE SX_EXISTENCIAS SET CANTIDAD=?,RECORTE=?,TX_REPLICADO=NOW() WHERE YEAR=? AND MES=? AND SUCURSAL_ID=? AND CLAVE=?";
						Object[] params={cantidad.doubleValue(),recorte.doubleValue(),year,mes,sucursalId,producto};
						JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursal);
						int updated=template.update(update, params);
						if(updated==0){
							logger.info("No existe el registro de existencia en sucursal destino generando uno nuevo para : "+producto);
							Existencia e=Services.getInstance().getExistenciasDao().buscar(producto, sucursalId, year, mes);
							if(e!=null)
								ReplicaServices.getInstance().getHibernateTemplate(sucursal).replicate(e, ReplicationMode.OVERWRITE);
						}
					} catch (Exception e) {
						logger.error("Imposible replicar el registro: "+row+ "\n a la sucursal: "+sucursal,e);
						actualizar=false;
					}
				}
				
				//Si paso a todas las sucursales actualizamos el registro replicado
				if(actualizar){
					try {
						Number id=(Number)row.get("ID");
						String update="UPDATE SX_EXISTENCIAS_LOG SET TX_REPLICADO=NOW() WHERE ID=?";
						Object[] params={id};
						JdbcTemplate template=Services.getInstance().getJdbcTemplate();
						template.update(update, params);
					} catch (Exception e) {
						logger.error("Imposible actualizar la bitacora de replicado",e);
					}
				}
				
			} catch (Exception e) {
				logger.error("Imposible replicar el registro: "+row,e);
			}
			
		}
	}
	
	public synchronized void replicarTodas(){
		for(Long sucursalId:getSucursales()){
			try {
				replicarTodas(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible replicar todas las existencias de la sucursal: "+sucursalId);
			}
		}
	}
	
	public synchronized void replicarTodas(Long sucursalId){
		Date hoy=new Date();
		int year=Periodo.obtenerYear(hoy);
		int mes=Periodo.obtenerMes(hoy)+1;
		String sql="SELECT * FROM SX_EXISTENCIAS WHERE YEAR=? AND MES=? AND SUCURSAL_ID=?";
		List<Map<String,Object>> rows=Services.getInstance().getJdbcTemplate().queryForList(sql,new Object[]{year,mes,sucursalId});
		for(Long sucursal:getSucursales()){			
			if(sucursal.equals(sucursalId))
				continue; //Excluimos la sucursal origen
			logger.info("Replicando todas las existencia de la scursal "+sucursalId+ " a la sucursal: "+sucursal);
			JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursal);
			int contador=0;
			for(Map<String,Object> row:rows){					
				String producto=(String)row.get("CLAVE");
				Number cantidad=(Number)row.get("CANTIDAD");
				Number recorte=(Number)row.get("RECORTE");
				String update="UPDATE SX_EXISTENCIAS SET CANTIDAD=?,RECORTE=?,TX_REPLICADO=NOW() WHERE YEAR=? AND MES=? AND SUCURSAL_ID=? AND CLAVE=?";
				Object[] params={cantidad.doubleValue(),recorte.doubleValue(),year,mes,sucursalId,producto};				
				try {
					int updated=template.update(update, params);
					if(updated==0){
						logger.info("No existe el registro de existencia en sucursal destino generando uno nuevo para : "+producto);
						Existencia e=Services.getInstance().getExistenciasDao().buscar(producto, sucursal, year, mes);
						ReplicaServices.getInstance().getHibernateTemplate(sucursal).replicate(e, ReplicationMode.OVERWRITE);
					}
					contador++;
					System.out.println("Progress: "+contador+" de "+rows.size());
				} catch (Exception e) {
					logger.error("Imposible replicar el registro: "+row+ "\n a la sucursal: "+sucursal,e);
				}
			}	
		}			
	}
	
	/**
	 * Envia todas las existencias a las sucursales. Util para correr en forma
	 * esporadica para sincronizar las existencias en todas las sucursales
	 * se debe ejecutar CUANDO EL LA REPLICACION EN LINEA NO ESTE OPERANDO
	 * 
	 */
	public void enviarTodas(){
		Date hoy=new Date();
		int year=Periodo.obtenerYear(hoy);
		int mes=Periodo.obtenerMes(hoy)+1;
		for(Long sucursalId:getSucursales()){
			try {
				enviarExistencias(sucursalId, year, mes);
			} catch (Exception e) {
				logger.error("Imposible enviar existencias actualizadas a la sucursal: "+sucursalId
						+" \n Tipo: "+ExceptionUtils.getRootCauseMessage(e)
						,e);
			}
						
		}
	}
	
	public void enviarTodas(int year,int mes){
		for(Long sucursalId:getSucursales()){
			try {
				enviarExistencias(sucursalId, year, mes);
			} catch (Exception e) {
				logger.error("Imposible enviar existencias actualizadas a la sucursal: "+sucursalId
						+" \n "+ExceptionUtils.getRootCauseMessage(e)
						,e);
			}
						
		}
	}
	
	public void enviarExistencias(Long sucursalId,int year,int mes){		
		logger.info("Enviando registros de existencia a la sucursal: "+sucursalId+ " "+year+ " / "+mes );
		
		HibernateTemplate target=ReplicaServices
				.getInstance()
				.getHibernateTemplate(sucursalId);
		
		String hql="from Existencia e where e.year=? and e.mes=? and sucursal.id!=?";
	    List<Existencia> existencias=Services.getInstance().getHibernateTemplate().find(hql,new Object[]{year,mes,sucursalId});
		//String hql="from Existencia e where e.year=2011 and e.mes=3 and sucursal.id=3";
		//List<Existencia> existencias=Services.getInstance().getHibernateTemplate().find(hql);
		
		for(Existencia e:existencias){
			try {
				target.replicate(e, ReplicationMode.OVERWRITE);
				System.out.print(".");
			} catch (Exception e2) {
				logger.error("Error enviando existencia: "+e ,e2);
			}
		}		
		logger.info("\nExistencias replicadas(enviadas) a la sucursal_ "+sucursalId+ "Exis: "+existencias.size());
	}
	
	public void enviarExistencias(Long sucursalId,String clave,int year,int mes){		
		logger.info("Enviando registros de existencia a la sucursal: "+sucursalId+ "Clave: "+clave);
		
		HibernateTemplate target=ReplicaServices
				.getInstance()
				.getHibernateTemplate(sucursalId);
		
		String hql="from Existencia e where e.clave=? and e.year=? and e.mes=? and sucursal.id!=?";
		List<Existencia> existencias=Services.getInstance().getHibernateTemplate().find(hql,new Object[]{clave,year,mes,sucursalId});
		for(Existencia e:existencias){
			try {
				target.replicate(e, ReplicationMode.OVERWRITE);
				System.out.print(".");
			} catch (Exception e2) {
				logger.error("Error enviando existencia: "+e ,e2);
			}
		}		
		logger.info("\nExistencia replicada(enviada) a la sucursal_ "+sucursalId+ "Exis: "+existencias);
	}
	
	
	/**
	 * Elimina las existencias remotas de la sucursal indicada. Es decir todas las existencias de sucursales ajenas a la indicada
	 * 
	 * @param sucursalId
	 * @param year
	 * @param mes
	 */
	public void eliminarExistenciasRemotas(Long sucursalId,int year,int mes){
		logger.info("Eliminando los registros de existencias anteriores en la sucursal: "+sucursalId);
		try {
			JdbcTemplate jdbcTemplate=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			String sql="delete from sx_existencias where sucursal_id<>? and year=? and mes=?";
			Object args[]={sucursalId,year,mes};
			int deleted=jdbcTemplate.update(sql, args);
			logger.info("Registros eliminados="+deleted);
		} catch (Exception e) {
			logger.error("Error al eliminar existencias anteriores",e);
		}
	}
	
	
	public void generar_enviar_exisDeProductoNuevo(String clave,int year,int mes){
		for(Long sucursalId:getSucursales()){
			Existencia e=Services.getInstance().getExistenciasDao().generar(clave, sucursalId, year, mes);
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			target.replicate(e, ReplicationMode.IGNORE);
		}
	}
	
	
	public Set<Long> getSucursales() {
		return sucursales;
	}

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public void addSucursales(Long...longs){
		for(Long suc:longs){
			getSucursales().add(suc);
		}
	}
	
	public void limpiarLog(){
		for(Long sucursalId:getSucursales()){
			try {
				limpiarLog(sucursalId);
			} catch (Exception e) {
				logger.error("Imposible enviar existencias actualizadas a la sucursal: "+sucursalId,e);
			}			
		}
	}
	
	public void limpiarLog(Long sucursalId){
		logger.info("Eliminando log de existencias en la sucursal: "+sucursalId);
		try {
			JdbcTemplate jdbcTemplate=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			String sql="delete from sx_existencias_log";
			int deleted=jdbcTemplate.update(sql);
			logger.info("Registros eliminados="+deleted);
		} catch (Exception e) {
			logger.error("Error al eliminar log de existencias anteriores",e);
		}
		
		
	}

	public static void main(String[] args) {
	
		ReplicadordeExistencias replicador=new ReplicadordeExistencias();
		//ReplicadordeExistencias replicador=(ReplicadordeExistencias)ReplicaServices.getInstance().getContext().getBean("");
		replicador.addSucursales(2L,6L,5L,3L,9L);
		//replicador.addSucursales(3L);
		//replicador.addSucursales(5L);
//		replicador.importar();
//		replicador.replicar();
		//replicador.addSucursales(6l);
		//replicador.importarTodas();
		//replicador.enviarTodas();
		//replicador.importarTodas(2012,1);
		//replicador.enviarTodas(2012,4);
		//replicador.limpiarLog();
		//replicador.enviarExistencias(9L, 2011,8 );
		//replicador.enviarExistencias(3L, 2011, 3);
		//replicador.enviarExistencias(2L, 2010, 11);
		//replicador.enviarExistencias(6L, 2010, 11);
		//replicador.enviarExistencias(5L, 2010, 11);
		//replicador.enviarExistencias(3L,"CAP212024", 2010, 10);
		replicador.generar_enviar_exisDeProductoNuevo("BB66.7", 2013, 3);
		//replicador.importarTodas(6L);
	//replicador.importarTodas(3L);
		//replicador.replicarTodas(3L);
		;
		/*String[] claves={"CAP9018"};
		for(String clave:claves){
			//replicador.enviarExistencias(3L, 2010, 9);
			//replicador.generar_enviar_exisDeProductoNuevo(clave, 2010, 9);
		}*/
		//replicador.generar_enviar_exisDeProductoNuevo("CAP2A53424", 2010, 10);
	}
	

}
