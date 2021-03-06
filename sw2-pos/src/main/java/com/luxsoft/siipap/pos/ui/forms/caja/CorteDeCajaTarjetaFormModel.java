package com.luxsoft.siipap.pos.ui.forms.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.Date;
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
public class CorteDeCajaTarjetaFormModel extends DefaultFormModel {
		

	public CorteDeCajaTarjetaFormModel() {
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
		caja.setTipo(Caja.Tipo.TARJETA);
		caja.setOrigen(null);
		Handler handler=new Handler();
		
		getModel("fecha").addValueChangeListener(handler);
		getModel("origen").addValueChangeListener(handler);
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
		}
	}

	public Caja getCaja(){
		return (Caja)getBaseBean();
	}
	
	public void actualizarPagos(){
		if(getCaja().getOrigen()==null)
			return;
		logger.info("Actualizando---"+ "Sucursal: "+getCaja().getSucursal());
		String hql="select sum(p.total) from PagoConTarjeta p " +
				"where p.primeraAplicacion=? " +
				"  and p.sucursal.id=?" +				
				"  and p.origenAplicacion=\'@ORIGEN\'" +
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
		String hql2="select sum(p.total) from PagoConTarjeta p " +
			"where p.fecha=? " +
			"  and p.sucursal.id=?" +				
			"  and p.origen=\'@ORIGEN\'" +
			"  and p.anticipo=true" 
		;		
		hql2=hql2.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		List<BigDecimal> res2=Services.getInstance().getHibernateTemplate().find(hql2, params);
		BigDecimal anticipos=res2.get(0)==null?BigDecimal.ZERO:res2.get(0);
		logger.info("Anticipos: "+anticipos);
		pagos=pagos.add(anticipos);
		
		
		String hql3="select sum(p.total) from PagoPorCambioDeTarjeta p " +
				"where p.primeraAplicacion=? " +
				"  and p.sucursal.id=?" +				
				"  and p.origen=\'@ORIGEN\'" 
				;
		hql3=hql3.replaceAll("@ORIGEN", getCaja().getOrigen().name());
		Object[] params3={
				getCaja().getFecha()
				,getCaja().getSucursal().getId()
				};
		List<BigDecimal> res3=Services.getInstance().getHibernateTemplate().find(hql3, params3);
		BigDecimal cambiosDeTarjeta=res3.get(0)==null?BigDecimal.ZERO:res3.get(0);
		logger.info("Cambios de Tarjeta: "+cambiosDeTarjeta);
		pagos=pagos.add(cambiosDeTarjeta);
		logger.info("Total pagos: "+pagos);
		getCaja().setPagos(pagos);
		
		
	}
	
	public void actualizarCortesAcumulados(){
		if(getCaja().getOrigen()==null)
			return;
		String hql="select sum(c.deposito) from Caja c " +
				" where c.fecha=? " +
				" and c.sucursal.id=?" +
				" and c.concepto=\'CORTE_CAJA\'" +
				" and c.tipo=\'TARJETA\'" +
				" and c.origen=\'@ORIGEN\'";
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

	
	
	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {			
			actualizarPagos();	
			actualizarCortesAcumulados();
			
			getCaja().setDisponible(getCaja().getDisponibleCalculado());
			getCaja().setImporte(getCaja().getDisponible());
			getCaja().aplicar();
		}		
	}
	
	/**
	 * Regresa una entridad de caja lista para ser persistida
	 * 
	 */
	public  Caja commit(){
		Caja proxy=getCaja();
		Caja target=new Caja();
		Bean.normalizar(proxy, target, new String[]{});
		target.setCorte(new Date());
		target.setImporte(proxy.getImporte());
		target.aplicar();
		return target;
	}
}
