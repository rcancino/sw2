package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.hibernate.validator.NotNull;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturas extends AbstractSelector<Venta>{
	
	protected Periodo periodo=Periodo.getPeriodoConAnteriroridad(3);
	protected Cliente cliente;
	
	private SelectorDeFacturas() {
		super(Venta.class, "Facturas");
		
	}
	
	@Override
	protected TableFormat<Venta> getTableFormat() {
		String props[]={"clave","sucursal.nombre","fecha","tipoDocto","numeroFiscal","documento","total","devoluciones","descuentos","bonificaciones","saldo"};
		String labels[]={"Cliente","Sucursal","Fecha","Tipo","Documento","Fiscal","Total","Devs","Descs","Bonific","Saldo"};
		return GlazedLists.tableFormat(Venta.class,props,labels);
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){
		String title=cliente!=null?cliente.getNombreRazon():"NA";
		header=new HeaderPanel(title,periodo.toString());
		return header;
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	public void cambiarPeriodo(){
		ValueHolder holder=new ValueHolder(periodo);
		AbstractDialog dialog=Binder.createPeriodoSelector(holder);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=(Periodo)holder.getValue();			
			load();
			updatePeriodoLabel();
		}
	}
	
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}

	protected void updatePeriodoLabel(){
		header.setDescription("Periodo: "+periodo.toString());
	}

	@Override
	protected List<Venta> getData() {
		return ServiceLocator2.getVentaDao().buscarVentas(periodo, cliente);
	}
	
	public void clean(){
		cliente=null;
		source.clear();
	}
	
	

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;		
	}
	
	
	
	
	public static Venta buscarVenta(final Cliente c){
		SelectorDeFacturas selector=new SelectorDeFacturas();
		selector.setCliente(c);
		selector.open();
		if(!selector.hasBeenCanceled()){
			selector.clean();
			return selector.getSelected();
		}		
		return null;
	}
	
	
	
	public static List<Venta> buscarVentas(final Cliente c){
		SelectorDeFacturas selector=new SelectorDeFacturas();
		selector.setCliente(c);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<Venta> ventas=new ArrayList<Venta>();
			ventas.addAll(selector.getSelectedList());
			selector.clean();
			return ventas;
		}		
		return new ArrayList<Venta>(0);
		
	}
	
	public static Venta buscarVentaSelectiva(){		
		final DefaultFormModel model=new DefaultFormModel(Bean.proxy(VentaLooup.class));
		final FacturaSearchDialog dialog=new FacturaSearchDialog(model);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			final VentaLooup res=(VentaLooup)model.getBaseBean();
			
			return ServiceLocator2.getVentaDao().buscarVenta(
					res.getSucursal().getId()
					, res.getDocumento()
					, res.getOrigen()
					,res.getFecha()
					);
		}
		return null;
	}
	
	public static class FacturaSearchDialog extends AbstractForm{

		public FacturaSearchDialog(IFormModel model) {
			super(model);
		}

		@Override
		protected JComponent buildFormPanel() {
			final FormLayout layout=new FormLayout(
					"p,3dlu,p:g"
					,"");
			final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Documento", getControl("documento"));
			builder.append("Sucursal", getControl("sucursal"));
			builder.append("Tipo", getControl("origen"));
			builder.append("Fecha", getControl("fecha"));
			return builder.getPanel();
		}
		@Override
		protected JComponent createCustomComponent(String property) {
			if("sucursal".equals(property)){
				return Bindings.createSucursalesBinding(model.getModel(property));
			}else if("origen".equals(property)){
				SelectionInList sl=new SelectionInList(OrigenDeOperacion.values(),model.getModel(property));
				JComponent c=BasicComponentFactory.createComboBox(sl);
				return c;
			}else if("documento".equals(property)){
				return BasicComponentFactory.createLongField(model.getModel(property));
			}
			return super.createCustomComponent(property);
		}
	}
	
	public static class VentaLooup {
		
		@NotNull(message="Se requiere la sucursal de la factura")
		private Sucursal sucursal;
		@NotNull(message="Se requiere el número de la factura")
		private Long documento;
		@NotNull(message="Se requiere el tipo de la factura")
		private OrigenDeOperacion origen;
		
		@NotNull
		private Date fecha;
		
		public Sucursal getSucursal() {
			return sucursal;
		}
		public void setSucursal(Sucursal sucursal) {
			this.sucursal = sucursal;
		}
		public Long getDocumento() {
			return documento;
		}
		public void setDocumento(Long documento) {
			this.documento = documento;
		}
		public OrigenDeOperacion getOrigen() {
			return origen;
		}
		public void setOrigen(OrigenDeOperacion origen) {
			this.origen = origen;
		}
		public Date getFecha() {
			return fecha;
		}
		public void setFecha(Date fecha) {
			this.fecha = fecha;
		}
		
		
	}
	
	
	/**
	public static buscarVenta(final Cliente c){
		
	}
	**/

	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				//buscarVenta(new Cliente("I020376","Impresos litopolis"));
				System.out.println(buscarVentaSelectiva());
				System.exit(0);
			}
			
		});
		
	}

}
