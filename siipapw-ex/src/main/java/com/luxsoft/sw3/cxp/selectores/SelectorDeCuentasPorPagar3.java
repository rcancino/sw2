package com.luxsoft.sw3.cxp.selectores;

import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxp.model.CXPCargo;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.MonedasUtils;
import com.luxsoft.sw3.cxp.consultas.CargoRow;


/**
 * 
 * 
 * @author Ruben Cancino 
 *
 */
public  class SelectorDeCuentasPorPagar3 extends AbstractSelector<CargoRow>{
	
	
	protected Proveedor proveedor;
	protected Currency moneda;
	
	private SelectorDeCuentasPorPagar3() {
		super(CargoRow.class, "Cuentas por pagar");
		
	}
	
	protected JTextField docField=new JTextField(10);
	
	@Override
	protected void installEditors(EventList<MatcherEditor<CargoRow>> editors) {
		textFilter=new JTextField(10);
		TextComponentMatcherEditor reqEditor=new TextComponentMatcherEditor(textFilter
				,GlazedLists.textFilterator(new String[]{"requisicion"}));
		TextComponentMatcherEditor docEditor=new TextComponentMatcherEditor(docField,
				GlazedLists.textFilterator(new String[]{"documento"}));
		editors.add(reqEditor);
		editors.add(docEditor);
	}

	protected JComponent buildFilterPanel(){
		ButtonBarBuilder builder=ButtonBarBuilder.createLeftToRightBuilder();
		//builder.addLabel("Requisición");
		//builder.addRelatedGap();
		
		builder.addUnrelatedGap();
		builder.addGridded(new JLabel("Requisición"));
		builder.addRelatedGap();
		builder.addGridded(textFilter);
		builder.addRelatedGap();
		builder.addGridded(new JLabel("Factura"));
		builder.addRelatedGap();
		builder.addGridded(docField);
		builder.addGlue();
		return builder.getPanel();
	}

	@Override
	protected TableFormat<CargoRow> getTableFormat() {
		String props[]={
				"requisicion"
				,"cxp"
				,"documento"
				,"fecha"
				,"vencimiento"
				,"moneda"
				,"total"
				,"pagos"
				,"saldo"
				};
		String labels[]={
				"Requisición"
				,"Folio"
				,"Factura"
				,"Fecha"
				,"Vto"
				,"Moneda"
				,"Total"
				,"Pagos"
				,"Saldo"
				};
		return GlazedLists.tableFormat(CargoRow.class,props,labels);
	}
	
	private HeaderPanel header=new HeaderPanel("","");
	
	protected JComponent buildHeader(){
		updateHeader();
		return header;
	}
	
	protected void setPreferedDimension(JComponent gridComponent){
		gridComponent.setPreferredSize(new Dimension(810,500));
	}
	
	

	protected void updateHeader(){
		header.setDescription("Cuentas por pagar   ");
		if(proveedor!=null)
			header.setTitle(proveedor.getNombreRazon());
	}

	@Override
	protected List<CargoRow> getData() {
		return CargoRow.buscarPendientes(proveedor.getId(), moneda.getCurrencyCode());
	}
	
	public void clean(){
		proveedor=null;
		source.clear();
	}	

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	

	public void setMoneda(Currency moneda) {
		this.moneda = moneda;
	}
	
	
	public static List<CXPCargo> buscarCuentasPorPagar(final Proveedor p,Currency moneda){
		SelectorDeCuentasPorPagar3 selector=new SelectorDeCuentasPorPagar3();//getInstance();
		selector.setProveedor(p);
		selector.setMoneda(moneda);
		selector.setSelectionMode(ListSelection.MULTIPLE_INTERVAL_SELECTION);
		selector.open();
		if(!selector.hasBeenCanceled()){
			List<CXPCargo> facturas=new ArrayList<CXPCargo>();
			for(CargoRow rd:selector.getSelectedList()){
				CXPCargo cargo=(CXPCargo)ServiceLocator2.getUniversalDao().get(CXPCargo.class, rd.getCxp());
				facturas.add(cargo);
			}
			selector.clean();
			return facturas;
		}		
		return new ArrayList<CXPCargo>(0);
		
	}
	
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SWExtUIManager.setup();
		SwingUtilities.invokeAndWait(new Runnable(){
			 
			public void run() {
				buscarCuentasPorPagar(ServiceLocator2.getProveedorManager()
						.buscarPorClave("C003"),MonedasUtils.PESOS);
				System.exit(0);
			}
			
		});
		
	}

}
