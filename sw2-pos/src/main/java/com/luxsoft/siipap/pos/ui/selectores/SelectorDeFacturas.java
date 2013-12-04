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
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.AbstractDialog;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.siipap.ventas.model.VentaDet;
import com.luxsoft.sw3.services.Services;

/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturas extends AbstractSelector<Venta>{
	
	protected Periodo periodo=Periodo.getPeriodo(-3);
	private Cliente cliente;
	
	
	public SelectorDeFacturas() {
		super(Venta.class, "Facturas");
	}
	
	
	
	@Override
	protected void installEditors(EventList<MatcherEditor<Venta>> editors) {
		TextFilterator textFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor e1=new TextComponentMatcherEditor(documentoField,textFilterator);
		editors.add(e1);
	}
	
	private JTextField documentoField=new JTextField(10);

	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout("p,2dlu,p,2dlu p,2dlu,p,70dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Documento",documentoField);
		return builder.getPanel();
	}



	@Override
	protected TableFormat<Venta> getTableFormat() {
		String props[]={"clave","sucursal.nombre","fecha","origen","numeroFiscal","documento","total","devoluciones","descuentos","bonificaciones","saldo"};
		String labels[]={"Cliente","Sucursal","Fecha","Tipo","Documento","Fiscal","Total","Devs","Descs","Bonific","Saldo"};
		return GlazedLists.tableFormat(Venta.class,props,labels);
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){		
		header=new HeaderPanel("Lista de facturas generadas",periodo.toString());
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
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(650,400));
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
		String hql="from Venta v where v.fecha between ? and ?";
		Object[] params=new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal()};
		if(cliente!=null){
			hql="from Venta v where v.fecha between ? and ? and v.cliente.clave=?";
			params=new Object[]{periodo.getFechaInicial(),periodo.getFechaFinal(),cliente.getClave()};
		}
		return Services.getInstance().getHibernateTemplate()
			.find(hql, params);
	}
	
	public void clean(){		
		source.clear();
	}
	
	public static List<Venta> seleccionar(){
		SelectorDeFacturas selector=new SelectorDeFacturas();
		
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
	
	public static Venta seleccionar(Cliente cliente){
		SelectorDeFacturas selector=new SelectorDeFacturas();
		
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
