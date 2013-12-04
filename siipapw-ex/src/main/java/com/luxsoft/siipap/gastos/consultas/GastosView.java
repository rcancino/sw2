package com.luxsoft.siipap.gastos.consultas;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.jdesktop.swingx.JXTable;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.CollectionList;
import ca.odell.glazedlists.CompositeList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.CollectionList.Model;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.swing.EventTableModel;

import com.luxsoft.siipap.model.gastos.GCompra;
import com.luxsoft.siipap.model.gastos.GCompraDet;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;
import com.luxsoft.siipap.swing.browser.InternalTaskAdapter;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.views2.DefaultTaskView;
import com.luxsoft.siipap.swing.views2.InternalTaskTab;

public class GastosView extends DefaultTaskView{
	
	private InternalTaskTab tab;
	
	public void showTab(){
		if(tab==null){
			ComprasPanel panel=new ComprasPanel();
			InternalTaskAdapter adapter=new InternalTaskAdapter(panel);
			tab=new InternalTaskTab(adapter);
		}
		addTab(tab);
	}
	
	
	
	public static void main(String[] args) {
		SXAbstractDialog dialog=new SXAbstractDialog("TEST"){
			
			private GastosView view;

			@Override
			protected JComponent buildContent() {
				view=new GastosView();
				return view.getContent();
			}

			@Override
			protected void onWindowOpened() {
				view.showTab();
			}
			
			
			
			
		};
		dialog.setResizable(true);
		dialog.open();
	}
	
	
	private class ComprasPanel extends FilteredBrowserPanel<GCompra>{

		public ComprasPanel() {
			super(GCompra.class);
			addProperty("id","sucursal","proveedor","tipo","total","fecha");
			addLabels("Id","Sucursal","Proveedor","Tipo","Total","Fecha");
		}
		
		protected JComponent buildContent() {
			JSplitPane panel=new JSplitPane(JSplitPane.VERTICAL_SPLIT,super.buildContent(),buildDatailPanel());
			return panel;
		}
		
		private JXTable detallesGrid;
		
		private JComponent buildDatailPanel(){
			detallesGrid=ComponentUtils.getStandardTable();
			ComponentUtils.decorateActions(detallesGrid);
			EventTableModel<GCompraDet> tm=new EventTableModel<GCompraDet>(getPartidasList(),getPartidasTableformat());
			detallesGrid.setModel(tm);
			JScrollPane sp=new JScrollPane(detallesGrid);
			return sp;
		}
		
		private EventList<GCompraDet> getPartidasList(){
			EventList<GCompra> source= selectionModel.getSelected();
			Model<GCompra, GCompraDet> model=new CollectionList.Model<GCompra, GCompraDet>(){
				public List<GCompraDet> getChildren(GCompra parent) {					
					return new ArrayList<GCompraDet>(parent.getPartidas());
				}
			};
			CollectionList<GCompra, GCompraDet> comList=new CollectionList<GCompra, GCompraDet>(source,model);
			return comList;
		}
		
		
		
		@Override
		protected List<GCompra> findData() {
			String hql="from GCompra c " +
					"left join fetch c.partidas";
			HibernateTemplate tm=new HibernateTemplate(ServiceLocator2.getSessionFactory());
			tm.setMaxResults(50);
			return tm.find(hql);
		}

		private TableFormat<GCompraDet> getPartidasTableformat(){
			String props[]={"id","producto","cantidad"};
			return GlazedLists.tableFormat(GCompraDet.class, props,props);
		}
		
	}

}
