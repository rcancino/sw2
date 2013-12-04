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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.ActionLabel;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.ventas.model.Venta;
import com.luxsoft.sw3.services.Services;

/**
 * Selector de facturas para un cliente, A diferencia sel {@link SelectorDeFacturas}
 * este filtra por origen,sucursal y el filtro permite filtrar por fecha
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeFacturas2 extends AbstractSelector<Venta>{
	
	protected Periodo periodo=Periodo.hoy();
	
	private Sucursal sucursal;
	
	private OrigenDeOperacion origen;
	
	
	public SelectorDeFacturas2() {
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
		String props[]={
				"sucursal.nombre"
				,"origen"
				,"documento"
				,"numeroFiscal"
				,"fecha"
				,"clave"
				,"nombre"
				,"total"
				};
		String labels[]={
				"Sucursal"
				,"Tipo"
				,"Documento"
				,"Fiscal"
				,"Fecha"
				,"Cliente"
				,"Nombre"
				,"Total"
				};
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
		Action a=CommandUtils.createViewAction(this, "buscar");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Buscar facturas en otro periodo");
		builder.add(a);		
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}

	private ActionLabel periodoLabel;
	
	public ActionLabel getPeriodoLabel(){
		if(periodoLabel==null){			
			periodoLabel=new ActionLabel("Buscar: "+periodo.toString());
			periodoLabel.addActionListener(EventHandler.create(ActionListener.class, this, "cambiarPeriodo"));
		}
		return periodoLabel;
	}
	
	public void buscar(){
		BuscadorSelectivoVentas dialog=new BuscadorSelectivoVentas();
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			periodo=new Periodo(dialog.getFecha());
			setSucursal(dialog.getSucursal());
			setOrigen(dialog.getOrigen());
			load();
			updatePeriodoLabel();
		}
	}
	
	@Override
	protected void setPreferedDimension(JComponent gridComponent) {
		gridComponent.setPreferredSize(new Dimension(650,400));
	}

	@Override
	protected void onWindowOpened() {
		//load();
	}

	protected void updatePeriodoLabel(){
		header.setDescription("Periodo: "+periodo.toString());
	}

	@Override
	protected List<Venta> getData() {
		String hql="from Venta v " +
				" where v.sucursal.id=? " +
				"    and v.origen=@ORIGEN " +
				"    and v.fecha between ? and ?" +
				"";
		hql=hql.replace("@ORIGEN", "\'"+getOrigen().name()+"\'");
		Object[] params=new Object[]{
				getSucursal().getId()
				,periodo.getFechaInicial()
				,periodo.getFechaFinal()
				};
		return Services.getInstance().getHibernateTemplate()
			.find(hql, params);
	}
	
	public void clean(){		
		source.clear();
	}
	
	public Sucursal getSucursal() {
		if(sucursal==null){
			sucursal=Services.getInstance().getConfiguracion().getSucursal();
			System.out.println("Suc: "+sucursal);
		}
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		
		this.sucursal = sucursal;
	}

	public OrigenDeOperacion getOrigen() {
		if(origen==null)
			origen=OrigenDeOperacion.CRE;
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	public static Venta seleccionar(){
		SelectorDeFacturas2 selector=new SelectorDeFacturas2();
		
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			Venta venta=selector.getSelected();
			selector.clean();
			return venta;
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
				Venta res=seleccionar();
				System.out.println("Res: "+res);
				System.exit(0);
			}
			
		});
		
	}

}
