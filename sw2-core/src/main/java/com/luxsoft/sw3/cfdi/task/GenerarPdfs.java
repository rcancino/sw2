package com.luxsoft.sw3.cfdi.task;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.CFDIPrintServices;
import com.luxsoft.sw3.cfdi.model.CFDI;

public class GenerarPdfs {
	
	private static String CFDI_DIR=".cfdiPdf";
	
	private static SimpleDateFormat df=new SimpleDateFormat("dd_MM_yyyy");
	
	public static  File getDirectorioDestino(Date fecha){
		File dir=new File(System.getProperty("user.home"));
		String dest="CFDI_DIR/"+df.format(fecha);
		File target=new File(dir,dest);
		if(!target.exists()){
			target.mkdirs();
		}
		return target;
	}
	
	public void run(final Date fecha){
		//Procesamos todas las ventas del dia para el cliente indicado
		String sql="SELECT ORIGEN_ID FROM SX_CFDI V WHERE DATE(V.CREADO)=? AND V.TIPO=? " +
				"AND V.UUID is not null " +
				"and V.ORIGEN_ID NOT IN(SELECT X.CARGO_ID FROM SX_CXC_CARGOS_CANCELADOS X WHERE X.CARGO_ID=V.ORIGEN_ID)" ;
		Object[] params=new Object[]{
			new SqlParameterValue(Types.DATE,fecha),"FACTURA"	
		};
		List<String> ventas=ServiceLocator2.getJdbcTemplate().queryForList(sql, params, String.class);
		for(String id:ventas){
			try {
				Venta venta=ServiceLocator2.getVentasManager().buscarVentaInicializada(id);
				CFDI cfdi=ServiceLocator2.getCFDIManager().buscarCFDI(venta);
				JasperPrint jp=CFDIPrintServices.impripirComprobante(venta, cfdi, null, false);
				byte[] pdf=JasperExportManager.exportReportToPdf(jp);
				//InputStreamSource sourcePdf=new ByteArrayResource(pdf);
				String pdfName=StringUtils.replace(cfdi.getXmlFilePath(), ".xml", ".pdf");
				FileOutputStream out=new FileOutputStream(new File(getDirectorioDestino(venta.getFecha()),pdfName));
				out.write(pdf);
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		System.setProperty("jdbc.url", "jdbc:mysql://10.10.1.228/produccion");
		System.setProperty("sucursalOrigen", "OFICINAS");
		System.setProperty("cfdi.timbrado", "test");
		System.setProperty("sw3.reports.path","file:z:/Reportes_MySQL/");
		
		Date fecha=DateUtil.toDate("17/02/2014");
		new GenerarPdfs().run(fecha);
	}

}
