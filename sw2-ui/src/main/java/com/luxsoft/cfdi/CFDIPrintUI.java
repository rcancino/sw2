package com.luxsoft.cfdi;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import java.awt.Dimension;
import java.awt.Image;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import mx.gob.sat.cfd.x3.TUbicacion;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x3.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.uif.util.ScreenUtils;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.inventarios.model.Traslado;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.ImporteALetra;
import com.luxsoft.sw3.cfdi.CFDINotaDeCargoPrintServices;
import com.luxsoft.sw3.cfdi.CFDINotaPrintServices;
import com.luxsoft.sw3.cfdi.CFDIPrintServices;
import com.luxsoft.sw3.cfdi.QRCodeUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;
import com.luxsoft.sw3.cfdi.model.TimbreFiscal;

public class CFDIPrintUI {
	
	
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static  void impripirComprobante(Venta venta,CFDI cfdi,String destinatario,Date time,HibernateTemplate hibernateTemplate,boolean printPreview){
		try {
			JasperPrint jasperPrint = CFDIPrintServices.impripirComprobante(venta, cfdi, destinatario, printPreview);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			if(venta.getImpreso()==null && (cfdi!=null)){
				venta.setImpreso(time);
				hibernateTemplate.merge(venta);
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
		
		
	}
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static  void impripirComprobante(NotaDeCredito nota,CFDI cfdi
			,String destinatario
			,Date time
			,boolean printPreview){
		try {
			JasperPrint jasperPrint = CFDINotaPrintServices.impripirComprobante(nota, cfdi);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
		
		
	}
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static  void impripirComprobante(NotaDeCargo nota,CFDI cfdi
			,String destinatario
			,Date time
			,boolean printPreview){
		try {
			JasperPrint jasperPrint = CFDINotaDeCargoPrintServices.impripirComprobante(nota, cfdi);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
	}
	
	public static JasperPrint impripirComprobante(Traslado tps,CFDI cfdi){
		
		
		Assert.notNull(cfdi,"CFDI nulo");
		
		String reporte="CFDITraslado.jasper";
		
		
		List<Concepto> conceptosArray=Arrays.asList(cfdi.getComprobante().getConceptos().getConceptoArray());
		final EventList conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(cfdi);
		parametros.put("KILOS",BigDecimal.valueOf(tps.getKilos()));
		parametros.put("SUCURSAL",tps.getSucursal().getNombre() );
		parametros.put("SURTIDOR",tps.getSurtidor());
		parametros.put("SUPERVISOR",tps.getSuperviso());
		parametros.put("ELABORO", tps.getLog().getCreateUser());
		parametros.put("CHOFER",tps.getChofer());
		
		
		
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		String jasper="INVENT/"+reporte;
		Resource res = loader.getResource(getJasperReport(jasper));
		System.out.println("Generando impresion de CFDI con parametros: "+parametros+ " \nruta: "+res);
		Assert.isTrue(res.exists());
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidad","NoIdentificacion","descripcion","unidad","ValorUnitario","Importe"};
			String[] etiquetas={"cantidad","NoIdentificacion","descripcion","unidad","ValorUnitario","Importe"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			//final EventTableModel tableModel=new EventTableModel(new BasicEventList(),tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			return jasperPrint;
		} catch (Exception ioe) {
			ioe.printStackTrace();
			throw new RuntimeException(ExceptionUtils.getRootCauseMessage(ioe));
		}
	}
	
	public static  void impripirComprobante(Traslado tps,CFDI cfdi,boolean printPreview){
		try {
			JasperPrint jasperPrint = impripirComprobante(tps,cfdi);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
	}
	
	public static Map resolverParametros(CFDI cfdi){
		
		Comprobante comprobante=cfdi.getComprobante();
		Map  parametros=new HashMap();
		// Datos tomados del Comprobante fiscal digital XML
		parametros.put("SERIE", 			comprobante.getSerie());
		parametros.put("FOLIO", 			comprobante.getFolio());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado());
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello());
		parametros.put("CADENA_ORIGINAL", 	cfdi.getCadenaOriginal());
		parametros.put("RECEPTOR_NOMBRE", 	comprobante.getReceptor().getNombre()); //Recibir como Parametro
		parametros.put("RECEPTOR_RFC", 		comprobante.getReceptor().getRfc());
		parametros.put("FECHA", 			comprobante.getFecha().getTime());
		parametros.put("NFISCAL", 			comprobante.getSerie()+" - "+comprobante.getFolio());
		parametros.put("IMPORTE_BRUTO", 			comprobante.getSubTotal());
		parametros.put("IVA", 				comprobante.getImpuestos().getTotalImpuestosTrasladados());
		parametros.put("TOTAL", 			comprobante.getTotal());
		parametros.put("RECEPTOR_DIRECCION",getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()) );
		parametros.put("NUM_CTA_PAGO", 		comprobante.getNumCtaPago());
		parametros.put("METODO_PAGO", 		comprobante.getMetodoDePago());
		parametros.put("FORMA_PAGO", 		comprobante.getFormaDePago());
		//Datos tomado de la aplicacion
		parametros.put("IMP_CON_LETRA", ImporteALetra.aLetra(CantidadMonetaria.pesos(comprobante.getTotal())) );
		parametros.put("FORMA_DE_PAGO",	comprobante.getFormaDePago());
		parametros.put("PINT_IVA","16 ");
		
		parametros.put("DESCUENTOS", 	comprobante.getDescuento());
		
		
		if(comprobante.getReceptor().getRfc()=="XAXX010101000"){
			parametros.put("IMPORTE", 			comprobante.getTotal());
		}
		
		Emisor emisor=comprobante.getEmisor();
		parametros.put("EMISOR_NOMBRE", 	emisor.getNombre());
		parametros.put("EMISOR_RFC", 		emisor.getRfc());
		String pattern="{0} {1}  {2}  {3}" +
				"\n{4}  {5}  {6}";
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
		parametros.put("EXPEDIDO_DIRECCION", direccionEmisor);
		parametros.put("REGIMEN",comprobante.getEmisor().getRegimenFiscalArray(0).getRegimen());
		
		if (emisor.getExpedidoEn() != null){
			TUbicacion expedido=emisor.getExpedidoEn();
		
			String pattern2="{0} {1}  {2}  {3}" +
				"\n{4}  {5}  {6}";
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
		
		//Especiales para CFDI
			
		if(cfdi.getUUID()!=null){
			//println 'Imagen generada: '+img
			Image img=QRCodeUtils.generarQR(cfdi.getComprobante());
			parametros.put("QR_CODE",img);
			TimbreFiscal timbre=new TimbreFiscal(cfdi.getComprobante());
			parametros.put("FECHA_TIMBRADO", timbre.FechaTimbrado);
			parametros.put("FOLIO_FISCAL", timbre.UUID);
			parametros.put("SELLO_DIGITAL_SAT", timbre.selloSAT);
			parametros.put("CERTIFICADO_SAT", timbre.noCertificadoSAT);
			parametros.put("CADENA_ORIGINAL_SAT", timbre.cadenaOriginal());
		}
		Resource logors=rloader.getResource("images/empresaFacLogo.jpg");
		if(logors.exists()){
			try {
				Image logo=ImageIO.read(logors.getInputStream());
				parametros.put("LOGO", logo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return parametros;
	}
	
	static DefaultResourceLoader rloader=new DefaultResourceLoader();
	
	public static String getDireccionEnFormatoEstandar(TUbicacion u){
		if(u==null)
			return "";
		String pattern="{0} {1} {2} {3}" +
				" {4} {5} {6}" +
				" {7} {8}";
		//StringUtils.
		return MessageFormat.format(pattern
				,u.getCalle() !=null?u.getCalle():""
				,(u.getNoExterior()!=null && !u.getNoExterior().equals(".") )?"NO."+u.getNoExterior():""
				,(u.getNoInterior()!=null && !u.getNoInterior().equals(".") )?"INT."+u.getNoInterior():""
				,u.getColonia()!=null?","+u.getColonia():""
				,u.getCodigoPostal() !=null?","+u.getCodigoPostal():""
				,u.getMunicipio()!=null?","+u.getMunicipio():""
				,u.getLocalidad()!=null?","+u.getLocalidad():""
				,u.getEstado()!=null?","+u.getEstado()+",":""
				,u.getPais()!=null?u.getPais():""
				);
	}
	
	public static String getJasperReport(String reportName){
		String reps=System.getProperty("sw3.reports.path");
		if(StringUtils.isEmpty(reps))
			throw new RuntimeException("No se ha definido la ruta de los reportes en: sw3.reports.path");
		return reps+reportName;
	}
}


