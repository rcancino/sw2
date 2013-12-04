package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.hssf.dev.BiffViewer;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.Matchers;

import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

/**
 * Genera el registro contable para las ventas
 * 
 * @author Ruben Cancino
 *
 */
public class Proc_Ventas implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		
		EventList<VentaPorSucursal> ventas=(EventList<VentaPorSucursal>)model.get("ventas");
		String asiento="VENTAS";
		
		String ref1=OrigenDeOperacion.CRE.name();
		Matcher m1=Matchers.beanPropertyMatcher(VentaPorSucursal.class, "origen", OrigenDeOperacion.CRE);
		FilterList<VentaPorSucursal> ventasCredito=new FilterList<VentaPorSucursal>(ventas,m1);
		
		Comparator c=GlazedLists.beanPropertyComparator(VentaPorSucursal.class, "sucursal");
		GroupingList<VentaPorSucursal> ventasCrePorSuc=new GroupingList<VentaPorSucursal>(ventas,c);
		
		for(List<VentaPorSucursal> vs:ventasCrePorSuc){
			BigDecimal importe=BigDecimal.ZERO;
			BigDecimal impuesto=BigDecimal.ZERO;
			VentaPorSucursal v1=vs.get(0);
			String desc2=MessageFormat.format("Ventas:{0} ",ref1);			
			String ref2=v1.getSucursal();
			
			for(VentaPorSucursal venta:vs){
				OrigenDeOperacion origen=venta.getOrigen();	
				if(origen.equals(OrigenDeOperacion.CRE)){
					
					importe=importe.add(venta.getImporte());
					impuesto=impuesto.add(venta.getImpuesto());
				}
			}
			
			PolizaDetFactory.generarPolizaDet(poliza, "401", "VTAS03", false, importe, desc2, ref1, ref2, asiento+" "+ref1);
			//Abono a Iva en Ventas x Acre
			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false, impuesto, desc2, ref1, ref2, asiento+" "+ref1);
		}
		
		
		for(VentaPorSucursal venta:ventas){
			
	
			OrigenDeOperacion origen=venta.getOrigen();
			
			String desc2=MessageFormat.format("Ventas:{0} ", venta.getOrigen().name());
			ref1=origen.name();
			String ref2=venta.getSucursal();
			String sucursalId=PolizaUtils.getSucursalId(ref2);			
			switch (origen) {
			case CAM:
				if(venta.getAnticipoAplicado().doubleValue()>0){
					
				
					//Abono a ventas
					BigDecimal importe=PolizaUtils.calcularImporteDelTotal(venta.getAnticipoAplicado());
					BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
					importe=PolizaUtils.redondear(importe);
					iva=PolizaUtils.redondear(iva);
					PolizaDetFactory.generarPolizaDet(poliza, "401", "VTAS02", false, importe,"Anticipo Aplicado "+  desc2, ref1, ref2, "ANTICIPO APLICADO "+origen);
					//Abono a Iva en Ventas
		//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, iva,"Anticipo Aplicado "+  desc2, ref1, ref2, "ANTICIPO APLICADO "+origen);
					
					//Cargo a Anticipo a Clientes
					PolizaDetFactory.generarPolizaDet(poliza, "204",venta.getClave(), true, importe,"Anticipo Aplicado "+  desc2, ref1, ref2, "ANTICIPO APLICADO "+origen);
		//			PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV05", true, iva,"Anticipo Aplicado "+  desc2, ref1, ref2, "ANTICIPO APLICADO "+origen);
					System.out.println("Cliente de anticipo: "+venta.getClave());
					//Cancelacion de IETU Aplicacion de Anticipo
					PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU05", false, importe,"IETU Anticipo Aplicado ", ref1, ref2, "ANTICIPO APLICADO "+origen);
					PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA05", true, importe,"IETU Anticipo Aplicado ", ref1, ref2, "ANTICIPO APLICADO "+origen);
					
					PolizaDetFactory.generarPolizaDet(poliza, "902", "AIETU02", true, importe,"IETU Anticipo Aplicado ", ref1, ref2, "ANTICIPO APLICADO "+origen);
					PolizaDetFactory.generarPolizaDet(poliza, "903", "IETUA02", false, importe,"IETU Anticipo Aplicado ", ref1, ref2, "ANTICIPO APLICADO "+origen);

					break;				}
				if (venta.getAnticipo()) {					
					
					PolizaDet res=PolizaDetFactory.generarPolizaDet(poliza, "105", venta.getClave(), true, venta.getTotal(),"Anticipo Facturado "+ desc2, ref1, ref2, "ANTICIPO FACTURADO "+origen);
					//PolizaDet res=PolizaDetFactory.generarPolizaDet(poliza, "105", "I010520", true, venta.getTotal(),"Anticipo Facturado "+ desc2, ref1, ref2, "ANTICIPO FACTURADO "+origen);
					if(res.getConcepto()==null){
						Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(venta.getClave());
						if(cliente!=null){
							ConceptoContable cc=PolizaDetFactory.generarConceptoContable(cliente.getClave(), cliente.getNombre(),"105");
							res.setConcepto(cc);
						}
					}
					
					PolizaDet res1=PolizaDetFactory.generarPolizaDet(poliza, "204", venta.getClave(), false, venta.getImporte(),"Anticipo Facturado "+ desc2, ref1, ref2,"ANTICIPO FACTURADO "+origen);
					//PolizaDet res1=PolizaDetFactory.generarPolizaDet(poliza, "204", "I010520", false, venta.getImporte(),"Anticipo Facturado "+ desc2, ref1, ref2,"ANTICIPO FACTURADO "+origen);
									PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV05", false, venta.getImpuesto(),"Anticipo Facturado "+ desc2, ref1, ref2,"ANTICIPO FACTURADO "+origen);
					if(res1.getConcepto()==null){
						Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(venta.getClave());
						if(cliente!=null){
							ConceptoContable cc=PolizaDetFactory.generarConceptoContable(cliente.getClave(), cliente.getNombre(),"204");
							res1.setConcepto(cc);
						}
					}
					
					
/*					//Cargo a clientes
					PolizaDetFactory.generarPolizaDet(poliza, "106", venta.getClave(), true, venta.getTotal(),"Anticipo Facturado "+ desc2, ref1, ref2, asiento+" "+origen);
					//Abono a anticipo clientes
					PolizaDetFactory.generarPolizaDet(poliza, "204", venta.getClave(), true, venta.getTotal(),"Anticipo Facturado "+ desc2, ref1, ref2, asiento+" "+origen);
					*/
					
				}else{
					
					System.out.println("sucursal "+ref2 +"Suc ID "+ sucursalId);
					
					//Abono a ventas De Producto Inventario
					PolizaDetFactory.generarPolizaDet(poliza, "401", "VTAS02", false, venta.getImporte(), desc2, ref1, ref2, asiento+" "+origen);
					//Abono a Iva en Ventas x Acre
					PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false, venta.getImpuesto(), desc2, ref1, ref2, asiento+" "+origen);
					//Cargo a clientes
					PolizaDetFactory.generarPolizaDet(poliza, "105", sucursalId, true, venta.getTotal(), desc2, ref1, ref2, asiento+" "+origen);
					}
				break;
			case CRE:
				//Abono a ventas
				//PolizaDetFactory.generarPolizaDet(poliza, "401", "VTAS03", false, venta.getImporte(), desc2, ref1, ref2, asiento+" "+origen);
				//Abono a Iva en Ventas x Acre
				//PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV02", false, venta.getImpuesto(), desc2, ref1, ref2, asiento+" "+origen);
				//Cargo a clientes
				
				PolizaDet res=PolizaDetFactory.generarPolizaDet(poliza, "106", venta.getClave(), true, venta.getTotal(), desc2, ref1, ref2, asiento+" "+origen);
				if(res.getConcepto()==null){
					Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(venta.getClave());
					if(cliente!=null){
						PolizaDetFactory.generarConceptoContable(cliente.getClave(), cliente.getNombre(),"106");
					}
				}
				break;
			case MOS:
				//Abono a ventas
				PolizaDetFactory.generarPolizaDet(poliza, "401", "VTAS01", false, venta.getImporte(), desc2, ref1, ref2, asiento+" "+origen);
				//Abono a Iva en Ventas x Acre
				PolizaDetFactory.generarPolizaDet(poliza, "206", "IVAV01", false, venta.getImpuesto(), desc2, ref1, ref2, asiento+" "+origen);
				break;
			default:
				throw new RuntimeException("Tipo de venta no soportado: "+origen);
			}
		}
		
		

		
	}
	
	
}
