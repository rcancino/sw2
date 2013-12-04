package com.luxsoft.siipap.replica;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.Inventario;
import com.luxsoft.siipap.model.Periodo;

/**
 * Genera archivos de replica para exportar los saldos de productos a SIIPAP DBF
 * 
 * Su unica responsabilidad es generar un String a partir de un grupo de 
 * movimientos de inventario
 * 
 * @author Ruben Cancino
 * 
 */
public class ExportadorDeExistencias implements ReplicaExporter{

	public static final String SUC_EXIS_REPLICA_KEY="replica.exis.sucurs";
	
	

	public ExportadorDeExistencias(){
		/*sucursales=new HashSet<Integer>();
		String sucs=System.getProperty(SUC_EXIS_REPLICA_KEY,"9,2");
		String[] parts=StringUtils.split(sucs, ',');
		for(String part:parts){
			try {
				Integer sucursal=Integer.valueOf(part);
				sucursales.add(sucursal);
			} catch (Exception e) {
				System.out.println("No es posible convertir a entero: "+part);
			}
		}*/
	}
	
	

	public String export(Object bean,Tipo tipo) {
		Existencia ex=(Existencia)bean;
		if(ex.getSucursal().getId()==5){
			ex=validar(ex);
			if(ex!=null){
				String res=generarLinea(ex,tipo);
				return res;
			}
		}
		return "";
	}

	public String export(Collection beans,Tipo tipo) {
		StringBuffer buf = new StringBuffer();
		final int end = beans.size();
		int row = 1;
		for(Object obj:beans){
			Existencia bean=(Existencia)obj;
			bean=validar(bean);
			if(bean!=null){
				String res=generarLinea(bean,tipo);
				buf.append(res);
				if(row++<end){
					//System.out.println("ADIG");
					//buf.append("@");
				}
			}
		}
		return buf.toString();
	}

	public Class getBeanClass() {
		return Inventario.class;
	}
	
	//private Set<Integer> sucursales;
	
	private Existencia validar(Existencia exis){
		//if(sucursales.contains(exis.getSucursal().getClave()))
			//return exis;
		return exis;
	}
	
	/**
	 * Aplica la regla particular para generar un archivo de replica
	 * para un bean de existencia
	 * 
	 * @param dd
	 * @return
	 */
	private String generarLinea(Existencia dd,Tipo tipo){
		StringBuffer buf = new StringBuffer();
		char SEP = '[';
		// Archivo
		buf.append("ARSALD@MES.ACU");
		buf.append(SEP);
		// TIPO
		buf.append(Tipo.C);
		buf.append(SEP);

		// Sucursal
		int sucClave = dd.getSucursal().getClave();
		String suc = StringUtils.leftPad(String.valueOf(sucClave), 3, ' ');
		buf.append(suc);
		// buf.append(SEP);

		// Producto
		String prod=StringUtils.rightPad(dd.getProducto().getClave().trim(), 10, ' ');
		buf.append(prod);
		buf.append(SEP);

		// Indices
		buf.append("ARSSUC@MES.NTX|ARSARS@MES.NTX|ARSFEC@MES.NTX");
		buf.append(SEP);

		// ?
		buf.append("1");
		buf.append(SEP);

		// Campos
		if(Tipo.A.equals(tipo)){
			buf.append("SASUCURSAL?"+suc+"?N");
			buf.append("|");
			buf.append("SAARTICULO?"+prod+"?C");
			buf.append("|");
		}
		buf.append("SASALDACT");
		buf.append("?");
		buf.append(dd.getCantidad());
		buf.append("?");
		buf.append("N");
		buf.append(SEP);

		buf.append("*");
		buf.append(SEP);

		buf.append("ASA");
		buf.append(SEP);

		buf.append("F");
		buf.append(SEP);

		// Año
		Date fecha=new Date();
		int year = Periodo.obtenerYear(fecha);
		buf.append(year);
		int mes = Periodo.obtenerMes(fecha)+1;
		String mmes = StringUtils.leftPad(String.valueOf(mes), 2, '0');
		String res = buf.toString();
		res = res.replaceAll("@MES", mmes);
		
		
		
		
		return res;
	}



	public String export(Object bean, Tipo tipo, Object... objects) {
		// TODO Auto-generated method stub
		return null;
	}

	

	

}
