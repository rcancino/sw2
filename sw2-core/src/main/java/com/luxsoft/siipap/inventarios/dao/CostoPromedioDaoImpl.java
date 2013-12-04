package com.luxsoft.siipap.inventarios.dao;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.luxsoft.siipap.dao.hibernate.GenericDaoHibernate;
import com.luxsoft.siipap.inventarios.model.CostoPromedio;
import com.luxsoft.siipap.model.core.Producto;

public class CostoPromedioDaoImpl extends GenericDaoHibernate<CostoPromedio, Long> implements CostoPromedioDao{

	public CostoPromedioDaoImpl() {
		super(CostoPromedio.class);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public CostoPromedio buscar(final String clave,int year,int mes){		
		String hql="from CostoPromedio cp where cp.producto.clave=? and cp.year=? and cp.mes=?";
		Object[] values={clave,year,mes};
		List<CostoPromedio> found=getHibernateTemplate().find(hql, values);
		return found.isEmpty()?null:found.get(0);
	}
	
	public void eliminarCostoPromedio(int year,int mes){
		int res=getHibernateTemplate().bulkUpdate("delete CostoPromedio cp where cp.year=? and cp.mes=?", new Object[]{year,mes});
		logger.info("Costos promedios eliminados: "+res);
	}
	
	public void eliminarCostoPromedio(final Producto p,int year,int mes){
		CostoPromedio cp=buscar(p.getClave(), year, mes);
		if(cp!=null)
			remove(cp.getId());
	}
	
	
	/**
	 * Localiza el costo promedio para todos los productos en el periodo indicado
	 * 
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final int year,int mes){
		return getHibernateTemplate().find("from CostoPromedio cp where cp.year=? and cp.mes=?",new Object[]{year,mes});
	}
	
	/**
	 * Localiza el costo promedio para el producto en el periodo indicado
	 * 
	 * @param year
	 * @param mes
	 * @return
	 */
	public List<CostoPromedio> buscarCostosPromedios(final String clave,final int year,int mes){
		return getHibernateTemplate().find("from CostoPromedio cp where cp.year=? and cp.mes=? and cp.clave=?",new Object[]{year,mes,clave});
	}
	

}
