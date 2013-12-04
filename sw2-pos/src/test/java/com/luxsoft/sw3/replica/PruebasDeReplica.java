package com.luxsoft.sw3.replica;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

public class PruebasDeReplica {
	
	public static void testReplicaDeExistencia(){
		Existencia exis=Services.getInstance().getExistenciasDao().generar("POL90", 3L, 2013, 3);
		exis.setCantidad(exis.getCantidad()+1000);
		Services.getInstance().getExistenciasDao().save(exis);
	}
	
	public static void testReplicaDeVentas(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Venta v=(Venta)session.get(Venta.class, "00000000-3da238db-013d-a23d6f5d-0004");
				v.setComentario2("Test de replica "+System.currentTimeMillis());
				
				return null;
			}
		});
	}
	
	public static void testReplicaDeCompras(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,
					SQLException {
				Compra2 v=(Compra2)session.get(Compra2.class, "8a8a8161-3da2bc12-013d-a2bc9e15-0004");
				v.setComentario("Test de replica "+System.currentTimeMillis());
				
				return null;
			}
		});
	}
	
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		//testReplicaDeExistencia();
		//testReplicaDeVentas();
		testReplicaDeCompras();
	}

}
