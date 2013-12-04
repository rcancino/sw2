package com.luxsoft.siipap.tesoreria.movimientos;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.luxsoft.siipap.model.tesoreria.CargoAbono;
import com.luxsoft.siipap.model.tesoreria.FormaDePago;
import com.luxsoft.siipap.model.tesoreria.Requisicion;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.reports.ReportUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;

public class ControladorDeImpresion {
	
	private static  n2t funcion;
	 
	

	
	public static void prepararImpresion(){
		MessageUtils.showMessage("Prepare su impresora de cheques", "Cheques");
	}
	
	public static Requisicion imprimierCheque(final Requisicion req){
		if(req.getFormaDePago().equals(FormaDePago.CHEQUE)){
			final Date time=new Date();
			prepararImpresion();
			boolean res=imprimirCheque(req.getPago());
			req.getPago().setImpreso(time);
			Requisicion bean=ServiceLocator2.getRequisiciionesManager().save(req);
			CargoAbono pago=bean.getPago();
			if(res){
				//req.getPago().setImpreso(time);
				//return ServiceLocator2.getRequisiciionesManager().save(req);
				return bean;
			}else{
				bean=ServiceLocator2.getRequisiciionesManager().cancelarPago(bean);
				bean.registrarPagoDeGastos(pago.getCuenta(), pago.getFecha(), pago.getComentario(),pago.getRfc());
				bean.getPago().setReferencia(nextCheque(pago.getCuenta().getId()));
				//imprimierCheque(req);
				bean=ServiceLocator2.getRequisiciionesManager().save(bean);
				imprimierCheque(bean);
			}
		}
		return null;
	}
	
	private static String nextCheque(Long cuentaId){
		long val=ServiceLocator2.getRequisiciionesManager().nextCheque(cuentaId);
		return String.valueOf(val);
	}
	
	public static boolean imprimirCheque(final CargoAbono pago){
		if(pago.getFormaDePago().equals(FormaDePago.CHEQUE)){
			imprimir(pago);			
			boolean res=MessageUtils.showConfirmationMessage("Fue correcta la impresión del cheque: "+pago.getReferencia(),"Impresión de Cheques");
			return res;
		}
		return false;
	}
	
	public static void imprimir(final CargoAbono pago){
		Map<String, Object>param=new HashMap<String, Object>();
		if(pago.getFormaDePago().equals(FormaDePago.CHEQUE)){
			
			DecimalFormat myFormatter = new DecimalFormat("###,##0.00");
			SimpleDateFormat fecha=new SimpleDateFormat("dd-MMMMM-yyyy");
			
			BigDecimal impl=pago.getImporte().abs();
			funcion=new n2t();
			String let=funcion.convertirLetras(impl.intValue());
			int t=new Integer(pago.getImporte().intValue());
			BigDecimal h=pago.getImporte();
			BigDecimal g=new BigDecimal(t);
			BigDecimal l=h.subtract(g);
			DecimalFormat df=new DecimalFormat("###,##0.00");
			String stramount=df.format(l);
			String res=JOptionPane.showInputDialog("Desea cambiar el número de cheque?",pago.getReferencia());
		if(res!=null)
			pago.setReferencia(StringUtils.substring(res, 0, 100));
		
		/**
		 * Impresion De Cheque para HSBC
		 * 
		 */
		if(pago.getCuenta().getDescripcion().equals("HSBC MEXICO SA")){
				System.out.println("cuenta HSBC MEXICO SA");
				Font hsbc1=new Font("Times new Roman",Font.BOLD+Font.PLAIN,7);
				Font hsbc2=new Font("Times new Roman",Font.BOLD+Font.ITALIC,9);
				PrintJob pj=Toolkit.getDefaultToolkit().getPrintJob(new Frame(), "SCAT", null);	
				Graphics pagina;
				pagina=pj.getGraphics();
				pagina.setFont(hsbc1);
				pagina.setColor(Color.BLACK);
				String date=fecha.format(pago.getFecha());
				pagina.drawString(StringUtils.upperCase(date), 363,19);
				String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
				pagina.drawString(pago.getAFavor(), 105, 47);
				String centavos=stramount.substring(3);
				centavos=StringUtils.leftPad(centavos, 2,'0');
				pagina.drawString("("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")", 90, 75);
				pagina.setFont(hsbc2);
				pagina.drawString(importe, 363, 47);
				pagina.dispose();
				pj.end();
				imprimirPoliza(pago);
			
		}
		
		/**
		 * Impresion De Cheque para Santander
		 * 
		 */
		
		else if(pago.getCuenta().getDescripcion().equals("SANTANDER SERFIN")||pago.getCuenta().getDescripcion().equals("SANTANDER SERFINN")){
			System.out.println("cuenta SANTANDER");
			Font san1=new Font("Times new Roman",Font.BOLD+Font.PLAIN,8);
			Font san2=new Font("Times new Roman",Font.BOLD+Font.PLAIN,9);
			PrintJob pj=Toolkit.getDefaultToolkit().getPrintJob(new Frame(), "SCAT", null);	
			Graphics pagina;
			pagina=pj.getGraphics();
			pagina.setFont(san1);
			pagina.setColor(Color.BLACK);
			String date=fecha.format(pago.getFecha());
			pagina.drawString(pago.getAFavor(), 35, 79);
			pagina.drawString(StringUtils.upperCase(date), 270, 35);
			String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
			String centavos=stramount.substring(3);
			centavos=StringUtils.leftPad(centavos, 2,'0');
			pagina.drawString("("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")", 35, 103);
			pagina.setFont(san2);
			pagina.drawString(importe, 335,79 );
			pagina.dispose();
			pj.end();
			imprimirPoliza(pago);
		}
		
		/**
		 * Impresion De Cheque para Banamex
		 * 
		 */
		else if(pago.getCuenta().getDescripcion().equals("BANAMEX INVERSION")||pago.getCuenta().getDescripcion().equals("BANAMEX SA")){
					System.out.println("cuenta BANAMEX");
					Font ban1=new Font("Times new Roman",Font.BOLD+Font.ITALIC,8);
					Font ban2=new Font("Times new Roman",Font.BOLD+Font.ITALIC,10);
					PrintJob pj=Toolkit.getDefaultToolkit().getPrintJob(new Frame(), "SCAT", null);	
					Graphics pagina;
					pagina=pj.getGraphics();
					pagina.setFont(ban1);
					pagina.setColor(Color.BLACK);
					String date=fecha.format(pago.getFecha());
					pagina.drawString(StringUtils.upperCase(date),325 ,35);
					pagina.drawString(pago.getAFavor(), 36, 70);
					String imp=myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
					String centavos=stramount.substring(3);
					centavos=StringUtils.leftPad(centavos, 2,'0');					
					pagina.drawString("("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")", 36,91);
					pagina.setFont(ban2);
					pagina.drawString(imp,372,78);
					pagina.dispose();
					pj.end();
					imprimirPoliza(pago);
		}
		/**
		 * Impresion De Cheque para Scotia OLD
		 * 
		 */
		else if(pago.getCuenta().getDescripcion().equals("SCOTIABANK")){
					System.out.println("cuenta SCOTIA");
					Font sco1=new Font("Times new Roman",Font.BOLD+Font.PLAIN,9);
					Font sco2=new Font("Times new Roman",Font.BOLD+Font.ITALIC,11);
					PrintJob pj=Toolkit.getDefaultToolkit().getPrintJob(new Frame(), "SCAT", null);	
					Graphics pagina;
					pagina=pj.getGraphics();
					pagina.setFont(sco1);
					pagina.setColor(Color.BLACK);
					String date=fecha.format(pago.getFecha());
					pagina.drawString(pago.getAFavor(), 43, 102);
					pagina.drawString(StringUtils.upperCase(date), 490, 57);
					String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
					String centavos=stramount.substring(3);
					centavos=StringUtils.leftPad(centavos, 2,'0');					
					pagina.drawString("("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")", 43, 126);
					pagina.setFont(sco2);
					pagina.drawString(importe, 487,100 );
					pagina.dispose();
					pj.end();
					imprimirPoliza(pago);
		}
		/**
		 * Impresion de Scotia
		 */
		else if(pago.getCuenta().getDescripcion().equals("SCOTIABANK_BACK")){
			PrinterJob job = PrinterJob.getPrinterJob();
		    PageFormat pf = job.defaultPage();
		     pf.setOrientation(PageFormat.LANDSCAPE);
		     Paper paper=new Paper();
		     double INCHE=72;
		     paper.setSize(INCHE*3, INCHE*8.5);
		     paper.setImageableArea(2, 2, paper.getWidth()-1, paper.getHeight()-1);
		    
		    Book bk = new Book();
		    bk.append(new ChequeScotia(let,pago,stramount), pf);
		    //bk.append(new paintContent(), job.defaultPage(), 1);

		    job.setPageable(bk);
		    job.setJobName("Impresión de SCOTIABANK");
		    if (job.printDialog()) {
		      try {
		        job.print();
		        imprimirPoliza(pago);
		      } catch (PrinterException e) {
		        System.out.println(e);
		      }
		    }
			
		}
		/**
		 * Impresion De Cheque para Bancomer
		 * 
		 */
		else if(pago.getCuenta().getDescripcion().equals("BBVA BANCOMER SA")){
			String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
			String date=fecha.format(pago.getFecha());
			String centavos=stramount.substring(3);
			//String path=ReportUtils.toReportesPath("bancomerUno.jasper");
			
			param.put("FECHA",StringUtils.upperCase(date));
			param.put("IMPORTE",importe);
			param.put("AFAVOR",pago.getAFavor());
			centavos=StringUtils.leftPad(centavos, 2,'0');	
			param.put("IMPLETRA","("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")");
			//ReportUtils.printReport(path, param, true);
			ReportUtils.printReport(ReportUtils.toReportesPath("tesoreria/bancomerUno.jasper"), param,true);
			imprimirPoliza(pago);
		}
			
		}
	}
	
	public static void imprimirPoliza(final CargoAbono pago){
		DecimalFormat myFormatter = new DecimalFormat("###,##0.00");
		BigDecimal impl=pago.getImporte().abs();
		funcion=new n2t();
		String let=funcion.convertirLetras(impl.intValue());
		int t=new Integer(pago.getImporte().intValue());
		BigDecimal h=pago.getImporte();
		BigDecimal g=new BigDecimal(t);
		BigDecimal l=h.subtract(g);
		DecimalFormat df=new DecimalFormat("###,##0.00");
		String stramount=df.format(l);
		String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
		SimpleDateFormat fecha=new SimpleDateFormat("dd-MMMMM-yyyy");
		if(pago.getFormaDePago().equals(FormaDePago.CHEQUE)){
			String date=fecha.format(pago.getFecha());
			MessageUtils.showMessage("Imprimiendo poliza para el cheque: "+pago.getReferencia(),"Impresión de poliza");
			Map<String, Object>parametros=new HashMap<String, Object>();
			parametros.put("PROVEEDOR", pago.getAFavor());
			parametros.put("FECHA",date);
			String centavos=stramount.substring(3);
			centavos=StringUtils.leftPad(centavos, 2,'0');
			parametros.put("IMP_LETRA","("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")");
			parametros.put("IMPORTE",importe);
			parametros.put("BANCO",pago.getCuenta().getDescripcion().toString());
			parametros.put("NUM",pago.getReferencia());
			parametros.put("HECHO_POR",KernellSecurity.instance().getCurrentUserName());
			parametros.put("POLIZA",pago.getCuenta().getClave().toString());
			
			ReportUtils.viewReport(ReportUtils.toReportesPath("tesoreria/PolizaChekeOR.jasper"), parametros);
		}
		
	}

	static class ChequeScotia implements Printable {
		
		private final String let;
		private final CargoAbono pago;
		private final String stramount;
		
		DecimalFormat myFormatter = new DecimalFormat("###,##0.00");
		SimpleDateFormat fecha=new SimpleDateFormat("dd-MMMMM-yyyy");
		
				
		public ChequeScotia(String let, CargoAbono pago, String stramount) {
			super();
			this.let = let;
			this.pago = pago;
			this.stramount = stramount;
		}


		public int print(Graphics pagina, PageFormat pf, int pageIndex) throws PrinterException {

		    Font sco1=new Font("Times new Roman",Font.BOLD+Font.PLAIN,9);
			Font sco2=new Font("Times new Roman",Font.BOLD+Font.ITALIC,11);
			
			//PrintJob pj=Toolkit.getDefaultToolkit().getPrintJob(new Frame(), "SCAT", null);	
			//Graphics pagina;
			//pagina=pj.getGraphics();
			pagina.setFont(sco1);
			pagina.setColor(Color.BLACK);
			String date=fecha.format(pago.getFecha());
			pagina.drawString(pago.getAFavor(), 43, 102);
			pagina.drawString(StringUtils.upperCase(date), 490, 57);
			String importe = myFormatter.format(pago.getImporte().multiply(new BigDecimal(-1)));
			String centavos=stramount.substring(3);
			centavos=StringUtils.leftPad(centavos, 2,'0');					
			pagina.drawString("("+let+" "+"PESOS"+" "+centavos+"/100 MN"+")", 43, 126);
			pagina.setFont(sco2);
			pagina.drawString(importe, 487,100 );
			pagina.dispose();
		    return Printable.PAGE_EXISTS;
		  }
		}
	


}
