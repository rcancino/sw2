package com.luxsoft.sw3.cfd;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import mx.gob.sat.cfd.x2.TUbicacion;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Emisor;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.old.ImporteALetra;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;

public class CFDParametrosUtils {
	
	public static Map resolverParametros(Venta venta,ComprobanteFiscal cf,String cadenaOriginal){
		Assert.notNull(cf,"El comprobante fiscal no puede ser nulo");
		Assert.isTrue(StringUtils.isNotBlank(cadenaOriginal),"Falta la cadena original");
		cf.loadComprobante();
		Comprobante comprobante=cf.getComprobante();
		Map<String, Object> parametros = new HashMap<String, Object>();
		
// Datos tomados del Comprobante fiscal digital XML
		
		
		parametros.put("FOLIO", 			comprobante.getSerie()+"-"+comprobante.getFolio());
		parametros.put("ANO_APROBACION", 	comprobante.getAnoAprobacion());
		parametros.put("NO_APROBACION", 	comprobante.getNoAprobacion().intValue());
		parametros.put("NUM_CERTIFICADO", 	comprobante.getNoCertificado()); //Recibir como Parametro
		parametros.put("SELLO_DIGITAL", 	comprobante.getSello()); //Recibir como Parametro
		parametros.put("CADENA_ORIGINAL", 	cadenaOriginal); //Recibir como Parametro
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
		parametros.put("KILOS", 		venta.getKilos());
				
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
		parametros.put("MONEDA", 		venta.getMoneda().getCurrencyCode());
		return parametros;
	}
	
	public static Venta buscarVenta(String ventaId){
		return ServiceLocator2.getVentasManager().buscarVentaInicializada(ventaId);
	}

}
