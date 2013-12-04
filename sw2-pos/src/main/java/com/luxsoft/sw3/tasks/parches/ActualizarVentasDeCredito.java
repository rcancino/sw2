package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

/**
 * Actualiza :
 * 
 * DiaRevision(pedido.getCliente().getCredito().getDiarevision());
   setDiaDelPago(pedido.getCliente().getCredito().getDiacobro());
    setRevision(!pedido.getCliente().getCredito().isVencimientoFactura());
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarVentasDeCredito {
	
	
	public void execute(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Cargo v where v.origen=\'CRE\' and YEAR(fecha)=2010 order by v.clave";
				ScrollableResults rs=session.createQuery(hql).scroll();
				int buff=0;
				while(rs.next()){
					Cargo v=(Cargo) rs.get()[0];
					Cliente c=v.getCliente();
					if(c.getCredito()!=null){
						v.setDiaRevision(c.getCredito().getDiarevision());
						v.setDiaDelPago(c.getCredito().getDiacobro());
						v.setRevision(!c.getCredito().isVencimientoFactura());
						System.out.println("Venta fixed: "+v);
					}
					buff++;
					if(buff%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		new ActualizarVentasDeCredito().execute();
	}

}
