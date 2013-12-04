package com.luxsoft.siipap.cxc.ui.form;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.controls.AbstractControl;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

public class ProductoFinder extends AbstractControl{
	
	protected EventList<Producto> source;
	protected SortedList<Producto> sortedSource;

	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		JPanel content=new JPanel(new BorderLayout(1,5));
		content.add(buildTopPanel(),BorderLayout.NORTH);
		content.add(buildGridPanel(),BorderLayout.CENTER);
		return content;
	}
	
	protected void initGlazedLists(){
		source=new BasicEventList<Producto>();
		
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		editors.add(new TextComponentMatcherEditor(lineaField,GlazedLists.textFilterator(new String[]{"linea.nombre"})));
		editors.add(new TextComponentMatcherEditor(marcaField,GlazedLists.textFilterator(new String[]{"marca.nombre"})));
		editors.add(new TextComponentMatcherEditor(claseField,GlazedLists.textFilterator(new String[]{"clase.nombre"})));
		editors.add(new TextComponentMatcherEditor(productoField,GlazedLists.textFilterator(new String[]{"clave","descripcion"})));
		CompositeMatcherEditor matcher=new CompositeMatcherEditor(editors);
		source=new FilterList(source,matcher);
		sortedSource=new SortedList<Producto>(source,null);
		
	}
	
	
	protected JTextField lineaField=new JTextField(10);;
	protected JTextField marcaField=new JTextField(10);;
	protected JTextField claseField=new JTextField(10);;
	protected JTextField productoField=new JTextField(20);;
	
	protected JPanel buildTopPanel(){
		FormLayout layout=new FormLayout(
				"p,2dlu,p, 2dlu," +
				"p,2dlu,p, 2dlu," +
				"p,2dlu,p, 2dlu," +
				"p,2dlu,f:p:g" 
				,"p");
		final PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		int row=1;
		int col=1;
		builder.addLabel("Linea", cc.xy(col, row));
		col=col+2;		
		builder.add(lineaField,cc.xy(col, row));
		col=col+2;
		builder.addLabel("Marca", cc.xy(col, row));
		col=col+2;
		builder.add(marcaField,cc.xy(col, row));
		
		col=col+2;
		builder.addLabel("Clase", cc.xy(col, row));
		col=col+2;		
		builder.add(claseField,cc.xy(col, row));
		
		col=col+2;
		builder.addLabel("Producto", cc.xy(col, row));
		col=col+2;		
		builder.add(productoField,cc.xy(col, row));
		
		return builder.getPanel();
	}
	
	JXTable grid;
	EventSelectionModel<Producto> selectionModel;
	
	protected JComponent buildGridPanel(){
		initGlazedLists();
		grid=ComponentUtils.getStandardTable();
		String[] props={"clave","descripcion","lineaOrigen","deLinea","nacional","linea.nombre","marca.nombre","clase.nombre","kilos","gramos"};
		String[] names={"Clave","Descripcion","Familia","DeLinea","Nac","Línea","Marca","Clase","Kilos","Gramos"};
		TableFormat<Producto> tf=GlazedLists.tableFormat(Producto.class, props,names);
		EventTableModel tm=new EventTableModel(sortedSource,tf);
		grid.setModel(tm);
		selectionModel=new EventSelectionModel<Producto>(sortedSource);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.addEnterAction(grid, new DispatchingAction(this,"select"));
		new TableComparatorChooser(grid,sortedSource,true);
		grid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					select();
			}			
		});
		grid.setColumnControlVisible(false);
		JScrollPane sp=new JScrollPane(grid);
		return sp;
	}
	
	public void select(){
		List<Producto> prods=new ArrayList<Producto>();
		prods.addAll(selectionModel.getSelected());
		dispose();
		onSelection(prods);
	}
	
	public void onSelection(final List<Producto> selected){
		
	}
	
	public void dispose(){
		source.clear();
	}
	
	SwingWorker<List<Producto>,String> worker;
	
	private boolean deLinea=true;
	
	public void load(){
		worker=new SwingWorker<List<Producto>, String>(){
			@Override
			protected List<Producto> doInBackground() throws Exception {
				String hql="from Producto p where p.activo=true and p.inventariable=true ";
				if(isDeLinea())
					hql="from Producto p where p.activo=true and p.inventariable=true and p.deLinea=true ";
				return ServiceLocator2.getHibernateTemplate().find(hql);
				
			}
			@Override
			protected void done() {
				source.clear();
				try {
					source.addAll(get());
					grid.packAll();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	
	public boolean isDeLinea() {
		return deLinea;
	}

	public void setDeLinea(boolean deLinea) {
		this.deLinea = deLinea;
	}

	public static Producto findwithDialog(){
		List<Producto> res=findWithDialog(true);
		return res.isEmpty()?null:res.get(0);
	}
	
	public static List<Producto> findWithDialog(){
		return findWithDialog(true);
	}
	
	public static List<Producto> findWithDialog(boolean deLinea){
		final ProductoFinderDialog dialog=new ProductoFinderDialog();
		dialog.setDeLinea(deLinea);
		dialog.open();
		
		return dialog.getProductos();
	}
	
	
	
	static class  ProductoFinderDialog extends SXAbstractDialog{
		
		private List<Producto> productos=new ArrayList<Producto>();
		
		private boolean deLinea=true;

		public ProductoFinderDialog() {
			super("Productos activos");
			
		}

		@Override
		protected JComponent buildContent() {			
			return finder.getControl();
		}
		
		@Override
		protected void onWindowOpened() {
			finder.setDeLinea(isDeLinea());
			finder.load();
		}


		ProductoFinder finder=new ProductoFinder(){

			@Override
			public void onSelection(List<Producto> selected) {
				setProductos(selected);
				doAccept();
			}
			
		};

		public List<Producto> getProductos() {
			return productos;
		}

		public void setProductos(List<Producto> productos) {
			this.productos = productos;
		}

		public boolean isDeLinea() {
			return deLinea;
		}

		public void setDeLinea(boolean deLinea) {
			this.deLinea = deLinea;
		}
		
		
	}
	
	public static void main(String[] args) {
		List<Producto> selected=findWithDialog();
		System.out.println(selected);
		System.exit(0);
	}

}
