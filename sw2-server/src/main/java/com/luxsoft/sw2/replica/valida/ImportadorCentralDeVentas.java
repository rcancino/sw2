package com.luxsoft.sw2.replica.valida;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.orm.hibernate3.HibernateCallback;


import com.luxsoft.siipap.cxc.model.CancelacionDeCargo;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.utils.LoggerHelper;

public class ImportadorCentralDeVentas implements ImportadorDeFaltantes{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	SimpleJdbcInsert insert;
	SimpleJdbcInsert insertPartidas;
	SimpleJdbcInsert insertCancelados;
	SimpleJdbcInsert insertAutorizaciones;
	
	public ImportadorCentralDeVentas(){
		insert=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_VENTAS");
		insertPartidas=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_VENTASDET");
		insertCancelados=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("SX_CXC_CARGOS_CANCELADOS");
		insertAutorizaciones=new SimpleJdbcInsert(ServiceLocator2.getJdbcTemplate()).withTableName("sx_autorizaciones2");
	}
	
	/*public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select CARGO_ID from SX_VENTAS where date(creado)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(Venta.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Faltantes localizados: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_VENTAS where CARGO_ID=?", new Object[]{id});
				row.put("PEDIDO_ID", null);
				pendientes.add(row);
			}
			int[] res=insert.executeBatch(pendientes.toArray(new Map[0]));
			logger.info("Insertados: "+res.length);
			importarPartidasFaltantes(fecha, template, sucursalId);
			importarCancelados(fecha, template, sucursalId);
		}		
	}*/
	
	public void importarFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		
			importarPartidasFaltantes(fecha, template, sucursalId);
			importarCancelados(fecha, template, sucursalId);
				
	}
	
	public void importarPartidasFaltantes(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select INVENTARIO_ID from SX_VENTASDET where date(fecha)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Registros de Partidas detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(VentaDet.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_VENTASDET where INVENTARIO_ID=?", new Object[]{id});
				pendientes.add(row);
			}
			int[] res=insertPartidas.executeBatch(pendientes.toArray(new Map[0]));
			logger.info(" Partidas Insertados: "+res.length);
		}		
	}
	
	public void importarCancelados(final Date fecha,JdbcTemplate template,Long sucursalId){
		String sql="select ID from SX_CXC_CARGOS_CANCELADOS where date(creado)=?";
		List<String> rows=template.queryForList(sql,ValUtils.getDateAsSqlParameter(fecha),String.class);
		logger.info("Cargos cancelados detectados: "+rows.size());
		if(!rows.isEmpty()){
			List<String> faltantes=new ArrayList<String>();
			for(String id:rows){
				Object found=ServiceLocator2.getHibernateTemplate().get(CancelacionDeCargo.class, id);
				if(found==null)
					faltantes.add(id);
			}
			logger.info("Partidas faltantes localizadas: "+faltantes.size()+ " De la sucursal: "+sucursalId);
			List<Map<String, Object>> pendientes=new ArrayList<Map<String,Object>>();
			for(String id:faltantes){
				Map<String,Object> row=template.queryForMap("select * from SX_CXC_CARGOS_CANCELADOS where ID=?", new Object[]{id});
				pendientes.add(row);
			}
			
			for(Map<String,Object> row:pendientes){
				String aut_id=(String)row.get("AUT_ID");
				Map<String,Object> authRow=template.queryForMap("select * from sx_autorizaciones2 where AUT_ID=?", new Object[]{aut_id});
				insertAutorizaciones.execute(authRow);
				logger.info("Autorizacion insertada: "+authRow);
			}
			
			int[] res=insertCancelados.executeBatch(pendientes.toArray(new Map[0]));
			logger.info(" Cargos cancelados insertados: "+res.length);
			for(Map<String,Object> row:pendientes){
				final String id=(String)row.get("CARGO_ID");
				Venta venta=(Venta)ServiceLocator2.getVentasManager().buscarVentaInicializada(id);
				if(venta!=null){
					if(!StringUtils.containsIgnoreCase("CANCELADO", venta.getComentario2())){
						ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {							
							public Object doInHibernate(Session session) throws HibernateException,SQLException {
								Venta v=(Venta)session.load(Venta.class, id);
								v.setImporte(BigDecimal.ZERO);
								v.setTotal(BigDecimal.ZERO);
								v.setImpuesto(BigDecimal.ZERO);
								v.setComentario2("CANCELADO");
								v.getPartidas().clear();
								return null;
							}
						});
						
						//ServiceLocator2.getHibernateTemplate().merge(venta);
					}					
				}
			}
		}		
	}

}
