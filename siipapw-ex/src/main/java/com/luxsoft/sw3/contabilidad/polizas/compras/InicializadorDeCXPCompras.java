package com.luxsoft.sw3.contabilidad.polizas.compras;

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
import com.luxsoft.siipap.cxp.model.AnalisisDeTransformacion;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.maquila.model.AnalisisDeFlete;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;
import com.luxsoft.utils.LoggerHelper;


public class InicializadorDeCXPCompras {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	Logger logger=LoggerHelper.getLogger();
	
	public InicializadorDeCXPCompras(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		compras(model);
		anticipos(model);
		cargarFletesHojeo(model);
		diferencias(model);
      	
	}
	
	private void compras(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		Comparator c=GlazedLists.beanPropertyComparator(CXPFactura.class, "id");
		final UniqueList<CXPFactura> facturas=new UniqueList<CXPFactura>(new BasicEventList<CXPFactura>(0),c); 
		List<AnalisisDeFactura> allAnalisis=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="select distinct det.analisis.id from AnalisisDeFacturaDet det where date(det.entrada.fecha)=?";
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql)
						.setParameter(0, fecha,Hibernate.DATE)
						.list());
				
				List<AnalisisDeFactura> res=new ArrayList<AnalisisDeFactura>();
				for(Long id:ids){
					AnalisisDeFactura a=(AnalisisDeFactura)session.load(AnalisisDeFactura.class, id);					
					for(AnalisisDeFacturaDet det:a.getPartidas()){
						Hibernate.initialize(det.getEntrada().getRecepcion());	
						
					}
					facturas.add(a.getFactura());
					
					res.add(a);
				}	
				for(CXPFactura fac:facturas){
					for(AnalisisDeFactura ax:fac.getAnalisis()){
						Hibernate.initialize(ax.getPartidas());
					}
					if(fac.getAnticipo()!=null){
						Hibernate.initialize(fac.getAnticipo());
					}
					//PolizaDetFactory.generarConceptoContable(fac.getClave(), fac.getNombre(), "200");
					
				}
				return res;
			}
		});
		//System.out.println("Analisis registrados: "+allAnalisis.size());
		model.addAttribute("analisis", GlazedLists.eventList(allAnalisis));
		model.addAttribute("facturas",facturas);
		
			
	}

	
	
	private void anticipos(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		
		List<CXPFactura> allAnticipos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="from CXPFactura ant where ant.fecha=? and  ant.anticipof is true";
				Set<CXPFactura> ids=new HashSet<CXPFactura>(session.createQuery(hql)
						.setParameter(0, fecha,Hibernate.DATE)
						.list());
				
				List<CXPFactura> res=new ArrayList<CXPFactura>();
				
				for(CXPFactura id:ids){
					//CXPFactura a=(CXPFactura)session.load(CXPFactura.class, id);					
				    
					res.add(id);
					//System.out.println("anticipo nuestro: "+id);
					PolizaDetFactory.generarConceptoContable(id.getClave(), id.getNombre(), "200");
				}	
				
				return res;
		
			}
		});
		
		
	/*	List<CXPFactura> allAnticipos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="select ant.id from CXPFactura ant where ant.fecha=? and  ant.anticipof is true";
				Set<Long> ids=new HashSet<Long>(session.createQuery(hql)
						.setParameter(0, fecha,Hibernate.DATE)
						.list());
				
				List<CXPFactura> res=new ArrayList<CXPFactura>();
				
				for(Long id:ids){
								    
					CXPFactura a= (CXPFactura)session.load(CXPFactura.class, id);
					res.add(a);
				}	
				
				return res;
			}
		});*/
		
		//System.out.println("Anticipos registrados: "+allAnticipos.size());
		model.addAttribute("anticipos",GlazedLists.eventList(allAnticipos));
		
			
	}
	
	private void cargarFletesHojeo(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		Comparator c1=GlazedLists.beanPropertyComparator(AnalisisDeFlete.class, "id");
		Comparator c2=GlazedLists.beanPropertyComparator(AnalisisDeHojeo.class, "id");
		Comparator c3=GlazedLists.beanPropertyComparator(AnalisisDeTransformacion.class, "id");
		
		final UniqueList<AnalisisDeFlete> analisisDeFlete=new UniqueList<AnalisisDeFlete>(new BasicEventList<AnalisisDeFlete>(0),c1);
		final UniqueList<AnalisisDeHojeo> analisisDeHojeo=new UniqueList<AnalisisDeHojeo>(new BasicEventList<AnalisisDeHojeo>(0),c2);
		final UniqueList<AnalisisDeTransformacion> analisisDeTransformaciones=new UniqueList<AnalisisDeTransformacion>(new BasicEventList<AnalisisDeTransformacion>(0),c3);
		 
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				
				analisisDeTransformaciones
				.addAll(session.createQuery("from AnalisisDeTransformacion a left join fetch a.partidas pp where a.cxpFactura.fecha=?")
						.setParameter(0,fecha, Hibernate.DATE)
						.list()
						);
				
				analisisDeHojeo
				.addAll(session.createQuery("from AnalisisDeHojeo a left join fetch a.entradas pp where a.fechaFactura=? and a.cxpFactura is not null ")
						.setParameter(0,fecha, Hibernate.DATE)
						.list()
						);
				for(AnalisisDeHojeo a:analisisDeHojeo){
					Hibernate.initialize(a.getTransformaciones());
				}
				
				analisisDeFlete
				.addAll(session.createQuery("from AnalisisDeFlete a left join fetch a.entradas pp where a.cxpFactura.fecha=?")
						.setParameter(0,fecha, Hibernate.DATE)
						.list()
						);
				for(AnalisisDeFlete a:analisisDeFlete){
					Hibernate.initialize(a.getTraslados());
					Hibernate.initialize(a.getTransformaciones());
					Hibernate.initialize(a.getComs());
				}
				return null;
			}
		});
		if(!analisisDeFlete.isEmpty()){
			logger.info("Analisis de flete: "+analisisDeFlete.size());
		}
		if(!analisisDeHojeo.isEmpty()){
			logger.info("Analisis de Hojeo: "+analisisDeHojeo.size());
		}
		if(!analisisDeTransformaciones.isEmpty()){
			logger.info("Analisis de Trs: "+analisisDeTransformaciones.size());
		}
		model.addAttribute("analisisDeFlete", analisisDeFlete);
		model.addAttribute("analisisDeHojeo", analisisDeHojeo);
		model.addAttribute("analisisDeTransformaciones", analisisDeTransformaciones);
	}
	
	private void diferencias(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<CXPFactura> diferencias=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				String hql="from CXPFactura f where f.diferenciaFecha=?";
				return session.createQuery(hql)
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
			}
		});
		model.addAttribute("diferencias",GlazedLists.eventList(diferencias));
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}



}
