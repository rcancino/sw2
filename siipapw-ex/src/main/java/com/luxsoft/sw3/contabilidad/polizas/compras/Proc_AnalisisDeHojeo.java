package com.luxsoft.sw3.contabilidad.polizas.compras;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.springframework.ui.ModelMap;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.inventarios.model.CostoHojeable;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.Transformacion;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.polizas.IProcesador;
import com.luxsoft.sw3.contabilidad.polizas.PolizaDetFactory;
import com.luxsoft.sw3.contabilidad.polizas.PolizaUtils;
import com.luxsoft.sw3.maquila.model.AnalisisDeHojeo;

public class Proc_AnalisisDeHojeo implements IProcesador {

	private boolean evaluarEntrada(Date fechaPoliza, AnalisisDeHojeo det) {
		/*Date fechaFactura = det.getCxpFactura().getFecha();
		Date fechaEntrada = fechaPoliza;

		int mesFactura = Periodo.obtenerMes(fechaFactura) + 1;
		int yearFactura = Periodo.obtenerYear(fechaFactura);
		int mesEntrada = Periodo.obtenerMes(fechaEntrada) + 1;
		int yearEntrada = Periodo.obtenerYear(fechaEntrada);

		if (yearFactura != yearEntrada)
			return false;
		return mesFactura == mesEntrada;*/
		return true;
	}
	

	public void procesar(Poliza poliza, ModelMap model) {

		EventList<AnalisisDeHojeo> analisis = (EventList<AnalisisDeHojeo>) model.get("analisisDeHojeo");
		String asiento = "Maquila corte";
		System.out.println("Proceando analisis de hojeo: "+analisis.size());

		for (AnalisisDeHojeo a : analisis) {
			Date fechaPol = poliza.getFecha();
			
			EventList source = GlazedLists.eventList(a.getEntradas());
			source.addAll(a.getTransformaciones());
			Comparator c = GlazedLists.beanPropertyComparator(EntradaDeMaquila.class, "sucursal.id");
			//final GroupingList<EntradaDeMaquila> entradasPorSucursal = new GroupingList<EntradaDeMaquila>(source, c);
			final GroupingList<Inventario> entradasPorSucursal = new GroupingList<Inventario>(source, c);
			
			CXPFactura fac = a.getCxpFactura();
			String ref1 = fac.getNombre();
			

			String desc2 = MessageFormat.format("Fac: {0}  {1,date, short}", fac.getDocumento(), fac.getFecha(),"A. HOJEO:");
			
			
			
			//for (List<EntradaDeMaquila> maqs : entradasPorSucursal) {
			for (List<Inventario> maqs: entradasPorSucursal) {
				BigDecimal importePorSucursal = BigDecimal.ZERO;
				Long almacenId= 0L;
				
			    	 String ref2 = maqs.get(0).getSucursal().getNombre();
				     String inventario_id=maqs.get(0).getId(); 
			
					
				     
				     
				     String sql=" SELECT max(x.almacen_id) as almacen_id FROM ( " +
					" SELECT max(r.almacen_id) as almacen_id FROM  sx_maq_analisis_hojeo a join sx_inventario_maq m on(a.ANALISIS_ID=m.ANALISIS_HOJEO_ID) join sx_maq_salida_hojeadodet s on(s.INVENTARIO_ID=m.INVENTARIO_ID) join sx_maq_recepcion_cortedet r on(s.RECEPCIONDET_ID=r.RECEPCIONDET_ID) where m.inventario_id=? " +
					" union " +
					" SELECT ifnull(sum(0),0) as almacen_id FROM  sx_maq_analisis_hojeo a join sx_inventario_trs m on(a.ANALISIS_ID=m.ANALISIS_HOJEO_ID) where m.inventario_id=? " +					
					" ) as x ";
				      
								
					
			//		String sql="select max(r.almacen_id) from sx_inventario_maq m join sx_maq_salida_hojeadodet s on(s.INVENTARIO_ID=m.INVENTARIO_ID) join sx_maq_recepcion_cortedet r on(s.RECEPCIONDET_ID=r.RECEPCIONDET_ID)"
			//					+" where m.inventario_id=?";
					 almacenId=(Long)ServiceLocator2.getJdbcTemplate().queryForObject(sql,new Object[]{inventario_id,inventario_id}, Long.class);
					
					System.out.println("almacen_id cual: "+inventario_id+" ref1 prov: "+ref1);
					
					String almacen="";
					 
					if(almacenId.equals(0l) )
					{
						TransformacionDet trs= (TransformacionDet) maqs.get(0);
						almacen= trs.getAnalisisHojeo().getClave();
						
						System.out.println("almacen_id cual: "+inventario_id+" ref1 prov: "+ref1 +" inventar : "+ trs.getId() +" hoje"+ trs.getAnalisisHojeo().getClave());
					}
					else{
						almacen=almacenId.toString();
					}
						
								
					for (final Inventario det : maqs) {
						CostoHojeable ca=(CostoHojeable)det;
						CantidadMonetaria costounitario=CantidadMonetaria.pesos(ca.getCostoCorte());
						costounitario=costounitario.multiply(det.getCantidadEnUnidad());
						importePorSucursal = importePorSucursal.add(costounitario.amount());

					}
					
				
				
				
				
				System.out.println("almacen_id: "+almacenId+" ref1: "+ref1);
				
				importePorSucursal = PolizaUtils.redondear(importePorSucursal);
				PolizaDetFactory.generarPolizaDet(poliza, "119",almacen, true, importePorSucursal, desc2, ref1,ref2, asiento);
			}
			// Cargo a IVA Acumulado por analisis
			PolizaDetFactory.generarPolizaDet(poliza, "117", "IVAC02",true, fac.getImpuestoMN().amount(), desc2, ref1,"TODAS", asiento);
			// Abono a proveedor
			PolizaDetFactory.generarPolizaDet(poliza, "200",fac.getClave(), false, fac.getTotalMN().amount(),desc2, ref1, "TODAS", asiento);
		
		}
	}

}
