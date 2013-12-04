package com.luxsoft.siipap.pos.ui.consultas.caja;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Component;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.Ficha;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.PagoConDeposito;
import com.luxsoft.siipap.model.tesoreria.Banco;
import com.luxsoft.siipap.pos.facturacion.FacturacionModel;
import com.luxsoft.siipap.pos.ui.forms.caja.CajaForm;
import com.luxsoft.siipap.pos.ui.forms.caja.GastoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoConDepositoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoDePedidoFormModel;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoForm;
import com.luxsoft.siipap.pos.ui.forms.caja.PagoFormModel;
import com.luxsoft.siipap.pos.ui.forms.caja.SolicitudDeDepositoForm;
import com.luxsoft.siipap.pos.ui.selectores.DepositosRow;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeDepositos;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeSolicitudDeDepositos;
import com.luxsoft.siipap.service.KernellSecurity;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.caja.Caja;
import com.luxsoft.sw3.caja.Gasto;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.services.SolicitudDeDepositosServices;
import com.luxsoft.sw3.tasks.CorteDeCajaTask;
import com.luxsoft.sw3.tesoreria.model.SolicitudDeDeposito;

/**
 * 
 * @author Ruben Cancino Ramos
 *
 */
@Component("cajaController")
public class CajaController {
	
	private Logger logger=Logger.getLogger(getClass());
	
	private CorteDeCajaTask corteTask=new CorteDeCajaTask();
	
	/**
	 * Registra uno o mas pagos y los asocia al model de facturacion
	 * 
	 * @param facturacionModel
	 */
	public void registrarPago(final FacturacionModel facturacionModel){
		
		final PagoDePedidoFormModel model=new PagoDePedidoFormModel(facturacionModel);
		final PagoForm form=new PagoForm(model);
		model.setFormasDePago(null);
		/*if(facturacionModel.getPedido().getFormaDePago().equals(FormaDePago.DEPOSITO)
				||facturacionModel.getPedido().getFormaDePago().equals(FormaDePago.TRANSFERENCIA)){
			model.setFormasDePago(FormaDePago.EFECTIVO,FormaDePago.TARJETA,FormaDePago.CHEQUE);
		}else*/
			model.setFormasDePago(facturacionModel.getPedido().getFormaDePago());
		if(facturacionModel.getPagos().isEmpty()){
			form.setFormaDePagoModificable(false);
		}else
			form.setFormaDePagoModificable(true);
		form.setTitle("Facturación y cobro de pedido");
		form.setSelectionDeCliente(false);
		form.setPersistirAlAplicar(false);
		form.open();
		if(!form.hasBeenCanceled()){
			//Pago pago=model.persistir();
			Pago pago=model.getPago().toPago();
			facturacionModel.agregarPago(pago);
		}
		model.dispose();
	}
	
	/**
	 * Permite registrar pagos de un cliente sin asociarlos a ventas especificas
	 * 
	 */
	public void cobrar(){
		logger.info("Registrando pago de cliente");
		final PagoFormModel model=new PagoFormModel();
		final PagoForm form=new PagoForm(model);
		form.open();
		model.dispose();
	}
	
	public void registrarDeposito(){
		SolicitudDeDeposito sol=SolicitudDeDepositoForm.generar(OrigenDeOperacion.MOS);
		if(sol!=null){
			sol=Services.getInstance().getSolicitudDeDepositosManager().save(sol);
			SolicitudDeDepositosServices.enviarSolicitud(sol);
			MessageUtils.showMessage("Solicitud generada: "+sol.getDocumento(), "Solicitud de depositos");
		}
	}
	
	public SolicitudDeDeposito buscarDeposito(){
		DepositosRow row= SelectorDeSolicitudDeDepositos.buscar();
		if(row!=null){
			SolicitudDeDeposito target=(SolicitudDeDeposito)Services.getInstance()
					.getSolicitudDeDepositosManager().get(row.getSol_id());
			return target;
		}
		return null;
	}
	
	/**
	 * Cancela una factura aplicando las reglas de negocios correspondientes
	 * 
	 * De forma predeterminada solo se pueden cancelar facturas del dia del sistema
	 * 
	 * @param venta
	 */
	public void cancelar(final Venta venta){
		logger.info("Canelando factura: "+venta);
	}
	
	/**
	 * Genera un corte de caja segun las reglas de negocios vigentes
	 * 
	 * 
	 */
	public void corteDeCaja(){
		
		Caja caja=getBean();		
		caja.setConcepto(Caja.Concepto.CORTE_CAJA);
		//caja.setFecha(DateUtil.toDate("07/01/2010"));		
		Map pagos=corteTask.ejecutar(caja.getFecha(),caja.getSucursal());
		System.out.println(pagos);
		Map cortes=calcularCortesAcumulados(caja);
		final DefaultFormModel model=new CajaFormModel(caja);
		
		final CajaForm form=new CajaForm(model);
		form.setPagos(pagos);		
		form.setCortes(cortes);
		form.setCambios(calcularCambiosDeCheque(caja));
		form.setTipos(Caja.Tipo.TARJETA,Caja.Tipo.DEPOSITO);
		form.setTitle("Corte de Caja");
		form.open();
		if(!form.hasBeenCanceled()){
			Caja target=(Caja)model.getBaseBean();
			caja=persistir(target);
			logger.info("Corte de caja registrado: " +target);
		}
		
	}
	
	
	
	/**
	 * Cambio de un cheque por efectivo de la caja. Implica una entrada de caja (Cheque)  
	 * y una salida (Efectivo)
	 * 
	 * 
	 */
	public void cambiarChequePorEfectivo(){
		Caja caja=getBean();
		//caja.setFecha(DateUtil.toDate("03/07/2010"));
		caja.setConcepto(Caja.Concepto.CAMBIO_CHEQUE);
		caja.setTipo(Caja.Tipo.CHEQUE);
		final DefaultFormModel model=new CajaFormModel(caja);
		final CajaForm form=new CajaForm(model);
		form.setBancos(Services.getInstance().getUniversalDao().getAll(Banco.class));
		form.setTitle("Registro de Caja (Cambio de cheque)");
		form.open();
		if(!form.hasBeenCanceled()){
			caja=persistir(caja);
			logger.info("Cehque registrado: " +caja);
		}
	}
	
	
	/**
	 * Cambio de un cheque por efectivo de la caja. Implica una entrada de caja (Cheque)  
	 * y una salida (Efectivo)
	 * 
	 * 
	 */
	public void cambiarTarjetaPorEfectivo(){
		Caja caja=getBean();
		caja.setConcepto(Caja.Concepto.CAMBIO_TARJETA);
		caja.setTipo(Caja.Tipo.TARJETA);
		final DefaultFormModel model=new CajaFormModel(caja);
		final CajaForm form=new CajaForm(model);
		form.setBancos(Services.getInstance().getUniversalDao().getAll(Banco.class));
		form.setTitle("Registro de Caja (Cambio de tarjeta)");
		form.open();
		if(!form.hasBeenCanceled()){
			caja=persistir(caja);
			logger.info("Tarjeta registrado: " +caja);
		}
	}
	
	/**
	 * Registro de entrada y salida de la morralla
	 * 
	 */
	public void morralla(){
		Caja caja=getBean();		
		caja.setConcepto(Caja.Concepto.FONDO_FIJO);
		caja.setTipo(Caja.Tipo.MORRALLA);
		final DefaultFormModel model=new CajaFormModel(caja);
		final CajaForm form=new CajaForm(model);
		form.setTitle("Registro de Morralla (Fondo Fijo)");
		form.open();
		if(!form.hasBeenCanceled()){
			caja=persistir(caja);
			logger.info("Morralla registrada: " +caja);
		}
	}
	
	
	/**
	 * Alta de un gasto para la sucursal
	 * 
	 */
	public void registroDeGasto(){
		Gasto gasto=(Gasto)Bean.proxy(Gasto.class);
		gasto.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		gasto.setSolicitud(Services.getInstance().obtenerFechaDelSistema());
		final DefaultFormModel model=new DefaultFormModel(gasto);
		final GastoForm form=new GastoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			Gasto target=new Gasto();
			BeanUtils.copyProperties(model.getBaseBean(), target);
			Caja caja=getBean();
			caja.setGasto(gasto);
			persistir(caja);
			System.out.println("Gasto registrado: "+target);
			logger.info("Gasto registrado: "+target);
		}
	}
	
	/**
	 * Cancelacion de un gasto 
	 */
	public void cancelacionDeGasto(){
		
	}
	
	/**
	 * Entrada de dinero mediante un corte a los gastos pendientes de rembolso
	 * 
	 */
	public void rembolso(){
		Caja caja=getBean();
		caja.setConcepto(Caja.Concepto.FONDO_FIJO);
		caja.setTipo(Caja.Tipo.REMBOLSO);
		final DefaultFormModel model=new CajaFormModel(caja);
		final CajaForm form=new CajaForm(model);
		form.setTitle("Registro de Rembolso (Fondo Fijo)");
		form.open();
		if(!form.hasBeenCanceled()){
			caja=persistir(caja);
			logger.info("Rembolso registrado: " +caja);
		}
	}
	
	/**
	 * Genera las fichas de depositos a partir de los pagos registrados en la caja
	 * 
	 */
	public void generarFichasDeDeposito(){
		
	}
	
	/**
	 * Proceso para aplicar un RMD a una factura nueva (mas - menos)
	 * 
	 */
	public void devolucionRmd(){
		
	}
	
	/**
	 * Consulta y reporte del estado en línea de los fondos la caja
	 * 
	 */
	public void arqueo(){
		
	}
	
	private Caja getBean(){
		Caja caja=(Caja)Bean.proxy(Caja.class);
		caja.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		//Date time=Services.getInstance().obtenerFechaDelSistema();
		//Date time=DateUtil.toDate("04/01/2010");
		Date time=new Date();
		caja.setFecha(time);
		caja.setCorte(time);
		
		return caja;
	}
	
	private Caja persistir(final Caja source){
		if(source.getConcepto().equals(Caja.Concepto.CAMBIO_CHEQUE)){
			/*source.aplicar();
			source.setCorte(Services.getInstance().obtenerFechaDelSistema());
			
			Caja cheque=new Caja();
			BeanUtils.copyProperties(source, cheque);
			
			Caja efectivo=new Caja();
			BeanUtils.copyProperties(cheque, efectivo,new String[]{"chequeNumero","chequeNombre","banco"});
			efectivo.setDeposito(cheque.getCaja().multiply(BigDecimal.valueOf(-1.0)));
			efectivo.setTipo(Caja.Tipo.EFECTIVO);
			efectivo.setDeposito(cheque.getDeposito().multiply(BigDecimal.valueOf(-1.0)));
			registrarBitacora(efectivo);
			registrarBitacora(cheque);
			Services.getInstance().getUniversalDao().save(cheque);
			Services.getInstance().getUniversalDao().save(efectivo);
			
			return cheque;*/
			return Services.getInstance().getCorteDeCajaManager().registrarCambioDeCheque(source);
		}else if(source.getConcepto().equals(Caja.Concepto.CAMBIO_TARJETA)){
			return Services.getInstance().getCorteDeCajaManager().registrarCambioDeTarjeta(source);
		}else if(source.getConcepto().equals(Caja.Concepto.FONDO_FIJO)){			
			source.aplicar();
			source.setCorte(Services.getInstance().obtenerFechaDelSistema());
			Caja target=new Caja();
			BeanUtils.copyProperties(source, target);
			for(Gasto g:target.getRembolsos()){
				g.setRembolso(target.getFecha());
				Services.getInstance().getUniversalDao().save(g);
			}
			registrarBitacora(target);
			return (Caja)Services.getInstance().getUniversalDao().save(target);
			
		}else{
			source.aplicar();
			source.setCorte(Services.getInstance().obtenerFechaDelSistema());
			Caja target=new Caja();
			BeanUtils.copyProperties(source, target);
			registrarBitacora(target);
			return (Caja)Services.getInstance().getUniversalDao().save(target);
		}
	}
	
	
	
	private Map calcularCortesAcumulados(Caja caja){
		
		//Total 
		
		String sql="select tipo,sum(DEPOSITO) as TOTAL from sx_caja " +
				" where concepto=\'CORTE_CAJA\' and SUCURSAL_ID=?" +
				" and fecha=? group by tipo";
		SqlParameterValue p1=new SqlParameterValue(Types.NUMERIC,caja.getSucursal().getId());
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,caja.getFecha());
		List<Map> res=Services.getInstance().getJdbcTemplate().queryForList(sql, new Object[]{p1,p2});
		Map map=new HashMap();
		for(Map row:res){
			String tip=(String)row.get("TIPO");
			Number val=(Number)row.get("TOTAL");
			if(val==null)
				val=BigDecimal.ZERO;
			BigDecimal total=BigDecimal.valueOf(val.doubleValue());
			if(tip.equals("EFECTIVO"))
				map.put(Caja.Tipo.EFECTIVO, total);
			else if(tip.equals("CHEQUE"))
				map.put(Caja.Tipo.CHEQUE, total);
			else if(tip.equals("TARJETA"))
				map.put(Caja.Tipo.TARJETA, total);
			else if(tip.equals("DEPOSITO"))
				map.put(Caja.Tipo.DEPOSITO, total);
			else if(tip.equals("TRANSFERENCIA"))
				map.put(Caja.Tipo.TRANSFERENCIA, total);
		}
		return map;
	}
	
	
	private Map  calcularCambiosDeCheque(final Caja caja){
		String sql="select tipo,sum(DEPOSITO) as TOTAL from sx_caja " +
		" where concepto=\'CAMBIO_CHEQUE\' and SUCURSAL_ID=?" +
		" and fecha=? group by tipo";
		SqlParameterValue p1=new SqlParameterValue(Types.NUMERIC,caja.getSucursal().getId());
		SqlParameterValue p2=new SqlParameterValue(Types.DATE,caja.getFecha());
		List<Map> res=Services.getInstance().getJdbcTemplate().queryForList(sql, new Object[]{p1,p2});
		Map map=new HashMap();
		for(Map row:res){
			String tip=(String)row.get("TIPO");
			Number val=(Number)row.get("TOTAL");
			if(val==null)
				val=BigDecimal.ZERO;
			BigDecimal total=BigDecimal.valueOf(val.doubleValue());
			if(tip.equals("EFECTIVO"))
				map.put(Caja.Tipo.EFECTIVO, total);
			else if(tip.equals("CHEQUE"))
				map.put(Caja.Tipo.CHEQUE, total);
			
		}
		return map;
	}
	
	private void registrarBitacora(Caja bean){
		Date time=Services.getInstance().obtenerFechaDelSistema();
		String user=KernellSecurity.instance().getCurrentUserName();	
		String ip=KernellSecurity.getIPAdress();
		String mac=KernellSecurity.getMacAdress();
		
		bean.getLog().setModificado(time);
		bean.getLog().setUpdateUser(user);
		bean.getAddresLog().setUpdatedIp(ip);
		bean.getAddresLog().setUpdatedMac(mac);
		
		
		if(bean.getId()==null){
			bean.getLog().setCreado(time);
			bean.getLog().setCreateUser(user);
			bean.getAddresLog().setCreatedIp(ip);
			bean.getAddresLog().setCreatedMac(mac);
		}
		
	}
	
	
	/**
	 * La funcion principal es valirar el un registro de Caja
	 * 
	 * TODO Probablemente mover a un validator
	 * @author Ruben Cancino Ramos
	 *
	 */
	public class CajaFormModel extends DefaultFormModel {

		public CajaFormModel(Object bean) {
			super(bean);
		}
		
		private Caja getCaja(){
			return (Caja)getBaseBean();
		}

		@Override
		protected void addValidation(PropertyValidationSupport support) {
			validarImporte(support);
			validarCambioDeCheque(support);
			validarCorteDeCaja(support);
			
		}
		
		private void validarImporte(PropertyValidationSupport support){			
			if(getCaja().getConcepto().equals(Caja.Concepto.FONDO_FIJO)){
				if(getCaja().getTipo().equals(Caja.Tipo.REMBOLSO)){
					if(getCaja().getImporte().doubleValue()<=0)
						support.getResult().addError("Importe incorrecto");
				}else
					if(getCaja().getImporte().doubleValue()==0)
						support.getResult().addError("Importe incorrecto");
			}else if(getCaja().getConcepto().equals(Caja.Concepto.CAMBIO_CHEQUE)){
				if(getCaja().getImporte().doubleValue()<=0)
					support.getResult().addError("Importe requerido Registr el importe del cheque");
				
			}else if(getCaja().getConcepto().equals(Caja.Concepto.CAMBIO_TARJETA)){
				if(getCaja().getImporte().doubleValue()<=0)
					support.getResult().addError("Importe del pago con tarjeta");
				if(getCaja().getTarjeta()==null)
					support.getResult().addError("Seleccione la  tarjeta");
				
			}
		}
		
		private void validarCambioDeCheque(PropertyValidationSupport support){
			if(getCaja().getConcepto().equals(Caja.Concepto.CAMBIO_CHEQUE)){
				if(StringUtils.isBlank(getCaja().getChequeNombre())){
					support.getResult().addError("Digite el  propietario de la cuenta");
				}
				
				if(StringUtils.containsIgnoreCase(StringUtils.deleteWhitespace(getCaja().getChequeNombre()), "Papel")){
					support.getResult().addError("Nombre de propietario invalido");
				}
				if(getCaja().getChequeNumero()<=0){
					support.getResult().addError("Digite el número del cheque");
				}
				if(getCaja().getBanco()==null){
					support.getResult().addError("Seleccione el banco del cheque");
				}
			}
		}
		
		private void validarCorteDeCaja(PropertyValidationSupport support){
			if(Caja.Concepto.CORTE_CAJA.equals(getCaja().getConcepto())){
				if(getCaja().getImporte().doubleValue()<=0){
					support.getResult().addError("Importe del corte incorrecto");
				}
				/*if(getCaja().getImporte().doubleValue()>getCaja().getDisponibleCalculado().doubleValue())
					support.getResult().addError("Importe excedo el disponible ");*/
				/*if(!Caja.Tipo.EFECTIVO.equals(getCaja().getTipo())){
					if(getCaja().getCortesAcumulados().doubleValue()>0)
						support.getResult().addError("No se permite mas de un corte para: "+getCaja().getTipo());
				}*/
			}
		}
		
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				CajaController controller=new CajaController();
				
				//controller.cambiarTarjetaPorEfectivo();
				controller.corteDeCaja();
				System.exit(0);
			}

		});
	}

}
