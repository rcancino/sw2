package com.luxsoft.siipap.compras.dao;

import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.compras.model.ListaDePrecios;
import com.luxsoft.siipap.compras.model.ListaDePreciosDet;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;


public class ListaDePreciosDaoImpl extends GenericDaoHibernate<ListaDePrecios, Long> implements ListaDePreciosDao{

	public ListaDePreciosDaoImpl() {
		super(ListaDePrecios.class);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePrecios save(ListaDePrecios object) {
		Hibernate.initialize(object.getPrecios());
		for(ListaDePreciosDet det:object.getPrecios()){
			det.setNeto(det.getCosto().amount());
		}
		String user=KernellSecurity.instance().getCurrentUserName();
		Date time=new Date();
		if(object.getId()==null){
			object.getLog().setCreado(time);
			object.getLog().setCreateUser(user);
		}
		object.getLog().setModificado(time);
		object.getLog().setUpdateUser(user);
		object.setReplicado(null);
		ListaDePrecios lp=super.save(object);
		return lp;
	}


	@Override
	@Transactional(propagation=Propagation.REQUIRED)
	public void remove(Long id) {
		ListaDePrecios delete=get(id);
		delete.getPrecios().clear();
		delete.setDescripcion("ELIMINADA");
		String user=KernellSecurity.instance().getCurrentUserName();
		Date time=new Date();
		delete.getLog().setModificado(time);
		delete.getLog().setUpdateUser(user);
		super.save(delete);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePrecios get(final Long id) {		
		ListaDePrecios lp= super.get(id);
		Hibernate.initialize(lp.getPrecios());
		return lp;
	}

	public ListaDePrecios buscarListaVigente(final Proveedor p) {
		return (ListaDePrecios)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePrecios> res=session.createQuery(
						"from ListaDePrecios lp left join fetch lp.precios " +
						" where lp.vigente=? and lp.proveedor=?")
						.setParameter(0,Boolean.TRUE)
						.setEntity(1, p)
						.list();
				return res.isEmpty()?null:res.get(0);
			}
			
		});
	}
	

	public List<ListaDePrecios> buscarListasVigentes() {
		return getHibernateTemplate().find("" +
				"from ListaDePrecios lp " +
				" left join fetch lp.precios" +
				" where lp.vigente=?", Boolean.TRUE);
	}


	public List<ListaDePrecios> buscarListas(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				return session.createQuery("from ListaDePrecios lp left join fetch lp.precios where lp.fechaInicial between" +
						" ? and ?")
						.setParameter(0,p.getFechaInicial(), Hibernate.DATE)
						.setParameter(1,p.getFechaFinal(), Hibernate.DATE)
						.list();
			}
			
		});
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public ListaDePrecios copiar(Long id) {
		ListaDePrecios source=get(id);
		ListaDePrecios target=new ListaDePrecios();
		BeanUtils.copyProperties(source, target,new String[]{"id","version","precios","oldId","userLog"});
		target.setDescripcion("Copia de lista: "+source.getId());
		for(ListaDePreciosDet det:source.getPrecios()){
			ListaDePreciosDet tar=new ListaDePreciosDet(); 
			BeanUtils.copyProperties(det, tar,new String[]{"id","lista",""});
			target.agregarPrecio(tar);
		}
		return save(target);
	}

	/**
	 * Busca el precio de lista vigente y mas adecuado en las listas del proveedor
	 * 
	 * @param p
	 * @param prov
	 * @return
	 *//*
	public ListaDePreciosDet buscarPrecioVigente(final Producto p,final Proveedor prov){
		return (ListaDePreciosDet)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePreciosDet> found= session.createQuery(
						"from ListaDePreciosDet d " +
						" where d.lista.proveedor.id=?" +
						"   and d.lista.vigente=?" +
						"   and d.producto=?" +
						"   and d.unidad=?" +
						" order by d.lista.fechaFinal desc")
						.setLong(0, prov.getId())
						.setBoolean(1, true)
						.setEntity(2, p)
						.setString(3, p.getUnidad().getUnidad())
						.list();
				if(found.isEmpty()) return null;
				for(ListaDePreciosDet d:found){
					String pattern="Precio: {0} Lista: {1} Vigencia: {2,date,short} al {3,date,short}";
					System.out.println(MessageFormat.format(pattern, d,d.getLista().getId(),d.getLista().getFechaInicial(),d.getLista().getFechaFinal()));
				}
				return found.get(0);
			}			
		});
	}
*/

	public ListaDePreciosDet buscarPrecioVigente(final Producto p,final  Currency moneda,final Proveedor prov,final Date fecha) {
		return (ListaDePreciosDet)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePreciosDet> found= session.createQuery(
						"from ListaDePreciosDet d " +
						" where d.lista.proveedor.id=?" +
						"   and d.producto=?" +
						"   and d.precio.currency=?" +
						"  and ? between d.lista.fechaInicial and d.lista.fechaFinal" +
						" order by d.lista.fechaFinal desc")
						.setLong(0, prov.getId())
						.setEntity(1, p)
						.setParameter(2, moneda,Hibernate.CURRENCY)
						.setParameter(3, fecha,Hibernate.DATE)
						.list();
				if(found.isEmpty()) return null;
				/*for(ListaDePreciosDet d:found){
					String pattern="Precio: {0} Lista: {1} Vigencia: {2,date,short} al {3,date,short}";
					System.out.println(MessageFormat.format(pattern, d,d.getLista().getId(),d.getLista().getFechaInicial(),d.getLista().getFechaFinal()));
				}*/
				return found.get(0);
			}			
		});
	}
	
	public ListaDePreciosDet buscarPrecioVigente(final Producto p,final Proveedor prov,final Date fecha) {
		return (ListaDePreciosDet)getHibernateTemplate().execute(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePreciosDet> found= session.createQuery(
						"from ListaDePreciosDet d " +
						" where d.lista.proveedor.id=?" +
						"   and d.producto=?" +
						"  and ? between d.lista.fechaInicial and d.lista.fechaFinal" +
						" order by d.lista.fechaFinal desc")
						.setLong(0, prov.getId())
						.setEntity(1, p)
						.setParameter(2, fecha,Hibernate.DATE)
						.list();
				if(found.isEmpty()) return null;
				/*for(ListaDePreciosDet d:found){
					String pattern="Precio: {0} Lista: {1} Vigencia: {2,date,short} al {3,date,short}";
					System.out.println(MessageFormat.format(pattern, d,d.getLista().getId(),d.getLista().getFechaInicial(),d.getLista().getFechaFinal()));
				}*/
				return found.get(0);
			}			
		});
	}
	
	

	public List<ListaDePreciosDet> buscarPreciosVigentes(final String claveProd,final Date fecha) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				List<ListaDePreciosDet> found= session.createQuery(
						"from ListaDePreciosDet d " +
						" where d.producto.clave=?" +
						"  and ? between d.lista.fechaInicial and d.lista.fechaFinal" +
						" order by d.lista.fechaFinal desc")
						.setString(0, claveProd)
						.setParameter(1, fecha,Hibernate.DATE)
						.list();
				return found;
			}			
		});
	}

	public static void main(String[] args) {
		Producto p=ServiceLocator2.getProductoManager().buscarPorClave("SBS19024");
		Proveedor prov=ServiceLocator2.getProveedorManager().buscarPorClave("I024");
		Date fecha=DateUtil.toDate("10/09/2011");
		ListaDePreciosDet precio=ServiceLocator2.getListaDePreciosDao().buscarPrecioVigente(p,MonedasUtils.PESOS, prov, fecha);
		System.out.println(precio);
	}

}
