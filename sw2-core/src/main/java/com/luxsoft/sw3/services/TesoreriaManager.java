package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.sw3.tesoreria.model.Clasificacion;
import com.luxsoft.sw3.tesoreria.model.ComisionBancaria;
import com.luxsoft.sw3.tesoreria.model.Inversion;
import com.luxsoft.sw3.tesoreria.model.SaldoDeCuentaBancaria;
import com.luxsoft.sw3.tesoreria.model.TraspasoDeCuenta;
import com.luxsoft.utils.LoggerHelper;

@Service("tesoreriaManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class TesoreriaManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	Logger logger=LoggerHelper.getLogger();
	
	@Transactional(propagation=Propagation.REQUIRED)
	public TraspasoDeCuenta salvar(TraspasoDeCuenta t){		
		if(t.getId()==null){
			t.setMoneda(t.getCuentaOrigen().getMoneda());		
			Assert.isTrue(t.getMoneda().equals(t.getCuentaDestino().getMoneda()),"Las cuentas deben ser de la misma moneda");
			
			//Retiro
			t.agregarMovimiento(t.getCuentaOrigen()
					, t.getImporte().multiply(BigDecimal.valueOf(-1))
					, Clasificacion.RETIRO
					, t.getFecha()
					, "Traspaso a: "+t.getCuentaDestino().getCuentaDesc()
					);
			
			
			//Deposito
			t.agregarMovimiento(t.getCuentaDestino()
					, t.getImporte()
					, Clasificacion.DEPOSITO
					, t.getFecha()
					, "Traspaso de : "+t.getCuentaOrigen().getCuentaDesc()
					);
			
			if(t.getComision().doubleValue()>0){
				//Comision
				t.agregarMovimiento(t.getCuentaOrigen()
						, t.getComision().multiply(BigDecimal.valueOf(-1))
						, Clasificacion.COMISION
						, t.getFecha()
						, "Comisión traspaso a: "+t.getCuentaDestino().getCuentaDesc()
						);
			}
			
			if(t.getImpuesto().doubleValue()>0){
				//Impuesto
				t.agregarMovimiento(t.getCuentaOrigen()
						, t.getImpuesto().multiply(BigDecimal.valueOf(-1))
						, Clasificacion.IMPUESTO_POR_TRASPASO
						, t.getFecha()
						, "Impuesto traspaso a: "+t.getCuentaDestino().getCuentaDesc()
						);
				
			}
		}		
		t=(TraspasoDeCuenta)hibernateTemplate.merge(t);
		return t;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Inversion salvar(Inversion t){
		if(t.getId()==null){
			t.setMoneda(t.getCuentaOrigen().getMoneda());		
			Assert.isTrue(t.getMoneda().equals(t.getCuentaDestino().getMoneda()),"Las cuentas deben ser de la misma moneda");
			
			//Retiro
			t.agregarMovimiento(t.getCuentaOrigen()
					, t.getImporte().multiply(BigDecimal.valueOf(-1))
					, Clasificacion.RETIRO
					, t.getFecha()
					, "Inversión a: "+t.getCuentaDestino().getCuentaDesc()+" Tasa:"+t.getTasa()
					);
			
			
			//Deposito
			BigDecimal deposito=t.getImporte().add(t.getRendimientoCalculado());
			t.agregarMovimiento(t.getCuentaDestino()
					, deposito
					, Clasificacion.DEPOSITO
					, t.getFecha()
					, "Inversión de : "+t.getCuentaOrigen().getCuentaDesc()+" Tasa:"+t.getTasa()
					);
			t.setRendimientoReal(t.getRendimientoCalculado());
			
		}		
		t=(Inversion)hibernateTemplate.merge(t);
		return t;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Inversion registrarRegresoDeInversion(Inversion i){
		
		CargoAbono retiro=(CargoAbono) CollectionUtils.find(i.getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), Clasificacion.INTERESES.name());
			}
		});
		Assert.isNull(retiro,"Ya se regreso la inversión: "+i.getId());
		CargoAbono inversionGenerada=(CargoAbono) CollectionUtils.find(i.getMovimientos(), new Predicate() {
			public boolean evaluate(Object object) {
				CargoAbono ca=(CargoAbono)object;
				return StringUtils.equals(ca.getClasificacion(), Clasificacion.DEPOSITO.name());
			}
		});
		Assert.notNull(inversionGenerada,"No existe el deposito de la inversión con intereses: "+i.getId());
		//Retiro
		retiro=i.agregarMovimiento(i.getCuentaDestino()
				, inversionGenerada.getImporte().multiply(BigDecimal.valueOf(-1))
				, Clasificacion.RETIRO_POR_INVERSION
				, i.getVencimiento()
				, "Inversión a: "+i.getCuentaDestino().getCuentaDesc()+" Tasa:"+i.getTasa()
				);
		retiro.setReferencia(i.getId().toString());
		
		//Deposito		
		CargoAbono deposito=i.agregarMovimiento(i.getCuentaOrigen()
				, inversionGenerada.getImporte()
				, Clasificacion.DEPOSITO_POR_INVERSION
				, i.getVencimiento()
				, "Inversión de : "+i.getCuentaOrigen().getCuentaDesc()+" Tasa:"+i.getTasa()
				);		
		deposito.setReferencia(i.getId().toString());
		i=(Inversion)hibernateTemplate.merge(i);
		return i;
	}
	
	public Inversion buscarInversion(Long id) {
		String hql="from Inversion i left join fetch i.movimientos m where i.id=?";
		List<Inversion> res=getHibernateTemplate().find(hql,id);
		return res.isEmpty()?null:res.get(0);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public ComisionBancaria salvar(ComisionBancaria t) {
		CantidadMonetaria importe=CantidadMonetaria.pesos(t.getComision()).multiply(-1);
		CargoAbono comision=t.agregarMovimiento(t.getCuenta(), importe.amount(), Clasificacion.COMISION.name(), t.getFecha()
				, Clasificacion.COMISION.name()+" "+StringUtils.trim(t.getComentario())
				);
		t.setComisionId(comision);
		CantidadMonetaria impuesto=CantidadMonetaria.pesos(t.getImpuesto()).multiply(-1);
		if(impuesto.abs().amount().doubleValue()>0){
			CargoAbono iva=t.agregarMovimiento(t.getCuenta(), impuesto.amount(), Clasificacion.IVA_COMISION_BANCARIA.name(), t.getFecha()
					, Clasificacion.IVA_COMISION_BANCARIA.name());
			t.setImpuestoId(iva);
		}		
		t.setTc(buscarTipoDeCambio(DateUtils.addDays(t.getFecha(), -1)));
		ComisionBancaria res=(ComisionBancaria)getHibernateTemplate().merge(t);
		return res;
	}
	
	private double buscarTipoDeCambio(Date fecha){
		String hql="select t.factor from TipoDeCambio t where t.fecha=?";
		List<Double> res=getHibernateTemplate().find(hql,fecha);
		if(res==null) return 1d;
		if(res.isEmpty()) return 1d;
		return res.get(0);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Cuenta> buscarCuentas(){
		return getHibernateTemplate().find("from Cuenta c ");
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public SaldoDeCuentaBancaria actualizarSaldo(Cuenta cuenta,int year, int mes){
		
		SaldoDeCuentaBancaria saldo=buscarSaldo(cuenta, year, mes);
		if(saldo==null){
			saldo=new SaldoDeCuentaBancaria();
			saldo.setCuenta(cuenta);
			saldo.setMes(mes);
			saldo.setYear(year);
		}
		int mesAnterior=mes-1;
		if(mes==1)
			mesAnterior=12; //Ajuste para enero
		SaldoDeCuentaBancaria saldoInicial=buscarSaldo(cuenta, year-1, mesAnterior);
		if(saldoInicial!=null){
			saldo.setSaldoInicial(saldoInicial.getSaldoFinal());
		}
		actualizarMovimientos(saldo);
		saldo.actualizar();
		saldo=(SaldoDeCuentaBancaria)getHibernateTemplate().merge(saldo);
		return saldo;
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public SaldoDeCuentaBancaria buscarSaldo(Cuenta cuenta,int year, int mes){
		List<SaldoDeCuentaBancaria> saldos=getHibernateTemplate()
				.find("from SaldoDeCuentaBancaria s " +
						" where s.cuenta.id=? and s.year=? and s.mes=?"
						,new Object[]{cuenta.getId(),year,mes});
		return saldos.isEmpty()?null:saldos.get(0);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public void actualizarMovimientos(SaldoDeCuentaBancaria saldo){
		BigDecimal depositos=(BigDecimal)getHibernateTemplate().find("select sum(c.importe) from CargoAbono c " +
				" where c.cuenta.id=? " +
				" and year(c.fecha)=?" +
				" and month(c.fecha)=?" +
				" and c.importe>0" +
				" and c.conciliado=false",
				new Object[]{saldo.getCuenta().getId(),saldo.getYear(),saldo.getMes()}
				).iterator().next();
		BigDecimal retiros=(BigDecimal)getHibernateTemplate().find("select sum(c.importe) from CargoAbono c " +
				" where c.cuenta.id=? " +
				" and year(c.fecha)=?" +
				" and month(c.fecha)=?" +
				" and c.importe<0" +
				" and c.conciliado=false",
				new Object[]{saldo.getCuenta().getId(),saldo.getYear(),saldo.getMes()}
				).iterator().next();
		if(depositos==null)depositos=BigDecimal.ZERO;
		if(retiros==null)retiros=BigDecimal.ZERO;
		saldo.setDepositos(depositos);
		saldo.setRetiros(retiros.abs());
		
	}


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
