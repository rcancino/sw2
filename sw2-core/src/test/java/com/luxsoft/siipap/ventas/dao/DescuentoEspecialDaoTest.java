package com.luxsoft.siipap.ventas.dao;

import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.core.Descuento;
import com.luxsoft.siipap.ventas.model.DescuentoEspecial;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Pruebas de persistencia para descuentos epeciales
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class DescuentoEspecialDaoTest extends BaseDaoTestCase{
	
	private HibernateTemplate hibernateTemplate;
	
	Venta venta;
	
	
	
	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		venta=(Venta) hibernateTemplate.execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery("from Venta v left join fetch v.partidas p  where " +
						"v.precioBruto=true and v.saldo>0")
				.setMaxResults(1)
				.uniqueResult();
			}
		});
	}

	public void testAddRemove(){
		assertNotNull("Se requiere una venta para las pruebas",venta);
		DescuentoEspecial de=new DescuentoEspecial();
		de.setCargo(venta);
		
		final Autorizacion2 autorizacion=new Autorizacion2();
		autorizacion.setAutorizo("ADMIN");
		autorizacion.setComentario("DESCUENTO ESPECIAL DE PRUEBA");
		autorizacion.setFechaAutorizacion(new Date());		
		de.setAutorizacion(autorizacion);
		
		final Descuento descuento=new Descuento(45.5,"DESCUENTO FIJO");		
		de.setDescuento(descuento);
		
		
		de=(DescuentoEspecial)universalDao.save(de);
		flush();
		setComplete();
		
		
		logger.info("Descuento especial: "+toStringBean(de));
		assertNotNull(de.getId());
		//Probamos transitive persistance de la autorizacion
		assertNotNull(de.getAutorizacion().getId()); 
		
		universalDao.remove(DescuentoEspecial.class, de.getId());
		flush();
		try {
			universalDao.get(DescuentoEspecial.class, de.getId());
			fail("Debio mandar error al no encotrar el descuento");			
		} catch (DataRetrievalFailureException ex) {			
			assertNotNull(ex);
			System.out.println(ExceptionUtils.getRootCauseMessage(ex));
			
		}
		
		try {
			//Probamos eliminacion de la autorizacion
			universalDao.get(Autorizacion2.class, de.getAutorizacion().getId());
			fail("Debio mandar error al no encotrar la autorizacion");
		} catch (DataRetrievalFailureException ex) {			
			assertNotNull(ex);
			System.out.println(ExceptionUtils.getRootCauseMessage(ex));
			
		}
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	
}
