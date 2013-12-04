package com.luxsoft.siipap.model.gastos;

import java.text.MessageFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.CantidadMonetaria;
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
public class GeneradorDePolizaDePagos implements GeneradorDePoliza{

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
		registrarConcepto(pago, p);
		cargoAProvision(pago, p);
		afectarBancos(pago, p);
		cargoAIva(pago, p);
		registrarRetenciones(pago, p);
		registrarFletesMensajeria(pago,p);
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
	
	private void cargoAProvision(final CargoAbono pago,final Poliza p){
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				AsientoContable a=new AsientoContable();
				String pattern="PROV F:{0} {1}";				
				String descripcion=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
				a.setDescripcion(descripcion);
				a.setCuenta("212-0006-000");
				a.setDebe(det.getTotal());
				if(pago.getSucursal()!=null)
					a.setSucursal(pago.getSucursal().getNombre());
				else
					a.setSucursal("NA");
				a.setConcepto("Provision de Gastos");
				String pat="{0} {1}";
				a.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
				a.setDescripcion3(det.getDocumento());
				p.getRegistros().add(a);
			}
		}
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
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("Iva Acreditable");
			a1.setCuenta("117-0001-003");
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
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			AsientoContable a1=new AsientoContable();
			a1.setConcepto("Iva por acreditar");
			a1.setCuenta("117-0003-003");
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
		if(pago.getId()==256424L){
			System.out.println("DEBUG");
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
				a.setCuenta("117-0001-005");
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
				a.setCuenta("117-0003-005");
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
					cuentaHaber="117-0003-005";
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
