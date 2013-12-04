package com.luxsoft.siipap.service.gastos;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Parche para corregir la relacion entre {@link GFacturaPorCompra} y
 * {@link RequisicionDe} para hacerla de one-to-one a many-to-many
 * 
 * @author Ruben Cancino
 *
 */
public class Parche_G1_CorrectorDeFacturas extends HibernateDaoSupport{
	
	/**
	public static void execute(){
		HibernateTemplate template=new HibernateTemplate(ServiceLocator2.getSessionFactory());
		template.execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				ScrollableResults rs=session.createCriteria(GFacturaPorCompra.class).scroll();
				int row=1;
				while(rs.next()){
					GFacturaPorCompra fac=(GFacturaPorCompra)rs.get()[0];
					RequisicionDe req=fac.getRequisiciondet();
					
					if(req!=null){
						System.out.println("Procesando req="+req.getId()+ "  Row: "+row++);
						
						fac.agregarRequisicion(req);
						//fac.setRequisiciondet(null);
						fac.actualizarSaldo();
						
					}
				}
				return null;
			}
			
		});
	}
	*/
	public static void main(String[] args) {
		//execute();
	}

}
