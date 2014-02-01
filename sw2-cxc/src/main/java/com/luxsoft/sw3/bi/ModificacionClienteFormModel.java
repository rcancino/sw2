package com.luxsoft.sw3.bi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.ValueHolder;
import com.luxsoft.siipap.cxc.ui.clientes.ClienteFormModel;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.ClienteAuditLog;
import com.luxsoft.siipap.model.core.ClienteCredito;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.service.ServiceLocator2;

/**
 * 
 * @author Ruben Cancino
 * 
 *
 */
public class ModificacionClienteFormModel extends ClienteFormModel{
	
	private PresentationModel creditoModel;
	private ValueHolder contactoChannel;
	private ValueHolder comentarioModel;
	
	private final String message;
	private final String property;
	private final String label;
	

	public static ModificacionClienteFormModel getLineaModel(Cliente c){
		return new ModificacionClienteFormModel(c, "Línea de crédito", "credito.linea", "Línea");
	}
	
	public static ModificacionClienteFormModel getPlazoModel(Cliente c){
		return new ModificacionClienteFormModel(c, "Plazo", "plazo", "Plazo");
	}
	
	public static ModificacionClienteFormModel getChequeModel(Cliente c){
		return new ModificacionClienteFormModel(c, "Permitir cheques posfechados", "credito.chequePostfechado", "Permitir cheque posfechado");
	}
	
	public static ModificacionClienteFormModel getDescuentoFijoModel(Cliente c){
		return new ModificacionClienteFormModel(c, "Descuento fijo", "credito.descuentoEstimado", "Modificar descuento fijo");
	}
	
	public static ModificacionClienteFormModel getAtrasoMaximoModel(Cliente c){
		return new ModificacionClienteFormModel(c, "Atraso máximo", "credito.atrasoMaximo", "Modificar atraso máximo");
	}
	
	public static ModificacionClienteFormModel getCheckPlusModel(Cliente c){
		return new ModificacionClienteFormModel(c, "CheckPlus", "credito.checkplus", "Modificar Checkplus");
	}
	
	public static ModificacionClienteFormModel getPermitirChequeModel(Cliente c){
		return new ModificacionClienteFormModel(c, "PermitirCheque", "permitirCheque", "Modificar PermitirCheque");
	}
	
	public static ModificacionClienteFormModel getSuspendido(Cliente c){
		return new ModificacionClienteFormModel(c, "Suspendido", "credito.suspendido", "Modificar Suspendido");
	}
	
	public static ModificacionClienteFormModel getVenceFacturaModel(Cliente c){
		return new ModificacionClienteFormModel(c, "vencimientoFactura", "credito.vencimientoFactura", "Vencimiento Factura");
	}
	
	
	public ModificacionClienteFormModel(Cliente bean,String message,String property,String label) {
		super(bean);
		this.message=message;
		this.property=property;
		this.label=label;
	}
	
	protected Cliente getCliente(){
		return (Cliente)getBaseBean();
	}
	public PresentationModel getCreditoModel(){
		return creditoModel;
	}
	
	protected void init(){
		pmodel.addPropertyChangeListener(PresentationModel.PROPERTYNAME_BEAN, new BeanChannelHandler());
		
		getModel("credito").addValueChangeListener(new CreditoHandler());
		comentarioModel=new ValueHolder();
		initSubModels();
	}	
	
	private void initSubModels(){
		if(getCliente().getCredito()!=null)
			creditoModel=new PresentationModel(getCliente().getCredito());
		else
			creditoModel=new PresentationModel(new ClienteCredito());
		contactoChannel=new ValueHolder(null,true);
	}
	
	public ValueHolder getContactosChannel(){
		return contactoChannel;
	}
	
	
	
	
	
	public ValueHolder getComentarioModel() {
		return comentarioModel;
	}

	public void setComentarioModel(ValueHolder comentarioModel) {
		this.comentarioModel = comentarioModel;
	}




	/**
	 * Detecta cambios en el BeanChannel para actualizar los sub modelos
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class BeanChannelHandler implements PropertyChangeListener{

		public void propertyChange(PropertyChangeEvent evt) {
			initSubModels();
		}
		
	}
	
	private class CreditoHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt){			
			if(evt.getOldValue()==null){
				getCliente().habilitarCredito();
				creditoModel.setBean(getCliente().getCredito());
			}
		}
	}

	public String getMessage() {
		return message;
	}

	public String getProperty() {
		return property;
	}

	public String getLabel() {
		return label;
	}
	
	public Cliente commit(){
		final Cliente old=ServiceLocator2.getClienteManager().buscarPorClave(getCliente().getClave());
		final BeanWrapperImpl w1=new BeanWrapperImpl(old);
		final Cliente res=getCliente();
		final BeanWrapperImpl w2=new BeanWrapperImpl(res);
		final String ip=KernellSecurity.getIPAdress();
		final String message=MessageFormat.format("ANTERIOR:{0}, NUEVO:{1} COMENTARIO:{2} "
				,w1.getPropertyValue(getProperty())
				,w2.getPropertyValue(getProperty())
				,getComentarioModel().getValue()
				);
		final String user=KernellSecurity.instance().getCurrentUserName();
		return (Cliente)ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException,SQLException {
				session.update(res);
				ClienteAuditLog log=new ClienteAuditLog(res, res.getId(), getMessage().toUpperCase(), ip, "OFICINAS");
				
				log.setMessage(message);
				log.setOrigen(res.getNombre());
				log.setUsuario(user);
				log.setLastUpdated(new Date());
				res.getLog().setUpdateUser(user);
				res.getAddresLog().setUpdatedIp(ip);
				session.save(log);
				//session.flush();
				return res;
			}
		});
	}

}
