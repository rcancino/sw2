package com.luxsoft.siipap.swing.reports;


import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.view.JRViewer;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXFrame;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class ReportManagerImpl implements ReportManager{
	
	private Logger logger=Logger.getLogger(getClass());
	
	private DefaultResourceLoader resourceSupport;
	
	public ReportManagerImpl(){
		resourceSupport=new DefaultResourceLoader();
	}
	
	public JComponent execute(String location, Map<String, Object> params) {
		return execute(resourceSupport.getResource(location),params);
	}
	
	public JComponent execute(Resource resource, Map<String, Object> params) {
		Assert.isTrue(resource.exists(),"No existe el reporte: "+resource);
		try {
			InputStream is=resource.getInputStream();
			return run(is,params);
		} catch (IOException e) {			
			e.printStackTrace();
			
		}
		return null;
	}
	
	protected JComponent run(final InputStream is,Map<String, Object> params){
		try {
			final JasperPrint jp=getJasperPrint(is, params);
			final JRViewer view=new JRViewer(jp);
			return view;
		} catch (Exception e) {
			MessageUtils.showError("Error ejecutando reporte", e);
		}
		return null;
	}
	
	private JasperPrint getJasperPrint(final InputStream io,final Map<String, Object> params){
		return (JasperPrint)getJdbcTemplate().execute(new ConnectionCallback(){
			public Object doInConnection(Connection con) throws SQLException, DataAccessException {
				try {
					return JasperFillManager.fillReport(io,params,con);
				} catch (JRException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					throw new SQLException("Error generando JasperPrint");
				}
			}
		});
	}
	
	public void printReport(String location,Map<String, Object> params,boolean printPreview){
		Resource rs=resourceSupport.getResource(location);
		Assert.isTrue(rs.exists(), "No existe el reporte "+location);		
		try {
			JasperPrint jp=getJasperPrint(rs.getInputStream(), params);
			JasperPrintManager.printReport(jp, printPreview);
		} catch (Exception e) {
			MessageUtils.showError("Error imprimiento reporte", e);
		}
	}
	

	public JdbcTemplate getJdbcTemplate() {
		return ServiceLocator2.getJdbcTemplate();
	}
	
	private Empresa empresa;
	
	private Empresa getEmpresa(){
		if(empresa==null){
			empresa=(Empresa)ServiceLocator2.getHibernateTemplate().get(Empresa.class, 1L);
		}
		return empresa;
	}

	public void showInDialog(final String path,final Map<String,Object> params){
		params.put("COMPANY", getEmpresa().getNombre());
		logger.info("Ejecutando reporte: "+path+ "Parametros: "+params);
		SwingWorker<JRViewer, String> worker=new SwingWorker<JRViewer, String>(){
			
			protected JRViewer doInBackground() throws Exception {
				final JRViewer view=(JRViewer)ReportManagerImpl.this.execute(path, params);
				view.setPreferredSize(new Dimension(750,900));
				return view;
			}
			
			protected void done() {
				try {
					showInDialog(get(),path);
				} catch (Exception e) {
					MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e) ,"Error ejecutando reporte:" +path);
				}
			}
		};
		TaskUtils.executeTask(worker);
		/*
		final JRViewer view=(JRViewer)execute(path, params);
		view.setPreferredSize(new Dimension(750,900));
		final SXAbstractDialog dialog=new SXAbstractDialog("Reporte: "+path){

			@Override
			protected JComponent buildContent() {
				return view;
			}

			@Override
			protected void setResizable() {
				setResizable(true);
			}
			
		};
		dialog.setModal(false);
		dialog.open();
		*/		
	}
	
	private void showInDialog(final JRViewer view,final String rname){
		/**
		final SXAbstractDialog dialog=new SXAbstractDialog("Reporte"){

			@Override
			protected JComponent buildContent() {
				return view;
			}

			@Override
			protected void setResizable() {
				setResizable(true);
			}
			
		};
		dialog.setModal(false);
		dialog.open();
		**/
		
		final JXFrame f=new JXFrame("Reporte: "+rname);
		
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.addComponent(view);
		f.pack();
		f.setVisible(true);
	}
	
	public static void main(String[] args) {
		ReportManagerImpl manager=new ReportManagerImpl();
		manager.showInDialog("reportes/CobranzaDiaCredito.jasper", new HashMap<String, Object>());
		System.exit(0);
	}
	
	

}
