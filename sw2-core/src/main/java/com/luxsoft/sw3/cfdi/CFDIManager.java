package com.luxsoft.sw3.cfdi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.edicom.ediwinws.cfdi.client.CfdiClient;
import com.edicom.ediwinws.service.cfdi.CancelaResponse;
import com.luxsoft.siipap.cxc.model.NotaDeCargo;
import com.luxsoft.siipap.cxc.model.NotaDeCredito;
import com.luxsoft.siipap.model.Empresa;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.cfdi.model.CFDI;

@Service("cfdiManager")
public class CFDIManager {
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private IFactura iFactura;
	
	@Autowired
	private INotaDeCargo iNotaDeCargo;
	
	@Autowired
	private CFDITimbrador cfdiTimbrador;
	
	public CFDI getCFDI(String id){
		return (CFDI)hibernateTemplate.get(CFDI.class, id);
	}
	
	public CFDI buscarCFDI(Venta venta){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",venta.getId());
		Assert.notEmpty(res,"No localizo el CFDI origen: "+venta.getId());
		return res.get(0);
	}
	
	
	public CFDI buscarCFDI(NotaDeCredito nota){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",nota.getId());
		Assert.notEmpty(res,"No localizo el CFDI origen: "+nota.getId());
		return res.get(0);
	}
	
	public CFDI buscarCFDI(NotaDeCargo nota){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",nota.getId());
		Assert.notEmpty(res,"No localizo el CFDI origen: "+nota.getId());
		return res.get(0);
	}
	
	public CFDI buscarPorUUID(String uuid){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.UUID=?",uuid);
		return res.isEmpty()?null:res.get(0);
	}
	
	public CFDI buscarPorOrigen(String origenId){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",origenId);
		return res.isEmpty()?null:res.get(0);
	}
	
	public CFDI existe(Venta venta){
		List<CFDI> res=hibernateTemplate.find("from CFDI c where c.origen=?",venta.getId());
		return res.isEmpty()?null:res.get(0);
	}
	
	public CFDI generarFactura(Venta venta){
		return getiFactura().generar(venta);
		
	}
	
	public CFDI timbrar(CFDI cfdi) throws Exception{
		return cfdiTimbrador.timbrar(cfdi);
	}
	
	@Transactional(propagation=Propagation.SUPPORTS)
	public void cancelar(Empresa empresa,String ... uuidList) throws Exception{
		System.out.println("Mandando canclera CFDIS: "+ArrayUtils.toString(uuidList));
		
		CfdiClient client=new CfdiClient();
		
		CancelaResponse res=client.cancelCfdi(
				"PAP830101CR3"
				,"yqjvqfofb"
				, empresa.getRfc()
				, uuidList
				, empresa.getCertificadoDigitalPfx()
				, "certificadopapel");
		String msg=res.getText();
		String aka=res.getAck();
		String[] uuids=res.getUuids();
		try {
			//byte[] d1=Base64.decode(msg.getBytes());
			byte[] d1=msg.getBytes();
			File file1=new File("c://basura//file1.xml");
			FileOutputStream out1=new FileOutputStream(file1);
			out1.write(d1);
			out1.close();
			
			
			//byte[] d2=Base64.decode(aka.getBytes());
			byte[] d2=aka.getBytes();
			File file2=new File("c://basura//file2.xml");
			FileOutputStream out2=new FileOutputStream(file2);
			out2.write(d2);
			out2.close();
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		/*String msg="OK";
		String aka="?";
		String[] uuids=new String[]{"C5B89C69-69DB-412A-8CD2-FC467ECD7C8B"};
		*/
		
		for(String uuid:uuids){
			CFDI cfdi=buscarPorUUID(uuid);
			cfdi.setCancelacion(new Date());
			//cfdi.setCancelacionResponse(msg);
			//cfdi.setCancelacionResponseAka(aka);
			hibernateTemplate.merge(cfdi);
		}
		
	}

	public IFactura getiFactura() {
		return iFactura;
	}

	public void setiFactura(IFactura iFactura) {
		this.iFactura = iFactura;
	}

	public void setCfdiTimbrador(CFDITimbrador cfdiTimbrador) {
		this.cfdiTimbrador = cfdiTimbrador;
	}
	
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	public void setiNotaDeCargo(INotaDeCargo iNotaDeCargo) {
		this.iNotaDeCargo = iNotaDeCargo;
	}

}
