package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

/**
 * Re clasifica las ventas de los clientes asignados a un vendedor
 * para el periodo indicado
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ReclasificarVentasPorVendedor {
	
	
	
	/**
	 * Para todos los clientes asignados al vendedor indicado
	 * reclasifica sus ventas en el periodo
	 *  
	 * @param vendedor
	 * @param periodo
	 */
	public void execute(final Long vendedor,final Periodo periodo){
		final Vendedor v=(Vendedor)Services.getInstance().getUniversalDao().get(Vendedor.class, vendedor);
		List<String> claves=Services.getInstance().getHibernateTemplate()
			.find("select c.clave from Cliente c where c.vendedor.id=?",vendedor);
		for(String clave:claves){
			System.out.println("Reclasificando ventas de: "+clave+ "  Asignandolas al vendedor: "+v);
			final String cliente=clave;
			Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){
				public Object doInHibernate(Session session)throws HibernateException, SQLException {
					List<Venta> ventas=session.createQuery("from Venta v where  v.fecha between ? and ? and v.clave=?")
					.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
					.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
					.setString(2, cliente)
					.list();
					for(Venta vent:ventas){
						vent.setVendedor(v);
						System.out.println("Re asignando: "+vent.getId());
					}
					return null;
				}
				
			});
		}
		
	}
	
	public static void main(String[] args) {
		new ReclasificarVentasPorVendedor().execute(50L, new Periodo("01/03/2010","26/04/2010"));
	}

}
