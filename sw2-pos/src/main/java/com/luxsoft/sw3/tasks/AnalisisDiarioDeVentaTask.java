package com.luxsoft.sw3.tasks;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.AnalisisDeVentas;

public class AnalisisDiarioDeVentaTask {
	
	public void execute(){
		execute(Services.getInstance().obtenerFechaDelSistema()
				,Services.getInstance().getConfiguracion().getSucursal()
				);
	}
	
	public void execute(final Date fecha,final Sucursal  suc){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Venta v where v.sucursal.id=3 and v.fecha=?";
				AnalisisDeVentas a=new AnalisisDeVentas();
				List<Venta> list=session
				.createQuery(hql)
				.setLong(0, suc.getId())
				.setParameter(1, fecha,Hibernate.DATE)
				.list();
				a.getVentas().clear();
				a.getVentas().addAll(list);
				System.out.println(ToStringBuilder.reflectionToString(a));
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		POSDBUtils.whereWeAre();
		new AnalisisDiarioDeVentaTask().execute();
	}

}
