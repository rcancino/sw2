package com.luxsoft.sw3.contabilidad.polizas.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

public class Proc_PagoEspecialRH implements IProcesador{
	
	public void procesar(Poliza poliza, ModelMap model) {
		CargoAbono pago=(CargoAbono)model.get("pago");
		Requisicion requisicion=pago.getRequisicion();	
		if(requisicion==null) return;
		if(requisicion.getConcepto()==null) return;
		
		String asiento="RH "+requisicion.getConcepto().getClave();
		final Date fechaPago=pago.getFecha();
		Date fechaCobro=pago.getFechaCobro();
		if(fechaCobro==null)
			fechaCobro=fechaPago;			
		GFacturaPorCompra factura=requisicion
								.getPartidas().iterator().next()
								.getFacturaDeGasto();
		if(factura==null)
			return;
		//Descriminar RH Especial
		if(factura.getCompra().getTipo().equals(TipoDeCompra.ESPECIAL)){
			poliza.setDescripcion(poliza.getDescripcion()+" ("+requisicion.getConcepto().getClave()+" RH)");
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
					String pattern="DOC: {0}  ({1,date,short}), {2}";
					System.out.println("-------"+gasto.getId()+"----------"+gasto.getComentario());
					String descripcion2=MessageFormat.format(pattern
							, gasto.getFactura()
							,factura.getFecha()							
							,gasto.getRubro().getCuentaContable().equals("110-V001-000")?gasto.getComentario():
								gasto.getRubro().getCuentaContable().equals("118-0002-000")?gasto.getComentario():
								gasto.getRubro()!=null?gasto.getRubro().getDescripcion():"SIN CONCEPTO DE GASTO"
							);
					cargoAGastos.setDescripcion2(descripcion2);
					String proveedor=factura.getProveedor();
					cargoAGastos.setReferencia(proveedor);
					String ref2=gasto.getSucursal()!=null?gasto.getSucursal().getNombre():"SIN SUCURSAL ASIGNADA";
					cargoAGastos.setReferencia2(gasto.getSucursal().getNombre());
					
					if(gasto.getImporte().doubleValue()<=0){
						cargoAGastos.setHaber(gasto.getImporte().abs());
					}else{
						cargoAGastos.setDebe(gasto.getImporte().abs());	
					}
										
				}
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
