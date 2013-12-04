package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

public class Proc_ChequeTransito implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="CHEQUE TRANSITO";
		Requisicion requisicion=pago.getRequisicion();	
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			return;			
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
		if( fechaCobro==null || !DateUtil.isSameMonth(fechaPago, fechaCobro)){
			
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				poliza.setDescripcion(poliza.getDescripcion()+ " (TRANSITO PROVISIONADO)");
				canclearProvision(poliza, pago,factura,asiento);
				return;
				
			}else{
				poliza.setDescripcion(poliza.getDescripcion()+ " (TRANSITO)");
				registrarGastoPorChequeEnTransito(poliza,pago,asiento);
				return;
			}
			
		}
	}
	
	private void canclearProvision(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura,String asiento) {
		
		for(RequisicionDe detProv:pago.getRequisicion().getPartidas()){
	//		generarCargoAGastosAcumulado(detProv.getFacturaDeGasto(), poliza,pago,asiento);
			String pattern="FAC: {0} {1,date,short}";	
			String desc2=MessageFormat.format(pattern, detProv.getDocumento(),detProv.getFechaDocumento());
			BigDecimal total=detProv.getFacturaDeGasto().getCompra().getTotal();//getIvaDeGastos(det.getFacturaDeGasto(), poliza, pago, asiento);
		//	BigDecimal ivaRetenido=detProv.getFacturaDeGasto().getCompra().getRetencion1Imp();
		//	BigDecimal isrRetenido=detProv.getFacturaDeGasto().getCompra().getRetencion2Imp(); 
		//	iva=iva.subtract(ivaRetenido);
		//	PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", true, iva, desc2, pago.getAFavor(), "OFICINAS", asiento);
			
			
			
			//Cancelar la provision (C)
//			String pattern="PROVISION F:{0}  {1,date,short}";				
//			String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
			String ref1=factura.getCompra().getProveedor().getNombreRazon();
			String ref2=factura.getCompra().getSucursal().getNombre();
			
			if(factura.getCompra().getProveedor().getId()==245346L ){  //PAPER IMPORTS
				PolizaDetFactory.generarPolizaDet(poliza, "200", "P095", true, total, desc2, ref1, ref2, asiento);
			}else if(factura.getCompra().getProveedor().getId()==753345L){ //IMPAP
				PolizaDetFactory.generarPolizaDet(poliza, "200", "I001", true, total, desc2, ref1, ref2, asiento);
			}else{
				PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true, total, desc2, ref1, ref2, asiento);
			}
		}
		
		//Bancos (A)
		registrarAbonoABancos(poliza, pago,asiento);
	}
	
/*	
	private void canclearProvision(Poliza poliza,CargoAbono pago,GFacturaPorCompra factura,String asiento) {
		
			//Cancelar la provision (C)
		//Cancelar la provision (C)
		String pattern="PROVISION xx F:{0}  {1,date,short}";				
		String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		String ref1=factura.getCompra().getProveedor().getNombreRazon();
		String ref2=factura.getCompra().getSucursal().getNombre();
		PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true, factura.getTotalMN().amount(), desc2, ref1, ref2, asiento);
	
		//Bancos (A)
		registrarAbonoABancos(poliza, pago,asiento);
	}
	*/
	private void registrarGastoPorChequeEnTransito(Poliza poliza,CargoAbono pago,String asiento) {
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			generarCargoAGastosAcumulado(det.getFacturaDeGasto(), poliza,pago,asiento);
			String pattern="FAC: {0} {1,date,short}";	
			String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			BigDecimal iva=det.getFacturaDeGasto().getCompra().getImpuesto();//getIvaDeGastos(det.getFacturaDeGasto(), poliza, pago, asiento);
			BigDecimal ivaRetenido=det.getFacturaDeGasto().getCompra().getRetencion1Imp();
			BigDecimal isrRetenido=det.getFacturaDeGasto().getCompra().getRetencion2Imp(); 
			iva=iva.subtract(ivaRetenido);
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", true, iva, desc2, pago.getAFavor(), "OFICINAS", asiento);
			
			PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR02", true, ivaRetenido, desc2, pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR03", false, ivaRetenido, desc2, pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR04", false, isrRetenido, desc2, pago.getAFavor(), "OFICINAS", asiento);
		}
		registrarAbonoABancos(poliza, pago,asiento);
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
//		String sucursal=null;
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
//			if(sucursal==null)
//				sucursal=new String(gasto.getSucursal().getNombre());
			String sucursal=gasto.getSucursal().getNombre();
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
	/*
	private void registrarIvaPorAcreditarGastos(Poliza poliza,CargoAbono pago,BigDecimal iva,boolean abono,String asiento){
		// Abono a IVa por acreditar en gasto
		PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", !abono, iva, "", pago.getAFavor(), "OFICINAS", asiento);		
	}*/
	
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
