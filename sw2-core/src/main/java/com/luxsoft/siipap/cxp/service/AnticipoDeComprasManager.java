package com.luxsoft.siipap.cxp.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.model.AnticipoDeCompra;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.utils.LoggerHelper;

@Service("anticipoDeComprasManager")
@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class AnticipoDeComprasManager {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private FacturaManager facturasManager;
	
	Logger logger=LoggerHelper.getLogger();
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnticipoDeCompra get(Long id){
		AnticipoDeCompra a=(AnticipoDeCompra)this.hibernateTemplate.get(AnticipoDeCompra.class, id);
		Hibernate.initialize(a.getFacturas());
		Hibernate.initialize(a.getFactura().getAnalisis());
		return a;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnticipoDeCompra salvar(final AnticipoDeCompra bean){
		registrarBitacura(bean);
		generarFactura(bean);
		return (AnticipoDeCompra)this.hibernateTemplate.merge(bean);		
		
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	private void generarFactura(AnticipoDeCompra bean){
		Assert.isNull(bean.getFactura(),"Anticipo ya facturado");
		CXPFactura factura=new CXPFactura();		
		factura.setProveedor(bean.getProveedor());
		factura.setTc(bean.getTc());
		factura.setMoneda(bean.getMoneda());
		factura.setTotal(bean.getImporte());
		factura.setAnticipo(true);
		factura.setAnticipo(bean);
		factura.setComentario("ANTICIPO DE COMPRA");
		factura.setDocumento(bean.getDocumento());
		factura.setFecha(bean.getFecha());
		factura.setImporte(MonedasUtils.calcularImporteDelTotal(bean.getImporte()));
		factura.setImpuesto(MonedasUtils.calcularImpuesto(factura.getImporte()));
		bean.setFactura(factura);
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void generarNotaDeDesucneotComercial( AnticipoDeCompra bean){
		bean=get(bean.getId());
		Assert.isNull(bean.getDescuentoNota(),"Nota de desucuento comercial  ya generada");
		if(bean.getDescuento()>0 && (bean.getDescuentoNota()==null)){			
			CXPNota nota=new CXPNota();
			bean.setDescuentoNota(nota);
			nota.setProveedor(bean.getProveedor());
			nota.setDocumento(bean.getDocumentoDescuentoComercial());
			nota.setConcepto(CXPNota.Concepto.DESCUENTO);
			nota.setDescuento(bean.getDescuento());
			BigDecimal total=bean.getImporteDescuento();
			nota.setTotal(total);
			nota.setImporte(MonedasUtils.calcularImporteDelTotal(total));
			nota.setImpuesto(MonedasUtils.calcularImpuestoDelTotal(total));
			nota.setTc(bean.getTc());
			nota.setFecha(bean.getFecha());
			nota.setMoneda(bean.getMoneda());
			nota.setComentario("DESCUENTO COMERCIAL EN ANTICIPO DE COMPRA");
			
			CXPAplicacion aplicacion=new CXPAplicacion();
			aplicacion.setAbono(nota);
			aplicacion.setCargo(bean.getFactura());
			aplicacion.setComentario("DESCUENTO COMERCIAL EN ANTICIPO DE COMPRA");
			aplicacion.setFecha(nota.getFecha());
			aplicacion.setImporte(nota.getTotal());			
			nota.agregarAplicacion(aplicacion);
			
			bean.setDescuentoNota(nota);
			getHibernateTemplate().merge(bean);
			
		}
		
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void generarNotaDeDescuentoFinanciero( AnticipoDeCompra bean){
		Assert.isNull(bean.getNota(),"Nota de desucuento financiero ya generada");
		bean=get(bean.getId());
		if(bean.getDescuentoFinanciero()>0 && (bean.getNota()==null)){
			
			CXPNota nota=new CXPNota();
			bean.setNota(nota);
			nota.setProveedor(bean.getProveedor());
			nota.setDocumento(bean.getDocumentoNota());
			nota.setConcepto(CXPNota.Concepto.DESCUENTO_FINANCIERO);
			nota.setDescuento(bean.getDescuentoFinanciero());
			BigDecimal total=bean.getImporteDescuentoFinanciero();
			nota.setTotal(total);
			nota.setImporte(MonedasUtils.calcularImporteDelTotal(total));
			nota.setImpuesto(MonedasUtils.calcularImpuestoDelTotal(total));
			nota.setTc(bean.getTc());
			nota.setMoneda(bean.getMoneda());
			nota.setFecha(bean.getFecha());
			nota.setComentario("DESCUENTO FINANCIERO EN ANTICIPO");
			
			CXPAplicacion aplicacion=new CXPAplicacion();
			aplicacion.setAbono(nota);
			aplicacion.setCargo(bean.getFactura());
			aplicacion.setComentario(" DESCUENTO FINANCIERO EN ANTICIPO");
			aplicacion.setFecha(nota.getFecha());
			aplicacion.setImporte(nota.getTotal());
			
			nota.agregarAplicacion(aplicacion);
			
			bean.setNota(nota);
			getHibernateTemplate().merge(bean);
		}
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnticipoDeCompra asignarFacturas(AnticipoDeCompra anticipo,CXPFactura factura) {
		AnticipoDeCompra target=get(anticipo.getId());
		BigDecimal disp=target.getDisponible();
		if(disp.doubleValue()>=factura.getImporte().doubleValue()){
			factura.setAnticipo(target);
			target.getFacturas().add(factura);
			this.hibernateTemplate.merge(factura);
			return get(anticipo.getId());
			//return (AnticipoDeCompra)this.hibernateTemplate.merge(anticipo); 
		}
		return anticipo;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnticipoDeCompra removerFacturas(AnticipoDeCompra anticipo,CXPFactura factura) {
		AnticipoDeCompra target=get(anticipo.getId());
		factura.setAnticipo(null);
		target.getFacturas().remove(factura);
		this.hibernateTemplate.merge(factura);
		return get(anticipo.getId());
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminar(final Long analisisId){
		AnticipoDeCompra a=get(analisisId);
		getHibernateTemplate().update(a);
		Assert.isTrue(a.getFactura().getRequisitado().abs().doubleValue()==0,"Anticipo con factura requisitada ");
		/*if(a.getFactura()!=null){
			a.getFactura().getAplicaciones().clear();
		}
		if(a.getNota()!=null){
			a.getNota().getAplicaciones().clear();
		}*/
		for(CXPFactura ff:a.getFacturas()){
			Assert.isTrue(ff.getAnalizado().doubleValue()<=0,"Anticipo con facturas analizada Fac: "+ff.getDocumento()+" Analizado: "+ff.getAnalizado());
		}
		a.getFacturas().clear();
		//Assert.isTrue(a.getFactura().getPagos().abs().doubleValue()==0,"Anticipo con abonos aplicados no se puede eliminar");
		//Assert.isTrue(a.getFacturas().isEmpty(),"Anticipo con facturas vinculadas");
		//CXPFactura fac=a.getFactura();
		//a.setFactura(null);
		//this.hibernateTemplate.delete(a);
		/*
		CXPFactura factura=a.getFactura();
		CXPNota nota=a.getNota();
		a.setNota(null);
		a.setFactura(null);
		if(nota!=null)
			this.hibernateTemplate.delete(nota);
		if(factura!=null)
			this.hibernateTemplate.delete(factura);*/
		//CXPFactura factura=a.getFactura();
		//a.setFactura(null);
		CXPNota nota=a.getNota();
		if(nota!=null){
			a.setNota(null);
			this.hibernateTemplate.delete(nota);
		}
		
		this.hibernateTemplate.delete(a);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public AnticipoDeCompra registrarDiferencia(AnticipoDeCompra anticipo) {
		AnticipoDeCompra target=get(anticipo.getId());
		target.setDiferencia(target.getDisponible());
		return get(anticipo.getId());
	}
	
	
	public List<AnticipoDeCompra> buscar(Periodo periodo){
		String hql="from AnticipoDeCompra a where a.fecha between ? and ?";
		return getHibernateTemplate().find(hql,new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()});
	}
	
	private void registrarBitacura(AnticipoDeCompra bean){
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


	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}

	public FacturaManager getFacturasManager() {
		return facturasManager;
	}

	public void setFacturasManager(FacturaManager facturasManager) {
		this.facturasManager = facturasManager;
	}

	
	
}
