package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.ControladorDinamico;

/**
 * Controlador para el mantenimiento de polizas de pago de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ControladorDePagoDeTesoreria extends ControladorDinamico{
	
	
	
	InicializadorDePagoDeTesoreria inicializador;
	
	public  ControladorDePagoDeTesoreria() {
		setClase("PAGOS");
		inicializador=new InicializadorDePagoDeTesoreria(getHibernateTemplate(), getJdbcTemplate());
		getProcesadores().add(new Proc_PagoNormalTesoreria());
		//getProcesadores().add(new Proc_PagosConRequisicionTesoreria());		
	}
	
	@Override
	public List<Poliza> generar(Periodo periodo) {
		System.out.println("Generand poliza de pago de gastos para el periodo: "+periodo);
		final List<Poliza> polizas=new ArrayList<Poliza>();
		
		model=new ModelMap();
		model.addAttribute("periodo",periodo);
		model.addAttribute("jdbcTemplate",getJdbcTemplate());
		model.addAttribute("hibernateTemplate",getHibernateTemplate());
		inicializador.inicializar(model);
		cargar(model);
		doProcesar(polizas);
		return polizas;
	}
	
	private void doProcesar(final List<Poliza> polizas){
		getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				List<Long> pagosIds=(List<Long>)model.get("pagosIds");
				//System.out.println("Pagos a procesar: "+pagosIds.size() );
				for(Long id:pagosIds){					
					CargoAbono pago=(CargoAbono)session.load(CargoAbono.class, id);
					/*Poliza existente=existe(getClase(),String.valueOf(pago.getId()));
					if(existente!=null){
						logger.info("Polzia existente: "+existente);
						polizas.add(existente);
						continue;
					}*/
					model.addAttribute("pago", pago);
					final Poliza poliza=new Poliza();
					poliza.setFecha(pago.getFecha());
					poliza.setReferencia(pago.getId().toString());
					poliza.setClase(getClase());					
					poliza.setTipo(Poliza.Tipo.EGRESO);
					Requisicion req=pago.getRequisicion();
					if(req==null && pago.getImporte().doubleValue()!=0){
						continue;
					}
					if( (req!=null) && req.getFormaDePago().equals(FormaDePago.TRANSFERENCIA)){
						poliza.setTipo(Poliza.Tipo.DIARIO);
					}
						
					String banco=pago.getCuenta().getBanco().getClave();
					String forma=req!=null?pago.getRequisicion().getFormaDePago().name():"";
					forma=StringUtils.substring(forma, 0,2);
					String pattern="TESORERIA: {0} {1} {2} {3}";
					String ms=MessageFormat.format(pattern,banco+": ",forma,pago.getReferencia()+", ",pago.getAFavor());
					ms=StringUtils.substring(ms, 0,255);
					
					
					
					poliza.setDescripcion(ms);
					if(pago.getImporte().doubleValue()!=0)
						procesar(poliza, model);
					else{
						poliza.setDescripcion((poliza.getDescripcion()+ "(CANCELADO)"));
					}
					poliza.actualizar();
					polizas.add(poliza);					
				}
				return null;
			}
		});
	}
	
	
	public void recargar(Poliza poliza,boolean total){
		
		poliza.getPartidas().clear();
		poliza.actualizar();		
		if(total){
			logger.info("Inicializando model");
			final List<Long> pagosIds=new ArrayList<Long>();
			pagosIds.add(Long.valueOf(poliza.getReferencia()));			
			model.addAttribute("pagosIds", pagosIds);
			final List<Poliza> polizas=new ArrayList<Poliza>();
			doProcesar(polizas);
			Poliza target=polizas.get(0);
			poliza.setDescripcion(target.getDescripcion());
			for(PolizaDet det:target.getPartidas()){
				poliza.agregarPartida(det);
			}
			poliza.actualizar();
		}else{
			System.out.println("PENDIENTE DE IMPLEMENTAR PARTE DINAMICA...");
			//procesar(poliza, model);
		}	
	}
	
	
	
	public static void main(String[] args) {
		//DBUtils.whereWeAre();
		ControladorDePagoDeTesoreria c=new ControladorDePagoDeTesoreria();
		c.generar(Periodo.getPeriodoDelMesActual(DateUtil.toDate("08/12/2011")));
		
	}

		
}


