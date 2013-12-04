package com.luxsoft.siipap.swing.reports;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXFrame;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;

import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Facade para el uso de Rerportes 
 * 
 * @author Ruben Cancino
 *
 */
public final class ReportUtils {
	
	private final static ReportManagerImpl rmanager;
	
	private static DefaultResourceLoader resourceSupport;
	
	static{
		rmanager=new ReportManagerImpl();
		resourceSupport=new DefaultResourceLoader();
	}
	
	public static ReportManager getManager(){		
		return rmanager;
	}
	
	public static boolean existe(String reportPath){
		return resourceSupport.getResource(reportPath).exists();
	}
	
	/**
	 * Ejecuta el reporte mostrandolo en panatalla
	 * 
	 * @param path
	 * @param params
	 */
	public static void viewReport(final String path,Map<String,Object> params){
		if(params==null)params=new HashMap<String, Object>();
		rmanager.showInDialog(path, params);
	}
	
	/**
	 * Ejecuta el reporte y lo imprime sin poderlo ver en panatalla pero con la posibilidad de
	 * configurar la impresora
	 * 
	 * @param path
	 * @param params
	 * @param preview
	 */
	public static void printReport(final String path,Map<String,Object> params,boolean preview){
		if(params==null)
			params=new HashMap<String, Object>();
		if(preview)			
			rmanager.printReport(path, params,preview);
		else{
			rmanager.printReport(path, params,preview);
		}
	}
	
	public static String toReportesPath(final String reporteName){
		//String reps="file:z:/Reportes_MySQL/";
		//String reps=System.getProperty("sw3.reports.path", "file:z:/Reportes_MySQL/");
		String reps=System.getProperty("sw3.reports.path");
		if(StringUtils.isEmpty(reps))
			throw new RuntimeException("No se ha definido la ruta de los reportes en: sw3.reports.path");
		return reps+reporteName;
	}
	
	public static String toReportesPathVentas(final String reporteName){
		//String reps="file:z:/reps/Ventas/";
		String reps=System.getProperty("VENTAS_REPORTS_PATH", "file:z:/reps/Ventas/");
		return reps+reporteName;
	}
	
	public static void viewReport(final String reportPath,final Map params,final TableModel tablemodel){
		String pattern="Ejecutando reporte {0} parametros: {1} #Columnas del tableModel:{2}";
				
		System.out.println(MessageFormat.format(pattern, reportPath,params,tablemodel.getColumnCount()));
		try {
			TableModelReportViewer view=new TableModelReportViewer(reportPath,params,tablemodel);
			view.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static class TableModelReportViewer extends SXAbstractDialog{
		
		private final Map parametros;
		private final TableModel tableModel;
		private final String reportPath;
		
		public TableModelReportViewer(final String reportPath,Map parametros,final TableModel tableModel) {
			super("Reporte...");
			this.parametros=parametros;
			this.tableModel=tableModel;
			this.reportPath=reportPath;
			
		}

		public JComponent displayReport(){
			  
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(reportPath);
                try
                {
                	System.out.println("resource: "+res.getFile().getAbsolutePath());
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	
                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(tableModel));
                    }
                    catch(JRException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
                JRViewer jasperViewer = new JRViewer(jasperPrint);
                jasperViewer.setPreferredSize(new Dimension(1000, 600));
                return jasperViewer;

			}

		@Override
		protected JComponent buildContent() {
			return displayReport();
		}

		@Override
		protected void setResizable() {
		setResizable(true);
		}
		
	}
	
	public static void viewWindowReport(final String reportPath,final Map params,final TableModel tablemodel){
		try {
			TableModelReportWindow view=new TableModelReportWindow(reportPath,params,tablemodel);
			final JXFrame f=new JXFrame("Reporte: "+reportPath);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			f.addComponent(view.displayReport());
			f.pack();
			f.setVisible(true);
			f.setAlwaysOnTop(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static class TableModelReportWindow {
		
		private final Map parametros;
		private final TableModel tableModel;
		private final String reportPath;
		
		public TableModelReportWindow(final String reportPath,Map parametros,final TableModel tableModel) {
			this.parametros=parametros;
			this.tableModel=tableModel;
			this.reportPath=reportPath;
		}

		public JComponent displayReport(){
                net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
                DefaultResourceLoader loader = new DefaultResourceLoader();
                Resource res = loader.getResource(reportPath);
                try
                {	
                    java.io.InputStream io = res.getInputStream();
                    try
                    {
                    	
                        jasperPrint = JasperFillManager.fillReport(io, parametros, new JRTableModelDataSource(tableModel));
                    }
                    catch(JRException e)
                    {
                        e.printStackTrace();
                    }
                }
                catch(IOException ioe)
                {
                    ioe.printStackTrace();
                }
                JRViewer jasperViewer = new JRViewer(jasperPrint);
                jasperViewer.setPreferredSize(new Dimension(1000, 600));
                return jasperViewer;
		}
		
	}
	
	
	
	/**
	 * Crea una accion para ejecutar un reporte que se encuentra localizado en Z:\reps
	 * Asume que el objeto model que se pasa como parametro tiene un metodo getParametros
	 * para obtener los parametros del report
	 * 
	 * 
	 * @return
	 */
	public static Action createStandarReportAction(final String label,final String reportName ,final Object model){
		Action a=new AbstractAction(label){
			public void actionPerformed(ActionEvent e) {
				Method m=BeanUtils.findMethodWithMinimalParameters(model.getClass(), "getParametros");
				Map params;
				try {
					params = (Map) m.invoke(model);
					if(params!=null)
						ReportUtils.viewReport(ReportUtils.toReportesPath(reportName), params);
				} catch (Exception e1) {
					throw new RuntimeException("Error al tratar de obtener parametros para el reporte",e1);
				}
			}			
		};		
		a.putValue(Action.NAME, label);
		return a;
	}
	
	private static Logger logger=Logger.getLogger(ReportUtils.class);
	
	
	
	public static void runReportInOracle(final String path,final Map<String,Object> params){
		logger.info("Ejecutando reporte en oracle: "+path+ "Parametros: "+params);
		SwingWorker<JRViewer, String> worker=new SwingWorker<JRViewer, String>(){
			
			protected JRViewer doInBackground() throws Exception {
				final InputStream ios=resourceSupport.getResource(path).getInputStream();
				final JasperPrint jp=getJasperPrint(ios, params);
				final JRViewer view=new JRViewer(jp);
				view.setPreferredSize(new Dimension(750,900));
				return view;
			}
			
			protected void done() {
				try {
					showInDialog(get(),path);
				} catch (Exception e) {
					MessageUtils.showError("Error ejecutando reporte:" +path, e);
					logger.error(ExceptionUtils.getRootCauseMessage(e),e);
				}
			}
		};
		TaskUtils.executeTask(worker);		
	}
	
	
	
	private static JasperPrint getJasperPrint(final InputStream io,final Map<String, Object> params){
		return (JasperPrint)ServiceLocator2.getAnalisisJdbcTemplate().execute(new ConnectionCallback(){
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				try {
					return JasperFillManager.fillReport(io,params,con);
				} catch (JRException e) {
					logger.error(ExceptionUtils.getRootCauseMessage(e),e);
					throw new SQLException(ExceptionUtils.getRootCauseMessage(e));
				}finally{
					try {
						io.close();
					} catch (IOException e) {						
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	protected  static void showInDialog(final JRViewer view,final String rname){
		
		final JXFrame f=new JXFrame("Reporte: "+rname);
		
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.addComponent(view);
		f.pack();
		f.setVisible(true);

	}
	
	
	public static void main(String[] args) {
		//printReport("reportes/CobranzaCredito.jasper", null, true);
		//viewReport("reportes/CobranzaCredito.jasper", null);
		//viewReport("reportes/RecepcionDeFacturas.jasper", null);
		DefaultResourceLoader loader=new DefaultResourceLoader();
		Resource r=loader.getResource(ReportUtils.toReportesPath("ventas/Pedido.jasper"));
		System.out.println(r);
		/*
		
		String path=ReportUtils.toReportesPath("maquila/INVENTARIO_HOJEO_MAQ.jasper");
		System.out.println(path);
		ReportUtils.runReportInOracle(path, new HashMap());
		*/
		
		Map map=new HashMap();
		printReport(ReportUtils.toReportesPath("ventas/Pedido.jasper"), map, true);
		
	}

}
