package com.luxsoft.siipap.service.parches;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;

public class ActualizarContrarecibosCxP {
	
	public static void run(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				ScrollableResults rs=session.createQuery("from Requisicion r  where year(r.fecha)>2010 and r.origen=\'COMPRAS\'").scroll();
				int buff=0;
				while(rs.next()){
					Requisicion r=(Requisicion)rs.get()[0];
					if(r.getProveedor()==null){
						System.out.println("Req si proveedor: "+r.getId());
						continue;
					}
					for(RequisicionDe det:r.getPartidas()){
						if(StringUtils.isBlank(det.getDocumento()))
							continue;
						String hql="from ContraReciboDet r where r.recibo.proveedor.id=? and r.documento=? and r.requisicion is null";
						
						List<ContraReciboDet> dets=session.createQuery(hql)
								.setLong(0, r.getProveedor().getId())
								.setString(1, det.getDocumento()).list();
						for(ContraReciboDet cr:dets){
							
							System.out.println("Actualizando contrarecibodet......."+cr.getId()+ "  Asignando RequisicionDet:"+det.getId());
							cr.setRequisicion(det.getId());
							session.saveOrUpdate(cr);
						}
						
					}
					
					buff++;
					if(buff%20==0){
						session.flush();
						session.clear();
						
						System.out.println("Salvando...");
					}
				}
				return null;
			}
		});
	}

	public static void main(String[] args) {
		run();
		
	}
}
