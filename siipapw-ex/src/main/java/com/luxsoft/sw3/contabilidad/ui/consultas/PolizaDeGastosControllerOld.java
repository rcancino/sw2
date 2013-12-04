package com.luxsoft.sw3.contabilidad.ui.consultas;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;





import com.luxsoft.siipap.model.gastos.ConceptoDeGasto;
import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;
import com.luxsoft.sw3.contabilidad.model.Poliza;
import com.luxsoft.sw3.contabilidad.model.PolizaDet;
import com.luxsoft.sw3.contabilidad.services.AbstractPolizaMultipleManager;
import com.luxsoft.sw3.contabilidad.services.PolizaContableManager;

/**
 * Controlador para el mantenimiento de polizas de gastos
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeGastosControllerOld extends AbstractPolizaMultipleManager{
		
	
	protected List<Poliza> generaPoliza(final Date fecha) {
		final List<Poliza> polizas=new ArrayList<Poliza>(0);
		List<CargoAbono> pagos=buscarPagos(fecha);
		for(CargoAbono pago:pagos){
			Poliza poliza=generar(fecha, pago);
			poliza.setTipo(Poliza.Tipo.DIARIO);
			poliza.setClase("Gastos");
			poliza.actualizar();
			polizas.add(poliza);
		}
		return polizas;
	}
	
	private Poliza generar(final Date fecha,final CargoAbono pagoOrigen){
		
		final Poliza poliza=new Poliza();
		poliza.setFecha(fecha);		
		String forma=pagoOrigen.getFormaDePago().name();
		forma=StringUtils.substring(forma, 0,2);
		String pattern="{0} {1} {2}";
		String ms=MessageFormat.format(pattern, forma,pagoOrigen.getReferencia(),pagoOrigen.getAFavor());
		ms=StringUtils.substring(ms, 0,255);
		poliza.setDescripcion(ms);
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				CargoAbono pago=(CargoAbono)session.get(CargoAbono.class, pagoOrigen.getId());
				procesarGastos(poliza, pago);
				afectarBancos(pago, poliza);
				if(pago.getRequisicion().getConcepto().getId()!=737332L){
					cargoAIva(pago, poliza);
				}
				return null;
			}
		});
		
		return poliza;
		
	}
	
	private void procesarGastos(final Poliza poliza,final CargoAbono pago){
		final Date fecha=poliza.getFecha();
		List<GFacturaPorCompra> facturas=new ArrayList<GFacturaPorCompra>();
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			facturas.add(det.getFacturaDeGasto());
		}
		for(GFacturaPorCompra fac:facturas){
			if(fac==null)
				continue;
			Periodo periodo=Periodo.getPeriodoEnUnMes(fecha);
			if(periodo.isBetween(fac.getFecha())){
				generarCargoAGastosAcumulado(fac.getCompra(), poliza,fac.getDocumento(),pago);
				registrarIva(poliza, facturas);				
				registrarRetenciones(pago,poliza);
			}else{
				//Matar la provision
				matarProvision(fac, poliza);
			}
		}		
	}
	
	private void generarCargoAGastosAcumulado(final GCompra compra,final Poliza poliza,final String factura,final CargoAbono pago){		
		final EventList<GCompraDet> eventList=GlazedLists.eventList(compra.getPartidas());
		final Comparator<GCompraDet> c1=GlazedLists.beanPropertyComparator(GCompraDet.class, "rubro.id");
		final Comparator<GCompraDet> c2=GlazedLists.beanPropertyComparator(GCompraDet.class, "sucursal.clave");
		Comparator<GCompraDet>[] comps=new Comparator[]{c1,c2};
		final GroupingList groupList=new GroupingList(eventList,GlazedLists.chainComparators(Arrays.asList(comps)));
		
		CantidadMonetaria ivaAcumulado=CantidadMonetaria.pesos(0);
		CantidadMonetaria importeAcumulado=CantidadMonetaria.pesos(0);
		
		String proveedor=null;
		String sucursal=null;
		for(int index=0;index<groupList.size();index++){
			List<GCompraDet> dets=groupList.get(index);
			String asiento="GASTO";
			GCompraDet gasto=dets.get(0);
			PolizaDet cargoAGastos=poliza.agregarPartida();
			cargoAGastos.setCuenta(getCuenta("600"));
			
			
			if( (gasto.getRubro()!=null) || (gasto.getRubro().getRubroCuentaOrigen()!=null) ){
				ConceptoDeGasto concepto=gasto.getRubro().getRubroCuentaOrigen();
				String cc=concepto!=null?concepto.getDescripcion():"NA";
				cargoAGastos.setDescripcion(cc);	
			}
			
			String pattern="F-{0} {1}";
			String descripcion=MessageFormat.format(pattern, gasto.getFactura(),gasto.getProducto().getDescripcion());
			descripcion=StringUtils.substring(descripcion, 0,28);
			cargoAGastos.setDescripcion2(descripcion);
			if(proveedor==null)
				proveedor=dets.get(0).getCompra().getProveedor().getNombreRazon();
			cargoAGastos.setReferencia(proveedor);
			if(sucursal==null)
				sucursal=new String(gasto.getSucursal().getNombre());
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
		
		if(pago.getRequisicion().getConcepto()==null){
			System.out.println("Req con concepto nulo: = " + pago.getRequisicion().getId());
		}
		//if(pago.getRequisicion().getConcepto().getId()==737332L){
		if((pago.getRequisicion().getConcepto()!=null) 
				&& (pago.getRequisicion().getConcepto().getId()==737332L)){
			
			PolizaDet ivaPendiente=poliza.agregarPartida();
			ivaPendiente.setCuenta(getCuenta("117"));
			ivaPendiente.setConcepto(ivaPendiente.getCuenta().getConcepto("IVAG02"));
			//ivaPendiente.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_GASTOS);
			ivaPendiente.setDebe(ivaAcumulado.amount());
			ivaPendiente.setDescripcion2("IVA PENDIENTE DE GASTOS");
			ivaPendiente.setReferencia(proveedor);
			ivaPendiente.setReferencia2(sucursal);
			
			PolizaDet abonoAProvision=poliza.agregarPartida();
			abonoAProvision.setCuenta(getCuenta("212"));
			abonoAProvision.setConcepto(abonoAProvision.getCuenta().getConcepto("PRVG03"));
			abonoAProvision.setHaber(importeAcumulado.amount());
			abonoAProvision.setDescripcion2("Provisión de gastos");
			abonoAProvision.setReferencia(proveedor);
			abonoAProvision.setReferencia2(sucursal);
		}	
	}
	
	
	
	private void cargoAIva(final CargoAbono pago,final Poliza p){
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			PolizaDet a1=p.agregarPartida();
			a1.setDescripcion(PolizaContableManager.IVA_EN_GASTOS);
			a1.setCuenta(getCuenta("117"));
			String pattern="F:{0} {1,date,short}";				
			String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			a1.setDescripcion2(descripcion2);
			a1.setReferencia(pago.getAFavor());
			CantidadMonetaria monto=det.getImpuestoParaPolizaMN();
			Assert.isTrue(monto.getCurrency().equals(MonedasUtils.PESOS),"El monto debe ser en pseos");
			monto=monto.subtract(det.getRetencion1());
			if(monto.amount().doubleValue()!=0){
				a1.setDebe(monto.amount());
			}
			a1.setReferencia(pago.getAFavor());
			try {
				a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(det);
				a1.setReferencia2(" ERR :"+det);
				a1.setDescripcion("err...");
			}
			
		}		
		
		 //IVA de LA provision
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			Periodo periodo=Periodo.getPeriodoEnUnMes(pago.getFecha());
			if(periodo.isBetween(det.getFechaDocumento())){
				continue;
			}
			PolizaDet abonoAlIva=p.agregarPartida();
			abonoAlIva.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_GASTOS);
			abonoAlIva.setCuenta(getCuenta("117"));
			String pattern="F:{0} {1}";				
			String descripcion2=MessageFormat.format(pattern, det.getDocumento(),pago.getAFavor());
			abonoAlIva.setDescripcion2(descripcion2);
			CantidadMonetaria monto=det.getImpuestoParaPolizaMN();
			monto=monto.subtract(det.getRetencion1());
			if(monto.amount().doubleValue()!=0){
				abonoAlIva.setHaber(monto.amount());
			}
			abonoAlIva.setReferencia(pago.getAFavor());
			abonoAlIva.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
		}
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){
			if(det.getRequisicion().getConcepto()!=null && det.getRequisicion().getConcepto().getId()==737332){
				PolizaDet a1=p.agregarPartida();
				a1.setCuenta(getCuenta("117"));
				a1.setConcepto(a1.getCuenta().getConcepto("IVAG02"));
				String pattern="F:{0} {1,date,short}";				
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				a1.setDescripcion2(descripcion2);
				a1.setReferencia(pago.getAFavor());
				CantidadMonetaria monto=det.getImpuestoParaPolizaMN();
				Assert.isTrue(monto.getCurrency().equals(MonedasUtils.PESOS),"El monto debe ser en pseos");
				monto=monto.subtract(det.getRetencion1());
				if(monto.amount().doubleValue()!=0){
					a1.setHaber(monto.amount());
				}
				try {
					a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(det);
					a1.setReferencia2(" ERR :"+det);
				}
			}
		}
		
		/** Gastos**/
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			PolizaDet a1=p.agregarPartida();
			a1.setDescripcion("IETU DEDUCIBLE GASTOS");
			a1.setCuenta(getCuenta("900"));
			String pattern="F:{0} {1,date,short}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			a1.setDescripcion2(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);			
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(!prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setDebe(ietu.amount());
			a1.setReferencia(pago.getAFavor());
			a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
		}
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			PolizaDet a1=p.agregarPartida();
			a1.setDescripcion("DEDUCIBLE IETU GASTOS ");
			a1.setCuenta(getCuenta("901"));
			String pattern="F:{0} {1,date,short}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			a1.setDescripcion2(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(!prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setHaber(ietu.amount());
			a1.setReferencia(pago.getAFavor());
			a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
		}
		
		/** Activo Fijo**/
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			PolizaDet a1=p.agregarPartida();
			a1.setDescripcion("IETU DEDUCIBLE GASTOS");
			a1.setCuenta(getCuenta("900"));
			String pattern="F:{0} {1,date,short}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			a1.setDescripcion2(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setReferencia(pago.getAFavor());
			a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			a1.setDebe(ietu.amount());
		}
		
		for(RequisicionDe det:pago.getRequisicion().getPartidas()){			
			PolizaDet a1=p.agregarPartida();
			a1.setDescripcion("DEDUCIBLE IETU GASTOS ");
			a1.setCuenta(getCuenta("901"));
			String pattern="F:{0} {1,date,short}";				
			String descripcion=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
			a1.setDescripcion2(descripcion);
			if(det.getFacturaDeGasto()==null)
				continue;
			CantidadMonetaria ietu=CantidadMonetaria.pesos(0);
			for(GCompraDet prod:det.getFacturaDeGasto().getCompra().getPartidas()){
				if(prod.getProducto().getInversion()){
					ietu=ietu.add(prod.getIetu());
				}
			}
			a1.setHaber(ietu.amount());
			a1.setReferencia(pago.getAFavor());
			a1.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
		}
	}
	
	private void registrarIva(Poliza p,List<GFacturaPorCompra> facs){
		
		/**Impuesto de gastos**/
		for(GFacturaPorCompra fac:facs){
			PolizaDet a=p.agregarPartida();
			String pattern="IVA F:{0} {1}";
			String descripcion=MessageFormat.format(pattern, fac.getDocumento(),fac.getCompra().getProveedor().getNombreRazon());
			a.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_GASTOS);
			a.setReferencia(descripcion);
			a.setCuenta(getCuenta("117"));
			CantidadMonetaria impuestoGastos=CantidadMonetaria.pesos(0);
			for(GCompraDet det:fac.getCompra().getPartidas()){
				if(det.getProducto().getInversion()){
					CantidadMonetaria imp=det.getImpuestoMN();
					impuestoGastos=impuestoGastos.add(imp);
				}
			}
			a.setDebe(impuestoGastos.amount());
			a.setReferencia2(fac.getCompra().getSucursal().getNombre());
			a.setDescripcion2(fac.getCompra().getProveedor().getNombreRazon());
			
		}
	}
	
	private void registrarRetenciones(final CargoAbono pago,final Poliza p){
		if(pago.getRequisicion()!=null){
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet cargoIvaAcreditableRet=p.agregarPartida();
				
				cargoIvaAcreditableRet.setDescripcion(PolizaContableManager.IVA_ACREDITABLE_RETENIDO);
				
				String pattern="PROV F:{0} {1,date,short}";				
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargoIvaAcreditableRet.setDescripcion2(descripcion2);
				
				cargoIvaAcreditableRet.setCuenta(getCuenta("117"));
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					cargoIvaAcreditableRet.setDebe(monto.amount());
					if(pago.getSucursal()!=null)
						cargoIvaAcreditableRet.setReferencia2(pago.getSucursal().getNombre());
					else
						cargoIvaAcreditableRet.setReferencia2("NA");
					/*
					String pat="{0} {1}";
					cargoIvaAcreditableRet.setDescripcion2(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					cargoIvaAcreditableRet.setReferencia(det.getDocumento());
					*/
				}
				cargoIvaAcreditableRet.setReferencia(pago.getAFavor());
				cargoIvaAcreditableRet.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
				
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet ablnoIvaRetenido=p.agregarPartida();
				ablnoIvaRetenido.setDescripcion(PolizaContableManager.IVA_RETENIDO);
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				ablnoIvaRetenido.setDescripcion2(descripcion2);
				ablnoIvaRetenido.setCuenta(getCuenta("205"));
				CantidadMonetaria monto=det.getRetencion1();
				if(monto.amount().doubleValue()!=0){
					ablnoIvaRetenido.setHaber(monto.amount());
					if(pago.getSucursal()!=null)
						ablnoIvaRetenido.setReferencia2(pago.getSucursal().getNombre());
					else
						ablnoIvaRetenido.setReferencia2("NA");					
					//String pat="{0} {1}";
					//ablnoIvaRetenido.setReferencia(MessageFormat.format(pat, pago.getOrigen().name(),pago.getComentario()));
					//ablnoIvaRetenido.setReferencia(det.getDocumento());
				}
				ablnoIvaRetenido.setReferencia(pago.getAFavor());
				ablnoIvaRetenido.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			}
			/*********************************************************************/ 
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet cargoIvaRetenidoPendiente=p.agregarPartida();
				cargoIvaRetenidoPendiente.setDescripcion(PolizaContableManager.IVA_RETENIDO_PENDIENTE);
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				cargoIvaRetenidoPendiente.setDescripcion2(descripcion2);
				cargoIvaRetenidoPendiente.setCuenta(getCuenta("117"));
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					cargoIvaRetenidoPendiente.setDebe(monto.amount());
					if(pago.getSucursal()!=null)
						cargoIvaRetenidoPendiente.setReferencia2(pago.getSucursal().getNombre());
					else
						cargoIvaRetenidoPendiente.setReferencia2("NA");
				}
				cargoIvaRetenidoPendiente.setReferencia(pago.getAFavor());
				cargoIvaRetenidoPendiente.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			}
			for(RequisicionDe det:pago.getRequisicion().getPartidas()){				
				PolizaDet a=p.agregarPartida();				
				a.setCuenta(getCuenta("117"));
				a.setDescripcion(PolizaContableManager.IVA_POR_ACREDITAR_RETENIDO);
				String pattern="PROV F:{0} {1,date,short}";			
				String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
				a.setDescripcion2(descripcion2);
				CantidadMonetaria monto=det.getRetencion2();
				if(monto.amount().doubleValue()!=0){
					a.setHaber(monto.amount());
					if(pago.getSucursal()!=null)
						a.setReferencia2(pago.getSucursal().getNombre());
					else
						a.setReferencia2("NA");
				}
				a.setReferencia(pago.getAFavor());
				a.setReferencia2(det.getFacturaDeGasto().getCompra().getSucursal().getNombre());
			}
		}
	}
	
	/**
	 *
	 * 
	 * @param pago
	 * @param p
	 */
	private void matarProvision(final GFacturaPorCompra factura,final Poliza p){
		
		PolizaDet a=p.agregarPartida();
		a.setCuenta(getCuenta("212"));
		a.setDebe(factura.getTotalMN().amount());
		a.setDescripcion("PROV REEMBOLSO DE GASTOS DEDUCIBLES");
		String pattern="PROV F:{0} {1,date,short}";				
		final String descripcion2=MessageFormat.format(pattern, factura.getDocumento(),factura.getFecha());
		a.setDescripcion2(descripcion2);
		a.setReferencia(factura.getCompra().getProveedor().getNombreRazon());
		a.setReferencia2(factura.getCompra().getSucursal().getNombre());
	}
	
	private void afectarBancos(final CargoAbono pago,final Poliza p){
		PolizaDet a1=p.agregarPartida();
		CuentaContable cuenta=getCuenta("102");
		a1.setCuenta(cuenta);
		a1.setDescripcion(cuenta.getDescripcion());
		String c=MessageFormat.format("{0} {1}", pago.getCuenta().getDescripcion(),pago.getCuenta().getNumero());
		c=StringUtils.substring(c, 0,50);
		a1.setDescripcion2(c);
		a1.setHaber(pago.getImporteMN().abs().amount());
		a1.setReferencia(pago.getAFavor());
		a1.setReferencia2("OFICINAS");
		if(pago.getRequisicion().getConcepto()!=null){
			if(pago.getRequisicion().getConcepto().getId()==201136L){
				for(RequisicionDe det:pago.getRequisicion().getPartidas()){
					PolizaDet cargo=p.agregarPartida();
					cargo.setCuenta(getCuenta("111"));
					cargo.setDescripcion("ANTICIPOS PROVEEDORES GASTOS");
					String pattern="ANTICIPO F:{0} {1,date,short}";			
					String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
					cargo.setDescripcion2(descripcion2);
					cargo.setReferencia(pago.getAFavor());
					cargo.setReferencia2("OFICINAS");
					cargo.setDebe(det.getTotalMN().amount());
				}
			}
			if(pago.getRequisicion().getConcepto().getId()==737332L){
				for(RequisicionDe det:pago.getRequisicion().getPartidas()){
					PolizaDet cargo=p.agregarPartida();
					cargo.setCuenta(getCuenta("212"));
					//cargo.setDescripcion("ANTICIPOS PROVEEDORES GASTOS");
					cargo.setConcepto(cargo.getCuenta().getConcepto("PRVG03"));
					String pattern="PAGO PARCIAL F:{0} {1,date,short}";			
					String descripcion2=MessageFormat.format(pattern, det.getDocumento(),det.getFechaDocumento());
					cargo.setDescripcion2(descripcion2);
					cargo.setReferencia(pago.getAFavor());
					cargo.setReferencia2("OFICINAS");
					cargo.setDebe(det.getTotalMN().amount());
				}
			}
		}
	}
	
	private List<CargoAbono> buscarPagos(final Date fecha){
		return getHibernateTemplate().executeFind(new HibernateCallback(){
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				List<CargoAbono> pagos= session.createQuery("from CargoAbono c where c.fecha=? " +
						"and c.importe<0 " +
						"and c.requisicion is not null " +
						"and c.requisicion.origen=? ")
				.setParameter(0, fecha,Hibernate.DATE)
				.setString(1, Requisicion.GASTOS)
				.list();				
				return pagos;
			}});
	}
		
}


