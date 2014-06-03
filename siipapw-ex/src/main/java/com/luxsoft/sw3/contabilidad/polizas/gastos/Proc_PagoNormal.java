package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.BasicEventList;
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

public class Proc_PagoNormal implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="PAGO_NORMAL";
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
		if(factura.getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO) || factura.getCompra().getTipo().equals(TipoDeCompra.ESPECIAL) || factura.getCompra().getTipo().equals(TipoDeCompra.SEGURO))
					return;
		final Date fechaFactura=factura.getFecha();	
		Long concepto=pago.getRequisicion().getConcepto()!=null?pago.getRequisicion().getConcepto().getId():0L;
		
		if(concepto==737332L || concepto==201136L){
			return;
		}
		if(!DateUtil.isSameMonth(fechaPago, fechaCobro)){
			return;
			
		}else{
			if(PolizaUtils.esProvisionable(requisicion))
				return;
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				return;
			}else{
				registrarPagoNormal(poliza,pago,asiento);
			}
		}			
	}
	
	private void registrarPagoNormal(Poliza poliza,CargoAbono pago,String asiento) {
		poliza.setDescripcion(poliza.getDescripcion()+" (Normal) ");
		registrarAbonoABancos(poliza, pago,asiento);
		Requisicion req=pago.getRequisicion();
		//BigDecimal ietu=BigDecimal.ZERO;
		//BigDecimal iva=BigDecimal.ZERO;
		for(RequisicionDe det:req.getPartidas()){
			GFacturaPorCompra factura=det.getFacturaDeGasto();
			generarCargoAGastosAcumulado(factura, poliza,pago,asiento);
			String pattern="F:{0} {1,date,short}";				
			String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			BigDecimal iva=BigDecimal.ZERO;			
			iva=iva.add(factura.getImpuesto().abs().amount());
			iva=iva.subtract(factura.getCompra().getRet1MN().amount());
			//ietu=ietu.add(factura.getCompra().getIetu().amount());
			registrarDescuentos(factura, poliza, pago, asiento);
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true, iva, desc2, pago.getAFavor(), "OFICINAS", asiento);
			
		}
		
		registrarRetenciones(pago, poliza,asiento);
		registrarIetuAcumuladoPorConcepto(poliza, pago, asiento);
		//PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);	
		//PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
		//PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true, iva, "", pago.getAFavor(), "OFICINAS", asiento);
	}
	
	public static void registrarIetuAcumuladoPorConcepto(Poliza poliza,CargoAbono pago,String asiento){
		EventList<GCompraDet> partidasDeCompras=new BasicEventList<GCompraDet>(0);
		Requisicion req=pago.getRequisicion();
		
		for(RequisicionDe det:req.getPartidas()){
			GFacturaPorCompra factura=det.getFacturaDeGasto();
			partidasDeCompras.addAll(factura.getCompra().getPartidas());
		}
		Comparator c=GlazedLists.beanPropertyComparator(GCompraDet.class, "conceptoContable");
		
		GroupingList<GCompraDet> partidasPorConcepto=new GroupingList<GCompraDet>(partidasDeCompras, c);
		for(List<GCompraDet> grupo:partidasPorConcepto){
			BigDecimal ietu=BigDecimal.ZERO;
			String descripcionDeConcepto=grupo.get(0).getConceptoContable();
			for(GCompraDet det:grupo){
				ietu=ietu.add(det.getIetu().amount());
			}
			PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "900", descripcionDeConcepto, true,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
			PolizaDetFactory.generarPolizaDetPorDescripcionDeConcepto(poliza, "901", descripcionDeConcepto, false,ietu, "IETU Deducible", pago.getAFavor(), "OFICINAS", asiento);
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
	
	private void registrarDescuentos(final GFacturaPorCompra factura,final Poliza poliza,final CargoAbono pago,String asiento){
		for(GCompraDet gasto:factura.getCompra().getPartidas()){
			if(gasto.getDescuento1()>0){
				List<Map<String, Object>> notas=ServiceLocator2.getJdbcTemplate()
						.queryForList("SELECT * FROM SX_GNOTAS_DET WHERE FACTURA_ID=?", new Object[]{factura.getId()});
				for(Map<String,Object> nota:notas){
					System.out.println("Registrando nota de gastos: "+nota);
					BigDecimal importe=(BigDecimal)nota.get("IMPORTE");
					BigDecimal impuesto=(BigDecimal)nota.get("IMPUESTO");
					BigDecimal total=(BigDecimal)nota.get("TOTAL");
					String documento=(String)nota.get("DOCUMENTO");
					
					PolizaDetFactory.generarPolizaDet(poliza, "600", "151688", false, total
							, "DESCUENTO NOTAS GASTOS DOCTO: "+documento
							, ""
							, gasto.getSucursal().getNombre()
							, asiento);
					
					PolizaDetFactory.generarPolizaDet(poliza, "600", "151688", true, importe
							, "DESCUENTO NOTAS GASTOS DOCTO: "+documento
							, ""
							, gasto.getSucursal().getNombre()
							, asiento);
					
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01", true, impuesto
							, "DESCUENTO NOTAS GASTOS DOCTO: "+documento
							, ""
							, gasto.getSucursal().getNombre()
							, asiento);
				}
				return;
			}
		}
	}
	
	private void registrarRetenciones(final CargoAbono pago,final Poliza p,String asiento){
		
		//BigDecimal retencion=factura.getCompra().getRetencionesMN().abs().amount();
		//if(retencion.doubleValue()==0){
			//return;
		//}
		
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
				String pattern="F:{0} {1,date,short}";				
				String desc2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				CantidadMonetaria monto=det.getRetencion1();
				boolean cargo=false;
				String ref2="";
				if(monto.amount().doubleValue()!=0){
					cargo=true;
					if(pago.getSucursal()!=null)
						ref2=pago.getSucursal().getNombre();
					
				}
				PolizaDetFactory.generarPolizaDet(p, "117","IVAR01", cargo, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento+"XXX");
				
			}
			
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				String pattern="F:{0} {1,date,short}";						
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
				
				String pattern="F:{0} {1,date,short}";						
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
