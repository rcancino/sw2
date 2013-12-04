package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.contabilidad.dao.PolizaDao;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;

import com.luxsoft.utils.LoggerHelper;

@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class PolizasManagerImpl implements PolizasManager{
	
	private PolizaDao polizaDao;
	
	private SaldoDeCuentasManager saldosManager;
	
	Logger logger=LoggerHelper.getLogger();
	
	public boolean existe(Poliza poliza) {
		String hql="from Poliza p where date(p.fecha)=? and p.clase=?";
		List<Poliza> res=getHibernateTemplate().find(hql,new Object[]{poliza.getFecha(),poliza.getClase()});
		return !res.isEmpty();
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Poliza salvarPoliza(Poliza poliza) {
		return doSalvarPoliza(poliza, obtenerFechaDelSistema());
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public Poliza doSalvarPoliza(Poliza poliza,Date time){
		 
		//Assert.isTrue(poliza.getCuadre().abs().doubleValue()<=3,"La poliza no cuadra no se puede salvar Cuadre: "+poliza.getCuadre());
		 if(poliza.getCuadre().abs().doubleValue()<=3){
			 BigDecimal cuadre=poliza.getCuadre();
			 if(cuadre.doubleValue()>0){
					generarPolizaDet(poliza, "702", "OING01", false, cuadre, "AJUSTE AUTOMATICO", "", "OFICINAS", "OTROS");
					poliza.actualizar();
				}else{
					generarPolizaDet(poliza, "704", "OGST01", true, cuadre.abs(), "AJUSTE AUTOMATICO", "", "OFICINAS", "OTROS");
					poliza.actualizar();
			}
		 }else{
			 BigDecimal cuadre=poliza.getCuadre();
			 if(cuadre.doubleValue()>0){
					generarPolizaDet(poliza, "800", "CUAD01", false, cuadre, "AJUSTE POR ACLARAR", "", "OFICINAS", "OTROS");
					poliza.actualizar();
				}else{
					generarPolizaDet(poliza, "800", "CUAD01", true, cuadre.abs(), "AJUSTE POR ACLARAR", "", "OFICINAS", "OTROS");
					poliza.actualizar();
			}
		 }
		 
		//Generacion de folio	
		if(poliza.getFolio()==null){
			String clase=poliza.getClase();
			if(clase.equals("PAGOS")){
				poliza.setFolio(polizaDao.buscarProximaPoliza(poliza.getYear(), poliza.getMes(), clase,poliza.getTipo().name()));
			}else{
				poliza.setFolio(polizaDao.buscarProximaPoliza(poliza.getYear(), poliza.getMes(), clase));
			}
			
		}
		//Bitacora
		registrarBitacora(poliza,time);
		//Assert.isTrue(ValidationUtils.isValid(poliza),ValidationUtils.validar(poliza));
		poliza=polizaDao.save(poliza);
		try {
			//actualizarSaldos(poliza);
		} catch (Exception e) {
			logger.error("Error actualizando saldos: "+ExceptionUtils.getRootCauseMessage(e),e);
		}
		
		return poliza;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private void actualizarSaldos(final Poliza poliza){
		logger.debug("Solicitando actualizar saldos para cuentas en poliza: "+poliza.getId());
		List<CuentaContable> cuentas=getHibernateTemplate().find("select d.cuenta from PolizaDet d where d.poliza.id=?",poliza.getId());
		for(CuentaContable c:cuentas){
			getSaldosManager().recalcularSaldo(c, poliza.getYear(), poliza.getMes());
		}
		/*
		Runnable runner=new Runnable() {
			public void run() {
				
			}
		};
		Thread t=new Thread(runner);
		t.start();*/
	}
	
	public Poliza cancelarPoliza(Poliza poliza) {
		return doCancelarPoliza(poliza, obtenerFechaDelSistema());
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private Poliza doCancelarPoliza(Poliza poliza,Date time){
		poliza.getPartidas().clear();		
		String pattern="CANCELADA :{0,date,short}";
		poliza.setDescripcion(MessageFormat.format(pattern, new Date()));
		registrarBitacora(poliza,time);
		poliza=polizaDao.save(poliza);
		return poliza;
	}
	
	public PolizaDao getPolizaDao() {
		return polizaDao;
	}

	public void setPolizaDao(PolizaDao polizaDao) {
		this.polizaDao = polizaDao;
	}
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(Poliza bean,Date time){
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
			bean.getAddresLog().setUpdatedMac(mac);
		}
		
	}

	public SaldoDeCuentasManager getSaldosManager() {
		return saldosManager;
	}

	public void setSaldosManager(SaldoDeCuentasManager saldosManager) {
		this.saldosManager = saldosManager;
	}
	

	/**
	 * Metodo generico para simplificar la creacion de instancias de PolizaDet
	 * @param poliza 
	 * @param cuenta
	 * @param concepto
	 * @param cargo
	 * @param importe
	 * @param desc2
	 * @param ref1
	 * @param ref2
	 * @param asiento
	 * @return
	 */
	public  PolizaDet generarPolizaDet(Poliza poliza,String cuenta,String concepto,boolean cargo,BigDecimal importe,String desc2
			,String ref1,String ref2,String asiento){
		PolizaDet det=poliza.agregarPartida();
		det.setDescripcion2(desc2);
		det.setReferencia(ref1);
		det.setReferencia2(ref2);
		det.setAsiento(asiento);
		if(cargo)
			det.setDebe(importe);
		else
			det.setHaber(importe);
		det.setCuenta(buscarPorClave(cuenta));
		det.setConcepto(det.getCuenta().getConcepto(concepto));
		return det;
	}
	
	private CuentaContable buscarPorClave(String clave){
		List<CuentaContable> data=getHibernateTemplate()
				.find("from CuentaContable c left join fetch c.conceptos cc where c.clave=?",clave);
		Assert.isTrue(!data.isEmpty(),"No existe la cuenta con la clave: "+clave);
		return data.get(0);
	}

}


