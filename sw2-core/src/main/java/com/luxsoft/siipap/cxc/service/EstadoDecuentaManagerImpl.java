package com.luxsoft.siipap.cxc.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.MovimientoDeCuenta;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;

public class EstadoDecuentaManagerImpl extends HibernateDaoSupport implements EstadoDeCuentaManager{

	/**
	 * Busca los movimientos de cuenta de un cliente para un period indicado
	 * 
	 * @param c
	 * @param p
	 * @return
	 */
	public List<MovimientoDeCuenta> buscarMovimientos(final Cliente c,final Periodo p){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				System.out.println("Buscando cargos: "+c+" Per: "+p);
				List<MovimientoDeCuenta> movimientos=new ArrayList<MovimientoDeCuenta>();
				
				//Los Cargos
				List<Cargo> cargos=session.createQuery(
						"from Cargo c where c.cliente.clave=? " +
						"and c.fecha between ? and ? " +
						"and c.origen=\'CRE\'"
						)
				.setString(0, c.getClave())
				.setParameter(1, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(2, p.getFechaFinal(),Hibernate.DATE)
				.list();
				
				for(Cargo c:cargos){
					MovimientoDeCuenta mov=new MovimientoDeCuenta(c);
					movimientos.add(mov);
				}
				
				List<Abono> abonos=session.createQuery(
						"from Abono c where c.cliente.clave=? " +
						"and c.fecha between ? and ?" +
						"and c.origen=\'CRE\'"
						)
				.setString(0, c.getClave())
				.setParameter(1, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(2, p.getFechaFinal(),Hibernate.DATE)
				.list();
				for (Abono abono : abonos) {
					MovimientoDeCuenta mov=new MovimientoDeCuenta(abono);
					movimientos.add(mov);
				}
				
				return movimientos;
			}
		});
	}
	
	/**
	 * Busca los movimientos de cuenta para todo un periodo
	 * 
	 * @param p
	 * @return
	 */
	public List<MovimientoDeCuenta> buscarMovimientos(final Periodo p){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				System.out.println("Buscando cargos Per: "+p);
				List<MovimientoDeCuenta> movimientos=new ArrayList<MovimientoDeCuenta>();
				
				//Los Cargos
				List<Cargo> cargos=session.createQuery(
						"from Cargo c where " +
						" c.fecha between ? and ? " +
						" and c.origen=\'CRE\'"+
						" order by c.fecha desc"
						)
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
				
				for(Cargo c:cargos){
					MovimientoDeCuenta mov=new MovimientoDeCuenta(c);
					
					System.out.println("Agrgando mov: "+mov);
					movimientos.add(mov);
				}
				
				List<Abono> abonos=session.createQuery(
						"from Abono c where " +
						" c.fecha between ? and ? " +
						" and c.origen=\'CRE\'"+
						" order by c.fecha desc"
						)
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
				for (Abono abono : abonos) {
					MovimientoDeCuenta mov=new MovimientoDeCuenta(abono);
					movimientos.add(mov);
				}
				
				List<Aplicacion> aplicaciones=session.createQuery(
						"from Aplicacion c where " +
						" c.fecha between ? and ? " +
						" and c.abono.origen=\'CRE\'"+
						" order by c.fecha desc"
						)
				.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
				.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
				.list();
				for (Aplicacion aplicacion: aplicaciones) {
					MovimientoDeCuenta mov=new MovimientoDeCuenta(aplicacion);
					movimientos.add(mov);
				}
				
				return movimientos;
			}
		});
	}

}
