package com.luxsoft.siipap.cxp.ui.consultas;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.GroupingList;

import com.luxsoft.siipap.cxp.model.CXPAnalisisDet;
import com.luxsoft.siipap.cxp.model.CXPFactura;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.contabilidad.AsientoContable;
import com.luxsoft.siipap.model.contabilidad.Poliza;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * Genera la poliza de diario para el amancen
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class PolizaDeAlmacenPorProveedorModel_bak {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private DateFormat df=new SimpleDateFormat("dd-MM-yy");
	
	private String proveedor;
	
	
	
	protected List<Poliza> generarPoliza(final Periodo p){
		List<Poliza> polizas=new ArrayList<Poliza>();
		
		String hql="from CXPAnalisisDet d where d.factura.clave=? and d.entrada.fecha between ? and ? " +
		" order by d.factura.proveedor.clave, d.factura.documento";
		
		final EventList<CXPAnalisisDet> entradas=GlazedLists.eventList(ServiceLocator2.getHibernateTemplate()
				.find(hql,new Object[]{ getProveedor(),p.getFechaInicial(),p.getFechaFinal()}));
		logger.info(MessageFormat.format("Entradas analizadas para el proveedor {0} periodo {1} : {2} ", p,getProveedor(),entradas.size()));
	
		Date dia=p.getFechaFinal();
		final Poliza pol=new Poliza();
		pol.setConcepto("Entradas de Almacen "+getProveedor());
		pol.setFecha(dia);
		pol.setFolio(0);
		pol.setTipo("Al");
		cargoAlmacen(entradas,pol);
		abonoProveedor(entradas,pol);
		cargoFletes(entradas, pol);
		provision(entradas, pol);
		pol.depurar();
		polizas.add(pol);
		return polizas;
	}
	
	private void cargoAlmacen(final EventList<CXPAnalisisDet> entradas,final Poliza pol){		
		
		Comparator<CXPAnalisisDet> c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "entrada.sucursal.id","factura.id");
		GroupingList<CXPAnalisisDet> grupos=new GroupingList<CXPAnalisisDet>(entradas,c);
		for(List<CXPAnalisisDet> grupo:grupos){
			
			CXPFactura factura=grupo.get(0).getFactura();
			Sucursal suc=grupo.get(0).getEntrada().getSucursal();
			logger.info("Generando asiento con "+grupo.size()+ " entradas "+ "para sucursal: "+suc.getNombre());
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			for(CXPAnalisisDet det:grupo){
				importe=importe.add(det.getImporteNetoCalculadoMN());
			}
			
			AsientoContable cargo=new AsientoContable();
			cargo.setAgrupador("A");
			cargo.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			cargo.setDebe(importe);
			cargo.setCuenta("119-0"+suc.getClaveContableAsString()+"-002");
			cargo.setSucursal(suc.getNombre());
			cargo.setTipo("D");
			cargo.setDescripcion(suc.getNombre());
			cargo.setDescripcion2(factura.getNombre());
			cargo.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargo);
			
			/*AsientoContable cargoIva=new AsientoContable();
			cargoIva.setAgrupador("A");
			cargoIva.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			cargoIva.setDebe(importe.multiply(.15));
			cargoIva.setCuenta("117-0003-001");
			cargoIva.setSucursal(suc.getNombre());
			cargoIva.setTipo("D");
			cargoIva.setDescripcion(suc.getNombre());
			cargoIva.setDescripcion2(factura.getNombre());
			cargoIva.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargoIva);*/
					
		}
		
		c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "factura.id");
		grupos=new GroupingList<CXPAnalisisDet>(entradas,c);
		for(List<CXPAnalisisDet> grupo:grupos){
			
			CXPFactura factura=grupo.get(0).getFactura();
			Sucursal suc=grupo.get(0).getEntrada().getSucursal();
			logger.info("Generando asiento con "+grupo.size()+ " entradas "+ "para sucursal: "+suc.getNombre());
			CantidadMonetaria importe=CantidadMonetaria.pesos(0);
			for(CXPAnalisisDet det:grupo){
				importe=importe.add(det.getImporteNetoCalculadoMN());
			}
			
			AsientoContable cargoIva=new AsientoContable();
			cargoIva.setAgrupador("A");
			cargoIva.setConcepto("F"+factura.getDocumento()+" "+ df.format(factura.getFecha())+" "+factura.getNombre());
			cargoIva.setDebe(importe.multiply(.15));
			cargoIva.setCuenta("117-0003-001");
			cargoIva.setSucursal(suc.getNombre());
			cargoIva.setTipo("D");
			cargoIva.setDescripcion(suc.getNombre());
			cargoIva.setDescripcion2(factura.getNombre());
			cargoIva.setDescripcion3(factura.getDocumento());
			pol.agregarAsiento(cargoIva);
					
		}
		
		
	}
	
	private void abonoProveedor(final EventList entradas,final Poliza pol){
		final Comparator<CXPAnalisisDet> c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "factura.id");
		GroupingList<CXPAnalisisDet> grupos=new GroupingList<CXPAnalisisDet>(entradas,c);
		for(List<CXPAnalisisDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getFactura();
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
		final Comparator<CXPAnalisisDet> c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "factura.id");
		GroupingList<CXPAnalisisDet> grupos=new GroupingList<CXPAnalisisDet>(entradas,c);
		for(List<CXPAnalisisDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getFactura();
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
			fleteIva.setCuenta("117-0003-001");
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
		
		final Comparator<CXPAnalisisDet> c=GlazedLists.beanPropertyComparator(CXPAnalisisDet.class, "factura.id");
		GroupingList<CXPAnalisisDet> grupos=new GroupingList<CXPAnalisisDet>(entradas,c);
		for(List<CXPAnalisisDet> grupo:grupos){
			CXPFactura factura=grupo.get(0).getFactura();
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
			cargoIva.setCuenta("117-0003-001");
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
	
	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public static void main(String[] args) {
		PolizaDeAlmacenPorProveedorModel_bak model=new PolizaDeAlmacenPorProveedorModel_bak();
		model.generarPoliza(Periodo.getPeriodoEnUnMes(5, 2009));
		
	}

}
