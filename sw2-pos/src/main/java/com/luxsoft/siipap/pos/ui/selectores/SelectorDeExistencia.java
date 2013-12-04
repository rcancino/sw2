package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.ui.reports.ConteoSelectivoDeInventarioForm.Familia;
import com.luxsoft.siipap.pos.ui.reports.ReportControls;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.services.Services;
//import com.luxsoft.siipap.model.core.Producto;

/**
 * Version de Selector de productos q busca con existencias
 * @author Ruben Cancino Ramos
 *
 */
public class SelectorDeExistencia extends SXAbstractDialog{


	protected JTextField lineaField=new JTextField(10);
	protected JTextField marcaField=new JTextField(10);
	protected JTextField claseField=new JTextField(10);
	protected JTextField productoField=new JTextField(20);
	
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
	public EventSelectionModel selectionModel;
	private int selectionType=ListSelection.MULTIPLE_INTERVAL_SELECTION;
	
	private Sucursal sucursal;
	
	public SelectorDeExistencia() {
		super("Catálogo de productos");
		sucursal=Services.getInstance().getConfiguracion().getSucursal();
	}

	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		JPanel content=new JPanel(new BorderLayout(1,5));
		
		content.add(buildGridPanel(),BorderLayout.CENTER);
		content.add(buildTopPanel(),BorderLayout.NORTH);
		content.setPreferredSize(new Dimension(1050,700));
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
		builder.nextLine();
		builder.append("Línea",lineaField);
		builder.append("Clase",claseField);
		builder.append("Marca",marcaField);
		
		builder.nextLine();
		builder.append("Caras",carasField);
		builder.append("Acabado",acabadoField);
		builder.append("Presentación",presentacionField);
		builder.nextLine();
		builder.append("Ancho",anchoField);
		builder.append("Largo",largoField);
		builder.append("Calibre",calibreField);
		builder.append("Kilos",kilosField);
		builder.append("Gramos",gramosField);
		builder.append("Filtro",cantidadMatcherEditor.getSelector());
		builder.append("Estado",estadoMatcherEditor.getSelector());
		builder.append("Tipo",deLineaMatcherEditor.getSelector());
		builder.nextLine();
		builder.append("Familia Inicial",familiaInicialMatcherEditor.getInicialSelector());
		builder.append("Familia Final",familiaInicialMatcherEditor.getFinalSelector());
		//builder.append("Familia Final",familiaFinalMatcherEditor.getSelector());
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		return builder.getPanel();
	}
	
	
	CantidadMatcherEditor cantidadMatcherEditor;
	EstadoMatcherEditor estadoMatcherEditor;
	DeLineaMatcherEditor deLineaMatcherEditor;
	FamiliaInicialMatcherEditor familiaInicialMatcherEditor;
	FamiliaFinalMatcherEditor familiaFinalMatcherEditor;
	FilterList filterList;
	
	protected void initGlazedLists(){
		source=new BasicEventList<Existencia>();	
		
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		cantidadMatcherEditor=new CantidadMatcherEditor();
		estadoMatcherEditor=new EstadoMatcherEditor();
		deLineaMatcherEditor=new DeLineaMatcherEditor();
		familiaInicialMatcherEditor=new FamiliaInicialMatcherEditor();
		//familiaFinalMatcherEditor=new FamiliaFinalMatcherEditor();
		
		editors.add(cantidadMatcherEditor);
		editors.add(estadoMatcherEditor);
		editors.add(deLineaMatcherEditor);
		editors.add(familiaInicialMatcherEditor);
		//editors.add(familiaFinalMatcherEditor);
		
		editors.add(new TextComponentMatcherEditor(productoField,GlazedLists.textFilterator(new String[]{"clave","descripcion"})));		
		editors.add(new TextComponentMatcherEditor(lineaField,GlazedLists.textFilterator(new String[]{"producto.linea.nombre"})));
		
		editors.add(new TextComponentMatcherEditor(carasField,GlazedLists.textFilterator(new String[]{"producto.caras"})));
		editors.add(new TextComponentMatcherEditor(acabadoField,GlazedLists.textFilterator(new String[]{"producto.acabado"})));
		editors.add(new TextComponentMatcherEditor(presentacionField,GlazedLists.textFilterator(new String[]{"producto.presentacion"})));
		
		editors.add(new TextComponentMatcherEditor(marcaField,GlazedLists.textFilterator(new String[]{"producto.marca.nombre"})));
		editors.add(new TextComponentMatcherEditor(claseField,GlazedLists.textFilterator(new String[]{"producto.clase.nombre"})));
		
		editors.add(new TextComponentMatcherEditor(anchoField,GlazedLists.textFilterator(new String[]{"producto.ancho"})));
		editors.add(new TextComponentMatcherEditor(largoField,GlazedLists.textFilterator(new String[]{"producto.largo"})));
		editors.add(new TextComponentMatcherEditor(calibreField,GlazedLists.textFilterator(new String[]{"producto.calibre"})));
		editors.add(new TextComponentMatcherEditor(kilosField,GlazedLists.textFilterator(new String[]{"producto.kilos"})));
		editors.add(new TextComponentMatcherEditor(gramosField,GlazedLists.textFilterator(new String[]{"producto.gramos"})));
		
		
		CompositeMatcherEditor matchers=new CompositeMatcherEditor(editors);
		
		filterList=new FilterList(source,new ThreadedMatcherEditor(matchers));
		sortedSource=new SortedList(filterList,null);
	}	
	
	
	protected JComponent buildGridPanel(){
		initGlazedLists();		
		grid=ComponentUtils.getStandardTable();
		String[] props={
				"producto.linea.nombre"
				,"producto.clave"
				,"producto.descripcion"
				,"producto.lineaOrigen"
				,"cantidad"
				,"producto.nacional"
				,"producto.caras"
				,"producto.acabado"
				,"producto.presentacion"
				,"producto.clase.nombre"
				,"producto.marca.nombre"
				,"producto.ancho"
				,"producto.largo"
				,"producto.calibre"
				,"producto.kilos"
				,"producto.gramos"
				};
		String[] names={
				"Línea"
				,"clave"
				,"Desc"				
				,"Familia"
				,"Cantidad"
				,"Nac"
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
		TableFormat<Existencia> tf=GlazedLists.tableFormat(Existencia.class, props,names);
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
		List<Existencia> prods=new ArrayList<Existencia>();
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
		return Services.getInstance().getInventariosManager().buscarExistencias(sucursal);
	}
	
	public boolean isParcial(){
		//System.out.println("Existencias totales: "+source.size()+ " Seleccionadas: "+selectionModel.getSelected().size());
		return selectionModel.getSelected().size()!=source.size();
	}
	
	public void load(){
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
		TaskUtils.executeSwingWorker(worker);
	}
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	
	
	public static List<Existencia> find(){
		SelectorDeExistencia finder=new SelectorDeExistencia();
		finder.open();
		if(!finder.hasBeenCanceled()){
			return finder.selectionModel.getSelected();
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static Existencia seleccionar(){
		SelectorDeExistencia finder=new SelectorDeExistencia();
		finder.setSelectionType(ListSelection.SINGLE_INTERVAL_SELECTION);
		finder.open();
		if(!finder.hasBeenCanceled()){
			return (Existencia)finder.selectionModel.getSelected().get(0);
		}
		return null;
	}
	
	
	
	static class CantidadMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		private JComboBox selector;
		
		public CantidadMatcherEditor() {
			selector=new JComboBox(new String[]{"TODOS","POSITIVOS","NEGATIVOS","CERO","DIF CERO"});
			selector.getModel().setSelectedItem("Existencia");
			selector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final String tipo=(String)this.selector.getSelectedItem();
			//System.out.println("Seleccion: "+tipo);
			if(tipo==null || "TODOS".equals(tipo))
				fireMatchAll();
			else if("POSITIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getCantidad()>0;
					}
				});
			}else if("NEGATIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getCantidad()<0;
					}
				});
			}else if("CERO".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getCantidad()==0;
					}
				});
			}else if("DIF CERO".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getCantidad()!=0;
					}
				});
			}
		}
		
		public JComboBox getSelector(){
			return selector;
		}
	}
	
	static class EstadoMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		private JComboBox selector;
		
		public EstadoMatcherEditor() {
			selector=new JComboBox(new String[]{"ACTIVOS","INACTIVOS","TODOS"});
			selector.getModel().setSelectedItem("Seleccion");
			selector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final String tipo=(String)this.selector.getSelectedItem();
			if(tipo==null || "TODOS".equals(tipo))
				fireMatchAll();
			else if("ACTIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getProducto().isActivo();
					}
				});
			}else if("INACTIVOS".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return !exis.getProducto().isActivo();
					}
				});
			}
		}
		
		public JComboBox getSelector(){
			return selector;
		}
	}
	
	static class DeLineaMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		private JComboBox selector;
		
		public DeLineaMatcherEditor() {
			selector=new JComboBox(new String[]{"DE LINEA","ESPECIAL","TODOS"});
			selector.getModel().setSelectedItem("Seleccion Tipo");
			selector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final String tipo=(String)this.selector.getSelectedItem();
			if(tipo==null || "TODOS".equals(tipo))
				fireMatchAll();
			else if("DE LINEA".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return exis.getProducto().isDeLinea();
					}
				});
			}else if("ESPECIAL".equals(tipo)){
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						return !exis.getProducto().isDeLinea();
					}
				});
			}
		}
		
		public JComboBox getSelector(){
			return selector;
		}
	}
	
	
	static class FamiliaInicialMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		
		private JComboBox inicialSelector;
		private JComboBox finalSelector;
		
		public FamiliaInicialMatcherEditor() {
			inicialSelector=ReportControls.createFamiliasBox();
			finalSelector=ReportControls.createFamiliasBox();
			//inicialSelector.getModel().setSelectedItem("Familia");
			inicialSelector.addActionListener(this);
			finalSelector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final Familia familiaIni=(Familia)inicialSelector.getSelectedItem();
			final Familia familiaFin=(Familia)finalSelector.getSelectedItem();
			fireChanged(new Matcher(){
				public boolean matches(Object item) {
					Existencia exis=(Existencia)item;
					
					if(exis.getProducto().getLineaOrigen()!=null){
						//System.out.println("Evaluando "+exis.getProducto().getLineaOrigen());
						double clave1=0;
						double clave2=9999999999d;
						if(familiaIni!=null)
							clave1=NumberUtils.toDouble(familiaIni.getClave());
						if(familiaFin!=null)
							clave2=NumberUtils.toDouble(familiaFin.getClave());
						double val=NumberUtils.toInt(exis.getProducto().getLineaOrigen());
						boolean valido=(val>=clave1 && val<=clave2);
						//System.out.println(MessageFormat.format("FamExis: {0} Ini:{1} Fin:{2} Res:{3}", val,clave1,clave2,valido));
						return valido;
					}else
						return true;
					
				}
			});
		}

		public JComboBox getInicialSelector() {
			return inicialSelector;
		}

		public JComboBox getFinalSelector() {
			return finalSelector;
		}
		
		
	}
	
	
	static class FamiliaFinalMatcherEditor extends AbstractMatcherEditor implements ActionListener{
		private JComboBox selector;
		
		public FamiliaFinalMatcherEditor() {
			selector=ReportControls.createFamiliasBox();
			selector.getModel().setSelectedItem("Familia");
			selector.addActionListener(this);
		}
		
		public void actionPerformed(ActionEvent e) {
			final Familia familia=(Familia)selector.getSelectedItem();
			if(familia==null )
				fireMatchAll();
			else {
				fireChanged(new Matcher(){
					public boolean matches(Object item) {
						Existencia exis=(Existencia)item;
						System.out.println("Evaluando "+item);
						if(exis.getProducto().getLineaOrigen()!=null){
							Integer clave1=NumberUtils.toInt(exis.getProducto().getLineaOrigen());
							Integer clave2=NumberUtils.toInt(familia.getClave());
							return clave2<=clave1;
						}else
							return true;
						
					}
				});
				
				
			}
		}
		
		public JComboBox getSelector(){
			return selector;
		}
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				SelectorDeExistencia finder=new SelectorDeExistencia();
				finder.open();
				if(!finder.hasBeenCanceled()){
					final List<Existencia> seleccion= finder.selectionModel.getSelected();
					System.out.println("Seleccion: "+seleccion);
				}
			}
		});
	}

}
