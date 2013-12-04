package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.ContaUtils;
import com.luxsoft.siipap.model.contabilidad.GeneradorDePoliza;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Implementacion de {@link GeneradorDePoliza} para crear la o las polizas
 * de gastos a partir de Facturas de gastos
 * 
 * @author Ruben Cancino 
 *
 */
public class GeneradorDePolizaDeGastos implements GeneradorDePoliza{

	@SuppressWarnings("unchecked")
	public Poliza generar(Object...params) {
		List<GFacturaPorCompra> facs=(List<GFacturaPorCompra>)params[0];
		Poliza p=generaPolizaDeGastos(facs);
		ContaUtils.depurarPoliza(p);
		return p;
	}
	
	/**
	 * Genera una poliza de gastos a partir de una factura de gastos recibida
	 * 
	 * @param factura
	 * @return
	 */
	public  Poliza generaPolizaDeGastos(final List<GFacturaPorCompra> facturas){
		Assert.notEmpty(facturas,"No se puede genera la poliza sin facturas de gastos");
		final Date fecha=facturas.get(0).getFechaContable();
		Poliza p=new Poliza();
		p.setTipo("Dr");
		p.setConcepto(MessageFormat.format("Provision de facturas", DateUtil.convertDateToString(fecha)));
		p.setFecha(fecha);
		p.setYear(DateUtil.toYear(fecha));
		p.setMes(DateUtil.toMes(fecha));
		
		for(GFacturaPorCompra fac:facturas){
			registrarGastosAgrupados(fac.getCompra(), p.getRegistros(),fac.getDocumento());
		}
		registrarProvision(p, facturas);
		registrarIva(p, facturas);
		registrarRetenciones(p, facturas);
		return p;
	}
	
	/**
	 * Registra los gastos agrupandolos por rubro y sucursal
	 * 
	 * @param compra
	 * @param registros
	 */
	@SuppressWarnings("unchecked")
	public void registrarGastosAgrupados(final GCompra compra,final List<AsientoContable> registros,final String factura){		
		final EventList<GCompraDet> eventList=GlazedLists.eventList(compra.getPartidas());
		final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
		final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
		Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> row=groupList.get(index);
			registrarGasto(row,registros,factura);			
		}
	}
	
	/**
	 * 
	 * @param dets
	 * @param registros
	 */
	public void registrarGasto(final List<GCompraDet> dets,final List<AsientoContable> registros,final String factura){
		GCompraDet det=dets.get(0);
		AsientoContable asiento=new AsientoContable();
		asiento.setCuenta(getCuentaContable(det));
		asiento.setDescripcion(getDescripcion(det));
		asiento.setDescripcion2(dets.get(0).getCompra().getProveedor().getNombreRazon());
		if( (det.getRubro()!=null) || (det.getRubro().getRubroCuentaOrigen()!=null) ){
			ConceptoDeGasto concepto=det.getRubro().getRubroCuentaOrigen();
			String cc=concepto!=null?concepto.getDescripcion():"NA";
			asiento.setConcepto(cc);	
		}
		asiento.setSucursal(det.getSucursal().getNombre());
		CantidadMonetaria debe=CantidadMonetaria.pesos(0);
		for(GCompraDet part:dets){
			debe=debe.add(part.getImporteMN());
		}
		asiento.setDebe(debe);
		asiento.setDescripcion3(factura);
		registros.add(asiento);
	}
	
	public String getCuentaContable(GCompraDet det){
		
		String cc=det.getRubro().getCuentaOrigen();
		if(cc.startsWith("600")){
			String prefix=StringUtils.substring(cc,0,1);
			String suffix=StringUtils.substring(cc,3,cc.length());
			if(det.getSucursal()==null)
				throw new RuntimeException("Detalle de compra sin sucursal asignada. GCompraDet: "+det);
			String suc=String.valueOf(det.getSucursal().getClaveContable());		
			suc=StringUtils.leftPad(suc, 2,'0');
			
			String cta=prefix+suc+suffix;
			return cta;
		}else{
			return cc;
		}
		
	}
	
	public static String getDescripcion(GCompraDet det){
		String pattern="F-{0} {1}";
		String descripcion=MessageFormat.format(pattern, det.getFactura(),det.getProducto().getDescripcion());
		descripcion=StringUtils.substring(descripcion, 0,28);
		return descripcion;
	}
	
	public static void registrarProvision(Poliza p,List<GFacturaPorCompra> facs){
		for(GFacturaPorCompra fac:facs){
			AsientoContable a=new AsientoContable();
			String pattern="PROV F:{0} {1}";
			String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion(descripcion);
			a.setCuenta("212-0006-000");
			a.setHaber(fac.getTotatMN());
			a.setSucursal(fac.getCompra().getSucursal().getNombre());
			a.setConcepto("Provision de Gastos");
			a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion3(fac.getDocumento());
			p.getRegistros().add(a);
		}
	}
	
	public static void registrarIva(Poliza p,List<GFacturaPorCompra> facs){
		//Impuesto de gastos
		for(GFacturaPorCompra fac:facs){
			AsientoContable a=new AsientoContable();
			String pattern="IVA F:{0} {1}";
			String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion(descripcion);
			a.setCuenta("117-0006-003");
			CantidadMonetaria impuestoGastos=CantidadMonetaria.pesos(0);
			for(GCompraDet det:fac.getCompra().getPartidas()){
				if(!det.getProducto().getInversion()){
					CantidadMonetaria imp=det.getImpuestoMN();
					impuestoGastos=impuestoGastos.add(imp);
				}
			}
			a.setDebe(impuestoGastos);
			//a.setDebe(fac.getImpuesto());
			if( (fac.getCompra().getRet1MN()!=null) &&( fac.getCompra().getRet1MN().amount().doubleValue()>0)){
				a.setDebe(a.getDebe().subtract(fac.getCompra().getRet1MN()));
			}
			a.setSucursal(fac.getCompra().getSucursal().getNombre());
			a.setConcepto("Iva x acreditar");
			a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion3(fac.getDocumento());
			p.getRegistros().add(a);
		}
		/**Impuesto de gastos**/
		for(GFacturaPorCompra fac:facs){
			AsientoContable a=new AsientoContable();
			String pattern="IVA F:{0} {1}";
			String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion(descripcion);
			a.setCuenta("117-0006-004");
			CantidadMonetaria impuestoGastos=CantidadMonetaria.pesos(0);
			for(GCompraDet det:fac.getCompra().getPartidas()){
				if(det.getProducto().getInversion()){
					CantidadMonetaria imp=det.getImpuestoMN();
					impuestoGastos=impuestoGastos.add(imp);
				}
			}
			a.setDebe(impuestoGastos);
			/*
			if( (fac.getCompra().getRet1MN()!=null) &&( fac.getCompra().getRet1MN().amount().doubleValue()>0)){
				a.setDebe(a.getDebe().subtract(fac.getCompra().getRet1MN()));
			}
			*/
			a.setSucursal(fac.getCompra().getSucursal().getNombre());
			a.setConcepto("Iva x acreditar");
			a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion3(fac.getDocumento());
			p.getRegistros().add(a);
		}
	}
	
	public static final double RETENCION_TRANSPORTE=4.0;
	public static final double RETENCION_HONORARIOS=10.0;
	
	public static final String IVA_RET_PEND_POR_ACRED="117-0006-005";	
	
	public static final String IVA_RET_PEND_POR_PAGAR="208-0001-000";
	
	public static final String ISR_RET_PEND_POR_PAGAR="208-0002-000";
	
	public static final String CUENTA_ISR_RETENIDO="205-0003-000";
	
	public static void registrarRetenciones(Poliza p,List<GFacturaPorCompra> facs){
		
		for(GFacturaPorCompra fac:facs){			
			
			for(GCompraDet det:fac.getCompra().getPartidas()){				
				if(det.getRetencion1()==RETENCION_HONORARIOS){
					//honorarios=honorarios.add(det.getRetencion1Imp());
					//honorarios=det.getRetencion1Imp();
					AsientoContable a=new AsientoContable();
					String pattern="F:{0} {1}";
					String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion(descripcion);
					a.setCuenta(IVA_RET_PEND_POR_ACRED);
					a.setDebe(CantidadMonetaria.pesos(det.getRetencion2Imp().doubleValue()));			
					a.setConcepto("IVA Ret Pend X Acred");
					a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a);				
					
					AsientoContable a2=new AsientoContable();				
					a2.setDescripcion(descripcion);
					a2.setCuenta(IVA_RET_PEND_POR_PAGAR);
					a2.setHaber(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()));			
					a2.setConcepto("IVA Ret Pend X Pagar");
					a2.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a2.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a2);
					
					AsientoContable a3=new AsientoContable();				
					a3.setDescripcion(descripcion);
					a3.setCuenta(ISR_RET_PEND_POR_PAGAR);
					a3.setHaber(CantidadMonetaria.pesos(det.getRetencion2Imp().doubleValue()));			
					a3.setConcepto("ISR Ret Pend X Pagar");
					a3.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a3.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a3);
					continue;
				}else if(det.getRetencion1()==RETENCION_TRANSPORTE){
					//Cargo a Iva Acreditable Retenido
					AsientoContable a=new AsientoContable();
					String pattern="F:{0} {1}";
					String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion(descripcion);
					a.setCuenta(IVA_RET_PEND_POR_ACRED);
					a.setDebe(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()));			
					a.setConcepto("IVA Ret Pend X Acred");
					a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a);
					
					//Abono a IVA RETENIDO
					AsientoContable a2=new AsientoContable();				
					a2.setDescripcion(descripcion);
					a2.setCuenta(IVA_RET_PEND_POR_PAGAR);
					a2.setHaber(CantidadMonetaria.pesos(det.getRetencion1Imp().doubleValue()));			
					a2.setConcepto("IVA Ret Pend X Pagar");
					a2.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a2.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a2);
				}
			}
		}		
			
	}
	
	

}
