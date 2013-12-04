package com.luxsoft.siipap.cxc.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Reglas de negocios asociadas con la aplicacion de abonos
 * a cuentas por cobrar (Cargos) 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class AplicacionDeAbonoRules {
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	
	
	
	/**
	 * Distribuye el disponible del abono entre las aplicaciones 
	 * en forma descendente
	 * 
	 * @param abono
	 * @param aplicaciones
	 */
	protected void distribuirElImporte(final Abono abono,final List<Aplicacion> aplicaciones,BigDecimal disponible){
		Assert.notNull(abono,"No se puede aplicar el importe si el abono es nulo");
		Assert.notEmpty(aplicaciones,"La coleccion de aplicaciones no puede ser nula o estar vacia");
		logger.info("Distribuyendo importe de abono entre aplicaciones");
		//BigDecimal disponible=abono.getDisponible();
		for(Aplicacion a:aplicaciones){
			final Cargo cargo=a.getCargo();	
			
			BigDecimal total=cargo.getTotal();
			BigDecimal dev=cargo.getDevoluciones();
			BigDecimal desc=cargo.getDescuentos();
			BigDecimal bon=cargo.getBonificaciones();			
			BigDecimal descSugerido=calcularImporteDeDescuento(cargo);
			BigDecimal pagos=ServiceLocator2.getCXCManager().sumarPagos(cargo);
			
			BigDecimal porAplicar=total.subtract(dev)
				.subtract(desc)
				.subtract(bon)
				.subtract(descSugerido)
				.subtract(pagos);
			
			//Si el disponible es suficiente
			if(disponible.doubleValue()>=porAplicar.doubleValue()){
				a.setImporte(porAplicar);
				disponible=disponible.subtract(porAplicar);
				continue;
			}else{
				// Si el disponible no es suficiente aplicamos lo que se puede y terminamos
				a.setImporte(disponible);
				disponible=BigDecimal.ZERO;
				break;
			}
			
		}
	}
	
	/**
	 * Copiado de {@link NotaDescuentoRules} 
	 * 
	 * @return El importe del descuento correspondiente al cargo, cero si el cargo no califica para un descuento
	 */
	private BigDecimal calcularImporteDeDescuento(final Cargo cargo){
		if(!cargo.isPrecioBruto()){
			return BigDecimal.ZERO; //Las ventas a precio neto no requieren nota de descuento
		}else if(cargo.getDescuentos().doubleValue()>0)
			return BigDecimal.ZERO; //Ya existe un descuento aplicado
		BigDecimal val=cargo.getTotal();
		
		CantidadMonetaria vval=CantidadMonetaria.pesos(val.doubleValue());
		vval=vval.subtract(CantidadMonetaria.pesos(cargo.getDevoluciones().doubleValue()));
		vval=vval.subtract(CantidadMonetaria.pesos(cargo.getBonificaciones().doubleValue()));
		CantidadMonetaria impDDesc=vval.multiply(cargo.getDescuentoGeneral());
		return impDDesc.amount().abs();
	}
	
	/**
	 * Verifica que la aplicacion indicada pueda ser asignada al abono
	 * Punto de extensibilidad que actualmente no esta en uso
	 * 
	 * @param a
	 * @param aplicacion
	 * @return
	 */
	public Aplicacion generarAplicacion(final Abono a,final Aplicacion aplicacion){
		for(ValidarAplicacion v:validaciones){
			v.validar(aplicacion);
		}
		return aplicacion;
	}
	
	private List<ValidarAplicacion> validaciones=new ArrayList<ValidarAplicacion>();
	
	public void validar(final Aplicacion aplicacion){
		for(ValidarAplicacion v:validaciones){
			v.validar(aplicacion);
		}
	}
	
	public List<ValidarAplicacion> getValidaciones() {
		return validaciones;
	}


	public void setValidaciones(List<ValidarAplicacion> validaciones) {
		this.validaciones = validaciones;
	}

	
	/**
	 * Permite hacer un decouple para delegar la validacion de una aplicacion a otro
	 * objeto. La forma de validar una aplicacion es mandando un error solo y solo
	 * si la aplicacion no puede ser.
	 * 
	 * @author Ruben Cancino
	 *
	 */
	public static interface ValidarAplicacion {
		
		/**
		 * Valida si la aplicacion es correcta
		 * Las implementaciones estan obligadas a mandar error para
		 * informar que la aplicacion de abono no es valida
		 * 
		 * @param a
		 * @throws AplicacionException Si la aplicacion no es valida
		 * 
		 */
		public void validar(Aplicacion a) throws AplicacionException;
	}


	

}
