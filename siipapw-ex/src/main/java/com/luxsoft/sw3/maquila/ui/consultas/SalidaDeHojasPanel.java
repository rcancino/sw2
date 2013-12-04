package com.luxsoft.sw3.maquila.ui.consultas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.ListSelection;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.maquila.model.EntradaDeMaquila;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.model.Periodo;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.AbstractMasterDatailFilteredBrowserPanel;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.Renderers;
import com.luxsoft.siipap.swing.utils.TaskUtils;
import com.luxsoft.sw3.maquila.model.RecepcionDeCorteDet;
import com.luxsoft.sw3.maquila.model.SalidaDeHojasDet;
import com.luxsoft.sw3.maquila.task.parches.ActualizarCostos;
import com.luxsoft.sw3.maquila.ui.forms.SalidaDeHojasForm;
import com.luxsoft.sw3.maquila.ui.reportes.ReporteInventarioHojeado;
import com.luxsoft.sw3.maquila.ui.selectores.SelectorDeHojeadoDisponible;

/**
 * Panel para el mantenimiento de entradas de maquila
 * 
 * @author Ruben Cancino
 *
 */
public class SalidaDeHojasPanel extends AbstractMasterDatailFilteredBrowserPanel<RecepcionDeMaquila, EntradaDeMaquila>{

	public SalidaDeHojasPanel() {
		super(RecepcionDeMaquila.class);
	}
	
	@Override
	protected void agregarMasterProperties() {
		addProperty("sucursal.nombre","fecha","documento","proveedor.nombre","comentario");
		addLabels("sucursal","fecha","Docto","Proveedor","Comentario");
		
		setDetailTitle("Entradas al inventario (MAQ)");
		//installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal.nombre"});
		installTextComponentMatcherEditor("Sucursal", "sucursal.nombre");
		installTextComponentMatcherEditor("Proveedor", "proveedor.nombre");		
		installTextComponentMatcherEditor("Folio", "documento");
		manejarPeriodo();
	}

	@Override
	protected TableFormat createDetailTableFormat() {
		String[] props={
				"sucursal.nombre"
				,"fecha"
				,"documento"
				,"remision"
				,"clave"
				,"descripcion"
				,"cantidad"
				,"atendido"
				,"pendiente"
				,"costoFlete"
				,"costoCorte"
				,"costoMateria"
				,"costo"
				,"comentario"
				};
		String[] names={
				"Sucursal"
				,"Fecha"
				,"Maq"
				,"Remisión"
				,"Producto"
				,"Descripción"
				,"Cantidad"
				,"Atendido"
				,"Pendiente"
				,"Flete"
				,"Hojeo"
				,"Costo M.P."
				,"Costo"
				,"Comentario"
				};
		return GlazedLists.tableFormat(EntradaDeMaquila.class, props,names);
	}

	@Override
	protected Model<RecepcionDeMaquila, EntradaDeMaquila> createPartidasModel() {
		return new Model<RecepcionDeMaquila, EntradaDeMaquila>(){
			public List<EntradaDeMaquila> getChildren(RecepcionDeMaquila parent) {
				return getHibernateTemplate().find("from EntradaDeMaquila e where e.recepcion.id=?",parent.getId());
			}
		};
	}
	
	@Override
	public Action[] getActions() {
		if(actions==null)
			actions=new Action[]{
				getLoadAction()
				,addAction(null, "registrarSalida", "Registrar Salida")
				,addAction(null, "consultarSalidas", "Consultar Salidas")
				,addAction(null, "actualizarCostos", "Actualizar Costos")
				};
		return actions;
	}
	
	
	@Override
	protected List<Action> createProccessActions() {
		List<Action> procesos=super.createProccessActions();
		procesos.add(addAction("", "reporte1", "Reporte 1"));
		procesos.add(addAction("", "reporteDeInvHojeado", "Reporte De Inventario Hojeado"));
		return procesos;
	}
	
	public void reporteDeInvHojeado(){
		ReporteInventarioHojeado.run();
	}
	
	private JCheckBox pendientesBox;
	
	public JComponent[] getOperacionesComponents(){
		if(pendientesBox==null){
			pendientesBox=new JCheckBox("Pendientes",false);
			pendientesBox.setOpaque(false);
		}
		return new JComponent[]{pendientesBox};
	}
	
	protected void adjustDetailGrid(final JXTable grid){
		grid.getColumnExt("Cantidad").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.getColumnExt("Atendido").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.getColumnExt("Pendiente").setCellRenderer(Renderers.buildBoldDecimalRenderer(1));
		grid.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) consultarSalidas();
			}			
		});
	}
		
	
	@Override
	protected List<RecepcionDeMaquila> findData() {
		if(pendientesBox.isSelected()){
			return buscarPendientes();
		}else
			return buscarPorPeriodo();		
	}
	
	private List<RecepcionDeMaquila> buscarPorPeriodo(){
		String hql="from RecepcionDeMaquila r where r.fecha>='2012-01-01' and r.fecha between ? and ?";
		Object params[]={periodo.getFechaInicial(),periodo.getFechaFinal()};
		return getHibernateTemplate().find(hql,params);
	}
	
	private List<RecepcionDeMaquila> buscarPendientes(){
		String hql="from RecepcionDeMaquila r " +
				" where r.id in( select distinct e.recepcion.id from EntradaDeMaquila e where e.cantidad-e.atendido-atendidoDirecto>0)";		
		return getHibernateTemplate().find(hql);
	}
	
	private RecepcionDeCorteDet buscarCapaInicializada(Long id){
		String hql="from RecepcionDeCorteDet e " +
				" left join fetch e.salidas s" +
				" where e.id=?";
		List<RecepcionDeCorteDet> res=getHibernateTemplate().find(hql,id);
		return res.isEmpty()?null:res.get(0);
	}
	
	public void registrarSalida(){
		if(!detailSelectionModel.isSelectionEmpty()){
			List<EntradaDeMaquila> maqs=new ArrayList<EntradaDeMaquila>(detailSelectionModel.getSelected());
			for(EntradaDeMaquila maq:maqs){
				RecepcionDeCorteDet entrada=SelectorDeHojeadoDisponible.find(maq.getProducto());
				
				if(entrada==null)
					continue;
				entrada=buscarCapaInicializada(entrada.getId());
				if(entrada.getDisponible()>=maq.getPendiente())
					entrada.setCantidadDeSalida(maq.getPendiente());
				if(entrada.getDisponible()<maq.getPendiente())
					entrada.setCantidadDeSalida(entrada.getDisponible());
				SalidaDeHojasForm form=new SalidaDeHojasForm(entrada);
				form.setRequerido(maq.getPendiente());
				form.open();				
				if(!form.hasBeenCanceled()){
					if(entrada.getCantidadDeSalida()>0){
						SalidaDeHojasDet salida=new SalidaDeHojasDet();
						salida.setCantidad(entrada.getCantidadDeSalida());
						salida.setDestino(maq);
						salida.setFecha(maq.getFecha());
						Assert.isTrue(maq.getProducto().equals(entrada.getProducto()),"El producto no es el mismo para la entrada y salida");
						salida.setProducto(entrada.getProducto());
						entrada.agregarSalida(salida);
						getHibernateTemplate().merge(salida);
						/*int index=detailSelectionModel.getSelected().indexOf(maq);
						if(index!=-1){
							maq=(EntradaDeMaquila)getHibernateTemplate().get(EntradaDeMaquila.class, maq.getId());
							detailSelectionModel.getSelected().set(index, maq);
						}*/
							
					}
				}
			}
		}
		refreshDetailSelection();
	}
	
	private void refreshDetailSelection(){
		if(!detailSelectionModel.isSelectionEmpty()){
			List<EntradaDeMaquila> maqs=new ArrayList<EntradaDeMaquila>(detailSelectionModel.getSelected());
			for(EntradaDeMaquila maq:maqs){
				int index=detailSelectionModel.getSelected().indexOf(maq);
				if(index!=-1){
					maq=(EntradaDeMaquila)getHibernateTemplate().get(EntradaDeMaquila.class, maq.getId());
					detailSelectionModel.getSelected().set(index, maq);
				}
			}
		}
	}
	
	public void consultarSalidas(){
		if(!detailSelectionModel.isSelectionEmpty()){
			EntradaDeMaquila maq=(EntradaDeMaquila)detailSelectionModel.getSelected().get(0);
			String hql="from SalidaDeHojasDet s where s.destino.id=?";
			List<SalidaDeHojasDet> salidas=getHibernateTemplate().find(hql,maq.getId());
			SalidasAsignadas form=new SalidasAsignadas();
			form.getSalidas().addAll(salidas);
			form.open();
			if(form.isModificado())
				refreshDetailSelection();
		}
	}
	
	protected void executeLoadWorker(final SwingWorker worker){
		TaskUtils.executeSwingWorker(worker);
	}
	
	public void actualizarCostos(){
		int year=Periodo.obtenerYear(periodo.getFechaFinal());
		int mes=Periodo.obtenerMes(periodo.getFechaFinal())+1;
		ActualizarCostos.actualizarCostosDeMaquila(year, mes);
	}
	
	protected HibernateTemplate getHibernateTemplate(){
		return ServiceLocator2.getHibernateTemplate();
	}
	
	
	/**
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	public static class SalidasAsignadas extends SXAbstractDialog{
		
		private  EventList<SalidaDeHojasDet> salidas;
		private boolean modificado=false;

		public SalidasAsignadas() {
			super("Salidas asignadas");			
		}

		@Override
		protected JComponent buildContent() {
			JPanel panel=new JPanel(new BorderLayout());
			panel.add(buildToolbar(),BorderLayout.NORTH);
			panel.add(buildGridPanel(),BorderLayout.CENTER);
			panel.add(buildButtonBarWithClose(),BorderLayout.SOUTH);
			
			return panel;
		}
		
		private Component buildToolbar() {
			ToolBarBuilder builder=new ToolBarBuilder();
			builder.add(CommandUtils.createDeleteAction(this, "eliminar"));
			return builder.getToolBar();
		}
		
		private EventSelectionModel selectionModel;

		private Component buildGridPanel() {
			String[] props={"origen.id","id","destino.documento","producto.clave","fecha"
					,"cantidad","costo","costoPorMillar"};
			String[] names={"Entrada","Salida","MAQ","Producto","Fecha","Cantidad","Costo","Costo x Mill"};
			TableFormat tf=GlazedLists.tableFormat(props,names);			
			SortedList sorted=new SortedList(getSalidas(),null);
			final EventTableModel tm=new EventTableModel(sorted,tf);
			selectionModel=new EventSelectionModel(sorted);
			selectionModel.setSelectionMode(ListSelection.SINGLE_SELECTION);
			final JXTable grid=ComponentUtils.getStandardTable();
			grid.setModel(tm);
			grid.setSelectionModel(selectionModel);			
			TableComparatorChooser.install(grid, sorted, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
			grid.packAll();
			JComponent res=ComponentUtils.createTablePanel(grid);
			res.setPreferredSize(new Dimension(400,250));
			return res;
		}
		
		
		
		public EventList<SalidaDeHojasDet> getSalidas() {
			if(salidas==null){
				salidas=new BasicEventList<SalidaDeHojasDet>();
			}
			return salidas;
		}

		public void setSalidas(EventList<SalidaDeHojasDet> salidas) {
			this.salidas = salidas;
		}
		
		
		
		public boolean isModificado() {
			return modificado;
		}

		public void setModificado(boolean modificado) {
			this.modificado = modificado;
		}
		
		

		public void eliminar(){
			if(!selectionModel.isSelectionEmpty()){
				SalidaDeHojasDet sal=(SalidaDeHojasDet)selectionModel.getSelected().get(0);
				ServiceLocator2.getHibernateTemplate().delete(sal);
				salidas.remove(sal);
				setModificado(true);
			}
		}
		
	}

}
