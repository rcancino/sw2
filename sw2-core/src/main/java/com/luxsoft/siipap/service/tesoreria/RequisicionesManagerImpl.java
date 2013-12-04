package com.luxsoft.siipap.service.tesoreria;


import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.ContraReciboDet;
import com.luxsoft.siipap.dao.tesoreria.CargoAbonoDao;
import com.luxsoft.siipap.dao.tesoreria.RequisicionDao;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.GProveedor;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.model.tesoreria.Requisicion.Estado;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;
import com.luxsoft.utils.LoggerHelper;

public class RequisicionesManagerImpl extends GenericManagerImpl<Requisicion, Long> implements RequisicionesManager{
	
	//private ComprasDeGastosManager comprasDeGastosManager;
	Logger logger=LoggerHelper.getLogger();
	
	private CargoAbonoDao cargoAbonoDao;

	public RequisicionesManagerImpl(RequisicionDao dao) {
		super(dao);
	}
	
	private RequisicionDao getRequisicionDao(){
		return (RequisicionDao)genericDao;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Requisicion save(Requisicion req) {
		
		if(req.getId()==null)			
			req.actualizarTotal();
		for(RequisicionDe det:req.getPartidas()){
			GFacturaPorCompra fac=det.getFacturaDeGasto();
			if(fac!=null){
				
			}
		}
		for(RequisicionDe det:req.getPartidas()){
			if(det.getAnalisis()!=null){
				//det.setFacturaDeCompras(det.getAnalisis().getFactura());
			}
			//det.getFacturaDeCompras();
			//System.out.println("DF en Fac de CXP: "+det.getFacturaDeCompras().getDescuentoFinanciero());
		}
		try {
			actualizarAnalisis(req);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Requisicion r=super.save(req);
		try {
			actualizarContrarecibos(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return r;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Requisicion registrarPago(Requisicion req) {
		if(req.getPago()!=null){
			req.getPago().setFechaCobro(req.getPago().getFecha());
		}
		req.setEstado(Estado.PAGADA);
		return save(req);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Requisicion cancelarPago(Requisicion req) {
		
		if(req.getPago()!=null){
			//Long pagoId=req.getPago().getId();
			req=get(req.getId());
			CargoAbono ca=req.getPago();
			ca.cancelar();
			ca.setComentario("CANCELADO REQ:"+req.getId());
			ca.setAFavor("(CH) CANCELADO ");
			getCargoAbonoDao().save(ca);
			//getCargoAbonoDao().remove(pagoId);
			req.cancelarPago();
		}
		
		return save(req);
	}
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		Requisicion req=get(id);
		for(RequisicionDe det:req.getPartidas()){
			if(det.getAnalisis()!=null){
				det.getAnalisis().setRequisicionDet(null);
				det.setAnalisis(null);
			}
			
		}
		getHibernateTemplate().delete(req);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public void eliminarRequisicionAutomatica(Long  id) {
		Requisicion req=get(id);
		//Verificar si existen facturas relacionadas con las partidas de esta requisicion
		for(RequisicionDe det:req.getPartidas()){			
			//getRequisicionDao().cancleraFacturaRequisitada(det);
		}
		remove(id);
	}

	public List<Requisicion> buscarRequisicionesDeGastos() {
		return getRequisicionDao().buscarRequisicionesDeGastos();
	}
	
	public List<Requisicion> buscarRequisicionesDeGastos(Periodo p){
		return getRequisicionDao().buscarRequisicionesDeGastos(p);
	}
	
	/**
	 * Regresa una lista de las requisiciones elaboradas en el modulo de compras
	 * en el periodo indicado
	 * 
	 * @param p
	 * @return
	 */
	public List<Requisicion> buscarRequisicionesDeCompras(Periodo p){
		return getRequisicionDao().buscarRequisicionesDeCompras(p);
	}
	

	public Requisicion buscarRequisicionDeCompras(Long id) {		
		return getRequisicionDao().buscarRequisicionDeCompras(id);
	}

	/**
	 * @return the cargoAbonoDao
	 */
	public CargoAbonoDao getCargoAbonoDao() {
		return cargoAbonoDao;
	}

	/**
	 * @param cargoAbonoDao the cargoAbonoDao to set
	 */
	public void setCargoAbonoDao(CargoAbonoDao cargoAbonoDao) {
		this.cargoAbonoDao = cargoAbonoDao;
	}

	public long nextCheque(final Long cuentaId) {
		return getCargoAbonoDao().buscarProximoCheque(cuentaId);
	}

	public List<RequisicionDe> buscarAnticiposPendientes(final GProveedor prov) {
		return getRequisicionDao().buscarAnticiposDisponibles(prov);
	}

	
	private HibernateTemplate hibernateTemplate;

	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	private void actualizarContrarecibos(Requisicion r){
		if(r.getOrigen().equals(Requisicion.COMPRAS)){
			for(RequisicionDe det:r.getPartidas()){
				String hql="from ContraReciboDet r where r.recibo.proveedor.id=? and r.documento=?";
				Object[] params={r.getProveedor().getId(),det.getDocumento()};
				List<ContraReciboDet> dets=getHibernateTemplate().find(hql,params);
				for(ContraReciboDet cr:dets){
					//logger.info("Actualizando contrarecibo... "+det.getId());
					cr.setRequisicion(det.getId());
				}
			}
		}
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	private void actualizarAnalisis(Requisicion r){
		logger.info("Desvinculando analisis para requisicion: "+r.getId()+ " Con analisis: "+r.getAnalisisPorActualizar().size());
		for(AnalisisDeFactura det:r.getAnalisisPorActualizar()){
			
			det.setRequisicionDet(null);
			this.hibernateTemplate.update(det);
			/*
			String hql="from AnalisisDeCompra where a.id=?";
			List<AnalisisDeFactura> dets=getHibernateTemplate().find(hql,det.getId());
			for(AnalisisDeFactura a:dets){
				System.out.println("Actualizando analisis : "+a.getId());
				a.setRequisicionDet(null);
			}*/
		}
	}
	

}
