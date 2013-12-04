package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_PagoDeSeguros implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		String asiento="PAGO DE SEGUROS";
		CargoAbono pago=(CargoAbono)model.get("pago");
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
		if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO)){
			Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
			if(concepto==307216L){
				poliza.setDescripcion(poliza.getDescripcion()+" (Seguros)");
				registrarAbonoABancos(poliza, pago,asiento);
				
				String pattern="SEGURO F:{0}  {1,date,short}";				
				String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
				String ref1=factura.getCompra().getProveedor().getNombreRazon();
				String ref2=factura.getCompra().getSucursal().getNombre();
				PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR04", true, pago.getImporteMN().amount().abs(), desc2, ref1, ref2, asiento);
						
				BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(pago.getImporte().abs());		
				
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, ivaPago, "", pago.getAFavor(), "OFICINAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, ivaPago, "", pago.getAFavor(), "OFICINAS", asiento);
				
				
				
				
				BigDecimal ietu=pago.getImporteMNSinIva().amount().abs();
				String descripcionDeConcepto=factura.getCompra().getPartidas().iterator().next().getConceptoContable();
				PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "900", descripcionDeConcepto, true,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
				PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "901", descripcionDeConcepto, false,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);				
			}	
		}
	}
	
	private void registrarAbonoABancos(Poliza poliza,CargoAbono pago,String asiento){
		String numeroDeCuenta=pago.getCuenta().getNumero().toString();
		String desc2=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		desc2=StringUtils.substring(desc2, 0,50);
		PolizaDetFactory.generarPolizaDet(poliza, "102", numeroDeCuenta, false, pago.getImporteMN().abs().amount(), desc2, pago.getAFavor(), "OFICINAS", asiento);
		
	}
	
	
	

}
