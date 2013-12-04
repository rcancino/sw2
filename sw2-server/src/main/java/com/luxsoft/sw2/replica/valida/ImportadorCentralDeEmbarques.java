package com.luxsoft.sw2.replica.valida;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.EntregaDet;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeEmbarques implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	SimpleJdbcInsert insertPartidasDet;
	
	public ImportadorCentralDeEmbarques(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_EMBARQUES");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_ENTREGAS");
		insertPartidasDet=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_ENTREGAS_DET");
	}
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		actualizarRetorno(template, sucursalId, fecha);
		
		String sql="select EMBARQUE_ID from SX_EMBARQUES where date(modificado)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Embarque.class, id);
				if(found==null)
					faltantes.add(id);
				if(found!=null){
					try{
						Embarque registroEnOficina= (Embarque) found;
						Date modificadoOficina=registroEnOficina.getLog().getModificado();
						
						String sql1="select * from SX_EMBARQUES where EMBARQUE_ID= ?";
						String[] args={id};
					    Map<String,Object> registroEnSucursal=template.queryForMap(sql1,args );
						Date modificadoSucursal=(Date)registroEnSucursal.get("MODIFICADO");
						
						int res=modificadoSucursal.compareTo(modificadoOficina);
						
						if(res>0){
							// El de la sucursal es mas nuevo asi que insertamos en oficinas
							boolean modificable=true;
							for(Entrega det:registroEnOficina.getPartidas()){
								if(det.getFechaComision()!=null){
									modificable=false;
									break;
								}
							}
							if(registroEnOficina!=null && modificable){
								Date regreso=(Date)registroEnSucursal.get("REGRESO");
								boolean reg=true;
								if(registroEnOficina.getRegreso()!=null)
									reg=!registroEnOficina.getRegreso().equals(regreso);
									
								String chofer=(String)registroEnSucursal.get("CHOFER");
								BigDecimal valor=(BigDecimal)registroEnSucursal.get("VALOR");
								Long transporte=(Long)registroEnSucursal.get("TRANSPORTE_ID");
								if( reg ||registroEnOficina.getChofer().equalsIgnoreCase(chofer) || !registroEnOficina.getValor().equals(valor) || !registroEnOficina.getTransporte().getId().equals(transporte)){
									//Borrar todo el embarque
									JdbcTemplate t=ServiceLocator2.getJdbcTemplate();
									t.update("DELETE FROM SX_ENTREGAS_DET WHERE ENTREGA_ID IN(SELECT ENTREGA_ID FROM SX_ENTREGAS WHERE EMBARQUE_ID=?)", new Object[]{registroEnOficina.getId()});
									t.update("DELETE FROM SX_ENTREGAS WHERE EMBARQUE_ID=?", new Object[]{registroEnOficina.getId()});
									t.update("DELETE FROM SX_EMBARQUES WHERE EMBARQUE_ID=?", new Object[]{registroEnOficina.getId()});
									
									//Insertar el maestro
									insert.execute(registroEnSucursal);
									List<Map<String,Object>> partidas=template.queryForList("SELECT * FROM SX_ENTREGAS WHERE EMBARQUE_ID=?",new Object[]{registroEnOficina.getId()});
									insertPartidas.executeBatch(partidas.toArray(new Map[0]));
									List<Map<String,Object>> partidasDet=template.queryForList("SELECT * FROM SX_ENTREGAS_DET WHERE ENTREGA_ID IN(SELECT ENTREGA_ID FROM SX_ENTREGAS WHERE EMBARQUE_ID=?)",new Object[]{registroEnOficina.getId()});
									insertPartidasDet.executeBatch(partidasDet.toArray(new Map[0]));
									
								}
								logger.info("Embarque actualizado: "+registroEnSucursal);
							}
							
							
						}else if(res<0){
							//El de las oficinas es mas nuevo asi que insertamos en sucursales
							
						}
					}catch (Exception e) {
						logger.error("Error actualizando embarque: "+ id +" Causa: "+ExceptionUtils.getRootCauseMessage(e));
					}
					
					
				
					
				}
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_EMBARQUES where EMBARQUE_ID=?", new Object[]{id});
				
				pendientes.add(row);
			}
			int[] res=insert.executeBatch(pendientes.toArray(new Map[0]));
			logger.info("Insertados: "+res.length);
			importarPartidasFaltantes(fecha, template, sucursalId);
			
					
		}		
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select ENTREGA_ID from SX_ENTREGAS where date(creado)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Entrega.class, id);
				if(found==null)
					faltantes.add(id);
			
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_ENTREGAS where ENTREGA_ID=?", new Object[]{id});
				pendientes.add(row);
			}
			int[] res=insertPartidas.executeBatch(pendientes.toArray(new Map[0]));
			logger.info(" Partidas Insertados: "+res.length);
			importarBorrados(rows,  template, sucursalId);
			
		}		
	}
	
	public void importarBorrados(List<String> entregas,JdbcTemplate template,Long sucursalId){
		
		for(String ent:entregas){
			String sql="select * from SX_ENTREGAS_DET where ENTREGA_ID=?";
			List<Map<String, Object>>  rows=template.queryForList(sql, new Object[]{ent});
			for(Map<String,Object> row:rows){
				try {
					int res=insertPartidasDet.execute(row);
					if(res>0)
						logger.info("Registro insertado: "+row);
				} catch (Exception e) {
					logger.error("Error insertando registro causa: "+ExceptionUtils.getRootCauseMessage(e));
				}
			}			
		}	
	}
	
	

	
	
	public void actualizarRetorno(JdbcTemplate template,Long sucursalId,Date fecha){
		String sql="select * from SX_EMBARQUES where date(modificado)=?";
		List<Map<String,Object>> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha));
		logger.info("Registros detectados: "+rows.size());
		for(Map<String,Object> row:rows){
			try {
				String id=(String)row.get("EMBARQUE_ID");
				Embarque e=(Embarque)ServiceLocator2.getHibernateTemplate().get(Embarque.class, id);
				boolean modificable=true;
				for(Entrega det:e.getPartidas()){
					if(det.getFechaComision()!=null){
						modificable=false;
						break;
					}
				}
				if(e!=null && modificable){
					Date regreso=(Date)row.get("REGRESO");
					boolean reg=true;
					if(e.getRegreso()!=null)
						reg=!e.getRegreso().equals(regreso);
						
					String chofer=(String)row.get("CHOFER");
					BigDecimal valor=(BigDecimal)row.get("VALOR");
					Long transporte=(Long)row.get("TRANSPORTE_ID");
					if( reg ||e.getChofer().equalsIgnoreCase(chofer) || !e.getValor().equals(valor) || !e.getTransporte().getId().equals(transporte)){
						//Borrar todo el embarque
						JdbcTemplate t=ServiceLocator2.getJdbcTemplate();
						t.update("DELETE FROM SX_ENTREGAS_DET WHERE ENTREGA_ID IN(SELECT ENTREGA_ID FROM SX_ENTREGAS WHERE EMBARQUE_ID=?)", new Object[]{e.getId()});
						t.update("DELETE FROM SX_ENTREGAS WHERE EMBARQUE_ID=?", new Object[]{e.getId()});
						t.update("DELETE FROM SX_EMBARQUES WHERE EMBARQUE_ID=?", new Object[]{e.getId()});
						
						//Insertar el maestro
						insert.execute(row);
						List<Map<String,Object>> partidas=template.queryForList("SELECT * FROM SX_ENTREGAS WHERE EMBARQUE_ID=?",new Object[]{e.getId()});
						insertPartidas.executeBatch(partidas.toArray(new Map[0]));
						List<Map<String,Object>> partidasDet=template.queryForList("SELECT * FROM SX_ENTREGAS_DET WHERE ENTREGA_ID IN(SELECT ENTREGA_ID FROM SX_ENTREGAS WHERE EMBARQUE_ID=?)",new Object[]{e.getId()});
						insertPartidasDet.executeBatch(partidasDet.toArray(new Map[0]));
						
					}
					logger.info("Embarque actualizado: "+row);
				}
			} catch (Exception e) {
				logger.error("Error actualizando embarque:  "+row+ " Causa: "+ExceptionUtils.getRootCauseMessage(e));
			}
			
		}
	}
	
	public static void main(String[] args) {
		Long[] sucursales ={9l};
		Date fecha=(DateUtil.toDate("28/09/2013"));
		ImportadorCentralDeEmbarques imp=new ImportadorCentralDeEmbarques();
		for(Long suc:sucursales){
			JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(suc);
			
			imp.importarFaltantes(fecha,template,suc);
		}
	}

}
