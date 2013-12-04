package com.luxsoft.siipap.pos.ui.utils;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;

import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.services.Services;

/**
 * Manda imprimir una nota de credito o un cargo mediante el programa 
 * externo IMPRNOTA.BAT
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class ImpresionUtils {
	
	private static Logger logger=Logger.getLogger(ImpresionUtils.class);
	
	
	
	
	
	public static void imprimirNotaDevolucion(String id){
		String fol=JOptionPane.showInputDialog(null, "Folio",24L);
		if(!StringUtils.isBlank(id)){
			if(NumberUtils.isNumber(fol)){
				Integer folio=Integer.valueOf(fol);
				NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)Services.getInstance().getUniversalDao().get(NotaDeCreditoDevolucion.class, id);
				nota.setFolio(folio);
				Services.getInstance().getUniversalDao().save(nota);
			}
		}
		//NotaDeCreditoDevolucion nota=(NotaDeCreditoDevolucion)ServiceLocator2.getCXCManager().getAbono(id);
		MessageUtils.showMessage("Prepare su impresora ", "Impresión de Notas");
		try {
			Services.getInstance().getHibernateTemplate().execute(new ImpresionDeNotaDevolucion(id));
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	
	
	
	/**
	 * Habilita una nota de credito para poder ser impresa
	 * 
	 * @param notaId
	 */
	public static void resetearImpresion(final String notaId){
		NotaDeCredito nota=(NotaDeCredito)Services.getInstance().getUniversalDao().get(Abono.class, notaId);
		nota.setImpreso(null);
		Services.getInstance().getHibernateTemplate().update(nota);
	}
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		//imprimirNotaGeneral("8a8a81c7-1f1e4ebc-011f-1e4f175a-01d4");
		//imprimirNotaDeCargo("8a8a81c7-1f663b25-011f-663b873a-0001");
		//imprimirNotaBonificacion("8a8a8198-216dd819-0121-6de393aa-0005");
		/**
		try {
	         FileOutputStream fos = new FileOutputStream("LPT1");
	         PrintStream ps = new PrintStream(fos);
	                 ps.print("Prueba de impresion directa");
	                 ps.print("\f");
	                 ps.close();
	     } catch (Exception e) {
	         System.out.println("Exception occurred: " + e);
	     }
		**/
		SwingUtilities.invokeAndWait(new Runnable(){

			public void run() {
				String res=JOptionPane.showInputDialog(null, "Folio",24L);
				System.out.println("Res: "+res);
				
			}
			
		});
	}
	
	

}
