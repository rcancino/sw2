package com.luxsoft.siipap.dao.tesoreria;

import org.springframework.orm.ObjectRetrievalFailureException;

import com.luxsoft.siipap.dao.BaseDaoTestCase;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Departamento;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;

public class RequisicionDaoTest extends BaseDaoTestCase{
	
	private RequisicionDao requisicionDao;
	
	public void testAddRemove(){
		Requisicion r=createReqTest();
		final String afavor=r.getAfavor();
		r.actualizarTotal();
		r=requisicionDao.save(r);
		flush();
		
		assertEquals(afavor,r.getAfavor());
		assertNotNull(r.getId());
		setComplete();
		
		log.debug("Eliminando requisicion");
		requisicionDao.remove(r.getId());
		flush();
		
		try {
			r=requisicionDao.get(r.getId());
			fail("Debio mandar error");
		} catch (ObjectRetrievalFailureException e) {
			log.debug("Expected exception: "+e.getMessage());
			assertNotNull(e);
		}
	}
	
	/*
	public void testAplicarPago(){
		final User user=(User)userDao.loadUserByUsername("admin");
		assertNotNull(user);
		final Autorizacion aut=new Autorizacion(new Date(),user);
		aut.setComentario("Aut test:"+aut.getAutorizo().getUsername());
		Requisicion r=createReqTest();
		r.autorizar(aut);		
		r=requisicionDao.save(r);
		flush();
		
		assertNotNull(r.getConcepto().getId());
		
		Cuenta cta=(Cuenta)universalDao.get(Cuenta.class, new Long(-1));
		PagoDeRequisicion p=requisicionDao.aplicarPago(r, cta);
		assertNotNull(p);
		flush();
		setComplete();
	}*/
	
	private Requisicion  createReqTest(){
		Departamento d=(Departamento)universalDao.get(Departamento.class, new Long(1));
		assertNotNull(d);
		final Concepto concepto=(Concepto)universalDao.get(Concepto.class, new Long(2));
		assertNotNull(concepto);
		Sucursal s=(Sucursal)universalDao.get(Sucursal.class, new Long(1));
		assertNotNull(s);
		Requisicion r=new Requisicion();
		r.setAfavor("Sistemas integrales");
		r.setComentario("Requisicion de prueba");
		for(int i=0;i<10;i++){
			RequisicionDe det=new RequisicionDe();
			det.setComentario("ReqDet #"+i+1);
			det.setDocumento("FAC"+i+1);
			det.setTotal(CantidadMonetaria.pesos(500));
			det.setSucursal(s);
			det.setDepartamento(d);
			r.agregarPartida(det);
		}
		r.setConcepto(concepto);
		return r;
	}
	

	public void setRequisicionDao(RequisicionDao requisicionDao) {
		this.requisicionDao = requisicionDao;
	}
	
	

}
