package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Ajuste de abonos para actualizar la fecha de la primera aplicacion 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AjusteDeAbonosSAF {
	
	public void execute(){
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery("from Pago p where p.fecha>=? and p.primeraAplicacion is null")
				.setParameter(0, DateUtil.toDate("15/12/2009"))
				.scroll();
				int buff=0;
				while(rs.next()){
					Pago p=(Pago)rs.get()[0];
					if(!p.getAplicaciones().isEmpty()){
						Aplicacion a=(Aplicacion)p.getAplicaciones().get(0);
						if(a==null){
							a=(Aplicacion)p.getAplicaciones().get(1);
						}
						if(a!=null){
							p.setPrimeraAplicacion(a.getFecha());
						}
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
		POSDBUtils.whereWeAre();
		new AjusteDeAbonosSAF().execute();
	}

}
