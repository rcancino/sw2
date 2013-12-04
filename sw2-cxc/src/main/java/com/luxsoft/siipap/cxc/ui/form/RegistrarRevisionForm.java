package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Registra un grupo de documentos como revisados
 * 
 * @author Ruben Cancino
 *
 */
public class RegistrarRevisionForm extends SXAbstractDialog{
	
	private final EventList<Cargo> cuentas;

	public RegistrarRevisionForm(final EventList<Cargo> cuentas) {
		super("Recepción de documentos");
		this.cuentas=cuentas;
	}

	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new BorderLayout(2,5));		
		panel.add(buildGrid(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	private TableFormat<Cargo> createCXCTableFormat(){
		String props[]={"cobrador.id","diaRevision","diaDelPago","sucursal.clave","documento","numeroFiscal","fecha","vencimiento","total","saldo","atraso"};
		String names[]={"Cob","Dia Rev","Dia Cob","Suc","Docto","Fiscal","Fecha","Vto","Total","Saldo","Atraso"};
		return GlazedLists.tableFormat(Cargo.class,props, names);
	}
	
	
	private JXTable grid;
	private EventSelectionModel selectionModel;
	
	private JComponent buildGrid(){
		grid=ComponentUtils.getStandardTable();
		grid.setColumnControlVisible(false);
		SortedList sortedList=new SortedList(cuentas,null);
		EventTableModel tm=new EventTableModel(sortedList,createCXCTableFormat());
		grid.setModel(tm);		
		new TableComparatorChooser(grid,sortedList,true);
		selectionModel=new EventSelectionModel(sortedList);
		JComponent c=ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(450,430));
		ComponentUtils.addDeleteAction(grid, new com.luxsoft.siipap.swing.actions.DispatchingAction(this,"delete"));
		return c;
	}
	
	public void delete(){
		if(!selectionModel.isSelectionEmpty()){
			int min=selectionModel.getMinSelectionIndex();
			int max=selectionModel.getMaxSelectionIndex();
			for(int index=min;index<=max;index++){
				cuentas.remove(index);
			}
		}
	}	
	

	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Faturas revisadas","Marca las facturas seleccionadas como revisadas");
	}
	
	public static List<Cargo> showForm(final List<Cargo> cuentas){		
		final RegistrarRevisionForm form=new RegistrarRevisionForm(GlazedLists.eventList(cuentas));
		form.open();
		if(!form.hasBeenCanceled()){
			return form.cuentas;			
		}
		return null;
	}
	
	
	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable(){
			public void run() {
				List<Cargo> cuentas=new ArrayList<Cargo>();//ServiceLocator2.getCXCManager().buscarCuentasPorCobrar(new Cliente("C030486",""), OrigenDeOperacion.CRE); 
				cuentas=showForm(cuentas);
			}			
		});
	}

}
