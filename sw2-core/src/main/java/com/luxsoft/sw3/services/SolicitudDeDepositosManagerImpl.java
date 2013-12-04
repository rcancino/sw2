package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.AutorizacionDeAbono;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.replica.EntityLog;
import com.luxsoft.sw3.tesoreria.dao.SolicitudDeDepositosDao;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;

/**
 * Implementacion de {@link SolicitudDeDepositosManager}
 * para la administración de solicitudes de depositos
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Service("solicitudDeDepositosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class SolicitudDeDepositosManagerImpl implements SolicitudDeDepositosManager{
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FolioDao folioDao;
	
	@Autowired
	private SolicitudDeDepositosDao solicitudDeDepositoDao;
	
	@Autowired
	private IngresosManager ingresosManager;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	protected Logger logger=Logger.getLogger(getClass());

	public boolean exists(String id) {
		return solicitudDeDepositoDao.exists(id);
	}

	@Transactional(propagation=Propagation.SUPPORTS)
	public SolicitudDeDeposito get(String id) {
		SolicitudDeDeposito sol= solicitudDeDepositoDao.get(id);
		hibernateTemplate.initialize(sol.getBancoOrigen());
		hibernateTemplate.initialize(sol.getCuentaDestino());
		hibernateTemplate.initialize(sol.getCliente());
		return sol;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeDeposito save(SolicitudDeDeposito sol) {
		if(sol.getId()==null){
			Folio folio=folioDao.buscarNextFolio(sol.getSucursal(), "SOLICITUD_DEPOSITO");
			sol.setDocumento(folio.getFolio());
			folioDao.save(folio);
		}
		registrarBitacora(sol);
		sol.setReplicado(null);
		sol.setImportado(null);
		sol=solicitudDeDepositoDao.save(sol);
		hibernateTemplate.initialize(sol.getBancoOrigen());
		hibernateTemplate.initialize(sol.getCuentaDestino());
		hibernateTemplate.initialize(sol.getCliente());
		/*try {
			if(sol.getSucursal().getId().intValue()==1){
				EntityLog log=new EntityLog(sol,sol.getId(),sol.getSucursal().getNombre(),EntityLog.Tipo.CAMBIO);
				jmsTemplate.convertAndSend("REPLICA.QUEUE."+sol.getSucursal().getNombre(), log);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		return sol;
	}
	
	private void registrarBitacora(SolicitudDeDeposito bean){
		Date time=new Date();
		
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
	}
	
	private void registrarBitacora(PagoConDeposito bean){
		Date time=new Date();
		String user=KernellSecurity.instance().getCurrentUserName();
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeDeposito autorizar(SolicitudDeDeposito sol) {
		Assert.isNull(sol.getPago(),"La solicitud ya ha sido autorizada");
		
		AutorizacionDeAbono autorizacion=new AutorizacionDeAbono();
		autorizacion.setAutorizo(KernellSecurity.instance().getCurrentUserName());
		autorizacion.setComentario("AUTORIZACION DE DEPOSITO");
		if(sol.isDuplicado())
			autorizacion.setComentario("AUTORIZACION DE DEPOSITO DUPLICADO");
		autorizacion.setFechaAutorizacion(new Date());
		autorizacion.setIpAdress(KernellSecurity.getIPAdress());
		autorizacion.setMacAdress(KernellSecurity.getMacAdress());
		
		Double tipoDeCambio=1.00;
		if(!sol.getCuentaDestino().getMoneda().toString().equals("MXN")){
			tipoDeCambio=buscarTipoDeCambio(DateUtils.addDays(sol.getFechaDeposito(),-1));
		}
		
		
		PagoConDeposito pago=new PagoConDeposito();
		pago.setCliente(sol.getCliente());
		pago.setSucursal(sol.getSucursal());
		pago.setAnticipo(sol.getAnticipo());
		pago.setAutorizacion(autorizacion);
		pago.setBanco(sol.getBancoOrigen().getClave());
		pago.setCheque(sol.getCheque());
		pago.setCuenta(sol.getCuentaDestino());
		pago.setMoneda(sol.getCuentaDestino().getMoneda());
		pago.setTc(tipoDeCambio);
		pago.setEfectivo(sol.getEfectivo());
		pago.setFecha(new Date());
		pago.setFechaDeposito(sol.getFechaDeposito());
		registrarBitacora(pago);
		pago.setOrigen(sol.getOrigen());
		pago.setLiberado(new Date());
		pago.setReferenciaBancaria(sol.getReferenciaBancaria());
		pago.setSolicito(sol.getSolicita());
		pago.setTransferencia(sol.getTransferencia());
		pago.setTotal(sol.getTotal());
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(pago.getTotal()));
		pago.actualizarImpuesto();
		pago.setFolio(sol.getDocumento().intValue());
		sol.setPago(pago);
		sol= save(sol);
		registrarIngreso(sol);
		sol.getBancoOrigen().getNombre();
		sol.getCuentaDestino().getDescripcion();
		return sol;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public SolicitudDeDeposito autorizar(SolicitudDeDeposito sol,AutorizacionDeAbono autorizacion){
		Assert.isNull(sol.getPago(),"La solicitud ya ha sido autorizada");
		if(sol.isDuplicado())
			autorizacion.setComentario("AUTORIZACION DE DEPOSITO DUPLICADO");
		PagoConDeposito pago=new PagoConDeposito();
		pago.setCliente(sol.getCliente());
		pago.setSucursal(sol.getSucursal());
		pago.setAnticipo(sol.getAnticipo());
		pago.setAutorizacion(autorizacion);
		pago.setBanco(sol.getBancoOrigen().getClave());
		pago.setCheque(sol.getCheque());
		pago.setCuenta(sol.getCuentaDestino());
		pago.setEfectivo(sol.getEfectivo());
		pago.setFecha(new Date());
		pago.setFechaDeposito(sol.getFechaDeposito());
		registrarBitacora(pago);
		pago.setOrigen(sol.getOrigen());
		pago.setLiberado(new Date());
		pago.setReferenciaBancaria(sol.getReferenciaBancaria());
		pago.setSolicito(sol.getSolicita());
		pago.setTransferencia(sol.getTransferencia());
		pago.setTotal(sol.getTotal());
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(pago.getTotal()));
		pago.actualizarImpuesto();
		pago.setFolio(sol.getDocumento().intValue());
		sol.setPago(pago);
		sol= save(sol);
		//registrarIngreso(sol);
		//sol.getBancoOrigen().getNombre();
		//sol.getCuentaDestino().getDescripcion();
		return sol;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	private void registrarIngreso(SolicitudDeDeposito sol){
		if(sol.getOrigen().equals(OrigenDeOperacion.CRE)
				||sol.getOrigen().equals(OrigenDeOperacion.JUR)
				||sol.getOrigen().equals(OrigenDeOperacion.CHE))
		{
			ingresosManager.registrarIngreso(sol.getPago());
		}
	}
	

	public SolicitudDeDeposito buscarDuplicada(final SolicitudDeDeposito sol) {
		final String hql="from SolicitudDeDeposito s left join fetch s.cuentaDestino  sc left join fetch s.bancoOrigen b" +
				" where s.total=?" +
				"  and s.fechaDeposito=? " +
				"  and s.bancoOrigen.id=? " +
				"  and s.cuentaDestino.id=?" +
				"  and s.id!=?";
		List<SolicitudDeDeposito> data=getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				return session.createQuery(hql)
				.setBigDecimal(0, sol.getTotal())
				.setParameter(1, sol.getFechaDeposito(),Hibernate.DATE)
				.setLong(2, sol.getBancoOrigen().getId())
				.setLong(3, sol.getCuentaDestino().getId())
				.setString(4, sol.getId())
				.list()
				;
			}
			
		});		
		return data.isEmpty()?null:data.get(0);
	}
	
	
	private double buscarTipoDeCambio(Date fecha){
		String hql="select t.factor from TipoDeCambio t where t.fecha=?";
		List<Double> res=getHibernateTemplate().find(hql,fecha);
		if(res==null) return 1d;
		if(res.isEmpty()) return 1d;
		return res.get(0);
	}
	

	public List<SolicitudDeDeposito> getAll() {
		throw new UnsupportedOperationException("No se deben requerir todas las solicitudes");
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(String id) {
		solicitudDeDepositoDao.remove(id);		
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public FolioDao getFolioDao() {
		return folioDao;
	}

	public void setFolioDao(FolioDao folioDao) {
		this.folioDao = folioDao;
	}

	public SolicitudDeDepositosDao getSolicitudDeDepositoDao() {
		return solicitudDeDepositoDao;
	}

	public void setSolicitudDeDepositoDao(
			SolicitudDeDepositosDao solicitudDeDepositoDao) {
		this.solicitudDeDepositoDao = solicitudDeDepositoDao;
	}

	public IngresosManager getIngresosManager() {
		return ingresosManager;
	}

	public void setIngresosManager(IngresosManager ingresosManager) {
		this.ingresosManager = ingresosManager;
	}
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

}
