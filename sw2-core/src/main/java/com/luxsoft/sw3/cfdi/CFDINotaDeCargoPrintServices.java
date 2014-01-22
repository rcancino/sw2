package com.luxsoft.sw3.cfdi;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x3.TUbicacion;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.ImporteALetra;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.TimbreFiscal;

public class CFDINotaDeCargoPrintServices {
	
	public static String getJasperReport(String reportName){
		String reps=System.getProperty("sw3.reports.path");
		if(StringUtils.isEmpty(reps))
			throw new RuntimeException("No se ha definido la ruta de los reportes en: sw3.reports.path");
		return reps+reportName;
	}
	
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static JasperPrint impripirComprobante(NotaDeCargo nota,CFDI cfdi){
		
		Assert.notNull(nota,"Nota nula");
		Assert.notNull(cfdi,"CFDI nulo");
		
		String reporte="NotaCarDigitalCFDI.jasper";
		
		//final EventList<VentaDet> conceptos=GlazedLists.eventList(venta.getPartidas());
		List<Concepto> conceptosArray=Arrays.asList(cfdi.getComprobante().getConceptos().getConceptoArray());
		final EventList conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(nota,cfdi);
		
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		String jasper="CXC/"+reporte;
		Resource res = loader.getResource(getJasperReport(jasper));
		System.out.println("Generando impresion de CFDI con parametros: "+parametros+ " \nruta: "+res);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidad","descripcion","valorUnitario","importe","descripcion"};
			String[] etiquetas={"CANTIDAD","DESCRIPCION","PRECIO","IMPORTE","GRUPO"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			//String[] columnas= {"cantidadEnUnidad","clave","descripcion","kilosMillar","producto.gramos","precio","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","producto.unidad.unidad","ordenp","precioConIva","importeConIva"};
			//String[] etiquetas={"CANTIDAD","CLAVE","DESCRIPCION","KXM","GRAMOS","PRECIO","IMPORTE","CORTES_INSTRUCCION","MDV","GRUPO","MDV","UNIDAD","ORDENP","PRECIO_IVA","IMPORTE_IVA"};
			//final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			return jasperPrint;
		} catch (Exception ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(ioe));
		}
	}
	
	
	public static Map resolverParametros(NotaDeCargo nota,CFDI cfdi){
		Comprobante comprobante=cfdi.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
		// Datos tomados del Comprobante fiscal digital XML
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado());
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); 
		parametros.put("CADENA_ORIGINAL", 	cfdi.getCadenaOriginal());
		parametros.put("NOMBRE", 			comprobante.getReceptor().getNombre()); //Recibir como Parametro
		parametros.put("RFC", 				comprobante.getReceptor().getRfc());
		parametros.put("FECHA", 			comprobante.getFecha().getTime());
		parametros.put("NFISCAL", 			comprobante.getSerie()+" - "+comprobante.getFolio());		
		
		parametros.put("IMPORTE", 			comprobante.getSubTotal());
		parametros.put("IMPUESTO", 			comprobante.getImpuestos().getTotalImpuestosTrasladados()); 
		parametros.put("TOTAL", 			comprobante.getTotal()); 
		parametros.put("DIRECCION", 		CFDIUtils.getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()) );
		parametros.put("CUENTA", 		comprobante.getNumCtaPago());
		parametros.put("METODO_PAGO", 		comprobante.getMetodoDePago());
		parametros.put("CARGO_ID", 			nota.getId());
		parametros.put("IMP_CON_LETRA", 	ImporteALetra.aLetra(nota.getTotalCM()));
		parametros.put("SUCURSAL", 			nota.getSucursal().getId()); 		
		parametros.put("CLAVCTE", 			nota.getClave()); 		
		parametros.put("SUC", 				nota.getSucursal().getClave()); 
		
		parametros.put("TEL", 				nota.getCliente().getTelefonosRow());		
		parametros.put("D_REV", 			nota.getDiaRevision());
		parametros.put("D_PAG", 			nota.getDiaDelPago());
		parametros.put("COB", 				nota.getCobrador()!=null?nota.getCobrador().getId():null);
		parametros.put("PLAZO", 			nota.getPlazo());
		parametros.put("FREV", 				nota.isRevision()?"R":"");
		
		parametros.put("TIPO", 				nota.getOrigen().toString());
		parametros.put("DOCTO", 			nota.getDocumento());		
		parametros.put("TAR_COM_IMP", 		nota.getCargos());
		parametros.put("COMENTARIO", 		nota.getComentario());
		parametros.put("COMENTARIO_CAR", 		nota.getComentario());
		parametros.put("ELAB_FAC", 		nota.getLog().getCreateUser());
		parametros.put("PINT_IVA",		MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
		
		Emisor emisor=comprobante.getEmisor();
		parametros.put("EMISOR_NOMBRE", 	emisor.getNombre());
		parametros.put("EMISOR_RFC", 		emisor.getRfc());
		String pattern="{0} {1}  {2}" +
				"\n{3}" +
				"\n{4}" +
				"\n{5}  {6}";
		String direccionEmisor=MessageFormat.format(pattern
				,emisor.getDomicilioFiscal().getCalle()
				,emisor.getDomicilioFiscal().getNoExterior()
				,StringUtils.defaultIfEmpty(emisor.getDomicilioFiscal().getNoInterior(),"")				
				,emisor.getDomicilioFiscal().getColonia()				
				,emisor.getDomicilioFiscal().getMunicipio()
				,emisor.getDomicilioFiscal().getCodigoPostal()
				,emisor.getDomicilioFiscal().getEstado()
				);
		parametros.put("EMISOR_DIRECCION", direccionEmisor);
		
		if (emisor.getExpedidoEn() != null){
			TUbicacion expedido=emisor.getExpedidoEn();
		
			String pattern2="{0} {1}  {2}" +
				"\n{3}" +
				"\n{4}" +
				"\n{5}  {6}";
			String expedidoDir=MessageFormat.format(pattern2
				,expedido.getCalle()
				,expedido.getNoExterior()
				,StringUtils.defaultIfEmpty(expedido.getNoInterior(),"")
				,expedido.getColonia()
				,expedido.getMunicipio()
				,expedido.getCodigoPostal()
				,expedido.getEstado()
				);
			parametros.put("EXPEDIDO_DIRECCION", expedidoDir);
		}
		else
			parametros.put("EXPEDIDO_DIRECCION", "SNA");
		//Especiales para CFDI
		if(cfdi.getTimbreFiscal()!=null){
			parametros.put("QR_CODE", QRCodeUtils.generarQR(cfdi.getComprobante()));
			TimbreFiscal timbre=cfdi.getTimbreFiscal();
			parametros.put("FECHA_TIMBRADO", timbre.FechaTimbrado);
			parametros.put("FOLIO_FISCAL", timbre.UUID);
			parametros.put("SELLO_DIGITAL_SAT", timbre.selloSAT);
			parametros.put("CERTIFICADO_SAT", timbre.noCertificadoSAT);
			parametros.put("CADENA_ORIGINAL_SAT", timbre.cadenaOriginal());
		}
		return parametros;
	}
	
	
}


