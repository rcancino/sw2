package com.luxsoft.sw3.pedidos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.siipap.model.core.ProductoRow;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class SelectorDeProductosRow extends SXAbstractDialog{


	protected JTextField lineaField=new JTextField(10);;
	protected JTextField marcaField=new JTextField(10);;
	protected JTextField claseField=new JTextField(10);
	protected JTextField productoField=new JTextField(20);;
	
	protected JTextField carasField=new JTextField(10);
	protected JTextField acabadoField=new JTextField(10);
	protected JTextField presentacionField=new JTextField(10);
	
	protected JTextField anchoField=new JTextField(10);
	protected JTextField largoField=new JTextField(10);
	protected JTextField calibreField=new JTextField(10);
	protected JTextField kilosField=new JTextField(10);
	protected JTextField gramosField=new JTextField(10);
	
	protected EventList source;
	protected SortedList sortedSource;
	protected JXTable grid;
	protected EventSelectionModel selectionModel;
	private int selectionType=ListSelection.MULTIPLE_INTERVAL_SELECTION;
	
	public SelectorDeProductosRow() {
		super("Catálogo de productos");		
	}

	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		JPanel content=new JPanel(new BorderLayout(1,5));
		content.add(buildTopPanel(),BorderLayout.NORTH);
		content.add(buildGridPanel(),BorderLayout.CENTER);
		content.setPreferredSize(new Dimension(950,500));
		return content;
	}
	
	protected JPanel buildTopPanel(){
		FormLayout layout=new FormLayout(
				" p,2dlu,p,	1dlu," +
				" p,2dlu,p, 1dlu," +
				" p,2dlu,p, 1dlu," +
				" p,2dlu,p, 1dlu," +
				" p,2dlu,p" 
				,"");
		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",productoField);
		builder.append("Línea",lineaField);
		builder.nextLine();
		
		builder.append("Caras",carasField);
		builder.append("Acabado",acabadoField);
		builder.append("Presentación",presentacionField);
		builder.nextLine();		
		builder.append("Clase",claseField);
		builder.append("Marca",marcaField);
		builder.nextLine();
		
		builder.append("Ancho",anchoField);
		builder.append("Largo",largoField);
		builder.append("Calibre",calibreField);
		builder.append("Kilos",kilosField);
		builder.append("Gramos",gramosField);
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	
	
	protected void initGlazedLists(){
		source=new BasicEventList();		
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		
		editors.add(new TextComponentMatcherEditor(productoField,GlazedLists.textFilterator(new String[]{"clave","descripcion"})));		
		editors.add(new TextComponentMatcherEditor(lineaField,GlazedLists.textFilterator(new String[]{"linea"})));
		
		editors.add(new TextComponentMatcherEditor(carasField,GlazedLists.textFilterator(new String[]{"caras"})));
		editors.add(new TextComponentMatcherEditor(acabadoField,GlazedLists.textFilterator(new String[]{"acabado"})));
		editors.add(new TextComponentMatcherEditor(presentacionField,GlazedLists.textFilterator(new String[]{"presentacion"})));
		
		editors.add(new TextComponentMatcherEditor(marcaField,GlazedLists.textFilterator(new String[]{"marca"})));
		editors.add(new TextComponentMatcherEditor(claseField,GlazedLists.textFilterator(new String[]{"clase"})));
		
		editors.add(new TextComponentMatcherEditor(anchoField,GlazedLists.textFilterator(new String[]{"ancho"})));
		editors.add(new TextComponentMatcherEditor(largoField,GlazedLists.textFilterator(new String[]{"largo"})));
		editors.add(new TextComponentMatcherEditor(calibreField,GlazedLists.textFilterator(new String[]{"calibre"})));
		editors.add(new TextComponentMatcherEditor(kilosField,GlazedLists.textFilterator(new String[]{"kilos"})));
		editors.add(new TextComponentMatcherEditor(gramosField,GlazedLists.textFilterator(new String[]{"gramos"})));
		
		CompositeMatcherEditor matcher=new CompositeMatcherEditor(editors);
		source=new FilterList(source,matcher);
		sortedSource=new SortedList(source,null);
	}	
	
	
	
	public EventSelectionModel getSelectionModel() {
		return selectionModel;
	}

	protected JComponent buildGridPanel(){
		initGlazedLists();		
		grid=ComponentUtils.getStandardTable();
		String[] props={
				"clave"
				,"descripcion"
				,"nacional"
				,"linea"
				,"caras"
				,"acabado"
				,"presentacion"
				,"clase"
				,"marca"
				,"ancho"
				,"largo"
				,"calibre"
				,"kilos"
				,"gramos"
				};
		String[] names={
				"clave"
				,"Desc"
				,"Nac"
				,"Línea"
				,"Caras"
				,"Acabado"
				,"Presentacion"
				,"Clase"
				,"Marca"
				,"Ancho"
				,"Largo"
				,"Calibre"
				,"Kilos"
				,"Gramos"
				};
		TableFormat tf=GlazedLists.tableFormat(ProductoRow.class, props,names);
		EventTableModel tm=new EventTableModel(sortedSource,tf);
		grid.setModel(tm);
		selectionModel=new EventSelectionModel(sortedSource);
		selectionModel.setSelectionMode(getSelectionType());
		grid.setSelectionModel(selectionModel);
		ComponentUtils.addEnterAction(grid, new DispatchingAction(this,"select"));
		TableComparatorChooser.install(grid,sortedSource,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					select();
			}			
		});
		grid.setColumnControlVisible(false);
		return ComponentUtils.createTablePanel(grid);
	}
	
	public void select(){
		List<ProductoRow> prods=new ArrayList<ProductoRow>();
		prods.addAll(selectionModel.getSelected());	
		doClose();
		onSelection(selectionModel.getSelected());
	}
	
	public void onSelection(final List selected){
	}
	
	
	public int getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(int selectionType) {
		this.selectionType = selectionType;
	}

	protected List findData(){
		return PedidoUtils.getProductos();
	}
	
	public void load(){
		source.clear();
		try {
			source.addAll(findData());
			grid.packAll();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		/*
		final SwingWorker<List,String> worker=new SwingWorker<List, String>(){
			@Override
			protected List doInBackground() throws Exception {
				return findData();
			}
			@Override
			protected void done() {
				source.clear();
				try {
					source.addAll(get());
					grid.packAll();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		};
		TaskUtils.executeSwingWorker(worker);*/
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	
	
	public static ProductoRow seleccionar(){
		SelectorDeProductosRow finder=new SelectorDeProductosRow();
		finder.setSelectionType(ListSelection.SINGLE_INTERVAL_SELECTION);
		finder.open();
		if(!finder.hasBeenCanceled()){
			return (ProductoRow)finder.selectionModel.getSelected().get(0);
		}
		return null;
	}
	
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				PedidoUtils.initProductos();
				System.out.println(seleccionar());
				System.exit(0);
			}

		});
	}

}
