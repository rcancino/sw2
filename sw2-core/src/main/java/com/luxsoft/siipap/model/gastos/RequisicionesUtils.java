package com.luxsoft.siipap.model.gastos;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.util.MonedasUtils;

/**
 * Utilerias relacionadas con la generación y mantenimiento de
 * de Requisiciones a tesoreria
 * 
 * @author Ruben Cancino
 *
 */
public class RequisicionesUtils {
	
	
	
	public static Requisicion generarRequisicion(GFacturaPorCompra...facs){
		return generarRequisicion(Arrays.asList(facs));
	}
	
	/**
	 * Genera una Requisicion de pago para las facturas indicadas. Deben ser del mismo proveedor
	 * 
	 * 
	 * @param facturas
	 * @return
	 */
	public static Requisicion generarRequisicion(Collection<GFacturaPorCompra> facturas){
		validarMismoProveedor(facturas);
		GFacturaPorCompra fac=facturas.iterator().next();
		Requisicion r=new Requisicion();
		r.setAfavor(fac.getProveedor());
		r.setFecha(new Date());
		r.setFechaDePago(fac.getVencimiento());		
		r.setMoneda(MonedasUtils.PESOS);
		r.setOrigen(Requisicion.GASTOS);
		r.setRfc(fac.getRfc());
		r.setTipoDeCambio(BigDecimal.ONE);
		fac.getCompra().calcularVencimiento();
		fac.setVencimiento(fac.getVencimiento());
		for(GFacturaPorCompra f:facturas){
			RequisicionDe det=new RequisicionDe();
			det.setFacturaDeGasto(f);
			det.setDepartamento(f.getCompra().getDepartamento());
			det.setDocumento(f.getDocumento());
			det.setFechaDocumento(f.getFecha());
			det.setSucursal(f.getCompra().getSucursal());
			det.setTotal(f.getPorRequisitar());
			r.agregarPartida(det);
			det.actualizarImportesDeGastosProrrateado();
			//f.setRequisiciondet(det);
			
			f.agregarRequisicion(det);			
			//det.setFactura(f);
			
		}
		r.actualizarTotal();
		
		return r;
	}
	
	/**
	public static CantidadMonetaria calcularMontoProrrateado(final GFacturaPorCompra fac,CantidadMonetaria valor){
		double pro=prorrateo(fac);
		CantidadMonetaria res=valor.multiply(pro);
		return res;
	}
	
	
	public static double prorrateo(GFacturaPorCompra factura){
		BigDecimal total=factura.getCompra().getTotal();
		if((total==null) || (total.equals(BigDecimal.ZERO)))
			return 1;
		double val=factura.getTotal().amount().doubleValue()/total.doubleValue();
		return val;
	}
	
	public static void actualizarImportes(final RequisicionDe det,final GFacturaPorCompra f){
		det.setImporte(calcularMontoProrrateado(f, f.getImporte()));
		det.setImpuesto(calcularMontoProrrateado(f, f.getImpuesto()));
	}
	**/
	
	private static void validarMismoProveedor(Collection<GFacturaPorCompra> facturas){
		if(facturas.isEmpty())
			throw new RuntimeException("La lista de facturas esta vacia");
		String prov=facturas.iterator().next().getProveedor();
		for(GFacturaPorCompra f:facturas){
			if(!f.getProveedor().equals(prov)){
				String msg="La factura {0} no corresponde al proveedor \n{1} corresponde a:\n{2}";
				throw new RuntimeException(MessageFormat.format(msg, f.getDocumento(),prov,f.getProveedor()));
			}
				
		}
		
	}
	
	 

}
