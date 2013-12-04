package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;


public class InteresesPrestamoCamionetaInicializador {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InteresesPrestamoCamionetaInicializador(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		cargos(model);
	}	
	
	private void cargos(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Cargo> cargos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<NotaDeCargo> res= session.createQuery("from NotaDeCargo f left join fetch f.conceptos cc " +
						" where f.fecha=? and f.origen=\'CAM\' and f.cliente.frecuente=false")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<NotaDeCargo> source=GlazedLists.eventList(res);
				Comparator<NotaDeCargo> c=GlazedLists.beanPropertyComparator(NotaDeCargo.class, "id");
				UniqueList<NotaDeCargo> notas=new UniqueList<NotaDeCargo>(source,c);
				return notas;
			}
		});
		System.out.println("Notas de cargo procesadas: "+cargos.size());
		model.addAttribute("notasDeCargo",cargos);
		
	
		
	}
	
	
	
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


}
