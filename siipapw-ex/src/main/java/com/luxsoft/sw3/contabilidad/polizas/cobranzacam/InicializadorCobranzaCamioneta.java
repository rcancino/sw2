package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

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
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;


public class InicializadorCobranzaCamioneta {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorCobranzaCamioneta(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	
	public void inicializar(ModelMap model){
		pagos(model);
		fichas(model);
		correcciones(model);
		cargos(model);
		anticiposFac(model);
	}
	
	private void pagos(ModelMap model){
		Date fecha=(Date)model.get("fecha");
		final List<Pago> pagos=new ArrayList<Pago>();
		final List<NotaDeCredito> notas=new ArrayList<NotaDeCredito>();
		
		String sql=" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('CAM') and y.ANTICIPO=false and y.tipo_id like 'PAGO%'" +
				" AND CAR_TIPO<>'TES' and x.CAR_ANTICIPO=false" +
				" union " +
				"select a.abono_id from sx_cxc_abonos a where a.fecha = ? and a.anticipo=true and a.origen in('CAM') and TIPO_ID<>'PAGO_TAR' " +
				" union " +
				"select DISTINCT(a.abono_id) from sx_cxc_abonos a JOIN sx_cxc_aplicaciones Z ON(A.ABONO_ID=Z.ABONO_ID) where a.DIFERENCIA_FECHA=? and A.DIFERENCIA>0 and A.TIPO_ID<>'PAGO_HXE' AND Z.CAR_ORIGEN='CAM' and z.CAR_ANTICIPO=false " +
				" AND CAR_TIPO<>'TES' AND A.TIPO_ID NOT LIKE 'NOTA%'" +
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('CAM') and y.ANTICIPO=true and x.CAR_ANTICIPO=false" +
				" union " +
				" select x.abono_id from sx_cxc_abonos x where x.fecha=? and tipo_id like 'NOTA_%' and x.total>0";
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, fecha)	
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
				,new SqlParameterValue(Types.DATE, fecha)
		};
		final List<String> abonos=getJdbcTemplate().queryForList(sql, args,String.class);
		System.out.println("Abonos :"+abonos.size());
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
					if(abono instanceof Pago){
						pagos.add((Pago)abono);
					}
					else if(abono instanceof NotaDeCredito){
						if(abono instanceof NotaDeCreditoBonificacion){
							NotaDeCreditoBonificacion bon=(NotaDeCreditoBonificacion)abono;
							Hibernate.initialize(bon.getConceptos());
						}
						notas.add((NotaDeCredito)abono);
					}
				}
				
				return null;
			}
		});
		//System.out.println("Pagos: "+pagos.size());
		//System.out.println("Notas: "+notas.size());
		model.addAttribute("pagos", GlazedLists.eventList(pagos));
		model.addAttribute("notas", GlazedLists.eventList(notas));
	}
	
	
private void anticiposFac(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Aplicacion> aplic=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<Aplicacion> res= session.createQuery("from Aplicacion f " +
						" where f.fecha=? and f.cargo.origen=\'CAM\'  and f.cargo.anticipo=true and f.abono.tipo != \'PAGO_DIF\' ")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<Aplicacion> source=GlazedLists.eventList(res);
				Comparator<Aplicacion> c=GlazedLists.beanPropertyComparator(Aplicacion.class, "id");
				UniqueList<Aplicacion> aplicaciones=new UniqueList<Aplicacion>(source,c);
				return aplicaciones;
			}
		});
		
		/*for (Cargo car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"113");
			}*/
		
		//System.out.println("aplicaciones procesadas: "+aplic.size());
		
		model.addAttribute("aplicaciones",aplic);
		
	
		
	}
	
	private void fichas(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<Ficha> fichas=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				return session.createQuery("from Ficha f where date(f.fecha)=? and f.origen=\'CAM\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
			}
		});
		//System.out.println("Fichas: "+fichas.size());
		model.put("fichas", fichas);
	}
	
	
private void cargos(ModelMap model){
		
		final Date fecha=(Date)model.get("fecha");
		final List<Cargo> cargos=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<NotaDeCargo> res= session.createQuery("from NotaDeCargo f left join fetch f.conceptos cc " +
						" where f.fecha=? and f.origen=\'CAM\' and f.cliente.frecuente=true")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
				EventList<NotaDeCargo> source=GlazedLists.eventList(res);
				Comparator<NotaDeCargo> c=GlazedLists.beanPropertyComparator(NotaDeCargo.class, "id");
				UniqueList<NotaDeCargo> notas=new UniqueList<NotaDeCargo>(source,c);
				return notas;
			}
		});
		
		/*for (Cargo car:cargos){
			PolizaDetFactory.generarConceptoContable(car.getClave(), car.getNombre(),"113");
			}*/
		
		//System.out.println("Notas de cargo procesadas: "+cargos.size());
		
		model.addAttribute("notasDeCargo",cargos);
		
	
		
	}
	
	
	private void correcciones(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<Ficha> correcciones=getHibernateTemplate().executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				return session.createQuery("from CorreccionDeFicha c where date(c.fecha)=? and c.ficha.origen=\'CAM\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
			}
		});
		//System.out.println("Correcciones Fichas: "+correcciones.size());
		model.put("correcciones", correcciones);
	}
	
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		DBUtils.whereWeAre();
		InicializadorCobranzaCamioneta i=new InicializadorCobranzaCamioneta(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("fecha", DateUtil.toDate("03/01/2012"));
		i.inicializar(model);
			
	}

}
