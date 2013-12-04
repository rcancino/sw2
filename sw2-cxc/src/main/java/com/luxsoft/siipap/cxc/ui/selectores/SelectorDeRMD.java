package com.luxsoft.siipap.cxc.ui.selectores;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
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

import org.hibernate.HibernateException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateCallback;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.UniqueList;
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
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.SWExtUIManager;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.ventas.model.Devolucion;

/**
 * Selector de RMD
 * 
 * @author Ruben Cancino 
 *
 */
public abstract class SelectorDeRMD extends JPanel{
	
	private OrigenDeOperacion origen=OrigenDeOperacion.CRE;
	
	
	public SelectorDeRMD() {
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
		source=new UniqueList(new BasicEventList(),GlazedLists.beanPropertyComparator(Devolucion.class, "id"));
		final EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		editors.add(new TextComponentMatcherEditor(inputField,GlazedLists.textFilterator(new String[]{"numero","venta.nombre","venta.sucursal.nombre"})));
		final MatcherEditor editor=new CompositeMatcherEditor(editors);
		final FilterList filterList=new FilterList(source,new ThreadedMatcherEditor(editor));
		SortedList sorted=new SortedList(filterList,null);
		final EventTableModel tm=new EventTableModel(sorted,getTableFormat());
		selectionModel=new EventSelectionModel(sorted);
		
		grid.setModel(tm);
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
		TableComparatorChooser.install(grid,sorted,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		JComponent c= ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(750,400));
		return c;
	}
	
	public void doSelect(){
		List dat=new ArrayList();
		dat.addAll(selectionModel.getSelected());
		onSelection(dat);
	}
	
	
	
	public OrigenDeOperacion getOrigen() {
		return origen;
	}

	public void setOrigen(OrigenDeOperacion origen) {
		this.origen = origen;
	}

	abstract void onSelection(final List data);

	
	protected List<Devolucion> getData() {
		String hql3="from Devolucion d where " +
		"d  not in(select nd.devolucion from NotaDeCreditoDevolucion nd)" +
		" and d.venta.origen=\'"+getOrigen().name()+"\'";		
		return ServiceLocator2.getHibernateTemplate().find(hql3);
	}

	
	protected TableFormat<Devolucion> getTableFormat() {
		String props[]={"numero","venta.sucursal.nombre","venta.nombre","fecha","venta.documento","venta.numeroFiscal","venta.origen","total","venta.descuentos","autorizada","venta.saldoCalculado"};
		String labels[]={"Folio","Sucursal","Cliente","Fecha","Factura","Fiscal","Origen","Importe","Descuentos","Autorizada","Saldo"};
		return GlazedLists.tableFormat(Devolucion.class,props,labels);
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
	 * Regresa una lista de devoluciones aplicables
	 * 
	 * @param cliente
	 * @return
	 */
	public static List<Devolucion> seleccionarAplicables(){		
		
		
		DialogSelector dialog=new DialogSelector("");
		dialog.setTitle("Devoluciones pendientes por aplicar");
		
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			List data=new ArrayList();
			data.addAll(dialog.selector.selectionModel.getSelected());			
			dialog=null;
			return data;			
			
		}
		return new ArrayList<Devolucion>(0);
	}
	
	/**
	 * Regresa una lista de devoluciones aplicables
	 * 
	 * @param cliente
	 * @return
	 */
	public static List<Devolucion> seleccionarAplicables(OrigenDeOperacion origen){		
		
		
		DialogSelector dialog=new DialogSelector("");
		dialog.setTitle("Devoluciones pendientes por aplicar");
		dialog.setOrigen(origen);
		dialog.open();
		if(!dialog.hasBeenCanceled()){
			List data=new ArrayList();
			data.addAll(dialog.selector.selectionModel.getSelected());			
			dialog=null;
			return data;			
			
		}
		return new ArrayList<Devolucion>(0);
	}
	
	public static class DialogSelector extends SXAbstractDialog{
		
		protected SelectorDeRMD selector;

		public DialogSelector(String title) {
			super(title);
			//setModal(false);
			selector=new SelectorDeRMD(){

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
		
		public void setOrigen(OrigenDeOperacion origen){
			selector.setOrigen(origen);
		}
	}
	
	public static void main(String[] args) {
		/*final String hql3="from Devolucion d where " +
		"d  not in(select nd.devolucion from NotaDeCreditoDevolucion nd)" +
		" order by d.id";		
		ServiceLocator2.getHibernateTemplate().execute(new HibernateCallback(){
			
			public Object doInHibernate(Session session)throws HibernateException, SQLException {
				ScrollableResults rs=session.createQuery(hql3).scroll();
				try {
					while(rs.next()){
						Devolucion dev=(Devolucion)rs.get()[0];
						System.out.println("Dev: "+dev);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			
		});*/
		SWExtUIManager.setup();		
		SwingUtilities.invokeLater(new Runnable(){
			 
			public void run() {
				List res=seleccionarAplicables();
				System.out.println(res);
				System.exit(0);
			}
			
		});
		
	}

}
