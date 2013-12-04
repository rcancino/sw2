package com.luxsoft.sw3.contabilidad.polizas.complementocom;

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

import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.utils.LoggerHelper;


public class InicializadorComplementoCom {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	Logger logger=LoggerHelper.getLogger();
	
	public InicializadorComplementoCom(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		analisisDeHojeo(model);
		analisisDeTransformacion(model);
		flete(model);
      	
	}
	
	/**
	 * Carga todos los analisis de hojeo correspondientes a la fecha de la poliza y que participen maquila y transformaciones
	 * 
	 * @param model
	 */
	private void analisisDeHojeo(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		Comparator c=GlazedLists.beanPropertyComparator(CXPFactura.class, "id");
		final UniqueList<CXPFactura> facturas=new UniqueList<CXPFactura>(new BasicEventList<CXPFactura>(0),c); 
		List<AnalisisDeHojeo> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				String hql="select distinct det.analisisHojeo.id from EntradaDeMaquila det where date(det.fecha)=?  and  analisisHojeo!=null";				
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());				
				
				hql="select distinct det.analisisHojeo.id from TransformacionDet det where date(det.fecha)=? and  analisisHojeo!=null";
				ids.addAll(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());
				
				List<AnalisisDeHojeo> res=new ArrayList<AnalisisDeHojeo>();
				for(Long id:ids){
					AnalisisDeHojeo a=(AnalisisDeHojeo)session.load(AnalisisDeHojeo.class, id);
					Hibernate.initialize(a.getEntradas());
					Hibernate.initialize(a.getTransformaciones());
					for(EntradaDeMaquila det:a.getEntradas()){
						Hibernate.initialize(det.getRecepcion());
					}
					facturas.add(a.getCxpFactura());
					
					res.add(a);
				}	
				for(CXPFactura fac:facturas){
					if(fac.getAnticipo()!=null){
						Hibernate.initialize(fac.getAnticipo());
					}
				}
				return res;
			}
		});
		System.out.println("Analisis de hojeo registrados: "+allAnalisis.size());
		model.addAttribute("analisisDeHojeo", GlazedLists.eventList(allAnalisis));		
	}

	
	
	private void analisisDeTransformacion(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		Comparator c=GlazedLists.beanPropertyComparator(CXPFactura.class, "id");
		final UniqueList<CXPFactura> facturas=new UniqueList<CXPFactura>(new BasicEventList<CXPFactura>(0),c); 
		List<AnalisisDeTransformacion> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="select distinct det.analisis.id from TransformacionDet det where date(det.fecha)=? and  analisis!=null";
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql)
						.setParameter(0, fecha,Hibernate.DATE)
						.list());
				
				
				List<AnalisisDeTransformacion> res=new ArrayList<AnalisisDeTransformacion>();
				for(Long id:ids){
					AnalisisDeTransformacion a=(AnalisisDeTransformacion)session.load(AnalisisDeTransformacion.class, id);
					Hibernate.initialize(a.getPartidas());
					facturas.add(a.getCxpFactura());
					res.add(a);
				}	
				for(CXPFactura fac:facturas){
					if(fac.getAnticipo()!=null){
						Hibernate.initialize(fac.getAnticipo());
					}
				}
				return res;
			}
		});
		System.out.println("Analisis de trnsformaciones registrados: "+allAnalisis.size());
		model.addAttribute("analisisDeTransfomracion", GlazedLists.eventList(allAnalisis));
			
	}
	
	private void flete(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<AnalisisDeFlete> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				String hql="select distinct det.analisisFlete.id from EntradaDeMaquila det where date(det.fecha)=?  and  analisisFlete!=null";
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());
				
				hql="select distinct det.analisisFlete.id from TransformacionDet det where date(det.fecha)=? and  analisisFlete!=null";
				ids.addAll(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());
				
				hql="select distinct det.analisisFlete.id from EntradaPorCompra det where date(det.fecha)=? and  analisisFlete!=null";
				ids.addAll(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());
				
				hql="select distinct det.analisisFlete.id from TrasladoDet det where date(det.fecha)=? and  analisisFlete!=null";
				ids.addAll(session.createQuery(hql).setParameter(0, fecha,Hibernate.DATE).list());
				
				
				List<AnalisisDeFlete> res=new ArrayList<AnalisisDeFlete>();
				for(Long id:ids){
					AnalisisDeFlete a=(AnalisisDeFlete)session.load(AnalisisDeFlete.class, id);					
					Hibernate.initialize(a.getEntradas());
					Hibernate.initialize(a.getTransformaciones());
					Hibernate.initialize(a.getTraslados());
					Hibernate.initialize(a.getComs());
					res.add(a);
				}	
				
				return res;
			}
		});
		System.out.println("Analisis de fletes registrados: "+allAnalisis.size());
		model.addAttribute("analisisDeFlete", GlazedLists.eventList(allAnalisis));	
	}
	
	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}



}
