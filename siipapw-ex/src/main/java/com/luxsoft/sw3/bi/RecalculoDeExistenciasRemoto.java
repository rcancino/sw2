package com.luxsoft.sw3.bi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.neo4j.cypher.internal.compiler.v2_1.perty.docbuilders.toStringDocBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.SQLUtils;
import com.luxsoft.utils.LoggerHelper;

public class RecalculoDeExistenciasRemoto {
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	private boolean todo=false;
	
	protected Set<Long> sucursales=new HashSet<Long>();
	
	
	public RecalculoDeExistenciasRemoto(){
		//insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_EXISTENCIAS");
		
		   
	}
	

	

 public void recalcularExistencias(){
	    final Date fecha=new Date();
		final int mes=Periodo.obtenerMes(fecha)+1;
	    
	    /**Para Arreglar discrepancias del mes anterior**/
	  //  final int mes=Periodo.obtenerMes(fecha);
		
	    final int year=Periodo.obtenerYear(fecha);
		//final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		final Periodo per=Periodo.getPeriodoEnUnMes(mes-1, year);
		
		try {
			
			for(Long sucursalId:sucursales){
				 System.out.println("Conectando a la Sucursal:"+sucursalId+"  Recalculando para: "+mes+"-"+year);
				 JdbcTemplate template=ConnectionServices.getInstance().getJdbcTemplate(sucursalId);
				 List<Map<String, Object>> claves= buscarDiscrepancias(template, sucursalId, year, mes);
				 
				Map<String, Object> suc=template.queryForMap("SELECT NOMBRE FROM SW_SUCURSALES WHERE SUCURSAL_ID=?",new Object[]{sucursalId});
				String sucName= suc.get("NOMBRE").toString();
				 
				 if(claves.isEmpty()){
					 System.out.println("NO HAY DISCREPANCIAS");
				 }else{
					 ActualizarExistencias(template, claves, sucursalId, per, mes, year,sucName);
				 }
			 }

			
		} catch (Exception e) {
			System.err.println("No hay conexion con la sucursal");
		}
		 
	 
 }
	
 	public  List<Map<String, Object>> buscarDiscrepancias(JdbcTemplate template,Long sucursalId, int year, int mes ){
 		
 		System.out.println("Buscando Discrepancias para :"+ sucursalId);
 		
 		String sql=SQLUtils.loadSQLQueryFromResource("sql/discrepanciasPorSucursal.sql");
 		
 		sql=sql.replaceAll("@SUCURSAL", sucursalId.toString());
 		sql=sql.replaceAll("@YEAR", year+"");
 		sql=sql.replaceAll("@MES", mes+"");
 		
 		List<Map<String, Object>> discrepancias=template.queryForList(sql);
 		
 	for(Map<String, Object> discrepancia:discrepancias){
 		System.out.println("Discrepancia en :"+discrepancia.get("CLAVE"));
 	}
 		
 		return discrepancias;
 	}
 
 
 public void ActualizarExistencias(JdbcTemplate template ,List<Map<String,Object>> claves,Long sucursalId,Periodo per,int mes, int year, String sucName){
 
	for(Map<String, Object> clave:claves){
		 String sql=SQLUtils.loadSQLQueryFromResource("sql/calculoDeExistenciaPorClave.sql");
		 
		sql=sql.replaceAll("@SUCURSAL", sucursalId+"");		
		sql=sql.replaceAll("@FECHA_INI", "2009/01/01");
		SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
	    String fecha1=  formato.format(per.getFechaFinal());
	   	sql=sql.replaceAll("@CORTE_FIN",fecha1 );
	   	sql=sql.replaceAll("@CORTE","2009/01/01" );	
		sql=sql.replaceAll("@CLAVE", clave.get("CLAVE").toString());
	    Map<String, Object> row= template.queryForMap(sql);
	     System.out.println("--"+row.get("CLAVE"));
	    String sqlExist="SELECT INVENTARIO_ID,CANTIDAD,CLAVE FROM SX_EXISTENCIAS WHERE CLAVE=? AND MES=? AND YEAR=? AND SUCURSAL_ID=?";
	    Map<String, Object> existencia= template.queryForMap(sqlExist,new Object[]{clave.get("CLAVE"),mes,year,sucursalId});
		 if(!row.get("CANTIDAD").equals(existencia.get("CANTIDAD"))){
			    System.err.println("Se debe recalcular: "+ row.get("CLAVE")+" "+row.get("CANTIDAD")+" "+existencia.get("CANTIDAD"));
			    String updateSql="UPDATE SX_EXISTENCIAS SET CANTIDAD=?, MODIFICADO=? WHERE  INVENTARIO_ID=?";
			      template.update(updateSql,new Object[]{row.get("CANTIDAD"),formato.format(new Date()),existencia.get("INVENTARIO_ID")});
			    String insertAudit="INSERT INTO audit_log (entityId,entityName,action,tableName,ip,SUCURSAL_ORIGEN,SUCURSAL_DESTINO,dateCreated,lastUpdated,replicado,message,version)"+
			    					"VALUES "+
			    					"(?,'Existencia','UPDATE','SX_EXISTENCIAS','10.10.X.X',?,'OFICINAS',NOW(),NOW(),null,null,0); ";
			    
			    System.out.println("INVENTARIO_ID: "+existencia.get("INVENTARIO_ID"));
			    template.update(insertAudit, new Object[]{existencia.get("INVENTARIO_ID"),sucName });
			    
		 }
		
	}
		
	 
 } 
	
	
	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	public RecalculoDeExistenciasRemoto addSucursal(Long...ids){
		sucursales.clear();
		for(Long id:ids){
			sucursales.add(id);
		}
		return this;
	}


	
	public static void main(String[] args) {
		new RecalculoDeExistenciasRemoto()
		//.addSucursal(2L,3L,5L,6L,9L,14L,11L)
		.addSucursal(14L)
		//.addSucursal(11L)
		//.actualizarExistenciasOficinas(DateUtil.toDate("14/02/2014"));
		.recalcularExistencias();
		
	}
	

}
