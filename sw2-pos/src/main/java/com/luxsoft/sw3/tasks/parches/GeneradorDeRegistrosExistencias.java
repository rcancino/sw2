package com.luxsoft.sw3.tasks.parches;

import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.sw3.services.POSDBUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Genera las entidades de existencias requeridas para todos los productos inventariables
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class GeneradorDeRegistrosExistencias {
	
	public void execute(String clave){
		final Producto p=Services.getInstance().getProductosManager().buscarPorClave(clave);
		final Date fecha=Services.getInstance().obtenerFechaDelSistema();
		final Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
		Existencia e=new Existencia();
		e.setSucursal(s);
		e.setProducto(p);
		e.setCreateUser("ADMIN");
		e.setFecha(fecha);
		e.setYear(Periodo.obtenerYear(fecha));
		e.setMes(Periodo.obtenerMes(fecha)+1);
		Services.getInstance().getUniversalDao().save(e);
	}
	
	public void execute(){
		final Date fecha=Services.getInstance().obtenerFechaDelSistema();
		final Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
		Services.getInstance().getHibernateTemplate().execute(new HibernateCallback(){
			
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="from Producto p " +
						" where p.inventariable=true" +
						" and p not in(select e.producto from Existencia e where e.sucursal.id=?)" +
						" order by p.clave desc";
				ScrollableResults rs=session
				.createQuery(hql)
				.setLong(0, s.getId())
				.scroll();
				int buf=0;
				while(rs.next()){
					Producto p=(Producto)rs.get()[0];
					Existencia e=new Existencia();
					e.setSucursal(s);
					e.setProducto(p);
					e.setCreateUser("ADMIN");
					e.setFecha(fecha);
					e.setYear(Periodo.obtenerYear(fecha));
					e.setMes(Periodo.obtenerMes(fecha));
					session.saveOrUpdate(e);
					
					if(buf%20==0){
						session.flush();
						session.clear();
					}
					
					System.out.println("Procesando : "+p);
				}
				return null;
			}
			
		});
	}
	
	public static void main(String[] args) {
		//new ActualizarExistencias().execute();
		POSDBUtils.whereWeAre();
		String[] claves={"SBSB17324"};
			for(String clave:claves){
			new GeneradorDeRegistrosExistencias().execute(clave);
		}
		
	}

}
