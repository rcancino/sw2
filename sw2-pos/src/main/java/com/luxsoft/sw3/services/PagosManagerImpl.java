package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.AplicacionDePago;
import com.luxsoft.siipap.cxc.model.CancelacionDeAbono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.model.PagoDeDiferencias;
import com.luxsoft.siipap.dao.UniversalDao;
import com.luxsoft.siipap.model.Autorizacion2;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.model.Venta;

@Service("pagosManager")
public class PagosManagerImpl extends HibernateDaoSupport implements PagosManager{
	
	@Autowired
	private UniversalDao universalDao;
	
	@Autowired
	private VentaDao ventaDao;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	

	@Transactional(propagation=Propagation.REQUIRED)
	public Pago cancelarPago(String id, Autorizacion2 aut,final Date fecha) {
		Assert.notNull(aut,"Se requiere autorizacion para esta cancelacion");		
		Pago pago=(Pago)getAbono(id);
		Assert.isTrue(pago.getAplicaciones().isEmpty(),"El pago tiene aplicaciones no se puede cancelar");
		Assert.isNull( pago.getDeposito(),"El pago ya ha sido depositado en el banco: "+pago.getDeposito());
		
		CancelacionDeAbono c=new CancelacionDeAbono(pago);
		c.setAutorizacion(aut);
		c.setAbono(pago);
		c.setFecha(fecha);
		return (Pago)universalDao.save(pago);
		
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Abono getAbono(String id) {
		Abono pago=(Abono)universalDao.get(Abono.class, id);
		getHibernateTemplate().initialize(pago.getAplicaciones());
		return pago;
	}

	public boolean isCancelable(Pago pago) {
		if(!pago.isCancelado())
			return isModificable(pago);
		return false;
	}

	public boolean isModificable(Pago pago) {
		return (!pago.isCancelado() 
				&& pago.getAplicado().doubleValue()==0
				);
	}

	/**
	 * Verifica q no exista un deposito con los mismos siguientes datos:
	 * 
	 * Fecha del deposito
	 * Banco emisor
	 * Cuenta destino
	 * Importe
	 * Clave del cliente
	 * 
	 *  Regresa verdadero si el deposito existe
	 */
	public boolean verificarExistenciaDeDeposito(PagoConDeposito deposito) {
		final Date fecha=deposito.getFechaDeposito();
		final String banco=deposito.getBanco();
		final Long ctaId=deposito.getCuenta().getId();
		final BigDecimal total=deposito.getTotal();
		final String claveCliente=deposito.getCliente().getClave();
		final String hql="from PagoConDeposito d " +
				" where d.fechaDeposito=?" +
				"  and d.cliente.clave=?" +
				"  and d.banco=?" +
				"  and d.cuenta.id=?" +
				"  and d.total=?";
		return (Boolean)getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<PagoConDeposito> pagos=session.createQuery(hql)
				.setParameter(0, fecha,Hibernate.DATE)
				.setString(1,claveCliente)
				.setString(2, banco)
				.setLong(3,ctaId)
				.setBigDecimal(4, total)
				.list();
				return pagos.isEmpty()?Boolean.FALSE:Boolean.TRUE;
			}
			
		});
		
		
	}	

	@Transactional(propagation=Propagation.REQUIRED)
	public Pago salvar(Pago pago) {
		if( (pago instanceof PagoConTarjeta) 
				||( pago instanceof PagoConCheque )
				||( pago instanceof PagoConEfectivo)
				){
			
			pago.setLiberado(pago.getFecha());
		}
		return (Pago)universalDao.save(pago);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void cobrarFactura(final Venta venta, final Abono pago,final Date fecha) {
		if(venta.getSaldoCalculado().doubleValue()<=0)
			return;
		CantidadMonetaria saldo=venta.getSaldoCalculadoCM();
		CantidadMonetaria disponible=pago.getDisponibleCM();
		if(disponible.compareTo(saldo)>0){
			AplicacionDePago a=new AplicacionDePago();
			a.setCargo(venta);				
			a.setFecha(fecha);
			a.setImporte(saldo.amount());			
			pago.agregarAplicacion(a);
			a.actualizarDetalle();
			disponible=disponible.subtract(saldo);
			if(disponible.amount().doubleValue()<=10.00d){
				pago.setDiferencia(disponible.amount());
				pago.setDirefenciaFecha(fecha!=null?fecha:new Date());
			}
			pago.setImportado(null);
			getHibernateTemplate().merge(pago);
			
		}else{
			AplicacionDePago a=new AplicacionDePago();
			a.setCargo(venta);				
			a.setFecha(fecha);
			a.setImporte(disponible.amount());			
			pago.agregarAplicacion(a);
			a.actualizarDetalle();
			pago.setImportado(null);
			getHibernateTemplate().merge(pago);
		}
		
	}
	

	public VentaDao getVentaDao() {
		return ventaDao;
	}
	
	
	
}
