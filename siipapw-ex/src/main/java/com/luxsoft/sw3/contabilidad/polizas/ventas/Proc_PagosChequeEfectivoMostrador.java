package com.luxsoft.sw3.contabilidad.polizas.ventas;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.UniqueList;

import com.luxsoft.siipap.cxc.model.Abogado;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.Ficha.TiposDeFicha;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConCheque;
import com.luxsoft.siipap.cxc.model.PagoConEfectivo;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;

public class Proc_PagosChequeEfectivoMostrador implements IProcesador{

	public void procesar(Poliza poliza, ModelMap model) {
		
		String asiento="COBRANZA FICHA";
		
	
		List<Ficha> fichas=(List<Ficha>)model.get("fichas");
		 fichas.addAll((List<Ficha>)model.get("fichasEfe"));
		Set<CargoAbono> ingresos=new HashSet<CargoAbono>();
		if(fichas==null) return;
		
		for(Ficha ficha:fichas){
		
			String  desc2=MessageFormat.format("Ficha - {0}",ficha.getTipoDeFicha()+ " Folio: "+ficha.getFolio());
			if(!ficha.getTipoDeFicha().equals("EFECTIVO")){
				
				PolizaDetFactory.generarPolizaDet(poliza,"102",ficha.getCuenta().getNumero().toString(), true, ficha.getTotal(), desc2, ficha.getOrigen().name(), ficha.getSucursal().getNombre(),asiento+" "+ficha.getOrigen().name());
			}else{
	
				if(ficha.getIngreso()!=null && DateUtils.isSameDay(poliza.getFecha(), ficha.getfechaDep())){
					ingresos.add(ficha.getIngreso());

				}
					
				if(ficha.getfechaDep().before(poliza.getFecha())){
					PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",true,ficha.getTotal()
	                           , "Faltante Anticipo: "+desc2,"MOS"
	                           ,ficha.getSucursal().getNombre(), asiento +" MOS");
				}
				
				
				if(ficha.getFecha().after(poliza.getFecha())){
					PolizaDetFactory.generarPolizaDet(poliza,"109", "198838",false,ficha.getTotal()
	                        , "Sobrante Anticipo: "+desc2,"MOS"
	                        ,ficha.getSucursal().getNombre(), asiento+" MOS");
				}
					
			}
				
		}
		
		for(CargoAbono ingreso:ingresos){
			
			PolizaDetFactory.generarPolizaDet(poliza,"102",ingreso.getCuenta().getNumero().toString(), true, ingreso.getImporte(),ingreso.getReferencia()+" "+ingreso.getFormaDePago(), "MOS",ingreso.getSucursal().getNombre(),asiento+" MOS");
		}
		
	
		EventList<Pago> pagos=(EventList<Pago>)model.get("pagos");
		if(pagos==null) return;

		Comparator<Pago> c=GlazedLists.beanPropertyComparator(Pago.class, "sucursal.id");
		GroupingList<Pago> pagosPorSucursal=new GroupingList<Pago>(pagos,c);
	
		for(List<Pago> lpagos:pagosPorSucursal){
			
			BigDecimal totalAplicado=BigDecimal.ZERO;
			BigDecimal importeAplicado=BigDecimal.ZERO;
			BigDecimal ivaAplicado=BigDecimal.ZERO;
			
			Pago pago=lpagos.get(0);
			for(Pago p:lpagos){
				
				if(p.isAnticipo())
				{
				continue;
				}
		
				String ori=p.getOrigenAplicacion();
				if(ori.equals("CAM")){
					if((p instanceof PagoConEfectivo)){
						if(DateUtils.isSameDay(p.getPrimeraAplicacion(), poliza.getFecha())){
                          
							totalAplicado=totalAplicado.add(p.getAplicado(poliza.getFecha()));
							//PolizaDetFactory.generarSaldoAFavor(poliza, p, p.getOrigenAplicacion(), asiento);
							PolizaDetFactory.generarOtrosIngresos(poliza, p, p.getOrigenAplicacion(), asiento);
						}
				
					}
					
				}
				PolizaDetFactory.generarSaldoAFavor(poliza, p, p.getOrigenAplicacion(), asiento);
			}
			
			importeAplicado=PolizaUtils.calcularImporteDelTotal(totalAplicado);		
			ivaAplicado=PolizaUtils.calcularImpuesto(importeAplicado);
			importeAplicado=PolizaUtils.redondear(importeAplicado);
			ivaAplicado=PolizaUtils.redondear(ivaAplicado);
			
			
			String ref2=pago.getSucursal().getNombre();
			String ref1="MOS";
			String origen="CAM";
		
				//Abono a cliente camioneta 
				PolizaDetFactory.generarPolizaDet(poliza,"105", pago.getSucursal().getId().toString(), false, totalAplicado, "Clientes cobranza Efectivo "+origen, origen, ref2, asiento+" "+ref1);
				PolizaDetFactory.generarPolizaDet(poliza,"206", "IVAV02", true,  MonedasUtils.calcularImpuestoDelTotal(totalAplicado), "Clientes cobranza "+origen, origen, ref2, asiento+" "+ref1);
				PolizaDetFactory.generarPolizaDet(poliza,"206", "IVAV01", false, MonedasUtils.calcularImpuestoDelTotal(totalAplicado), "Clientes cobranza "+origen, origen, ref2, asiento+" "+ref1);
			
		}	
	}
	


}
