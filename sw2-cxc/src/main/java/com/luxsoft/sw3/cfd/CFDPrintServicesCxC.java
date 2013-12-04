package com.luxsoft.sw3.cfd;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.uif.util.ScreenUtils;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class CFDPrintServicesCxC {
	
	
	
	public static void imprimirNotaDeCreditoElectronica(String notaDeCreditoId){
		
		NotaDeCredito nota=buscarNotaDeCreditoInicializada(notaDeCreditoId);
		if(nota==null)
			return;
		String reporte="NotaDevDigital.jasper";
		if(nota instanceof NotaDeCreditoBonificacion){
			reporte="NotaBonDigital.jasper";
		}
		ComprobanteFiscal cf=buscarComprobante(nota.getId());
		cf.loadComprobante();
		List<Concepto> conceptosArray=Arrays.asList(cf.getComprobante().getConceptos().getConceptoArray());
		final EventList<Concepto> conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(nota,null);
		
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource(ReportUtils
				.toReportesPath("cxc/"+reporte));
		System.out.println("Generando impresion de CFD con parametros: "+parametros);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidad","unidad","descripcion","valorUnitario","importe","descripcion"};
			String[] etiquetas={"CANTIDAD","UNIDAD","DESCRIPCION","PRECIO","IMPORTE","GRUPO"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			
			JRViewer jasperViewer = new JRViewer(jasperPrint);
			jasperViewer.setPreferredSize(new Dimension(900, 1000));
			ReportDialog dialog=new ReportDialog(jasperViewer,"Nota de credito digital",false);
			ScreenUtils.locateOnScreenCenter(dialog);
			dialog.open();
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFD", ioe);
			return;
		}		
	}
	
	private static NotaDeCredito buscarNotaDeCreditoInicializada(final String id){
		
		return (NotaDeCredito)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				NotaDeCredito res=(NotaDeCredito)session.get(NotaDeCredito.class, id);
				res.getCliente().getTelefonosRow();
				if(res.getConceptos()!=null && !res.getConceptos().isEmpty()){
					res.getConceptos().iterator().next();
				}
				if(!res.getAplicaciones().isEmpty()){
					res.getAplicaciones().iterator().next();
				}
				if(res instanceof NotaDeCreditoDevolucion){
					NotaDeCreditoDevolucion ndev=(NotaDeCreditoDevolucion)res;
					ndev.getDevolucion().getPartidas().iterator().next();
					ndev.getDevolucion().isTotal();
				}
				return res;
			}
			
		});
		
	}
	
	public static Map resolverParametros(NotaDeCredito nota,ComprobanteFiscal cf){
		if(cf==null)
			cf=buscarComprobante(nota.getId());
		cf.loadComprobante();
		Comprobante comprobante=cf.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
// Datos tomados del Comprobante fiscal digital XML
		
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado()); //Recibir como Parametro
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); //Recibir como Parametro
		parametros.put("CADENA_ORIGINAL", 	getCadenaOriginal(cf.getComprobanteDocument())); //Recibir como Parametro
		parametros.put("NOMBRE", 			comprobante.getReceptor().getNombre()); //Recibir como Parametro
		parametros.put("RFC", 				comprobante.getReceptor().getRfc());
		parametros.put("FECHA", 			comprobante.getFecha().getTime());
		parametros.put("NFISCAL", 			comprobante.getSerie()+" - "+comprobante.getFolio());		
		parametros.put("IMPORTE", 			comprobante.getSubTotal()); 
		parametros.put("IMPUESTO", 			comprobante.getImpuestos().getTotalImpuestosTrasladados()); 
		parametros.put("TOTAL", 			comprobante.getTotal()); 
		parametros.put("DIRECCION", 		CFDUtils.getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()));
		
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
		
		//Datos tomado de la aplicacion
		
		parametros.put("CARGO_ID", 			nota.getId());
		parametros.put("IMP_CON_LETRA", 	ImporteALetra.aLetra(nota.getTotalCM()));
		parametros.put("SUCURSAL", 			nota.getSucursal().getId()); 		
		parametros.put("CLAVCTE", 			nota.getClave()); 		
		parametros.put("SUC", 				nota.getSucursal().getClave()); 
		
		parametros.put("TEL", 				nota.getCliente().getTelefonosRow());
		if(nota.getCliente().isDeCredito()){
			parametros.put("D_REV", 			nota.getCliente().getCredito().getDiarevision());
			parametros.put("D_PAG", 			nota.getCliente().getCredito().getDiacobro());
			parametros.put("COB", 				nota.getCliente().getCobrador()!=null?nota.getCliente().getCobrador().getId():null);
			
			parametros.put("PLAZO", 			nota.getCliente().getPlazo());
			parametros.put("FREV", 				nota.getCliente().getCredito().isRevision()?"R":"");
		}
		String prefix="BON ";
		if(nota instanceof NotaDeCreditoDevolucion)
			prefix="DEV ";
		parametros.put("TIPO", 				prefix+nota.getOrigen().toString());
		parametros.put("COMENTARIO_NCRE", 		nota.getComentario());
		parametros.put("ELAB_FAC", 		nota.getLog().getCreateUser());
		parametros.put("PINT_IVA",		MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
		
		if(nota instanceof NotaDeCreditoDevolucion){
			NotaDeCreditoDevolucion ndevo=(NotaDeCreditoDevolucion)nota;
			Devolucion devo=ndevo.getDevolucion();
			Venta venta=devo.getVenta();
			parametros.put("DESCUENTO", 	BigDecimal.valueOf(venta.getDescuentoGlobal()));
			
			if(devo.isTotal()){				
				parametros.put("COMENTARIO_NCRE", 		"Devolución total de la factura: "+venta.getDocumento()+ " Suc: "+venta.getSucursal().getNombre());				
				parametros.put("CORTES", 		venta.getImporteCortes());	
				parametros.put("CARGOS", 		venta.getCargos());
				parametros.put("FLETE", 		venta.getFlete());
				parametros.put("IMPORTE_BRUTO", venta.getImporteBruto());
				parametros.put("SUBTOTAL_2", 	venta.getImporteBruto().subtract(venta.getImporteDescuento()));
				//parametros.put("IMP_DESC", 		venta.getSubTotal2());
				parametros.put("DESCUENTOS", 	venta.getImporteDescuento());
				
			}else{
				parametros.put("COMENTARIO_NCRE","Devolución parcial de la factura: "+venta.getDocumento()+ " Suc: "+venta.getSucursal().getNombre());				
				parametros.put("IMPORTE_BRUTO", devo.getImporteBruto());
				parametros.put("SUBTOTAL_2", 	devo.getImporteBruto().subtract(devo.getImporteDescuento()));
				//parametros.put("IMP_DESC", 		devo.getImporteDescuento());
				parametros.put("DESCUENTOS", 	devo.getImporteDescuento());
			}
		}
		
		return parametros;
	}
	
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static void impripirComprobante(Venta venta,ComprobanteFiscal cf,String destinatario,boolean printPreview){
		cf.loadComprobante();
		String cadenaOriginal=getCadenaOriginal(cf.getComprobanteDocument());
		final EventList<VentaDet> conceptos=GlazedLists.eventList(venta.getPartidas());		
		Map parametros=CFDParametrosUtils.resolverParametros(venta,cf,cadenaOriginal);
		parametros.put("DESTINATARIO", destinatario);
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		
		String ruta="";
		if (parametros.get("EXPEDIDO_DIRECCION")== "SNA")
		{
		  ruta="ventas/FacturaDigitalSEX.jasper";
		}
		else{
			ruta="ventas/FacturaDigital.jasper";
		}
		if(venta.getFecha().before(DateUtil.toDate("23/02/2011"))){
			ruta="ventas/FacturaDigital20110223.jasper";
		}
		
		Resource res = loader.getResource(ReportUtils				
				.toReportesPath(ruta));
				
				System.out.println("Generando impresion de CFD con parametros: "+parametros);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidadEnUnidad","clave","descripcion","producto.kilos","producto.gramos","precio","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","producto.unidad.unidad","ordenp","precioConIva","importeConIva"};
			String[] etiquetas={"CANTIDAD","CLAVE","DESCRIPCION","KXM","GRAMOS","PRECIO","IMPORTE","CORTES_INSTRUCCION","MDV","GRUPO","MDV","UNIDAD","ORDENP","PRECIO_IVA","IMPORTE_IVA"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			//final Comparator c=GlazedLists.beanPropertyComparator(VentaDet.class, "ordenp" );
			//final SortedList sortedList=new SortedList(conceptos,GlazedLists.reverseComparator(forward));
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				ReportDialog dialog=new ReportDialog(jasperViewer,"Factura digital",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFD", ioe);
			return;
		}
		
	}
	
	
	public static ComprobanteFiscal buscarComprobante(String origenId){
		String hql="from ComprobanteFiscal c where c.origen=?";
		return (ComprobanteFiscal)ServiceLocator2.getHibernateTemplate().find(hql,origenId).iterator().next();		
	}
	
	public static String getCadenaOriginal(ComprobanteDocument document){
		return ServiceLocator2.getCFDManager().generarCadenaOriginal(document);
	}
	
	
	public static void imprimirNotaDeCargoElectronica(String notaDeCargoId){
		
		NotaDeCargo nota=buscarNotaInicializada(notaDeCargoId);
		if(nota==null)
			return;
		ComprobanteFiscal cf=buscarComprobante(nota.getId());
		cf.loadComprobante();
		List<Concepto> conceptosArray=Arrays.asList(cf.getComprobante().getConceptos().getConceptoArray());
		final EventList<Concepto> conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(nota,null);
		
		JasperPrint jasperPrint = null;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource(ReportUtils
				.toReportesPath("cxc/NotaCarDigital.jasper"));
		System.out.println("Generando impresion de CFD con parametros: "+parametros);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidad","descripcion","valorUnitario","importe","descripcion"};
			String[] etiquetas={"CANTIDAD","DESCRIPCION","PRECIO","IMPORTE","GRUPO"};
			final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
			
			final EventTableModel tableModel=new EventTableModel(conceptos,tf);
			final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
			jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
			
			JRViewer jasperViewer = new JRViewer(jasperPrint);
			jasperViewer.setPreferredSize(new Dimension(900, 1000));
			ReportDialog dialog=new ReportDialog(jasperViewer,"Nota de cargo digital",false);
			ScreenUtils.locateOnScreenCenter(dialog);
			dialog.open();
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFD", ioe);
			return;
		}		
	}
	
	private static NotaDeCargo buscarNotaInicializada(final String id){
		
		return (NotaDeCargo)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				NotaDeCargo res=(NotaDeCargo)session.get(NotaDeCargo.class, id);
				res.getCliente().getTelefonosRow();
				if(!res.getAplicaciones().isEmpty()){
					res.getAplicaciones().iterator().next();
				}
				return res;
			}
			
		});
		
	}
	
	public static Map resolverParametros(NotaDeCargo nota,ComprobanteFiscal cf){
		if(cf==null)
			cf=buscarComprobante(nota.getId());
		cf.loadComprobante();
		Comprobante comprobante=cf.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
		// Datos tomados del Comprobante fiscal digital XML
		
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado()); //Recibir como Parametro
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); //Recibir como Parametro
		parametros.put("CADENA_ORIGINAL", 	getCadenaOriginal(cf.getComprobanteDocument())); //Recibir como Parametro
		parametros.put("NOMBRE", 			comprobante.getReceptor().getNombre()); //Recibir como Parametro
		parametros.put("RFC", 				comprobante.getReceptor().getRfc());
		parametros.put("FECHA", 			comprobante.getFecha().getTime());
		parametros.put("NFISCAL", 			comprobante.getSerie()+" - "+comprobante.getFolio());		
		parametros.put("IMPORTE", 			comprobante.getSubTotal()); 
		parametros.put("IMPUESTO", 			comprobante.getImpuestos().getTotalImpuestosTrasladados()); 
		parametros.put("TOTAL", 			comprobante.getTotal()); 
		parametros.put("DIRECCION", 		CFDUtils.getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()));
		
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
		
		//Datos tomado de la aplicacion
		
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
		
		return parametros;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				//imprimirNotaDeCargoElectronica("8a8a81c7-2d38a227-012d-38a3b4cc-0001");
				//imprimirNotaDeCargoElectronica("8a8a81c7-2d387ffd-012d-38807cfe-0001");
				imprimirNotaDeCreditoElectronica("8a8a8198-2ce0316a-012c-e09bcf95-001e");
				 System.exit(0);
			}

		});
	}
	
	
}


