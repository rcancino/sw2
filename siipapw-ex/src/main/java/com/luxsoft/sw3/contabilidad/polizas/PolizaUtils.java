package com.luxsoft.sw3.contabilidad.polizas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.gastos.GFacturaPorCompra;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.model.tesoreria.RequisicionDe;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;

import freemarker.core.ReturnInstruction.Return;

public class PolizaUtils {
	
	
	public static final BigDecimal calcularImpuesto(BigDecimal importe){
		return importe.multiply(MonedasUtils.IVA);
	}
	public static  final BigDecimal calcularTotal(BigDecimal importe){
		return importe.add(calcularImpuesto(importe));
	}
	public static  final BigDecimal calcularImporteDelTotal(BigDecimal total){
		return calcularImporteDelTotal(total, 4);
	}
	
	public static final BigDecimal calcularImporteDelTotal(BigDecimal total,int decimales){
		BigDecimal val=BigDecimal.valueOf(1).add(MonedasUtils.IVA);
		BigDecimal importe=total.divide(val,decimales,RoundingMode.HALF_EVEN);
		return importe;
	}
	
	public static final BigDecimal redondear(BigDecimal valor){
		return CantidadMonetaria.pesos(valor).amount();
	}
	
	/**
	 * Determina si una requisicion es provisionable
	 * 
	 * @param r
	 * @return
	 */
	public static boolean esProvisionable(Requisicion r){
		boolean res=false;
		final Date fechaPago=r.getPago().getFecha();
		for(RequisicionDe det:r.getPartidas()){
			GFacturaPorCompra factura=det.getFacturaDeGasto();
			if(factura==null){
				return false;
			}
			final Date fechaFactura=factura.getFecha();
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				res=true;
			}
		}
		return res;
	}
	
	public static boolean esProvisionableT(Requisicion r){
		boolean res=false;
		final Date fechaPago=r.getPago().getFecha();
		for(RequisicionDe det:r.getPartidas()){
			String documento=det.getDocumento();
			if(documento==null){
				return false;
			}
			final Date fechaFactura=r.getFecha();
			if(!DateUtil.isSameMonth(fechaPago, fechaFactura)){
				res=true;
			}
		}
		return res;
	}
	
	public static Map<String,Sucursal> sucursales;
	
	public static String getSucursalId(String nombre){
		if(sucursales==null){
			sucursales=new HashMap<String, Sucursal>();
			List<Sucursal> res=ServiceLocator2.getHibernateTemplate().find("from Sucursal");
			for(Sucursal s:res){
				sucursales.put(s.getNombre(), s);
			}
		}
		Sucursal sucursal=sucursales.get(nombre);
		if(sucursal!=null){
			return sucursal.getId().toString();				
		}
		return "";
	}

}
