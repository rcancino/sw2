package com.luxsoft.siipap.ventas.service;

import java.math.BigDecimal;
import java.util.List;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVenta;
import com.luxsoft.siipap.ventas.model.ListaDePreciosVentaDet;
import com.luxsoft.siipap.ventas.model.TipoDeLista;

public class PreciosDeVentaSupport {
	
	public static TipoDeLista buscarTipo(){
		List<TipoDeLista> tipos=ServiceLocator2.getHibernateTemplate()
		.find("from TipoDeLista t where t.nombre=?", "GENERAL");
		if(tipos.isEmpty()){
			TipoDeLista tipo=new TipoDeLista();
			tipo.setNombre("GENERAL");
			tipo.setDescripcion("LISTA GENERAL DE PRECIOS PARA VENTA");
			return (TipoDeLista)ServiceLocator2.getUniversalDao().save(tipo);
		}else
			return tipos.get(0);
			
	}
	
	public static ListaDePreciosVenta generarListaBasica(){
		ListaDePreciosVenta lista=new ListaDePreciosVenta();		
		lista.setComentario("LISTA GENERAL DE VENTA ");
		//lista.setVigente(false);
		for(Producto p:ServiceLocator2.getProductoManager().buscarProductosActivos()){
			//System.out.println("Procesando: "+p);
			ListaDePreciosVentaDet det=generarPrecio(p);
			lista.agregarPrecio(det);			
			
		}
		return lista;
	}
	
	public static ListaDePreciosVentaDet generarPrecio(Producto p){
		ListaDePreciosVentaDet det=new ListaDePreciosVentaDet();
		
		det.setProducto(p);
		det.setProveedorClave("NA");
		det.setProveedorNombre("NO ASIGNADO");
		det.setPrecioAnterior(BigDecimal.valueOf(p.getPrecioContado()));
		det.setPrecioAnteriorCredito(BigDecimal.valueOf(p.getPrecioCredito()));
			return det;
	}
	
	public static void main(String[] args) {
		//ListaDePreciosVenta lista=PreciosDeVentaSupport.generarListaBasica();
		//ServiceLocator2.getUniversalDao().save(lista);
		buscarTipo();
	}

}
