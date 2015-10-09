package com.luxsoft.siipap.pos.ui.forms;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.User;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoFormModel;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeFacturasParaAsignacionDeEmbarques;
import com.luxsoft.siipap.pos.ui.utils.ReportUtils2;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.embarque.Embarque;
import com.luxsoft.sw3.embarque.Entrega;
import com.luxsoft.sw3.embarque.Transporte;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.services.KernellUtils;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;



/** 
 * Controlador para el manejo de embarques
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class EmbarqueController {
	
	public void eliminarEmbarque(final Embarque e){
		if(e.getSalida()!=null){
			MessageUtils.showMessage("El embarque ya salió no se puede eliminar", "Eliminar embarque");
			return;
		}
		e.getPartidas().clear();
		e.setComentario("CANCELADO");
		persistir(e);
		//Services.getInstance().getUniversalDao().remove(Embarque.class, e.getId());
	}
	
	public Embarque cerrarEmbarque(final Embarque embarque,JComponent parentComponent){
		Assert.isTrue(embarque.getCerrado()==null,"El embarque ya esta cerrado");
		//Assert.isTrue(embarque.getValor().doubleValue()>0,"El embarque no tiene facturas por entregar");
		String res=JOptionPane.showInputDialog(parentComponent, "Comentario","CERRADO");
		embarque.setComentario(res);
		embarque.setCerrado(Services.getInstance().obtenerFechaDelSistema());
		//return persist(embarque,null);
		return persistir(embarque);
	}
	
	public Embarque cancelarCierreDeEmbarque(final Embarque embarque,JComponent parentComponent){
		Assert.isTrue(embarque.getCerrado()!=null,"El embarque no esta cerrado");
		Assert.isTrue(embarque.getRegreso()==null,"El embarque ya fue entregado");
		embarque.setCerrado(null);
		embarque.setSalida(null);
		//return persist(embarque,null);
		return persistir(embarque);
	}
	
	public Embarque registrarSalida( Embarque embarque){
	
		if(embarque.getSalida()!=null){
			String pattern="El embarque ya salio el  {0,date,short} a las {0,time}" +
			" por lo que no es modificable";
			MessageUtils.showMessage(MessageFormat.format(pattern,embarque.getSalida()), "Embarques");
			return embarque;
		}
		Date salida=RegistroDeSalida.seleccionar();
		if(salida!=null){
			embarque=find(embarque.getId());
			embarque.setSalida(salida);
			if(embarque.getChofer().equals("\"DIRECTO\"") || embarque.getChofer().equals("\"PATN,TRICICLO,DIABLO,CAMINANDO\"" )){
				embarque.setRegreso(salida);
			}
			
			System.out.println("Registrando salida con entregas: "+embarque.getPartidas().size());
			//Embarque target=persist(embarque,null);
			Embarque target=persistir(embarque);
			final Map map=new HashMap();
			map.put("EMBARQUE_ID", target.getId());
			map.put("SUCURSAL", target.getSucursal());
			ReportUtils2.runReport("embarques/AsignacionDeEnvio.jasper", map);
			return target;
		}
		return embarque;
	}
	
	public Embarque registrarRetorno(final Embarque embarque){
		
		
		if(embarque.getRegreso()!=null){
			String pattern="El embarque ya registro su regreso el  {0,date,short} a las {0,time}" +
			" por lo que no es modificable";
			MessageUtils.showMessage(MessageFormat.format(pattern,embarque.getRegreso()), "Embarques");
			return embarque;
		}
		if(embarque.getSalida()==null){
			MessageUtils.showMessage("El embarque no ha registrado su salida", "Embarques");
			return embarque;
		}

		Embarque target=Services.getInstance().getEmbarquesManager().getEmbarquer(embarque.getId());
		
		final RegresoDeEmbarqueFormModel model=new RegresoDeEmbarqueFormModel(target);
		final RegresoDeEmbarqueForm form=new RegresoDeEmbarqueForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			model.comiit();
			
			for(Entrega entrega:target.getPartidas()){
				Venta vent=entrega.getFactura();
				if(vent.getPedido().isContraEntrega()){
					System.out.println("Detectando venta contraentrega");
					entrega.setPorCobrar(entrega.getValor());
					if(vent.getFormaDePago().equals(FormaDePago.EFECTIVO)){
						System.out.println("Venta contra entrega de efectivo aplicando el pago de la factura");
						final BigDecimal porPagar=vent.getSaldoCalculado();
						final PagoFormModel mod=new PagoFormModel();
						
						mod.setFormasDePago(vent.getFormaDePago());
						mod.setSucursal(vent.getSucursal());
						mod.getPago().setCliente(vent.getCliente());
						mod.getPago().registrarImporte(porPagar);
						
						Pago pago=mod.getPago().toPago();
						Date fecha=Services.getInstance().obtenerFechaDelSistema();
						//Venta venta=getFactura(selected.getId());
						Services.getInstance().getPagosManager().cobrarFactura(vent, pago,fecha);				
						Services.getInstance().getFacturasManager().generarAbonoAutmatico(Arrays.asList(vent));
						
						mod.dispose();
					}
					
				}
			}
			return persistir(target);
		}else
			return embarque;
		
		
		
	}
	
	public Embarque registrarIncidente(final Embarque embarque){
		throw new UnsupportedOperationException("NO SE HA IMPLEMENTADO");
	}
	
	public Embarque agregarEntrega(final Embarque e){
		if(e.getSalida()!=null){
			String pattern="El embarque ya salio el  {0,date,short} a las {0,time}" +
			" por lo que no es modificable";
			MessageUtils.showMessage(MessageFormat.format(pattern,e.getSalida()), "Embarques");
			return e;
		}
		//final Venta v=SelectorDeFacturasParaEntrega.seleccionarVenta();
		final Venta v=SelectorDeFacturasParaAsignacionDeEmbarques.seleccionar();
		if(v==null)
			return e;
		
		Embarque target=find(e.getId());
		
		Entrega entrega=new Entrega();
		
		InstruccionDeEntrega ie=v.getPedido().getInstruccionDeEntrega();
		/*
		if(v.getPedido().getInstruccionDeEntrega()==null){
			if(MessageUtils.showConfirmationMessage("La venta no tiene instrucción de entrega desea darla de alta?", "Embarques")){
				ie=InstruccionDeEntregaForm.crearNueva(v.getCliente());
				if(ie!=null){
					v.getPedido().setInstruccionDeEntrega(ie);
					Services.getInstance().getUniversalDao().save(v.getPedido());
					ie=v.getPedido().getInstruccionDeEntrega();
				}else 
					return e;
			}else
				return e;
		}*/
		if(target.getChofer().equals("\"DIRECTO\"") || target.getChofer().equals("\"PATN,TRICICLO,DIABLO,CAMINANDO\"")){
		  entrega.setArribo(new Date());
		  entrega.setRecepcion(new Date());
		}
		
		entrega.setFactura(v);
		entrega.setInstruccionDeEntrega(ie);
		
		if(target.getPartidas().contains(entrega)){
			MessageUtils.showMessage("Factura ya registrada", "Embarques");
			return target;
		}
			

		final EntregaController controller=new EntregaController(entrega);
		final EntregaForm form=new EntregaForm(controller);
		
		if(controller.getEntrega().getFactura()==null)
			return target;
		form.open();
		if(!form.hasBeenCanceled()){
			try {
				entrega=controller.commit();			
				target.agregarUnidad(entrega);
				Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
				Services.getInstance().getEmbarquesManager().salvarEntrega(entrega, sucursal);
				//return persist(target,null);
				return e;
			} catch (Exception e2) {
				MessageUtils.showMessage(ExceptionUtils.getRootCauseMessage(e2), "Error");
			}
			
		}
		return target;
	}
	
public Embarque elminarEntregaEnRetorno(final Embarque embarque,Entrega...entregas){
		
		Embarque target=find(embarque.getId());
		String userName=null;
		if( embarque.getRegreso()==null){
			for(Entrega e:entregas){
				target.getPartidas().remove(e);
				e.setEmbarque(null);
			}
		}
		
		
		return persistir(target);
	}
	
	public Embarque elminarEntrega(final Embarque embarque,Entrega...entregas){
		
		Embarque target=find(embarque.getId());
		String userName=null;
		if(embarque.getSalida()!=null && embarque.getRegreso()==null){
			String pattern="El embarque ya salio el  {0,date,short} a las {0,time}" +
			" por lo que no es modificable";
			MessageUtils.showMessage(MessageFormat.format(pattern,embarque.getSalida()), "Embarques");
			return embarque;
		}else if(embarque.getSalida()!=null && embarque.getRegreso()!=null){
			User user=KernellSecurity.instance().getCurrentUser();
			boolean ok=KernellSecurity.instance().hasRole(POSRoles.ADMINISTRACION_EMBARQUES.name(),false);
			if(!ok){
				user=KernellUtils.buscarUsuario();
				if(user==null || (!user.hasRole(POSRoles.ADMINISTRACION_EMBARQUES.name())) ){
					MessageUtils.showMessage("Permisos insuficientes", "Modificación de embarques");
					return embarque;
				}
				userName=user.getUsername();
			}
			
		}
		for(Entrega e:entregas){
			target.getPartidas().remove(e);
			e.setEmbarque(null);
			//Sucursal sucursal=Services.getInstance().getConfiguracion().getSucursal();
			//Services.getInstance().getEmbarquesManager().eliminarEntrega(e, sucursal);
		}
		//return target;
		//return persist(target,userName);
		return persistir(target);
	}
	
	public Embarque modificarEntrega(final Embarque embarque,final Entrega ent){
		if(embarque.getSalida()!=null){
			String pattern="El embarque ya salio el  {0,date,short} a las {0,time}" +
			" por lo que no es modificable";
			MessageUtils.showMessage(MessageFormat.format(pattern,embarque.getSalida()), "Embarques");
			return embarque;
		}
		
		//Embarque target=entrega.getEmbarque();
		String hql="from Entrega e " +
				" left join fetch e.factura v " +
				" left join fetch v.partidas vp " +
				" left join fetch e.instruccionDeEntrega ie" +
				" left join fetch v.pedido p" +
				" where e.id=?";
		List<Entrega> data=Services.getInstance().getHibernateTemplate().find(hql, ent.getId());
		Entrega entregaTarget=data.get(0);
		final EntregaController controller=new EntregaController(entregaTarget);
		final EntregaForm form=new EntregaForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			entregaTarget=controller.commit();
			entregaTarget=(Entrega)Services.getInstance().getUniversalDao().save(entregaTarget);
			//return persist(entregaTarget.getEmbarque());
			return embarque;
		}
		return embarque;
	}
	
	public Embarque consultarEntrega(final Embarque e,Entrega entrega){
		
		Embarque target=entrega.getEmbarque();
		String hql="from Entrega e " +
				" left join fetch e.factura v " +
				" left join fetch e.instruccionDeEntrega ie" +
				" left join fetch v.pedido p" +
				" where e.id=?";
		List<Entrega> data=Services.getInstance().getHibernateTemplate().find(hql, entrega.getId());
		entrega=data.get(0);
		final EntregaController controller=new EntregaController(entrega);
		controller.setReadOnly(true);
		final EntregaForm form=new EntregaForm(controller);
		form.open();
		if(!form.hasBeenCanceled()){
			return target;
		}
		return target;
	}
	
	public Embarque generarEmbarque(){
		Embarque embarque=new Embarque();
		embarque.setFecha(Services.getInstance().obtenerFechaDelSistema());
		embarque.setSucursal(Services.getInstance().getConfiguracion().getSucursal().getNombre());
		final DefaultFormModel model=new DefaultFormModel(embarque);
		final EmbarqueForm form=new EmbarqueForm(model);
		form.setTransportes(Services.getInstance().getUniversalDao().getAll(Transporte.class));
		form.setChoferes(Services.getInstance().getJdbcTemplate().queryForList("select nombre from SX_CHOFERES", String.class));
		
		form.setTitle("Embarque nuevo");
		form.open();
		if(!form.hasBeenCanceled()){
			Embarque target=(Embarque)model.getBaseBean();
			return persistir(target);
		}
		return null;
	}
	
	public Embarque editarEmbarque( Embarque embarque){
		embarque=find(embarque.getId());
		Embarque original=find(embarque.getId());
		
		System.out.println("Editando embarque: "+embarque.getDocumento()+"  con entrgas: "+embarque.getPartidas().size());
		final DefaultFormModel model=new DefaultFormModel(embarque);
		final EmbarqueForm form=new EmbarqueForm(model);
		form.setTransportes(Services.getInstance().getUniversalDao().getAll(Transporte.class));
	//	
	//	form.setChoferes(Services.getInstance().getJdbcTemplate().queryForList("select nombre from SX_CHOFERES", String.class));	
	//	Object [] args=new Object[]{form.getChoferes().get(0)};
	//	form.setTransportes(Services.getInstance().getJdbcTemplate().queryForList("select TRANSPORTE_ID from SX_TRANSPORTES T JOIN SX_CHOFERES C ON (T.CHOFER_ID=C.CHOFER_ID) WHERE C.CHOFER_ID=?", args,Long.class));
		form.setTitle("Modificación de embarque");
		form.open();
		if(!form.hasBeenCanceled()){
			System.out.println("Salvando edicion de embarque"+ embarque.getDocumento()+" con entregas: "+embarque.getPartidas().size());
			
			Embarque target=(Embarque)model.getBaseBean();
			target.setTransporte(embarque.getTransporte());
			return persistir(target);
		}else
			return original;          
	}
	
	private Embarque persistir( Embarque e){
		System.out.println("Salvando embarque con retorno: "+e.getRegreso());
		System.out.println("TRansporte "+ e.getTransporte()+ "CHOFER  " + e.getChofer());
		Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
		Embarque res=Services.getInstance().getEmbarquesManager().salvarEmbarque(e, s);
		System.out.println("Embarque persistido: "+e.getDocumento()+ " Partidas: "+e.getPartidas().size());
		return res;
	}
	
	public Embarque actualizarEntrega(final Embarque embarque,Entrega source){
		
		if(embarque.getSalida()==null){
			String pattern="El embarque no ha  salido por lo que no se puede registrar la llegada al cliente";
			MessageUtils.showMessage(pattern, "Embarques");
			return embarque;
		}
		if(source.getArribo()==null){
			String pattern="No se ha registrado la llegada con el cliente";
			MessageUtils.showMessage(pattern, "Embarques");
			return embarque;
		}
		Entrega target=new Entrega();
		BeanUtils.copyProperties(source, target,new String[]{"id","version","partidas"});
		target=EntregaActualizacionForm.editar(target);
		if(target!=null){
			if(target.getRecepcion()==null){
				target.setRecepcion(new Date());
			}
			source.setRecepcion(target.getRecepcion());
			source.setRecibio(target.getRecibio());
			source.setComentario(target.getComentario());
			//source=(Entrega)Services.getInstance().getUniversalDao().save(source);
			Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
			source=Services.getInstance().getEmbarquesManager().salvarEntrega(source, s);
			return find(embarque.getId());
		}
		return embarque;
	}
	
	 
	
	public Embarque find(String id){
		String hql="from Embarque e left join fetch e.partidas where e.id=?";
		List<Embarque> res=Services.getInstance().getHibernateTemplate().find(hql, id);
		return res.isEmpty()?null:res.get(0);
	}
	
	/*private Embarque persist(Embarque target,String user){
		Date modificacion=Services.getInstance().obtenerFechaDelSistema();
		if(StringUtils.isBlank(user)){
			user=KernellSecurity.instance().getCurrentUserName();
		}
		if(target.getAddresLog()==null)
			target.setAddresLog(new AdressLog());
		if(target.getLog()==null)
			target.setLog(new UserLog());
		if(target.getId()==null ){
			target.setLog(new UserLog());
			
			target.getLog().setCreado(modificacion);
			target.getLog().setCreateUser(user);
			target.getAddresLog().setCreatedIp(KernellSecurity.getIPAdress());
			target.getAddresLog().setCreatedMac(KernellSecurity.getMacAdress());
		}
		target.getLog().setModificado(modificacion);
		target.getLog().setUpdateUser(user);
		
		target.getAddresLog().setUpdatedIp(KernellSecurity.getIPAdress());
		target.getAddresLog().setUpdatedMac(KernellSecurity.getMacAdress());
		
		return (Embarque)Services.getInstance().getUniversalDao().save(target);
	}*/
	
	public Embarque registrarLlegadaCliente( Embarque embarque,Entrega source){		
		if(embarque.getSalida()==null){
			String pattern="El embarque no ha  salido por lo que no se puede registrar la llegada al cliente";
			MessageUtils.showMessage(pattern, "Embarques");
			return embarque;
		}
		
		final Date llegada=RegistroRecepcionClienteForm.seleccionar();
		if(llegada!=null){
			source.setArribo(llegada);
			Sucursal s=Services.getInstance().getConfiguracion().getSucursal();
			source=Services.getInstance().getEmbarquesManager().salvarEntrega(source, s);
			return find(embarque.getId());
		}
		return embarque;
	}

}
