package com.luxsoft.siipap.inventario.ui.forms;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.luxsoft.siipap.compras.model.CompraUnitaria;
import com.luxsoft.siipap.compras.ui.consultas.ComprasCentralizadasController;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

@SuppressWarnings("serial")
public class ComprasPendientesForm extends SXAbstractDialog{

	public ComprasPendientesForm() {
		super("Compras pendientes", true);
		
	}
	
	private JTable grid;
	
	private EventSelectionModel selectionModel;
	
	private EventList partidasList=new BasicEventList();

	@Override
	protected JComponent buildContent() {
		String[] propertyNames={
				"compra.folio"
				,"pendiente"
				,"compra.entrega"
				};
		String[] columnLabels={
				"Compra"
				,"Pendiente"
				,"Fecha de entrega"
				};
		
		final TableFormat tf=GlazedLists.tableFormat(CompraUnitaria.class,propertyNames, columnLabels);
		SortedList sorted=new SortedList(partidasList,null);
		final EventTableModel tm=new EventTableModel(sorted,tf);
		grid=new JTable(tm);
		selectionModel=new EventSelectionModel(sorted);
		grid.setSelectionModel(selectionModel);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2){
					view();
				}	
			}			
		});
		JComponent gridComponent=ComponentUtils.createTablePanel(grid);
		gridComponent.setPreferredSize(new Dimension(750,300));
		return gridComponent;
	}
	
	public void view(){
		if(!selectionModel.getSelected().isEmpty()){
			CompraUnitaria selected=(CompraUnitaria)selectionModel.getSelected().get(0);
			ComprasCentralizadasController controller=new ComprasCentralizadasController();
			controller.mostrarCompra(selected.getCompra().getId());
		}
		
	}
	
	public EventList getPartidasList() {
		return partidasList;
	}

}
