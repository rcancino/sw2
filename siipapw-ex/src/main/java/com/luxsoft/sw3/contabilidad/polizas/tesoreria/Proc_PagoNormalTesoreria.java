package com.luxsoft.sw3.contabilidad.polizas.tesoreria;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_PagoNormalTesoreria implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="DEVOLUCION CLIENTE";
		Requisicion requisicion=pago.getRequisicion();			
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;			

		Date fechade= null;
		for (RequisicionDe r: requisicion.getPartidas() ){
			fechade= r.getFechaDocumento(); 
		}
		 procesarPagosDeImportaciones(poliza,pago);	

		
		procesarDevolucionCte(poliza, pago);
		procesarCompraDolares(poliza,pago);
	}
	
	private void procesarDevolucionCte(Poliza poliza,CargoAbono pago){
		

		
		Long concepto=pago.getRequisicion().getConcepto()!=null || pago.getRequisicion().getPago()==null ?pago.getRequisicion().getConcepto().getId():0L;
		
		if(concepto==224289L){	
			
			System.out.println("procesando devolucion con pago "+pago.getId()+" para la requisicion"+ pago.getRequisicion().getId());
			
			String asiento="DEVOLUCION CLIENTE";
			String desc2="DEV:"+pago.getId()+" CTE: "+pago.getAFavor();
			String ref1=pago.getCuenta().getBanco().getNombre();
			String ref2="";
			Long sucursalId=0L;
			Requisicion r=pago.getRequisicion();
			BigDecimal importe=pago.getImporte().abs();
			BigDecimal tc=pago.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
			
			if(r!=null){
				if(!r.getPartidas().isEmpty()){
					RequisicionDe rd=r.getPartidas().iterator().next();
					if(rd.getSucursal()!=null){
						ref2=rd.getSucursal().getNombre();
						sucursalId=rd.getSucursal().getId();
					}else
						ref2="OFICINAS RequisicionDe sin suc";
				}else{
					ref2="OFICINAS S/Partida en Req";
				}
			}else{
				ref2="OFICINAS S/R";
			}
		
				
			if(StringUtils.isNotBlank(pago.getRequisicion().getClaveCliente()) &&  pago.getRequisicion().getNotificar().equals("SALDO_A_FAVOR") ){
				
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" SAF");
				PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR03" , true ,MonedasUtils.calcularImporteDelTotal(importe), desc2, ref1, ref2, asiento+" SAF");

				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV01", true ,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" SAF");

			
				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU06", false ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" SAF");
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA06", true ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" SAF");
				
				return;
			}
			if(pago.getRequisicion().getNotificar()==null) 		
				return;
				
			String tipo=pago.getRequisicion().getNotificar();
			tipo=StringUtils.trim(tipo);
			
		
			if(pago.getRequisicion().getNotificar().equals("DEPOSITO_POR_IDENTIFICAR")){	
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" DEP X ID");
				PolizaDetFactory.generarPolizaDet(poliza, "203","DEPI01", true,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" DEP X ID");
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAD01", true,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" DEP X ID");
				
				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU04", false ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" DEP X ID");
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA04", true ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" DEP X ID");
				
		
			}else if(StringUtils.isNotBlank(pago.getRequisicion().getComentario()) &&  pago.getRequisicion().getNotificar().equals("DEPOSITO_DEVUELTO") ){
		
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getRequisicion().getComentario(), true,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());	
						
							
			}else if(StringUtils.isNotBlank(pago.getRequisicion().getClaveCliente()) &&  pago.getRequisicion().getNotificar().equals("NOTA_CREDITO") ){
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "106",pago.getRequisicion().getClaveCliente(), true,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV03", false,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV02", true,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());

				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU03", false ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA03", true ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
						
				
			}else if(pago.getRequisicion().getNotificar().equals("NOTA_CAMIONETA")){	
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "105",sucursalId.toString(), true,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV01", true,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV02", false,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());

				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU06", false ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA06", true ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
						

				
			}else if(pago.getRequisicion().getNotificar().equals("NOTA_MOSTRADOR")){	
				PolizaDetFactory.generarPolizaDet(poliza, "102", pago.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR01", true ,importe, desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV03", false ,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "206","IVAV01", true ,MonedasUtils.calcularImpuestoDelTotal(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				
				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU06", false ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA06", true ,MonedasUtils.calcularImporteSinIva(importe), desc2, ref1, ref2, asiento+" "+pago.getRequisicion().getNotificar());

				
			}			
		}
		
	}
	
	private void procesarPagosDeImportaciones(Poliza poliza,CargoAbono m){
		Long conceptoID=m.getRequisicion().getConcepto()!=null?m.getRequisicion().getConcepto().getId():0L;	

		
		if(conceptoID==737336L){
			String asiento="IMPORTACION";
			String afavor=m.getAFavor();
			BigDecimal importe=m.getImporte().abs();
			BigDecimal tc=m.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
			
			String concepto="";
			if(StringUtils.containsIgnoreCase(afavor, "IMPAP")){
				concepto="I001";
			}else if(StringUtils.containsIgnoreCase(afavor, "PAPER")){
				concepto="P095";
			}
			for(RequisicionDe det:m.getRequisicion().getPartidas()){
				BigDecimal total=det.getTotal().amount();
				BigDecimal tcD=det.getTc().abs();
				total=new BigDecimal(total.doubleValue()*tcD.doubleValue());
				
				PolizaDetFactory.generarPolizaDet(poliza, "200",concepto, true
						,total
						, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
						, afavor
						,"OFICINAS"
						, asiento
						);
				
				PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC01", true
						,MonedasUtils.calcularImpuestoDelTotal(total)
						, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
						, afavor
						,"OFICINAS"
						, asiento
						);
				
				PolizaDetFactory.generarPolizaDet(poliza, "117","IVAC02", false
						,MonedasUtils.calcularImpuestoDelTotal(total)
						, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
						, afavor
						,"OFICINAS"
						, asiento
						);
				
				PolizaDetFactory.generarPolizaDet(poliza, "900","IETUD01", true
						,MonedasUtils.calcularImporteDelTotal(total)
						, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
						, afavor
						,"OFICINAS"
						, asiento
						);
				PolizaDetFactory.generarPolizaDet(poliza, "901","DIETU01", false
						,MonedasUtils.calcularImporteDelTotal(total)
						, MessageFormat.format("Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario())
						, afavor
						,"OFICINAS"
						, asiento
						);
				
			}
			PolizaDetFactory.generarPolizaDet(poliza, "102",m.getCuenta().getNumero().toString() , false,importe, m.getRequisicion().getFormaDePago().name()+ " "+m.getReferencia(),  afavor,"OFICINAS", asiento);
		}
		
		// Cargo Acreedor Diverso
		
		if(conceptoID==737344L){
			String asiento="CARGO ACREEDOR";
			BigDecimal importe=m.getImporte().abs();
			BigDecimal tc=m.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
			
			String desc2="Cargo Acreedor: "+m.getId();
			String ref1=m.getCuenta().getBanco().getNombre();
			String ref2="OFICINAS";
			
			PolizaDetFactory.generarPolizaDet(poliza, "203","DIVR01", true ,importe, desc2, ref1, ref2, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), false,importe, desc2, ref1, ref2, asiento);
			
		}
		
	}
	
	
	private void procesarCompraDolares(Poliza poliza,CargoAbono m){
		Long conceptoID=m.getRequisicion().getConcepto()!=null?m.getRequisicion().getConcepto().getId():0L;	

		
		if(conceptoID==492721L){
			String asiento="COMPRA DOLARES";
			String concepto= m.getCuenta().getNumero().toString();
			String afavor=m.getAFavor();
			BigDecimal importe=m.getImporte().abs();
			BigDecimal tc=m.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());

			for(RequisicionDe det:m.getRequisicion().getPartidas()){
				String desc2=MessageFormat.format("Compra Dolares Fac: {0}  F:{1} {2}", det.getDocumento(),det.getFechaDocumento(),det.getComentario());
				BigDecimal total=det.getTotal().amount();
				BigDecimal tcD=det.getTc().abs();
				total=new BigDecimal(total.doubleValue()*tcD.doubleValue());
				
				PolizaDetFactory.generarPolizaDet(poliza, "102",concepto,false, importe, desc2, afavor, "OFICINAS", asiento);					
			}			
		}
		
		// Cargo Acreedor Diverso
		
		if(conceptoID==737342L){
			String asiento="COMPRA DOLARES";
			BigDecimal importe=m.getImporte().abs();
			BigDecimal tc=m.getTc().abs();
			importe=new BigDecimal(importe.doubleValue()*tc.doubleValue());
			
			String desc2="Compra de Dolares: "+m.getId()+" "+m.getComentario();
			String ref1=m.getCuenta().getBanco().getNombre();
			String ref2="OFICINAS";
			
			PolizaDetFactory.generarPolizaDet(poliza, "102", m.getCuenta().getNumero().toString(), true,importe, desc2, ref1, ref2, asiento);
			
		}
		
	}
	
	

}
