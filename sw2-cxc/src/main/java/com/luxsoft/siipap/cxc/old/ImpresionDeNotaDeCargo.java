package com.luxsoft.siipap.cxc.old;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
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


import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * {@link HibernateCallback} para leer una nota de cargo y salvar el archivo
 * de impresion adecuado para el programa externo IMPRNOTA.BAT
 * Actualiza la fecha de impresion de la nota de cargo
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImpresionDeNotaDeCargo implements HibernateCallback{
	
	private final String id;
	private final int folio;
	
	protected Logger logger=Logger.getLogger(getClass());
	public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	public static final DateFormat df2 = new SimpleDateFormat("HH:mm:ss");

	public ImpresionDeNotaDeCargo(String id,int folio) {
		this.id = id;
		this.folio=folio;
	}

	public Object doInHibernate(Session session) throws HibernateException,SQLException {
		NotaDeCargo nota=(NotaDeCargo)session.load(NotaDeCargo.class, id);
		try {
			if(nota.getImpreso()==null){
				nota.setNumeroFiscal(folio);
				nota.setDocumento(new Long(folio));
			}
			imprimir(nota);
			if(nota.getImpreso()==null){
				nota.setImpreso(new Date());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo generar el archivo de impreion para la nota: "+nota.getDocumento(),e);
			
		}
		return null;
	}
	
	public  void imprimir(final NotaDeCargo nota) throws Exception {
		
		File destDir=new File("C:\\PRUEBAS\\");
		if(!destDir.exists()){
			destDir.mkdir();
		}
		//File target=new File(destDir,"IMPRNOTA"+ ".txt");		
		File target=new File("IMPRNOTA"+ ".txt");
		FileOutputStream writer = new FileOutputStream(target);
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(writer,"ASCII"));
		PrintWriter p = new PrintWriter(w);
		p.println("<HEAD>");
		p.println(nota.getCliente().getCuentaContable()!=null?nota.getCliente().getCuentaContable()+ ";":"" + ";");
		p.println("M" + ";");
		final String numero=nota.getDocumento()!=0?String.valueOf(nota.getDocumento()):"ND";
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
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		for (NotaDeCargoDet det : nota.getConceptos()) {			
			
			String sucursal = String.valueOf(det.getVenta().getSucursal().getClave());
			sucursal = StringUtils.leftPad(sucursal, 2);
			
			String tipo = "";
			tipo = StringUtils.leftPad(tipo, 1);
			
			String factura = String.valueOf(det.getVenta().getDocumento());
			factura = StringUtils.leftPad(factura, 7);
			
			String fiscal="";
			if(det.getVenta()!=null){
				fiscal = String.valueOf(det.getVenta().getNumeroFiscal());				
			}
			fiscal = StringUtils.leftPad(fiscal.trim(), 7);
			CantidadMonetaria devoluciones;
			devoluciones=CantidadMonetaria.pesos(det.getVenta().getDevoluciones().doubleValue());
			devoluciones=devoluciones.divide(1.15);
			
			String importe;
			importe = nf.format(det.getVenta().getImporte().subtract(devoluciones.amount()).abs().doubleValue());//.toString(); //
			importe = StringUtils.leftPad(importe.trim(), 12);
					
			
			//String desc = nf.format(Math.abs(det.getDescuento()));
			String desc = StringUtils.leftPad(calcularDescuento(det), 5);
			//final BigDecimal impN=det.getImporte().abs().divide(BigDecimal.valueOf(1.15), RoundingMode.HALF_EVEN);
			final BigDecimal impN=det.getImporte().abs();
			String impNota = nf.format(impN.doubleValue())+ ";";
			impNota = StringUtils.leftPad(impNota.trim(), 12);
			
			
			String m2 = "{0} {1}  {2}-{3}  {4}  {5}  {6}";
			p.println(MessageFormat.format(m2
					,sucursal
					,tipo
					,factura
					,fiscal
					,importe
					,desc
					,impNota
					)
				);
		}
		String comentario=nota.getComentario();
		
		if(!StringUtils.isEmpty(comentario) && nota.getConceptos().size()<=10)
			p.println(comentario.trim()+";");  //Temporalmente en lo que se libera el parche de Andrés *** 25/10/07 ***
		
		p.println("<TOTALES>");
		p.println(df2.format(new Date()) + ";");
		
		int diaRevision=0;
		int diaPago=0;
		if( (nota.getCliente()!=null) && (nota.getCliente().getCredito()!=null)){
			diaRevision=nota.getCliente().getCredito().getDiarevision();
			diaPago=nota.getCliente().getCredito().getDiacobro();
		}
		p.println("REV" + ";");
		p.println(diaRevision + ";");
		p.println("PAG" + ";");
		p.println(diaPago + ";");
		p.println("AGE" + ";");
		p.println(nota.getCliente().getVendedor() + ";");
		p.println("COB" + ";");
		p.println(nota.getCliente().getCobrador() + ";");
		p.println(nf.format(nota.getImporte().abs().doubleValue()) + ";");
		CantidadMonetaria total=CantidadMonetaria.pesos(nota.getTotal().doubleValue());
		CantidadMonetaria iva=CantidadMonetaria.pesos(nota.getImpuesto().doubleValue());
		p.println("("+ImporteALetra.aLetra(total.abs())+");");
		p.println(nf.format(iva.abs().amount().doubleValue()) + ";");
		p.println(nf.format(nota.getTotal().abs().doubleValue()));
		p.flush();
		p.close();
		writer.flush();
		writer.close();		
		logger.info("Archivo de impresion generado para nota: "+nota.getId());	
		
	}
	
	
	
	
	static NumberFormat nf=NumberFormat.getPercentInstance();
	static{
		DecimalFormat df=(DecimalFormat)nf;
		df.setMinimumFractionDigits(2);
		df.setMaximumIntegerDigits(2);
	}
	
	public String calcularDescuento(NotaDeCargoDet a){
		try {
			double res=a.getCargoAplicado();
			return nf.format(res);

		} catch (Exception e) {
			e.printStackTrace();
			return "ERR-%";
		}		
	}
	
	

}
