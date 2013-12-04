package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.rules.RevisionDeCargosRules;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Actualiza las propiedades mas siginificativas de una venta a credito con el catalogo
 * de clientes credito
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarVentasDeCredito {
	
	public void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				final String hql="from Cargo c left join fetch c.cobrador cc " +
				"  where c.origen=\'CRE\' " +
				"    and (c.total-c.aplicado)!=0 " +
				"  and c.fecha>?" +
				" order by c.nombre";
				ScrollableResults rs=session.createQuery(hql)
				.setParameter(0, DateUtil.toDate("31/12/2008"),Hibernate.DATE)
				.scroll();
				int count=0;
				while(rs.next()){
					Cargo c=(Cargo)rs.get()[0];
					//System.out.println(	"Actualizando Fac: "+c.getDocumento()+ "Suc: "+c.getSucursal()+ "Cliente: "+c.getNombre()+ "Saldo: "+c.getSaldoCalculado());
					ClienteCredito credito=c.getCliente().getCredito();
					
					if(credito==null){
						System.out.println("Venta a credito sin cliente credito: "+c.getNombre()+ " "+c.getClave());
						continue;
					}
					
					c.setDiaDelPago(credito.getDiacobro());
					c.setDiaRevision(credito.getDiarevision());					
					c.setPlazo(credito.getPlazo());
					c.setRevision(!credito.isVencimientoFactura());					
					if(c instanceof Venta){
						Venta v=(Venta)c;
						v.setVendedor(c.getCliente().getVendedor());
						v.setCobrador(c.getCobrador());
					}
					
					count++;
					if(count%20==0){
						session.flush();
						session.clear();
					}
					
					
				}
				return null;
			}
			
		});
	}
	
	public void execute(final String ventaId,final Date fecha){
		System.out.println("Actualizando para : "+fecha);
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				Cargo c=(Cargo)session.get(Cargo.class, ventaId);
				ClienteCredito credito=c.getCliente().getCredito();
				
				if(credito==null){
					System.out.println("Venta a credito sin cliente credito: "+c.getNombre()+ " "+c.getClave());
					return null;
				}
				
				c.setDiaDelPago(credito.getDiacobro());
				c.setDiaRevision(credito.getDiarevision());					
				c.setPlazo(credito.getPlazo());
				c.setRevision(!credito.isVencimientoFactura());					
				if(c instanceof Venta){
					Venta v=(Venta)c;
					v.setVendedor(c.getCliente().getVendedor());
					v.setCobrador(c.getCobrador());
				}
				RevisionDeCargosRules.instance().actualizar(c, fecha);
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		//new ActualizarVentasDeCredito().execute();
		new ActualizarVentasDeCredito().execute("8a8a8189-259dc174-0125-9dc25059-0041",ServiceLocator2.obtenerFechaDelSistema());
	}

}
