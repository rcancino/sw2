package com.luxsoft.siipap.inventarios.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.model.TransformacionDet;


public final class TransformacionesUtils {
	
	
	public static List<TransformacionDet> convertir(final List<MovimientoDet> movs){
		validarMismoDocumento(movs);
		int size=movs.size();
		List<TransformacionDet> res=new ArrayList<TransformacionDet>(size);
		int buff=0;
		while(buff%2==0){
			if(buff>=size) break;
			
			int salidaIndex=0;
			int entradaIndex=0;
			
			if(movs.get(buff).getCantidad()<0){
				salidaIndex=buff;
				entradaIndex=buff+1;
			}else{
				salidaIndex=buff+1;
				entradaIndex=buff;
			}
			
			final MovimientoDet salida=movs.get(salidaIndex);			
			final TransformacionDet salidaTarget=new TransformacionDet();
			BeanUtils.copyProperties(salida, salidaTarget,Inventario.class);
			salidaTarget.setId(null);			
			salidaTarget.setVersion(0);
			salidaTarget.setConceptoOrigen(salida.getConcepto());
			salidaTarget.setMoviOrigen(salida);
			salidaTarget.setCostoOrigen(salida.getCostoPromedio());
			
			final MovimientoDet entrada=movs.get(entradaIndex);
			final TransformacionDet entradaTarget=new TransformacionDet();			
			BeanUtils.copyProperties(entrada, entradaTarget,Inventario.class);
			entradaTarget.setId(null);
			entradaTarget.setVersion(0);
			entradaTarget.setCostoOrigen(salida.getCostoPromedio());
			entradaTarget.actualizarCosto();
			entradaTarget.setConceptoOrigen(entrada.getConcepto());
			entradaTarget.setMoviOrigen(entrada);
			
			salidaTarget.setDestino(entradaTarget);
			entradaTarget.setOrigen(salidaTarget);
			
			res.add(salidaTarget);
			res.add(entradaTarget);
			
			buff=buff+2;
		}
		return res;
	}
	
	/**
	 * Valida que los movimientos todos sean del  mismo documento / sucursal/ tipo
	 * 
	 * @param movs
	 * @return
	 */
	public static void validarMismoDocumento(final List<MovimientoDet> movs){
		Assert.isTrue(!movs.isEmpty(),"La lista de movimientos no puede estar vacia");
		Long suc=movs.get(0).getSucursal().getId();
		Long docto=movs.get(0).getDocumento();
		String tipo=movs.get(0).getConcepto();
		for(MovimientoDet det:movs){
			Assert.isTrue(suc.equals(det.getSucursal().getId()),"Movimientos no de la misma sucursal");
			Assert.isTrue(docto.equals(det.getDocumento()),"Movimientos no del mismo documento");
			Assert.isTrue(tipo.equals(det.getConcepto()),"Movimientos no del mismo concepto");
			
		}
	}

}
