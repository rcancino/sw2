package com.luxsoft.siipap.compras.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.compras.model.Compra;
import com.luxsoft.siipap.compras.model.CompraDet;
import com.luxsoft.siipap.compras.model.CompraRow;
import com.luxsoft.siipap.compras.model.EntradaPorCompra;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;

@SuppressWarnings("unchecked")
public class CompraDaoImpl extends GenericDaoHibernate<Compra, Long> implements CompraDao{

	public CompraDaoImpl() {
		super(Compra.class);
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Compra inicializarCompra(final Long id){
		Compra c=get(id);
		c.getProveedor().getNombreRazon();
		c.getSucursal().getNombre();
		for(CompraDet det:c.getPartidas()){
			for(EntradaPorCompra i:det.getEntradas()){
				i.getSucursal();
				//i.getRecepcion();
			}
		}
		return c;
	}

	@SuppressWarnings("unchecked")
	public List<CompraRow> buscarComprasRow() {
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List<CompraRow> rows=new ArrayList<CompraRow>();
				ScrollableResults rs=session.createQuery("from Compra c").scroll();
				while(rs.next()){
					Compra c=(Compra)rs.get()[0];
					CompraRow row=new CompraRow(c);
					rows.add(row);
				}
				return rows;
			}
			
		});
	}
	
	/**
	 * {@inheritDoc}
	 */
	
	public List<CompraDet> buscarComprasPendientesPorProveedor(final Proveedor p){
		return getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				List<CompraDet> res=new ArrayList<CompraDet>();
				List<CompraDet> data=session.createQuery("from CompraDet c " +
						" left join fetch c.sucursal s" +
						" where c.compra.proveedor=?" +
						" and c.depurada=?" 
						 
						)
						.setParameter(0, p,Hibernate.entity(Proveedor.class))
						.setBoolean(1, false)
						.list()
						;
				for(CompraDet det:data){
					if(det.isPendiente())
						res.add(det);
				}				
				return res;
			}
			
		});
	}

	public List<Compra> buscarCompras(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				return session.createQuery(
						"from Compra c " +
						" left join fetch c.proveedor" +
						" left join fetch c.sucursal" +
						" left join fetch c.partidas p" +
						" left join fetch p.entradas" +
						" where c.fecha between :f1 and :f2"
						)
				.setDate("f1", p.getFechaInicial())
				.setDate("f2", p.getFechaFinal())
				.list();
			}
		});
	}

	public Compra buscarPorFolio(final int sucursal,final int folio){
		final String hql="from Compra c " +
				"left join fetch c.partidas " +
				"left join fetch c.sucursal s " +
				"left join fetch c.proveedor p " +
				"where c.sucursal.clave=? and c.folio=?";
		List<Compra> res=getHibernateTemplate().executeFind(new HibernateCallback(){

			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				List<Compra> res=session.createQuery(hql)
				.setInteger(0, sucursal)
				.setInteger(1, folio)
				.list();
				for(Compra c:res){
					if(!c.getPartidas().isEmpty()){
						for(CompraDet det:c.getPartidas()){
							if(!det.getEntradas().isEmpty()){
								for(EntradaPorCompra e:det.getEntradas()){
									e.getProducto().getClave();
								}
							}
						}
					}
						
				}
				return res;
			}
			
		});
		return res.isEmpty()?null:res.get(0);
	}

	

}
