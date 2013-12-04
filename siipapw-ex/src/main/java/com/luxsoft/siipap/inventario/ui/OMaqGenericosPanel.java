package com.luxsoft.siipap.inventario.ui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.apache.commons.collections.ListUtils;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;

import com.jgoodies.uif.panel.SimpleInternalFrame;
import com.luxsoft.siipap.inventario.MovimientoForm;
import com.luxsoft.siipap.inventarios.model.Movimiento;
import com.luxsoft.siipap.inventarios.model.MovimientoDet;
import com.luxsoft.siipap.inventarios.service.InventarioManager;
import com.luxsoft.siipap.maquila.model.RecepcionDeMaquila;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.sw3.services.MaquilaManager;

/**
 * Panel con la lista de movimientos de inventario
 * 
 * @author Ruben Cancino Ramos
 *
 */
public class OMaqGenericosPanel extends FilteredBrowserPanel<RecepcionDeMaquila>{
	
	

	public OMaqGenericosPanel() {
		super(RecepcionDeMaquila.class);
		addProperty("id","fecha","sucursal","comentario");
		addLabels("ID","Fecha","sucursal","comentario");
		installTextComponentMatcherEditor("Sucursal", new String[]{"sucursal.nombre"});
	}

	
	private MaquilaManager getManager(){
		return ServiceLocator2.getMaquilaManager();
	}

	@Override
	protected JComponent buildContent() {
		JComponent parent=super.buildContent();
		JSplitPane sp=new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		sp.setOneTouchExpandable(true);
		sp.setResizeWeight(.4);
		sp.setTopComponent(parent);
		sp.setBottomComponent(buildDeailPanel());
		return sp;
	}
	
	JXTable detailGrid;
	
	private JComponent buildDeailPanel(){
		
		EventList eventLst=selectionModel.getSelected();
		CollectionList partidasList=new CollectionList(eventLst,createPartidasModel());
		SortedList sortedDetail=new SortedList(partidasList,null);
		EventTableModel tm=new EventTableModel(sortedDetail,createDetailTableFormat());
		
		detailGrid=ComponentUtils.getStandardTable();
		detailGrid.setModel(tm);
		new TableComparatorChooser(detailGrid,sortedDetail,true);
		JScrollPane sp=new JScrollPane(detailGrid);
		SimpleInternalFrame frame=new SimpleInternalFrame("Productos");
		frame.setContent(sp);
		return frame;
	}
	
	protected TableFormat createDetailTableFormat(){
		String[] props={"producto.clave","producto.descripcion","cantidad"};
		String[] cols={"Producto","Descripcion","recibido"};
		return GlazedLists.tableFormat(MovimientoDet.class, props, cols);
	}
	
	protected Model createPartidasModel(){
		return new Model(){
			public List getChildren(Object parent) {
				Movimiento m=(Movimiento)parent;
				return new ArrayList(m.getPartidas());
			}
		};
	}



	@Override
	protected List<RecepcionDeMaquila> findData() {
		//return ServiceLocator2.getMaquilaManager().getAll();
		return ListUtils.EMPTY_LIST;
	}
	
	
	/**** Altas/Bajas/Cambios ***/

	@Override
	protected RecepcionDeMaquila doInsert() {
		RecepcionDeMaquila m=new RecepcionDeMaquila();
		m=OrdenDeMaquilaForm.showForm(m);
		if(m!=null){
			//m=ServiceLocator2.getMaquilaManager().save(m);
			return m;
		}
		return null;
	}

	@Override
	protected void doSelect(Object bean) {
		RecepcionDeMaquila m=(RecepcionDeMaquila)bean;
		m=OrdenDeMaquilaForm.showForm(m,true);
	}



	@Override
	public boolean doDelete(RecepcionDeMaquila bean) {
		//ServiceLocator2.getMaquilaManager().remove(bean.getId());
		return true;
		 
	}
	
	
	@Override
	protected RecepcionDeMaquila doEdit(RecepcionDeMaquila bean) {
		/*RecepcionDeMaquila m=getManager().get(bean.getId());
		m=OrdenDeMaquilaForm.showForm(m, false);
		if(m!=null){
			System.out.println("Actualizando Orden de Maq: "+m.getId()+"Comentario: "+m.getComentario());
			RecepcionDeMaquila res=getManager().save(m);
			return res;
		}*/
		return bean;
	}


	
	/**** END Altas/Bajas/Cambios ***/	

}
