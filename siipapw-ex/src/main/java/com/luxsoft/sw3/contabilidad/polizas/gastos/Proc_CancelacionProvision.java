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
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_CancelacionProvision implements IProcesador{
	
	
	
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="CANCELACION DE PROVISION";
		/**debug**/
		if(pago.getId()==847806L){
			System.out.println("DEBUG...");
		}
		Requisicion requisicion=pago.getRequisicion();	
		if(requisicion==null) return;
		
		//Aqui CPG*
		Date fechaCobro=pago.getFechaCobro();		
		if(fechaCobro==null  || (!DateUtil.isSameMonth(fechaCobro, pago.getFecha()))) return;
		
		if(!PolizaUtils.esProvisionable(requisicion)) return;
		poliza.setDescripcion(poliza.getDescripcion()+" (CANCELA PROV)");
		
		//Verificar q no se trate de anticipo o parcialidades ?
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		if(concepto==201136L || concepto==737332L || concepto==307216L)
			return;
		
		//Lo puse arriba de Descripcion del maestro CPG*
//		Date fechaCobro=pago.getFechaCobro();		
//		if(fechaCobro==null  || (!DateUtil.isSameMonth(fechaCobro, pago.getFecha()))) return;	
		
		final Date fechaPago=pago.getFecha();
		
		BigDecimal ietu=BigDecimal.ZERO;
		
		GFacturaPorCompra factura=null;
		for(RequisicionDe de:requisicion.getPartidas()){
			 factura=de.getFacturaDeGasto();
			final Date fechaFactura=factura.getFecha();
			
			ietu=ietu.add(factura.getCompra().getIetu().amount());

			
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				
				String pattern="PROVISION F:{0}  {1,date,short}";				
				String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
				String ref1=factura.getCompra().getProveedor().getNombreRazon();
				String ref2=factura.getCompra().getSucursal().getNombre();
				
				if(factura.getCompra().getProveedor().getId()==245346L ){  //PAPER IMPORTS
					PolizaDetFactory.generarPolizaDet(poliza, "200", "P095", true, factura.getTotalMN().amount(), desc2, ref1, ref2, asiento);
				}else if(factura.getCompra().getProveedor().getId()==753345L){ //IMPAP
					PolizaDetFactory.generarPolizaDet(poliza, "200", "I001", true, factura.getTotalMN().amount(), desc2, ref1, ref2, asiento);
				}else{
					
					if(factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO))
						PolizaDetFactory.generarPolizaDet(poliza, "203", "DIVR04", true,factura.getTotalMN().amount(), desc2, ref1, ref2, asiento);
					else
						PolizaDetFactory.generarPolizaDet(poliza, "212", "PRVG03", true,factura.getTotalMN().amount(), desc2, ref1, ref2, asiento);				
			
				}
				
				
				BigDecimal iva=factura.getCompra().getImpuesto();
				iva=iva.subtract(factura.getCompra().getRetencion1Imp());
				
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, iva, desc2, pago.getAFavor(), "OFICINAS", asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG02", false, iva, desc2, pago.getAFavor(), "OFICINAS", asiento);	
				
				
		//		registrarRetenciones(pago, poliza,factura,asiento);
			}else{
				String pattern="F:{0}  {1,date,short}";				
				String desc2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
				
				generarCargoAGastosAcumulado(factura, poliza, pago, asiento);
				PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true, factura.getCompra().getImpuesto()
						, desc2, pago.getAFavor(), "OFICINAS", asiento);
			//	registrarRetenciones(pago, poliza,factura,asiento);
			}
		}
		registrarRetenciones(pago, poliza,factura,asiento);
		Proc_PagoNormal.registrarIetuAcumuladoPorConcepto(poliza, pago, asiento);
		//PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,ietu, "IETU Deducible ", pago.getAFavor(), "OFICINAS", asiento);				
		//PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false,ietu, "Deducible IETU", pago.getAFavor(), "OFICINAS", asiento);
		registrarAbonoABancos(poliza, pago,asiento);
		
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
				String pattern="PROVISION F:{0} {1,date,short}";				
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
				PolizaDetFactory.generarPolizaDet(p, "117","IVAR02", !cargo, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento);
				
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
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR02", cargo, monto.amount().abs(), desc2, pago.getAFavor(), det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR03", !cargo, monto.amount().abs(), desc2, pago.getAFavor(), det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				
			}			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				
				String pattern="PROV F:{0} {1,date,short}";						
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion2();
				boolean cargo=true;
				if(monto.amount().doubleValue()!=0){
					cargo=false;
					PolizaDetFactory.generarPolizaDet(p, "205","IMPR04", true, monto.amount().abs(), desc2, pago.getAFavor(), det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				}
				PolizaDetFactory.generarPolizaDet(p, "205","IMPR01", cargo, monto.amount().abs(), desc2, pago.getAFavor(), det.getFacturaDeGasto().getCompra().getSucursal().getNombre(), asiento);
				
			}
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
		//String sucursal=null;
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
	
	private ConceptoContable buscarConceptoContable(String clave){
		String hql="from ConceptoContable c where c.clave=?";
		List<ConceptoContable> res=ServiceLocator2.getHibernateTemplate().find(hql,clave);
		return res.isEmpty()?null:res.get(0);
	}

}
