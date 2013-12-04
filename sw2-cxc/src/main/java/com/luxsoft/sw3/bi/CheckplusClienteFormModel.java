package com.luxsoft.sw3.bi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.ventas.CheckPlusCliente;
import com.luxsoft.sw3.ventas.CheckPlusDocumento;
import com.luxsoft.sw3.ventas.CheckPlusReferenciaBancaria;

public class CheckplusClienteFormModel extends DefaultFormModel{

	private CheckPlusCliente clienteExistente;
	
	public CheckplusClienteFormModel(CheckPlusCliente c){
		this();
		this.clienteExistente=c;
		//getCliente().setReferenciasBancarias(c.getReferenciasBancarias());
		//getCliente().setDocumentos(c.getDocumentos());
		BeanUtils.copyProperties(c, getCliente());
	}
	
	public CheckplusClienteFormModel() {
		super(Bean.proxy(CheckPlusCliente.class));
	}
	
	protected void init() {
		getModel("cliente").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(clienteExistente==null){
					Cliente cliente=getCliente().getCliente();
					getCliente().setNombre(cliente!=null?cliente.getNombre():null);
					getCliente().setRfc(cliente!=null?cliente.getRfc():null);
					getCliente().setTelefono1(cliente!=null?cliente.getTelefono1():null);
					getCliente().setTelefono2(cliente!=null?cliente.getTelefono2():null);
					getCliente().setPersonaFisica(cliente!=null?cliente.isPersonaFisica():false);
					getCliente().setDireccion(cliente!=null?cliente.getDireccionFiscal():null);
				}
			}
		});
	};
	
	CheckPlusCliente getCliente(){
		return (CheckPlusCliente)getBaseBean();
	}
	
	public CheckPlusCliente commit(){
		
		 if(clienteExistente==null){
			 //Alta
			 CheckPlusCliente target=new CheckPlusCliente();
			 BeanUtils.copyProperties(getCliente(), target,new String[]{"version","addresLog","log","referenciasBancarias","documentos"});
			 for(CheckPlusReferenciaBancaria r:getCliente().getReferenciasBancarias()){
				 target.agregarReferencia(r);
				 
			 }
			 for(CheckPlusDocumento doc:getCliente().getDocumentos()){
				 target.agregarDocumento(doc);
			 }
			 return persistir(target);
		 }else{
			 BeanUtils.copyProperties(getCliente(), clienteExistente,new String[]{"id","version","addresLog","log","referenciasBancarias","documentos"});
			 for(CheckPlusReferenciaBancaria r:getCliente().getReferenciasBancarias()){
				 //clienteExistente.agregarReferencia(r);
			 }
			 for(CheckPlusDocumento doc:getCliente().getDocumentos()){
				 //clienteExistente.agregarDocumento(doc);
			 }
			 return persistir(clienteExistente);
		 }
		 /*
		 KernellSecurity.instance().registrarAddressLog(target, "addresLog");
		 KernellSecurity.instance().registrarUserLog(target, "log");
		 return (CheckPlusCliente)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				session.saveOrUpdate(target);
				target.getCliente().getCredito().setCheckplus(true);
				session.update(target.getCliente().getCredito());
				return session.merge(target);
			}
		});*/
		 //target=(CheckPlusCliente)ServiceLocator2.getHibernateTemplate().merge(target);
		 
	}
	
	private CheckPlusCliente persistir(final CheckPlusCliente target){
		KernellSecurity.instance().registrarAddressLog(target, "addresLog");
		 KernellSecurity.instance().registrarUserLog(target, "log");
		 return (CheckPlusCliente)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				session.saveOrUpdate(target);
				target.getCliente().getCredito().setCheckplus(true);
				session.update(target.getCliente().getCredito());
				return session.merge(target);
			}
		});
	}
	
	protected void addValidation(PropertyValidationSupport support){
		//if(getCliente().getReferenciasBancarias().size()<1)
			//support.addError("referencias", "Se requiere por lo menos 1 referencias bancarias "+getCliente().getReferenciasBancarias().size());
	}
}
