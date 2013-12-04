package com.luxsoft.sw3.contabilidad.polizas.jur;

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
import com.luxsoft.siipap.cxc.model.Juridico;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoEnEspecie;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.cobranza.PagosPorTipo;


public class InicializadorCobranzaJur {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorCobranzaJur(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		pagos(model);
		fichas(model);
		cargos(model);
		traspasoJuridico(model);
		
		
	}
	
	private void pagos(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		final List<Pago> pagos=new ArrayList<Pago>();
		final List<NotaDeCredito> notas=new ArrayList<NotaDeCredito>();
		
		String sql=" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) join sx_juridico j on (x.cargo_id=j.cargo_id)" +
				" where x.fecha=?  and y.ANTICIPO=false and y.tipo_id like 'PAGO%' and x.fecha>=j.traspaso " +
				" union " +
				" select a.abono_id from sx_cxc_abonos a where a.DIFERENCIA_FECHA=? and a.DIFERENCIA>0 and TIPO_ID<>'PAGO_HXE' and a.origen in('JUR') " +
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) join sx_juridico j on (x.cargo_id=j.cargo_id)" +
				" where x.fecha=? and  y.ANTICIPO=true and x.fecha>=j.traspaso" +
				" union " +
				" select x.abono_id from sx_cxc_abonos x where x.fecha=? and tipo_id like 'NOTA_%'  and x.origen in('JUR') ";
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)	
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
					{
						PolizaDetFactory.generarConceptoContable(abono.getClave(), abono.getNombre(), "114");
						if(abono instanceof PagoEnEspecie && abono.getOrigen().equals(OrigenDeOperacion.JUR))
							PolizaDetFactory.generarConceptoContable(abono.getClave(), abono.getNombre(), "115");
						
						pagos.add((Pago)abono);
					}
					else if(abono instanceof NotaDeCredito){
						//System.out.println("es una nota de credito");
						if(abono instanceof NotaDeCreditoBonificacion){
							//System.out.println("Es una bonificacion");
							NotaDeCreditoBonificacion bon=(NotaDeCreditoBonificacion)abono;
							Hibernate.initialize(bon.getConceptos());
							// Generar el concepto contable si no existe en la cuenta de Juridico
							PolizaDetFactory.generarConceptoContable(bon.getClave(), bon.getNombre(), "114");
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
		
		
		//System.out.println("Pagos registrados: "+pagos.size());
		//System.out.println("Notas registradas: "+notas.size());
		//System.out.println("Pagos aplicados por tipo: "+pagosPorTipo.resumenAplicado(fecha));
		
	}
	
	private void fichas(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<Ficha> fichas=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				return session.createQuery("from Ficha f where date(f.fecha)=? and f.origen=\'JUR\'")
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
						" where f.fecha=? and f.origen=\'JUR\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<NotaDeCargo> source=GlazedLists.eventList(res);
				Comparator<NotaDeCargo> c=GlazedLists.beanPropertyComparator(NotaDeCargo.class, "id");
				UniqueList<NotaDeCargo> notas=new UniqueList<NotaDeCargo>(source,c);
				return notas;
			}
		});
		
		for (Cargo car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"114");
			}
		
		System.out.println("Notas de cargo procesadas: "+cargos.size());
		
		model.addAttribute("notasDeCargo",cargos);
		
	
		
	}

	private void traspasoJuridico(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Juridico> cargos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<Juridico> res= session.createQuery("from  Juridico f  " +
						" where f.traspaso=?")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<Juridico> source=GlazedLists.eventList(res);
				Comparator<Juridico> c=GlazedLists.beanPropertyComparator(Juridico.class, "id");
				UniqueList<Juridico> notas=new UniqueList<Juridico>(source,c);
				return notas;
			}
		});
		
		for (Juridico car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"114");
			}
		System.out.println("Traspasos a Juridico procesados "+cargos.size());
		
		model.addAttribute("traspasosJur",cargos);
		
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		InicializadorCobranzaJur i=new InicializadorCobranzaJur(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("fecha", DateUtil.toDate("01/12/2011"));
		i.inicializar(model);
			
	}

}
