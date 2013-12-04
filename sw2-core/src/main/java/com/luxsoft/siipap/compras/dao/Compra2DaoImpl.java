package com.luxsoft.siipap.compras.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import com.luxsoft.siipap.compras.model.Compra2;
import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;

@Service("comprasDao")
public class Compra2DaoImpl extends GenericDaoHibernate<Compra2, String> implements Compra2Dao{

	public Compra2DaoImpl() {
		super(Compra2.class);
	}

	public List<Compra2> buscarCompras(Periodo p) {
		String hql="from Compra2 c where c.fecha between ? and ?";
		return getHibernateTemplate().find(hql,new Object[]{p.getFechaInicial(),p.getFechaFinal()});
	}

	public List<CompraUnitaria> buscarComprasPendientesPorProveedor(Proveedor p) {
		String hql="from CompraUnitaria u " +
		" left join fetch u.producto p " +
		" left join fetch u.sucursal s " +
		" where u.proveedor.id=? and u.depuracion is null";
		return getHibernateTemplate().find(hql,p.getId());
	}

	public List<CompraUnitaria> buscarPartidas(Compra2 c) {
		String hql="from CompraUnitaria u " +
				" left join fetch u.producto p " +
				" left join fetch u.sucursal s " +
				" where u.compra.id=?";
		return getHibernateTemplate().find(hql,c.getId());
	}

	public Compra2 buscarPorFolio(int sucursal, int folio) {
		String hql="from Compra2 c left join  fetch c.sucursal s" +
				" left join fetch c.proveedor p " +
				" left join fetch c.partidas l " +
				" where c.sucursal.clave=? " +
				" and c.sucursal.folio=?"; 
		List<Compra2> data=getHibernateTemplate().find(hql, new Object[]{sucursal,folio});
		return data.isEmpty()?null:data.get(0);
	}

	public Compra2 inicializarCompra(String id) {
		String hql="from Compra2 c left join  fetch c.sucursal s" +
				" left join fetch c.proveedor p " +
				" left join fetch c.partidas l " +
				" where c.id=? "; 
		List<Compra2> data=getHibernateTemplate().find(hql, id);
		Compra2 res= data.isEmpty()?null:data.get(0);
		if(res!=null)
			Hibernate.initialize(res.getProveedor().getComentarios());
		return res;
	}
	

	public Compra2 save(Compra2 object) {
		return (Compra2)getHibernateTemplate().merge(object);
	}
	
	public List<Compra2> buscarPendientes(){
		List<String> ids=getHibernateTemplate()
			.find("select distinct cu.compra.id from CompraUnitaria cu where cu.solicitado-cu.depurado-cu.recibido>0 and cu.depuracion is null");
		List<Compra2> res=new ArrayList<Compra2>();
		for(String id:ids){
			Compra2 c=get(id);
			res.add(c);
		}
		//String hql="from Compra2 c where c.depuracion is null";
		//return getHibernateTemplate().find(hql);
		return res;
	}
	
	

	public static void main(String[] args) {
		Compra2Dao dao=(Compra2Dao)ServiceLocator2.instance()
		.getContext().getBean("compra2Dao");
		System.out.println(dao.buscarPendientes().size());
	}

}
