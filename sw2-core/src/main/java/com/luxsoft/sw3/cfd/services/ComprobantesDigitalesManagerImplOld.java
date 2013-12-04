package com.luxsoft.sw3.cfd.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import mx.gob.sat.cfd.x2.ComprobanteDocument;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Impuestos;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Receptor;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.TipoDeComprobante;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Conceptos.Concepto;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Impuestos.Traslados;
import mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante.Impuestos.Traslados.Traslado;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDet;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.cfd.model.CFDException;
import com.luxsoft.sw3.cfd.model.ComprobanteFiscal;
import com.luxsoft.utils.LoggerHelper;

//@Service("cfdManager")
//@Transactional(propagation=Propagation.SUPPORTS,readOnly=true)
public class ComprobantesDigitalesManagerImplOld extends CFDSupportImpl implements ComprobantesDigitalesManager{
	
	Logger logger=LoggerHelper.getLogger();
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public ComprobanteFiscal generarComprobante(Venta venta) {
		//getHibernateTemplate().update(venta);		
		final ComprobanteDocument document=inicializar();
		Comprobante cfd=document.getComprobante();
		cfd.setFormaDePago("Pago en una sola exhibicion");
		cfd.setTipoDeComprobante(TipoDeComprobante.INGRESO);
		
		//Emisor		
		registrarEmisor(cfd);		
		//Receptor
		registrarReceptor(cfd, venta.getCliente());
		Receptor rec=cfd.getReceptor();
		rec.setNombre(venta.getNombre());
		registrarExpedidoEn(cfd, venta.getSucursal());
		//Conceptos
		generarConceptos(cfd, venta);
		
		// Totales
		
		cfd.setTotal(venta.getTotal());
		cfd.setSubTotal(venta.getImporteBruto());
		Impuestos impuestos=cfd.addNewImpuestos();
		if(venta.getClave().equals("1")){
			
			CantidadMonetaria subTotal=CantidadMonetaria.pesos(venta.getImporteBruto());
			subTotal.multiply(1d+MonedasUtils.IVA.doubleValue());
			cfd.setSubTotal(subTotal.amount());
			//cfd.setSubTotal(venta.getImporteBruto().multiply(multiplicand));			
			CantidadMonetaria  desc=CantidadMonetaria.pesos(venta.getImporteDescuento());
			desc=desc.multiply(1d+MonedasUtils.IVA.doubleValue());
			cfd.setDescuento(desc.amount());
		}else{
			
			impuestos.setTotalImpuestosTrasladados(venta.getImpuesto());
			
			Traslados traslados=impuestos.addNewTraslados();
			Traslado traslado=traslados.addNewTraslado();
			traslado.setImpuesto(Traslado.Impuesto.IVA);
			traslado.setImporte(venta.getImpuesto());
			traslado.setTasa(MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
			cfd.setDescuento(venta.getImporteDescuento());
		}
		
		depuracionFinal(document);
		
		registrarSerieFolio(getSerie(venta), venta.getSucursal(), cfd);		
		
		//Sello digital y datos relacionados con el certificado digital
		registrarSelloDigital(document);
		
		
		ComprobanteFiscal cf=registrarComprobante(document, "FACTURA", venta.getId());
		
		//depuracionFinal(document);
		
		//Validamos antes de salvar
		validarDocumento(document);
		
		String fileName=getDocumentXMLFileName(cfd, venta);
		
		cf=salvar(fileName,document,cf);
		//actualizarOrigen("SX_VENTAS");
		venta.setDocumento(new Long(cf.getFolio()));
		getHibernateTemplate().update(venta);
		return cf;
	}
	
	
	private String getSerie(Venta venta) {
		
		String pattern="{0}{1}{2}";
		String sucursal=venta.getSucursal().getNombre();
		sucursal=StringUtils.substring(sucursal, 0, 2);
		String serie=MessageFormat.format(pattern
				,sucursal
				,"FAC"
				,venta.getOrigen().name()
				);
		return serie;
		/*
		String pattern="QR{0}{1}";
		String serie=MessageFormat.format(pattern
				,"FAC"
				,venta.getOrigen().name()
				);
		return serie;
		*/
	}
	
	/**
	 * Regresa la serie adecuada para la nota de credito
	 *  Por el momento solo se ocupa la rseie TANOTCRE
	 *  
	 * @param nota
	 * @return
	 */
	private String getSerie(NotaDeCredito nota) {
		return "TANOTCRE";
	}
	
	public ComprobanteFiscal cargarComprobante(final Venta venta){
		String hql="from ComprobanteFiscal c where c.origen=?";
		List<ComprobanteFiscal> data=getHibernateTemplate().find(hql,venta.getId());
		return data.isEmpty()?null:data.get(0);
	}
	
	private void generarConceptos(Comprobante cfd, Venta venta){
		Conceptos conceptos=cfd.addNewConceptos();
		for(VentaDet det:venta.getPartidas()){
			Concepto c=conceptos.addNewConcepto();
			//c.setCantidad(new BigDecimal(det.getCantidadEnUnidad()).abs().setScale(3,RoundingMode.HALF_EVEN));
			c.setCantidad(new BigDecimal(det.getCantidadEnUnidad()).multiply(BigDecimal.valueOf(-1)).setScale(3,RoundingMode.HALF_EVEN));
			c.setUnidad(det.getUnidad().getUnidad());
			c.setNoIdentificacion(det.getClave());
			c.setDescripcion(det.getDescripcion());
			c.setValorUnitario(det.getPrecio().setScale(2));
			c.setImporte(det.getImporte().setScale(2));
			if(venta.getClave().equals("1")){
				c.setValorUnitario(det.getPrecioConIva());
				c.setImporte(det.getImporteConIva());
			}
		}
	}
	
	private void generarConceptos(Comprobante cfd, NotaDeCredito nota){
		if(nota instanceof NotaDeCreditoDevolucion){
			NotaDeCreditoDevolucion ndevo=(NotaDeCreditoDevolucion)nota;
			Conceptos conceptos=cfd.addNewConceptos();
			
			List<DevolucionDeVenta> partidas=getHibernateTemplate()
				.find("from DevolucionDeVenta det where det.devolucion.id=?",ndevo.getDevolucion().getId());
			Assert.notEmpty(partidas,"Imposible generar CFD de Devolucion ya que no se encontraron partidas para la devolucion: "+ndevo.getId());
			
			for(DevolucionDeVenta det:partidas){
				Concepto c=conceptos.addNewConcepto();
				c.setCantidad(new BigDecimal(det.getCantidadEnUnidad()).abs().setScale(3,RoundingMode.HALF_EVEN));
				c.setUnidad(det.getUnidad().getUnidad());
				c.setNoIdentificacion(det.getClave());
				c.setDescripcion(det.getDescripcion());
				c.setValorUnitario(det.getPrecio().setScale(2));
				c.setImporte(det.getImporteBruto().setScale(2));
			}
			
		}else if(nota instanceof NotaDeCreditoBonificacion){
			NotaDeCreditoBonificacion bon=(NotaDeCreditoBonificacion)nota;
			Conceptos conceptos=cfd.addNewConceptos();
			if(bon.getConceptos().isEmpty()){
				Concepto c=conceptos.addNewConcepto();
				c.setCantidad(BigDecimal.ONE);
				c.setDescripcion(bon.getComentario());
				c.setValorUnitario(bon.getImporte().setScale(2));
				c.setImporte(bon.getImporte().setScale(2));
			}else{
				
				for(NotaDeCreditoDet det:bon.getConceptos()){
					Concepto c=conceptos.addNewConcepto();
					c.setCantidad(BigDecimal.ONE);
					String pattern="Suc:{0} Docto:{1,number,#######} ({2,date,short})";						
					c.setDescripcion(MessageFormat.format(pattern
							,det.getVenta().getSucursal()
							,det.getVenta().getDocumento()
							,det.getVenta().getFecha()
							)
							);
					
					c.setValorUnitario((det.getImporte().divide((MonedasUtils.IVA.add(new BigDecimal(1))),2,RoundingMode.HALF_EVEN)).setScale(2));
					c.setImporte((det.getImporte().divide((MonedasUtils.IVA.add(new BigDecimal(1))),2,RoundingMode.HALF_EVEN)).setScale(2));
				}
				
			}
			/*
			if(bon.getAplicaciones().isEmpty()){
				Concepto c=conceptos.addNewConcepto();
				c.setCantidad(BigDecimal.ONE);
				c.setDescripcion(bon.getComentario());
				c.setValorUnitario(bon.getImporte().setScale(2));
				c.setImporte(bon.getImporte().setScale(2));
			}else{
				Date primeraAplicacion=bon.getPrimeraAplicacion();
				Date fecha=bon.getFecha();
				if(DateUtils.isSameDay(primeraAplicacion, fecha)){
					for(Aplicacion a:bon.getAplicaciones()){
						Concepto c=conceptos.addNewConcepto();
						c.setCantidad(BigDecimal.ONE);
						String pattern="Suc:{0} Docto:{1,number,#######} ({2,date,short})";						
						c.setDescripcion(MessageFormat.format(pattern
								,a.getDetalle().getSucursal()
								,a.getDetalle().getDocumento()
								,a.getDetalle().getFechaCargo()
								)
								);
						
						c.setValorUnitario((a.getImporte().divide((MonedasUtils.IVA.add(new BigDecimal(1))),2,RoundingMode.HALF_EVEN)).setScale(2));
						c.setImporte((a.getImporte().divide((MonedasUtils.IVA.add(new BigDecimal(1))),2,RoundingMode.HALF_EVEN)).setScale(2));
					}
				}else{
					
					Concepto c=conceptos.addNewConcepto();
					c.setCantidad(BigDecimal.ONE);
					String desc=bon.getComentario();
					if(StringUtils.isBlank(desc))
						throw new RuntimeException("Este tipo de Nota de credito por bonificaciön requiere de un comentario");
					c.setDescripcion(desc);
					c.setValorUnitario(bon.getImporte().setScale(2));
					c.setImporte(bon.getImporte().setScale(2));
						
				}
				
			}*/
		}
		
	}
	
		
	/*
	 * (non-Javadoc)
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#getDocumentXMLFileName(mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante, java.lang.Object)
	 */
	public String getDocumentXMLFileName(Comprobante cfd,Object source){
		if(source instanceof Venta){
			Venta venta=(Venta)source;
			String folio=cfd.getFolio();
			folio=StringUtils.leftPad(folio, 7, '0');
			String pattern="{0}{1}{2}";
			String name=MessageFormat.format(pattern
					,venta.getSucursal().getId()
					,venta.getOrigen().name()
					,folio
					);
			return name;
		}else if(source instanceof NotaDeCredito){
			NotaDeCredito nota=(NotaDeCredito)source;
			String folio=cfd.getFolio();
			folio=StringUtils.leftPad(folio, 7, '0');
			String pattern="NOTA_{0}{1}{2}";
			String name=MessageFormat.format(pattern
					,nota.getSucursal().getId()
					,nota.getOrigen().name()
					,folio
					);
			return name;
		}else if(source instanceof NotaDeCargo){
			String folio=cfd.getFolio();
			folio=StringUtils.leftPad(folio, 7, '0');
			String pattern="NOTA_CARGO_{0}";
			String name=MessageFormat.format(pattern
					,folio
					);
			return name;
		}else
			throw new CFDException("Tipo de objeto no es soportado para CFD");
	}
	
	/*
	 * Se sobre escribe para anotar la transaccion
	 * 
	 * 
	 * @see com.luxsoft.sw3.cfd.services.CFDSupport#registrarSerieFolio(java.lang.String, mx.gob.sat.cfd.x2.ComprobanteDocument.Comprobante)
	 * 
	
	@Transactional(propagation=Propagation.MANDATORY)
	public void registrarSerieFolio(String serie,Sucursal sucursal,Comprobante cfd) {
		super.registrarSerieFolio(serie, sucursal, cfd);
	}
	 */
	
	public ComprobanteFiscal cancelarComprobante(Venta venta) {
		
		return null;
	}
	
	

	@Transactional(propagation=Propagation.REQUIRED)
	public ComprobanteFiscal generarComprobante(NotaDeCredito nota) {
		final ComprobanteDocument document=inicializar();
		Comprobante cfd=document.getComprobante();
		cfd.setFormaDePago("Pago en una sola exhibicion");
		cfd.setTipoDeComprobante(TipoDeComprobante.EGRESO);
				
		registrarEmisor(cfd);
		
		//Receptor
		registrarReceptor(cfd, nota.getCliente());
		Receptor rec=cfd.getReceptor();
		rec.setNombre(nota.getNombre());
		registrarExpedidoEn(cfd, nota.getSucursal());
		
		
		//Conceptos
		generarConceptos(cfd, nota);
		
		// Totales
		
		cfd.setTotal(nota.getTotal());
		cfd.setSubTotal(nota.getImporte());
		Impuestos impuestos=cfd.addNewImpuestos();
		
		//Si es de devolucion
		if(nota instanceof NotaDeCreditoDevolucion){
			NotaDeCreditoDevolucion ndev=(NotaDeCreditoDevolucion)nota;
			Venta venta=ndev.getDevolucion().getVenta();			
			if(ndev.getDevolucion().isTotal()){
				cfd.setSubTotal(venta.getImporteBruto());
				cfd.setDescuento(venta.getImporteBruto().subtract(venta.getImporteDescuento()));
			}
			else{
				cfd.setSubTotal(ndev.getDevolucion().getImporteBruto());
				cfd.setDescuento(ndev.getDevolucion().getImporteDescuento());
			}
		}
		if(nota.getClave().equals("1")){			
			cfd.setSubTotal(cfd.getTotal());
		}else{			
			impuestos.setTotalImpuestosTrasladados(nota.getImpuesto());			
			Traslados traslados=impuestos.addNewTraslados();
			Traslado traslado=traslados.addNewTraslado();
			traslado.setImpuesto(Traslado.Impuesto.IVA);
			traslado.setImporte(nota.getImpuesto());
			traslado.setTasa(MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
		}		
		depuracionFinal(document);		
		Sucursal oficinas=(Sucursal)getHibernateTemplate().get(Sucursal.class,1L);
		String tipoSerie;
		OrigenDeOperacion origen=nota.getOrigen();
		switch (origen) {
		case CRE:
			tipoSerie=System.getProperty("cfd.ncredito_cre.serie", "TANOTCRE");
			break;
		case CAM:
			tipoSerie=System.getProperty("cfd.ncredito_cam.serie", "TANOTCAM");
			break;
		case MOS:
			tipoSerie=System.getProperty("cfd.ncredito_mos.serie", "TANOTMOS");
			break;
		default:
			tipoSerie=System.getProperty("cfd.ncredito_cre.serie", "TANOTCRE");
			break;
		}
		//String notaCargoSerie=System.getProperty("cfd.ncredito.serie", "TANOTCRE");
		registrarSerieFolio(tipoSerie, oficinas, cfd);		
		
		//Sello digital y datos relacionados con el certificado digital
		registrarSelloDigital(document);		
		ComprobanteFiscal cf=registrarComprobante(document, "NOTA_CREDITO", nota.getId());
		
		//Validamos antes de salvar
		validarDocumento(document);
		
		String fileName=getDocumentXMLFileName(cfd, nota);
		
		cf=salvar(fileName,document,cf);
		nota.setFolio(new Integer(cf.getFolio()));
		if(origen.equals(OrigenDeOperacion.MOS)){
			nota.setReplicado(null);
		}else
			getHibernateTemplate().update(nota);
		return cf;
	}
	
	public ComprobanteFiscal cargarComprobante(final NotaDeCredito cargo){
		String hql="from ComprobanteFiscal c where c.origen=?";
		List<ComprobanteFiscal> data=getHibernateTemplate().find(hql,cargo.getId());
		return data.isEmpty()?null:data.get(0);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public ComprobanteFiscal generarComprobante(final NotaDeCargo notaDeCargo){
		final ComprobanteDocument document=inicializar();
		Comprobante cfd=document.getComprobante();
		cfd.setFormaDePago("Pago en una sola exhibicion");
		cfd.setTipoDeComprobante(TipoDeComprobante.INGRESO);
				
		registrarEmisor(cfd);
		
		//Receptor
		registrarReceptor(cfd, notaDeCargo.getCliente());
		Receptor rec=cfd.getReceptor();
		rec.setNombre(notaDeCargo.getNombre());
		registrarExpedidoEn(cfd, notaDeCargo.getSucursal());
		
		
		//Conceptos
		generarConceptos(cfd, notaDeCargo);
		
		// Totales
		
		cfd.setTotal(notaDeCargo.getTotal());
		cfd.setSubTotal(notaDeCargo.getImporte());
		Impuestos impuestos=cfd.addNewImpuestos();
		if(notaDeCargo.getClave().equals("1")){			
			cfd.setSubTotal(cfd.getTotal());
		}else{
			
			impuestos.setTotalImpuestosTrasladados(notaDeCargo.getImpuesto());
			
			Traslados traslados=impuestos.addNewTraslados();
			Traslado traslado=traslados.addNewTraslado();
			traslado.setImpuesto(Traslado.Impuesto.IVA);
			traslado.setImporte(notaDeCargo.getImpuesto());
			traslado.setTasa(MonedasUtils.IVA.multiply(BigDecimal.valueOf(100)));
			
		}
		
		depuracionFinal(document);
		
		Sucursal oficinas=(Sucursal)getHibernateTemplate().get(Sucursal.class,1L);
		String tipoSerie;
		OrigenDeOperacion origen=notaDeCargo.getOrigen();
		switch (origen) {
		case CRE:
			tipoSerie=System.getProperty("cfd.ncargo_cre.serie", "TACARCRE");
			break;
		case CAM:
			tipoSerie=System.getProperty("cfd.ncargo_cam.serie", "TACARCAM");
			break;
		case MOS:
			tipoSerie=System.getProperty("cfd.ncargo_mos.serie", "TACARMOS");
			break;
		default:
			tipoSerie=System.getProperty("cfd.ncargo_cre.serie", "TACARCRE");
			break;
		}
		//String notaCargoSerie=System.getProperty("cfd.ncargo.serie", "TACARCRE");
		registrarSerieFolio(tipoSerie, oficinas, cfd);		
		
		//Sello digital y datos relacionados con el certificado digital
		registrarSelloDigital(document);
		
		ComprobanteFiscal cf=registrarComprobante(document, "NOTA_CARGO", notaDeCargo.getId());
		
		//Validamos antes de salvar
		validarDocumento(document);
		
		String fileName=getDocumentXMLFileName(cfd, notaDeCargo);
		
		cf=salvar(fileName,document,cf);
		//actualizarOrigen("SX_VENTAS");
		notaDeCargo.setDocumento(new Long(cf.getFolio()));
		getHibernateTemplate().update(notaDeCargo);
		return cf;
		
	}
	
	private void generarConceptos(Comprobante cfd, NotaDeCargo nota){
		Conceptos conceptos=cfd.addNewConceptos();
		if(nota.getConceptos().isEmpty()){
			Concepto c=conceptos.addNewConcepto();
			c.setCantidad(BigDecimal.ONE);
			c.setDescripcion(nota.getComentario());
			c.setValorUnitario(nota.getImporte().setScale(2));
			c.setImporte(nota.getImporte().setScale(2));
		}else{
			for(NotaDeCargoDet det:nota.getConceptos()){
				Concepto c=conceptos.addNewConcepto();
				c.setCantidad(BigDecimal.ONE);
				c.setDescripcion(det.getComentario());
				c.setValorUnitario(det.getImporte().setScale(2));
				c.setImporte(det.getImporte().setScale(2));
				
			}
		}
		
	}
	
	public ComprobanteFiscal cargarComprobante(final NotaDeCargo cargo){
		String hql="from ComprobanteFiscal c where c.origen=?";
		List<ComprobanteFiscal> data=getHibernateTemplate().find(hql,cargo.getId());
		return data.isEmpty()?null:data.get(0);
	}

	public static void main(String[] args) {
		
		DBUtils.whereWeAre();
		/*
		Venta venta=ServiceLocator2
			.getVentasManager()
			.buscarVentaInicializada("8a8a81eb-2b870086-012b-880205e6-0032");
		
		ComprobantesDigitalesManager manager=ServiceLocator2.getCFDManager();
		manager.generarComprobante(venta);
		*/
		
		final String nota_id="8a8a8198-2ce0316a-012c-e09bcf95-001e";
		ServiceLocator2.getJdbcTemplate().update("DELETE FROM SX_CFD WHERE ORIGEN_ID=?",new Object[]{nota_id});
		final HibernateTemplate template=ServiceLocator2.getHibernateTemplate();
		NotaDeCredito nota=(NotaDeCredito)template.execute(new HibernateCallback(){
			public Object doInHibernate(Session session)	throws HibernateException, SQLException {
				NotaDeCreditoDevolucion nd=(NotaDeCreditoDevolucion)session.get(NotaDeCreditoDevolucion.class, nota_id);
				template.initialize(nd.getDevolucion());
				template.initialize(nd.getDevolucion().getPartidas());
				template.initialize(nd.getDevolucion().getVenta());
				template.initialize(nd.getDevolucion().getVenta().getPartidas());
				return nd;
				
				
			}			
		});
		ServiceLocator2.getCFDManager().generarComprobante(nota);
		
		
			//(NotaDeCredito)ServiceLocator2.getCXCManager().getAbono(nota_id);	
		
	}

	

	

}
