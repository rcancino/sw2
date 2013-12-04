package com.luxsoft.siipap.replica;

import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Producto;


public class ExportadorDeProductos implements ReplicaExporter{
	
	private Logger logger=Logger.getLogger(getClass());
	
	public Class getBeanClass() {
		return Producto.class;
	}
	
	public String export(Collection beans, Tipo tipo) {
		throw new UnsupportedOperationException("La exportacion de productos no se puede hacer en bulk");
	}

	public String export(Object bean, Tipo tipo) {
		switch (tipo) {
		case A:
		case C:
			return generarAltaCambio((Producto)bean,tipo);
		case B:
			//return generarBaja((Producto)bean);
			return generarAltaCambio((Producto)bean,tipo);
		default:
			return null;
		}
		
	}
	
	private String generarAltaCambio(Producto p,Tipo tipo){
		StringBuffer buf = new StringBuffer();
		char SEP = '[';
		
		// Seccion 1 Archivo
		buf.append("ARTICULO.DBF");
		buf.append(SEP);
		
		// Seccion 2 TIPO de operacion
		buf.append(tipo);
		buf.append(SEP);

		// Seccion 3 Llave del indice 1
		String prod=StringUtils.rightPad(p.getClave(), 10, ' ');
		buf.append(prod);
		buf.append(SEP);

		// Seccion 4 Indices
		buf.append("ARTCLAVE.NTX|ARTALFAB.NTX|ARTFAMIL.NTX|ARTHPCVE.NTX|ARTHPNOM.NTX");
		buf.append(SEP);

		// Seccion 5 Induce activo o de uso
		buf.append("1");
		buf.append(SEP);

		// Seccion 6 Campos a afectar
		String[] campos={				
				"ARTCLAVE"
				,"ARTNOMBR"
				,"ARTKILOS"
				,"ARTGRAMS"
				,"ARTPRECI"
				,"ARTPRECRE"
				,"ARTFACNETO"
				,"ARTUNIDAD"
				,"ARTCODORIG"
				,"ARTESTADO"
				,"ARTOPEVEN"
				,"ARTOPEVECO"
				,"ARTOPEINV"
				,"ARTOPEINCO"
				,"ARTOPECOM"
				,"ARTOPECOCO"
				,"ARTCLASIFI"
				,"ARTDESPHP"
				,"ARTAFEINV"
				,"ARTFAMILIA"
				,"ARTFACNECR"
				};		
		String[] valores={
				 p.getClave() 		//"ARTCLAVE"
				,StringUtils.substring(p.getDescripcion(),0,30) //"ARTNOMBR"
				,String.valueOf(p.getKilos())		//"ARTKILOS"
				,String.valueOf(p.getGramos()) 		//"ARTGRAMS"
				,String.valueOf(p.getPrecioContado())				//"ARTPRECI"
				,String.valueOf(p.getPrecioCredito())			//"ARTPRECRE"
				,p.getModoDeVenta() 					//"ARTFACNETO"
				,p.getUnidad().getUnidad()				//"ARTUNIDAD"
				,p.getClave()	//"ARTCODORIG"
				,p.isActivo()?"A":"S" //""ARTESTADO"
				,p.isActivoVentas()?"S":"N"	//"ARTOPEVEN"
				,p.getActivoVentasObs()
				,p.isActivoInventario()?"S":"N"	//"ARTOPEINV"
				,p.getActivoInventarioObs()
				,p.isActivoCompras()?"S":"N"	//"ARTOPECOM"
				,p.getActivoComprasObs()
				,p.isDeLinea()?"L":"E" //"ARTCLASIFI"
				,"S"
				,p.isInventariable()?"S":"N"
				,p.getLineaOrigen()	
				,p.isARTFACNECR()?"N":"B"
			};
		String[] tipos={
				 "C"//"ARTCLAVE"
				,"C"//"ARTNOMBR"
				,"N"//"ARTKILOS"
				,"N"//"ARTGRAMS"
				,"N"//"ARTPRECI"
				,"N"//"ARTPRECRE"
				,"C"//"ARTFACNETO"
				,"C"//"ARTUNIDAD"
				,"C"//"ARTCODORIG"
				,"C"//"ARTESTADO"
				,"C"//"ARTOPEVEN"
				,"C"//"ARTOPEVEN"
				,"C"//"ARTOPEINV"
				,"C"//"ARTOPEINV"
				,"C"//"ARTOPECOM"				
				,"C"//"ARTOPECOM"
				,"C"//"ARTCLASIFI"
				,"C"//"ARTDESPHP"
				,"C"//"ARTAFEINV"
				,"C"//"ARTFAMILIA"
				,"C"//"ARTFACNECR"
				
		};
		for(int index=0;index<campos.length;){
			buf.append(campos[index]);
			buf.append("?");
			buf.append(valores[index]);
			buf.append("?");
			buf.append(tipos[index]);
			index++;
			if(index!=campos.length)
				buf.append("|");
		}
		buf.append(SEP);

		// Seccion 7 ?
		buf.append("*");
		buf.append(SEP);

		// Seccion 8 ?
		buf.append("ART");
		buf.append(SEP);

		// Seccion 9 ?
		buf.append("N");
		buf.append(SEP);

		// Seccion 10 Año ?
		Date fecha=new Date();
		int year = Periodo.obtenerYear(fecha);
		buf.append(year);
		String res = buf.toString();
		logger.info("String para replica de producto: \n"+res);
		return res;
	}

	public String export(Object bean, Tipo tipo, Object... objects) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
