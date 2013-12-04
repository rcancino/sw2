package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.Inversion;


public class InicializadorDeControladorDeTesoreria {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorDeControladorDeTesoreria(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public void inicializar(ModelMap model){
		transferencias(model);
		inversion(model);
		retornoDeinversion(model);
		comisiones(model);
		cargarMorralla(model);
		//cargarProvisionIDE(model);
	}
	
	private void transferencias(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="from CargoAbono c left join fetch c.traspaso t" +
			" where t.class=TraspasoDeCuenta and c.fecha=? and c.traspaso!=null ";		
		final List<CargoAbono> transferencias=getHibernateTemplate().find(hql,fecha);
		model.addAttribute("transferencias", transferencias);
		
	}
	
	private void inversion(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="from Inversion i left join fetch i. movimientos m" +
			" where  i.fecha=? ";		
		final EventList data=GlazedLists.eventList(getHibernateTemplate().find(hql,fecha));
		Comparator c=GlazedLists.beanPropertyComparator(Inversion.class, "id");
		UniqueList inversiones= new UniqueList(data,c);
		model.addAttribute("inversiones",inversiones);
		
	}
	
	private void retornoDeinversion(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="select m from Inversion i join i.movimientos m  where m.fecha=? and m.clasificacion='RETIRO_POR_INVERSION'";
		final EventList movimientos=GlazedLists.eventList(getHibernateTemplate().find(hql,fecha));
		model.addAttribute("retornoDeinversiones",movimientos);
		
	}
	
	private void comisiones(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="from ComisionBancaria i " +
			" where  i.fecha=? ";
		model.addAttribute("comisiones",getHibernateTemplate().find(hql,fecha));
		
	}
	
	private void cargarMorralla(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="from CargoAbono c " +
			" where  c.fecha=? and c.concepto.id in(737294L,737321L,737331L,737343L,737345L,737337L,737335L,737330L)";		
		final List<CargoAbono> transferencias=getHibernateTemplate().find(hql,fecha);
		model.addAttribute("movimientos", transferencias);
	}
	
/*	private void cargarProvisionIDE(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		String hql="from Requisicion r " +
			" where  r.fecha=? and r.concepto.id in(737337L) and r.pago is null";		
		final List<Requisicion> reqIds=getHibernateTemplate().find(hql,fecha);
		model.addAttribute("reqIds", reqIds);		
	}*/
	
		
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		List<CargoAbono> data=ServiceLocator2.getHibernateTemplate().find(
				" select m from Inversion i join i.movimientos m  where m.fecha=? and m.clasificacion='RETIRO_POR_INVERSION'"
				,DateUtil.toDate("06/03/2012"));
		for(CargoAbono o:data){
			System.out.println("Row: "+o.getImporte()+"  "+o.getClasificacion()+  "  Fecha "+o.getFecha());
		}
		/*
		DBUtils.whereWeAre();
		InicializadorDeControladorDeTesoreria i=new InicializadorDeControladorDeTesoreria(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("periodo", Periodo.getPeriodoDelMesActual(DateUtil.toDate("04/01/2012")));
		i.inicializar(model);*/
			
	}

}
