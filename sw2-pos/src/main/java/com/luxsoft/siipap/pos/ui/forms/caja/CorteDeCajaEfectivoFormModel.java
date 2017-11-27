package com.luxsoft.siipap.pos.ui.forms.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.services.Services;

/**
 * Modelo y controller para el corte de caja
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class CorteDeCajaEfectivoFormModel extends DefaultFormModel {
		

	public CorteDeCajaEfectivoFormModel() {
		super(Bean.proxy(Caja.class));
	}
	

	
	@Override
	protected void init() {
		Caja caja=getCaja();
		caja.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		Date time=new Date();
		caja.setFecha(time);
		caja.setCorte(time);		
		caja.setConcepto(Caja.Concepto.CORTE_CAJA);		
		caja.setOrigen(null);
		
		
		Handler handler=new Handler();
		//addBeanPropertyChangeListener(handler);
		getModel("fecha").addValueChangeListener(handler);

		getModel("origen").addValueChangeListener(handler);
		getModel("cierre").addValueChangeListener(handler);
		
		
		
	}

	@Override
	protected void addValidation(PropertyValidationSupport support) {
		validarCorteDeCaja(support);		
	}	
	
	private void validarCorteDeCaja(PropertyValidationSupport support){
		if(Caja.Concepto.CORTE_CAJA.equals(getCaja().getConcepto())){
			if(getCaja().getDisponibleCalculado().doubleValue()<=0){
				support.getResult().addError("No hay disponible por depositar");
				return;
			}
			if(getCaja().getImporte().doubleValue()<=0){
				support.getResult().addError("Importe del corte incorrecto");
			}
			
			if((getCaja().getDisponibleCalculado().subtract(getCaja().getImporte())).doubleValue() < 0.00){
				support.getResult().addError("Importe del corte incorrecto");
			}
			
			if(getCaja().isCierre() && (getCaja().getDisponibleCalculado().subtract(getCaja().getImporte())).doubleValue() != 0.00){
				support.getResult().addError("El importe debe ser igual al disponible en el Cierre");
			}
			
		}
	}

	public Caja getCaja(){
		return (Caja)getBaseBean();
	}
	
	public void actualizarPagos(){
		if(getCaja().getOrigen()==null)
			return;
		logger.info("Actualizando---"+ "Sucursal: "+getCaja().getSucursal());
		/*String hql="select sum(p.total) from PagoConEfectivo p " +
				"where p.primeraAplicacion=? " +
				"  and p.sucursal.id=?" +				
				"  and p.origenAplicacion=\'@ORIGEN\'" +
				"  and p.anticipo=false" 
				;
		*/
		String hql="select sum(p.total) from PagoConEfectivo p " +
				"where p.primeraAplicacion=? " +
				"  and p.sucursal.id=?" +				
				"  and p.anticipo=false" 
				;
		hql=hql.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		Object[] params={
				getCaja().getFecha()
				,getCaja().getSucursal().getId()
				};
		List<BigDecimal> res=Services.getInstance().getHibernateTemplate().find(hql, params);
		BigDecimal pagos=res.get(0)==null?BigDecimal.ZERO:res.get(0);
		logger.info("Pagos: "+pagos);
		/*String hql2="select sum(p.total) from PagoConEfectivo p " +
			"where p.fecha=? " +
			"  and p.sucursal.id=?" +				
			"  and p.origenAplicacion=\'@ORIGEN\'" +
			"  and p.anticipo=true" 
		;	*/	
		
		String hql2="select sum(p.total) from PagoConEfectivo p " +
				"where p.fecha=? " +
				"  and p.sucursal.id=?" +				
				"  and p.anticipo=true" 
			;	
		hql2=hql2.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		List<BigDecimal> res2=Services.getInstance().getHibernateTemplate().find(hql2, params);
		BigDecimal anticipos=res2.get(0)==null?BigDecimal.ZERO:res2.get(0);
		logger.info("Anticipos: "+anticipos);
		getCaja().setPagos(pagos.add(anticipos));
		
	}
	
	public void actualizarCortesAcumulados(){
		if(getCaja().getOrigen()==null)
			return;
	/*	String hql="select sum(c.deposito) from Caja c " +
				" where c.fecha=? " +
				" and c.sucursal.id=?" +
				" and c.concepto=\'CORTE_CAJA\'" +
				" and c.tipo=\'EFECTIVO\'" +
				" and c.origen=\'@ORIGEN\'";*/
		String hql="select sum(c.deposito) from Caja c " +
				" where c.fecha=? " +
				" and c.sucursal.id=?" +
				" and c.concepto=\'CORTE_CAJA\'" +
				" and c.tipo=\'EFECTIVO\'" ;
		hql=hql.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		Object[] params={
				getCaja().getFecha()
				,getCaja().getSucursal().getId()
				};
		List<BigDecimal> res=Services.getInstance().getHibernateTemplate().find(hql, params);
		BigDecimal acumulado=res.get(0)==null?BigDecimal.ZERO:res.get(0);
		logger.info("Pagos: "+acumulado);
		getCaja().setCortesAcumulados(acumulado);
	}

	public void actualizarCambiosDeCheque(){
		if(getCaja().getOrigen()==null)
			return;
		/*String hql="select sum(c.deposito) from Caja c " +
		" where c.fecha=? " +
		" and c.sucursal.id=?" +
		" and c.concepto=\'CAMBIO_CHEQUE\'" +
		" and c.tipo=\'EFECTIVO\'" +
		" and c.origen=\'@ORIGEN\'";*/
		
		String hql="select sum(c.deposito) from Caja c " +
				" where c.fecha=? " +
				" and c.sucursal.id=?" +
				" and c.concepto=\'CAMBIO_CHEQUE\'" +
				" and c.tipo=\'EFECTIVO\'" ;
		hql=hql.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		Object[] params={
				getCaja().getFecha()
				,getCaja().getSucursal().getId()
			};
		List<BigDecimal> res=Services.getInstance().getHibernateTemplate().find(hql, params);
		BigDecimal cambiosDeCheque=res.get(0)==null?BigDecimal.ZERO:res.get(0);
		logger.info("Cambios de cheque: "+cambiosDeCheque);
		getCaja().setCambiosDeCheque(cambiosDeCheque);
	}
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {			
			
			System.out.println("Fecha Deposito"+getCaja().getfechaDep());
			
			actualizarPagos();	
			actualizarCortesAcumulados();
			actualizarCambiosDeCheque();
			
			BigDecimal importeNew= BigDecimal.ZERO;
			
			if(!getCaja().isCierre()){
				
				double f=  (getCaja().getDisponibleCalculado().divide(new BigDecimal(1000))).doubleValue();
				int i=(int) f;
				int m=1000*i;
				importeNew=new BigDecimal(m);
				
				
				//importeNew=getCaja().getDisponibleCalculado();
			}else{
				importeNew=getCaja().getDisponibleCalculado();
			}
			
				
			
			getCaja().setDisponible(getCaja().getDisponibleCalculado());
			//getCaja().setDisponible(importeNew);
			getCaja().setImporte(importeNew);
			getCaja().aplicar();
			
		}		
	}
	
	
	/**
	 * Regresa una entridad de caja lista para ser persistida
	 * 
	 */
	public  Caja commit(){
		
		if(getCaja().isAnticipoCorte()){
			
			Calendar c1 = GregorianCalendar.getInstance();
			c1.setTime(getCaja().getFecha());			        

	        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	        System.out.println("Fecha Formateada: " + sdf.format(c1.getTime()));

	        
	         int diaSemana = c1.get(Calendar.DAY_OF_WEEK);
	         
	         if(diaSemana == 2){
	        	 c1.add(Calendar.DATE, -2);
			        System.out.println("Fecha Formateada: " + sdf.format(c1.getTime()) + "Dia de la semana"+ diaSemana);
	         }else{
	        	 c1.add(Calendar.DATE, -1);
			        System.out.println("Fecha Formateada: " + sdf.format(c1.getTime()) + "Dia de la semana"+ diaSemana); 	 
	         }
	        
	       
	        
	        
	        
	        getCaja().setFechaDep(c1.getTime());
			
			
			
			
		}else{
			getCaja().setFechaDep(getCaja().getFecha());
		}
		Caja proxy=getCaja();
		Caja target=new Caja();
		Bean.normalizar(proxy, target, new String[]{});
		target.setCorte(new Date());
		target.setImporte(proxy.getImporte());
		target.aplicar();
		return target;
	}
}
