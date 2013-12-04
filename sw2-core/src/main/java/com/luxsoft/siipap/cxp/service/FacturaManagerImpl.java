package com.luxsoft.siipap.cxp.service;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxp.dao.FacturaDao;
import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPAplicacion;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPUtils;
import com.luxsoft.siipap.dao.GenericDao;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.impl.GenericManagerImpl;

public class FacturaManagerImpl extends GenericManagerImpl<CXPFactura, Long> implements FacturaManager{
	
	private HibernateTemplate template;

	public FacturaManagerImpl(GenericDao<CXPFactura, Long> genericDao) {
		super(genericDao);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPFactura get(Long id) {
		CXPFactura fac = super.get(id);
		Hibernate.initialize(fac.getPartidas());
		Hibernate.initialize(fac.getAnalisis());
		return fac;
		
	}



	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPFactura save(CXPFactura factura) {
		final Proveedor p=factura.getProveedor();		
		if(p!=null){
			final Date fecha=factura.getFecha();
			Date revision=fecha;
			Date vto   = CXPUtils.calcularVencimiento(revision, fecha, p);
			Date vtoDf = CXPUtils.calcularVencimientoDescuentoF(revision, fecha,p);
			factura.setRevision(revision);
			factura.setVencimiento(vto);
			factura.setVencimientoDF(vtoDf);
		}
		CXPFactura res= super.save(factura);
		return res;
	}

	

	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		CXPFactura fac=get(id);
		Assert.isTrue(fac.getRequisitado().abs().doubleValue()==0,"Anális con requisiciones ");
		Assert.isTrue(fac.getPagos().abs().doubleValue()==0,"Análisis con factura con pagos aplicados no se puede eliminar");
		super.remove(id);
	}

	public FacturaDao getFacturaDao(){
		return (FacturaDao)this.genericDao;
	}

	/**
	 * Busca las facturas del periodo
	 * 
	 * @param p
	 * @return
	 */
	public List<CXPFactura> buscarFacturas(Periodo p) {
		return getFacturaDao().buscarFacturas(p);
	}
	
	/**
	 * Localiza todas las facturas pendientes 
	 * 
	 * @return
	 */
	public List<CXPFactura> buscarFacturasPendientes(){
		return getFacturaDao().buscarFacturasPendientes();
	}
	
	/**
	 * Busca las facturas relacionadas con el proveedor
	 * 
	 * @param proveedor
	 * @return
	 */
	public List<CXPFactura> buscarFacturas(final Proveedor proveedor){
		return getFacturaDao().buscarFacturas(proveedor);
	}
	
	/**
	 * Regresa el detalle del analisis para la factura indicada
	 * 
	 * @param factura
	 * @return
	 */
	public List<CXPAnalisisDet> buscarAnalisis(final CXPFactura factura){
		return getFacturaDao().buscarAnalisis(factura);
	}
	
	/**
	 * Busca todas las facturas con pagos pendientes
	 * 
	 * @param proveedor
	 * @param moneda
	 * @return
	 */
	public List<CXPFactura> buscarFacturasPorRequisitar(final Proveedor proveedor,final Currency moneda){
		return getFacturaDao().buscarFacturasPorRequisitar(proveedor, moneda);
	}
	


	public List<CXPCargo> buscarCuentasPorPagar(Proveedor proveedor,Currency moneda) {
		return getFacturaDao().buscarCuentasPorPagar(proveedor, moneda);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public CXPFactura buscarFactura(final String numero,final Proveedor p){
		String hql="from CXPFactura f where f.documento=? and f.proveedor.id=?";
		Object[] values=new Object[]{numero,p.getId()};
		List<CXPFactura> res =getTemplate().find(hql, values);
		return res.isEmpty()?null:get(res.get(0).getId());
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<CXPFactura> buscarFacturas(final List<String> numeros,final Proveedor p){
		List<CXPFactura> facs=new ArrayList<CXPFactura>();
		for(String doc:numeros){
			try {
				CXPFactura fac=buscarFactura(doc, p);
				if(fac!=null)
					facs.add(fac);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return facs;
 	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public CXPFactura registrarDiferencia(CXPFactura fac) {
		CXPFactura target=get(fac.getId());
		target.setDiferencia(fac.getSaldoCalculado());
		target.setDiferenciaFecha(new Date());
		return get(target.getId());
	}
	
	@Transactional(propagation=Propagation.MANDATORY)
	public void actualizarSaldo(CXPCargo fac){
		CantidadMonetaria saldo=fac.getTotalCM();
		for(CXPAplicacion a:fac.getAplicaciones()){
			saldo=saldo.subtract(a.getImporteCM());
		}
		fac.setSaldo(saldo.amount());
		System.out.println("Actualizando saldo: "+saldo);
	}
	
	public HibernateTemplate getTemplate() {
		return template;
	}

	public void setTemplate(HibernateTemplate template) {
		this.template = template;
	}

	public static void main(String[] args) {
		CXPServiceLocator.getInstance().getFacturasManager().get(704L);
		
	}
	
}
