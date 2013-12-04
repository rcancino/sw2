package com.luxsoft.siipap.inventarios.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;

/**
 * Actualiza los kilos de los movimientos de inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ActualizarKilosDeMovimientos {
	
	public void execute(final int mes){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session
				.createQuery("from Inventario  i where YEAR(i.fecha)=2009 and MONTH(i.fecha)=?")
				.setInteger(0, mes)
				.scroll();
				int buff=0;
				while(rs.next()){
					Inventario inv=(Inventario)rs.get()[0];
					System.out.println("Procesando movimiento: "+inv.getMes()+ "Row:"+rs.getRowNumber());
					inv.actualizarKilosDelMovimiento();
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
		DBUtils.whereWeAre();
		new ActualizarKilosDeMovimientos().execute(1);
	}

}
