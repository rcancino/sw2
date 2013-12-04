package com.luxsoft.siipap.pos.ui.selectores;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.collections.ListUtils;
import org.jdesktop.swingx.JXTable;
import org.springframework.beans.BeanUtils;

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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import com.luxsoft.luxor.utils.Bean;
import com.luxsoft.siipap.inventarios.model.Existencia;
import com.luxsoft.siipap.inventarios.model.ExistenciaConteo;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.model.Sucursal;
import com.luxsoft.siipap.pos.POSRoles;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.siipap.util.DBUtils;
import com.luxsoft.siipap.util.DateUtil;
import com.luxsoft.sw3.inventarios.form.MantenimientoDeExistenciasForm;
import com.luxsoft.sw3.services.Services;

public class SelectorDeExistencias extends SXAbstractDialog{


	protected JTextField lineaField=new JTextField(10);;
	protected JTextField marcaField=new JTextField(10);;
	protected JTextField claseField=new JTextField(10);;
	protected JTextField productoField=new JTextField(20);;
	
	protected EventList source;
	protected SortedList sortedSource;
	protected JXTable grid;
	protected EventSelectionModel<Existencia> selectionModel;
	private Sucursal sucursal;
	private int year;
	private int mes;
	
	public SelectorDeExistencias() {
		super("Inventario  ");
	}

	@Override
	protected JComponent buildContent() {
		initGlazedLists();
		JPanel content=new JPanel(new BorderLayout(1,5));
		content.add(buildTopPanel(),BorderLayout.NORTH);
		content.add(buildGridPanel(),BorderLayout.CENTER);
		return content;
	}
	
	protected JPanel buildTopPanel(){
		FormLayout layout=new FormLayout(
				" p,2dlu,p:g,1dlu," +
				" p,2dlu,p,  1dlu," +
				" p,2dlu,p,  1dlu," +
				" p,2dlu,p,3dlu,p" 
				,"");
		
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Producto",productoField);
		builder.append("Línea",lineaField);
		builder.append("Clase",claseField);
		builder.append("Marca",marcaField);
		builder.append(getRecorteButton());
		return builder.getPanel();
	}
	
	
	protected void initGlazedLists(){
		source=new BasicEventList<Existencia>();		
		EventList<MatcherEditor> editors=new BasicEventList<MatcherEditor>();
		editors.add(new TextComponentMatcherEditor(lineaField,GlazedLists.textFilterator(new String[]{"producto.linea.nombre"})));
		editors.add(new TextComponentMatcherEditor(marcaField,GlazedLists.textFilterator(new String[]{"producto.marca.nombre"})));
		editors.add(new TextComponentMatcherEditor(claseField,GlazedLists.textFilterator(new String[]{"producto.clase.nombre"})));
		editors.add(new TextComponentMatcherEditor(productoField,GlazedLists.textFilterator(new String[]{"clave","descripcion"})));
		CompositeMatcherEditor matcher=new CompositeMatcherEditor(editors);
		source=new FilterList(source,matcher);
		sortedSource=new SortedList(source,null);
	}	
	
	protected TableFormat createTableFormat(){
		String[] props={
				"producto.clave"
				,"producto.descripcion"
				,"cantidad"
				,"recorte"
				,"unidad"				
				,"producto.linea.nombre"
				,"producto.clase.nombre"
				,"producto.marca.nombre"
				,"recorteComentario"
				,"recorteFecha"
				,"year"
				,"mes"
				};
		
		String[] names={
				"Clave"
				,"Descripción"
				,"Existencia"
				,"Recorte"
				,"Uni"				
				,"Línea"
				,"Clase"
				,"Marca"
				,"Recorte Comentario"
				,"Recorte (Fecha)"
				,"Año"
				,"Mes"
				
		};
		return GlazedLists.tableFormat(Existencia.class, props,names);
	}
	
	protected JComponent buildGridPanel(){
		initGlazedLists();		
		grid=ComponentUtils.getStandardTable();		
		EventTableModel tm=new EventTableModel(sortedSource,createTableFormat());
		grid.setModel(tm);
		selectionModel=new EventSelectionModel(sortedSource);
		grid.setSelectionModel(selectionModel);
		ComponentUtils.addEnterAction(grid, new DispatchingAction(this,"select"));
		TableComparatorChooser.install(grid,sortedSource,TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		grid.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					select();
			}			
		});
		grid.setColumnControlVisible(true);
		JComponent c =ComponentUtils.createTablePanel(grid);
		c.setPreferredSize(new Dimension(870,450));
		return c;
	}
	
	public void select(){
		List prods=new ArrayList();
		prods.addAll(selectionModel.getSelected());	
		doClose();
		onSelection(selectionModel.getSelected());
	}
	
	public void onSelection(final List<Existencia> selected){
		
	}
	
	protected List findData(){	
		return Services.getInstance().getInventariosManager().buscarExistencias(sucursal);
	}
	
	public void load(){
		setTitle("Inventario Sucursal: "+getSucursal()+ "        Periodo: "+getYear()+" / "+getMes());
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
	
	private JButton recorteButton;
	
	public JButton getRecorteButton(){
		if(recorteButton==null){
			Action edit=CommandUtils.createEditAction(this,"edit", POSRoles.ADMINISTRACION_INVENTARIOS.name());
			recorteButton=new JButton(edit);
			
		}
		return recorteButton;
	}
	
	public void edit(){
		if(!selectionModel.isSelectionEmpty()){
			Existencia selected=selectionModel.getSelected().get(0);
			int index=source.indexOf(selected);
			if(index!=-1){
				Existencia target=(Existencia)Bean.proxy(Existencia.class);
				BeanUtils.copyProperties(selected, target);
				final DefaultFormModel model=new DefaultFormModel(target);
				final MantenimientoDeExistenciasForm form=new MantenimientoDeExistenciasForm(model);
				form.open();
				if(!form.hasBeenCanceled()){					
					selected.setRecorte(target.getRecorte());
					selected.setRecorteComentario(target.getRecorteComentario());
					selected.setRecorteFecha(new Date());
					selected=(Existencia)Services.getInstance().getUniversalDao().save(selected);
					source.set(index, selected);
					selectionModel.setSelectionInterval(index, index);					
				}
			}
			
		}
		
	}
	
	
	@Override
	protected void onWindowOpened() {
		load();
	}
	
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public static List<Existencia> find(){
		SelectorDeExistencias finder=new SelectorDeExistencias();
		finder.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		finder.setYear(Periodo.obtenerYear(fecha));
		finder.setMes(Periodo.obtenerMes(fecha)+1);
		finder.open();
		if(!finder.hasBeenCanceled()){
			return finder.selectionModel.getSelected();
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static Existencia seleccionar(){
		SelectorDeExistencias finder=new SelectorDeExistencias();
		finder.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
		Date fecha=Services.getInstance().obtenerFechaDelSistema();
		finder.setYear(Periodo.obtenerYear(fecha));
		finder.setMes(Periodo.obtenerMes(fecha)+1);
		finder.open();
		if(!finder.hasBeenCanceled()){
			if(!finder.selectionModel.isSelectionEmpty())
				return finder.selectionModel.getSelected().get(0);
			return null;
		}
		return null;
	}
	
	public static List<Existencia> find(final Sucursal sucursal,Date fecha){
		SelectorDeExistencias finder=new SelectorDeExistencias();
		finder.setSucursal(sucursal);
		finder.setYear(Periodo.obtenerYear(fecha));
		finder.setMes(Periodo.obtenerMes(fecha)+1);
		finder.open();
		if(!finder.hasBeenCanceled()){
			return finder.selectionModel.getSelected();
		}
		return ListUtils.EMPTY_LIST;
	}
	
	public static Existencia seleccionar(final Sucursal sucursal,Date fecha){
		List<Existencia> res=find(sucursal,fecha);
		return res.isEmpty()?null:res.get(0);
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				List<Existencia> selected=find();
				System.out.println(selected);
				System.exit(0);
			}

		});
	}

}
