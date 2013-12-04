package com.luxsoft.siipap.model.gastos;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.ContaUtils;
import com.luxsoft.siipap.model.contabilidad.GeneradorDePoliza;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Implementacion de {@link GeneradorDePoliza} para crear la o las polizas
 * de gastos a partir de un grupo de pagos para un dia
 * 
 * @author Ruben Cancino 
 *
 */
public class GeneradorDePolizaDePagos2 implements GeneradorDePoliza{

	@SuppressWarnings("unchecked")
	public Poliza generar(Object...params) {
		CargoAbono pagos=(CargoAbono)params[0];
		Poliza p= generaPolizaDePagos(pagos);
		ContaUtils.depurarPoliza(p);
		return p;
	}
	
	/**
	 * Genera una poliza de pago a partir de una lista de {@link CargoAbono}
	 * 
	 * @param factura
	 * @return
	 */
	public  Poliza generaPolizaDePagos(CargoAbono pago){
		final Date fecha=pago.getFecha();
		Poliza p=new Poliza();
		if(pago.getFormaDePago().equals(FormaDePago.CHEQUE))
			p.setTipo(pago.getCuenta().getClave());
		else
			p.setTipo("Dr");
		
		// Le pone titulo a la poliza
		registrarConcepto(pago, p);
		
		// Genera asientos contables		
		//cargoAProvision(pago, p);
		
		//Generamos el Gasto
		List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			facturas.add(det.getFacturaDeGasto());
		}
		for(GFacturaPorCompra fac:facturas){
			if(fac==null)
				continue;
			Periodo periodo=Periodo.getPeriodoEnUnMes(fecha);
			//System.out.println("F.Fac: "+fac.getFecha());
			//Assert.notNull(fac,"La  factura es nula");
			//Assert.notNull(fac.getFecha(),"La fecha de la factura:"+fac.getDocumento()+" es nula");
			if(periodo.isBetween(fac.getFecha())){
				registrarGastosAgrupados(fac.getCompra(), p.getRegistros(),fac.getDocumento());
				registrarIva(p, facturas);
				registrarRetenciones(p, facturas);
			}else{
				//Matar la provision
				matarProvision(fac, p);
			}
		}
		
		
		//Otros asientos diversos relacionados con los pagos
		afectarBancos(pago, p);
		cargoAIva(pago, p);
		registrarRetenciones(pago, p);
		registrarFletesMensajeria(pago,p);
		
		// Asigna fecha,mes y año a la poliza
		p.setFecha(fecha);
		p.setYear(DateUtil.toYear(fecha));
		p.setMes(DateUtil.toMes(fecha));
		
		return p;
	}
	
	private void registrarConcepto(final CargoAbono pago,final Poliza p){
		String forma=pago.getFormaDePago().name();
		forma=StringUtils.substring(forma, 0,2);
		String pattern="{0} {1} {2}";
		String ms=MessageFormat.format(pattern, forma,pago.getReferencia(),pago.getAFavor());
		ms=StringUtils.substring(ms, 0,120);
		p.setConcepto(ms);
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
	
	public static void registrarIva(Poliza p,List<GFacturaPorCompra> facs){
		
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
	
	public static final double RETENCION_HONORARIOS_1=10.0;
	
	public static final double RETENCION_HONORARIOS_2=28.0;
	
	public static final String IVA_RET_PEND_POR_ACRED="117-0006-005";	
	
	public static final String IVA_RET_PEND_POR_PAGAR="208-0001-000";
	
	public static final String ISR_RET_PEND_POR_PAGAR="208-0002-000";
	
	public static final String CUENTA_ISR_RETENIDO="205-0003-000";
	
	public static void registrarRetenciones(Poliza p,List<GFacturaPorCompra> facs){
		
		for(GFacturaPorCompra fac:facs){			
			
			for(GCompraDet det:fac.getCompra().getPartidas()){				
				if(det.getRetencion1()==RETENCION_HONORARIOS_1){
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
				}else if(det.getRetencion1()==RETENCION_HONORARIOS_2){
					
					// Cargo 2% sobre nomina
					final AsientoContable a=new AsientoContable();
					String pattern="AL CONSEJO {0}";
					String descripcion=MessageFormat.format(pattern, dateFormat.format(fac.getFecha()));
					a.setDescripcion(descripcion);
					a.setCuenta("600-0001-016");
					CantidadMonetaria ret=det.getImporteMN();
					ret=ret.multiply(.02);
					a.setDebe(ret);			
					a.setConcepto("2% Gasto  S/Nom");
					a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a);				
					
					final AsientoContable a2=new AsientoContable();
					a2.setDescripcion(descripcion);
					a2.setCuenta("205-0015-000");
					CantidadMonetaria pasivo=det.getImporteMN();
					pasivo=pasivo.multiply(.02);
					a2.setHaber(pasivo);			
					a2.setConcepto("2% Pasivo x ret S/Nom");
					a2.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
					a2.setDescripcion3(fac.getDocumento());
					p.getRegistros().add(a2);
					
					AsientoContable a3=new AsientoContable();				
					a3.setDescripcion(descripcion);
					a3.setCuenta("205-0002-000");
					CantidadMonetaria monto=det.getImporteMN();
					monto=monto.multiply(.28);
					a3.setHaber(monto);			
					a3.setConcepto("ISR Honorarios al consejo");
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
	
	static DateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy");
	/**
	 *
	 * 
	 * @param pago
	 * @param p
	 */
	private void matarProvision(final GFacturaPorCompra factura,final Poliza p){
		
		AsientoContable a=new AsientoContable();
		String pattern="PROV F:{0} {1}";				
		String descripcion=MessageFormat.format(pattern, factura.getDocumento(),factura.getProveedor());
		a.setDescripcion(descripcion);
		a.setCuenta("212-0006-000");
		a.setDebe(factura.getTotalMN());
		a.setConcepto("Provision de Gastos");
		a.setDescripcion2(dateFormat.format(factura.getFecha()));
		a.setDescripcion3(factura.getDocumento());
		p.getRegistros().add(a);
			
		
	}
	
	private void afectarBancos(final CargoAbono pago,final Poliza p){
		AsientoContable a1=new AsientoContable();		
		a1.setCuenta(pago.getCuenta().getCuentaContable());
		String c=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		c=StringUtils.substring(c, 0,28);
		a1.setConcepto(c);
		a1.setHaber(pago.getImporteMN().abs());
		a1.setDescripcion(StringUtils.substring(p.getConcepto(), 0,28));
		p.getRegistros().add(a1);
	}
	
	private void cargoAIva(final CargoAbono pago,final Poliza p){
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){	
			Periodo periodo=Periodo.getPeriodoEnUnMes(pago.getFecha());
			/*if(!periodo.isBetween(det.getFechaDocumento())){
				continue;
			}*/
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("Iva Acreditable");
			a1.setCuenta("117-0005-003");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			CantidadMonetaria monto=det.getImpuestoParaPolizaMN();
			Assert.isTrue(monto.getCurrency().equals(MonedasUtils.PESOS),"El monto debe ser en pseos");
			monto=monto.subtract(det.getRetencion1());
			if(monto.amount().doubleValue()!=0){
				a1.setDebe(monto);
				a1.setDescripcion3(det.getDocumento());
				p.getRegistros().add(a1);
			}
			
		}
		
		
		 //IVA de LA provision
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			Periodo periodo=Periodo.getPeriodoEnUnMes(pago.getFecha());
			if(periodo.isBetween(det.getFechaDocumento())){
				continue;
			}
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("Iva por acreditar");
			a1.setCuenta("117-0006-003");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			CantidadMonetaria monto=det.getImpuestoParaPolizaMN();
			monto=monto.subtract(det.getRetencion1());
			if(monto.amount().doubleValue()!=0){
				a1.setHaber(monto);
				a1.setDescripcion3(det.getDocumento());
				p.getRegistros().add(a1);
			}
			
		}
		
		
		/** Gastos**/
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("IETU ");
			a1.setCuenta("900-0002-000");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			//a1.setDebe(det.getIetu());
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(!prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setDebe(ietu);
			a1.setDescripcion3(det.getDocumento());
			p.getRegistros().add(a1);
		}
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("IETU ");
			a1.setCuenta("901-0002-000");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(!prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			//a1.setHaber(det.getIetu());
			a1.setHaber(ietu);
			
			a1.setDescripcion3(det.getDocumento());
			p.getRegistros().add(a1);
		}
		
		/** Activo Fijo**/
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("IETU ");
			a1.setCuenta("900-0003-000");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			//a1.setDebe(det.getIetu());
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setDebe(ietu);
			a1.setDescripcion3(det.getDocumento());
			p.getRegistros().add(a1);
		}
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("IETU ");
			a1.setCuenta("901-0003-000");
			String pattern="F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			a1.setDescripcion(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			//a1.setHaber(det.getIetu());
			a1.setHaber(ietu);
			
			a1.setDescripcion3(det.getDocumento());
			p.getRegistros().add(a1);
		}
		
	}
	
	private void registrarRetenciones(final CargoAbono pago,final Poliza p){
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				AsientoContable a=new AsientoContable();
				String pattern="PROV F:{0} {1}";				
				String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
				a.setDescripcion(descripcion);
				a.setCuenta("117-0005-005");
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					a.setDebe(monto);
					if(pago.getSucursal()!=null)
						a.setSucursal(pago.getSucursal().getNombre());
					else
						a.setSucursal("NA");
					a.setConcepto("IVA Acred Ret");
					String pat="{0} {1}";
					a.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					a.setDescripcion3(det.getDocumento());
					p.getRegistros().add(a);
				}
				
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				AsientoContable a=new AsientoContable();
				String pattern="PROV F:{0} {1}";				
				String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
				a.setDescripcion(descripcion);
				a.setCuenta("205-0011-000");
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					a.setHaber(monto);
					if(pago.getSucursal()!=null)
						a.setSucursal(pago.getSucursal().getNombre());
					else
						a.setSucursal("NA");
					a.setConcepto("IVA Retenido");
					String pat="{0} {1}";
					a.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					a.setDescripcion3(det.getDocumento());
					p.getRegistros().add(a);
				}
			}
			/*********************************************************************/ 
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				AsientoContable a=new AsientoContable();
				String pattern="PROV F:{0} {1}";				
				String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
				a.setDescripcion(descripcion);
				a.setCuenta("208-0001-000");
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					a.setDebe(monto);
					if(pago.getSucursal()!=null)
						a.setSucursal(pago.getSucursal().getNombre());
					else
						a.setSucursal("NA");
					a.setConcepto("IVA Pend Ret");
					String pat="{0} {1}";
					a.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					a.setDescripcion3(det.getDocumento());
					p.getRegistros().add(a);
				}
				
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				AsientoContable a=new AsientoContable();
				String pattern="PROV F:{0} {1}";				
				String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
				a.setCuenta("117-0006-005");
				a.setDescripcion(descripcion);
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					a.setHaber(monto);
					if(pago.getSucursal()!=null)
						a.setSucursal(pago.getSucursal().getNombre());
					else
						a.setSucursal("NA");
					a.setConcepto("IVA por Acred Ret");
					String pat="{0} {1}";
					a.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					a.setDescripcion3(det.getDocumento());
					p.getRegistros().add(a);
				}
			}
		}
	}
	
	private void registrarFletesMensajeria(final CargoAbono pago,final Poliza p){
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			if(det.getFacturaDeGasto()==null){
				System.out.println("ReqDet: sin factura "+det.getId());
				return;
			}
			GCompra compra=det.getFacturaDeGasto().getCompra();
			boolean honorarios=false;
			boolean flete=false;
			for(GCompraDet cdet:compra.getPartidas()){
				if(cdet.getRetencion1()==10){
					honorarios=true;
				}else if(cdet.getRetencion1()==4){
					flete=true;
				}
			}
			
			String cuentaDebe="";
			String cuentaHaber="";
			
			if(!honorarios && !flete){
				return;
			}else{
				if(honorarios){
					cuentaDebe="208-0002-000";
					cuentaHaber="205-0003-000";
				}
				else{
					cuentaDebe="208-0001-000";
					cuentaHaber="117-0006-005";
				}
					
			}
			
			AsientoContable debe=new AsientoContable();
			String pattern="PROV F:{0} {1}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			debe.setDescripcion(descripcion);
			debe.setCuenta(cuentaDebe);
			CantidadMonetaria monto=det.getRetencion1();
			if(monto.amount().doubleValue()!=0){
				debe.setDebe(monto);
				if(pago.getSucursal()!=null)
					debe.setSucursal(pago.getSucursal().getNombre());
				else
					debe.setSucursal("NA");
				debe.setConcepto("IVA Retenido");
				String pat="{0} {1}";
				debe.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
				debe.setDescripcion3(det.getDocumento());
				p.getRegistros().add(debe);
			}
			
			AsientoContable haber=new AsientoContable();
			haber.setDescripcion(descripcion);
			haber.setCuenta(cuentaHaber);
			monto=det.getRetencion1();
			if(monto.amount().doubleValue()!=0){
				haber.setHaber(monto);
				if(pago.getSucursal()!=null)
					haber.setSucursal(pago.getSucursal().getNombre());
				else
					haber.setSucursal("NA");
				haber.setConcepto("IVA Retenido");
				String pat="{0} {1}";
				haber.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
				haber.setDescripcion3(det.getDocumento());
				p.getRegistros().add(haber);
			}
		}
	}
	
}
