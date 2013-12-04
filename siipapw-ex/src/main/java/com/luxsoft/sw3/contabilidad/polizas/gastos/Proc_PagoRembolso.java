package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.gastos.TipoDeCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;

public class Proc_PagoRembolso implements IProcesador{
	
	@Autowired
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		String asiento="REMBOLSO";
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
		if(factura.getCompra().getTipo().equals(TipoDeCompra.REEMBOLSO)){
			poliza.setDescripcion(poliza.getDescripcion()+" (Rembolso) ");
			registrarAbonoABancos(poliza, pago,asiento);
			Requisicion req=pago.getRequisicion();
			
			for(RequisicionDe det:req.getPartidas()){
				GCompra compra=det.getFacturaDeGasto().getCompra();
				for(GCompraDet gasto:compra.getPartidas()){
					
					ConceptoDeGasto concepto=gasto.getRubro();
					PolizaDet cargoAGastos=poliza.agregarPartida();
					
					if(concepto!=null){							
						concepto=concepto.getRubroSegundoNivel(concepto);
						ConceptoContable conceptoContable=buscarConceptoContable(concepto.getId().toString());
						if(conceptoContable!=null){
							cargoAGastos.setConcepto(conceptoContable);
							cargoAGastos.setCuenta(conceptoContable.getCuenta());
						}			
					}else{
						System.out.println("Rubro de gasto: +"+gasto.getRubro()+ " sin concepto contable GcompraDet.id: "+gasto.getId());
					}
					String pattern="RE: {0}  ({1,date,short}), {2}";
					
									
					String descripcion2=MessageFormat.format(pattern
					//		, gasto.getFacturaRembolso() 
							,gasto.getFacturaRembolso()!=null?"Fac:"+gasto.getFacturaRembolso():"Compra:"+gasto.getFactura()
							,factura.getFecha()							
							,gasto.getRubro().getCuentaContable().equals("110-V001-000")?gasto.getComentario():
								gasto.getRubro()!=null?gasto.getRubro().getDescripcion():"SIN CONCEPTO DE GASTO"
							);
					cargoAGastos.setDescripcion2(descripcion2);
					String proveedor=gasto.getProveedorRembolso()!=null?gasto.getProveedorRembolso().getNombreRazon():"ERROR: ASIGNAR PROVEEDOR";
					cargoAGastos.setReferencia(proveedor);
					String ref2=gasto.getSucursal()!=null?gasto.getSucursal().getNombre():"SIN SUCURSAL ASIGNADA";
					cargoAGastos.setReferencia2(gasto.getSucursal().getNombre());
					
					if(gasto.getImporte().doubleValue()<=0.0){
						cargoAGastos.setHaber(gasto.getImporte().abs());
					}else{
						cargoAGastos.setDebe(gasto.getImporte().abs());	
					}
					
					
					cargoAGastos.setAsiento(asiento);
					
					PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAG01",true
							, gasto.getImpuestoMN().amount().subtract(gasto.getRetencion1Imp())
							,descripcion2
							,proveedor
							, ref2
							, asiento);
					
					PolizaDetFactory.generarPolizaDet(poliza, "117","IVAR01", true, gasto.getRetencion1Imp(), descripcion2, proveedor, ref2, asiento);
					
					PolizaDetFactory.generarPolizaDet(poliza, "205","IMPR02", false, gasto.getRetencion1Imp(), descripcion2, proveedor, ref2, asiento);
					
					
					
					PolizaDetFactory.generarPolizaDet(poliza, "900", "IETUD02", true,gasto.getIetu().amount()
							, "IETU Deducible", proveedor, ref2, asiento);					
					PolizaDetFactory.generarPolizaDet(poliza, "901", "DIETU02", false,gasto.getIetu().amount()
							, "IETU Deducible", proveedor, ref2, asiento);
					
				}
			}			
			registrarRetenciones(pago, poliza, factura, asiento);
		}
		
						
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
//				PolizaDetFactory.generarPolizaDet(p, "117","IVAR01", cargo, monto.amount().abs(), desc2, pago.getAFavor(), ref2, asiento);
				
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
	/*			PolizaDetFactory.generarPolizaDet(p, "205","IMPR02", cargo, monto.amount().abs()
						, desc2, pago.getAFavor()
						, det.getFacturaDeGasto().getCompra().getSucursal().getNombre()
						, asiento); */
				
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
