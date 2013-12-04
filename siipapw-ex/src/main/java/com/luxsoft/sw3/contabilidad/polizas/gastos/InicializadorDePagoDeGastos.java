package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;


public class InicializadorDePagoDeGastos {
	
	final HibernateTemplate hibernateTemplate;
	final JdbcTemplate jdbcTemplate;
	
	public InicializadorDePagoDeGastos(HibernateTemplate hibernateTemplate,JdbcTemplate jdbcTemplate){
		this.hibernateTemplate=hibernateTemplate;
		this.jdbcTemplate=jdbcTemplate;
	}
	
	public void inicializar(ModelMap model){
		pagos(model);
	}
	
	private void pagos(ModelMap model){
		Periodo periodo=(Periodo)model.get("periodo");
		String sql="SELECT B.CARGOABONO_ID" +
				"  FROM sw_bcargoabono B LEFT JOIN SW_TREQUISICION T ON (B.CARGOABONO_ID=T.CARGOABONO_ID) WHERE " +
				"  B.FECHA between ? and ? " +
				"  AND B.ORIGEN=\'GASTOS\'";
		Object[] args=new Object[]{
			new SqlParameterValue(Types.DATE, periodo.getFechaInicial())	
			,new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		final List<CargoAbono> pagos=new ArrayList<CargoAbono>();
		final List<Long> pagosIds=getJdbcTemplate().queryForList(sql, args,Long.class);
		/*
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				for(Long id:pagosIds){
					CargoAbono ca=(CargoAbono)session.load(CargoAbono.class, id);
					if(ca instanceof HibernateProxy)
						ca= (CargoAbono)((HibernateProxy)ca).getHibernateLazyInitializer().getImplementation();
					pagos.add(ca);					
					for(RequisicionDe det:ca.getRequisicion().getPartidas()){
						GCompra compra=det.getFacturaDeGasto().getCompra();
						Hibernate.initialize(compra.getPartidas());
					}					
				}
				
				return null;
			}
		});*/
		//System.out.println("CargoAbonos: "+pagos.size()+ " Ids: "+pagosIds.size());
		//EventList<CargoAbono> allPagos=GlazedLists.eventList(pagos);
		model.addAttribute("pagosIds", pagosIds);
		
	}
	
		
	public HibernateTemplate getHibernateTemplate() {
		return hibernateTemplate;
	}


	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}


	public static void main(String[] args) {
		DBUtils.whereWeAre();
		InicializadorDePagoDeGastos i=new InicializadorDePagoDeGastos(ServiceLocator2.getHibernateTemplate(),ServiceLocator2.getJdbcTemplate());
		ModelMap model=new ModelMap();
		model.addAttribute("periodo", Periodo.getPeriodoDelMesActual(DateUtil.toDate("04/01/2012")));
		i.inicializar(model);
			
	}

}
