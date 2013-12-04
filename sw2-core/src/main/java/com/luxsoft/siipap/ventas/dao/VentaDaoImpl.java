/*
 *  Copyright 2008 Ruben Cancino Ramos.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package com.luxsoft.siipap.ventas.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 *
 * @author Ruben Cancino Ramos
 * 
 */
public class VentaDaoImpl extends GenericDaoHibernate<Venta,String> implements VentaDao{

    public VentaDaoImpl() {
        super(Venta.class);
    }


	@Transactional(propagation=Propagation.REQUIRED)
    public Venta eliminarCredito(Venta v){
    	v=get(v.getId());
    	Assert.notNull(v.getCredito(),"La venta no es de credito");
    	getHibernateTemplate().delete(v.getCredito());
    	v.setCredito(null);
    	return save(v);
    	
    } 
	
	public Venta buscarVentaInicializada(String id) {
		String hql="from Venta v " +
				" left join fetch v.pedido p " +
				" left join fetch v.pedido.instruccionDeEntrega ie" +
				" left join fetch v.partidas pa" +
				" where v.id=?";
		List<Venta> res=getHibernateTemplate().find(hql, id);
		return res.isEmpty()?null:res.get(0);
	}


	public Venta buscarPorOracleId(Long id) {
		String hql="from Venta v left join fetch v.partidas where v.siipapWinId=?";
		List<Venta> res=getHibernateTemplate().find(hql, id);
		return res.isEmpty()?null:res.get(0);
	}


	public List<Venta> buscarVentas(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				return session.createQuery("from Venta v" +
						" where v.fecha between ? and ?")
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
						.list();
			}
			
		});
	}


	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.siipap.ventas.dao.VentaDao#buscarVentasConSaldo(java.lang.String)
	 */
	public List<Venta> buscarVentasConSaldo(String tipo) {
		return getHibernateTemplate().find("from Venta v where v.saldo!=0");
	}


	public List<Venta> buscarVentas(final Periodo p,final OrigenDeOperacion origen) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				return session.createQuery("from Venta v" +
						" where v.fecha between ? and ? and v.origen=?")
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
						.setString(2, origen.name())
						.list();
			}			
		});
	}


	public List<Venta> buscarVentas(final Periodo p, final Cliente cliente) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				return session.createQuery("from Venta v" +
						" where v.fecha between ? and ? and v.cliente.clave=?")
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(1, p.getFechaFinal(),Hibernate.DATE)
						.setParameter(2, cliente.getClave())
						.list();
			}			
		});
	}

	public Venta buscarVenta(final long sucursalId,final Long docto,final OrigenDeOperacion origen){
		String hql="from Venta v where v.sucursal.id=? and v.documento=? and v.origen=?";
		List<Venta> res=getHibernateTemplate().find(hql, new Object[]{sucursalId,docto,origen});
		return res.isEmpty()?null:res.get(0);
	}

	public Venta buscarVenta(final long sucursalId,final Long docto,final OrigenDeOperacion origen,final Date fecha){
		String hql="from Venta v where v.sucursal.id=? and v.documento=? and v.origen=? and date(v.fecha)=?";
		List<Venta> res=getHibernateTemplate().find(hql, new Object[]{sucursalId,docto,origen,fecha});
		return res.isEmpty()?null:get(res.get(0).getId());
	}

		
	
}
