package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Concepto;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_PagosConRequisicionTesoreria implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		
		List<CargoAbono> movimientos=(List<CargoAbono>)model.get("movimientos");
		
		for(CargoAbono m:movimientos){
			if(m.getRequisicion()== null) return;
			if(m.getRequisicion().getConcepto()==null) return;
			
			if(m.getRequisicion()!=null){
				Concepto concepto=m.getRequisicion().getConcepto();
				switch (concepto.getId().intValue()) {
				case 737337:
					procesarIDE(poliza, m);
					break;
/*				case 224289:
					procesarDevolucionCte(poliza, m);
					break;
				case 737336:
					procesarPagosDeImportaciones(poliza,m);
					break;
				case 307216:
					procesarSeguros(poliza, m);
					break;
*/
				default:
					System.out.println("Movimiento sin procesar: "+m.getId()+ " Requisicon: "+m.getRequisicion().getId()+  " Concepto: "+m.getRequisicion().getConcepto());
					break;
				}
			}
					
		}
		
//		registrar(poliza, model);
		
	}
	


	private void procesarIDE(Poliza poliza,CargoAbono m){

		String asiento="IDE RETENCION";
		String desc2="RETENCION IDE: "+m.getId();
		String ref1=m.getCuenta().getBanco().getNombre();
		String ref2="OFICINAS";
		BigDecimal importe=m.getImporte().abs();
		BigDecimal tc=m.getTc().abs();
		importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
		
		
		PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento);

		PolizaDetFactory.generarPolizaDet(poliza, "205","IMPP08", true,importe, desc2, ref1, ref2, asiento);
	
	}
	
	
/*	public void registrar(Poliza poliza, ModelMap model) {
		List<Requisicion> reqIds=(List<Requisicion>)model.get("reqIds");
	 if(!reqIds.isEmpty()){	
		for(Requisicion m:reqIds){
			if(m.getId()== null) return;
			if(m.getConcepto()==null) return;
			
			if(m.getId()!=null){
				Concepto concepto=m.getConcepto();
				switch (concepto.getId().intValue()) {

				case 737337:
					procesarIDEReq(poliza, m);
					break;
			
				default:
					System.out.println("Movimiento sin procesar: "+m.getId()+ " Requisicon: "+m.getId()+  " Concepto: "+m.getConcepto());
					break;
				}
			}
					
		}
	 }
	}
	
	//Provision del IDE
	private void procesarIDEReq(Poliza poliza,Requisicion m){

		final Periodo per=Periodo.getPeriodoDelMesActual(m.getFecha());
		final Date fecha=per.getFechaFinal();

		 if (m.getFechaDePago()!=null && m.getFechaDePago().compareTo(fecha)>0)
		 {
			 String asiento="PROVISION IDE RETENCION";
			 String desc2="RETENCION IDE: "+m.getId();
			 String ref1=m.getAfavor();
			 String ref2="OFICINAS";
		
		//El Campo notificar de Requisicion, se debe capturar el numero de cuenta bancaria
			 PolizaDetFactory.generarPolizaDet(poliza, "118", m.getNotificar().toString(), true,m.getTotal().amount(), desc2, ref1, ref2, asiento);
			 PolizaDetFactory.generarPolizaDet(poliza, "205", "IMPP08", false,m.getTotal().amount(), desc2, ref1, ref2, asiento);
		 }
	}*/
	
/*	private void procesarDevolucionCte(Poliza poliza,CargoAbono m){
		String asiento="DEVOLUCION CLIENTE";
		String desc2="DEV:"+m.getId()+" CTE: "+m.getAFavor();
		String ref1=m.getCuenta().getBanco().getNombre();
		String ref2="OFICINAS"; 
	//	String ref2=m.getSucursal().getNombre();
	
	//	PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento);
		
		if(m.getRequisicion().getNotificar().equals("DEPXI")){
			PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento+" DEP X ID");
			PolizaDetFactory.generarPolizaDet(poliza, "203","DEPI01", false ,MonedasUtils.calcularImporteSinIva(m.getImporte().abs()), desc2, ref1, ref2, asiento+" DEP X ID");
			PolizaDetFactory.generarPolizaDet(poliza, "206","IVAD01", false ,MonedasUtils.calcularImpuestoDelTotal(m.getImporte().abs()), desc2, ref1, ref2, asiento+" DEP X ID");
		}else if(m.getRequisicion().getNotificar().equals("NOTA")){
			PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento+" NOTA");
			
						
		}else {
			PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,m.getImporte().abs(), desc2, ref1, ref2, asiento+" SAF");
			PolizaDetFactory.generarPolizaDet(poliza, "203",m.getRequisicion().getNotificar(), false ,MonedasUtils.calcularImporteSinIva(m.getImporte().abs()), desc2, ref1, ref2, asiento+" SAF");
			PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV02", false ,MonedasUtils.calcularImpuestoDelTotal(m.getImporte().abs()), desc2, ref1, ref2, asiento+" SAF");
		}		
	}	
	
	private void procesarSeguros(Poliza poliza,CargoAbono m){
		
		String asiento="SEGUROS";
		String desc2="PRIMA NETA: "+m.getId();
		String ref1=m.getCuenta().getBanco().getNombre();
		String ref2=m.getSucursal().getNombre();
		
		PolizaDetFactory.generarPolizaDet(poliza, "160","735151", true,m.getImporte().abs(), desc2, ref1, ref2, asiento);
		PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR01", false,m.getImporte().abs(), desc2, ref1, ref2, asiento);
	}
	
	private void procesarPagosDeImportaciones(Poliza poliza,CargoAbono m){
		String asiento="IMPORTACION";
		String afavor=m.getAFavor();
		String concepto="";
		if(StringUtils.containsIgnoreCase(afavor, "IMPAP")){
			concepto="I001";
		}else if(StringUtils.containsIgnoreCase(afavor, "PAPER")){
			concepto="P095";
		}
		for(RequisicionDe det:m.getRequisicion().getPartidas()){
			PolizaDetFactory.generarPolizaDet(poliza, "200",concepto, true
					,MonedasUtils.calcularImporteDelTotal(det.getTotal().amount())
					, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
					, afavor
					,"OFICINAS"
					, asiento
					);
			
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC01", true
					,MonedasUtils.calcularImpuestoDelTotal(det.getTotal().amount())
					, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
					, afavor
					,"OFICINAS"
					, asiento
					);
			
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", false
					,MonedasUtils.calcularImpuestoDelTotal(det.getTotal().amount())
					, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
					, afavor
					,"OFICINAS"
					, asiento
					);
			
			PolizaDetFactory.generarPolizaDet(poliza, "900","DIETU01", true
					,MonedasUtils.calcularImporteDelTotal(det.getTotal().amount())
					, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
					, afavor
					,"OFICINAS"
					, asiento
					);
			PolizaDetFactory.generarPolizaDet(poliza, "901","IETUD01", false
					,MonedasUtils.calcularImporteDelTotal(det.getTotal().amount())
					, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
					, afavor
					,"OFICINAS"
					, asiento
					);
			
		}
		PolizaDetFactory.generarPolizaDet(poliza, "102",m.getCuenta().getNumero().toString() , false,m.getImporte().abs(), m.getRequisicion().getFormaDePago().name()+ " "+m.getReferencia(),  afavor,"OFICINAS", asiento);
	}*/
	
	


}
