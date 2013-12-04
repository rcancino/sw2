package com.luxsoft.sw3.contabilidad.polizas.cobranzacam;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.List;

import org.springframework.ui.ModelMap;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_PagoAnticipoFacturadoCam implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		 //System.out.println("Ya estoy en el procesador");
		List<Aplicacion> aplicaciones=(List<Aplicacion>)model.get("aplicaciones");
		for(Aplicacion aplic:aplicaciones){
			procesar(poliza,aplic);
		}
	}
	
void procesar(Poliza poliza, Aplicacion entidad) {
		
		Aplicacion aplic=(Aplicacion)entidad;
		// System.out.println("Ya estoy en el procesador");
		//for(NotaDeCargoDet det:cargo.getConceptos()){
			System.out.println("Procesamdp nota de cargo: "+aplic);
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CRE)){
			//if(det.getVenta().getOrigen().equals(OrigenDeOperacion.CHE)){
	//		if(aplic.get.getOrigen().equals(OrigenDeOperacion.CAM)){
				BigDecimal total=aplic.getImporte();
				BigDecimal importe=PolizaUtils.calcularImporteDelTotal(total);
				BigDecimal iva=PolizaUtils.calcularImpuesto(importe);
				BigDecimal tc=new BigDecimal(aplic.getAbono().getTc());
				total=PolizaUtils.redondear(total.multiply(tc));
				importe=PolizaUtils.redondear(importe.multiply(tc));
				iva=PolizaUtils.redondear(iva.multiply(tc));
				
				String asiento="COBRANZA ANTICIPO FACT ";
				
				String desc2=MessageFormat.format("Anticipo Facturado: {0}  Fac: {1} Cte:{2}",aplic.getCargo().getDocumento(),aplic.getCargo().getNombre());
				
				String ref1=aplic.getCargo().getOrigen().toString();
				String ref2=aplic.getCargo().getSucursal().getNombre();
				if(!(aplic.getAbono().getTipo().equals("PAGO_CHE") || aplic.getAbono().getTipo().equals("PAGO_EFE") || aplic.getAbono().getTipo().equals("PAGO_TAR") )){
				System.out.println("------------------------------------------------------------------------SI ENTRO-------------------------------------------------------------------");
				PolizaDet res=PolizaDetFactory.generarPolizaDet(poliza, "105", aplic.getCargo().getClave(), false, total,desc2, ref1, ref2, asiento+ref1);
				if(res.getConcepto()==null){
					Cliente cliente=ServiceLocator2.getClienteManager().buscarPorClave(aplic.getCargo().getClave());
					if(cliente!=null){
						ConceptoContable cc=PolizaDetFactory.generarConceptoContable(cliente.getClave(), cliente.getNombre(),"105");
						res.setConcepto(cc);
					}
				}
				String abono_id=aplic.getAbono().getId();
				String sql="select C.NUMERO FROM sx_cxc_abonos A JOIN sw_cuentas C ON(C.ID=A.CUENTA_ID) where ABONO_ID=?";
				String cuenta=(String)ServiceLocator2.getJdbcTemplate()
						.queryForObject(sql, new Object[]{abono_id}, String.class);
				if(cuenta==null)					
					throw new RuntimeException("No existe cargoabono para el ABONO_ID:"+abono_id);
				System.out.println("Cuenta: "+cuenta + "Abono_id: "+abono_id);
				//Cargo Bancos
			
				PolizaDetFactory.generarPolizaDet(poliza, "102",cuenta.toString(), true, total,desc2 , ref1, ref2, asiento+ref1);
				
				PolizaDetFactory.generarPolizaDet(poliza, "902","AIETU05", true, importe,desc2 , ref1, ref2, "IETU ANT FACT");
				
				PolizaDetFactory.generarPolizaDet(poliza, "903","IETUA05", false, importe,desc2 , ref1, ref2, "IETU ANT FACT");
				
				}
		
	}
	


}
