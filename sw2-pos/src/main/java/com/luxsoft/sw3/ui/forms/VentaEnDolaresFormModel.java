package com.luxsoft.sw3.ui.forms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.jgoodies.validation.util.PropertyValidationSupport;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.FormaDePago;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.pedidos.forms.PedidoDetForm2;
import com.luxsoft.sw3.pedidos.forms.PedidoDetFormModel2;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.PedidoDet;


/**
 * FormModel para la venta en dolares
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class VentaEnDolaresFormModel extends DefaultFormModel {
	
	
	private EventList<ProductoRow> productos;
	
	
	protected Logger logger=Logger.getLogger(getClass());

	public VentaEnDolaresFormModel() {
		super(Bean.proxy(VentaModel.class));
	}
	
	public VentaEnDolaresFormModel(Venta venta) {
		super(venta);
	}
	
	public VentaModel getVenta(){
		return (VentaModel)getBaseBean();
	}
	
	protected void init(){		
		
		
	}
	
	@Override
	protected void addValidation(PropertyValidationSupport support) {
		if(getVenta().getPartidas().size()==0){
			support.addError("", "Debe registrar por lo menos una partida");
		}
		super.addValidation(support);
	}
	
	public EventList<VentaDet> getPartidasSource() {
		return getVenta().getPartidas();
	}
	
	
	
	public void insertar(){
		System.out.println("Insertando partida");
		
		final PedidoDetFormModel2 model=new PedidoDetFormModel2(PedidoDet.getPedidoDet());
		model.setCredito(true);
		model.setSucursal(getVenta().getSucursal());
		final PedidoDetForm2 form=new PedidoDetForm2(model);
		form.setProductos(getProductos());
		form.open();
		if(!form.hasBeenCanceled()){
			PedidoDet target=(PedidoDet)form.getModel().getBaseBean();			
			boolean ok=getVenta().getPartidas().add(target.toVentaDet());
			if(ok){
				target.actualizarPrecioDeLista();
				//partidasSource.add(target);
				//actualizarImportes();
				validate();
				logger.info("Unidad de venta agregada: "+target);				
			}else{
				MessageUtils.showMessage("Ya esta registrado el producto: "+target.getProducto(), "Unidad de venta");
			}
		}
		/*
		for(EntradaDeMaterialDet target:SelectorDeBobinasParaAnalisis.seleccionar()){
			DefaultFormModel model=new DefaultFormModel(target);
			AnalisisDeMaterialDetForm form=new AnalisisDeMaterialDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				EntradaDeMaterialDet det=(EntradaDeMaterialDet)model.getBaseBean();
				boolean ok=getAnalisis().agregarEntrada(det);
				if(ok){
					afterInserPartida(det);
					partidasSource.add(det);
					getAnalisis().actualizar();
				}else{
					MessageUtils.showMessage("Ya esta registrado el producto: "+det.getProducto(), "Análisis de bobinas");
				}
			}
		}
		*/
	}
	
	public void afterInserPartida(VentaDet det){		
		validate();
	}
	
	public void elminarPartida(int index){
		/*
		EntradaDeMaterialDet det=partidasSource.get(index);
		if(det!=null){
			boolean ok=getAnalisis().eliminarEntrada(det);
			if(ok){
				partidasSource.remove(index);
				getAnalisis().actualizar();
				validate();
				return;
			}
		}
		System.out.println("Existe un error en la seleccion de partidas");
		*/
		System.out.println("Eliminando partida");
	}
	
	public void editar(int index){
		/*
		EntradaDeMaterialDet source=partidasSource.get(index);
		if(source!=null){
			EntradaDeMaterialDet target=beforeUpdate(source);
			DefaultFormModel model=new DefaultFormModel(target);
			AnalisisDeMaterialDetForm form=new AnalisisDeMaterialDetForm(model);
			form.open();
			if(!form.hasBeenCanceled()){
				afterUpdate(source, target);
				partidasSource.set(index, source);
				getAnalisis().actualizar();
				validate();
			}
		}*/
	}
	
	protected EventList<ProductoRow> getProductos(){
		if(productos==null|| productos.isEmpty()){
			productos=new BasicEventList<ProductoRow>();
			productos.addAll(Services.getInstance().getProductosManager().getActivosAsRows());		
			//productos=PedidoUtils.getProductos();
		}
		return productos;
	}
	
	
	
	private HibernateTemplate getHibernateTemplate(){
		return Services.getInstance().getHibernateTemplate();
	}
	
	public static class VentaModel{
		private String id;
		private Cliente cliente;
		private Sucursal sucursal;
		private Date fecha;
		private BigDecimal importe;
		private BigDecimal impuesto;
		private BigDecimal total;
		private String comentario;
		private Currency moneda=MonedasUtils.DOLARES;
		private BigDecimal tc;
		private EventList<VentaDet> partidas=GlazedLists.eventList(new ArrayList<VentaDet>());
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Cliente getCliente() {
			return cliente;
		}
		public void setCliente(Cliente cliente) {
			this.cliente = cliente;
		}
		public BigDecimal getImporte() {
			return importe;
		}
		public void setImporte(BigDecimal importe) {
			this.importe = importe;
		}
		public BigDecimal getImpuesto() {
			return impuesto;
		}
		public void setImpuesto(BigDecimal impuesto) {
			this.impuesto = impuesto;
		}
		public BigDecimal getTotal() {
			return total;
		}
		public void setTotal(BigDecimal total) {
			this.total = total;
		}
		public String getComentario() {
			return comentario;
		}
		public void setComentario(String comentario) {
			this.comentario = comentario;
		}
		public EventList<VentaDet> getPartidas() {
			return partidas;
		}
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		public Currency getMoneda() {
			return moneda;
		}
		public void setMoneda(Currency moneda) {
			this.moneda = moneda;
		}
		public BigDecimal getTc() {
			return tc;
		}
		public void setTc(BigDecimal tc) {
			this.tc = tc;
		}
		public Sucursal getSucursal() {
			return sucursal;
		}
		public void setSucursal(Sucursal sucursal) {
			this.sucursal = sucursal;
		}
		
		      
		
	}
	
}
