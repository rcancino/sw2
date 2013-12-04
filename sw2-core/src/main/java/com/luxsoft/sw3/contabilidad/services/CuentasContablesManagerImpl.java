package com.luxsoft.sw3.contabilidad.services;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.sw3.contabilidad.dao.CuentaContableDao;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;


@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CuentasContablesManagerImpl implements CuentasContablesManager{
	
	@Autowired
	private CuentaContableDao cuentaContableDao;

	@Transactional(propagation=Propagation.REQUIRED)
	public CuentaContable salvar(CuentaContable cuenta) {
		Date time=obtenerFechaDelSistema();
		return doSalvar(cuenta, time);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	private CuentaContable doSalvar(CuentaContable cuenta,Date time){
		registrarBitacora(cuenta, time);
		cuenta=cuentaContableDao.save(cuenta);
		return cuenta;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminarCuentaContable(CuentaContable cuenta){
		//cuentaContableDao.remove(cuenta.getId());
	}

	public CuentaContableDao getCuentaContableDao() {
		return cuentaContableDao;
	}

	public void setCuentaContableDao(CuentaContableDao cuentaContableDao) {
		this.cuentaContableDao = cuentaContableDao;
	}
	
	public synchronized Date obtenerFechaDelSistema(){
		return (Date)getJdbcTemplate().queryForObject("select now()", Date.class);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS,readOnly=false)
	private void registrarBitacora(CuentaContable bean,Date time){
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
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.contabilidad.services.CuentasContablesManager#buscarPorClave(java.lang.String)
	 */
	public CuentaContable buscarPorClave(String clave){
		List<CuentaContable> data=getHibernateTemplate()
				.find("from CuentaContable c left join fetch c.conceptos cc where c.clave=?",clave);
		Assert.isTrue(!data.isEmpty(),"No existe la cuenta con la clave: "+clave);
		return data.get(0);
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

}
