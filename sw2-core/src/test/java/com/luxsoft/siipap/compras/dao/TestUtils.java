package com.luxsoft.siipap.compras.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.RandomUtils;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.model.core.Unidad;

public class TestUtils {
	
	/**
	 * Gemera una lista de proveedores de prueba
	 * 
	 * @param cantidad
	 * @return
	 */
	public static List<Proveedor> generarProveedoresDePrueba(int cantidad){
		List<Proveedor> data=new ArrayList<Proveedor>();
		for(int i=0;i<cantidad;i++){
			Proveedor p=new Proveedor();
			int random=RandomUtils.nextInt(8000);
			String name="POV_TEST_"+random;
			p.setNombre(name);
			p.setClave("PT"+i);
			data.add(p);
		}
		return data;
		
	}
	
	public static Set<Producto> generarProductosDePrueba(int cantidad,Unidad u){
		Set<Producto> data=new HashSet<Producto>();
		for(int i=0;i<cantidad;i++){
			Producto p=new Producto();
			int random=RandomUtils.nextInt(8000);
			String name="PRDT_"+random;
			p.setClave(name);
			p.setDescripcion("Producto de Prueba:_"+name);
			p.setUnidad(u);
			data.add(p);
		}
		return data;
	}

}
