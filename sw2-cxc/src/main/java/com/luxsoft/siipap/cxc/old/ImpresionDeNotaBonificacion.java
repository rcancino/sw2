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

import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.model.CantidadMonetaria;

/**
 * {@link HibernateCallback} para leer una nota de credito y salvar el archivo
 * de impresion adecuado para el programa externo IMPRNOTA.BAT
 * Actualiza la fecha de impresion de la nota 
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImpresionDeNotaBonificacion implements HibernateCallback{
	
	private final String id;
	
	protected Logger logger=Logger.getLogger(getClass());
	public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
	public static final DateFormat df2 = new SimpleDateFormat("HH:mm:ss");

	public ImpresionDeNotaBonificacion(String id) {
		this.id = id;
	}

	public Object doInHibernate(Session session) throws HibernateException,SQLException {
		NotaDeCredito nota=(NotaDeCredito)session.load(NotaDeCredito.class, id);
		try {
			imprimir(nota);
			nota.setImpreso(new Date());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No se pudo generar el archivo de impreion para la nota: "+nota.getFolio(),e);
			
		}
		return null;
	}
	
	public  void imprimir(final NotaDeCredito nota) throws Exception {
		
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
		p.println("L" + ";");
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
		final NumberFormat nf=NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		for (Aplicacion det : nota.getAplicaciones()) {
			
			
			String sucursal = String.valueOf(det.getDetalle().getSucursal());
			sucursal = StringUtils.leftPad(sucursal, 2);
			
			String tipo = "";
			tipo = StringUtils.leftPad(tipo, 1);
			
			String factura = String.valueOf(det.getCargo().getDocumento());
			factura = StringUtils.leftPad(factura, 7);
			
			String fiscal="";
			if(det.getCargo()!=null){
				fiscal = String.valueOf(det.getCargo().getNumeroFiscal());				
			}
			fiscal = StringUtils.leftPad(fiscal.trim(), 7);
			CantidadMonetaria devoluciones;
			if(det.getCargo()!=null){
				devoluciones=CantidadMonetaria.pesos(det.getCargo().getDevoluciones().doubleValue());
				devoluciones=devoluciones.divide(1.16);
			}else
				devoluciones=CantidadMonetaria.pesos(0);
			
			String importe;
			if(det.getCargo()!=null){
				importe = nf.format(det.getCargo().getSaldoSinPagos().doubleValue()/1.16);//.toString(); //
				importe = StringUtils.leftPad(importe.trim(), 12);
			}else
				importe = nf.format(det.getImporte().doubleValue());//.toString(); //		
			
			BigDecimal importeActual=det.getCargo().getSaldoSinPagos();			
			importeActual=importeActual.add(det.getImporte());
			
			System.out.println("Importe aplicado: "+det.getImporte());
			System.out.println("Saldo actual: "+importeActual);
			
			double descuento=det.getImporte().doubleValue()
					/importeActual.doubleValue();
			String desc = StringUtils.leftPad(nf.format(descuento*100), 5);
			
			final BigDecimal impN=det.getImporte().abs().divide(BigDecimal.valueOf(1.16), RoundingMode.HALF_EVEN);
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
		
		if(!StringUtils.isEmpty(comentario) && nota.getAplicaciones().size()<=10)
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
	
	
	
	public String calcularDescuento(Aplicacion a){
		try {
			double res=((NotaDeCreditoBonificacion)a.getAbono()).getDescuento();
			return nf.format(res);

		} catch (Exception e) {
			e.printStackTrace();
			return "ERR-%";
		}		
	}
	
	

}
