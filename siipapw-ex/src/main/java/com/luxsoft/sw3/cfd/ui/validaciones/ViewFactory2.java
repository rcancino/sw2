package com.luxsoft.sw3.cfd.ui.validaciones;

import java.awt.Dimension;
import java.io.File;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.sw3.cfd.CFDUtils;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

/**
 * Factory class para la generacion de vistas version 2 del esquema para CFD (SAT) :
 * Genera dos tipos de vista: 
 * 	- Rsultados en HTML
 *  - Vista generica del comprobante (Jasper)
 * 
 * @author Ruben Cancino
 *
 */
public class ViewFactory2 {

	private static Configuration cfg;
	
	public static String getHTMLValidationView(List<Resultado> resultados,File xmlFile){
		if(cfg==null){
			cfg=new Configuration();
			cfg.setDateFormat("dd/MM/yyyy");
			cfg.setObjectWrapper(new DefaultObjectWrapper());
			cfg.setClassForTemplateLoading(ViewFactory2.class, "/");
			
			//String path="images2/papelLogo.jpg";
			
		}
		String path=ClassUtils.addResourcePathToPackagePath(ViewFactory2.class, "validacionResultCFD.ftl");
		
		Map root=new HashMap();
		root.put("version", 2);
		root.put("resultados", resultados);
		root.put("archivo", xmlFile.getAbsoluteFile());
		root.put("fecha", new Date());
		
		try {
			final Template temp=cfg.getTemplate(path);			
			StringWriter out=new StringWriter();
			temp.process(root, out);			
			return out.toString();
		} catch (Exception e) {		
			e.printStackTrace();
			return "Error en template: "+ExceptionUtils.getFullStackTrace(e);
		}	
	}
	
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static JComponent mostrar(ComprobanteDocument document,String xmlFile){
		Comprobante cfd=document.getComprobante();
		final EventList<Concepto> conceptos=GlazedLists.eventList(Arrays.asList(cfd.getConceptos().getConceptoArray()));
		
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		String ruta="ventas/FacturaDigitalGenerica.jasper";
		Resource res = loader.getResource(ReportUtils.toReportesPath(ruta));
		Map parametros=new HashMap();
		parametros.put("SERIE", cfd.getSerie());
		parametros.put("FOLIO", cfd.getFolio());
		parametros.put("FECHA", cfd.getFecha().getTime());
		parametros.put("SELLO", cfd.getSello());
		parametros.put("NO_APROBACION", new Integer(cfd.getNoAprobacion().intValue()));
		parametros.put("ANO_APROBACION", cfd.getAnoAprobacion());
		parametros.put("FORMA_DE_PAGO", cfd.getFormaDePago());
		parametros.put("NO_CERTIFICADO", cfd.getNoCertificado());
		parametros.put("CERTIFICADO", cfd.getCertificado());
		parametros.put("CONDICIONES_DE_PAGO", cfd.getCondicionesDePago());
		parametros.put("SUBTOTAL", cfd.getSubTotal());
		parametros.put("DESCUENTO", cfd.getDescuento());
		parametros.put("DESCUENTO_MOTIVO", cfd.getMotivoDescuento());
		parametros.put("TOTAL", cfd.getTotal());
		parametros.put("MEDOTO_DE_PAGO", cfd.getMetodoDePago());
		parametros.put("TIPO_COMPROBANTE", cfd.getTipoDeComprobante().toString());
		parametros.put("TOTAL_IMPUESTO_RETENIDO", cfd.getImpuestos().getTotalImpuestosRetenidos());
		parametros.put("TOTAL_IMPUESTO_TRASLADADO", cfd.getImpuestos().getTotalImpuestosTrasladados());
		parametros.put("IMP_CON_LETRA", ImporteALetra.aLetra(CantidadMonetaria.pesos(cfd.getTotal())));
		parametros.put("CADENA_ORIGINAL", ServiceLocator2.getCFDManager().generarCadenaOriginal(document));
	
		Emisor e=cfd.getEmisor();
		
		parametros.put("EMISOR_NOMBRE", e.getNombre());
		parametros.put("EMISOR_DOMICILIO", CFDUtils.getDireccionEnFormatoEstandar(e.getDomicilioFiscal()));
		parametros.put("EXPEDIDO_DIRECCION", CFDUtils.getDireccionEnFormatoEstandar(e.getExpedidoEn()));
		parametros.put("EMISOR_RFC", e.getRfc());
		
		parametros.put("RECEPTOR_NOMBRE", cfd.getReceptor().getNombre());
		parametros.put("RECEPTOR_DOMICILIO", CFDUtils.getDireccionEnFormatoEstandar(cfd.getReceptor().getDomicilio()));
		parametros.put("RECEPTOR_RFC", cfd.getReceptor().getRfc());
		parametros.put("XML", xmlFile);
		
		System.out.println("Generando impresion de CFD con parametros: "+parametros);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidad","unidad","descripcion","valorUnitario","importe"};
			String[] etiquetas={"CANTIDAD","UNIDAD","DESC","VALOR UNI","IMPORTE"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			JRViewer jasperViewer = new JRViewer(jasperPrint);
			jasperViewer.setPreferredSize(new Dimension(900, 1000));
			return jasperViewer;
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			return new JLabel(ExceptionUtils.getRootCauseMessage(ioe));
		}
		
		
	}

}
