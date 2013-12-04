package com.luxsoft.siipap.ventas.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.dao.VentaDao;
import com.luxsoft.siipap.ventas.model.Venta;

public class VentasManagerImpl extends HibernateDaoSupport implements VentasManager{
	
	private VentaDao ventaDao;
	
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.ventas.service.VentasManager#get(java.lang.String)
	 */
	public Venta get(String id){
		return ventaDao.get(id);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Venta buscarVentaInicializada(final String ventaId){
		Venta v=(Venta)getHibernateTemplate().get(Venta.class, ventaId);
		Hibernate.initialize(v.getCliente());
		Hibernate.initialize(v.getCliente().getTelefonos());
		Hibernate.initialize(v.getPartidas());
		Hibernate.initialize(v.getVendedor());
		Hibernate.initialize(v.getCobrador());
		Hibernate.initialize(v.getSocio());
		if(v.getPedido()!=null){
			Hibernate.initialize(v.getPedido());
			Hibernate.initialize(v.getPedido().getInstruccionDeEntrega());
		}
		return v;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public NotaDeCargo buscarNtaDeCargoInicializada(String cargoId) {
		NotaDeCargo cargo=(NotaDeCargo)getHibernateTemplate().get(NotaDeCargo.class, cargoId);
		Hibernate.initialize(cargo.getCliente());
		Hibernate.initialize(cargo.getConceptos());
		return cargo;
	}

	public List<Aplicacion> buscarAplicaciones(final String ventaId){
		String hql="from Aplicacion a where a.cargo.id=?";
		return getHibernateTemplate().find(hql, ventaId);
	}

	public double getVolumenDeVentas(final Periodo p, final Cliente c) {		
		return (Double)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				BigDecimal ventas=(BigDecimal)session.createQuery("selec sum(v.importe) from Venta v where v.cliente.id=?" +
						" and v.fecha between ? and ? ")
						.setParameter(0, c.getId())
						.setParameter(1, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(2, p.getFechaFinal(),Hibernate.DATE)
						.setMaxResults(1)
						.uniqueResult();
				BigDecimal devos=(BigDecimal)session.createQuery("selec sum(d.importe) from Devolucion d where d.cliente.id=?" +
				" and d.fecha between ? and ? ")
				.setParameter(0, c.getId())
				.setParameter(1, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(2, p.getFechaFinal(),Hibernate.DATE)
				.setMaxResults(1)
				.uniqueResult();
				if(ventas==null) ventas=BigDecimal.ZERO;
				if(devos==null) devos=BigDecimal.ZERO;
				return ventas.subtract(devos);
			}
			
		});
	}

	/**
	 * Persiste o actualiza  una venta
	 * 
	 * Notifica de esto mediante JMS
	 * 
	 * @param venta
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public Venta salvar(Venta venta) {
		//Notificar de cambios
		return getVentaDao().save(venta);
	}

	public VentaDao getVentaDao() {
		return ventaDao;
	}

	public void setVentaDao(VentaDao ventaDao) {
		this.ventaDao = ventaDao;
	}
	
	public static void main(String[] args) {
		Venta v=ServiceLocator2.getVentasManager().get("8a8a8189-23383809-0123-3838d0dd-00c4");
		System.out.println("Descuento F: "+v.getDescuentoFinanciero());
	}
	
	

}
