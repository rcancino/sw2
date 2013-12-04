package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.hssf.record.formula.functions.Round;
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
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_Anticipos implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="ANTICIPO";
		Requisicion requisicion=pago.getRequisicion();			
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;			
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		Date fechaCorte=DateUtils.addDays(poliza.getFecha(), -1);
		
		//Descriminar Rembolsos
		/*if(factura.getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO))
			return;
		
		
		*/
		final BigDecimal importePago=pago.getImporte().abs();
		BigDecimal saldo=factura==null?BigDecimal.ZERO:factura.getSaldoCalculadoAlCorte(fechaCorte).amount();
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		if(concepto!=201136L)
			return;
		
		if( (factura==null) || saldo.subtract(importePago).doubleValue()>0 ){
			registrarAnticipoParcial(poliza,pago,asiento);
		}else{
			registrarUltimoAnticipo(poliza,pago,factura,asiento);
			
		}
		
	}
	
	private void registrarAnticipoParcial(Poliza p,CargoAbono pago,String asiento) {
		
		p.setDescripcion(p.getDescripcion()+" (Anticipo parcial) ");
		//Bancos (A)
		registrarAbonoABancos(p, pago,asiento);
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){		
			
			GFacturaPorCompra factura=det.getFacturaDeGasto();
			CantidadMonetaria pendienteDePagar=det.getTotalMN();
			if(factura!=null){
				CantidadMonetaria saldo=factura.getSaldoCalculadoAlCorte(DateUtils.addDays(p.getFecha(), -1));
				pendienteDePagar=saldo.subtract(det.getTotalMN());
			}			
			if((factura==null) ||(pendienteDePagar.amount().abs().doubleValue()>0)){
				String tipoAnt="";
				if(det.getRequisicion().getComentario()!=null)
				tipoAnt=det.getRequisicion().getComentario();
				
				//Anticipo				
				String pattern="ANTICIPO F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				BigDecimal importe=MonedasUtils.calcularImporteDelTotal(det.getTotal().abs().amount());
				BigDecimal iva=MonedasUtils.calcularImpuesto(importe);
				iva=iva.setScale(2,RoundingMode.HALF_EVEN);
				if(tipoAnt.startsWith("INMUEBLE")){
					importe=det.getTotal().abs().amount();
					descripcion2=descripcion2+" "+tipoAnt;
				}
									
				
				
				PolizaDetFactory.generarPolizaDet(p, "111", "ANTP01", true,importe, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
				// IVA en Anticipo a Proveedor
				if(!tipoAnt.startsWith("INMUEBLE")){
					PolizaDetFactory.generarPolizaDet(p, "117", "IVAA01", true, iva, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
				}

				
				
				// IVA en Gastos
				//PolizaDetFactory.generarPolizaDet(p, "117", "IVAG01", true, iva, "", pago.getAFavor(), "OFICINAS", asiento);
				//IVA por Acreditar en Gastos				
				//PolizaDetFactory.generarPolizaDet(p, "117", "IVAG02", false, iva, "", pago.getAFavor(), "OFICINAS", asiento);
				
				PolizaDetFactory.generarPolizaDet(p, "900", "IETUD04", true,importe
						, "IETU de Anticipos", pago.getAFavor(), "OFICINAS", asiento);
				
				PolizaDetFactory.generarPolizaDet(p, "901", "DIETU04", false,importe
						, "IETU de Anticipos", pago.getAFavor(), "OFICINAS", asiento);
			}			
		}		
	}
	
	private void registrarUltimoAnticipo(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura,String asiento) {		
		
		poliza.setDescripcion(poliza.getDescripcion()+" (Anticipo final) ");
		
		List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();		
		
		String tipoAnt="";
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			if(det.getFacturaDeGasto()!=null)
				facturas.add(det.getFacturaDeGasto());
			
			if(det.getRequisicion().getComentario()!=null)
			tipoAnt=det.getRequisicion().getComentario();
		}
		for(GFacturaPorCompra fac:facturas){			
			//generarCargoAGastosAcumulado(fac, poliza,pago,asiento);
		}	
		generarCargoAGastosAcumulado(factura, poliza,pago,asiento);
		
		
		if(tipoAnt==null)
		tipoAnt="";
		
		tipoAnt=tipoAnt;
		
		//Anticipo
		String pattern="ANTICIPO F:{0} {1,date,short}";			
		String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		BigDecimal total=factura.getTotalMN().amount();
		BigDecimal ivaFac=MonedasUtils.calcularImpuestoDelTotal(total);
		BigDecimal importe=factura.getTotalMN().subtract(pago.getImporteMN().abs()).amount();	
		BigDecimal importeSinIVA=MonedasUtils.calcularImporteDelTotal(importe);
		BigDecimal impuesto=MonedasUtils.calcularImpuestoDelTotal(importe);
		if(tipoAnt.startsWith("INMUEBLE")){
			importeSinIVA=importe;
			descripcion2=descripcion2+" "+tipoAnt;
		}			
		
		PolizaDetFactory.generarPolizaDet(poliza, "111", "ANTP01", false, importeSinIVA, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
		
		if(!tipoAnt.startsWith("INMUEBLE")){

			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAA01", false, impuesto, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02",true, ivaFac, "", pago.getAFavor(), "OFICINAS", asiento);
			
			// IVA en Gastos
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, impuesto, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
			//IVA por Acreditar en Gastos				
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, impuesto, descripcion2, pago.getAFavor(), "OFICINAS", asiento);
			
		}
		
		registrarAbonoABancos(poliza, pago,asiento);
		
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			
			CantidadMonetaria aplicacion=CantidadMonetaria.pesos(0);
			if(factura!=null){
				CantidadMonetaria saldo=factura.getSaldoCalculadoAlCorte(DateUtils.addDays(poliza.getFecha(), -1));
				aplicacion=factura.getTotalMN().subtract(saldo);
			}
			
			CantidadMonetaria importeAplicado=MonedasUtils.calcularImporteDelTotal(aplicacion).abs();
			CantidadMonetaria importeFactura=MonedasUtils.calcularImporteDelTotal(factura.getTotalMN());
			CantidadMonetaria ivaDeFactura=MonedasUtils.calcularImpuesto(importeFactura);
			
			BigDecimal pagoAntFinal=pago.getImporteMN().abs().amount();
			BigDecimal importePagoAF=MonedasUtils.calcularImporteDelTotal(pagoAntFinal);
			BigDecimal ivaPagoAF=MonedasUtils.calcularImpuestoDelTotal(pagoAntFinal);
			if(tipoAnt.startsWith("INMUEBLE")){
				importeAplicado=aplicacion;
				importePagoAF=pagoAntFinal;
			}

			if(!tipoAnt.startsWith("INMUEBLE")){				
				// IVA en Gastos
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, ivaPagoAF, "", pago.getAFavor(), "OFICINAS", asiento);
				//IVA por Acreditar en Gastos				
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, ivaPagoAF, "", pago.getAFavor(), "OFICINAS", asiento);	
			}
			
			
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD04", false, importeAplicado.amount(), "CANCELACION DEL IETU ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU04", true, importeAplicado.amount(), "CANCELACION DEL IETU ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);
			
			//IETU en Gasto
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true, importeSinIVA, "IETU PARCIAL ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);
						
			PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false, importeSinIVA, "IETU PARCIAL ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);
			
			//IETU en Gasto
			PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,  importePagoAF, "IETU FINAL DE ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);
						
			PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false, importePagoAF, "IETU FINAL DE ANTICIPO", pago.getAFavor(), "OFICINAS", asiento);			
		}
	
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
	//		PolizaDet cargoAGastos=poliza.agregarPartida();
			//cargoAGastos.setCuenta(getCuenta("600"));
			

			ConceptoDeGasto concepto=gasto.getRubro();
			
			String cta="";
			String conceptoCta="";
			
			if(concepto!=null){	
							
				concepto=concepto.getRubroSegundoNivel(concepto);				
				ConceptoContable conceptoContable=buscarConceptoContable(concepto.getId().toString());
				
				if(conceptoContable!=null){
					cta=conceptoContable.getCuenta().getClave();
					conceptoCta=conceptoContable.getClave();
				}				
				
	//			if(conceptoContable!=null){
	//				cargoAGastos.setConcepto(conceptoContable);
	//				cargoAGastos.setCuenta(conceptoContable.getCuenta());
	//			}
				//cargoAGastos.setConcepto(cargoAGastos.getCuenta().getConcepto(concepto.getId().toString()));
	//			if(cargoAGastos.getConcepto()==null){
	//				cargoAGastos.setDescripcion("SIN CONCEPTO: "+concepto.getId().toString());					
	//			}				
			}			
			
			String pattern="FAC: {0}  ({1,date,short}), {2}";
			String descripcion2=MessageFormat.format(pattern
					, factura.getDocumento()
					,factura.getFecha()
					,gasto.getRubro()!=null?gasto.getRubro().getDescripcion():"SIN CONCEPTO DE GASTO"
					);
			descripcion2=StringUtils.substring(descripcion2, 0,255);
	//		cargoAGastos.setDescripcion2(descripcion2);
			if(proveedor==null)
				proveedor=dets.get(0).getCompra().getProveedor().getNombreRazon();
	//		cargoAGastos.setReferencia(proveedor);
			if(sucursal==null)
				sucursal=new String(gasto.getSucursal().getNombre());
	//		cargoAGastos.setReferencia2(sucursal);
			
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			
			String tipoAnt="";
			
			

			
			for(GCompraDet part:dets){
				
				
								
				importe=importe.add(part.getImporteMN());
				ivaAcumulado=ivaAcumulado.add(part.getImpuestoMN());
			}
			importeAcumulado=importeAcumulado.add(gasto.getCompra().getTotalMN());
	//		cargoAGastos.setDebe(importe.amount());
	//		cargoAGastos.setAsiento(asiento);		
			
			
			
			if(dets.get(0).getCompra().getFacturas().iterator().next().getRequisiciones().iterator().next().getRequisicion().getComentario()!=null)
			tipoAnt=dets.get(0).getCompra().getFacturas().iterator().next().getRequisiciones().iterator().next().getRequisicion().getComentario();
			
			if(tipoAnt.startsWith("INMUEBLE")){
				importe=importe.multiply(new BigDecimal(1.16));
				System.out.println("///////////********* si paso por aqui **************///////");
			}
			
			System.out.println("+++++++++********* que paso he **************+++++++++ Tipo Ant : "+tipoAnt);
			
			PolizaDetFactory.generarPolizaDet(poliza, cta, conceptoCta, true, importe.amount(), descripcion2, proveedor, sucursal, asiento);
		}
		
	}
	

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
