package com.luxsoft.sw3.cfd;

import java.awt.Dimension;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.uif.util.ScreenUtils;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.pos.ui.utils.ImporteALetra;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Pedido.FormaDeEntrega;

public class CFDPrintServices {
	
	//private static String cfdFacturaReportPath;
	/*
	public static String getCFDFacturaPath(){
		if(cfdFacturaReportPath==null){
			cfdFacturaReportPath=System.getProperty("cfd.factura.path","file:c:/sw3/cfd/formatos/FacturaDigital.jasper");
		}
		return cfdFacturaReportPath;
	}*/
	
	private static void imprimirFacturaElectronica(String facturaId,boolean printPreview,String... destinatarios){
		Venta venta=buscarVenta(facturaId);
		for(String destinatario:destinatarios){
			impripirComprobante(venta,null,destinatario,printPreview);
		}
	}
	
	/**
	 * Imprime las copias necesarias para el tipo de venta indicado
	 * Nota: La impresion es directa a la impresora sin previo
	 * 
	 * @param venta inicializada
	 */
	public static void imprimirFacturaEnMostrador(Venta venta,ComprobanteFiscal cf){
		if(cf==null)
			cf=Services.getInstance().getComprobantesDigitalManager().generarComprobante(venta);
		String[] tantos;
		if(venta.getOrigen().equals(OrigenDeOperacion.CRE)){
			//tantos=new String[]{"CLIENTE","ARCHIVO","CONSECUTIVO FISCAL"};
			tantos=new String[]{"CLIENTE","ARCHIVO"};
		}else
			//tantos=new String[]{"CLIENTE","ARCHIVO","CONSECUTIVO FISCAL"};
			tantos=new String[]{"CLIENTE","ARCHIVO"};
		for(String destino:tantos){
			CFDPrintServices.impripirComprobante(venta, cf, destino, false);
		}
	}
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static void impripirComprobante(Venta venta,ComprobanteFiscal cf,String destinatario,boolean printPreview){
		final EventList<VentaDet> conceptos=GlazedLists.eventList(venta.getPartidas());
		Map parametros=resolverParametros(venta,cf);
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
		
		//String path="file:z:/Reportes_MySQL/"+ruta;
		Resource res = loader.getResource(ReportUtils.toReportesPath(ruta));
		//Resource res = loader.getResource(path);
				
				System.out.println("Generando impresion de CFD con parametros: "+parametros+ " \nruta: "+res);
		try {
			java.io.InputStream io = res.getInputStream();
			String[] columnas= {"cantidadEnUnidad","clave","descripcion","kilosMillar","producto.gramos","precio","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","producto.unidad.unidad","ordenp","precioConIva","importeConIva"};
			//String[] columnas= {"cantidadFiscal","clave","descripcion","producto.kilos","producto.gramos","precioUnitarioFiscal","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","unidadFiscal.label","ordenp","precioConIva","importeConIva"};
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
	
	
	public static Map resolverParametros(Venta venta,ComprobanteFiscal cf){
		if(cf==null)
			cf=buscarComprobante(venta.getId());
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
		//parametros.put("IMPORTE", 			comprobante.getSubTotal());
		parametros.put("IMPORTE", 			venta.getImporteBruto().subtract(venta.getImporteDescuento()));
		parametros.put("IMPUESTO", 			comprobante.getImpuestos().getTotalImpuestosTrasladados()); 
		parametros.put("TOTAL", 			comprobante.getTotal()); 
		parametros.put("DIRECCION", 		CFDUtils.getDireccionEnFormatoEstandar(comprobante.getReceptor().getDomicilio()));
		parametros.put("CUENTA", 		comprobante.getNumCtaPago());
		parametros.put("METODO_PAGO", 		comprobante.getMetodoDePago());
		System.out.println("Metodo de pago: "+comprobante.getMetodoDePago());
		System.out.println("Cuenta de pago: "+comprobante.getNumCtaPago());
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
				
		/*
		if(venta.getPedido()!=null){
			parametros.put("ENVIO", 		venta.getPedido().getEntrega().equals(FormaDeEntrega.LOCAL)?"PASAN":"ENVIO");			
			parametros.put("PEDIDO", 		venta.getPedido().getFolio()); 			
			parametros.put("IP", 			venta.getPedido().getAddresLog().getCreatedIp());
			parametros.put("ELAB_VTA",		venta.getPedido().getLog().getCreateUser() ); 			
			parametros.put("KILOS", 		venta.getKilos());
		if(venta.getPedido().getInstruccionDeEntrega()!=null)
			parametros.put("DIR_ENTREGA", 	venta.getPedido().getInstruccionDeEntrega().oneLineString()); // venta "pedido" en ofi no se vincula
			
		}
		*/
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
		
		if (emisor.getExpedidoEn() != null)
		{
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
		
		return parametros;
	}
	
	public static Venta buscarVenta(String ventaId){
		return Services.getInstance().getFacturasManager().buscarVentaInicializada(ventaId);
	}
	
	
	public static ComprobanteFiscal buscarComprobante(String origenId){
		String hql="from ComprobanteFiscal c where c.origen=?";
		return (ComprobanteFiscal)Services.getInstance().getHibernateTemplate().find(hql,origenId).iterator().next();		
	}
	
	public static String getCadenaOriginal(ComprobanteDocument document){
		return Services.getInstance().getComprobantesDigitalManager().generarCadenaOriginal(document);
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		//DefaultResourceLoader loader=new DefaultResourceLoader();
		//Resource r=loader.getResource("file:Z:/Reportes_MySQL/ventas/FacturaDigital.jasper");
		//Resource r=loader.getResource("file:C:/CFD/CERT/TACUBA_CFD.KEY");
		//Resource r=loader.getResource("file:C:/CFD/Reportes_MySQL/ventas/FacturaDigital.jasper");
		
		
		
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				imprimirFacturaElectronica(
						"8a8a8161-37e75971-0137-e75c95c1-0001",true,"CLIENTE");
				System.exit(0);
			}

		});
	}
	
	
}


