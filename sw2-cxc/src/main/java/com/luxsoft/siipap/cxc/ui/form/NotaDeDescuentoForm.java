package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.component.UIFButton;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.AplicacionDeNota;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCreditoDescuento;
import com.luxsoft.siipap.cxc.ui.model.NotasDeDescuentoModel;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.ventas.model.Venta;

/**
 * Forma para la generacion de notas de credito de descuento
 * en batch
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class NotaDeDescuentoForm extends AbstractForm{
		

	public NotaDeDescuentoForm(NotasDeDescuentoModel model) {
		super(model);
	}
	public NotasDeDescuentoModel getMainModel(){
		return (NotasDeDescuentoModel)getModel();
	}
	
	@Override
	protected JComponent buildFormPanel() {
		JPanel panel=new JPanel(new BorderLayout(5,10));
		panel.add(buildHeaderPanel(),BorderLayout.NORTH);
		panel.add(buildEditorPanel(),BorderLayout.CENTER);
		return panel;
	}
	
	
	
	private JComponent buildHeaderPanel(){
		JPanel panel=new JPanel(new BorderLayout(5,10));		
		String desc="";
		String title="Cliente: "+getMainModel().getCliente();
		final HeaderPanel header=new HeaderPanel(title,desc);
		panel.add(header,BorderLayout.CENTER);
		return panel;
	}
	
	
	private EventSelectionModel selectionModel;
	
	public JComponent buildEditorPanel(){
		
		FormLayout layout=new FormLayout("p,2dlu,p,3dlu,p,2dlu,f:p:g","");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.appendTitle("Aplicaciones disponibles");		
		builder.nextLine();		
		builder.append("Fecha de Aplicación",getControl("fecha"));
		builder.append(crearAplicarButton());
		builder.nextLine();
		builder.append(buildGridPanel(),7);
		builder.appendTitle("Notas por generar");
		builder.nextLine();
		builder.append("Folio fiscal",addReadOnly("folio"));
		builder.append(crearResetButton());
		builder.nextLine();
		builder.append(buildNotasPanel(),7);
		return builder.getPanel();
	}
	
	protected UIFButton crearAplicarButton() {
		UIFButton b=new UIFButton("Aplicar");
		b.addActionListener(EventHandler.create(ActionListener.class, getMainModel(), "procesar"));
		return b;
	}
	
	protected UIFButton crearResetButton(){
		UIFButton b=new UIFButton("Limpiar");
		b.addActionListener(EventHandler.create(ActionListener.class, getMainModel(), "reset"));
		return b;
	}
	
	private JComponent buildGridPanel(){
		JTable grid=new JTable();
		Comparator<AplicacionDeNota> c1=GlazedLists.beanPropertyComparator(AplicacionDeNota.class, "detalle.sucursal");
		Comparator<AplicacionDeNota> c2=GlazedLists.beanPropertyComparator(AplicacionDeNota.class, "detalle.documento");
		List<Comparator<AplicacionDeNota>> list=Arrays.asList(c1,c2);
		SortedList sortedList=new SortedList(getMainModel().getAplicaciones(),GlazedLists.chainComparators(list));
		String[] props={"cargo.documento","cargo.fecha","cargo.total","cargo.devoluciones","cargo.bonificaciones","cargo.descuentos","cargo.saldoSinPagos","cargo.descuentoNota","importe"};
		String[] labels={"Docto","Fecha","Tot","Devoluciones","Bonificaciones","Descuentos","Saldo","Desc","Por Aplicar"};
		final TableFormat<AplicacionDeNota> tf=GlazedLists.tableFormat(AplicacionDeNota.class, props,labels);
		EventTableModel tm=new EventTableModel(sortedList,tf);
		selectionModel=new EventSelectionModel(sortedList);
		grid.setModel(tm);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.addDeleteAction(grid, new DispatchingAction(this,"delete"));
		JComponent res=ComponentUtils.createTablePanel(grid);
		res.setPreferredSize(new Dimension(650,350));
		return res;
	}
	
	
	
	private JComponent buildNotasPanel(){
		JTable grid=new JTable();
		final SortedList sortedNotas=new SortedList(getMainModel().getNotas(),null);
		TableFormat tf=GlazedLists.tableFormat(NotaDeCreditoDescuento.class, new String[]{"nombre","folio","fecha","total","comentario"}
		,new String[]{"Cliente","Número F.","Fecha","Total","Comentario"});
		final EventTableModel tm=new EventTableModel(sortedNotas,tf);
		grid.setModel(tm);
		TableComparatorChooser.install(grid, sortedNotas, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		JComponent res=ComponentUtils.createTablePanel(grid);
		res.setPreferredSize(new Dimension(200,200));
		return res;
	}
	
	public void delete(){
		int min=selectionModel.getMaxSelectionIndex();
		int max=selectionModel.getMaxSelectionIndex();
		for(int index=min;index<=max;index++){
			getMainModel().getAplicaciones().remove(index);
		}
	}
	
		
	public static List<NotaDeCreditoDescuento>  showForm(final List<Cargo> cargos){
		//Verificar q el cliente no tenga atrasos
		final NotasDeDescuentoModel model=new NotasDeDescuentoModel(cargos);
		//model.asignarFolio();
		final NotaDeDescuentoForm form=new NotaDeDescuentoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			return model.getNotas();
		}
		return null;
	}
	
	public static void main(String[] args) {
		DBUtils.whereWeAre();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				SWExtUIManager.setup();
				Venta v=ServiceLocator2.getVentasManager().get("8a8a8189-21b68550-0121-b6881306-005e");
				List<Cargo> cargos=new ArrayList<Cargo>();
				cargos.add(v);
				showForm(cargos);
				System.exit(0);
			}			
		});		
	}
	
	

}
