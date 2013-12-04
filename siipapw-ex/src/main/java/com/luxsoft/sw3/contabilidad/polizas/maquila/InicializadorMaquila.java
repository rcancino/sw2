package com.luxsoft.sw3.contabilidad.polizas.maquila;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;


import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.sw3.maquila.model.AnalisisDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterial;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.utils.LoggerHelper;


public class InicializadorMaquila {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	Logger logger=LoggerHelper.getLogger();
	
	public InicializadorMaquila(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		analisis(model);
      	
	}
	
	
/*	private void analisis(ModelMap model){
		final Date fecha=(Date)model.get("fecha"); 
		List<AnalisisDeMaterial> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="from AnalisisDeMaterial a where date(a.fecha)=?";
				Set<AnalisisDeMaterial> a=new HashSet<AnalisisDeMaterial> (session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE)
						.list());
				List<AnalisisDeMaterial> res=new ArrayList<AnalisisDeMaterial>();
				
				for(AnalisisDeMaterial b:a){
					Hibernate.initialize(b.getEntradas());
					res.add(b);
				}
				
				return res;
			}
			
		});
		
				
			model.addAttribute("analisis", GlazedLists.eventList(allAnalisis));
			
			
				
		}
	*/
		
	
	private void analisis(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		Comparator c=GlazedLists.beanPropertyComparator(CXPFactura.class, "id");
		final UniqueList<CXPFactura> facturas=new UniqueList<CXPFactura>(new BasicEventList<CXPFactura>(0),c); 
		List<AnalisisDeMaterial> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				String hql="select distinct det.analisis.id from EntradaDeMaterialDet det where date(det.fecha)=?  and  analisis!=null";				
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());				
				
				
				List<AnalisisDeMaterial> res=new ArrayList<AnalisisDeMaterial>();
				for(Long id:ids){
					AnalisisDeMaterial a=(AnalisisDeMaterial)session.load(AnalisisDeMaterial.class, id);
					 Hibernate.initialize(a.getEntradas());
					for(EntradaDeMaterialDet ent:a.getEntradas()){
						Hibernate.initialize(a.getEntradas());
					}
					facturas.add(a.getCxpFactura());
					
					res.add(a);
				}	
/*				for(CXPFactura fac:facturas){
					if(fac.getAnticipo()!=null){
						Hibernate.initialize(fac.getAnticipo());
					}
				}*/
				return res;
			}
		});
		System.out.println("Analisis de Material registrados: "+allAnalisis.size());
		model.addAttribute("analisisDeMaterial", GlazedLists.eventList(allAnalisis));		
	}
		
		
	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
}
