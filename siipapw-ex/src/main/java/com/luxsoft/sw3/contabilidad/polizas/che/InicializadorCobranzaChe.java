package com.luxsoft.sw3.contabilidad.polizas.che;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.ChequeDevuelto;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.PagosPorTipo;


public class InicializadorCobranzaChe {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorCobranzaChe(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		pagos(model);
		fichas(model);
		cargos(model);
		cargosCheDev(model);
		
	}
	
	private void pagos(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		final List<Pago> pagos=new ArrayList<Pago>();
		final List<NotaDeCredito> notas=new ArrayList<NotaDeCredito>();
		
		String sql=" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('CHE') and y.ANTICIPO=false and y.tipo_id like 'PAGO%'" +
				" union " +
				"select a.abono_id from sx_cxc_abonos a where a.fecha = ? and a.anticipo=true and a.origen in('CHE') " +
				" union " +
				"select a.abono_id from sx_cxc_abonos a where a.DIFERENCIA_FECHA=? and a.DIFERENCIA>0 and TIPO_ID<>'PAGO_HXE' and a.origen in('CHE')" +
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('CHE') and y.ANTICIPO=true" +
				" union " +
				" select x.abono_id from sx_cxc_abonos x where x.fecha=? and tipo_id like 'NOTA_%'  and x.origen in('CHE') ";
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)	
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
		};
		final List<String> abonos=getJdbcTemplate().queryForList(sql, args,String.class);
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				for(String id:abonos){
					Abono abono=(Abono)session.load(Abono.class, id);
					if(abono instanceof HibernateProxy)
						abono= (Abono)((HibernateProxy)abono).getHibernateLazyInitializer().getImplementation();
					
					Hibernate.initialize(abono.getAplicaciones());
					abono.getPrimeraAplicacion();
					abono.getSucursal().getNombre();
					abono.getOrigenAplicacion();
					if(abono instanceof Pago)
						pagos.add((Pago)abono);
					else if(abono instanceof NotaDeCredito){
						if(abono instanceof NotaDeCreditoBonificacion){
							NotaDeCreditoBonificacion bon=(NotaDeCreditoBonificacion)abono;
							Hibernate.initialize(bon.getConceptos());
						}
						notas.add((NotaDeCredito)abono);
					}
					//return null;
				}
				
				return null;
			}
		});
		System.out.println("Abonos: "+abonos.size());
		EventList<Pago> allPagos=GlazedLists.eventList(pagos);
		PagosPorTipo pagosPorTipo=new PagosPorTipo(allPagos,fecha);
		model.addAttribute("pagos", allPagos);
		model.addAttribute("pagosPorTipo",pagosPorTipo);
		model.addAttribute("notas", GlazedLists.eventList(notas));
		
		
		System.out.println("Pagos registrados: "+pagos.size());
		System.out.println("Notas registradas: "+notas.size());
		//System.out.println("Pagos aplicados por tipo: "+pagosPorTipo.resumenAplicado(fecha));
		
	}
	
	private void fichas(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<Ficha> fichas=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				return session.createQuery("from Ficha f where date(f.fecha)=? and f.origen=\'CHE\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
			}
		});
		model.addAttribute("fichas",fichas);
		
	}
	
	private void cargos(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Cargo> cargos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<NotaDeCargo> res= session.createQuery("from NotaDeCargo f left join fetch f.conceptos cc " +
						" where f.fecha=? and f.origen=\'CHE\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<NotaDeCargo> source=GlazedLists.eventList(res);
				Comparator<NotaDeCargo> c=GlazedLists.beanPropertyComparator(NotaDeCargo.class, "id");
				UniqueList<NotaDeCargo> notas=new UniqueList<NotaDeCargo>(source,c);
				return notas;
			}
		});
		
		for (Cargo car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"113");
			}
		
		System.out.println("Notas de cargo procesadas: "+cargos.size());
		
		model.addAttribute("notasDeCargo",cargos);
		
	
		
	}

	private void cargosCheDev(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Cargo> cargos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<ChequeDevuelto> res= session.createQuery("from  ChequeDevuelto f  " +
						" where f.fecha=? and f.origen=\'CHE\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<ChequeDevuelto> source=GlazedLists.eventList(res);
				Comparator<ChequeDevuelto> c=GlazedLists.beanPropertyComparator(ChequeDevuelto.class, "id");
				UniqueList<ChequeDevuelto> notas=new UniqueList<ChequeDevuelto>(source,c);
				return notas;
			}
		});
		
		for (Cargo car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"113");
			}
		
		System.out.println("Cheques devueltos procesados "+cargos.size());
		
		model.addAttribute("chequesDev",cargos);
		

		
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		InicializadorCobranzaChe i=new InicializadorCobranzaChe(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("fecha", DateUtil.toDate("01/12/2011"));
		i.inicializar(model);
			
	}

}
