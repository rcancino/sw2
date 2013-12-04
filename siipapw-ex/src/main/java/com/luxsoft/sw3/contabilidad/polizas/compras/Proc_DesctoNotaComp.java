package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFactura;
import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

@Deprecated
public class Proc_DesctoNotaComp implements IProcesador{
	
	
    private Proc_DesctoNotaComp(){}
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		EventList<CXPFactura> facturas=(EventList<CXPFactura>) model.get("facturas");
		String asiento="COMPRAS DESCTO PENDTE";
		for(CXPFactura fac:facturas){
			if(fac.getProveedor().isDescuentoNota()){
				BigDecimal analizado=getAnalizado(fac, poliza.getFecha());
				//BigDecimal pendiente=fac.getImporteMN().amount().add(fac.getImpuesto().multiply(BigDecimal.valueOf(fac.getTc()))).add(fac.getFleteMN().amount()).add(fac.getImpuestoFleteMN().amount()).subtract(fac.getRetencionFleteMN().amount())
				BigDecimal descuento=fac.getTotalMN().amount().setScale(2,BigDecimal.ROUND_HALF_EVEN)
						//.subtract(analizado.add(fac.getImpuesto().multiply(BigDecimal.valueOf(fac.getTc()))).add(fac.getFleteMN().amount()).add(fac.getImpuestoFleteMN().amount()).subtract(fac.getRetencionFleteMN().amount())).setScale(6,BigDecimal.ROUND_HALF_EVEN);
						.subtract(analizado.add(fac.getImpuestoMN().amount()).add(fac.getFleteMN().amount()).add(fac.getImpuestoFleteMN().amount()).subtract(fac.getRetencionFleteMN().amount())).setScale(2,BigDecimal.ROUND_HALF_EVEN);
				
				//System.out.println("Fac: "+fac.getDocumento()+" F: "+fac.getFecha()+" Imp: "+fac.getImporteMN()+" a: "+analizado+" p: "+pendiente);
				String desc2=MessageFormat.format("DESCUENTO PENDIENTE Fac: {0}  {1,date, short}",fac.getDocumento(),fac.getFecha());
				PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(),true,descuento,desc2,fac.getNombre(),"TODAS", asiento);
			}
		}
		/*
		EventList<AnalisisDeFactura> analisis=(EventList<AnalisisDeFactura>) model.get("analisis");
		String asiento="COMPRAS DESCTO PENDTE";
		
		for(AnalisisDeFactura a:analisis){
		
			String desc2=MessageFormat.format("DESCUENTO PENDIENTE Fac: {0}  {1,date, short}",a.getFactura().getDocumento(),a.getFactura().getFecha());
			String ref1=a.getFactura().getNombre();
			
			if (a.isPrimerAnalisis()){
				
				if(a.getFactura().getProveedor().isDescuentoNota() && !a.getFactura().isAnticipo()){	
					
					BigDecimal importeBruto=BigDecimal.ZERO;
					BigDecimal importeNeto=BigDecimal.ZERO;
					
					for(AnalisisDeFacturaDet det:a.getPartidas()){
						if(det.getSumaDescuentos()!=0l){
							importeBruto=importeBruto.add(det.getImporteBrutoCalculadoMN().amount());
							importeNeto=importeNeto.add(det.getImporteMN());
						}
					}					
					BigDecimal descuento=importeBruto.subtract(importeNeto);
					PolizaDetFactory.generarPolizaDet(poliza, "200",a.getFactura().getClave(),true,descuento,desc2,ref1,"TODAS", asiento);						
					}
				}
			}
			*/
		}
		
	public BigDecimal getAnalizado(CXPFactura fac,Date corte) {
		
		BigDecimal analizado=BigDecimal.ZERO;
		for(AnalisisDeFactura analisis:fac.getAnalisis()){
			for(AnalisisDeFacturaDet det:analisis.getPartidas()){
				Date fEntrada=DateUtils.truncate(det.getEntrada().getFecha(), Calendar.DATE);
				corte=DateUtils.truncate(corte, Calendar.DATE);
				if(fEntrada.compareTo(corte)<=0){
					analizado=analizado.add(det.getImporteMN());
						
				}
			}
		}
		return analizado;
	}
		
	

}
