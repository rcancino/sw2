package com.luxsoft.siipap.replica.aop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.Unidad;
import com.luxsoft.siipap.replica.ExportadorDeExistencias;
import com.luxsoft.siipap.replica.ExportadorDeProductos;
import com.luxsoft.siipap.replica.ReplicaExporter.Tipo;

/**
 * Manager que controla le exportacion de informacion a SIIPAP DBF
 * 
 * @author Ruben Cancino
 *
 */
public class ExportadorManager {
	
	private Logger logger=Logger.getLogger(getClass());
	private String destino;
	public static final String ARCHIVO_EXT=".DOR";
	public static final String REPLICA_ACTIVATION_KEY="replica.activa";
	
	private ExportadorDeExistencias exportadorDeSaldos;
	
	private ExportadorDeProductos exportadorDeProductos;
	
	public static  boolean enabled=false;
	
	private static ExportadorManager INSTANCE;
	
	public static ExportadorManager getInstance(){
		if(INSTANCE==null){
			INSTANCE=new ExportadorManager();
		}
		return INSTANCE;
	}
	
	public ExportadorManager(){
		String res=System.getProperty(REPLICA_ACTIVATION_KEY, "ON");
		enabled=res.equals("ON");
		
	}
	
	public void exportar(Object bean,Tipo tipo){
		if(!isEnabled())
			return;
		if(bean instanceof Producto){
			Producto p=(Producto)bean;
			exportarProducto(p, tipo);
		}
		else if(bean instanceof Existencia){
			exportarExistencia((Existencia)bean, tipo);
		}
	}
	
	/**
	 * Interfas publica para la exportacion de datos de productos
	 * a SIIPAP 
	 * 
	 * @param prod
	 * @param tipo
	 */
	public void exportarProducto(Producto prod ,Tipo tipo){
		if(prod.isReplicar()){
			String res=getExportadorDeProductos().export(prod, tipo);
			toFile(res);
		}
	}
	
	/**
	 * Exporta la existencia disponible de un producto
	 * y sucursal. Atendiendo a las reglas establecidas para
	 * dicho proceso 
	 * 
	 * @param exis
	 * @param tipo
	 */
	public String exportarExistencia(Existencia exis,Tipo tipo){
		
		//Siempre se requiere una alta primero
		String res=getExportadorDeSaldos().export(exis,Tipo.A);
		return toFile(res);
		
		/*if(!tipo.equals(Tipo.A)){
			try {
	            Thread.sleep(1000);
	        } catch (InterruptedException e) {}
			res=getExportadorDeSaldos().export(exis,tipo);
			toFile(res);
		}*/
		
	}
	
	/*
	public void exportarExistencia(Collection<Existencia> exis,Tipo tipo){		
		String res=getExportadorDeSaldos().export(exis,tipo);
		toFile(res);		
	}
	*/
	
	/**
	 * Lee todo el contenido de un archivo de texto de replica y lo convierte
	 * a String ASCII adecuado para ser analizado 
	 * 
	 * @param data
	 * @param name
	 */
	public String toFile(final String data){
		//System.out.println("Folio: 20-"+folio());
		if(StringUtils.isBlank(data))
			return null;
		final File target=new File(getDestino(),folio()+ARCHIVO_EXT);			
		FileOutputStream os;
		try {
			os = new FileOutputStream(target);
			final Writer out=new OutputStreamWriter(os,"ISO-8859-1");
			out.write(data);
			out.flush();
			out.close();
			os.close();
			if(logger.isDebugEnabled()){
				logger.debug("Archivo de replica generado: "+target.getName());
			}
			System.out.println("Archivo de replica generado: "+target.getAbsolutePath());
			return target.getAbsolutePath();
			//saveBackup(target);
		} catch (Exception e) {
			logger.info("Error al generar archivo de replica: "+e.getMessage());
			logger.error(e);
			return null;
		}	
		
	}
	
	
	
	
	/**
	 * Genera un consecutivo adecuado para usar como nombre del archivo de replica
	 * 
	 * @return
	 */
	public String folio(){
		long time=System.currentTimeMillis();
		String res="20"+StringUtils.substring(String.valueOf(time),-6);
		return res;
	}
		
	
	public String getDestino() {
		if(destino==null){
			destino=System.getProperty("replica.target","C:\\PRUEBAS\\REPLICA\\");
		}
		return destino;
	}
	
	public void setDestino(String destino) {
		
		this.destino = destino;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ExportadorDeExistencias getExportadorDeSaldos() {
		if(exportadorDeSaldos==null){
			exportadorDeSaldos=new ExportadorDeExistencias();
		}
		return exportadorDeSaldos;
	}

	public void setExportadorDeSaldos(ExportadorDeExistencias exportadorDeSaldos) {
		this.exportadorDeSaldos = exportadorDeSaldos;
	}
	

	public ExportadorDeProductos getExportadorDeProductos() {
		return exportadorDeProductos;
	}

	public void setExportadorDeProductos(ExportadorDeProductos exportadorDeProductos) {
		this.exportadorDeProductos = exportadorDeProductos;
	}

	public static void main(String[] args) {
		/*
		Movimiento mov=new Movimiento();
		mov.setConcepto(Concepto.CIS);
		mov.setSucursal(new Sucursal(10,"Ermita"));
		
		for(int i=0;i<3;i++){
			MovimientoDet det=new MovimientoDet();
			det.setCantidad(50);
			det.setComentario("PRUEBA DE MOVIMIENTO DET_"+i);
			det.setProducto(new Producto("PRUEBAS01"));
			mov.agregarPartida(det);
		}
		
		
		ExportadorManager manager=new ExportadorManager();
		manager.setDestino("C:\\pruebas\\replica\\");
		
		ExportadorDeSaldo exporter=new ExportadorDeSaldo();
		//String res=exporter.toString(mov.getPartidas());		
		String res=exporter.export(mov.getPartidas());
		manager.toFile(res, "20123456");
		//manager.exportar(mov.getPartidas());
		*/
		
		/* Actualizacion
		List<Existencia> exis=new ArrayList<Existencia>();
		Existencia e1=new Existencia(new Producto("PRUEBA01"),new Sucursal(10,"Ermite"),458);
		exis.add(e1);
		Existencia e2=new Existencia(new Producto("PRUEBA02"),new Sucursal(10,"Ermite"),726);
		exis.add(e2);
		ExportadorManager manager=new ExportadorManager();
		manager.setDestino("C:\\pruebas\\replica\\");
		manager.setExportadorDeSaldos(new ExportadorDeSaldos());
		manager.exportarExistencia(exis,Tipo.C);
			*/
		
		/** Alta/Cambio ARSALD 
		List<Existencia> exis=new ArrayList<Existencia>();
		Existencia e1=new Existencia(new Producto("PRUEBA03"),new Sucursal(10,"Ermite"),791);
		exis.add(e1);
		ExportadorManager manager=new ExportadorManager();
		manager.setDestino("C:\\pruebas\\replica\\");
		manager.setExportadorDeSaldos(new ExportadorDeSaldos());
		
		manager.exportarExistencia(exis,Tipo.C);
		**/
		
		/** Alta/Cambio PRODUCTOP 
		Producto p=new Producto("PRUEBA05","ARTICULO DE PRUEBA 5");
		p.setKilos(12.5);
		p.setPrecioContado(1200.00);
		p.setPrecioCredito(1200.00);
		p.setGramos(200);
		p.setLineaOrigen("1000000000");
		p.setUnidad(new Unidad("MIL",1000));
		ExportadorManager manager=new ExportadorManager();
		manager.setDestino("C:\\pruebas\\replica\\");
		manager.setExportadorDeProductos(new ExportadorDeProductos());
		
		manager.exportarProducto(p,Tipo.C);**/
		
		
		
	}

}
