package com.luxsoft.siipap.cxp.ui.consultas;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.AnalisisDeFacturaDet;

import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.cxp.model.CXPNota;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Genera la poliza de diario para el amancen
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeAlmacenModel2 {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private DateFormat df=new SimpleDateFormat("dd-MM-yy");
	
	private String proveedor;
	
	
	
	/**
	 * Genera las polizas  para el periodo indicado 
	 * 
	 * @param p
	 * @return
	 */
	public List<Poliza> generarPoliza(final Periodo p){
		if(!StringUtils.isBlank(getProveedor()))
			return generarPorProveedor(p);
		List<Poliza> polizas=new ArrayList<Poliza>();
		for(Date dia:p.getListaDeDias()){
			try {
				List<Poliza> res=generarPoliza(dia);
				polizas.addAll(res);
			} catch (Exception e) {
				logger.error("No genero la poliza para el dia: "+dia+ " \nMsg: "+ExceptionUtils.getRootCauseMessage(e)
						,e);
				e.printStackTrace();
			}
		}
		return polizas;
	}
	
	protected List<Poliza> generarPorProveedor(final Periodo p){
		List<Poliza> polizas=new ArrayList<Poliza>();
		
		String hql="from AnalisisDeFacturaDet d where d.analisis.factura.clave=? and d.entrada.fecha between ? and ? " +
		" order by d.analisis.factura.proveedor.clave, d.analisis.factura.documento";
		
		final EventList<AnalisisDeFacturaDet> entradas=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate()
				.find(hql,new Object[]{ getProveedor(),p.getFechaInicial(),p.getFechaFinal()}));
		logger.info(MessageFormat.format("Entradas analizadas para el proveedor {0} periodo {1} : {2} ", p,getProveedor(),entradas.size()));
	
		Date dia=p.getFechaFinal();
		//System.out.println("Entradas del dia: "+entradas.size());
		//final Comparator<CXPAnalisisDet> c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "entrada.sucursal.id");
		//GroupingList<CXPAnalisisDet> entradasPorSucursal=new GroupingList<CXPAnalisisDet>(entradasTodas,c);
		//EventList<CXPAnalisisDet> entradas=GlazedLists.eventList(entradasTodas);
		final Poliza pol=new Poliza();
		pol.setConcepto("Entradas de Almacen "+getProveedor());
		pol.setFecha(dia);
		pol.setFolio(0);
		pol.setTipo("Al");
		final Sucursal sucursal=entradas.get(0).getEntrada().getSucursal();
		pol.setSucursalNombre(sucursal.getNombre());
		pol.setSucursalId(sucursal.getClave());
		cargoAlmacen(entradas,pol);
		abonoProveedor(entradas,pol);
		cargoFletes(entradas, pol);
		provision(entradas, pol);
		pol.depurar();
		polizas.add(pol);
		
		//cancelacionProvision(pol);
		//descuentos(pol);
		return polizas;

	}
	
	public List<Poliza> generarPoliza(final Date dia){
		
		List<Poliza> polizas=new ArrayList<Poliza>();
		
		
		String hql="from AnalisisDeFacturaDet d where date(d.entrada.fecha)=? and d.analisis.factura.clave<>\'I001\'" +
		" order by d.analisis.factura.proveedor.clave, d.analisis.factura.documento";
		
		final EventList entradasTodas=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate().find(hql, dia));
		logger.info(MessageFormat.format("Entradas analizadas para el dia {0,date,short}  :{1}", dia,entradasTodas.size()));
		
		//System.out.println("Entradas del dia: "+entradas.size());
		final Comparator<AnalisisDeFacturaDet> c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "entrada.sucursal.id");
		GroupingList<AnalisisDeFacturaDet> entradasPorSucursal=new GroupingList<AnalisisDeFacturaDet>(entradasTodas,c);
		for(List<AnalisisDeFacturaDet> grupo:entradasPorSucursal){
			EventList<AnalisisDeFacturaDet> entradas=GlazedLists.eventList(grupo);
			final Poliza pol=new Poliza();
			pol.setConcepto("Entradas de Almacen "+df.format(dia));
			pol.setFecha(dia);
			pol.setFolio(0);
			pol.setTipo("Al");
			final Sucursal sucursal=entradas.get(0).getEntrada().getSucursal();
			pol.setSucursalNombre(sucursal.getNombre());
			pol.setSucursalId(sucursal.getClave());
			cargoAlmacen(entradas,pol);
			abonoProveedor(entradas,pol);  
			cargoFletes(entradas, pol);
			provision(entradas, pol);
			pol.depurar();
			polizas.add(pol);
		}
		
		//cancelacionProvision(pol);
		//descuentos(pol);
		return polizas;
	}
	
	
	private void cargoAlmacen(final EventList<AnalisisDeFacturaDet> entradas,final Poliza pol){		
		
		final Comparator<AnalisisDeFacturaDet> c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "entrada.sucursal.id","analisis.factura.id");
		GroupingList<AnalisisDeFacturaDet> grupos=new GroupingList<AnalisisDeFacturaDet>(entradas,c);
		for(List<AnalisisDeFacturaDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getAnalisis().getFactura();
			Sucursal suc=grupo.get(0).getEntrada().getSucursal();
			logger.info("Generando asiento con "+grupo.size()+ " entradas "+ "para sucursal: "+suc.getNombre());
			CantidadMonetaria importe=factura.getImporteMN();
			
			
			AsientoContable cargo=new AsientoContable();
			cargo.setAgrupador("A");
			cargo.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			cargo.setDebe(importe);
			String cuenta="119-0"+suc.getClaveContableAsString()+"-"+factura.getProveedor().getClaveEspceialConta();
			/*
			String cuentaContable=factura.getProveedor().getCuentaContable();
			if(!StringUtils.isBlank(cuenta)){
				String s1=StringUtils.substring(cuentaContable, 4,5);
				String s2=StringUtils.substring(cuentaContable, 6,8);
				cuenta=cuenta+"-"+s1+s2;
			}*/
			cargo.setCuenta(cuenta);
			cargo.setSucursal(suc.getNombre());
			cargo.setTipo("D");
			cargo.setDescripcion(suc.getNombre());
			cargo.setDescripcion2(factura.getNombre());
			cargo.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargo);
			
			AsientoContable cargoIva=new AsientoContable();
			cargoIva.setAgrupador("A");
			cargoIva.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			cargoIva.setDebe(importe.multiply(.15));
			cargoIva.setCuenta("117-0006-001");
			cargoIva.setSucursal(suc.getNombre());
			cargoIva.setTipo("D");
			cargoIva.setDescripcion(suc.getNombre());
			cargoIva.setDescripcion2(factura.getNombre());
			cargoIva.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargoIva);
					
		}
		
	}
	
	private void abonoProveedor(final EventList<AnalisisDeFacturaDet> entradas,final Poliza pol){
		final Comparator<AnalisisDeFacturaDet> c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "analisis.factura.id");
		GroupingList<AnalisisDeFacturaDet> grupos=new GroupingList<AnalisisDeFacturaDet>(entradas,c);
		for(List<AnalisisDeFacturaDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getAnalisis().getFactura();
			logger.info("Generando asiento con "+grupo.size()+ " facturas ");
			
			AsientoContable abono=new AsientoContable();
			abono.setAgrupador("A");
			abono.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			abono.setHaber(factura.getTotalMN());
			abono.setCuenta(factura.getProveedor().getCuentaContable());
			abono.setTipo("H");
			abono.setDescripcion("");
			abono.setDescripcion2(factura.getNombre());
			abono.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(abono);
			
			AsientoContable retencion=new AsientoContable();
			retencion.setAgrupador("A");
			retencion.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			retencion.setHaber(factura.getRetencionFleteMN());
			retencion.setCuenta("208-001-000");
			retencion.setTipo("H");
			retencion.setDescripcion("");
			retencion.setDescripcion2(factura.getNombre());
			retencion.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(retencion);
					
		}
	}
	
	private void cargoFletes(final EventList entradas,final Poliza pol){
		final Comparator<AnalisisDeFacturaDet> c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "analisis.factura.id");
		GroupingList<AnalisisDeFacturaDet> grupos=new GroupingList<AnalisisDeFacturaDet>(entradas,c);
		for(List<AnalisisDeFacturaDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getAnalisis().getFactura();
			Sucursal suc=grupo.get(0).getEntrada().getSucursal();
			logger.info("Generando flete para "+grupo.size()+ " facturas ");
			
			AsientoContable flete=new AsientoContable();
			flete.setAgrupador("A");
			flete.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			flete.setDebe(factura.getFleteMN());
			flete.setCuenta("119-0"+suc.getClaveContableAsString()+"-012");
			flete.setTipo("D");
			flete.setDescripcion(suc.getNombre());
			flete.setDescripcion2(factura.getNombre());
			flete.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(flete);					
			
			AsientoContable fleteIva=new AsientoContable();
			fleteIva.setAgrupador("A");
			fleteIva.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			fleteIva.setDebe(factura.getImpuestoFleteMN().subtract(factura.getRetencionFleteMN()));
			fleteIva.setCuenta("117-0006-001");
			fleteIva.setTipo("D");
			fleteIva.setDescripcion(suc.getNombre());
			fleteIva.setDescripcion2(factura.getNombre());
			fleteIva.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(fleteIva);
			
			AsientoContable fleteRet=new AsientoContable();
			fleteRet.setAgrupador("A");
			fleteRet.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			fleteRet.setDebe(factura.getRetencionFleteMN());
			fleteRet.setCuenta("117-0003-005");
			fleteRet.setTipo("D");
			fleteRet.setDescripcion(suc.getNombre());
			fleteRet.setDescripcion2(factura.getNombre());
			fleteRet.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(fleteRet);
		}
	}
	
	private void provision(final EventList entradas,final Poliza pol){
		
		final Comparator<AnalisisDeFacturaDet> c=GlazedLists.beanPropertyComparator(AnalisisDeFacturaDet.class, "analisis.factura.id");
		GroupingList<AnalisisDeFacturaDet> grupos=new GroupingList<AnalisisDeFacturaDet>(entradas,c);
		for(List<AnalisisDeFacturaDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getAnalisis().getFactura();
			Sucursal suc=grupo.get(0).getEntrada().getSucursal();
			logger.info("Generando asiento con "+grupo.size()+ " facturas ");
			
			AsientoContable cargoAProvision=new AsientoContable();
			cargoAProvision.setAgrupador("P");
			cargoAProvision.setConcepto("ProvF"+factura.getDocumento()+" "+factura.getProveedor().getNombreRazon());
			CantidadMonetaria provision=factura.getImporteMN();
			provision=provision.subtract(factura.getImporteAnalisisMN());
			provision=provision.multiply(-1);
			cargoAProvision.setDebe(provision);
			cargoAProvision.setCuenta("119-0"+suc.getClaveContableAsString()+"-004");
			cargoAProvision.setTipo("D");
			cargoAProvision.setDescripcion(suc.getNombre());
			cargoAProvision.setDescripcion2(factura.getNombre());
			cargoAProvision.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargoAProvision);
			
			AsientoContable cargoIva=new AsientoContable();
			cargoIva.setAgrupador("P");
			cargoIva.setConcepto("ProvF"+factura.getDocumento()+" "+factura.getProveedor().getNombreRazon());
			cargoIva.setDebe(provision.multiply(.15));
			cargoIva.setCuenta("117-0006-001");
			cargoIva.setTipo("D");
			cargoIva.setDescripcion(suc.getNombre());
			cargoIva.setDescripcion2(factura.getNombre());
			cargoIva.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargoIva);
			
			AsientoContable abonoProveedor=new AsientoContable();
			abonoProveedor.setAgrupador("P");
			abonoProveedor.setConcepto("ProvF"+factura.getDocumento()+" "+factura.getProveedor().getNombreRazon());
			abonoProveedor.setHaber(provision.multiply(1.15));
			abonoProveedor.setCuenta(factura.getProveedor().getCuentaContable());
			abonoProveedor.setTipo("H");
			abonoProveedor.setDescripcion("");
			abonoProveedor.setDescripcion2(factura.getNombre());
			abonoProveedor.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(abonoProveedor);
			
					
		}
	}
	
	private void cancelacionProvision(final Poliza pol){
		String hql="from CXPNota n where n.fecha=?";
		List<CXPNota> notas=ServiceLocator2.getHibernateTemplate().find(hql, pol.getFecha());
		for(CXPNota nota:notas){
			AsientoContable cancelacionProvision=new AsientoContable();
			
			
			cancelacionProvision.setAgrupador("B");
			cancelacionProvision.setConcepto("CanProv"+nota.getDocumento()+ " "+nota.getNombre());
			cancelacionProvision.setCuenta("119-0001-004");
			cancelacionProvision.setDebe(nota.getImporteMN());
			
			cancelacionProvision.setTipo("D");
			cancelacionProvision.setDescripcion("");
			cancelacionProvision.setDescripcion2(nota.getNombre());
			cancelacionProvision.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(cancelacionProvision);
			
			
			AsientoContable ivaCancelacionProvision=new AsientoContable();			
			ivaCancelacionProvision.setAgrupador("B");
			ivaCancelacionProvision.setConcepto("IvaCanProv "+nota.getDocumento()+ " "+nota.getNombre());
			ivaCancelacionProvision.setCuenta("117-0006-001");
			ivaCancelacionProvision.setDebe(nota.getImporteMN().multiply(.15));			
			ivaCancelacionProvision.setTipo("D");
			ivaCancelacionProvision.setDescripcion("");
			ivaCancelacionProvision.setDescripcion2(nota.getNombre());
			ivaCancelacionProvision.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(ivaCancelacionProvision);
			
			AsientoContable abonoProveedor=new AsientoContable();			
			abonoProveedor.setAgrupador("B");
			abonoProveedor.setConcepto("N"+nota.getDocumento()+ " "+nota.getNombre());
			abonoProveedor.setCuenta("201-"+nota.getClave()+"-000");
			abonoProveedor.setHaber(nota.getImporteMN().multiply(1.15));			
			abonoProveedor.setTipo("H");
			abonoProveedor.setDescripcion("");
			abonoProveedor.setDescripcion2(nota.getNombre());
			abonoProveedor.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(abonoProveedor);
			
		}
	}
	
	private void descuentos(final Poliza pol){
		String hql="from CXPNota n where n.fecha=? where n.concepto in (\'DEVOLUCION\',\'DESCUENTO\')";
		List<CXPNota> notas=ServiceLocator2.getHibernateTemplate().find(hql, pol.getFecha());
		for(CXPNota nota:notas){
			AsientoContable cancelacionProvision=new AsientoContable();
			
			
			cancelacionProvision.setAgrupador("D");
			cancelacionProvision.setConcepto("Desc"+nota.getDocumento()+ " "+nota.getNombre());
			cancelacionProvision.setCuenta("119-0001-003");
			cancelacionProvision.setDebe(nota.getImporteMN().multiply(-1));
			
			cancelacionProvision.setTipo("D");
			cancelacionProvision.setDescripcion("");
			cancelacionProvision.setDescripcion2(nota.getNombre());
			cancelacionProvision.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(cancelacionProvision);
			
			
			AsientoContable ivaCancelacionProvision=new AsientoContable();			
			ivaCancelacionProvision.setAgrupador("D");
			ivaCancelacionProvision.setConcepto("IvaDesc"+nota.getDocumento()+ " "+nota.getNombre());
			ivaCancelacionProvision.setCuenta("117-0001-002");
			ivaCancelacionProvision.setDebe(nota.getImporteMN().multiply(-0.15));			
			ivaCancelacionProvision.setTipo("D");
			ivaCancelacionProvision.setDescripcion("");
			ivaCancelacionProvision.setDescripcion2(nota.getNombre());
			ivaCancelacionProvision.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(ivaCancelacionProvision);
			
			AsientoContable cargoProveedor=new AsientoContable();			
			cargoProveedor.setAgrupador("D");
			cargoProveedor.setConcepto("N"+nota.getDocumento()+ " "+nota.getNombre());
			cargoProveedor.setCuenta(nota.getProveedor().getCuentaContable());
			cargoProveedor.setDebe(nota.getImporteMN().multiply(1.15));			
			cargoProveedor.setTipo("D");
			cargoProveedor.setDescripcion("");
			cargoProveedor.setDescripcion2(nota.getNombre());
			cargoProveedor.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(cargoProveedor);
			
			AsientoContable ivaAcreditable=new AsientoContable();			
			ivaAcreditable.setAgrupador("D");
			ivaAcreditable.setConcepto("IvaAcred"+nota.getDocumento()+ " "+nota.getNombre());
			ivaAcreditable.setCuenta("117-0001-001");
			ivaAcreditable.setDebe(nota.getImporteMN().multiply(0.15));			
			ivaAcreditable.setTipo("D");
			ivaAcreditable.setDescripcion("");
			ivaAcreditable.setDescripcion2(nota.getNombre());
			ivaAcreditable.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(ivaAcreditable);
			
			AsientoContable ivaPorAcreditar=new AsientoContable();			
			ivaPorAcreditar.setAgrupador("D");
			ivaPorAcreditar.setConcepto("IvaXAcred"+nota.getDocumento()+ " "+nota.getNombre());
			ivaPorAcreditar.setCuenta("117-0006-001");
			ivaPorAcreditar.setHaber(nota.getImporteMN().multiply(0.15));			
			ivaPorAcreditar.setTipo("H");
			ivaPorAcreditar.setDescripcion("");
			ivaPorAcreditar.setDescripcion2(nota.getNombre());
			ivaPorAcreditar.setDescripcion3(nota.getDocumento());
			pol.agregarAsiento(ivaPorAcreditar);
			
		}
	}
	
	
	
	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public static void main(String[] args) {
		PolizaDeAlmacenModel2 model=new PolizaDeAlmacenModel2();
		model.generarPoliza(DateUtil.toDate("27/02/2010"));
		
	}

}
