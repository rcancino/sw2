package com.luxsoft.sw3.cfd.services.parche;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.util.Assert;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;




import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;


import com.luxsoft.siipap.service.ServiceLocator2;


import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.CFDParametrosUtils;
import com.luxsoft.sw3.cfd.CFDUtils;


import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.utils.LoggerHelper;

/**
 * Tarea para agrupar los CFD's y su PDF de un cliente y /o periodo
 * 
 * @author Ruben Cancino
 *
 */
public class AgruparComprobantesFiscalesDigitales {
	
	Logger logger=LoggerHelper.getLogger();
	
	public AgruparComprobantesFiscalesDigitales agrupar(String rfc,String dir){
		//String sql="select CFD_ID from sx_cfd where rfc=?";
		String hql="from ComprobanteFiscal c where c.rfc=?  and DATE(CREADO)>=? and DATE(CREADO)<? ";
		//String hql="from ComprobanteFiscal c where c.rfc=? and tipo='NOTA_CARGO' and DATE(CREADO)>='2012-12-01'";
		List<ComprobanteFiscal> cfds=ServiceLocator2.getHibernateTemplate().find(hql
				,new Object[]{rfc,DateUtil.toDate("01/12/2010"),DateUtil.toDate("01/07/2012")});
		logger.info("CFDs a procesar: "+cfds.size());
		List<String> faltantes=new ArrayList<String>();
		for(ComprobanteFiscal cfd:cfds){
			try {
				cfd.loadComprobante();
				//generarXml(cfd, dir);
				if(StringUtils.equals(cfd.getTipo().trim(), "FACTURA")){
					generarPdfVenta(cfd, dir);
				}else if(cfd.getTipo().equals("NOTA_CREDITO")){
					generarPdfNotaDeCredito(cfd,dir);
				}else if(cfd.getTipo().equals("NOTA_CARGO")){
					generarPdfNotaDeCargo(cfd, dir);
				}
			} catch(Exception e){
				logger.info("Error procesando: "+cfd.getId()+"  Causa:"+ExceptionUtils.getRootCauseMessage(e));
				faltantes.add(cfd.getId());
			}
		}
		System.out.println( "XML faltantes: "+faltantes);
		return this;
	}
	
	private void generarXml(ComprobanteFiscal cfd,String destDir) throws IOException{
		File dir=new File(destDir);
		if(!dir.exists())
			dir.mkdir();
		String fileName=cfd.getSerie()+cfd.getFolio();
		//Generar XML
		File xml=new File(dir,fileName+".xml");
		
		cfd.getComprobanteDocument().save(xml);
	}
	
	
	public void generarPdfVenta(ComprobanteFiscal cf,String dir) throws JRException, IOException{
		//logger.info("Generando PDF para CFD tipo Factura: "+cf.getOrigen());
		
		Venta venta=ServiceLocator2.getVentasManager().buscarVentaInicializada(cf.getOrigen());
		
		String cadenaOriginal=CFDUtils.generarCadenaOrignal(cf.getComprobanteDocument());
		final EventList<VentaDet> conceptos=GlazedLists.eventList(venta.getPartidas());		
		Map parametros=CFDParametrosUtils.resolverParametros(venta,cf,cadenaOriginal);
		
		JasperPrint jasperPrint = null;
		String ruta="";
		if (parametros.get("EXPEDIDO_DIRECCION")== "SNA")
		{
		  ruta="ventas/FacturaDigitalSEX.jasper";
		}
		else{
			ruta="ventas/FacturaDigitalCFD.jasper";
		}
		if(venta.getFecha().before(DateUtil.toDate("23/06/2012"))){
			ruta="ventas/FacturaDigitalCFD.jasper";
		}if("2.0".equals(cf.getComprobanteDocument().getComprobante().getVersion())){
			ruta="ventas/FacturaDigitalANTCFD2.jasper";
		}
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource("file:z:/Reportes_MySQL/"+ruta);
		
		java.io.InputStream io = res.getInputStream();
		String[] columnas= {"cantidadEnUnidad","clave","descripcion","producto.kilos","producto.gramos","precio","importe","instruccionesDecorte","producto.modoDeVenta","clave","producto.modoDeVenta","producto.unidad.unidad","ordenp","precioConIva","importeConIva"};
		String[] etiquetas={"CANTIDAD","CLAVE","DESCRIPCION","KXM","GRAMOS","PRECIO","IMPORTE","CORTES_INSTRUCCION","MDV","GRUPO","MDV","UNIDAD","ORDENP","PRECIO_IVA","IMPORTE_IVA"};
		final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
		final EventTableModel tableModel=new EventTableModel(conceptos,tf);
		final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
		jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
		String fileName=cf.getSerie()+cf.getFolio()+".pdf";
		JasperExportManager.exportReportToPdfFile(jasperPrint,dir+fileName);
	}
	
	public  void generarPdfNotaDeCredito(ComprobanteFiscal cfd,String dir) throws Exception{
		
		NotaDeCredito nota=buscarNotaDeCreditoInicializada(cfd.getOrigen());
		if(nota==null)
			return;
		String reporte="NotaDevDigitalCFD.jasper";
		if(nota instanceof NotaDeCreditoBonificacion){
			reporte="NotaBonDigitalCFD.jasper";
		}
		/*
		if("2.01".equals(cfd.getComprobanteDocument().getComprobante().getVersion())){
			reporte="NotaDevDigital.jasper";
			if(nota instanceof NotaDeCreditoBonificacion){
				reporte="NotaBonDigital.jasper";
			}
		}*/
		List<Concepto> conceptosArray=Arrays.asList(cfd.getComprobante().getConceptos().getConceptoArray());
		final EventList<Concepto> conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(nota,cfd);
		
		JasperPrint jasperPrint = null;
		String ruta="cxc/"+reporte;
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource("file:z:/Reportes_MySQL/"+ruta);
		
		//System.out.println("Generando impresion de CFD con parametros: "+parametros);
		java.io.InputStream io = res.getInputStream();
		String[] columnas= {"cantidad","unidad","descripcion","valorUnitario","importe","descripcion"};
		String[] etiquetas={"CANTIDAD","UNIDAD","DESCRIPCION","PRECIO","IMPORTE","GRUPO"};
		final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
		
		final EventTableModel tableModel=new EventTableModel(conceptos,tf);
		final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
		jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);
		
		String fileName=cfd.getSerie()+cfd.getFolio()+".pdf";
		JasperExportManager.exportReportToPdfFile(jasperPrint,dir+fileName);		
	}
	
	private NotaDeCredito buscarNotaDeCreditoInicializada(final String id){
		
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
	
	public  void generarPdfNotaDeCargo(ComprobanteFiscal cfd,String dir) throws Exception{
		
		NotaDeCargo nota=buscarNotaInicializada(cfd.getOrigen());
		Assert.notNull(nota,"Error localizando nota de cargo para CFD: "+cfd.getId());
		cfd.loadComprobante();
		List<Concepto> conceptosArray=Arrays.asList(cfd.getComprobante().getConceptos().getConceptoArray());
		final EventList<Concepto> conceptos=GlazedLists.eventList(conceptosArray);
		Map parametros=resolverParametros(nota,cfd);
		DefaultResourceLoader loader = new DefaultResourceLoader();
		Resource res = loader.getResource("file:z:/Reportes_MySQL/cxc/NotaCarDigitalCFD.jasper");
		//System.out.println("Generando impresion de CFD con parametros: "+parametros);
		java.io.InputStream io = res.getInputStream();
		String[] columnas= {"cantidad","descripcion","valorUnitario","importe","descripcion"};
		String[] etiquetas={"CANTIDAD","DESCRIPCION","PRECIO","IMPORTE","GRUPO"};
		final TableFormat tf=GlazedLists.tableFormat(columnas, etiquetas);
		
		final EventTableModel tableModel=new EventTableModel(conceptos,tf);
		final JRTableModelDataSource tmDataSource=new JRTableModelDataSource(tableModel);
		JasperPrint jasperPrint = JasperFillManager.fillReport(io, parametros,tmDataSource);	
		String fileName=cfd.getSerie()+cfd.getFolio()+".pdf";
		JasperExportManager.exportReportToPdfFile(jasperPrint,dir+fileName);
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
	
	public static Map resolverParametros(NotaDeCredito nota,ComprobanteFiscal cf){
		
		Comprobante comprobante=cf.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado()); //Recibir como Parametro
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); //Recibir como Parametro
		parametros.put("CADENA_ORIGINAL", 	CFDUtils.generarCadenaOrignal(cf.getComprobanteDocument())); //Recibir como Parametro
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
		parametros.put("IMP_CON_LETRA", 	com.luxsoft.sw3.cfd.ImporteALetra.aLetra(nota.getTotalCM()));
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
				parametros.put("DESCUENTOS", 	venta.getImporteDescuento());
				
			}else{
				parametros.put("COMENTARIO_NCRE","Devolución parcial de la factura: "+venta.getDocumento()+ " Suc: "+venta.getSucursal().getNombre());				
				parametros.put("IMPORTE_BRUTO", devo.getImporteBruto());
				parametros.put("SUBTOTAL_2", 	devo.getImporteBruto().subtract(devo.getImporteDescuento()));
				parametros.put("DESCUENTOS", 	devo.getImporteDescuento());
			}
		}
		
		return parametros;
	}
	
	public static Map resolverParametros(NotaDeCargo nota,ComprobanteFiscal cf){
		
		Comprobante comprobante=cf.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
		// Datos tomados del Comprobante fiscal digital XML
		
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado()); //Recibir como Parametro
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); //Recibir como Parametro
		parametros.put("CADENA_ORIGINAL", 	CFDUtils.generarCadenaOrignal(cf.getComprobanteDocument())); //Recibir como Parametro
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
		parametros.put("IMP_CON_LETRA", 	com.luxsoft.sw3.cfd.ImporteALetra.aLetra(nota.getTotalCM()));
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
	
	public static void main(String[] args) {
		new AgruparComprobantesFiscalesDigitales()
		.agrupar("UCI840109JU0","C:\\basura\\union\\");
	}

}
