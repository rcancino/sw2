package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
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
import ca.odell.glazedlists.matchers.ThreadedMatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Selector de Cuentas por cobrar para un cliente
 * 
 * @author Ruben Cancino 
 *
 */
public abstract class SelectorDeCXC extends JPanel{
	
	private Cliente cliente;
	private OrigenDeOperacion origen;
	
	public SelectorDeCXC() {
		setLayout(new BorderLayout(2,5));
		init();
	}

	private JTextField inputField;
	private JButton loadBtn;
	
	protected void init(){
		inputField=new JTextField(20);
		loadBtn=new JButton(CommandUtils.createLoadAction(this, "load"));
		add(buildFilterPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
	}
	
	protected JComponent buildFilterPanel(){
		FormLayout layout=new FormLayout("40dlu,3dlu,f:p:g,3dlu,p","f:p");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.addLabel("Filtrar: ",cc.xy(1, 1));
		builder.add(inputField,cc.xy(3,1));
		builder.add(loadBtn,cc.xy(5, 1));
		//builder.setDefaultDialogBorder();
		return builder.getPanel();
	}
	
	private JXTable grid;
	private EventList source;
	protected EventSelectionModel selectionModel;
	
	protected JComponent buildGridPanel(){
		grid=ComponentUtils.getStandardTable();
		grid.setColumnControlVisible(false);
		source=new BasicEventList();
		final EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		editors.add(new TextComponentMatcherEditor(inputField,GlazedLists.textFilterator(new String[]{"documento","sucursal.nombre","clave","nombre"})));
		final MatcherEditor editor=new CompositeMatcherEditor(editors);
		final FilterList filterList=new FilterList(source,new ThreadedMatcherEditor(editor));
		SortedList sorted=new SortedList(filterList,null);
		final EventTableModel tm=new EventTableModel(sorted,getTableFormat());
		selectionModel=new EventSelectionModel(sorted);
		
		grid.setModel(tm);
		TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid .setSelectionModel(selectionModel);
		
		final Action select=new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				doSelect();
			}
		};
		ComponentUtils.addEnterAction(grid, select);
		grid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) doSelect();
			}			
		});
		grid.packAll();
		
		JComponent c= ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(750,400));
		return c;
	}
	
	public void doSelect(){
		List dat=new ArrayList();
		dat.addAll(selectionModel.getSelected());
		onSelection(dat);
	}
	
	abstract void onSelection(final List data);

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	
	protected List<Cargo> getData() {
		if(cliente!=null )
			return ServiceLocator2.getCXCManager().buscarCuentasPorCobrar(cliente,origen);
		else
			return new ArrayList<Cargo>();
	}

	
	protected TableFormat<Cargo> getTableFormat() {
		String props[]={"clave","sucursal.nombre","fecha","origen","tipoDocto","documento","numeroFiscal","precioNeto"
				,"total","devoluciones","descuentos","bonificaciones","descuentoNota","pagos","saldoCalculado","descuentoFinanciero"};
		String labels[]={"Cliente","Sucursal","Fecha","Origen","Tipo","Documento","Fiscal","P.N."
				,"Total","Devs","Descs","Bonific","Des(Nota)","Pagos","Saldo","D.F"};
		return GlazedLists.tableFormat(Cargo.class,props,labels);
	}
	
	/**
	 * Carga los registros en un sub-proceso que ejecuta el metodo getData()
	 * 
	 * @see getData()
	 */
	public void load(){
		final SwingWorker<List, String> worker=new SwingWorker<List, String>(){			
			protected List doInBackground() throws Exception {				
				return getData();
			}
			protected void done() {				
				try {
					List res=get();
					source.clear();
					source.addAll(res);
				} catch (Exception e) {
					MessageUtils.showError("Error al cargar datos", e);
				}finally{
					grid.packAll();
				}
			}
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	
	

	/**
	 * Regresa las cuentas por cobrar del cliente indicado
	 * 
	 * @param cliente
	 * @return
	 */
	public static List<Cargo> seleccionar(final Cliente cliente,OrigenDeOperacion origen){
		
		String nombre=cliente!=null?cliente.getLabel():"";
		String title="Facturas/Cargos  pendies para: " +nombre;
		DialogoSelectorDeCxC dialog=new DialogoSelectorDeCxC("");
		dialog.setTitle(title);
		dialog.selector.setCliente(cliente);
		dialog.selector.setOrigen(origen);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			List data=new ArrayList();
			data.addAll(dialog.selector.selectionModel.getSelected());			
			dialog=null;
			return data;			
			
		}
		return null;
	}
	
	public static class DialogoSelectorDeCxC extends SXAbstractDialog{
		
		protected SelectorDeCXC selector;

		public DialogoSelectorDeCxC(String title) {
			super(title);
			selector=new SelectorDeCXC(){

				@Override
				void onSelection(List data) {
					doAccept();
				}
				
			};
			
		}

		@Override
		protected JComponent buildContent() {
			
			return selector;
		}

		@Override
		protected void onWindowOpened() {
			selector.load();
		}		
		
		
	}
	
	public static void main(String[] args) {
		SWExtUIManager.setup();
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				List res=seleccionar(new Cliente("I020376","Litopolis"),null);
				System.out.println(res);
				System.exit(0);
			}
			
		});
		//System.exit(0);
	}

}
