package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
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
import com.luxsoft.siipap.compras.model.RecepcionDeCompra;
import com.luxsoft.siipap.model.Configuracion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.sw3.services.Services;

public class SelectorDeComs extends AbstractSelector<RecepcionDeCompra>{
	
	private Long sucursalId;
	private Periodo periodo;

	public SelectorDeComs(Long sucursalId) {
		super(RecepcionDeCompra.class, "Selector de Entradas por compra (COM)");
		this.sucursalId=sucursalId;
		periodo=Periodo.getPeriodoDelMesActual();
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){		
		header=new HeaderPanel("Compras recibidas ","");
		updatePeriodoLabel();
		return header;
	}

	@Override
	protected List<RecepcionDeCompra> getData() {
		String hql="from RecepcionDeCompra r" +
				" where r.sucursal.id=? " +
				"  and  r.fecha between ? and ?";
		Object[] values={sucursalId,periodo.getFechaInicial(),periodo.getFechaFinal()};
		List res=Services.getInstance().getHibernateTemplate().find(hql, values);
		return res;
	}

	@Override
	protected TableFormat<RecepcionDeCompra> getTableFormat() {
		String[] props={"sucursal.nombre","compra.nombre","fecha","documento","remision","compra.folio"};
		String[] names={"Sucursal","Proveedor","Fecha","COM","Remisión/Fac","Compra"};
		return GlazedLists.tableFormat(RecepcionDeCompra.class, props,names);
	}
	
	
	
	@Override
	protected void installEditors(
			EventList<MatcherEditor<RecepcionDeCompra>> editors) {
		// TODO Auto-generated method stub
		TextFilterator textFilterator=GlazedLists.textFilterator("documento");
		TextComponentMatcherEditor e1=new TextComponentMatcherEditor(documentoField,textFilterator);
		editors.add(e1);
	}

	private JTextField documentoField=new JTextField(10);

	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout("p,2dlu,p,2dlu p,2dlu,p,70dlu","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("COM ",documentoField);
		return builder.getPanel();
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "cambiarPeriodo");
		a.putValue(Action.NAME, "Periodo");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar coms en otro periodo");
		builder.add(a);
		builder.add(CommandUtils.createLoadAction(this, "load"));
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
	
	protected void updatePeriodoLabel(){
		header.setDescription("Periodo: "+periodo.toString());
	}

	public static RecepcionDeCompra seleccionar(){
		final SelectorDeComs selector=new SelectorDeComs(Configuracion.getSucursalLocalId());
		selector.setSelectionMode(ListSelection.SINGLE_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			RecepcionDeCompra res=selector.getSelected();
			return res;
		}
		return null;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println(seleccionar());
			}
		});
	}
			

}
