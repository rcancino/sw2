package com.luxsoft.siipap.swing.browser;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JRViewer;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.reports.ReportUtils;

/**
 * Forma para generar un reporte de Jasper a partir del TableModel de un FilterBrowserPanel 
 * @author pato
 *
 */
public class JRBrowserReportForm extends SXAbstractDialog{
	
	private final FilteredBrowserPanel browser;
	
	private String reportPath;
	
	public JRBrowserReportForm(String title,FilteredBrowserPanel browser) {
		super(title);
		this.browser=browser;
	}
	
	public Map<String, Object> getParametros(){
		Map<String, Object>parametros=new HashMap<String, Object>();
		agregarParametros(parametros);
		return parametros;
	}
	
	public void agregarParametros(Map<String, Object> map){
		
	}
	
	private TableModel getTableModel(){
		return browser.getGrid().getModel();
	}
		
		
	
	public JComponent displayReport(){
		
        net.sf.jasperreports.engine.JasperPrint jasperPrint = null;
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource res = loader.getResource(ReportUtils.toReportesPath(getReportPath()));
        try
            {
            	java.io.InputStream io = res.getInputStream();
                try
                {
                	//JTable table=getGrid();
                    jasperPrint = JasperFillManager.fillReport(io, getParametros(), new JRTableModelDataSource(getTableModel()));
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

	public String getReportPath() {
		return reportPath;
	}

	public void setReportPath(String reportPath) {
		this.reportPath = reportPath;
	}
	

	

}