package com.luxsoft.siipap.cxp.service;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxp.dao.ContraReciboDao;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.cxp.model.ContraRecibo;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.siipap.util.MonedasUtils;

public class ContraReciboManagerImpl extends GenericManagerImpl<ContraRecibo, Long> implements ContraReciboManager{

	public ContraReciboManagerImpl(GenericDao<ContraRecibo, Long> genericDao) {
		super(genericDao);
	}
	
	private ContraReciboDao getDao(){
		return (ContraReciboDao)this.genericDao;
	}

	public List<ContraReciboDet> buscarPartidas(final ContraRecibo recibo) {
		return getDao().buscarPartidas(recibo);
	}

	public List<ContraRecibo> buscarRecibos(Periodo p) {
		return getDao().buscarRecibos(p);
	}

	public ContraRecibo buscarInicializado(Long id) {
		return getDao().buscarInicializado(id);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public ContraRecibo save(ContraRecibo object) {
		/*
		for(ContraReciboDet det:object.getPartidas()){
			switch (det.getTipo()) {
			case FACTURA:
				CXPFactura factura=buildFactura(det);
				det.setCargoAbono(factura);
				break;
			case CARGO:
				CXPCargo cargo=buildCargo(det);
				det.setCargoAbono(cargo);
				break;
			case CREDITO:
				CXPNota nota=buildNota(det);
				det.setCargoAbono(nota);				
				break;
			default:
				break;
			}
		}*/
		return super.save(object);
	}

	private CXPNota buildNota(ContraReciboDet det) {
		CXPNota ca=new CXPNota();
		ca.setProveedor(det.getRecibo().getProveedor());
		ca.setFecha(det.getFecha());
		ca.setDocumento(det.getDocumento());
		ca.setMoneda(det.getMoneda());
		if(MonedasUtils.DOLARES.equals(ca.getMoneda()))
			ca.setTc(buscarTipoDeCambio(det.getFecha()));
		
		ca.setTotal(det.getTotal());
		ca.setImporte(MonedasUtils.calcularImporteDelTotal(ca.getTotal()));
		ca.setImpuesto(MonedasUtils.calcularImpuesto(ca.getImporte()));
				
		return ca;
	}

	private CXPCargo buildCargo(ContraReciboDet det) {
		CXPFactura ca=new CXPFactura();
		ca.setProveedor(det.getRecibo().getProveedor());
		ca.setFecha(det.getFecha());
		ca.setDescuentoFinanciero(det.getRecibo().getProveedor().getDescuentoFinanciero());
		ca.setDocumento(det.getDocumento());
		ca.setMoneda(det.getMoneda());
		if(MonedasUtils.DOLARES.equals(ca.getMoneda()))
			ca.setTc(buscarTipoDeCambio(det.getFecha()));
		ca.setComentario("NOTA DE CARGO");
		ca.setTotal(det.getTotal());
		ca.setImporte(MonedasUtils.calcularImporteDelTotal(ca.getTotal()));
		ca.setImpuesto(MonedasUtils.calcularImpuesto(ca.getImporte()));
		
		ca.actualizarVencimiento();
		ca.actualizarVencimientoDF();
		
		return ca;
	}

	private CXPFactura buildFactura(ContraReciboDet det) {
		CXPFactura ca=new CXPFactura();
		ca.setProveedor(det.getRecibo().getProveedor());
		ca.setFecha(det.getFecha());
		ca.setDescuentoFinanciero(det.getRecibo().getProveedor().getDescuentoFinanciero());
		ca.setDocumento(det.getDocumento());
		ca.setMoneda(det.getMoneda());
		if(MonedasUtils.DOLARES.equals(ca.getMoneda()))
			ca.setTc(buscarTipoDeCambio(det.getFecha()));
		
		ca.setTotal(det.getTotal());
		ca.setImporte(MonedasUtils.calcularImporteDelTotal(ca.getTotal()));
		ca.setImpuesto(MonedasUtils.calcularImpuesto(ca.getImporte()));
		
		ca.actualizarVencimiento();
		ca.actualizarVencimientoDF();
		
		return ca;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		ContraRecibo recibo=get(id);
		for(ContraReciboDet det:recibo.getPartidas()){
			if(det.getCargoAbono()!=null){
				String pattern="El documento {0} de tipo {1} esta ya esta contabilizado " +
						"por lo que el contrarecibo no puede ser eliminado";
				throw new IllegalStateException(MessageFormat.format(pattern, det.getCargoAbono().getDocumento(),det.getTipo()));
			}
		}
		super.remove(id);
	}
	
	
	/**
	 * Buscar recibos pendientes de analisis
	 * 
	 * @param p
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(final Proveedor p,ContraReciboDet.Tipo tipo){
		return getDao().buscarRecibosPendientes(p, tipo);
	}

	/**
	 * Busca recibos pendientes de analisis
	 * 
	 * @param tipo
	 * @return
	 */
	public List<ContraReciboDet> buscarRecibosPendientes(ContraReciboDet.Tipo tipo){
		return getDao().buscarRecibosPendientes(tipo);
	}
	
	private double buscarTipoDeCambio(final Date fecha){		
		//System.out.println("Buscando tipo de cambio: "+fecha);
		String hql="select t.factor from TipoDeCambio t where t.fecha=?";
		List<Double> res=getHibernateTemplate().find(hql,fecha);
		if(res==null) return 1d;
		if(res.isEmpty()) return 1d;
		return res.get(0);
		
	}
	
	private HibernateTemplate hibernateTemplate;

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	

}
