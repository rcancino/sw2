package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.Matchers;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.matchers.CheckBoxMatcher;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturasParaEntrega extends AbstractSelector<Venta>{
	
	protected Periodo periodo=Periodo.getPeriodo(-5);
	private Cliente cliente;
	Sucursal suc;
	
	
	public SelectorDeFacturasParaEntrega() {
		super(Venta.class, "Facturas");
		suc=Services.getInstance().getConfiguracion().getSucursal();
		setTitle("Facturas      Sucursal: "+suc.getNombre());
		
	}
	
	CheckBoxMatcher<Venta> e2;
	
	@Override
	protected void installEditors(EventList<MatcherEditor<Venta>> editors) {
		TextFilterator textFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor e1=new TextComponentMatcherEditor(documentoField,textFilterator);
		
		TextFilterator textFilterator2=GlazedLists.textFilterator("cliente.nombreRazon");
		TextComponentMatcherEditor e3=new TextComponentMatcherEditor(nombreField,textFilterator2);
		
		e2=new CheckBoxMatcher<Venta>(true){

			@Override
			protected Matcher<Venta> getSelectMatcher(Object... obj) {
				
				Matcher<Venta> m=new Matcher<Venta>(){

					
					public boolean matches(Venta item) {						
						return item.getPedido().getInstruccionDeEntrega()!=null;
					}
					
				};
				return m;
			}
			
			
			
		};		
		editors.add(e1);
		editors.add(e3);
		editors.add(e2);
		Matcher canceladosMatcher=Matchers.beanPropertyMatcher(Venta.class, "cancelado", false);
		editors.add(GlazedLists.fixedMatcherEditor(canceladosMatcher));
	}
	
	private JTextField documentoField=new JTextField(10);
	private JTextField nombreField=new JTextField(10);

	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout(
				"p,2dlu,p,2dlu," +
				"p,2dlu,p,2dlu," +
				"p,2dlu,p" +
				"","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Documento",documentoField);
		builder.append("Cliente",nombreField);
		builder.append("Con Instrucción",e2.getBox());
		e2.select();
		return builder.getPanel();
	}



	@Override
	protected TableFormat<Venta> getTableFormat() {
		String props[]={				
				"cliente.nombreRazon"
				,"pedido.folio"
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"origen"
				,"totalConAnticipo"
				,"pedido.entrega"
				,"pedido.instruccionDeEntrega.municipio"
				,"pedido.contraEntrega"
				};
		String labels[]={				
				"Cliente"
				,"Pedido"
				,"Docto"
				,"N. Fiscal"
				,"Fecha"
				,"Tipo"
				,"Total"
				,"Entrega"
				,"Destino"
				,"PCE"
				};
		return GlazedLists.tableFormat(Venta.class,props,labels);
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){		
		header=new HeaderPanel("Facturas para envio   desde sucursal: "+suc.getNombre(),periodo.toString());
		return header;
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		//builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Periodo: "+periodo.toString());
			//periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
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
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(780,400));
	}



	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
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
		
		String hql="from Venta v " +
				" left join fetch v.pedido p " +
				" left join fetch p.instruccionDeEntrega ie" +
				" where v.sucursal.id=? " +
				" and v.fecha between ? and ? " +
				" and v.pedido.id!=null " +
				" and (v.total+v.anticipoAplicado)>=0" +
				" and v.id not in(select v.id from Entrega e where e.parcial=false and e.factura.id=v.id)";
		Object[] params=new Object[]{suc.getId(),periodo.getFechaInicial(),periodo.getFechaFinal()};
		if(cliente!=null){
			hql="from Venta v " +
				" left join fetch v.pedido p " +
				" left join fetch p.instruccionDeEntrega" +
				" where v.sucursal.id=? and v.fecha between ? and ? and v.cliente.id=? and v.cancelacion=null";
			params=new Object[]{suc.getId(),periodo.getFechaInicial(),periodo.getFechaFinal(),cliente.getId()};
		}
		return Services.getInstance().getHibernateTemplate()
			.find(hql, params);
	}
	
	public void clean(){		
		source.clear();
	}
	
	/**
	 * Regresa una y solo una venta seleccionada, pero completamente inicializada
	 * 
	 * @return
	 */
	public static Venta seleccionarVenta(){
		SelectorDeFacturasParaEntrega selector=new SelectorDeFacturasParaEntrega();
		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Venta v=selector.getSelected();
			v=Services.getInstance().getFacturasManager().getVentaDao().buscarVentaInicializada(v.getId());
			return v;
		}		
		return null;
		
	}
	
	public static List<Venta> seleccionar(){
		SelectorDeFacturasParaEntrega selector=new SelectorDeFacturasParaEntrega();
		
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
	
	/**
	 * Regresa una y solo una venta pero completamente inicializada
	 * 
	 * @param cliente
	 * @return
	 */
	public static Venta seleccionar(Cliente cliente){
		SelectorDeFacturasParaEntrega selector=new SelectorDeFacturasParaEntrega();
		
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.setCliente(cliente);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Venta v= selector.getSelected();
			v= Services.getInstance().getFacturasManager().buscarVentaInicializada(v.getId());
			return v;
		}		
		return null;
		
	}
	
	
	
	
	/**
	public static buscarVenta(final Cliente c){
		
	}
	**/

	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				List<Venta> res=seleccionar();
				System.out.println("Res: "+res.size());
				System.exit(0);
			}
			
		});
		
	}

}
