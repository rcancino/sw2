package com.luxsoft.siipap.cxc.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
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
public class ImpresionDeNotaDevolucionEspecial implements HibernateCallback{
	
	private final String id;
	
	protected Logger logger=Logger.getLogger(getClass());
	public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	public static final DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
	
	private int folio;
	private Date fecha;
	
	public ImpresionDeNotaDevolucionEspecial(String id,int folio,Date fecha) {
		this.id=id;
		this.fecha=fecha;
		this.folio=folio;
	}


	public Object doInHibernate(Session session) throws HibernateException,SQLException {
		NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)session.load(NotaDeCreditoDevolucion.class, id);
		if(fecha!=null)
			nota.setFecha(fecha);
		if(folio>0)
			nota.setFolio(folio);
		try {
			List<DevolucionDeVenta> partidas=session.createQuery("from DevolucionDeVenta d where d.nota.id=?")
			.setParameter(0, nota.getId())
			.list();
			imprimir(nota,partidas);
			nota.setImpreso(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo generar el archivo de impreion para la nota: "+nota.getFolio(),e);
			
		}
		return null;
	}
	
	public  void imprimir(final NotaDeCreditoDevolucion nota,List<DevolucionDeVenta> partidas) throws Exception {
		
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
		String tt="J";
		if(nota.getOrigen().equals(OrigenDeOperacion.CAM))
			tt="I";
		if(nota.getOrigen().equals(OrigenDeOperacion.MOS))
			tt="H";
		String cta="";
		if(StringUtils.isNotBlank(nota.getCliente().getCuentaContable()))
			cta=nota.getCliente().getCuentaContable();
		p.println(cta+ ";");
		p.println(tt+";");
		final String numero=String.valueOf(nota.getFolio());
		p.println(numero+ ";");
		p.println(nota.getClave() + ";");
		p.println(nota.getCliente().getNombre().trim() + ";");		
		p.println(nota.getCliente().getRfc() + ";");
		String msg = "{0} {1} {2} {3};";
		
		p.println(MessageFormat.format(msg
				, nota.getCliente().getDireccionFiscal().getCalle()
				, nota.getCliente().getDireccionFiscal().getColonia()
				, nota.getCliente().getDireccionFiscal().getEstado()
				,nota.getCliente().getDireccionFiscal().getCp()));
		p.println(df.format(nota.getFecha()) + ";");
		
		
		p.println("<DETALLE>");
		//insertarDetalleParaDevolucion(nota, p);
		insertarDetalleParaDevolucion_BAK(nota,partidas, p);
		
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
	
	
	
	public static void insertarDetalleParaDevolucion_BAK(final NotaDeCreditoDevolucion nota,final List<DevolucionDeVenta> partidas ,final PrintWriter p){
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		
		final NumberFormat nf2=NumberFormat.getNumberInstance();
		
		final NumberFormat nf3=NumberFormat.getNumberInstance();
		nf3.setMinimumFractionDigits(1);
		Venta factura=nota.getDevolucion().getVenta();//(Venta)nota.getAplicaciones().get(0).getCargo();
		
		String comentario=nota.getDevolucion().isTotal()?"DEVOLUCION TOTAL ":"DEVOLUCION PARCIAL";
		
		p.println("   "+comentario+" DE LA FACTURA: "+factura.getDocumento()+"/"+factura.getNumeroFiscal()+" Suc: "+factura.getSucursal().getClave()+";"); 
				
		double subtotal=0;
		
		CantidadMonetaria cortesImpM=CantidadMonetaria.pesos(0);
		
		for(DevolucionDeVenta det:partidas){
				
				String cantidad=nf2.format(det.getCantidad());
				String desc=det.getProducto().getDescripcion();
				String kilos=StringUtils.leftPad(nf3.format(det.getProducto().getKilos()),5);
				String precio=nf.format(det.getVentaDet().getPrecio());
				double imp=det.getImporteNeto().doubleValue();
				
				String importe=nf.format(imp);
				String c1=StringUtils.leftPad(String.valueOf(cantidad),10);
				String c2=StringUtils.rightPad(StringUtils.left(desc, 23),23);
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
				CantidadMonetaria cortes=CantidadMonetaria.pesos(det.getImporteCortesCalculado());
				cortesImpM=cortesImpM.add(cortes);
		}
		
		p.println("                                              SUMA:"+StringUtils.leftPad(nf.format(subtotal),13)+";");		
		final double descuento=nota.getDescuento();
		final double subTotalCortes=subtotal*(descuento/100);
		NumberFormat dnf=NumberFormat.getNumberInstance();
		dnf.setMaximumIntegerDigits(2);
		dnf.setMaximumFractionDigits(2);
		dnf.setMinimumFractionDigits(2);		
		
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
	
	public static String getArchivoDeImpresionPath(){
		String path=System.getProperty("impresion.notas.path", "C:\\PRUEBAS\\");
		return path;
	}

}
