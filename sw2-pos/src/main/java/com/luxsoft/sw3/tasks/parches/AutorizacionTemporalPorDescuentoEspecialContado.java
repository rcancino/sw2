package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.AutorizacionDePedido;
import com.luxsoft.sw3.ventas.Pedido;

public class AutorizacionTemporalPorDescuentoEspecialContado {
	
	public static void autorizar(final long sucursalId,final long folio){
		
		Services.getInstance()
				.getHibernateTemplate().execute(new HibernateCallback(){

					public Object doInHibernate(Session session)throws HibernateException, SQLException {
						String hql="from Pedido p where p.folio=? and p.sucursal.id=?";
						Pedido pedido=(Pedido)session.createQuery(hql)
						.setLong(0, folio)
						.setLong(1, sucursalId)
						.setMaxResults(1)
						.uniqueResult();
						AutorizacionDePedido aut=new AutorizacionDePedido();
						aut.setComentario("DESCUENTO ESPECIAL DE CONTADO");
						aut.setIpAdress(KernellSecurity.getIPAdress());
						aut.setMacAdress(KernellSecurity.getMacAdress());
						aut.setFechaAutorizacion(new Date());
						pedido.setAutorizacion(aut);
						pedido.setFacturable(true);
						session.save(aut);
						
						//session.saveOrUpdate(pedido);
						session.flush();
						if(pedido.getPendiente()!=null){
							
						}else{
							System.out.println("El pedido no requiere autorizacion");
						}
						
						
						return null;
					}
					
				});
		
	}
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		autorizar(3L, 5008L);
		
	}

}
