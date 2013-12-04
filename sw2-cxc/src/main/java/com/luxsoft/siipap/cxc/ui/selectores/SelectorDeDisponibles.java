package com.luxsoft.siipap.cxc.ui.selectores;

import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Abono;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;


/**
 * Selector de facturas para un cliente, por periodo
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeDisponibles extends AbstractSelector<Abono>{
	
	
	protected Cliente cliente;
	
	private SelectorDeDisponibles() {
		super(Abono.class, "Pagos disponibles de aplicación");
	}
	
	@Override
	protected TableFormat<Abono> getTableFormat() {
		String props[]={"origen","tipo","folio","fecha","total","disponible","comentario"};
		String labels[]={"Origen","Tipo","Folio","Fecha","Total","Disponible","Comentario"};
		return GlazedLists.tableFormat(Abono.class,props,labels);
	}
	
	private HeaderPanel header;
	
	protected JComponent buildHeader(){
		String title=cliente!=null?cliente.getNombreRazon():"NA";
		header=new HeaderPanel(title,"Abonos disponibles");
		return header;
	}
	
	protected JComponent buildToolbar(){
		final JToolBar bar=new JToolBar();
		ToolBarBuilder builder=new ToolBarBuilder(bar);
		Action a=CommandUtils.createViewAction(this, "load");
		a.putValue(Action.NAME, "Buscar");
		a.putValue(Action.SHORT_DESCRIPTION, "Refrescar");
		builder.add(a);	
		builder.add(getAplicarAction());
		builder.add(buildFilterPanel());		
		return builder.getToolBar();
	}
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	protected void afterLoad(){
		CantidadMonetaria total=CantidadMonetaria.pesos(0);
		for(Abono a:source){
			total=total.add(a.getDisponibleCM());
		}
		header.setDescription("Total disponible: "+total);
	}

	@Override
	protected List<Abono> getData() {
		return ServiceLocator2.getCXCManager().buscarDisponibles(cliente);
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
	
	private Action aplicarDiferenciaAction;
	
	public Action getAplicarAction(){
		if(aplicarDiferenciaAction==null){
			aplicarDiferenciaAction=new DispatchingAction(this,"aplicarDiferencia");
			aplicarDiferenciaAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/folder_wrench.png"));
			aplicarDiferenciaAction.putValue(Action.SHORT_DESCRIPTION, "Saldar por diferencia");
			
		}
		return aplicarDiferenciaAction;
	}
	
	public void aplicarDiferencia(){
		int index=selectionModel.getMaxSelectionIndex();
		Abono abono=source.get(index);
		Abono res=CXCUIServiceFacade.generarAplicacionPorDiferencia(abono);
		if(res!=null){
			source.set(index, res);
		}
	}
	
	
	public static Abono buscar(final Cliente c){
		SelectorDeDisponibles selector=new SelectorDeDisponibles();
		selector.setCliente(c);
		selector.open();
		if(!selector.hasBeenCanceled()){
			selector.clean();
			return selector.getSelected();
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
				buscar(new Cliente("C050355","Impresos litopolis"));
				System.exit(0);
			}
			
		});
		
	}

}
