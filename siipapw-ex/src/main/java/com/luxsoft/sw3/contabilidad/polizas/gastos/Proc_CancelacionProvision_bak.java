package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_CancelacionProvision_bak implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="CANCELACION DE PROVISION";
		Requisicion requisicion=pago.getRequisicion();	
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;			
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		if(factura==null)
			return;
		
		//Descriminar Rembolsos
		if(factura.getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO) || factura.getCompra().getTipo().equals(TipoDeCompra.ESPECIAL))
					return;
		final Date fechaFactura=factura.getFecha();	
		
		//Verificar q no se trate de anticipo o parcialidades ?
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		if(concepto==201136L || concepto==737332L)
			return;
		//System.out.println(MessageFormat.format("Procesando pago: {0} Fecha Pago:{1,date,short} Fecha Cobro: {2,date,short} ",pago.getId(),fechaPago,fechaCobro));
		
		if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
			
			poliza.setDescripcion(poliza.getDescripcion()+" (CANCELA PROV)");
			//BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(pago.getImporteMN().amount().abs());
			//BigDecimal importe=MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount());
			BigDecimal ietu=BigDecimal.ZERO;
			BigDecimal iva=BigDecimal.ZERO;
			for(RequisicionDe det:requisicion.getPartidas()){
				GFacturaPorCompra fac=det.getFacturaDeGasto();
				String pattern="PROVISION F:{0}  {1,date,short}";				
				String desc2=MessageFormat.format(pattern, fac.getDocumento(),fac.getFecha());
				String ref1=fac.getCompra().getProveedor().getNombreRazon();
				String ref2=fac.getCompra().getSucursal().getNombre();
				
				if(fac.getCompra().getProveedor().getId()==245346L ){  //PAPER IMPORTS
					PolizaDetFactory.generarPolizaDet(poliza, "200", "P095", true, fac.getTotalMN().amount(), desc2, ref1, ref2, asiento);
				}else if(fac.getCompra().getProveedor().getId()==753345L){ //IMPAP
					PolizaDetFactory.generarPolizaDet(poliza, "200", "I001", true, fac.getTotalMN().amount(), desc2, ref1, ref2, asiento);
				}else{
					PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true, fac.getTotalMN().amount(), desc2, ref1, ref2, asiento);
				}
				
				//canclearProvision(poliza,pago,det.getFacturaDeGasto(),asiento +"(CANCELA PROV)");
				ietu=ietu.add(fac.getCompra().getIetu().amount());
				iva=iva.add(fac.getCompra().getImpuesto());
				
			}
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, iva, "", pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, iva, "", pago.getAFavor(), "IVA", asiento);	
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,ietu, "IETU Deducible ", pago.getAFavor(), "OFICINAS", asiento);				
			PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false,ietu, "Deducible IETU", pago.getAFavor(), "OFICINAS", asiento);
			
			registrarAbonoABancos(poliza, pago,asiento);
			registrarRetenciones(pago, poliza,factura,asiento);
		}		
	}
	
	
	private void registrarAbonoABancos(Poliza poliza,CargoAbono pago,String asiento){
		String numeroDeCuenta=pago.getCuenta().getNumero().toString();
		String desc2=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		desc2=StringUtils.substring(desc2, 0,50);
		PolizaDetFactory.generarPolizaDet(poliza, "102", numeroDeCuenta, false, pago.getImporteMN().abs().amount(), desc2, pago.getAFavor(), "OFICINAS", asiento);
		
	}
	
	
	
private void registrarRetenciones(final CargoAbono pago,final Poliza p,GFacturaPorCompra factura,String asiento){
		
		BigDecimal retencion=factura.getCompra().getRetencionesMN().abs().amount();
		if(retencion.doubleValue()==0){
			return;
		}
		
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				String pattern="PROV F:{0} {1,date,short}";				
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion1();
				boolean cargo=false;
				String ref2="";
				if(monto.amount().doubleValue()!=0){
					cargo=true;
					if(pago.getSucursal()!=null)
						ref2=pago.getSucursal().getNombre();
					
				}
				PolizaDetFactory.generarPolizaDet(p, "117","IVAR01", cargo, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento);
				
			}
			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				String pattern="PROV F:{0} {1,date,short}";						
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion1();
				boolean cargo=true;
				String ref2="";
				if(monto.amount().doubleValue()!=0){
					cargo=false;
				}
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR02", cargo, monto.amount().abs()
						, desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre()
						, asiento);
				
			}			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				
				String pattern="PROV F:{0} {1,date,short}";						
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion2();
				boolean cargo=true;
				if(monto.amount().doubleValue()!=0){
					cargo=false;
				}
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR01", cargo, monto.amount().abs(), desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				
			}
		}
	}
	
		

}
