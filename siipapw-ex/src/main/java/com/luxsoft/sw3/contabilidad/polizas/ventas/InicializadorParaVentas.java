package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.cxp.CxP2.Origen;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.tesoreria.model.CorteDeTarjeta;


public class InicializadorParaVentas {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorParaVentas(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public InicializadorParaVentas inicializarVentas(ModelMap model){
		
		String hql="select new com.luxsoft.sw3.contabilidad.polizas.ventas.VentaPorSucursal( " +
					" sucursal.nombre,v.origen,sum(v.importeporTc),sum(v.impuestoporTc),sum(v.totalporTc),sum(v.antAplicporTc),v.clave,v.anticipo ) " +
					" from Venta v where v.fecha=? " +
					" and v.origen in(\'CRE\')" +
					" group by v.sucursal.nombre,v.origen,v.clave,v.anticipo";
		Date fecha=(Date)model.get("fecha");
		
		List<VentaPorSucursal> ventas=hibernateTemplate.find(hql,fecha);
		
		hql="select new com.luxsoft.sw3.contabilidad.polizas.ventas.VentaPorSucursal( " +
				" sucursal.nombre,v.origen,sum(v.importeporTc),sum(v.impuestoporTc),sum(v.totalporTc),sum(v.antAplicporTc),v.clave,v.anticipo ) " +
				" from Venta v where v.fecha=? " +
				" and v.origen in(\'CAM\') and (v.anticipo is true or v.anticipoAplicado>0)" +
				" group by v.sucursal.nombre,v.origen,v.clave,v.anticipo";
		List<VentaPorSucursal>ventasCamionetaAnt=hibernateTemplate.find(hql,fecha);
		ventas.addAll(ventasCamionetaAnt);
		
		hql="select new com.luxsoft.sw3.contabilidad.polizas.ventas.VentaPorSucursal( " +
				" sucursal.nombre,v.origen,sum(v.importeporTc),sum(v.impuestoporTc),sum(v.totalporTc),sum(v.saldo),'CAMIONETA',v.anticipo ) " +
				" from Venta v where v.fecha=? " +
				" and v.origen in(\'CAM\') " +
				" and v.total>0  " +
				" and v.anticipo is false" +
			//	"and (v.anticipo is false and v.anticipoAplicado=0) " +			
			//	"and ((v.anticipo is false and v.anticipoAplicado=0)  OR (v.total>0 OR v.comentario like '%CANCELAD%')) " +				 
				" group by v.sucursal.nombre,v.origen,v.anticipo";
		List<VentaPorSucursal>ventasCamioneta=hibernateTemplate.find(hql,fecha);
		ventas.addAll(ventasCamioneta);
		
		hql="select new com.luxsoft.sw3.contabilidad.polizas.ventas.VentaPorSucursal( " +
				" sucursal.nombre,v.origen,sum(v.importeporTc),sum(v.impuestoporTc),sum(v.totalporTc),sum(v.antAplicporTc),'MOSTRADOR',v.anticipo ) " +
				" from Venta v where v.fecha=? " +
				" and v.origen in(\'MOS\')" +
				" group by v.sucursal.nombre,v.origen,v.anticipo";
		List<VentaPorSucursal>ventasMostrador=hibernateTemplate.find(hql,fecha);
		ventas.addAll(ventasMostrador);
		
		model.addAttribute("ventas",GlazedLists.eventList(ventas));
		/*
		{
			System.out.println("Ventas por sucursal y origen: "+ventas.size());
			for(VentaPorSucursal vv:ventas){
				System.out.println(vv);
			}			
		}*/		
		return this;
		
	}
	
	public InicializadorParaVentas inicializarAbonos(ModelMap model){
		
		final List<Pago> pagos=new ArrayList<Pago>();
		final List<NotaDeCredito> notas=new ArrayList<NotaDeCredito>();
		
		Date fecha=(Date)model.get("fecha");
		String sql=" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('MOS') and y.ANTICIPO=false and y.tipo_id like 'PAGO%' and tipo_id<>'PAGO_TAR' and x.car_tipo<>'TES'" +
				" union " +
				"select a.abono_id from sx_cxc_abonos a where a.fecha = ? and a.anticipo=true and a.origen in('MOS') and TIPO_ID<>'PAGO_TAR' " +
				" union " +
				" select a.abono_id from sx_cxc_abonos a where a.fecha = ? and a.anticipo=true and a.origen in('MOS','CAM') and a.tipo_id='PAGO_TAR'" +
				" union " +
				" select DISTINCT(a.abono_id) from sx_cxc_abonos a JOIN sx_cxc_aplicaciones Z ON(A.ABONO_ID=Z.ABONO_ID) where a.DIFERENCIA_FECHA=? and A.DIFERENCIA>0 and A.TIPO_ID<>'PAGO_HXE' AND Z.CAR_ORIGEN='MOS' " +
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=? and x.CAR_ORIGEN in('MOS') and y.ANTICIPO=true and x.car_tipo<>'TES'" +
				" union " +
				" select x.abono_id from sx_cxc_abonos x where x.fecha=? and tipo_id like 'NOTA_%'  and x.total>0" +
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=?  and y.ANTICIPO=false and y.tipo_id like 'PAGO%' and tipo_id='PAGO_TAR' and x.car_tipo<>'TES'"+
				" union " +
				" select distinct(y.abono_id) from sx_cxc_aplicaciones x    join sx_cxc_abonos y on(x.abono_id=y.abono_id) where x.fecha=?  and y.ANTICIPO=false and y.tipo_id like 'PAGO%' and tipo_id='PAGO_EFE' and x.car_tipo<>'TES'";
		Object[] args=new Object[]{
		new SqlParameterValue(Types.DATE, fecha)	
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		,new SqlParameterValue(Types.DATE, fecha)
		};
		final List<String> abonos=jdbcTemplate.queryForList(sql, args,String.class);
		//System.out.println("Abonos a procesar: "+abonos.size());
		hibernateTemplate.execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				int buffer=0;
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
					buffer++;
					if(buffer%20==0){
						session.flush();
						session.clear();
					}
				}
				return null;
				
			}
		});
		model.addAttribute("pagos",GlazedLists.eventList(pagos));
		model.addAttribute("notas",GlazedLists.eventList(notas));
		
		{
			//System.out.println("Pagos: "+pagos.size());
			//System.out.println("Notas: "+notas.size());
						
		}	
		return this;
	}
	
	public InicializadorParaVentas inicializarFichas(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		
		List<Ficha> fichasEfe=hibernateTemplate.find("from Ficha f where (date(f.fechaDep)=? or date(f.fecha)=?) and f.origen=\'MOS\' and f.tipoDeFicha=\'EFECTIVO\'",new Date[]{fecha,fecha});
		model.addAttribute("fichasEfe",fichasEfe);
		List<Ficha> fichas=hibernateTemplate.find("from Ficha f where date(f.fecha)=? and f.origen=\'MOS\' and f.tipoDeFicha!=\'EFECTIVO\'",new Date[]{fecha});
		model.addAttribute("fichas",fichas);
		/*List<Ficha> fichas=hibernateTemplate.find("from Ficha f where date(f.fecha)=? and f.origen=\'MOS\' and f.tipoDeFicha!=\'EFECTIVO\'"
						+ " union "
						+ " from Ficha f where (date(f.fechaDep)=? or date(f.fecha)=?) and f.origen=\'MOS\' and f.tipoDeFicha=\'EFECTIVO\'",new Date[]{fecha,fecha,fecha});
		model.addAttribute("fichas",fichas);*/
		return this;
	}
	
	public InicializadorParaVentas inicializarCortes(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		EventList<CorteDeTarjeta> source=GlazedLists.eventList(hibernateTemplate.find(
				"from CorteDeTarjeta c left join fetch c.aplicaciones ap where c.corte=? ",fecha));
		UniqueList<CorteDeTarjeta> cortes=new UniqueList<CorteDeTarjeta>(source,GlazedLists.beanPropertyComparator(CorteDeTarjeta.class,"id"));
		//System.out.println(" Cortes registrados: "+cortes.size());
		model.addAttribute("cortes",cortes);
		return this;
	}
	
	
	
	public InicializadorParaVentas inicializarCorreccionesDeFichas(ModelMap model){
		final Date fecha=(Date)model.get("fecha");
		List<Ficha> correcciones=hibernateTemplate.executeFind(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				return session.createQuery("from CorreccionDeFicha c where date(c.fecha)=? and c.ficha.origen=\'MOS\'")
						.setParameter(0, fecha,Hibernate.DATE)
						.list();
			}
		});
		//System.out.println("Correcciones Fichas: "+correcciones.size());
		model.put("correcciones", correcciones);
		return this;
	}
	
	
	public static void main(String[] args) {
		InicializadorParaVentas i=new InicializadorParaVentas(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("fecha", DateUtil.toDate("21/04/2017"));
		//i.inicializarVentas(model);
		//i.inicializarCortesEfectivo(model);
			
	}

}
