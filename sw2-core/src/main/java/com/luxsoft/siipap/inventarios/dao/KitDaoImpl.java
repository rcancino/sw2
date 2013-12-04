package com.luxsoft.siipap.inventarios.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.list.SetUniqueList;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.Kit;
import com.luxsoft.siipap.inventarios.model.KitDet;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.ConfiguracionKit;

public class KitDaoImpl extends GenericDaoHibernate<Kit, Long> implements KitDao{

	public KitDaoImpl() {
		super(Kit.class);
	}

	public List<Kit> buscarMovimientsKit(final Periodo p) {
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List res=session.createQuery("from Kit k " +
						" left join fetch k.salidas " +
						" left join k.entrada" +
						" where k.fecha between ? and ?")
						.setParameter(0, p.getFechaInicial(),Hibernate.DATE)
						.setParameter(0, p.getFechaFinal(),Hibernate.DATE)
						.list();
				SetUniqueList list=SetUniqueList.decorate(res);
				return list;
			}
			
		});
	}

	/**
	 * Arma el numero adecuado de salidas para atender la entrada indicada
	 * del producto kit
	 * 
	 */
	public List<KitDet> prepararSalidas(ConfiguracionKit config,final KitDet target) {
		final List<KitDet> res=new ArrayList<KitDet>();
		
		int cantidad=(int)target.getCantidad();
		
		// Armando las salidas
		for(ConfiguracionKit.Elemento e:config.getPartes()){
			KitDet det=new KitDet();
			det.setCantidad(e.getCantidad()*cantidad);
			det.setProducto(e.getProducto());
			res.add(det);
		}
		
		return res;
	}
	
	

}
