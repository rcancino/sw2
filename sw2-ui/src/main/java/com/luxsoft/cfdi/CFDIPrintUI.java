package com.luxsoft.cfdi;

import org.springframework.orm.hibernate3.HibernateTemplate;

import java.awt.Dimension;
import java.util.Date;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JRViewer;

import com.jgoodies.uif.util.ScreenUtils;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDINotaPrintServices;
import com.luxsoft.sw3.cfdi.CFDIPrintServices;
import com.luxsoft.sw3.cfdi.model.CFDI;

public class CFDIPrintUI {
	
	
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static  void impripirComprobante(Venta venta,CFDI cfdi,String destinatario,Date time,HibernateTemplate hibernateTemplate,boolean printPreview){
		try {
			JasperPrint jasperPrint = CFDIPrintServices.impripirComprobante(venta, cfdi, destinatario, printPreview);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			if(venta.getImpreso()==null && (cfdi!=null)){
				venta.setImpreso(time);
				hibernateTemplate.merge(venta);
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
		
		
	}
	
	/**
	 * Permite imprimir el comprobante fiscal tantas veces se requiera
	 * 
	 * @param venta
	 */
	public static  void impripirComprobante(NotaDeCredito nota,CFDI cfdi
			,String destinatario
			,Date time
			,boolean printPreview){
		try {
			JasperPrint jasperPrint = CFDINotaPrintServices.impripirComprobante(nota, cfdi);
			if(printPreview){
				JRViewer jasperViewer = new JRViewer(jasperPrint);
				jasperViewer.setPreferredSize(new Dimension(900, 1000));
				CFDIReportDialog dialog=new CFDIReportDialog(jasperViewer,"Representación impresa CFDI",false);
				ScreenUtils.locateOnScreenCenter(dialog);
				dialog.open();
			}else{
				JasperPrintManager.printReport(jasperPrint, false);
			}
			
		} catch (Exception ioe) {
			ioe.printStackTrace();
			MessageUtils.showError("Error imprimiendo CFDI", ioe);
			return;
		}
		
		
	}
}


