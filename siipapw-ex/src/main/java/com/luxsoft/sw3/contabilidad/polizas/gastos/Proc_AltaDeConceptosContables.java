package com.luxsoft.sw3.contabilidad.polizas.gastos;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_AltaDeConceptosContables implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");		
		Requisicion requisicion=pago.getRequisicion();
		if(requisicion==null)
			return;
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		if(factura!=null){
			GCompra compra=factura.getCompra();
			for(GCompraDet gasto:compra.getPartidas()){
				generarConcepto(gasto);
			}
		}	
	}
	
	private void generarConcepto(GCompraDet gasto){
		try {
			ConceptoDeGasto rubro=gasto.getRubro();
			String concepto="";
			String cuenta="";
			if(rubro!=null){	
				rubro=rubro.getRubroSegundoNivel(rubro);
				cuenta=StringUtils.substring(rubro.getCuentaContable(),0,3);
				concepto= rubro.getId().toString();
			}
			CuentaContable cuentaContable=PolizaDetFactory.getCuenta(cuenta);
			if(cuenta!=null){
				ConceptoContable conceptoContable=cuentaContable.getConcepto(concepto);
				if(conceptoContable==null &&(rubro!=null)){
					ConceptoContable cc=PolizaDetFactory.generarConceptoContable(concepto, rubro.getDescripcion(), cuenta);
					System.out.println("Concepto generado: "+cc+ " Cuenta: "+cc.getCuenta());
				}	
				
			}
		} catch (Exception e) {
			System.out.println("Imposible generar concepto para rubro: "+gasto.getRubro()+"  Error: "+ExceptionUtils.getRootCauseMessage(e));
		}
		
		
	}
	
		
	
	
	
	
	
	

}
