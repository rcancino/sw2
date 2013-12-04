package com.luxsoft.sw3.contabilidad.dao;


import java.util.List;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.sw3.contabilidad.model.Poliza;

public class PolizaDaoImpl extends GenericDaoHibernate<Poliza, Long> implements PolizaDao{

	public PolizaDaoImpl() {
		super(Poliza.class);		
	}
	

	@Override
	public Poliza get(Long id) {
		String hql="from Poliza p left join fetch p.partidas where p.id=?";
		List<Poliza> res=getHibernateTemplate().find(hql, id);
		return res.isEmpty()?null:res.get(0);
	}


	public Long buscarProximaPoliza(int year,int mes,String clase) {
		String hql="select max(folio) from Poliza p where year(p.fecha)=? and month(p.fecha)=? and p.clase=?";
		List res=getHibernateTemplate().find(hql,new Object[]{year,mes,clase});
		if(res.isEmpty())
			return 1l;
		Number val=(Number)res.get(0);
		if(val==null)
			return 1l;
		return val.longValue()+1;
	}
	
	public Long buscarProximaPoliza(int year,int mes,String clase,String tipo) {
		String hql="select max(folio) from Poliza p where year(p.fecha)=? and month(p.fecha)=? and p.clase=? and p.tipo='@TIPO'";
		hql=hql.replaceAll("@TIPO", tipo);
		List res=getHibernateTemplate().find(hql,new Object[]{year,mes,clase});
		if(res.isEmpty())
			return 1l;
		Number val=(Number)res.get(0);
		if(val==null)
			return 1l;
		return val.longValue()+1;
	}

}
