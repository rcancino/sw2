package com.luxsoft.siipap.ventas.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosCliente;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;

public class ListaDePreciosClienteDaoImpl extends GenericDaoHibernate<ListaDePreciosCliente, Long> implements ListaDePreciosClienteDao{

	public ListaDePreciosClienteDaoImpl() {
		super(ListaDePreciosCliente.class);
	}
	
	//@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePreciosCliente get(final Long id) {		
		return super.get(id);
	}
	
	public ListaDePreciosCliente buscarListaVigente(final Cliente p) {
		return (ListaDePreciosCliente)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePreciosCliente> res=session.createQuery(
						"from ListaDePreciosCliente lp left join fetch lp.precios " +
						" where lp.activo=? and lp.cliente=?")
						.setParameter(0,Boolean.TRUE)
						.setEntity(1, p)
						.list();
				return res.isEmpty()?null:res.get(0);
			}
			
		});
	}
	
	public BigDecimal buscarPrecio(final Cliente c, final Producto p,final Currency moneda) {
		Number res= (Number)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="select lp.precio from ListaDePreciosClienteDet lp" +
						" left join lp.lista l" +
						" where l.activo=? and lp.producto.clave=? and l.cliente.clave=?";
				return session.createQuery(hql)
				.setParameter(0,Boolean.TRUE)
				.setString(1, p.getClave())
				.setString(2, c.getClave())
				.setMaxResults(1)
				.uniqueResult();
			}
			
		});
		if(res==null)	{
			 res=0;
			 return BigDecimal.valueOf((res.doubleValue()));
		}		
		return BigDecimal.valueOf(res.doubleValue());

	}
	
	public double buscarDescuentoPorProducto(final Cliente c,final Producto p,final Currency moneda){
		Number res= (Number)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				String hql="select lp.descuento from ListaDePreciosClienteDet lp" +
						" left join lp.lista l" +
						" where l.activo=? and lp.producto.clave=? and l.cliente.clave=?";
				return session.createQuery(hql)
				.setParameter(0,Boolean.TRUE)
				.setString(1, p.getClave())
				.setString(2, c.getClave())
				.setMaxResults(1)
				.uniqueResult();
			}
			
		});
		if(res==null)	{
			 res=0;
			 return res.doubleValue();
		}		
		return res.doubleValue();
	}

	public List<ListaDePreciosCliente> buscarListasVigentes() {
		return getHibernateTemplate().find("" +
				"from ListaDePreciosCliente lp " +
				" left join fetch lp.precios" +
				" where lp.activo=?", Boolean.TRUE);
	}
	/*
	public List<ListaDePreciosCliente> buscarListas(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery("from ListaDePreciosCliente lp left join fetch lp.precios where lp.fechaInicial between" +
						" ? and ?")
						.setParameter(0,p.getFechaInicial(), Hibernate.DATE)
						.setParameter(1,p.getFechaFinal(), Hibernate.DATE)
						.list();
			}
			
		});
	}*/
	
	public ListaDePreciosCliente copiar(Long id) {
		ListaDePreciosCliente source=get(id);
		ListaDePreciosCliente target=new ListaDePreciosCliente();
		BeanUtils.copyProperties(source, target,new String[]{"id","version","precios"});
		target.setComentario("Copia de lista: "+source.getId());
		for(ListaDePreciosClienteDet det:source.getPrecios()){
			ListaDePreciosClienteDet tar=new ListaDePreciosClienteDet(); 
			BeanUtils.copyProperties(det, tar,new String[]{"id","lista"});
			target.agregarPrecio(tar);
		}
		return save(target);
	}



	public static void main(String[] args) {
		Producto p=new Producto("POL74","");
		Cliente c=new Cliente("I020376","");
		ListaDePreciosClienteDaoImpl dao=new ListaDePreciosClienteDaoImpl();
		dao.setSessionFactory(ServiceLocator2.getSessionFactory());
		BigDecimal precio=dao.buscarPrecio(c, p, MonedasUtils.PESOS);
		System.out.println("Precio: "+precio);
	}	
	


}
