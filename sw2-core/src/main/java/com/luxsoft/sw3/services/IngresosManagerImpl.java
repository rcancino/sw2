package com.luxsoft.sw3.services;


import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.cxc.model.PagoConTarjeta;
import com.luxsoft.siipap.cxc.service.DepositosManager;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Origen;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.tesoreria.dao.CorteDeTarjetaDao;
import com.luxsoft.sw3.tesoreria.model.CargoAbonoPorCorte;
import com.luxsoft.sw3.tesoreria.model.CorreccionDeFicha;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjetaDet;
import com.luxsoft.sw3.tesoreria.model.TipoDeAplicacion;

@Service("ingresosManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class IngresosManagerImpl implements IngresosManager{
	
	@Autowired
	private CorteDeTarjetaDao corteDeTarjetaDao;
	
	private DepositosManager depositosManager;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	private JdbcTemplate jdbcTemplate;
	
	protected Logger logger=Logger.getLogger(getClass());

	@Transactional(propagation=Propagation.REQUIRED)
	public CorteDeTarjeta registrarCorte(CorteDeTarjeta corte) {
		Assert.state(corte.getAplicaciones().isEmpty(),"Afecaciones a bancos (CargoAbono) ya realizadas");
		registrarBitacora(corte);
		registrarIngresosPorTarjeta(corte);
		Comparator<CargoAbonoPorCorte> c=GlazedLists.beanPropertyComparator(CargoAbonoPorCorte.class, "orden");
		SortedList<CargoAbonoPorCorte> cas=new SortedList<CargoAbonoPorCorte>(GlazedLists.eventList(corte.getAplicaciones()),c);
		for(CargoAbonoPorCorte ca:cas){
			this.hibernateTemplate.save(ca.getCargoAbono());
		}
		//hibernateTemplate.save(corte.getAp)
		return corteDeTarjetaDao.save(corte);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CorteDeTarjeta actualizarCorte(CorteDeTarjeta corte){
		Assert.notEmpty(corte.getAplicaciones(),"Afecaciones a bancos (CargoAbono) sin realizadas");
		//registrarBitacora(corte);
		for(CargoAbonoPorCorte ca:corte.getAplicaciones()){
			if(ca.getTipo().equals(TipoDeAplicacion.INGRESO))
				continue;
			registrarBitacora(ca.getCargoAbono());
			this.hibernateTemplate.update(ca.getCargoAbono());
		}
		return corteDeTarjetaDao.get(corte.getId());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.services.IngresosManager#eliminarCorte(java.lang.String)
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminarCorte(Long id){
		//CorteDeTarjeta corte=getCorteDeTarjetaDao().get(id);
		getCorteDeTarjetaDao().remove(id);
	}
	
	private void registrarBitacora(CorteDeTarjeta bean){
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
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminarCorte(CorteDeTarjeta corte) {
		for(CargoAbonoPorCorte ca:corte.getAplicaciones()){
			getHibernateTemplate().delete(ca.getCargoAbono());
		}
		getHibernateTemplate().delete(corte);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void registrarIngresoPorFichaEfectivo(Date fecha,Sucursal sucursal){
		
		System.out.println("Buscando fichas para el dia "+fecha + " de la sucursal"+ sucursal);
		
		Object[] values={sucursal.getId(),fecha};
		String hqlParc="from Ficha f where   " +
				" f.corte is null and f.cancelada is null " +
				" and f.origen in(\'MOS\',\'CAM\') and f.cierre is false and f.tipoDeFicha=\'EFECTIVO\' and f.sucursal.id=? and f.fechaDep=?";
		
		String hqlCie="from Ficha f where   " +
				" f.corte is null and f.cancelada is null " +
				" and f.origen in(\'MOS\',\'CAM\') and cierre is true and f.tipoDeFicha=\'EFECTIVO\' and f.sucursal.id=? and f.fecha=?";

		List<Ficha> fichasParciales=ServiceLocator2.getHibernateTemplate().find(hqlParc, values);
		List<Ficha> fichasCierre=ServiceLocator2.getHibernateTemplate().find(hqlCie, values);
		
		if(!fichasParciales.isEmpty()){
			Ficha ficha1=fichasParciales.get(0);
			BigDecimal totalParcial=BigDecimal.ZERO;
			for(Ficha f:fichasParciales){
				System.out.println("Ficha Parcial id "+f.getId() +" Maestro Parcial   "+ficha1);
				
				totalParcial=totalParcial.add(f.getTotal());
			}
			crearCargoAbonoFichaEfectivo(ficha1,fichasParciales, totalParcial);
		}
		
		if(!fichasCierre.isEmpty()){
			Ficha ficha2=fichasCierre.get(0);
			BigDecimal totalCierre=BigDecimal.ZERO;
			for(Ficha f:fichasCierre){
				System.out.println("Ficha Cierre id "+f.getId()+" Maestro Cierre  "+ ficha2);
				totalCierre=totalCierre.add(f.getTotal());
			}
			crearCargoAbonoFichaEfectivo(ficha2,fichasCierre, totalCierre);
		}
		
		
		
	}
	
	//@Transactional(propagation=Propagation.REQUIRED)
	public void crearCargoAbonoFichaEfectivo(Ficha ficha,List<Ficha> fichas,BigDecimal total) {
		
	
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(ficha.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(total);
		cargo.setCuenta(ficha.getCuenta());
		cargo.setFecha(ficha.getFecha());
		cargo.setMoneda(ficha.getCuenta().getMoneda());
		cargo.setSucursal(ficha.getSucursal());
		cargo.setEncriptado(false);
		cargo.setFormaDePago(FormaDePago.EFECTIVO);
		String pattern="Deposito en efectivo {0,date,short} sucursal: {1}";
		cargo.setComentario(MessageFormat.format(pattern, ficha.getCorte(),cargo.getSucursal()));		
		cargo.setReferencia("Ficha: "+ficha.getFolio());
		cargo.setOrigen(Origen.VENTA_MOSTRADOR);
		registrarBitacora(cargo);
		System.out.println("Cargo generado"+cargo);
		getHibernateTemplate().save(cargo);
		
		for(Ficha f:fichas){
			System.out.println("Cargo generado Finalmente"+f);
			System.out.println("Grabando cargo abono en ficha"+ f +" Cargo "+cargo);
			f.setIngreso(cargo);
			f.setCorte(new Date());
			
		//	getHibernateTemplate().merge(f);
			//getDepositosManager().save(f);
			
		}
		
	
	}
	
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Ficha registrarIngresoPorFicha(Ficha ficha) {
		
		ficha=(Ficha)getHibernateTemplate().get(Ficha.class, ficha.getId());
		Assert.isNull(ficha.getCorte(),"La ficha ya se registro en bancos :"+ficha.getCorte());
		Assert.isTrue(ficha.getTotal().doubleValue()>0,"El importe a registrar no es correcto");
		ficha.setCorte(new Date());
		Origen origen=null;
		switch (ficha.getOrigen()) {
		case CRE:
			origen=Origen.VENTA_CREDITO;
			break;
		case CAM:
			origen=Origen.VENTA_CAMIONETA;
			break;
		case CHE:
			origen=Origen.CHE;
			break;
		case JUR:
			origen=Origen.JUR;
			break;
		case MOS:
			origen=Origen.VENTA_MOSTRADOR;
			break;
		default:
			origen=Origen.VENTA_CONTADO;
		}
		//Caso 1 Ficha en efectivo
		if(ficha.getTipoDeFicha().endsWith(Ficha.FICHA_EFECTIVO)){
			CargoAbono cargo=new CargoAbono();
			cargo.setAFavor(ficha.getCuenta().getBanco().getEmpresa().getNombre());
			cargo.setImporte(ficha.getTotal());
			cargo.setCuenta(ficha.getCuenta());
			cargo.setFecha(ficha.getFecha());
			cargo.setMoneda(ficha.getCuenta().getMoneda());
			cargo.setSucursal(ficha.getSucursal());
			cargo.setEncriptado(false);
			cargo.setFormaDePago(FormaDePago.EFECTIVO);
			String pattern="Deposito en efectivo {0,date,short} sucursal: {1}";
			cargo.setComentario(MessageFormat.format(pattern, ficha.getCorte(),cargo.getSucursal()));		
			cargo.setReferencia("Ficha: "+ficha.getFolio());
			cargo.setOrigen(origen);
			registrarBitacora(cargo);
			ficha.setIngreso(cargo);
			return getDepositosManager().save(ficha);
		}
		// Caso 2 Cheques 
		else if(ficha.getTipoDeFicha().equals(Ficha.FICHA_MISMO_BANCO) || ficha.getTipoDeFicha().equals(Ficha.FICHA_OTROSBANCOS)){
			//for(FichaDet det:ficha.getPartidas()){
				CargoAbono cargo=new CargoAbono();
				cargo.setAFavor(ficha.getCuenta().getBanco().getEmpresa().getNombre());
				cargo.setImporte(ficha.getTotal());
				cargo.setCuenta(ficha.getCuenta());
				cargo.setFecha(ficha.getFecha());
				cargo.setMoneda(ficha.getCuenta().getMoneda());
				cargo.setSucursal(ficha.getSucursal());
				cargo.setEncriptado(false);
				cargo.setFormaDePago(FormaDePago.CHEQUE);
				String pattern="{0} {1,date,short} sucursal: {2}";
				cargo.setComentario(MessageFormat.format(pattern, ficha.getTipoDeFicha(),ficha.getCorte(),cargo.getSucursal()));		
				cargo.setReferencia("Ficha: "+ficha.getFolio());				
				cargo.setOrigen(origen);
				registrarBitacora(cargo);
				ficha.setIngreso(cargo);
				//cargo=(CargoAbono)getHibernateTemplate().merge(cargo);
				return getDepositosManager().save(ficha);
				
			//}
			
		}
		return ficha;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CorreccionDeFicha registrarCorreccionDeFicha(CorreccionDeFicha co) {
		Assert.notNull(co.getFicha(),"Correccin debe tener ficha");
		Assert.notNull(co.getFicha().getIngreso(),"Correccin debe tener ficha");
		
		Ficha ficha=co.getFicha();		
		CargoAbono cargoAbono=ficha.getIngreso();
		this.hibernateTemplate.update(ficha);
		this.hibernateTemplate.update(cargoAbono);
		
		switch (co.getTipo()) {
		case FALTANTE_CORRECCION_FICHA:
			ficha.setTotal(co.getImporteReal());
			cargoAbono.setImporte(co.getImporteReal());
			break;
		case FALTANTE_POR_OPERACION:
		case SOBRANTE_NO_IDENTIFICADO:
		case SOBRANTE_POR_COBRANZA:
			break;
		case FALTANTE_EN_VALORES:
			final CargoAbono cargo=new CargoAbono();
			cargo.setAFavor(ficha.getCuenta().getBanco().getEmpresa().getNombre());
			cargo.setImporte(co.getImporteReal().abs());
			cargo.setCuenta(ficha.getCuenta());
			cargo.setFecha(ficha.getFecha());
			cargo.setMoneda(ficha.getCuenta().getMoneda());
			cargo.setSucursal(ficha.getSucursal());
			cargo.setEncriptado(false);
			cargo.setFormaDePago(FormaDePago.CHEQUE);
			String pattern="{0} {1,date,short} sucursal: {2}";
			cargo.setComentario(MessageFormat.format(pattern, ficha.getTipoDeFicha(),ficha.getCorte(),cargo.getSucursal()));		
			cargo.setReferencia("Ficha: "+ficha.getFolio());				
			cargo.setOrigen(Origen.TESORERIA);
			cargo.setComentario2(ficha.getId());
			break;
		default:
			break;
		}
		
				
		registrarBitacora(cargoAbono);
		registrarBitacora(co);
		co=(CorreccionDeFicha)hibernateTemplate.merge(co);
		return co;
	}
	

	@Transactional(propagation=Propagation.MANDATORY)
	private void registrarIngresosPorTarjeta(CorteDeTarjeta corte){		
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(corte.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(corte.getTotal());
		cargo.setCuenta(corte.getCuenta());
		cargo.setFecha(corte.getCorte());
		//cargo.setConcepto();
		cargo.setMoneda(corte.getCuenta().getMoneda());
		cargo.setSucursal(corte.getSucursal());
		cargo.setEncriptado(false);
		
		String pattern="Corte por Tarjeta {0,date,short} sucursal: {1}";
		String comentario=MessageFormat.format(pattern, corte.getCorte(),corte.getSucursal());
		cargo.setComentario(comentario);		
		cargo.setReferencia(corte.getTipoDeTarjeta());
		
		cargo.setOrigen(Origen.VENTA_MOSTRADOR);
		try {
			OrigenDeOperacion origenOperacion=corte.getPartidas().iterator().next().getPago().toOrienDeAplicacion();
			switch (origenOperacion) {
			case CRE:
				cargo.setOrigen(Origen.VENTA_CREDITO);
				break;
			case MOS:
				cargo.setOrigen(Origen.VENTA_MOSTRADOR);
				break;
			case CAM:
				cargo.setOrigen(Origen.VENTA_CAMIONETA);
				break;
			case JUR:
				cargo.setOrigen(Origen.JUR);
				break;
			case CHE:
				cargo.setOrigen(Origen.CHE);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		registrarBitacora(cargo);
		CargoAbonoPorCorte ca=new CargoAbonoPorCorte();
		ca.setOrden(1);
		ca.setComentario(comentario);
		ca.setCargoAbono(cargo);
		ca.setImporte(corte.getTotal());
		ca.setSucursal(corte.getSucursal());
		ca.setTipo(TipoDeAplicacion.INGRESO);
		corte.agregarAplicacion(ca);
		registrarComisiones(corte, cargo.getOrigen());
	}
	
	private void registrarComisiones(CorteDeTarjeta corte,Origen origen){
		CantidadMonetaria comisionCredito=CantidadMonetaria.pesos(0);
		CantidadMonetaria comisionDebito=CantidadMonetaria.pesos(0);
		CantidadMonetaria comisionAmex=CantidadMonetaria.pesos(0);
		if(corte.getTipoDeTarjeta().equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[0])){
			double debito=0;
			double credito=0;
			for(CorteDeTarjetaDet det:corte.getPartidas()){
				//double comision=det.getPago().getComisionBancaria(); AJUSTE PARA MEJORAR EL REDONDEO
				//CantidadMonetaria imp=det.getPago().getTotalCM().multiply(comision/-100);
				
				CantidadMonetaria imp=det.getPago().getTotalCM();
				if(det.getPago().getTarjeta().isDebito()){
					if(debito==0)
						debito=det.getPago().getComisionBancaria();
					comisionDebito=comisionDebito.add(imp);
				}else{
					if(credito==0)
						credito=det.getPago().getComisionBancaria();
					comisionCredito=comisionCredito.add(imp);
				}					
			}
			comisionDebito=comisionDebito.multiply(debito/-100);
			comisionCredito=comisionCredito.multiply(credito/-100);
		}else if(corte.getTipoDeTarjeta().equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[1])){
			double amex=3.80;
			
	//		final Date fechaCorte=corte.getCorte();
	//		final Calendar calendar=Calendar.getInstance();
	//		calendar.setTime(fechaCorte);				
	//		int diaCorte=calendar.get(Calendar.DAY_OF_WEEK);
			
/*			switch (diaCorte) {
			case Calendar.MONDAY:
				amex=4.1574;
				break;
			case Calendar.TUESDAY:
				amex=4.1574;
				break;
			case Calendar.WEDNESDAY:
				amex=4.1574;
				break;
			case Calendar.THURSDAY:
				amex=4.0148;
				break;
			case Calendar.FRIDAY:
				amex=4.0148;
				break;
				
			case Calendar.SATURDAY:
				amex=4.2186;
				break;
				
			case Calendar.SUNDAY:

			default:
				break;
			}*/
			
			for(CorteDeTarjetaDet det:corte.getPartidas()){
				CantidadMonetaria imp=det.getPago().getTotalCM();
				comisionAmex=comisionAmex.add(imp);
			}
			comisionAmex=comisionAmex.multiply(amex/-100);
		}
		
		if(comisionCredito.amount().doubleValue()<0){
			//Comision credito
			String comentario="Comisin por Tarjeta CREDITO ";
			CargoAbonoPorCorte ca=aplicarCargoAbonoPorCorte(corte, comisionCredito.amount()
					, origen, comentario, TipoDeAplicacion.COMISION_CREDITO);
			ca.setOrden(2);
			corte.agregarAplicacion(ca);
			//Impuesto
			comentario="IVA Comision Tarjeta CREDITO";
			CargoAbonoPorCorte ca1=aplicarCargoAbonoPorCorte(corte, MonedasUtils.calcularImpuesto(comisionCredito.amount())
					, origen, comentario, TipoDeAplicacion.IMPUESTO);
			ca1.setOrden(3);
			corte.agregarAplicacion(ca1);
		}if(comisionDebito.amount().doubleValue()<0){
			//Comision debito
			String comentario="Comisin por Tarjeta DEBITO ";
			CargoAbonoPorCorte ca=aplicarCargoAbonoPorCorte(corte, comisionDebito.amount()
					, origen, comentario, TipoDeAplicacion.COMISION_DEBITO);
			ca.setOrden(4);
			corte.agregarAplicacion(ca);
			//Impuesto
			comentario="IVA Comision Tarjeta DEBITO";
			CargoAbonoPorCorte ca1=aplicarCargoAbonoPorCorte(corte, MonedasUtils.calcularImpuesto(comisionDebito.amount())
					, origen, comentario, TipoDeAplicacion.IMPUESTO);
			ca1.setOrden(5);
			corte.agregarAplicacion(ca1);
		}if(comisionAmex.amount().doubleValue()<0){
			String comentario="Comisin por Tarjeta AMEX ";
			CargoAbonoPorCorte ca=aplicarCargoAbonoPorCorte(corte, comisionAmex.amount()
					, origen, comentario, TipoDeAplicacion.COMISION_AMEX);
			ca.setOrden(6);
			corte.agregarAplicacion(ca);
			//Impuesto
			comentario="IVA Comision Tarjeta AMEX";
			CargoAbonoPorCorte ca1=aplicarCargoAbonoPorCorte(corte, MonedasUtils.calcularImpuesto(comisionAmex.amount())
					, origen, comentario, TipoDeAplicacion.IMPUESTO);
			ca1.setOrden(7);
			corte.agregarAplicacion(ca1);
		}
		
	}
	
	private CargoAbonoPorCorte aplicarCargoAbonoPorCorte(CorteDeTarjeta corte,BigDecimal importe,Origen origen,String comentario,TipoDeAplicacion tipo){
		CargoAbono caComisionCredito=preparar(corte);
		caComisionCredito.setOrigen(origen);
		caComisionCredito.setComentario(comentario);		
		caComisionCredito.setReferencia(corte.getTipoDeTarjeta());
		caComisionCredito.setImporte(importe);
		CargoAbonoPorCorte ca=new CargoAbonoPorCorte();
		ca.setComentario(caComisionCredito.getComentario());
		ca.setCargoAbono(caComisionCredito);
		ca.setImporte(caComisionCredito.getImporte());
		ca.setSucursal(caComisionCredito.getSucursal());
		ca.setTipo(tipo);
		corte.agregarAplicacion(ca);
		return ca;
	}
	
	private CargoAbono preparar(CorteDeTarjeta corte){
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(corte.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setCuenta(corte.getCuenta());
		cargo.setFecha(corte.getCorte());
		cargo.setMoneda(corte.getCuenta().getMoneda());
		cargo.setSucursal(corte.getSucursal());
		cargo.setReferencia(corte.getTipoDeTarjeta());
		cargo.setEncriptado(false);
		return cargo;
	}
	
	
	
	private void registrarIngresosPorTarjeta_old(CorteDeTarjeta ingreso){
		
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(ingreso.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(ingreso.getTotal());
		cargo.setCuenta(ingreso.getCuenta());
		cargo.setFecha(ingreso.getCorte());
		//cargo.setConcepto();
		cargo.setMoneda(ingreso.getCuenta().getMoneda());
		cargo.setSucursal(ingreso.getSucursal());
		cargo.setEncriptado(false);
		
		String pattern="Corte por Tarjeta {0,date,short} sucursal: {1}";
		cargo.setComentario(MessageFormat.format(pattern, ingreso.getCorte(),cargo.getSucursal()));		
		cargo.setReferencia(ingreso.getTipoDeTarjeta());
		cargo.setOrigen(Origen.VENTA_MOSTRADOR);
		try {
			OrigenDeOperacion origenOperacion=ingreso.getPartidas().iterator().next().getPago().toOrienDeAplicacion();
			switch (origenOperacion) {
			case CRE:
				cargo.setOrigen(Origen.VENTA_CREDITO);
				break;
			case MOS:
				cargo.setOrigen(Origen.VENTA_MOSTRADOR);
				break;
			case CAM:
				cargo.setOrigen(Origen.VENTA_CAMIONETA);
				break;
			case JUR:
				cargo.setOrigen(Origen.JUR);
				break;
			case CHE:
				cargo.setOrigen(Origen.CHE);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		registrarBitacora(cargo);
		ingreso.setIngreso(cargo);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConDeposito registrarIngreso(PagoConDeposito pago) {
		this.hibernateTemplate.update(pago);
		if(pago.getIngreso()!=null)
			return pago;
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(pago.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(pago.getTotal());
		cargo.setCuenta(pago.getCuenta());
		cargo.setFecha(pago.getPrimeraAplicacion());
		cargo.setTc(BigDecimal.valueOf(pago.getTc()));
		cargo.setMoneda(pago.getCuenta().getMoneda());
		cargo.setSucursal(pago.getSucursal());
		cargo.setEncriptado(false);
		
		String pattern="Pago con deposito {0,date,short} sucursal: {1}";
		cargo.setComentario(MessageFormat.format(pattern, pago.getFechaDeposito(),cargo.getSucursal()));		
		cargo.setReferencia(pago.getInfo());
		cargo.setOrigen(Origen.VENTA_MOSTRADOR);
		cargo.setPago(pago);
		if(pago.getOrigen().equals(OrigenDeOperacion.CRE)||
				pago.getOrigen().equals(OrigenDeOperacion.JUR)||
				pago.getOrigen().equals(OrigenDeOperacion.CHE)){
			
			boolean conciliado=!DateUtil.isSameMonth(pago.getFechaDeposito(),pago.getFecha());
			cargo.setConciliado(conciliado);
			cargo.setFecha(pago.getFecha());
			
		}else{
			boolean conciliado=!DateUtil.isSameMonth(pago.getFechaDeposito(),pago.getPrimeraAplicacion());
			cargo.setConciliado(conciliado);
		}
		//Tratamiento para Cargos tipo TES
		System.out.println("****************************************************--"+pago.getId());
		for(Aplicacion a:pago.getAplicaciones()){
			System.out.println("********************************"+a.getId()+"-----------");
			
			if(a.getDetalle().getCarTipo().equals("TES")){				
				Concepto concepto=(Concepto)hibernateTemplate.get(Concepto.class, 737331L);
				cargo.setConcepto(concepto);
			}
		}
		
		registrarBitacora(cargo);
		try {			
			switch (pago.toOrienDeAplicacion()) {
			case CRE:				
				cargo.setOrigen(Origen.VENTA_CREDITO);
				break;
			case CAM:
				cargo.setOrigen(Origen.VENTA_CAMIONETA);
				break;
			case CHE:
				cargo.setOrigen(Origen.CHE);
				break;
			case JUR:
				cargo.setOrigen(Origen.JUR);
				break;
			case MOS:
				cargo.setOrigen(Origen.VENTA_MOSTRADOR);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cargo=(CargoAbono)getHibernateTemplate().merge(cargo);
		return pago;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public PagoConTarjeta registrarIngreso(PagoConTarjeta pago){
		CargoAbono cargo=new CargoAbono();
		cargo.setAFavor(pago.getCuenta().getBanco().getEmpresa().getNombre());
		cargo.setImporte(pago.getTotal());
		cargo.setCuenta(pago.getCuenta());
		cargo.setFecha(pago.getFecha());
		cargo.setMoneda(pago.getCuenta().getMoneda());
		cargo.setSucursal(pago.getSucursal());
		cargo.setEncriptado(false);
		
		String pattern="Pago con tarjeta {0,date,short} sucursal: {1}";
		cargo.setComentario(MessageFormat.format(pattern, pago.getFecha(),cargo.getSucursal()));		
		cargo.setReferencia(pago.getInfo());
		cargo.setOrigen(Origen.VENTA_CREDITO);
		cargo.setPago(pago);
		
		cargo.setConciliado(false);
		registrarBitacora(cargo);
		try {			
			switch (pago.getOrigen()) {
			case CRE:				
				cargo.setOrigen(Origen.VENTA_CREDITO);
				break;
			case CAM:
				cargo.setOrigen(Origen.VENTA_CAMIONETA);
				break;
			case CHE:
				cargo.setOrigen(Origen.CHE);
				break;
			case JUR:
				cargo.setOrigen(Origen.JUR);
				break;
			case MOS:
				cargo.setOrigen(Origen.VENTA_MOSTRADOR);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		cargo=(CargoAbono)getHibernateTemplate().merge(cargo);
		return pago;
	}

	
	@Transactional(propagation=Propagation.REQUIRED)
	public void correccionDeFecha(CargoAbono ca, Date fecha) {
		ca=(CargoAbono)getHibernateTemplate().get(CargoAbono.class, ca.getId());
		ca.setFecha(fecha);		
		registrarBitacora(ca);
		
		
		Pago pago=ca.getPago();
		if(pago!=null && (pago instanceof PagoConDeposito)){
			PagoConDeposito deposito=(PagoConDeposito)pago;
			deposito=(PagoConDeposito)getHibernateTemplate().get(PagoConDeposito.class, pago.getId());
			deposito.setFecha(fecha);	
			if(!DateUtil.isSameMonth(fecha, deposito.getFechaDeposito())){
				ca.setConciliado(true);
			}else
				ca.setConciliado(false);
			
			if(!deposito.getAplicaciones().isEmpty()){
				deposito.getAplicaciones().iterator().next();
				for(Aplicacion a:deposito.getAplicaciones()){
					if(a.getCargo() instanceof Venta){
						Venta vv=(Venta)a.getCargo();
						vv.getPartidas().iterator().next();
					}
				}
			}
			getHibernateTemplate().update(deposito);
		}
		if(pago!=null){
			final Date saf=pago.getPrimeraAplicacion();
			for(Aplicacion a:pago.getAplicaciones()){
				if(DateUtils.isSameDay(saf, a.getFecha())){
					String fpago=a.getDetalle().getFormaDePago();
					fpago=StringUtils.removeStart(fpago, "SAF ");
					a.getDetalle().setFormaDePago(fpago);
				}
			}
		}
		getHibernateTemplate().save(ca);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void correccionDeFechaDeposito(CargoAbono ca, Date fechaDeposito) {
		ca=(CargoAbono)getHibernateTemplate().get(CargoAbono.class, ca.getId());
		
		Pago pago=ca.getPago();
		if(pago!=null && (pago instanceof PagoConDeposito)){
					
			registrarBitacora(ca);
			PagoConDeposito deposito=(PagoConDeposito)pago;
			deposito=(PagoConDeposito)getHibernateTemplate().get(PagoConDeposito.class, pago.getId());
			deposito.setFechaDeposito(fechaDeposito);
			
			if(!DateUtil.isSameMonth(ca.getFecha(), deposito.getFechaDeposito())){
				ca.setConciliado(true);
			}else
				ca.setConciliado(false);
			ca.setComentario(MessageFormat.format("Pago con deposito {0,date,short} sucursal: {1}",fechaDeposito,deposito.getSucursal().getNombre()));
			if(!deposito.getAplicaciones().isEmpty()){
				deposito.getAplicaciones().iterator().next();
				for(Aplicacion a:deposito.getAplicaciones()){
					if(a.getCargo() instanceof Venta){
						Venta vv=(Venta)a.getCargo();
						vv.getPartidas().iterator().next();
					}
				}
			}
			
			getHibernateTemplate().update(deposito);
			
			getHibernateTemplate().save(ca);
		}
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CargoAbono cambioDeCobro( CargoAbono cheque,Boolean cobrado){
		cheque=(CargoAbono)getHibernateTemplate().get(CargoAbono.class, cheque.getId());
		cheque.setCobrado(cobrado);		
		registrarBitacora(cheque);
		return (CargoAbono)getHibernateTemplate().merge(cheque);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CargoAbono correccionDeFechaCobrado( CargoAbono cheque,final Date fecha){
		cheque=(CargoAbono)getHibernateTemplate().get(CargoAbono.class, cheque.getId());
		cheque.setFechaCobrado(fecha);		
		registrarBitacora(cheque);
		return (CargoAbono)getHibernateTemplate().merge(cheque);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CargoAbono correccionDeFechaCobro( CargoAbono cheque,final Date fecha){
		cheque=(CargoAbono)getHibernateTemplate().get(CargoAbono.class, cheque.getId());
		cheque.setFechaCobro(fecha);		
		registrarBitacora(cheque);
		return (CargoAbono)getHibernateTemplate().merge(cheque);
	}
	
	public CargoAbono actualizarComisionDeAmex(final Long cargoabono_id,BigDecimal importe){
		//Obtener el corte_id de sx_corte_tarjetas_aplicaciones
		String sql="SELECT T.TARJETA_TIPO FROM sx_corte_tarjetas_aplicaciones  A " +
				" JOIN SX_CORTE_TARJETAS T ON(T.CORTE_ID=A.CORTE_ID) WHERE A.CARGOABONO_ID=? AND A.IMPORTE<0";
		List<String> id=getJdbcTemplate().queryForList(sql, new Object[]{cargoabono_id}, String.class);
		if((id!=null) && (!id.isEmpty())){
			String tipo=id.get(0);
			if(tipo.equals(CorteDeTarjeta.TIPOS_DE_TARJETAS[1])){
				String UPDATE_1="UPDATE SX_CORTE_TARJETAS_APLICACIONES SET IMPORTE=? WHERE CARGOABONO_ID=?";
				getJdbcTemplate().update(UPDATE_1, new Object[]{importe,cargoabono_id});
				
				String UPDATE_2="UPDATE SW_BCARGOABONO SET IMPORTE=? , COMENTARIO=REPLACE(COMENTARIO,'AMEX','AMEX MODIF')WHERE CARGOABONO_ID=?";
				getJdbcTemplate().update(UPDATE_2, new Object[]{importe,cargoabono_id});
			}else
				throw new RuntimeException("El movimiento no est relacionado con una comisin de AMEX");
		}else
			throw new RuntimeException("El movimiento no es una comisin de AMEX");
		return (CargoAbono)getHibernateTemplate().get(CargoAbono.class, cargoabono_id);
		
	}
	
	private void registrarBitacora(final CargoAbono bean,String user){
		Date time=new Date();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getUserLog().setModificado(time);
		bean.getUserLog().setUpdateUser(user);
		//bean.getAddresLog().setUpdatedIp(ip);
		//bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getUserLog().setCreado(time);
			bean.getUserLog().setCreateUser(user);
			//bean.getAddresLog().setCreatedIp(ip);
			///bean.getAddresLog().setCreatedMac(mac);
		}
	}
	
	private void registrarBitacora(final CargoAbono bean){
		String user=KernellSecurity.instance().getCurrentUserName();
		registrarBitacora(bean, user);
	}
	
	public CorteDeTarjetaDao getCorteDeTarjetaDao() {
		return corteDeTarjetaDao;
	}

	public void setCorteDeTarjetaDao(CorteDeTarjetaDao corteDeTarjetaDao) {
		this.corteDeTarjetaDao = corteDeTarjetaDao;
	}

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public DepositosManager getDepositosManager() {
		return depositosManager;
	}

	public void setDepositosManager(DepositosManager depositosManager) {
		this.depositosManager = depositosManager;
	}
	
	private void registrarBitacora(CorreccionDeFicha bean){
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

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public static void main(String[] args) {
		DBUtils.whereWeAre();
		ServiceLocator2.getIngresosManager().actualizarComisionDeAmex(777237L, BigDecimal.valueOf(-200.10));
	}
	
	
	
}
