package com.luxsoft.siipap.pos.ui.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * {@link HibernateCallback} para leer una nota de credito y salvar el archivo
 * de impresion adecuado para el programa externo IMPRNOTA.BAT
 * Actualiza la fecha de impresion de la nota 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImpresionDeNotaDevolucion implements HibernateCallback{
	
	private final String id;
	
	protected Logger logger=Logger.getLogger(getClass());
	public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	public static final DateFormat df2 = new SimpleDateFormat("HH:mm:ss");

	public ImpresionDeNotaDevolucion(String id) {
		this.id = id;
	}

	public Object doInHibernate(Session session) throws HibernateException,SQLException {
		NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)session.load(NotaDeCreditoDevolucion.class, id);
		try {
			imprimir(nota);
			nota.setImpreso(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo generar el archivo de impreion para la nota: "+nota.getFolio(),e);
			
		}
		return null;
	}
	
	public  void imprimir(final NotaDeCreditoDevolucion nota) throws Exception {
		
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		File destDir=new File(getArchivoDeImpresionPath());
		if(!destDir.exists()){
			destDir.mkdir();
		}
		//File target=new File(destDir,"IMPRNOTA"+ ".txt");		
		File target=new File("IMPRNOTA"+ ".txt");
		FileOutputStream writer = new FileOutputStream(target);
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(writer,"ASCII"));
		PrintWriter p = new PrintWriter(w);
		
		p.println("<HEAD>");
		p.println(nota.getCliente().getCuentaContable() + ";");
		p.println("J"+";");
		final String numero=String.valueOf(nota.getFolio());
		p.println(numero+ ";");
		p.println(nota.getClave() + ";");
		p.println(nota.getCliente().getNombre().trim() + ";");		
		p.println(nota.getCliente().getRfc() + ";");
		String msg = "{0} {1} {2} {3};";
		
		p.println(MessageFormat.format(msg, nota.getCliente().getDireccionFiscal().getCalle(), nota
				.getCliente().getDireccionFiscal().getColonia(), nota.getCliente().getDireccionFiscal().getEstado(),
				nota.getCliente().getDireccionFiscal().getCp()));
		p.println(df.format(nota.getFecha()) + ";");
		
		
		p.println("<DETALLE>");
		insertarDetalleParaDevolucion(nota, p);		
		
		int diaRevision=0;
		int diaPago=0;
		if( (nota.getCliente()!=null) && (nota.getCliente().getCredito()!=null)){
			diaRevision=nota.getCliente().getCredito().getDiarevision();
			diaPago=nota.getCliente().getCredito().getDiacobro();
		}
		
		p.println("<TOTALES>");		
		p.println(df2.format(new Date()) + ";");
		p.println("REV" + ";");
		p.println(diaRevision + ";");
		p.println("PAG" + ";");
		p.println(diaPago + ";");
		p.println("AGE" + ";");
		p.println(nota.getCliente().getVendedor() + ";");
		p.println("COB" + ";");
		p.println(nota.getCliente().getCobrador() + ";");	
		
		CantidadMonetaria total=CantidadMonetaria.pesos(nota.getTotal().doubleValue());
		CantidadMonetaria iva=CantidadMonetaria.pesos(nota.getImpuesto().doubleValue());
		
		p.println(nf.format(nota.getImporte().doubleValue()) + ";");		
		p.println("("+ImporteALetra.aLetra(total.abs())+");");
		p.println(nf.format(iva.abs().amount().doubleValue()) + ";");
		p.println(nf.format(total.abs().amount().doubleValue()));
		p.flush();
		p.close();
		writer.flush();
		writer.close();
		
	}
	
	public static void insertarDetalleParaDevolucion(final NotaDeCreditoDevolucion nota,final PrintWriter p){
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		final NumberFormat nf2=NumberFormat.getNumberInstance();
		
		final NumberFormat nf3=NumberFormat.getNumberInstance();
		nf3.setMinimumFractionDigits(1);
		Venta factura=nota.getDevolucion().getVenta();//(Venta)nota.getAplicaciones().get(0).getCargo();
		
		String comentario=nota.getDevolucion().isTotal()?"DEVOLUCION TOTAL ":"DEVOLUCION PARCIAL";
			//nota.getComentario()!=null?nota.getComentario():"";
		
		p.println("   "+comentario+" DE LA FACTURA: "+factura.getDocumento()+"/"+factura.getNumeroFiscal()+";"); 
				
		double subtotal=nota.getImporte().doubleValue();
		
		CantidadMonetaria cortesImpM=CantidadMonetaria.pesos(nota.getDevolucion().getImporteCortes().doubleValue());
		final double cortes=cortesImpM!=null?cortesImpM.amount().doubleValue():0;
		p.println("                                            "+"Cortes:"+StringUtils.leftPad(nf.format(cortes),13)+";");
		
		p.println("                                              SUMA:"+StringUtils.leftPad(nf.format(subtotal),13)+";");
		
		
		//final double descuento=nota.getDevolucion().getDescuentoNeto();
		final double descuento=nota.getDescuento();
		final double subTotalCortes=subtotal*(descuento/100);
		NumberFormat dnf=NumberFormat.getNumberInstance();
		dnf.setMaximumIntegerDigits(2);
		dnf.setMaximumFractionDigits(2);
		dnf.setMinimumFractionDigits(2);
		
		
		
		
		if(descuento!=0){
			String desc=StringUtils.leftPad(" "+dnf.format(descuento*-1),7);			
			p.println("                                            "+desc+StringUtils.leftPad(nf.format(subTotalCortes*-1),13)+";");
		}
		
		
	}
	
	public static void insertarDetalleParaDevolucion_BAK(final NotaDeCreditoDevolucion nota,final PrintWriter p){
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		final NumberFormat nf2=NumberFormat.getNumberInstance();
		
		final NumberFormat nf3=NumberFormat.getNumberInstance();
		nf3.setMinimumFractionDigits(1);
		Venta factura=nota.getDevolucion().getVenta();//(Venta)nota.getAplicaciones().get(0).getCargo();
		
		String comentario=nota.getDevolucion().isTotal()?"DEVOLUCION TOTAL ":"DEVOLUCION PARCIAL";
			//nota.getComentario()!=null?nota.getComentario():"";
		
		p.println("   "+comentario+" DE LA FACTURA: "+factura.getDocumento()+"/"+factura.getNumeroFiscal()+";"); 
				
		double subtotal=0;
		for(DevolucionDeVenta det:nota.getDevolucion().getPartidas()){
			
			//if(det.getNota().getId()==nota.getId()){
				
				String cantidad=nf2.format(det.getCantidad());
				String desc=det.getProducto().getDescripcion();
				String kilos=StringUtils.leftPad(nf3.format(det.getProducto().getKilos()),5);
				String precio=nf.format(det.getVentaDet().getPrecio());
				double imp=det.getVentaDet().getPrecio().doubleValue()*(det.getCantidad()/det.getProducto().getUnidad().getFactor());
				
				String importe=nf.format(imp);
				//String importe=nf.format(det.getImporte());
				
				String c1=StringUtils.leftPad(String.valueOf(cantidad),10);
				//String c2=StringUtils.rightPad(desc.substring(0, 23),23);				
				String c2=StringUtils.rightPad(StringUtils.left(desc, 23),23);
				//String c3=StringUtils.leftPad(kilos,5);
				String c4=StringUtils.leftPad(String.valueOf(precio),10);
				String c5=StringUtils.leftPad(String.valueOf(importe),12);
				
				String m2 = "{0} {1} {2} {3} {4};";
				p.println(MessageFormat.format(m2
						,c1
						,c2
						,kilos
						,c4
						,c5
						)
					);
				subtotal+=imp;
			//}			
			
		}
		
		p.println("                                              SUMA:"+StringUtils.leftPad(nf.format(subtotal),13)+";");
		
		
		//final double descuento=nota.getDevolucion().getDescuentoNeto();
		final double descuento=nota.getDescuento();
		final double subTotalCortes=subtotal*(descuento/100);
		NumberFormat dnf=NumberFormat.getNumberInstance();
		dnf.setMaximumIntegerDigits(2);
		dnf.setMaximumFractionDigits(2);
		dnf.setMinimumFractionDigits(2);
		
		//final CantidadMonetaria cortesImpM=nota.getDevolucion().getImporteCortes();
		CantidadMonetaria cortesImpM=CantidadMonetaria.pesos(nota.getDevolucion().getImporteCortes().doubleValue());
		final double cortes=cortesImpM!=null?cortesImpM.amount().doubleValue():0;
		p.println("                                            "+"Cortes:"+StringUtils.leftPad(nf.format(cortes),13)+";");
		
		if(descuento!=0){
			String desc=StringUtils.leftPad(" "+dnf.format(descuento*-1),7);			
			p.println("                                            "+desc+StringUtils.leftPad(nf.format(subTotalCortes*-1),13)+";");
		}
		
		
	}
	
	
	static NumberFormat nf=NumberFormat.getPercentInstance();
	static{
		DecimalFormat df=(DecimalFormat)nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumIntegerDigits(2);
	}
	
	public String calcularDescuento(Aplicacion a){
		try {
			double res=a.getDescuentoAplicado();
			return nf.format(res);

		} catch (Exception e) {
			e.printStackTrace();
			return "ERR-%";
		}		
	}
	
	public static String getArchivoDeImpresionPath(){
		String path=System.getProperty("impresion.notas.path", "C:\\PRUEBAS\\");
		return path;
	}
	
	

}
