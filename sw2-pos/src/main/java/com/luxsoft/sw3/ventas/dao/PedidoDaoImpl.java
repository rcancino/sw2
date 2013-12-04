package com.luxsoft.sw3.ventas.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.sw3.ventas.Pedido;
import com.luxsoft.sw3.ventas.Pedido.Tipo;

@Repository("pedidosDao")
public class PedidoDaoImpl extends GenericDaoHibernate<Pedido, String> implements PedidoDao{

	public PedidoDaoImpl() {
		super(Pedido.class);
		
	}

	@Override
	public Pedido get(String id) {
		
		Pedido p=super.get(id);
		this.getHibernateTemplate().initialize(p.getPartidas());
		this.getHibernateTemplate().initialize(p.getInstruccionDeEntrega());
		this.getHibernateTemplate().initialize(p.getSocio());
		this.getHibernateTemplate().initialize(p.getCliente().getTelefonos());
		
		return p;
	}

	/**
	 * Regresa la lista de los pedidos pendientes por facturar
	 * 
	 * @param sucursal
	 * @return
	 */
	public List<Pedido> buscarPendientes(final Sucursal sucursal) {
		//String hql="from Pedido p where p.sucursal.clave=? and p.facturable=true and p.totalFacturado=0";
		String hql="from Pedido p where p.sucursal.clave=? " +
				"and  p.totalFacturado=0 and p.facturable=false";
		return getHibernateTemplate().find(hql, sucursal.getClave());
	}
	
	public List<Pedido> buscarFacturables(final Sucursal sucursal){
		String hql="from Pedido p where p.sucursal.clave=? " +
				"and  p.totalFacturado=0 and p.facturable=true";
		List<Pedido> res= getHibernateTemplate().find(hql, sucursal.getClave());
		
		return res;
	}

	public List<Pedido> buscarFacturables(Sucursal sucursal, Tipo tipo) {
		String hql="from Pedido p where p.tipo=? and p.sucursal.clave=? " +
		"and  p.totalFacturado=0 and p.facturable=true";
		List<Pedido> res= getHibernateTemplate().find(hql, new Object[]{tipo,sucursal.getClave()});

		return res;
	}

	public Pedido buscarPorFolio(Long folio) {
		List<Pedido> res=getHibernateTemplate().find("from Pedido p where p.folio=?",folio);
		return res.isEmpty()?null:get(res.get(0).getId());
	}
	
	
/*
	@Override
	public Pedido save(Pedido pedido) {
		if(pedido.getId()==null)
			return super.save(pedido);
		else{
			getHibernateTemplate().update(pedido);
			pedido=get(pedido.getId());
			return pedido;
		}	
	}
	
	*/
	
}
