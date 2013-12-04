package com.luxsoft.siipap.cxc.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.cxc.model.Esquema;
import com.luxsoft.siipap.cxc.model.EsquemaPorTarjeta;
import com.luxsoft.siipap.cxc.model.Tarjeta;
import com.luxsoft.siipap.dao.BaseDaoTestCase;

/**
 * Prueba la presistencia del bean de {@link Tarjeta}
 * 
 * @author Ruben Cancino
 *
 */
public class TarjetasDaoTest extends BaseDaoTestCase{
	
	private List<Esquema> esquemas=new ArrayList<Esquema>();
	
	
		
	@Override
	protected void onSetUpInTransaction() throws Exception {
		esquemas.add((Esquema)universalDao.save(new Esquema("3 MESES SIN INTERESES")));
		esquemas.add((Esquema)universalDao.save(new Esquema("6 MESES SIN INTERESES")));
		esquemas.add((Esquema)universalDao.save(new Esquema("12 MESES SIN INTERESES")));
	}

	@Override
	protected void onSetUpBeforeTransaction() throws Exception {
		
	}

	public void testAddRemove(){
		Tarjeta t=new Tarjeta();
		t.setComisionBancaria(.05);
		t.setNombre("AMERICAN EXPRESS SERVICIOS");
		t=(Tarjeta)universalDao.save(t);
		flush();
		assertNotNull(t.getId());
		
		universalDao.remove(Tarjeta.class, t.getId());
		flush();
		try {
			universalDao.get(Tarjeta.class, t.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			assertNotNull(e);
		}
		
	}
	
	public void testAddRemoveEsquemas(){
		Tarjeta t=new Tarjeta();
		t.setComisionBancaria(.05);
		t.setNombre("AMERICAN EXPRESS SERVICIOS");
		t.agregarEsquema(esquemas.get(0), .04d);
		t.agregarEsquema(esquemas.get(1), .08d);
		t=(Tarjeta)universalDao.save(t);		
		flush();
		
		t=(Tarjeta)universalDao.get(Tarjeta.class, t.getId());
		assertEquals(2,t.getEsquemas().size());
		
		//Eliminamos un esquema
		EsquemaPorTarjeta e1=t.getEsquemas().iterator().next();		
		t.eliminarEsquema(e1);
		t=(Tarjeta)universalDao.save(t);
		flush();
		
		assertEquals(1, t.getEsquemas().size());
		
		System.out.println("Esquema restante: "+t.getEsquemas());
		
	}
	
	
	public void testAddBulkData(){
		Tarjeta t=new Tarjeta();
		t.setComisionBancaria(.05);
		t.setNombre("AMERICAN EXPRESS SERVICIOS");
		t.agregarEsquema(esquemas.get(0), .04d);
		t.agregarEsquema(esquemas.get(1), .8d);
		t=(Tarjeta)universalDao.save(t);
		
		Tarjeta t2=new Tarjeta();
		t2.setComisionBancaria(.05);
		t2.setNombre("BANAMEX");
		t2.agregarEsquema(esquemas.get(1), .04d);
		t2.agregarEsquema(esquemas.get(2), .8d);
		t2=(Tarjeta)universalDao.save(t2);
		
		Tarjeta t3=new Tarjeta();
		t3.setComisionBancaria(.05);
		t3.setNombre("SANTANDER CLASICA");
		t3=(Tarjeta)universalDao.save(t3);
		setComplete();
	}
	


}
