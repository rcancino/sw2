package com.luxsoft.sw3.contabilidad.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuenta;
import com.luxsoft.sw3.contabilidad.model.SaldoDeCuentaPorConcepto;
import com.luxsoft.utils.LoggerHelper;

@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class SaldoDeCuentasManagerImpl implements SaldoDeCuentasManager{
	
	protected Logger logger=LoggerHelper.getLogger();
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public SaldoDeCuenta getSaldo(CuentaContable cuenta, int year, int mes) {
		List<SaldoDeCuenta> saldos=getHibernateTemplate()
				.find("from SaldoDeCuenta s left join fetch s.conceptos c " +
						" where s.cuenta.id=? and s.year=? and s.mes=?"
						,new Object[]{cuenta.getId(),year,mes});
				SaldoDeCuenta saldo=null; 
		if(saldos.isEmpty()){
			saldo=new SaldoDeCuenta();
			saldo.setCuenta(cuenta);
			saldo.setYear(year);
			saldo.setMes(mes);
		}else
			saldo=saldos.get(0);
		//Verificar la existencia de saldos por concepto
		for(ConceptoContable concepto:cuenta.getConceptos()){
			SaldoDeCuentaPorConcepto saldoPorConcepto=saldo.getSaldoPorConcepto(concepto);
			if(saldoPorConcepto==null){
				saldoPorConcepto=new SaldoDeCuentaPorConcepto();
				saldoPorConcepto.setConcepto(concepto);
				saldoPorConcepto.setSaldo(saldo);
				saldoPorConcepto.setMes(saldo.getMes());
				saldoPorConcepto.setYear(saldo.getYear());
				saldo.getConceptos().add(saldoPorConcepto);
			}			
		}
		return saldo;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public SaldoDeCuenta recalcularSaldo(CuentaContable cuenta, final int year,final int mes) {
		cuenta=(CuentaContable)getHibernateTemplate().load(CuentaContable.class, cuenta.getId());
		logger.debug("Actualizando saldos para cuenta: "+cuenta+ " Year: "+year+ " Mes: "+mes);
		SaldoDeCuenta saldo=getSaldo(cuenta, year, mes);
		
		
		
		for(ConceptoContable concepto:saldo.getCuenta().getConceptos()){			
			
			Object params[]={concepto.getId(),year,mes};
			
			BigDecimal saldosaldoInicial=BigDecimal.ZERO;
			
			int mesAnterior=mes==1?13:mes-1;
			int yearAnterior=mes==1?year-1:year;
			String hql="from SaldoDeCuentaPorConcepto s where s.concepto.id=? and s.year=? and s.mes=?";
			List<SaldoDeCuentaPorConcepto> res=getHibernateTemplate()
					.find(hql, new Object[]{concepto.getId(),yearAnterior,mesAnterior});
			if(!res.isEmpty())
				saldosaldoInicial=res.get(0).getSaldoFinal();
			
			BigDecimal cargos=(BigDecimal)getHibernateTemplate().find("select sum(p.debe) from PolizaDet p " +
					" where p.concepto.id=? " +
					"  and year(p.poliza.fecha)=?" +
					"  and month(p.poliza.fecha)=? "
					+"  and p.poliza.referencia not in('CIERRE_ANUAL','CANCELACION_IETU')"				
					,params)
					.iterator().next();
			if(cargos==null)cargos=BigDecimal.ZERO;
			
			BigDecimal abonos=(BigDecimal)getHibernateTemplate().find("select sum(p.haber) from PolizaDet p " +
					" where p.concepto.id=? " +
					"  and year(p.poliza.fecha)=?" +
					"  and month(p.poliza.fecha)=? " 
					+"  and p.poliza.referencia not in('CIERRE_ANUAL','CANCELACION_IETU')"					
					,params)
					.iterator().next();
			if(abonos==null)abonos=BigDecimal.ZERO;			
			
			final SaldoDeCuentaPorConcepto saldoPorConcepto=saldo.getSaldoPorConcepto(concepto);
			saldoPorConcepto.setSaldoInicial(saldosaldoInicial);
			saldoPorConcepto.setDebe(cargos);
			saldoPorConcepto.setHaber(abonos);
			saldoPorConcepto.actualizar();
		}
		saldo.actualizar();
		saldo=(SaldoDeCuenta)getHibernateTemplate().merge(saldo);
		if(logger.isDebugEnabled()){
			logger.debug("Saldo actualizado: "+saldo);
		}
		return saldo;
	}
	
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public BigDecimal findSaldoPorConceptoAnterior(ConceptoContable c,int year,final int mes){
		int mesAnterior=mes-1;
		if(mes==1){
			year=year-1;
			mesAnterior=13;
		}
		String hql="from SaldoDeCuentaPorConcepto s where s.concepto.id=? and s.year=? and s.mes=?";
		List<SaldoDeCuentaPorConcepto> res=getHibernateTemplate()
				.find(hql, new Object[]{c.getId(),year,mesAnterior});
		if(res.isEmpty())
			return BigDecimal.ZERO;
		
		BigDecimal saldoAnteriorFinal= res.get(0).getSaldoFinal();
		
		return saldoAnteriorFinal;
	}
	

	@Transactional(propagation=Propagation.REQUIRED)
	public void recalcularSaldos(final int year,final int mes) {
		logger.info("Actualizando saldos contables en: "+year+" / "+mes);
		List<CuentaContable> cuentas=getHibernateTemplate().find("from CuentaContable c ");
		for(CuentaContable c:cuentas){
			try {
				recalcularSaldo(c, year, mes);
			} catch (Exception e) {
				logger.error("No se puede recalcular cuenta: "+c,e);
				e.printStackTrace();
			}
		}
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
	
	public static void main(String[] args) {
		/*
		List data=ServiceLocator2.getHibernateTemplate().find("from SaldoDeCuentaPorConcepto s where s.concepto.id=? and s.year=? and s.mes=?"
				,new Object[]{570L,2011,12});
		for(Object o:data){
			System.out.println(o);
		}*/
		//CuentaContable cuenta=ServiceLocator2.getCuentasContablesManager().buscarPorClave("114");
		//BigDecimal valor=ServiceLocator2.getSaldoDeCuentaManager().findSaldoPorConceptoAnterior(cuenta.getConcepto("M030455"), 2012, 1);
		//System.out.println("Saldo ini: "+valor);
		
		CuentaContable cuenta=ServiceLocator2.getCuentasContablesManager().buscarPorClave("205");
		ServiceLocator2.getSaldoDeCuentaManager().recalcularSaldo(cuenta, 2012, 2);
		/*
		int year=2012;
		for(int mes=2;mes<=2;mes++){
			Object[] params=new Object[]{year,mes};
			ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_CONTABILIDAD_SALDOSDET WHERE YEAR=? AND  MES=?",params);
			ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_CONTABILIDAD_SALDOS WHERE YEAR=? AND  MES=?",params);
			ServiceLocator2.getSaldoDeCuentaManager().recalcularSaldos(year, mes);
		}*/
		//int mes=3;
		
	}
	
}
