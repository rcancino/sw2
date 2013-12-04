package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Morralla implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		List<CargoAbono> movimientos=(List<CargoAbono>)model.get("movimientos");
		for(CargoAbono m:movimientos){
			Concepto concepto=m.getConcepto();
			if(concepto==null )
				continue;
			String asiento="MORRALLA";
			
			//Retiro de morralla	
			if(concepto.getId().equals(737321L)){
				
				String desc2="RETIRO DE MORRALLA: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), false,m.getImporte().abs(), desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "101","VALC01", true ,m.getImporte().abs(), desc2, ref1, ref2, asiento);
			}
			//Deposito de morralla
			if(concepto.getId().equals(737294L) ){
				
				//String desc2="Deposito de morralla: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "101", "VALC01", false,m.getImporte().abs()
						, "ENTREGA DE MORRALLA"
						, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "101", "CAJA01", true,m.getImporte().abs()
						, "INGRESO DE MORRALLA"
						, ref1, ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "101", "CAJA01", false,m.getImporte().abs()
						, "SALIDA AL BANCO: "+DateUtil.getDate(m.getFecha())
						, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs()
						, "DEPOSITO: "+m.getId()
						, ref1, ref2, asiento);
			}
			String comentario="";
			
			if(m.getComentario()!=null)
				comentario=m.getComentario();
				
			//Depositos por Identificar
			if(concepto.getId().equals(737331L) && m.getConciliado().equals(false) && !comentario.startsWith("Pago con deposito ")){
				
				
				System.out.println("PAGO+++++++++++++++"+ m.getId() +"  -     "+ m.getConciliado() +"----- " +m.getConcepto());
				asiento="DEPOSITOS POR IDENTIFICAR";
				
				String desc2="Deposito: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs().multiply(m.getTc()), desc2, ref1, ref2, asiento);	
				PolizaDetFactory.generarPolizaDet(poliza, "203","DEPI01", false ,MonedasUtils.calcularImporteSinIva(m.getImporte().abs().multiply(m.getTc())), desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAD01", false ,MonedasUtils.calcularImpuestoDelTotal(m.getImporte().abs().multiply(m.getTc())), desc2, ref1, ref2, asiento);
				
				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU04", true ,MonedasUtils.calcularImporteSinIva(m.getImporte().abs().multiply(m.getTc())), "IETU ACUMULABLE", ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA04", false ,MonedasUtils.calcularImporteSinIva(m.getImporte().abs().multiply(m.getTc())), "IETU ACUMULABLE", ref1, ref2, asiento);				
				
			}
			
			//Depositos Acreedor Diverso
			if(concepto.getId().equals(737343L)){
				asiento="DEPOSITO ACREEDOR";
				
				String desc2="Deposito Acreedor: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento);	
				PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR01", false ,m.getImporte().abs(), desc2, ref1, ref2, asiento);
						
			}
			
			//Depositos Saldo Deudor
			if(concepto.getId().equals(737345L)){
				asiento="SALDO DEUDOR";
				
				String desc2="Deposito Saldo Deudor: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento);	
				PolizaDetFactory.generarPolizaDet(poliza, "109","198838", false ,m.getImporte().abs(), desc2, ref1, ref2, asiento);
						
			}
			
			//Cargo IDE
			if(concepto.getId().equals(737337L)){
				asiento="IDE RETENCION";
				String desc2="RETENCION IDE: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), false,m.getImporte().abs(), desc2, ref1, ref2, asiento);
	
				PolizaDetFactory.generarPolizaDet(poliza, "205","740326", true,m.getImporte().abs(), desc2, ref1, ref2, asiento);

			}
			
			//Cargo ISR Retenido por concepto de manejo de cuenta
			if(concepto.getId().equals(737335L)){
				asiento="ISR RETENIDO  ";
				String desc2="RETENCION ISR: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), false,m.getImporte().abs(), desc2, ref1, ref2, asiento);
	
				PolizaDetFactory.generarPolizaDet(poliza, "750","IMPE02", true,m.getImporte().abs(), desc2, ref1, ref2, asiento);

			}
			
			//Abono intereses por manejo de cuenta 
			if(concepto.getId().equals(737330L)){
				asiento="INTERESES ";
				String desc2="INTERESES: "+m.getId();
				String ref1=m.getCuenta().getBanco().getNombre();
				String ref2=m.getSucursal().getNombre();
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "701","740346", false,m.getImporte().abs(), desc2, ref1, ref2, asiento);
	
				

			}
					
		}
		
	}
	
}
