package com.luxsoft.sw3.services;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoPorCambioDeCheque;
import com.luxsoft.siipap.cxc.model.PagoPorCambioDeTarjeta;
import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.dao.core.FolioDao;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Folio;
import com.luxsoft.siipap.model.tesoreria.Cuenta;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.caja.Caja;

@Service("corteDeCajaManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class CorteDeCajaManagerImpl implements CorteDeCajaManager{
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private DepositosManager depositosManager;
	
	@Autowired
	private FolioDao folioDao;
	
	protected Logger logger=Logger.getLogger(getClass());
	

	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCorteDeCaja(Caja caja) {
		
		Ficha ficha=new Ficha();
		String tipo="FICHAS";
		Folio folio=folioDao.buscarNextFolio(caja.getSucursal(), tipo);
		ficha.setFolio(folio.getFolio().intValue());
		caja.setFolio(ficha.getFolio());
		folioDao.save(folio);
		
		ficha.setSucursal(caja.getSucursal());
		ficha.setCuenta(getCuentaDestino());
		ficha.setFecha(caja.getFecha());
		ficha.setOrigen(caja.getOrigen());
		ficha.setTipoDeFicha(Ficha.FICHA_EFECTIVO);
		ficha.setTotal(caja.getDeposito());
		ficha.setComentario(caja.getComentario());
		registrarBitacora(caja,ficha);
		
		Ficha fichaRes=(Ficha)hibernateTemplate.merge(ficha);
		caja.setFicha(fichaRes.getId());
		Caja res=(Caja)hibernateTemplate.merge(caja);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCambioDeCheque(Caja source) {
		source.aplicar();
		source.setCorte(new Date());
		
		Caja cheque=new Caja();
		Caja efectivo=new Caja();
		
		BeanUtils.copyProperties(source, cheque);
		BeanUtils.copyProperties(cheque, efectivo,new String[]{"chequeNumero","chequeNombre","banco"});
		efectivo.setDeposito(cheque.getCaja().multiply(BigDecimal.valueOf(-1.0)));
		efectivo.setTipo(Caja.Tipo.EFECTIVO);
		efectivo.setDeposito(cheque.getDeposito().multiply(BigDecimal.valueOf(-1.0)));
		
		registrarBitacora(cheque,null);
		
		//Registrar el Abono
		PagoPorCambioDeCheque pago=new PagoPorCambioDeCheque();
		pago.setBanco(cheque.getBanco().getNombre());
		Cliente c=(Cliente)hibernateTemplate.get(Cliente.class, 8L);
		pago.setCliente(c);
		pago.setNombre(cheque.getChequeNombre());
		pago.setComentario("CAMBIO DE CHEQUE POR EFECTIVO");
		pago.setCuenta(getCuentaDestino());
		
		pago.setDiferencia(cheque.getDeposito());
		pago.setTotal(cheque.getDeposito());
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(pago.getTotal()));
		pago.actualizarImpuesto();
		pago.setFolio((int)cheque.getChequeNumero());
		pago.setDirefenciaFecha(source.getFecha());
		pago.setFecha(source.getFecha());
		pago.setOrigen(source.getOrigen());
		pago.setSucursal(source.getSucursal());
		pago.setPrimeraAplicacion(source.getFecha());
		registrarBitacora(efectivo,null,pago);
		
		pago=(PagoPorCambioDeCheque)hibernateTemplate.merge(pago);
		cheque=(Caja)hibernateTemplate.merge(cheque);
		cheque.setPagoCambioCheque(pago.getId());
		efectivo=(Caja)hibernateTemplate.merge(efectivo);
		return cheque;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCambioDeTarjeta(Caja source) {
		source.aplicar();
		source.setCorte(new Date());
		
		Caja tarjeta=new Caja();
		Caja efectivo=new Caja();
		String comentario=source.getComentario()+" CAMBIO DE TARJETA POR EFECTIVO Aut:"+source.getNumeroDeAutorizacion();
		BeanUtils.copyProperties(source, tarjeta);
		BeanUtils.copyProperties(tarjeta, efectivo);
		efectivo.setTipo(Caja.Tipo.EFECTIVO);
		efectivo.setDeposito(tarjeta.getDeposito().multiply(BigDecimal.valueOf(-1.0)));
		efectivo.setComentario(comentario);
		tarjeta.setComentario(comentario);
		registrarBitacora(tarjeta,null);
		
		//Registrar el Abono
		PagoPorCambioDeTarjeta pago=new PagoPorCambioDeTarjeta();
		Cliente c=(Cliente)hibernateTemplate.get(Cliente.class, 8L);
		pago.setCliente(c);
		pago.setNombre(c.getNombre());
		
		pago.setCuenta((Cuenta)hibernateTemplate.get(Cuenta.class, 151226L));
		pago.setDiferencia(tarjeta.getDeposito());
		pago.setTotal(tarjeta.getDeposito());
		pago.setImporte(MonedasUtils.calcularImporteDelTotal(pago.getTotal()));
		pago.actualizarImpuesto();
		pago.setFolio((int)tarjeta.getChequeNumero());
		pago.setDirefenciaFecha(source.getFecha());
		pago.setFecha(source.getFecha());
		pago.setOrigen(source.getOrigen());
		pago.setSucursal(source.getSucursal());
		pago.setPrimeraAplicacion(source.getFecha());
		pago.setTarjeta(source.getTarjeta());
		pago.setComentario(comentario);
		registrarBitacora(efectivo,null,pago);
		
		pago=(PagoPorCambioDeTarjeta)hibernateTemplate.merge(pago);
		tarjeta=(Caja)hibernateTemplate.merge(tarjeta);
		tarjeta.setPagoCambioCheque(pago.getId());
		efectivo=(Caja)hibernateTemplate.merge(efectivo);
		return tarjeta;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCorteDeCajaCheque(Caja caja,List<Ficha> fichas) {
		Assert.notNull(caja);
		Assert.isTrue(caja.getTipo().equals(Caja.Tipo.CHEQUE));
		Assert.isTrue(caja.getConcepto().equals(Caja.Concepto.CORTE_CAJA));
		registrarBitacora(caja, null);
		for(Ficha ficha:fichas){
			ficha.setOrigen(caja.getOrigen());
			ficha=depositosManager.save(ficha);			
		}
		Caja res=(Caja)hibernateTemplate.merge(caja);
		res.fichasParaCheques=fichas;	
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCorteDeCajaTarjeta(Caja caja){
		Assert.notNull(caja);
		Assert.isTrue(caja.getTipo().equals(Caja.Tipo.TARJETA));
		Assert.isTrue(caja.getConcepto().equals(Caja.Concepto.CORTE_CAJA));
		registrarBitacora(caja, null);
		Caja res=(Caja)hibernateTemplate.merge(caja);
		return res;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Caja registrarCorteDeCajaDeposito(Caja caja){
		Assert.notNull(caja);
		Assert.isTrue(caja.getTipo().equals(Caja.Tipo.DEPOSITO));
		Assert.isTrue(caja.getConcepto().equals(Caja.Concepto.CORTE_CAJA));
		registrarBitacora(caja, null);
		Caja res=(Caja)hibernateTemplate.merge(caja);
		return res;
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW)
	private void registrarBitacora(Caja bean,Ficha ficha){
		registrarBitacora(bean, ficha, null);
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	private void registrarBitacora(Caja bean,Ficha ficha,Pago pago){
		Date time=(Date)jdbcTemplate.queryForObject("select now()", Date.class);
		
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
		if(ficha!=null){
			ficha.getAddresLog().setCreatedIp(ip);
			ficha.getAddresLog().setUpdatedIp(ip);
			ficha.getAddresLog().setCreatedMac(mac);
			ficha.getAddresLog().setUpdatedMac(mac);
			ficha.setCreacion(time);
			ficha.setCreateUser(user);
			ficha.setUpdateUser(user);
			ficha.setModificado(time);
		}
		if(pago!=null){
			pago.getLog().setCreado(time);
			pago.getLog().setModificado(time);
			pago.getLog().setCreateUser(user);
			pago.getLog().setUpdateUser(user);
		}
	}
	
	private Cuenta getCuentaDestino(){
		return (Cuenta)hibernateTemplate.get(Cuenta.class, 151228L);
	}
	

}
