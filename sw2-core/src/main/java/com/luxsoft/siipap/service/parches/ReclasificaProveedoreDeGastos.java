package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Actualiza Nombre del proveedor 
 * 
 * @author RUBEN
 *
 */
public class ReclasificaProveedoreDeGastos extends HibernateDaoSupport{
	/*
	public void corregir(){
		
		final Long newId=163332l;
		
		getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from GCompra c where c.proveedor.id=?";
				ScrollableResults rs=session.createQuery(hql).setLong(0, newId)
				.scroll();
				
				while(rs.next()){
					GCompra com=(GCompra)rs.get()[0];
					//System.out.println(com);
					for(GFacturaPorCompra fac:com.getFacturas()){
						//fac.setProveedor(com.getProveedor().getNombreRazon());
							if(fac.getRequisiciondet()!=null){
								System.out.println(fac.getRequisiciondet().getRequisicion().getAfavor());
								Requisicion r=fac.getRequisiciondet().getRequisicion();
								//r.setAfavor(com.getProveedor().getNombreRazon());
								if(fac.getRequisiciondet().getRequisicion().getPago()!=null){
									System.out.println("Pago:" +fac.getRequisiciondet().getRequisicion().getPago().getAFavor());
									//CargoAbono ca=fac.getRequisiciondet().getRequisicion().getPago();
									//ca.setAFavor(com.getProveedor().getNombreRazon());
								}
							}
					}
				}
				return null;
			}
			
		});
	}
	*/
	public static void main(String[] args) {
		ReclasificaProveedoreDeGastos parche=new ReclasificaProveedoreDeGastos();
		parche.setSessionFactory(ServiceLocator2.getSessionFactory());
		//parche.corregir();
	}

}
