package com.luxsoft.sw3.cfdi.parches;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.sw3.cfdi.model.CFDI;

/**
 * Parche para generar las cancelaciones pendientes de CFDIs en el SAT
 * 
 * @author Ruben Cancino
 *
 */
public class CancelacionesDeCargos{
	
	CfdiClient client;
	Empresa empresa;
	final String pfxPassword;
	
	
	 public CancelacionesDeCargos(String password) {
		this.pfxPassword=password;
	}
	
	public void cancelacion(Periodo periodo){
		
		DBUtils.whereWeAre();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		
		String sql="SELECT x.CFD_ID FROM sx_cxc_cargos_cancelados c join sx_cfdi x on(x.origen_id=c.cargo_id) " +
				" where date(x.creado) between ? and ?" +
				"   and x.cancelacion is null";
		
		Object[] args=new Object[]{
				new SqlParameterValue(Types.DATE, periodo.getFechaInicial()),
				new SqlParameterValue(Types.DATE, periodo.getFechaFinal())
		};
		List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(
				sql
				, args
				, String.class);
		List<String> porCancelar=new ArrayList<String>();
		for(String id:rows){
			CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
			if(cfdi.getTimbreFiscal().getUUID()!=null){
				porCancelar.add(cfdi.getTimbreFiscal().getUUID());
			}
			
		}
		for(String uuid:porCancelar){
			System.out.println("Cancelacion para cfdi: "+uuid);
		}
		
		String[] array=porCancelar.toArray(new String[0]);
		System.out.println("Cargos por cancelar: : "+array.length);
		if(array.length==0){
			System.out.println("No hay cargos cancelados en este dia");
			return;
		}
		try {
			cancelar(array,periodo);
			for(String id:rows){
				CFDI cfdi=ServiceLocator2.getCFDIManager().getCFDI(id);
				cfdi.setCancelacion(new Date());
				cfdi=(CFDI)ServiceLocator2.getHibernateTemplate().merge(cfdi);
				System.out.println("Cancelacion: "+cfdi);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	public void cancelar(String[] uuidList,Periodo periodo) throws Exception{
		String dirPath="Z:\\CFDI\\cancelaciones";
		File dir=new File(dirPath);
		Assert.isTrue(dir.exists(),"No existe el directorio para cancelaciones: "+dirPath);
		Assert.isTrue(dir.isDirectory(),"La ruta para las cancelaciones no es un directorio "+dirPath);
		
		Resource pfx=ServiceLocator2.instance().getContext().getResource("sat/PAPEL_CFDI_CERT.pfx");
		//Resource pfx=ServiceLocator2.instance().getContext().getResource("sat/papelsacfdikey.pfx");
		Assert.isTrue(pfx.exists(),"No existe el archivo pfx");
		
		byte[] pfxData=new byte[(int)pfx.getFile().length()];
		pfx.getInputStream().read(pfxData);
		client=new CfdiClient();
		
		CancelaResponse res=client.cancelCfdi(
				"PAP830101CR3"
				,"yqjvqfofb"
				, empresa.getRfc()
				, uuidList
				, pfxData
				, pfxPassword);
		String msg=res.getText();
		String aka=res.getAck();
			
		//String msg=new String(Base64.encode("Prueba de cancelacion".getBytes()));
		//String aka=new String(Base64.encode("Prueba de cancelacion".getBytes()));
		try {
			
			String xmlFile=empresa.getClave()+"_CANCELACIONES_"+periodo.toString2();
			//String xmlFile="QUERETARO"+"_CANCELACIONES_"+periodo.toString2();
			File msgFile=new File(dir,xmlFile+"_MSG.xml");
			
			FileOutputStream out1=new FileOutputStream(msgFile);
			out1.write(Base64.decode(msg));
			out1.close();
			
			
			File akaFile=new File(dir,xmlFile+"_AKA.xml");
			FileOutputStream out2=new FileOutputStream(akaFile);
			out2.write(Base64.decode(aka.getBytes()));
			out2.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error salvando archivos de cancelacion: "+ExceptionUtils.getRootCauseMessage(e));
		}
	}
	
	public static void main(String[] args) {
		System.setProperty("jdbc.url", "jdbc:mysql://10.10.1.228/produccion");
		System.setProperty("sucursalOrigen", "OFICINAS");
		CancelacionesDeCargos task=new CancelacionesDeCargos("certificadopapel");
		
		//System.setProperty("jdbc.url", "jdbc:mysql://10.10.9.1/produccion");
		//System.setProperty("sucursalOrigen", "QRQUERETARO");
		//CancelacionesDeCargos task=new CancelacionesDeCargos("certificadopapelsabajio");
	
		Periodo per=new Periodo("01/05/2015","19/06/2015");
		//task.cancelacion(per);
		for(Date dia:per.getListaDeDias()){
			task.cancelacion(new Periodo(dia,dia));
		}
		
	}



}
