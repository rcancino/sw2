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
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_PagoEnParcialidades implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		String asiento="PAGO EN PARCIALIDADES";
		CargoAbono pago=(CargoAbono)model.get("pago");
		Requisicion requisicion=pago.getRequisicion();			
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;			
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		Date fechaCorte=DateUtils.addDays(poliza.getFecha(), -1);
		BigDecimal saldo=factura==null?BigDecimal.ZERO:factura.getSaldoCalculadoAlCorte(fechaCorte).amount();
		if(factura==null)
			return;
		
		//Descriminar Rembolsos
		if(factura.getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO) || factura.getCompra().getTipo().equals(TipoDeCompra.ESPECIAL))
					return;
				
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		//System.out.println("Procesando factura: "+factura);
		final BigDecimal totalFactura=factura.getTotalMN().amount();
		
		if(concepto==737332L ){
			if(saldo.doubleValue()==totalFactura.doubleValue()){
				registrarPrimeraParcialidad(poliza,pago,factura,asiento);		
			}else{
				registrarPagoParcial(poliza,pago,factura,asiento);
			}
		}		
	}
	
	
	private void registrarPrimeraParcialidad(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura,String asiento) {
	
		poliza.setDescripcion(poliza.getDescripcion()+" (Primera parcialidad) ");
		
		List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			if(det.getFacturaDeGasto()!=null)
				facturas.add(det.getFacturaDeGasto());
		}

		// Provision
		String pattern="PROVISION F:{0}  {1,date,short}";			
		
		if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO)){
			// Seguro
			pattern="SEGURO F:{0}  {1,date,short}";				
		}	
		
		String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		String ref1=factura.getCompra().getProveedor().getNombreRazon();
		String ref2=factura.getCompra().getSucursal().getNombre();
		BigDecimal ietu=MonedasUtils.calcularImporteDelTotal(pago.getImporte().abs());
		BigDecimal iva=MonedasUtils.calcularImpuestoDelTotal(factura.getTotalMN().amount());	
		
		if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO)){
			// Seguro
			pattern="SEGURO F:{0}  {1,date,short}";				
		}		
		
		if(!factura.getCompra().getProveedor().getNombreRazon().startsWith("PAPER IMPORT")){
			
			for(GFacturaPorCompra fac:facturas){
				String desc3=MessageFormat.format(pattern, fac.getDocumento(),fac.getFecha());
				BigDecimal ivaFac=MonedasUtils.calcularImpuestoDelTotal(fac.getTotalMN().amount());
				if(DateUtil.isSameMonth(pago.getFecha(), factura.getFecha())){
					generarCargoAGastosAcumulado(fac, poliza,pago,asiento);
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", true, iva, desc3, pago.getAFavor(), "OFICINAS", asiento);
					if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO))
						PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR04", false,fac.getTotalMN().amount(), desc2, ref1, ref2, asiento);
					else
						PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", false,fac.getTotalMN().amount(), desc2, ref1, ref2, asiento);	
				}				
			}						
		//	registrarIvaPorAcreditarGastos(poliza, pago, iva, false,asiento);
		}
		
		registrarAbonoABancos(poliza, pago,asiento);
		
		//Cancelar la provision (C)
		if(factura.getCompra().getProveedor().getNombreRazon().startsWith("PAPER IMPORT")){
			PolizaDetFactory.generarPolizaDet(poliza, "200", "P095", true,pago.getImporte().abs(), desc2, ref1, ref2, asiento);
		}			
		else{
			if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO))
				PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR04", true	,pago.getImporte().abs(), desc2, ref1, ref2, asiento);
			else
				PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true	,pago.getImporte().abs(), desc2, ref1, ref2, asiento);			
		}
		
		
		BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(pago.getImporte().abs());
		
//		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, false,asiento);		
		registrarIvaEnGastos(poliza, pago, ivaPago, false,asiento);
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, true,asiento);
	
	//	No corresponde el IETU total
	//	Proc_PagoNormal.registrarIetuAcumuladoPorConcepto(poliza, pago, asiento);
		
		String descripcionDeConcepto=factura.getCompra().getPartidas().iterator().next().getConceptoContable();
		PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "900", descripcionDeConcepto, true,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "901", descripcionDeConcepto, false,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);	
		
		//registrarIETUEnGastos(poliza, pago, MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount()),asiento);

			
	}
	
	private void registrarPagoParcial(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura,String asiento) {
		
		poliza.setDescripcion(poliza.getDescripcion()+" (Pago parcial) ");
		registrarAbonoABancos(poliza, pago,asiento);
		
		//Cancelar la provision (C)
		String pattern="PROVISION F:{0}  {1,date,short}";				
		String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		String ref1=factura.getCompra().getProveedor().getNombreRazon();
		String ref2=factura.getCompra().getSucursal().getNombre();
		PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true, pago.getImporteMN().amount().abs(), desc2, ref1, ref2, asiento+"xxxxxx");
				
		BigDecimal ivaPago=MonedasUtils.calcularImpuestoDelTotal(pago.getImporte().abs());		
		registrarIvaEnGastos(poliza, pago, ivaPago, false,asiento);
		
		//registrarIvaEnGastos(poliza, pago, ivaPago, false,asiento);
		registrarIvaPorAcreditarGastos(poliza, pago, ivaPago, true,asiento);
		//Proc_PagoNormal.registrarIetuAcumuladoPorConcepto(poliza, pago, asiento);
		
		BigDecimal ietu=pago.getImporteMNSinIva().amount().abs();
		String descripcionDeConcepto=factura.getCompra().getPartidas().iterator().next().getConceptoContable();
		PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "900", descripcionDeConcepto, true,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
		PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "901", descripcionDeConcepto, false,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
		//registrarIETUEnGastos(poliza, pago, MonedasUtils.calcularImporteDelTotal(pago.getImporteMN().abs().amount()),asiento);
	}
		
	private void generarCargoAGastosAcumulado(final GFacturaPorCompra factura,final Poliza poliza,final CargoAbono pago,String asiento){
		
		final EventList<GCompraDet> eventList=GlazedLists.eventList(factura.getCompra().getPartidas());
		final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
		final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
		Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
		
		CantidadMonetaria ivaAcumulado=CantidadMonetaria.pesos(0);
		CantidadMonetaria importeAcumulado=CantidadMonetaria.pesos(0);
		
		String proveedor=null;
		String sucursal=null;
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> dets=groupList.get(index);
			
			GCompraDet gasto=dets.get(0);
			PolizaDet cargoAGastos=poliza.agregarPartida();
			//cargoAGastos.setCuenta(getCuenta("600"));
			
			ConceptoDeGasto concepto=gasto.getRubro();
			
			if(concepto!=null){	
							
				concepto=concepto.getRubroSegundoNivel(concepto);
				
				ConceptoContable conceptoContable=buscarConceptoContable(concepto.getId().toString());
				if(conceptoContable!=null){
					cargoAGastos.setConcepto(conceptoContable);
					cargoAGastos.setCuenta(conceptoContable.getCuenta());
				}
				//cargoAGastos.setConcepto(cargoAGastos.getCuenta().getConcepto(concepto.getId().toString()));
				if(cargoAGastos.getConcepto()==null){
					cargoAGastos.setDescripcion("SIN CONCEPTO: "+concepto.getId().toString());					
				}				
			}
			
			String pattern="FAC: {0}  ({1,date,short}), {2}";
			String descripcion2=MessageFormat.format(pattern
					, factura.getDocumento()
					,factura.getFecha()
					,gasto.getRubro()!=null?gasto.getRubro().getDescripcion():"SIN CONCEPTO DE GASTO"
					);
			descripcion2=StringUtils.substring(descripcion2, 0,255);
			cargoAGastos.setDescripcion2(descripcion2);
			if(proveedor==null)
				proveedor=dets.get(0).getCompra().getProveedor().getNombreRazon();
			cargoAGastos.setReferencia(proveedor);
			if(sucursal==null)
				sucursal=new String(gasto.getSucursal().getNombre());
			cargoAGastos.setReferencia2(sucursal);
			
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			
			for(GCompraDet part:dets){
				importe=importe.add(part.getImporteMN());
				ivaAcumulado=ivaAcumulado.add(part.getImpuestoMN());
			}
			importeAcumulado=importeAcumulado.add(gasto.getCompra().getTotalMN());
			cargoAGastos.setDebe(importe.amount());
			cargoAGastos.setAsiento(asiento);				
		}
		
	}
	
	private void registrarIvaPorAcreditarGastos(Poliza poliza,CargoAbono pago,BigDecimal iva,boolean abono,String asiento){
		// Abono a IVa por acreditar en gasto
		PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", !abono, iva, "", pago.getAFavor(), "OFICINAS", asiento);		
	}
	
	private void registrarIvaEnGastos(Poliza poliza,CargoAbono pago,BigDecimal iva,boolean abono,String asiento){		
		PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", !abono, iva, "", pago.getAFavor(), "OFICINAS", asiento);
	}
	/*
	private void registrarIETUEnGastos(Poliza poliza,CargoAbono pago,BigDecimal importe,String asiento){		
		PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD04", true,importe
				, "IETU Deducible de Anticipos", pago.getAFavor(), "OFICINAS", asiento);
		
		PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU04", true,importe
				, "IETU Deducible de Anticipos", pago.getAFavor(), "OFICINAS", asiento);
	}
	*/
	private void registrarAbonoABancos(Poliza poliza,CargoAbono pago,String asiento){
		String numeroDeCuenta=pago.getCuenta().getNumero().toString();
		String desc2=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		desc2=StringUtils.substring(desc2, 0,50);
		PolizaDetFactory.generarPolizaDet(poliza, "102", numeroDeCuenta, false, pago.getImporteMN().abs().amount(), desc2, pago.getAFavor(), "OFICINAS", asiento);
		
	}
	
	private ConceptoContable buscarConceptoContable(String clave){
		String hql="from ConceptoContable c where c.clave=?";
		List<ConceptoContable> res=ServiceLocator2.getHibernateTemplate().find(hql,clave);
		return res.isEmpty()?null:res.get(0);
	}

	

}
