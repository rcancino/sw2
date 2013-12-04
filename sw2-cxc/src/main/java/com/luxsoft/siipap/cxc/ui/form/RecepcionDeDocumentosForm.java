package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.ui.model.RecepcionDeDocumentosModel;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * Forma para la recepcion de documentos por el departamento de
 * Cargo
 * 
 * @author Ruben Cancino
 *
 */
public class RecepcionDeDocumentosForm extends SXAbstractDialog{
	
	private RecepcionDeDocumentosModel model;

	public RecepcionDeDocumentosForm(final RecepcionDeDocumentosModel model) {
		super("Recepción de documentos");
		this.model=model;
		model.getFechaModel().addValueChangeListener(new FechaHandler());
		
	}

	@Override
	protected JComponent buildContent() {
		JPanel panel=new JPanel(new BorderLayout(2,5));
		panel.add(buildFormPanel(),BorderLayout.NORTH);
		panel.add(buildGrid(),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		return panel;
	}
	
	private JComponent buildFormPanel(){
		FormLayout layout=new FormLayout("p,3dlu,70dlu","p");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.addLabel("Fecha: ",cc.xy(1, 1));
		builder.add(Binder.createDateComponent(model.getFechaModel()),cc.xy(3, 1));
		builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	
	private JXTable grid;
	private EventSelectionModel selectionModel;
	
	private JComponent buildGrid(){
		grid=ComponentUtils.getStandardTable();
		grid.setColumnControlVisible(false);
		SortedList sortedList=new SortedList(model.getCuentas(),null);
		EventTableModel tm=new EventTableModel(sortedList,model.buildTableFormat());
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
				model.getCuentas().remove(index);
			}
		}
	}
	
	public void validar(){
		try {
			model.validar();
			getOKAction().setEnabled(true);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(RecepcionDeDocumentosForm.this
					, e.getMessage(),"Recepción incorrecta",JOptionPane.ERROR_MESSAGE);
			getOKAction().setEnabled(false);
		}
	}

	@Override
	protected JComponent buildHeader() {
		return new HeaderPanel("Recepción de Documentos","Facutras y Notas de cargos recibidas en el departamento de CxC");
	}
	
	public static List<Cargo> showForm(final List<Cargo> cuentas){
		final RecepcionDeDocumentosModel model=new RecepcionDeDocumentosModel(cuentas);
		final RecepcionDeDocumentosForm form=new RecepcionDeDocumentosForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			try {
				model.actualizar();
				return model.getCuentas();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(form, e.getMessage(),"Recepción incorrecta",JOptionPane.ERROR_MESSAGE);
			}
			
		}
		return null;
	}
	
	private class FechaHandler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			validar();
		}		
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
