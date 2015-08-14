package com.luxsoft.sw3.cfdi.parches;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.record.formula.functions.Time;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.cfdi.model.CFDI;


/**
 * Parche para generar las cancelaciones pendientes de CFDIs en el SAT
 * 
 * @author Ruben Cancino
 *
 */
public class CancelacionesEspecialDeNotasDeCredito {
	
	CfdiClient client;
	Empresa empresa;
	final String pfxPassword;
	List<CFDI> cfdis ;
	
	
	 public CancelacionesEspecialDeNotasDeCredito(String password) {
		this.pfxPassword=password;
	}
	
	public void cancelacion(Date dia){
		
		DBUtils.whereWeAre();
		empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		//empresa=ServiceLocator2.getConfiguracion().getSucursal().getEmpresa();
		//Localizando todas los CFDI de notas de credito
		/*List<CFDI> cfdis=ServiceLocator2.getHibernateTemplate()
				.find("from CFDI c where c.tipo=? and c.tipoCfd=?"
						,new Object[]{"NOTA_CREDITO","I"});*/
		/*String sql="SELECT X.CFD_ID FROM sx_cxc_abonos_cancelados C JOIN sx_cxc_abonos A ON(A.ABONO_ID=C.ABONO_ID)"+
						" LEFT JOIN SX_CFDI X ON(X.ORIGEN_ID=A.ABONO_ID)"+
						" where DATE(C.CREADO)= ?  AND A.TIPO_ID LIKE 'NOTA%'"+
						 "AND X.CANCELACION IS  NULL";*/
		
		
		String sql="SELECT X.CFD_ID FROM sx_cxc_abonos_cancelados C JOIN sx_cxc_abonos A ON(A.ABONO_ID=C.ABONO_ID)"+
				"  JOIN SX_CFDI X ON(X.ORIGEN_ID=A.ABONO_ID)"+
				" where  A.TIPO_ID LIKE 'NOTA%'"+
				 "AND X.CANCELACION IS  NULL";
		Object[] args={dia};
		//List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql, args,String.class);
		List<String> rows=ServiceLocator2.getJdbcTemplate().queryForList(sql,String.class);
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
		System.out.println("Notas por cancelar: : "+array.length);
		try {
			cancelar(array);
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
	
	
	
	public void cancelar(String[] uuidList) throws Exception{
		String dirPath="Z:\\CFDI\\cancelacionNotas";
		File dir=new File(dirPath);
		Assert.isTrue(dir.exists(),"No existe el directorio para cancelaciones: "+dirPath);
		Assert.isTrue(dir.isDirectory(),"La ruta para las cancelaciones no es un directorio "+dirPath);
		
		//Resource pfx=ServiceLocator2.instance().getContext().getResource("sat/PAPEL_CFDI_CERT.pfx");
		Resource pfx=ServiceLocator2.instance().getContext().getResource("sat/papelsacfdikey.pfx");
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
				
		try {
			
			String xmlFile=empresa.getClave()+"_NOTAS_"+new Date() +"_"+new Time();
			//String xmlFile="cancelacionNotas";
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
		//CancelacionesEspecialDeNotasDeCredito task=new CancelacionesEspecialDeNotasDeCredito("certificadopapel");
		CancelacionesEspecialDeNotasDeCredito task=new CancelacionesEspecialDeNotasDeCredito("certificadopapelsabajio");
		task.cancelacion(DateUtil.toDate("30/01/2015"));
	}

}
