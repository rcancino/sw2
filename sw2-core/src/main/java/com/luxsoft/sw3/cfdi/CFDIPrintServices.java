package com.luxsoft.sw3.cfdi;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x3.TUbicacion;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.ImporteALetra;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.TimbreFiscal;

public class CFDIPrintServices {
	
	public static String getJasperReport(String reportName){
		String reps=System.getProperty("sw3.reports.path");
		if(StringUtils.isEmpty(reps))
			throw new RuntimeException("No se ha definido la ruta de los reportes en: sw3.reports.path");
		return reps+reportName;
	}
	
	public static void imprimirFacturaElectronica(Venta venta,boolean printPreview,String... destinatarios){
		for(String destinatario:destinatarios){
			impripirComprobante(venta,null,destinatario,printPreview);
		}
	}
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static JasperPrint impripirComprobante(Venta venta,CFDI cfdi,String destinatario,boolean printPreview){
		final EventList<VentaDet> conceptos=GlazedLists.eventList(venta.getPartidas());
		Map parametros=resolverParametros(venta,cfdi);
		parametros.put("DESTINATARIO", destinatario);
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		String jasper=cfdi.getTimbreFiscal().getUUID()!=null?"ventas/FacturaCFDI.jasper":"ventas/RemisionVentaCFDI.jasper";
	
		//String jasper=cfdi.getTimbreFiscal().getUUID()!=null?"ventas/FacturaCFDIBajio.jasper":"ventas/RemisionVentaCFDI.jasper";
		Resource res = loader.getResource(getJasperReport(jasper));
		//System.out.println("Generando impresion de CFDI con parametros: "+parametros+ " \nruta: "+res);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidadEnUnidad","clave","descripcion","kilosMillar","producto.gramos","precio","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","producto.unidad.unidad","ordenp","precioConIva","importeConIva"};
			String[] etiquetas={"CANTIDAD","CLAVE","DESCRIPCION","KXM","GRAMOS","PRECIO","IMPORTE","CORTES_INSTRUCCION","MDV","GRUPO","MDV","UNIDAD","ORDENP","PRECIO_IVA","IMPORTE_IVA"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			return jasperPrint;
		} catch (Exception ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(ioe));
		}
	}
	
	
	public static Map resolverParametros(Venta venta,CFDI cfdi){
		Comprobante comprobante=cfdi.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
		// Datos tomados del Comprobante fiscal digital XML
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		//parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		//parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado());
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); 
		parametros.put("CADENA_ORIGINAL", 	cfdi.getCadenaOriginal());
		parametros.put("NOMBRE", 			comprobante.getReceptor().getNombre()); //Recibir como Parametro
		parametros.put("RFC", 				comprobante.getReceptor().getRfc());
		parametros.put("FECHA", 			comprobante.getFecha().getTime());
		parametros.put("NFISCAL", 			comprobante.getSerie()+" - "+comprobante.getFolio());		
		//parametros.put("IMPORTE", 			comprobante.getSubTotal());
		parametros.put("IMPORTE", 			venta.getImporteBruto().subtract(venta.getImporteDescuento()));
		parametros.put("IMPUESTO", 			comprobante.getImpuestos().getTotalImpuestosTrasladados()); 
		parametros.put("TOTAL", 			comprobante.getTotal()); 
		parametros.put("DIRECCION", 		CFDIUtils.getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()) );
		parametros.put("CUENTA", 		comprobante.getNumCtaPago());
		parametros.put("METODO_PAGO", 		comprobante.getMetodoDePago());
		
		System.out.println("Complemento"+comprobante.getComplemento());
		//System.out.println("Metodo de pago: "+comprobante.getMetodoDePago());
	//	System.out.println("Cuenta de pago: "+comprobante.getNumCtaPago());
		//Datos tomado de la aplicacion
		parametros.put("CARGO_ID", 			venta.getId());
		parametros.put("IMP_CON_LETRA", 	ImporteALetra.aLetra(venta.getTotalCM()));
		parametros.put("SUCURSAL", 			venta.getSucursal().getId()); 		
		parametros.put("CLAVCTE", 			venta.getClave()); 		
		parametros.put("SUC", 				venta.getSucursal().getClave()); 
		
		parametros.put("TEL", 				venta.getCliente().getTelefonosRow());		
		parametros.put("D_REV", 			venta.getDiaRevision());
		parametros.put("D_PAG", 			venta.getDiaDelPago());
		parametros.put("COB", 				venta.getCobrador()!=null?venta.getCobrador().getId():null);
		parametros.put("VEND", 				venta.getVendedor()!=null?venta.getVendedor().getId():null);
		parametros.put("PLAZO", 			venta.getPlazo());
		parametros.put("FREV", 				venta.isRevision()?"R":"");
		parametros.put("SOCIO", 			venta.getSocio()!=null?venta.getSocio().getNombre():null); 
		parametros.put("TIPO", 				venta.getOrigen().equals(OrigenDeOperacion.CRE)?"CREDITO":"CONTADO");
		parametros.put("DOCTO", 			venta.getDocumento());		
		parametros.put("TAR_COM_IMP", 		venta.getCargos());
		parametros.put("COMENTARIO", 		venta.getComentario()); 
		parametros.put("PCE", 				venta.isContraEntrega()?"COD":"PAGADO CON"); 
		
		
		parametros.put("ENVIO", 		venta.getPedidoFormaDeEntrega().equals("LOCAL")?"PASAN":"ENVIO");
		parametros.put("PEDIDO", 		venta.getPedidoFolio());
		parametros.put("IP", 			venta.getPedidoCreatedIp());
		parametros.put("ELAB_VTA",		venta.getPedidoCreateUser());
		parametros.put("PUESTO", 		venta.getPuesto()?"**PUESTO**":"");
		parametros.put("DIR_ENTREGA", 	venta.getMisma()?"***MISMA***":venta.getInstruccionDeEntrega());
		if(venta.getSocio()!=null && venta.getMisma()){
			parametros.put("DIR_ENTREGA", 	venta.getSocio().getDireccion());
		}
		parametros.put("KILOS", 		venta.getKilosCalculados());
		parametros.put("MONEDA", 		venta.getMoneda().getCurrencyCode());
		parametros.put("IMP_DESC", 		venta.getSubTotal2());
		parametros.put("CORTES", 		venta.getImporteCortes());
		parametros.put("FLETE", 		venta.getFlete()); // venta
		parametros.put("CARGOS", 		venta.getCargos()); // venta
		parametros.put("FPAGO", 		venta.getFormaDePago().name());
		
		parametros.put("ELAB_FAC", 		venta.getLog().getUpdateUser());
		parametros.put("SURTIDOR", 		venta.getSurtidor()); 
		parametros.put("IMPORTE_BRUTO", venta.getImporteBruto());
		parametros.put("SUBTOTAL_2", 	venta.getImporteBruto().subtract(venta.getImporteDescuento())); 
		parametros.put("DESCUENTO", 	BigDecimal.valueOf(venta.getDescuentoGlobal())); 
		parametros.put("DESCUENTOS", 	venta.getImporteDescuento());
		
		parametros.put("PINT_IVA",		MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
		parametros.put("TIPOX", 		venta.getOrigen().equals(OrigenDeOperacion.CRE)?"CREDITO":"CONTADO");
		//parametros.put("DESTINATARIO", "CLIENTE");
		parametros.put("IMPRESO", 	venta.getImpreso());
		
		if(venta.getClave().equals("1")){			
			CantidadMonetaria factor=CantidadMonetaria.pesos(1).add(CantidadMonetaria.pesos(MonedasUtils.IVA));
			parametros.put("IMPORTE_BRUTO", factor.multiply(venta.getImporteBruto()).amount());
			parametros.put("DESCUENTOS", 	factor.multiply(venta.getImporteDescuento()).amount());
			parametros.put("SUBTOTAL_2", 	factor.multiply(venta.getImporteBruto().subtract(venta.getImporteDescuento())).amount());
			parametros.put("IMP_DESC", 		factor.multiply(venta.getSubTotal2()).amount());
			parametros.put("CORTES", 		factor.multiply(venta.getImporteCortes()).amount());
			parametros.put("FLETE", 		factor.multiply(venta.getFlete()).amount()); 
			parametros.put("CARGOS", 		factor.multiply(venta.getCargos()).amount());
			
			parametros.put("IMPORTE", 			comprobante.getTotal()); 
			
			//parametros.put("ANTICIPO", MonedasUtils.calcularImporteSinIva(venta.getAnticipoAplicado()));
			
		
		}
		
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
		if (venta.getAnticipoAplicado()!= null)
		parametros.put("ANTICIPO", MonedasUtils.calcularImporteSinIva(venta.getAnticipoAplicado()));
		
		
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


