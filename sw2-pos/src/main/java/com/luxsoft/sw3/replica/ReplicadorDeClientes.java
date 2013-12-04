package com.luxsoft.sw3.replica;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.ReplicationMode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class ReplicadorDeClientes {
	
	private Set<Long> sucursales;
	
	private Logger logger=Logger.getLogger(getClass());
	
	/**
	 * Importa los clientes pendientes de todas las sucursales a produccion
	 * usando la tabla de sx_clientes_log
	 */
	public void refrescarPendientes(){
		//importarPendientes(getSucursales().toArray(new Long[0]));
		replicarPendientes();
	}
	
	
	
	public void replicarPendientes(){
		for(Long sucursalId:getSucursales()){
			importarPendientes(sucursalId);
		}
		final JdbcTemplate template=Services.getInstance().getJdbcTemplate();
		String sql="select * from sx_clientes_log where tx_replicado is null and date(modificado)>=? order by modificado desc";
		Object[] values=new Object[]{new SqlParameterValue(Types.DATE, new Date())};
		List<Map<String, Object>> rows=template.queryForList(sql,values);
		if(rows.size()>0){
			logger.info("Client es pendiente de replicado: "+rows.size());
		}
		for(Map<String, Object> row:rows){
			String clave=(String)row.get("CLAVE");
			Number id=(Number)row.get("ID");
			Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
			boolean aplicado=true;			
			if(c!=null){
				logger.info("Replicando :"+c.getClave()+ " Log ID:"+id);
				for(Long sucursalId:getSucursales()){					
					
					HibernateTemplate target=ReplicaServices.getInstance()
						.getHibernateTemplate(sucursalId);
					try {
						
						target.replicate(c, ReplicationMode.OVERWRITE);
						
					} catch (Exception e) {
						aplicado=false;
						logger.error("Error replicando cliente: "+c.getClave()+ " A sucursal: "+sucursalId+ "  "+ExceptionUtils.getRootCauseMessage(e));
						
					}
				}
				if(aplicado){
					try {
						Object[] params={
								new SqlParameterValue(Types.TIMESTAMP,new Date())
								,new SqlParameterValue(Types.NUMERIC,id.longValue())
								};
						template.update("update sx_clientes_log set tx_replicado=? where id=?",params);
						//logger.info("Log replicado: "+id+ "Cliente : "+c.getClave());
					} catch (Exception e) {
						logger.error(e);
					}
					logger.info("Actualizacion de cliente exitosa Log ID: "+id+" Cliente(Clave):"+c.getClave());
					
				}
				
			}
		}
	}
	
	/**
	 * Replica los clientes faltantes de enviar a las sucursales
	 * 
	 * Util para tareas especiales
	 * 
	 */
	public void replicarFaltantes(){
		final JdbcTemplate template=Services.getInstance().getJdbcTemplate();
		String sql="select distinct clave from sx_clientes_log where tx_replicado is null order by modificado desc";
		List<Map<String, Object>> rows=template.queryForList(sql);
		if(rows.size()>0){
			logger.info("Clientes pendiente de replicado: "+rows.size());
		}
		for(Map<String, Object> row:rows){
			String clave=(String)row.get("CLAVE");
			Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
			if(c!=null){
				logger.info("Replicando :"+c.getClave());
				for(Long sucursalId:getSucursales()){	
					HibernateTemplate target=ReplicaServices.getInstance()
						.getHibernateTemplate(sucursalId);
					try {						
						target.replicate(c, ReplicationMode.OVERWRITE);
						logger.info("Cliente replicado: "+c.getClave());
					} catch (Exception e) {
						logger.error("Error replicando cliente: "+c.getClave()+ " A sucursal: "+sucursalId+ "  "+ExceptionUtils.getRootCauseMessage(e));
						
					}
				}
			}
		}
	}
	
	
	/**
	 * Replica los cambios de un cliente hechos desde las oficinas
	 * 
	 * @param clave
	 */
	public void replicarDeProduccion(final String clave){
		Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
		for(Long sucursalId:getSucursales()){
			System.out.println("Replicando "+clave+ " a sucursal: "+sucursalId);
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			try {
				target.replicate(c, ReplicationMode.OVERWRITE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void replicarDeProduccion(String...claves){
		for(String clave:claves){
			replicarDeProduccion(clave);
		}
	}
	
	public void importarPendientes(Long sucursalId){
		String sql="select * from sx_clientes_log where tx_importado is null";
		JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
		List<Map<String, Object>> rows=template.queryForList(sql);
		for(Map<String,Object> row:rows){
			System.out.println("Importando: "+row);
			
			try {
				Number cliente_id=(Number)row.get("CLIENTE_ID");
				HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
				List<Cliente> clientes= source.find(
						"from Cliente c " +
						" left join fetch c.direcciones d" +
						" left join fetch c.telefonos t" +
						" where c.id=?"
						, cliente_id.longValue());
				Cliente c=clientes.get(0);
				Services.getInstance().getHibernateTemplate().replicate(c, ReplicationMode.IGNORE);
			} catch (Exception e) {
				logger.error("Error importando cliente: "+row,e);
			}
			try {
				Number id=(Number)row.get("ID");
				String update="UPDATE SX_CLIENTES_LOG SET TX_IMPORTADO=NOW() WHERE ID=?";
				template.update(update,new Object[]{id.longValue()});
			} catch (Exception e) {
				logger.error("Error actualizando importado: "+row);
			}
			
		}
	}
	
	
	
	/**
	 * Importa un cliente 
	 * 
	 * @param clave
	 * @param source
	 */
	public void importarCliente(
			String clave			
			,Long sucursalId
			){
		HibernateTemplate target=Services.getInstance().getHibernateTemplate();
		HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<Cliente> clientes= source.find(
				"from Cliente c left join fetch c.credito " +
				" left join fetch c.direcciones d" +
				" left join fetch c.telefonos t" +
				" where c.clave=?"
				, clave);
		Cliente c=clientes.get(0);
		target.replicate(c, ReplicationMode.EXCEPTION);
	}
	
	/**
	 * Importa todos los clientes pendientes de las sucursales
	 * 
	 */
	public void importarPendientes(){
		for(Long sucursalId:getSucursales()){
			importarPendientes(sucursalId);
		}
	}

	/**
	 * Manda a las sucursales el registro ClienteCredito para los clientes
	 * de credito
	 * 
	 */
	public void actualizarClientesDeCredito(){
		
		List<Cliente> clientes=Services.getInstance().getHibernateTemplate().find("from Cliente c left join fetch c.credito where c.credito is not null");
		for(Long sucursalId:getSucursales()){
			String update="UPDATE SX_CLIENTES SET CREDITO_ID=null ";
			String delete="DELETE FROM SX_CLIENTES_CREDITO";
			JdbcTemplate template=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
			HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
			int ups=template.update(update);
			System.out.println("Clientes actualizados en sucursal: "+sucursalId+ " "+ups);
			int dels=template.update(delete);
			System.out.println("Registros de credito eliminados en sucursal: "+sucursalId+ " "+dels);
			for(Cliente c:clientes){
				target.replicate(c, ReplicationMode.OVERWRITE);
				System.out.print(".");
			}
			System.out.println("Clientes actualizados en sucursal: "+sucursalId+ " :"+clientes.size());
		}
	}


	public Set<Long> getSucursales() {
		if(sucursales==null){
			sucursales=new HashSet<Long>();
			sucursales.add(2L);
			sucursales.add(3L);
			sucursales.add(5L);
			sucursales.add(6L);
			sucursales.add(9L);
		}
		return sucursales;
	}
	
	
	public void importarClientesPendientes(final Date desde){
		for(Long sucursal:getSucursales()){
			
			JdbcTemplate sucursalTemplate=ReplicaServices.getInstance().getJdbcTemplate(sucursal);
			String sql="select CLIENTE_ID from sx_clientes where date(creado)>=?";
			Object args[]={new SqlParameterValue(Types.DATE, desde)};
			final List<Long> ids=sucursalTemplate.queryForList(sql,args, Long.class);
			System.out.println("Clientes: "+ids.size());
			for(Long id:ids){
				System.out.println("Validando: "+id+ " En sucursal: "+sucursal);
				Cliente c=Services.getInstance().getClientesManager().get(id);
				if(c==null){
					System.out.println("Faltante: "+id+  " importandolo...");
					HibernateTemplate target=Services.getInstance().getHibernateTemplate();
					HibernateTemplate source=ReplicaServices.getInstance().getHibernateTemplate(sucursal);
					List<Cliente> clientes= source.find(
							"from Cliente c left join fetch c.credito " +
							" left join fetch c.direcciones d" +
							" left join fetch c.telefonos t" +
							" where c.id=?"
							, id);
					c=clientes.get(0);
					target.replicate(c, ReplicationMode.EXCEPTION);
				}
			}
			
		}
		
		
	}

	

	public void setSucursales(Set<Long> sucursales) {
		this.sucursales = sucursales;
	}
	
	
	
	public static void run(){
		Services.getInstance().getHibernateTemplate();
		ReplicadorDeClientes replicador=new ReplicadorDeClientes();
		String base=POSDBUtils.getAplicationDB_URL();
		while(true){
			String clave=JOptionPane.showInputDialog(null,"Clave a replicar",base,JOptionPane.INFORMATION_MESSAGE);
			replicador.replicarDeProduccion(clave);
		}
	}
	
	public void replicacionEspecial(Long sucursalId){
		String sql="SELECT distinct clave FROM sx_clientes_log where date(modificado)>=\'2011/04/18\'";
		HibernateTemplate target=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		List<String> faltantes=Services.getInstance().getJdbcTemplate().queryForList(sql,String.class);		
		for(String clave:faltantes){
			System.out.println("Replicando "+clave+ " a sucursal: "+sucursalId);
			Cliente c=Services.getInstance().getClientesManager().buscarPorClave(clave);
			try {
				target.replicate(c, ReplicationMode.OVERWRITE);
				System.out.print(".");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public static void main(String[] args) {
		
		ReplicadorDeClientes replicador=new ReplicadorDeClientes();
		//replicador.run();
		//replicador.importarPendientes(2l);
		//replicador.importarCliente("CA00276", 2l);
		//replicador.replicacionEspecial(5L);
		replicador.replicarPendientes();
		replicador.importarClientesPendientes(new Date());
		
			
	}

}
