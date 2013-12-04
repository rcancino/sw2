package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;


public class InicializadorParaDepositosEnTransito {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorParaDepositosEnTransito(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public void inicializar(ModelMap model){
		inicializarCortes(model);
	}
	
	public InicializadorParaDepositosEnTransito inicializarCortes(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		EventList<CorteDeTarjeta> source=GlazedLists.eventList(hibernateTemplate.find(
				"from CorteDeTarjeta c left join fetch c.aplicaciones ap where c.tipoDeTarjeta=? " +
				" and ap.cargoAbono.fechaDeposito=? " 
				+"and ap.cargoAbono.fecha!=ap.cargoAbono.fechaDeposito"
						,new Object[]{CorteDeTarjeta.TIPOS_DE_TARJETAS[1],fecha})
						);
		UniqueList<CorteDeTarjeta> cortes=new UniqueList<CorteDeTarjeta>(source,GlazedLists.beanPropertyComparator(CorteDeTarjeta.class,"id"));
		System.out.println(" Cortes registrados: "+cortes.size());
		model.addAttribute("cortes",cortes);
		return this;
	}
	
	
	public static void main(String[] args) {
		InicializadorParaDepositosEnTransito i=new InicializadorParaDepositosEnTransito(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("fecha", DateUtil.toDate("01/04/2012"));
		i.inicializarCortes(model);
			
	}

}
