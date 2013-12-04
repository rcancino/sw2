package com.luxsoft.sw3.solicitudes;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.BeanUtils;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.sw3.services.SolicitudDeModificacionesManager;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

public class SolicitudDeModificacionFormModel extends DefaultFormModel{

	private ValueModel passworHoler;
	private SolicitudDeModificacionesManager manager;
	private Sucursal sucursal;
	
	public SolicitudDeModificacionFormModel(){
		super(Bean.proxy(SolicitudDeModificacion.class));
	}
	
	public SolicitudDeModificacionFormModel(SolicitudDeModificacion source,Sucursal sucursal){
		super(Bean.proxy(SolicitudDeModificacion.class));
		Object target=getSolicitud();
		this.sucursal=sucursal;
		BeanUtils.copyProperties(source, target);
		addListeners();
	}
	
	public SolicitudDeModificacionFormModel(Sucursal sucursal) {
		super(Bean.proxy(SolicitudDeModificacion.class));
		this.sucursal=sucursal;
		setValue("sucursal", sucursal);
		addListeners();
	}
	
	protected void addListeners() {
		if(getSolicitud().getId()==null){
			getModel("documento").addValueChangeListener(new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent arg0) {
					localizarDocumento();
				}
			});
		}
		getModel("autorizo").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(evt.getNewValue()!=null)
					getSolicitud().setAutorizacion(new Date());
				else
					getSolicitud().setAutorizacion(null);
			}
		});
	};
	
	@SuppressWarnings("unchecked")
	private void localizarDocumento(){
		if(getSolicitud()==null && (getSolicitud().getId()!=null))
			return;
		String docto=getSolicitud().getDocumento();
		if(StringUtils.isNotBlank(docto) && NumberUtils.isNumber(docto)){
			switch (getSolicitud().getModulo()) {
			case DEPOSITOS:
				System.out.println("Localizando deposito: "+docto);
				
				List<SolicitudDeDeposito>found=getManager().getHibernateTemplate()
						.find("from SolicitudDeDeposito s left join fetch s.cliente c where s.sucursal.id=? and s.documento=?"
						, new Object[]{sucursal.getId(),NumberUtils.toLong(docto) });
				if(!found.isEmpty()){
					SolicitudDeDeposito sol=found.get(0);
					String desc=MessageFormat.format("Solicitud {0} Cliente: {1} Importe: {2} Fecha:{3} Creado por: {4}"
							, sol.getDocumento(),sol.getCliente().getNombre(),sol.getTotal(),sol.getFecha(),sol.getLog().getCreateUser());
					getSolicitud().setDocumentoDescripcion(sol!=null?desc:null);
				}
				break;
			default:
				getSolicitud().setDocumentoDescripcion(null);
				break;
			}
		}else{
			getSolicitud().setDocumentoDescripcion(null);
		}
	}
	
	SolicitudDeModificacion getSolicitud(){
		return (SolicitudDeModificacion)getBaseBean();
	}
	
	public ValueModel getPasswordHolder(){
		if(passworHoler==null){
			passworHoler=new ValueHolder();
			passworHoler.addValueChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent evt) {
					resolveUser(evt.getNewValue());
				}
			});
		}
		return passworHoler;
	}
	
	private void resolveUser(Object newValue) {
		String s=(String)newValue;
		if(getSolicitud().getId()==null)
			setValue("usuario", KernellSecurity.instance().findUser(s,manager.getHibernateTemplate()));
		else{
			setValue("autorizo", KernellSecurity.instance().findUser(s,manager.getHibernateTemplate()));
		}
		validate();
	}

	
	public SolicitudDeModificacion commit(){
		SolicitudDeModificacion target=new SolicitudDeModificacion();
		BeanUtils.copyProperties(getSolicitud(), target,new String[]{"version","addresLog","log"});
		
		return getManager().salvar(target); 
	}
	
	
	
	public SolicitudDeModificacionesManager getManager() {
		return manager;
	}

	public void setManager(SolicitudDeModificacionesManager manager) {
		this.manager = manager;
	}

	protected void addValidation(PropertyValidationSupport support){
		if(getSolicitud().getModulo()!=null){
			switch (getSolicitud().getModulo()) {
			case DEPOSITOS:
				if(StringUtils.isBlank(getSolicitud().getDocumentoDescripcion()) )
					support.addError("Documento", "Se requiere el folio del deposito a modificar");
				break;
			default:
				
				break;
			}
		}
		if(getSolicitud().getId()!=null){
			if(getSolicitud().getAutorizo()==null){
				support.addError("Usuario", "Digite su password");
			}
		}
	}
}
