package com.luxsoft.siipap.cxc.parches;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;

public class Parche3_AjusteDe4DeJunio {
	
	private static Long buscarId(final Venta v){
		String sql="select VENTA_ID from SW_VENTAS where NUMERO=? and SUCURSAL=? and SERIE=? ";
		Object[] params=new Object[]{v.getDocumento(),v.getSucursal().getClave(),v.getSerieSiipap()};
		List<Long> data=ServiceLocator2.getAnalisisJdbcTemplate().queryForList(sql, params,Long.class);
		return data.get(0);
	}
	
	private static Long buscarId(final VentaDet v){
		String sql="select VENTADET_ID from SW_VENTASDET where VENTA_ID=? and RENGLON=?";
		Object[] params=new Object[]{v.getVenta().getSiipapWinId(),v.getRenglon()};
		List<Long> data=ServiceLocator2.getAnalisisJdbcTemplate().queryForList(sql, params,Long.class);
		return data.get(0); 
	}
	
	
	
	public static void execute(){
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				String hql="from Venta c " +
					"  where c.origen=\'CRE\' " +
					"  	and c.fecha=?" +
					" order by c.clave desc";
				ScrollableResults rs=session.createQuery(hql)
					.setParameter(0, DateUtil.toDate("04/06/2009"),Hibernate.DATE)
				.scroll();
				int buff=0;
				while(rs.next()){
					
					Venta cargo=(Venta)rs.get()[0];
					cargo.setPrecioBruto(false);
					cargo.setSiipapWinId(buscarId(cargo));
					cargo.setDescuentoGeneral(0d);
					for(VentaDet det:cargo.getPartidas()){
						det.setSiipapWinId(buscarId(det));
					}					
					if((++buff)%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
			}
			
		});
	}
	
	
	public static void main(String[] args) {
		execute();
	}

}
