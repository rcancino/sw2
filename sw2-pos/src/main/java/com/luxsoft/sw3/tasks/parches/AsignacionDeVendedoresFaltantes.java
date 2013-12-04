package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.ventas.model.Vendedor;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

public class AsignacionDeVendedoresFaltantes {
	
	
	public void execute(final Periodo periodo){
		final Vendedor directo=(Vendedor)Services.getInstance().getUniversalDao().get(Vendedor.class, 1L);
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from Venta v where  v.fecha between ? and ? and v.vendedor is null")
				.setParameter(0, periodo.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, periodo.getFechaFinal(),Hibernate.DATE)
				.scroll();
				int buffer=0;
				while(rs.next()){
					Venta v=(Venta)rs.get()[0];
					if(v.getCliente().getVendedor()!=null){
						v.setVendedor(v.getCliente().getVendedor());
					}else{
						System.out.println("Asignando directo a: "+v.getDocumento()+ " Cliente:"+v.getClave());
						v.setVendedor(directo);
					}
					buffer++;
					if(buffer%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		new AsignacionDeVendedoresFaltantes().execute(new Periodo("01/03/2010","25/04/2010"));
	}

}
