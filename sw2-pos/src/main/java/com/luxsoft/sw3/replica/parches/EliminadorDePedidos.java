package com.luxsoft.sw3.replica.parches;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.replica.ReplicaServices;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.PedidoPendiente;

public class EliminadorDePedidos {
	
	/**
	 * Elimina pedidos  
	 * 
	 * @param sucursalId
	 */
	public void eliminarPedidos(Long sucursalId,Date fecha){
		JdbcTemplate jdbcTemplate=ReplicaServices.getInstance().getJdbcTemplate(sucursalId);
		String sql="select PEDIDO_ID from sx_pedidos p where fecha<=? " +
				"and pedido_id not in(select PEDIDO_ID as PEDIDO from sx_ventas v where v.PEDIDO_ID=p.PEDIDO_ID)";
		SqlParameterValue p1=new SqlParameterValue(Types.DATE,fecha);
		final List<String> ids=jdbcTemplate.queryForList(sql,new Object[]{p1},String.class);
		final HibernateTemplate template=ReplicaServices.getInstance().getHibernateTemplate(sucursalId);
		System.out.println("Pedidos a eliminar: "+ids.size());
				
		template.execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				int row=1;
				for(final String id: ids){
					try {
						System.out.println("Eliminando pedido: "+id+ " Row: "+row++);
						Pedido p=(Pedido)session.get(Pedido.class, id);
						//p.getPartidas().iterator().next();
						if(p.getPendiente()!=null){
							PedidoPendiente pendiente=p.getPendiente();
							//pendiente.setPedido(null);
							p.setPendiente(null);
							session.delete(pendiente);
						}
						if(p.getInstruccionDeEntrega()!=null){
							InstruccionDeEntrega ie=p.getInstruccionDeEntrega();
							p.setInstruccionDeEntrega(null);
							session.delete(ie);
						}
						if(p.getPagoContraEntrega()!=null){
							AutorizacionDePedido ap=p.getPagoContraEntrega();
							 p.setPagoContraEntrega(null);
							session.delete(ap);
						}
						if(p.getAutorizacion()!=null){
							AutorizacionDePedido ap=p.getAutorizacion();
							p.setAutorizacion(null);
							session.delete(ap);
						}
						//template.initialize(p.getAutorizacionSinExistencia());
						//template.initialize(p.getAutorizacion());
						//template.initialize(p.getPartidas());
						session.delete(p);
						session.flush();
						session.clear();
						if(row%20==0){
							
							System.out.println("Flushing....");
						}
					} catch (Exception e) {
						String msg=ExceptionUtils.getRootCauseMessage(e);
						System.out.println("Error: "+msg+ "Pedido: "+id);
						e.printStackTrace();
					}
					
					
				}
				
				return null;
			}
			
		});
		
		
	}
	
	
	public static void main(String[] args) {
		//new EliminadorDePedidos().eliminarPedidos(2L, DateUtil.toDate("23/02/2011"));
		//new EliminadorDePedidos().eliminarPedidos(5L, DateUtil.toDate("23/02/2011"));
		new EliminadorDePedidos().eliminarPedidos(6L, DateUtil.toDate("23/02/2011"));
	}

}
