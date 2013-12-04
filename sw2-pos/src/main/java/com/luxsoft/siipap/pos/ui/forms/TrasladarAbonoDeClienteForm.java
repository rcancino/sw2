package com.luxsoft.siipap.pos.ui.forms;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.text.NumberFormatter;

import org.apache.commons.collections.ListUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.AssertTrue;
import org.jfree.ui.tabbedui.VerticalLayout;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.UniqueList;

import com.jgoodies.binding.beans.Model;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.Aplicacion;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.model.Pago;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDevolucion.TipoDeDevolucion;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.pos.ui.selectores.BuscadorSelectivoVentas;
import com.luxsoft.siipap.pos.ui.selectores.SelectorDeClientes;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;
import com.luxsoft.siipap.ventas.model.DevolucionDeVenta;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.Factura;

/**
 * Proceso para trasladar un saldo a favor de una operacion
 * de contado de un cliente a otro
 *  
 * @author ruben
 *
 */
public class TrasladarAbonoDeClienteForm extends AbstractForm implements PropertyChangeListener{
	
	
	

	public TrasladarAbonoDeClienteForm() {
		super(new TrasnferenciaDeAbonoModel());	
		model.addBeanPropertyChangeListener(this);
	}

	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Traslado de disponible", "Transfiere el pago de una factura de un cliente" +
				" a otro cliente ");
	}



	@Override
	protected JComponent buildFormPanel() {
		
		FormLayout layout=new FormLayout("p,2dlu,max(p;270dlu):g,2dlu,20dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendSeparator("Origen");
		builder.append("Cliente",addReadOnly("clienteOrigen"),true);
		builder.append("Documento",addReadOnly("documentoOrigen"),createLookupFacturaBtn());
		builder.nextLine();		
		builder.append("Pago",addReadOnly("pagoOrigen"),true);
		builder.append("RMD",addReadOnly("rmdOrigen"),true);
		builder.append("Notas",addReadOnly("notasOrigen"),true);
		
		builder.appendSeparator("Destino");
		builder.append("Cliente",addReadOnly("clienteDestino"),createLookupClienteDestinoBtn());
		builder.nextLine();		
		builder.append("Factura",addReadOnly("facturaDestino"),createLookupFacturaDestinoBtn());
		builder.nextLine();
		
		return builder.getPanel();
	}
	
	private JButton createLookupFacturaBtn(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "buscarFacturaOrigen"));
		return btn;
	}
	
	private JButton createLookupClienteDestinoBtn(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "buscarClienteDestino"));
		return btn;
	}
	
	private JButton createLookupFacturaDestinoBtn(){
		JButton btn=new JButton("...");
		btn.addActionListener(EventHandler.create(ActionListener.class, this, "buscarFacturaDestino"));
		return btn;
	}
	
	public void buscarFacturaOrigen(){
		
		BuscadorSelectivoVentas form=new BuscadorSelectivoVentas();
		form.setOrigenes(OrigenDeOperacion.MOS,OrigenDeOperacion.CAM);
		form.setSucursales(Services.getInstance().getConfiguracion().getSucursal());
		form.setPorFecha(false);
		form.open();
		if(!form.hasBeenCanceled()){
			Sucursal suc=form.getSucursal();
			Long docto=form.getDocumento();
			OrigenDeOperacion origen=form.getOrigen();
			Venta v=Services.getInstance().getFacturasManager().getVentaDao().buscarVenta(suc.getId(), docto, origen);
			getTraslado().setFacturaOrigen(v);
			
		}
	}
	
	public void buscarClienteDestino(){
		Cliente c=SelectorDeClientes.seleccionar();
		if(c!=null){
			getTraslado().setClienteDestino(c);			
		}
	}
	
	public void buscarFacturaDestino(){
		BuscadorSelectivoVentas form=new BuscadorSelectivoVentas();
		form.setOrigenes(OrigenDeOperacion.MOS,OrigenDeOperacion.CAM);
		form.setSucursales(Services.getInstance().getConfiguracion().getSucursal());
		form.setPorFecha(false);
		form.open();
		if(!form.hasBeenCanceled()){
			Sucursal suc=form.getSucursal();
			Long docto=form.getDocumento();
			OrigenDeOperacion origen=form.getOrigen();
			Venta v=Services.getInstance().getFacturasManager().getVentaDao().buscarVenta(suc.getId(), docto, origen);
			getTraslado().setFacturaDestino(v);			
		}
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {
		JFormattedTextField field=new JFormattedTextField();
		return field;
		//return super.createCustomComponent(property);
	}


	private TrasladoModel getTraslado(){
		return getMainModel().getTraslado();
	}
	
	private TrasnferenciaDeAbonoModel getMainModel(){
		return (TrasnferenciaDeAbonoModel)model;
	}
	
	
	public void propertyChange(PropertyChangeEvent evt) {
		if("facturaOrigen".equals(evt.getPropertyName())){
			Venta v=(Venta)evt.getNewValue();
			JFormattedTextField tf1=(JFormattedTextField)getControl("documentoOrigen");
			JFormattedTextField tf2=(JFormattedTextField)getControl("clienteOrigen");
			if(v!=null){
				String pattern=" {0}    {1,date,short}  Tipo:{2}  Por :{3}";
				tf1.setValue(MessageFormat.format(pattern, v.getDocumento(),v.getFecha(),v.getOrigen().toString(),v.getTotalCM()));				
				tf2.setValue(MessageFormat.format("{0} ({1})", v.getNombre(),v.getClave()));
				asignarPagos(v);
				asignarRmd(v);
			}else{
				tf1.setValue("");				
				tf2.setValue("");
				getTraslado().setAplicaciones(ListUtils.EMPTY_LIST);
				getTraslado().setDevoluciones(ListUtils.EMPTY_LIST);
			}
		}else if("facturaDestino".equals(evt.getPropertyName())){
			Venta v=(Venta)evt.getNewValue();
			JFormattedTextField tf1=(JFormattedTextField)getControl("facturaDestino");
			JFormattedTextField tf2=(JFormattedTextField)getControl("clienteDestino");
			if(v!=null){
				String pattern=" {0}    {1,date,short}  Tipo:{2}  Por :{3}";
				tf1.setValue(MessageFormat.format(pattern, v.getDocumento(),v.getFecha(),v.getOrigen().toString(),v.getTotalCM()));
				tf2.setValue(MessageFormat.format("{0} ({1})", v.getNombre(),v.getClave()));
				getTraslado().setClienteDestino(v.getCliente());
			}else{
				tf1.setValue("");
				tf2.setValue("");
				getTraslado().setClienteDestino(null);
			}
		}else if("clienteDestino".equals(evt.getPropertyName())){
			Cliente c=(Cliente)evt.getNewValue();
			JFormattedTextField tf1=(JFormattedTextField)getControl("clienteDestino");
			if(c!=null)
				tf1.setValue(c.getNombreRazon());
			else
				tf1.setValue("");
		}
	}
	
	private void asignarPagos(Venta v){
		String hql="from AplicacionDePago a where a.cargo.id=?";
		List<Aplicacion> aplicaciones=Services.getInstance().getHibernateTemplate().find(hql,v.getId());
		getTraslado().setAplicaciones(aplicaciones);
		JFormattedTextField tf1=(JFormattedTextField)getControl("pagoOrigen");
		tf1.setValue(getTraslado().getDescripcionDePago());
	}
	
	private void asignarRmd(Venta v){
		String hql="from DevolucionDeVenta a left join fetch a.nota n where a.devolucion.venta.id=?";
		List<DevolucionDeVenta> devo=Services.getInstance().getHibernateTemplate().find(hql,v.getId());
		getTraslado().setDevoluciones(devo);
		JFormattedTextField tf1=(JFormattedTextField)getControl("rmdOrigen");
		tf1.setValue(getTraslado().getDescripcionDeRmd());
		JFormattedTextField tf2=(JFormattedTextField)getControl("notasOrigen");
		tf2.setValue(getTraslado().getDescripconDeNotas());
	}
	
	
	
	@Override
	public void doAccept() {
		if(MessageUtils.showConfirmationMessage(
				getMainModel().getProcesoMessage()+"\n Desea generar el proceso?", "Trasferencia de abono entre clientes"))
			super.doAccept();
		else
			return;
	}




	public static class TrasnferenciaDeAbonoModel extends DefaultFormModel{

		public TrasnferenciaDeAbonoModel() {
			super(Bean.proxy(TrasladoModel.class));
			
		}
		@Override
		protected void addValidation(PropertyValidationSupport support) {
			if(getTraslado().getFacturaOrigen()==null){
				support.getResult().addError("Seleccione la factura origen");
				//return;
			}else if(!getTraslado().validarUnSoloPago()){
				support.getResult().addError("La factura tiene más de un pago aplicado");
				for(Pago p:getTraslado().getPagos()){
					support.getResult().addWarning(" Pago registrado: "+p.getInfo());
				}
				//return;
			}if(!getTraslado().validarNotasDeCredito()){
				support.getResult().addError("No existe la nota(s) de credito para la devolución ");
				//return;
			}if(!getTraslado().validarImporteDeNotas()){
				support.getResult().addError("El disponible de abonos  (Notas) no es el de la factura origen");
				//return;
			}if(getTraslado().getFacturaDestino()==null && getTraslado().getClienteDestino()==null){
				support.getResult().addError("Indique el cliente o factura destino");
				//return;
			}
		}
		
		private TrasladoModel getTraslado(){
			return (TrasladoModel)getBaseBean();
		}
		
		public String getProcesoMessage(){
			
			String pattern="El proceseo generará los siginetes cambios:" +
					"\n 1.- El pago registrado" +
					"\n 	   del cliente:{0}" +
					" 		   Será redireccionado " +
					"\n  	   al cliente:{1}" +
					"\n 2.- Las aplicaciones del pago seran eliminadas " +
					"\n 3.-	El pago sera marcará como anticipo" +
					"\n 4.- Aplicara el abono (NotaDeCredito) al saldo de la factura original" +
					"";
			if(getTraslado().getFacturaDestino()!=null){
				pattern+="\n 5.- Aplicara el pago  a la factura destino";
			}
			return MessageFormat.format(pattern, getTraslado().getFacturaOrigen().getCliente().getNombreRazon()
					,getTraslado().getClienteDestino().getNombreRazon());
		}
		
		public void persist(){
			
		}
		
	}


	public static class TrasladoModel {
		
		private Venta facturaOrigen;		
		
		private List<DevolucionDeVenta> devoluciones=new ArrayList<DevolucionDeVenta>();
		private List<Aplicacion> aplicaciones=new ArrayList<Aplicacion>();	
		private Venta facturaDestino;
		private Cliente clienteDestino;
		private List<NotaDeCreditoDevolucion> notas;
		
		public Venta getFacturaOrigen() {
			return facturaOrigen;
		}
		public void setFacturaOrigen(Venta facturaOrigen) {
			this.facturaOrigen = facturaOrigen;
		}
				
		public List<DevolucionDeVenta> getDevoluciones() {
			return devoluciones;
		}
		
		public void setDevoluciones(List<DevolucionDeVenta> devoluciones) {
			this.devoluciones = devoluciones;
			if(devoluciones!=null){
				for(DevolucionDeVenta d:getDevoluciones()){
					getNotas().add((NotaDeCreditoDevolucion) d.getNota());
				}
			}else
				getNotas().clear();
		}
		
		public List<Aplicacion> getAplicaciones() {
			return aplicaciones;
		}
		public void setAplicaciones(List<Aplicacion> aplicaciones) {
			this.aplicaciones = aplicaciones;
		}
		public Venta getFacturaDestino() {
			return facturaDestino;
		}
		public void setFacturaDestino(Venta facturaDestino) {
			this.facturaDestino = facturaDestino;
		}
		public Cliente getClienteDestino() {
			return clienteDestino;
		}
		public void setClienteDestino(Cliente clienteDestino) {
			this.clienteDestino = clienteDestino;
		}
		
		public List<NotaDeCreditoDevolucion> getNotas(){
			if(notas==null ){
				notas=
					new UniqueList<NotaDeCreditoDevolucion>(new BasicEventList<NotaDeCreditoDevolucion>(),GlazedLists.beanPropertyComparator(NotaDeCreditoDevolucion.class, "id"));				
			}
			return notas;
		}
		
		public CantidadMonetaria getTotalEnNotas(){
			List<NotaDeCreditoDevolucion> notas=getNotas();
			CantidadMonetaria total=CantidadMonetaria.pesos(0);
			for(NotaDeCreditoDevolucion nota:notas){
				total=total.add(nota.getTotalCM());
			}
			return total;
		}
		
		public CantidadMonetaria getDisponibleEnNotas(){
			List<NotaDeCreditoDevolucion> notas=getNotas();
			CantidadMonetaria total=CantidadMonetaria.pesos(0);
			for(NotaDeCreditoDevolucion nota:notas){
				CantidadMonetaria dis=nota.getDisponibleCM();
				dis=MonedasUtils.calcularTotal(dis);
				total=total.add(dis);
			}
			return total;
		}
		
		public String getDescripcionDePago(){
			if(getAplicaciones().isEmpty())
				return "";
			Pago pago=(Pago)getAplicaciones().get(0).getAbono();
			String pattern="{0} {1,date,short} {2}";
			return MessageFormat.format(pattern, pago.getInfo(),pago.getFecha(),pago.getTotal());
		}
		
		public String getDescripcionDeRmd(){
			if(getDevoluciones().isEmpty())
				return "";
			Devolucion rmd=(Devolucion)getDevoluciones().get(0).getDevolucion();
			String pattern="{0}  Fecha: {1,date,short}   {2}";
			return MessageFormat.format(pattern, rmd.getNumero(),rmd.getFecha(),rmd.getTotal());
		}
		
		public String getDescripconDeNotas(){
			BigDecimal aplicado=BigDecimal.ZERO;
			for(NotaDeCreditoDevolucion nota:getNotas()){
				aplicado=aplicado.add(nota.getAplicado());
			}
			String pattern="Notas {0} Total: {1}  Disponible: {2} Aplicado:{3}";
			return MessageFormat.format(pattern, getNotas().size()
					,getTotalEnNotas().amount()
					,getDisponibleEnNotas().amount()
					,aplicado
					);
			
		}
		
		public List<Pago> getPagos(){
			UniqueList<Pago> pagos=new UniqueList<Pago>(new BasicEventList<Pago>(3),GlazedLists.beanPropertyComparator(Pago.class, "id"));
			if(getAplicaciones()!=null){
				for(Aplicacion a:getAplicaciones()){
					pagos.add((Pago)a.getAbono());
				}
			}			
			return pagos;
		}
		
		// Validadores TODOS DEBEN REGRESAR TRUE 
		
		public boolean validarUnSoloPago(){
			return getPagos().size()==1;
		}
		
		public boolean validarImporteDeNotas(){
			if(getFacturaOrigen()!=null){
				CantidadMonetaria impFac=getFacturaOrigen().getImporteCM();
				CantidadMonetaria impNotas=getDisponibleEnNotas();
				return impFac.equals(impNotas);
			}else
				return false;
		}
		
		
		
		public boolean validarNotasDeCredito(){
			return !getNotas().isEmpty();
		}
		
		
	}
	
	private class TaskWorker extends SwingWorker<String, String>{

		@Override
		protected String doInBackground() throws Exception {
			Services.getInstance().getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,SQLException {
					
					Pago pago=getTraslado().getPagos().get(0);					
					publish("Actualizando pago: "+pago.getInfo()+ "Id: "+pago.getId());
					session.update(pago);
					pago.getAplicaciones().clear();
					pago.setCliente(getTraslado().getClienteDestino());
					pago.setAnticipo(true);
					
					publish("Aplicando RMD(Notas de crédito) a la factura origen");
					
					
					return null;
				}
			});
			return null;
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
				TrasladarAbonoDeClienteForm form=new TrasladarAbonoDeClienteForm();
				form.open();
				System.exit(0);
			}

		});
	}

	

}
