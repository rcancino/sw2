package com.luxsoft.siipap.cxc.rules;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Cargo;
//import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
//import com.luxsoft.siipap.ventas.model.Venta;


/**
 * Reglas de negocios aplicables a las diversas fechas
 * relacionadas con la revision y cobro de cargos de credito
 * 
 * 
 * 
 * @author Ruben Cancino
 *
 */
public class RevisionDeCargosRules {
	
	private static RevisionDeCargosRules INSTANCE;
	
	private RevisionDeCargosRules(){
		
	}
	
	public static RevisionDeCargosRules instance(){
		if(INSTANCE==null){
			INSTANCE=new RevisionDeCargosRules();
		}
		return INSTANCE;
	}
	
	/**
	 * Utility method para valida si a todas y cada una de las cuentas son modificables en 
	 * las propiedades relacionadas con la revision 
	 * 
	 * @param cuentas
	 */
	public void validate(List<Cargo> cuentas){
		boolean res=CXCUtils.validarMismoCliente(cuentas);
		Assert.isTrue(res,"La seleccion no corresponde al mismo cliente ");
		
		res=CXCUtils.validarMismoFechaRevision(cuentas);
		Assert.isTrue(res,"La seleccion no corresponde al la misma fecha de revisión");		
		validarRecibidasCxC(cuentas);
	}
	
	
	
	/**
	 * Valida que todas las cuentas por cobrar ya esten recibidas en CxC
	 * 
	 */
	public void validarRecibidasCxC(List<Cargo> cuentas){
		for(Cargo cargo:cuentas){
			Assert.isTrue(cargo.isRecibidaCXC(),MessageFormat.format("El documento {0} no se ha recibido en CxC",cargo.getDocumento()));
		}
	}
	
	/**
	 * Calcula la fecha de revision de la cuenta por cobrar 
	 * 	A PARTIR DE UNA FECHA DE CALCULO ARBITRARIA
	 * 
	 *	@deprecated YA NO USAR
	 * @param cuenta
	 * @param fechaDelCalculo La fecha a partir de la cual queremos obtener la fecha de revision
	 * @return
	 */
	public Date calcularFechaDeRevision(final Cargo cuenta,final Date fechaDelCalculo){
		if(cuenta.isRevision()){
			final int diaRevision=cuenta.getDiaRevision();
			Date res=DateUtil.calcularFechaMasProxima(fechaDelCalculo, diaRevision, false);
			return res;
		}else{
			// La fecha del documento
			return cuenta.getFecha();
		}
	}
	
	/**
	 * Calcula la fecha original de revision.
	 * Actual de manera uniforme para cualquier cuenta por cobrar, regresando
	 * la fecha adecuada
	 * 
	 * @param cuenta
	 * @return
	 */
	public Date calcularFechaOriginalDeRevision(final Cargo cuenta){
		return calcularFechaDeRevision(cuenta, cuenta.getFecha());
	}
	
	/**
	 * Calcula la proxima fecha de revision para la cuenta por cobrar
	 * Se considera la fecha del sistema como la fecha a partir de la cual se 
	 * requiere la proxima revision 
	 * 
	 * @param cuenta
	 * @return
	 */
	public Date calcularProximaFechaDeRevision(final Cargo cuenta){
		return calcularFechaDeRevision(cuenta, new Date());
	}
		
	
	/**
	 * Actualiza de forma adecuada las fechas relacionadas con revision y cobro para el cargo indicado
	 * 
	 * 
	 * @param cuenta
	 */
	public void actualizar(final Cargo cuenta,final Date fechaDelCalculo){
		
		cuenta.setFechaRevision(cuenta.getFecha());
		int plazo=cuenta.getPlazo();
		int tolerancia=0;
		if(cuenta.getCliente().getCredito()!=null){
			if(!cuenta.getCliente().getCredito().isVencimientoFactura())
				//tolerancia=7; //Modificacion por ordenes de Direccion General (Lic. Jose Sanchez) 14/08/2013
				tolerancia=0;
		}
		//final Date vto=DateUtils.addDays(cuenta.getFecha(), plazo+tolerancia); //Modificacion por ordenes de Direccion General (Lic. Jose Sanchez) 14/08/2013
		//final Date vto=DateUtils.addDays(cuenta.getFechaRevisionCxc(), plazo+tolerancia);
		
		
		
		if(cuenta.getFechaRevisionCxc()==null){
			final Date vto=DateUtils.addDays(cuenta.getFechaRevision(), plazo+tolerancia);
			cuenta.setVencimiento(vto);
			if(cuenta.getCliente().getCredito().isVencimientoFactura()){
				cuenta.setRevisada(true);
			}
		}else{
			//Si el cliente es de credito
			if(cuenta.getCliente().getCredito().isVencimientoFactura()){
				final Date vto=DateUtils.addDays(cuenta.getFecha(), plazo);
				cuenta.setVencimiento(vto);
				
			}else{
				final Date vto=DateUtils.addDays(cuenta.getFechaRevisionCxc(), plazo+tolerancia);
				cuenta.setVencimiento(vto);
			}	
			//Para contado
			//cuenta.setVencimiento(cuenta.getFecha());
		}
		
		
		int diaDePago=7;
		int diaRevision=7;
		if(cuenta.getCliente().getCredito()!=null){
			diaDePago=cuenta.getCliente().getCredito().getDiacobro();
			diaRevision=cuenta.getCliente().getCredito().getDiarevision();
		}
		cuenta.setDiaPago(DateUtil.calcularFechaMasProxima(cuenta.getVencimiento(), diaDePago, true));
		
		
		Date fechaPivot=fechaDelCalculo.getTime()>cuenta.getVencimiento().getTime()?fechaDelCalculo:cuenta.getVencimiento();
		
		//Si la cuenta ya esta revisada solo se programa el pago 
		if(cuenta.isRevisada()){			
			if(cuenta.getSaldoCalculado().doubleValue()>1){
				int diaCobro=cuenta.getDiaDelPago()!=0?cuenta.getDiaDelPago():7;					 
				final Date proximoPago=DateUtil.calcularFechaMasProxima(fechaPivot, diaCobro, true);
				cuenta.setReprogramarPago(proximoPago);
			}				
		}else{
			//Reporgramar la proxima fecha de revision asi como el pago
			diaRevision=cuenta.getDiaRevision()!=0?cuenta.getDiaRevision():7;
			final Date proximaRevision=DateUtil.calcularFechaMasProxima(fechaDelCalculo, diaRevision, true);
			cuenta.setFechaRevisionCxc(proximaRevision);
			
			int diaCobro=cuenta.getDiaDelPago()!=0?cuenta.getDiaDelPago():7;
			final Date proximoPago=DateUtil.calcularFechaMasProxima(fechaPivot, diaCobro, true);
			cuenta.setReprogramarPago(proximoPago);
		}	
	}
	
	public void actualizar(List<Cargo> cuentas){
		for(Cargo c:cuentas){
			actualizar(c,new Date());
		}
	}
	
	public void actualizar(Cargo...cargos ){
		for(Cargo c: cargos){
			actualizar(c,new Date());
		}
	}
	
	/**
	 * Actualzia con la fecha del sistema
	 * @param cuentas
	 */
	public void actualizacionAutomatica(List<Cargo> cuentas){
		for(Cargo c:cuentas){
			actualizar(c,new Date());
		}
	}
	
	
	/**
	 * Marca la revision de una cuenta por el cliente o el departamento de CxC 
	 * 
	 * @param cuentas
	 * @param val
	 */
	public void marcarRevision(List<Cargo> cuentas,boolean val){
		validarRecibidasCxC(cuentas);
		for (Cargo cargo : cuentas) {
			cargo.setRevisada(val);
		}
	}
	
	public static void main(String[] args) {
		//Venta v=ServiceLocator2.getVentasManager().get("8a8a81c7-273f7633-0127-3f7b3fbd-0006");
		//RevisionDeCargosRules.instance().actualizar(v, new Date());
		//ServiceLocator2.getVentasManager().salvar(v);
		boolean incluyente=false;
		Date date=DateUtil.toDate("26/08/2013");		
		Date revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
		
		date=DateUtil.toDate("27/08/2013");
		revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
		
		date=DateUtil.toDate("28/08/2013");
		revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
		
		System.out.println("_____________________________");
		
		incluyente=true;
		date=DateUtil.toDate("26/08/2013");		
		revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
		
		date=DateUtil.toDate("27/08/2013");
		revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
		
		date=DateUtil.toDate("28/08/2013");
		revision=DateUtil.calcularFechaMasProxima(date, 2, incluyente);
		System.out.println(MessageFormat.format("Prox Remvision a partir del {0} es {1}",date,revision));
	}

}
