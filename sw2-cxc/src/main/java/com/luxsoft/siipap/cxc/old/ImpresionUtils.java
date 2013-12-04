package com.luxsoft.siipap.cxc.old;

import java.awt.Dialog;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import org.springframework.util.Assert;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoBonificacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;

/**
 * Manda imprimir una nota de credito o un cargo mediante el programa 
 * externo IMPRNOTA.BAT
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImpresionUtils {
	
	private static Logger logger=Logger.getLogger(ImpresionUtils.class);
	
	public static void imprimirNotaGeneral(String id){
		NotaDeCredito nota=(NotaDeCredito)ServiceLocator2.getCXCManager().getAbono(id);
		Assert.isTrue(nota.getFolio()>0,"El folio fiscal debe ser asignado");
		if(nota.getImpreso()!=null){
			String pattern="La nota {0,number,integer}  se imprimió el {1,date,medium} a las {1,time,short} ";
			MessageUtils.showMessage(MessageFormat.format(pattern, nota.getFolio(),nota.getImpreso()), "Impresión de Notas");
			return;
		}
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaDescuento(id));
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			//Process p=r.exec(new String[]{"IMPRNOTA.BAT"},null,new File("C:\\PRUEBAS"));
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirNotaBonificacion(String id){
		NotaDeCreditoBonificacion nota=(NotaDeCreditoBonificacion)ServiceLocator2.getCXCManager().getAbono(id);
		Assert.isTrue(nota.getFolio()>0,"El folio fiscal debe ser asignado");
		if(nota.getImpreso()!=null){
			String pattern="La nota {0,number,integer}  se imprimió el {1,date,medium} a las {1,time,short} ";
			MessageUtils.showMessage(MessageFormat.format(pattern, nota.getFolio(),nota.getImpreso()), "Impresión de Notas");
			return;
		}
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaBonificacion(id));
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirNotaDevolucion(String id,int folio,Date fecha){
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		try {
			ServiceLocator2.getHibernateTemplate()
			.execute(new ImpresionDeNotaDevolucion(id,folio,fecha));
		} catch (org.hibernate.AssertionFailure e) {}
		
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			//Process p=r.exec(new String[]{"IMPRNOTA.BAT"},null,new File("C:\\PRUEBAS"));
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirNotaDevolucionEspecial(String id,int folio,Date fecha){
		
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		try {
			ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaDevolucionEspecial(id,folio,fecha));
		} catch (org.hibernate.AssertionFailure e) {} //Some Hibernate Bug
		
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirNotaDevolucion(String id){
		//NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)ServiceLocator2.getCXCManager().getAbono(id);
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		try {
			ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaDevolucion(id));
		} catch (org.hibernate.AssertionFailure e) {} //Some Hibernate Bug
		
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			//Process p=r.exec(new String[]{"IMPRNOTA.BAT"},null,new File("C:\\PRUEBAS"));
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void imprimirNotaDeCargo(final String id){
		imprimirNotaDeCargo(id, false);
	}
	
	
	public static void imprimirNotaDeCargo(final String id,boolean especial){
		NotaDeCargo nota=(NotaDeCargo)ServiceLocator2.getCXCManager().getCargo(id);
		int folio=nota.getDocumento().intValue();
		if(nota.getImpreso()==null){
			//folio=ServiceLocator2.getCXCManager().buscarProximaNotaDeCargo();
			String sfolio=JOptionPane.showInputDialog(
					Application.isLoaded()?Application.instance().getMainFrame():null
					, "Folio del documento:"
					,folio					
					);
			if(!StringUtils.isBlank(sfolio))
				folio=NumberUtils.toInt(sfolio);
			else return;
		}
		Assert.isTrue(nota.getDocumento()>0,"El folio fiscal debe ser asignado");
		if(nota.getImpreso()!=null){
			String pattern="La nota de cargo {0,number,integer}  se imprimió el {1,date,medium} a las {1,time,short} ";
			MessageUtils.showMessage(MessageFormat.format(pattern, nota.getDocumento(),nota.getImpreso()), "Impresión de Notas de Cargo");
			return;
		}
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas de cargo");
		
		ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaDeCargo(id,folio));
		logger.info("Ejecutando prograram IMPRNOTA.BAT");
		Runtime r=Runtime.getRuntime();
		try {
			//Process p=r.exec(new String[]{"IMPRNOTA.BAT"},null,new File("C:\\PRUEBAS"));
			String programa="IMPRNOTA.BAT";
			if(especial)
				programa="IMPRNOT2.BAT";
			Process p=r.exec(new String[]{programa});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void reImprimirNotaDeCargo(final String id){
		NotaDeCargo nota=(NotaDeCargo)ServiceLocator2.getCXCManager().getCargo(id);
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas de cargo");		
		ServiceLocator2.getHibernateTemplate().execute(new ImpresionDeNotaDeCargo(id,nota.getDocumento().intValue()));		
		Runtime r=Runtime.getRuntime();
		try {
			//Process p=r.exec(new String[]{"IMPRNOTA.BAT"},null,new File("C:\\PRUEBAS"));
			Process p=r.exec(new String[]{"IMPRNOTA.BAT"});
			@SuppressWarnings("unused")
			int res=p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Habilita una nota de credito para poder ser impresa
	 * 
	 * @param notaId
	 */
	public static void resetearImpresion(final String notaId){
		NotaDeCredito nota=(NotaDeCredito)ServiceLocator2.getCXCManager().getAbono(notaId);
		nota.setImpreso(null);
		ServiceLocator2.getHibernateTemplate().update(nota);
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		//imprimirNotaDevolucionEspecial("8a8a81c7-2821624d-0128-216258fc-0001", 11596, DateUtil.toDate("30/04/2010"));
		//imprimirNotaDevolucionEspecial("8a8a81c7-2821624d-0128-2162590b-0003", 11597, DateUtil.toDate("30/04/2010"));
		imprimirNotaBonificacion("8a8a8197-2863a659-0128-63b53db1-0002");
	}
	
	

}
