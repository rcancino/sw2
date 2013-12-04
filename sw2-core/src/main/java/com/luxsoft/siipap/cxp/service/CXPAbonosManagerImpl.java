package com.luxsoft.siipap.cxp.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.dao.CXPNotaDao;
import com.luxsoft.siipap.cxp.dao.CXPPAgoDao;
import com.luxsoft.siipap.cxp.model.CXPAbono;
import com.luxsoft.siipap.cxp.model.CXPAnticipo;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.CXPPago;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;

public class CXPAbonosManagerImpl extends HibernateDaoSupport implements CXPAbonosManager{
	
	private CXPPAgoDao pagoDao;
	private CXPNotaDao notaDao;

	public void eliminarPago(Long pagoId) {
		pagoDao.remove(pagoId);
	}
	
	public void eliminarNota(Long notaId) {
		notaDao.remove(notaId);
	}

	public CXPPago salvarPago(CXPPago pago) {
		return pagoDao.save(pago);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPAnticipo salvarAnticipo(CXPAnticipo anticipo) {
		return pagoDao.salvarAnticipo(anticipo);
	}

	public List<CXPAbono> buscarAbonos(final Periodo p){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery("from CXPAbono c where c.fecha between ? and ?")
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
			}
		});
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPPago aplicarPago(final Requisicion r){
		//Requisicion r=(Requisicion)getHibernateTemplate().load(Requisicion.class, requisicionId);
		getHibernateTemplate().lock(r, LockMode.READ);
		List<CXPPago> pagos=getHibernateTemplate().find("from CXPPago p where p.requisicion.id=?", r.getId());
		Assert.isTrue(pagos.isEmpty(),"La requisición ya se há aplicado");
		CXPPago pago=new CXPPago();
		pago.setProveedor(r.getProveedor());
		pago.setFecha(r.getFecha());
		pago.setRequisicion(r);
		
		for(RequisicionDe det:r.getPartidas()){
			CXPCargo cargo=det.getFacturaDeCompras();
			CXPAplicacion aplicacion=new CXPAplicacion();
			aplicacion.setAbono(pago);
			aplicacion.setCargo(cargo);
			aplicacion.setComentario("Aplicacion automatica");
			aplicacion.setImporte(det.getTotal().amount());
			aplicacion.setFecha(pago.getFecha());
			pago.agregarAplicacion(aplicacion);
		}
		
		return pagoDao.save(pago);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPAnticipo aplicarAnticipo(Requisicion r) {
		getHibernateTemplate().lock(r, LockMode.READ);
		List<CXPPago> pagos=getHibernateTemplate().find("from CXPPago p where p.requisicion.id=?", r.getId());
		Assert.isTrue(pagos.isEmpty(),"La requisición ya se há aplicado");
		CXPAnticipo anticipo=new CXPAnticipo();
		anticipo.setProveedor(r.getProveedor());
		anticipo.setFecha(r.getFecha());
		anticipo.setRequisicion(r);
		anticipo.setDocumento(r.getId().toString());
		anticipo.setMoneda(r.getMoneda());
		return pagoDao.salvarAnticipo(anticipo);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPNota salvarNota(CXPNota nota) {
		CXPNota res= getNotaDao().save(nota);
		return getNotaDao().get(res.getId());
	}
	
	public List<CXPAplicacion> buscarAplicaciones(CXPAbono abono) {
		return getHibernateTemplate().find("from CXPAplicacion a where a.abono.id=?", abono.getId());
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public CXPNota buscarNota(Long id) {
		CXPNota nota=getNotaDao().get(id);
		Hibernate.initialize(nota.getAplicaciones());
		return nota;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPAnticipo buscarAnticipo(Long id) {
		CXPAnticipo anticipo=(CXPAnticipo)getHibernateTemplate().get(CXPAnticipo.class, id);
		Hibernate.initialize(anticipo.getAplicaciones());				
		return anticipo;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPAbono registrarDiferencia(CXPAbono abono) {
		abono=(CXPAbono)getSession().load(CXPAbono.class, abono.getId());
		abono.setDiferencia(abono.getDisponible());
		abono.setDiferenciaFecha(new Date());
		return abono;
	}

	public CXPPAgoDao getPagoDao() {
		return pagoDao;
	}

	public void setPagoDao(CXPPAgoDao pagoDao) {
		this.pagoDao = pagoDao;
	}

	public CXPNotaDao getNotaDao() {
		return notaDao;
	}

	public void setNotaDao(CXPNotaDao notaDao) {
		this.notaDao = notaDao;
	}

	
	

}
