package com.luxsoft.sw3.pedidos.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.luxsoft.siipap.inventarios.model.Existencia;

import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoRow;

import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.controls.SXTable;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.pedidos.SelectorDeProductosRow;
import com.luxsoft.sw3.services.Services;

import com.luxsoft.sw3.ventas.PedidoDet;

public class PedidosPendientesForm extends AbstractForm{
	
	 EventList<PedidosPendientesRow> pendientes;
	

	
	public PedidosPendientesForm(PedidoDetFormModel2 model) {
		super(model);		
		setTitle("Pedidos Pendientes ");
		pendientes=new BasicEventList<PedidosPendientesRow>();
	}
	
	private PedidoDetFormModel2 getDetModel(){
		return (PedidoDetFormModel2)getModel();
	}
	
	@Override
	protected JComponent buildFormPanel(){
		FormLayout layout=new FormLayout("p","p,2dlu,p,2dlu,t:p:g");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
	//	builder.add(buildFormMainPanel(),cc.xy(1, 1));
	//	builder.addSeparator("Pedidos Pendientes", cc.xy(1, 3));
		builder.add(buildPedidosPendientesPanel(),cc.xy(1,5));
		return builder.getPanel();
	}

	

	
	@Override
	protected JComponent addReadOnly(String property) {		
		JComponent res=super.addReadOnly(property);
		res.setBorder(null);
		return res;
	}

	@Override
	protected JComponent createCustomComponent(String property) {
		
		return super.createCustomComponent(property);
	}
	

	
	

	
	
	@Override
	protected void onWindowOpened() {
	//	getDetModel().updateHeader();
	}

	private JComponent buildPedidosPendientesPanel(){		
		final TableFormat tf=GlazedLists.tableFormat(PedidosPendientesRow.class
				, new String[]{"folio","fecha","clave","descripcion","solicitado","depurado","depuracion","entregado","ultimaEntrada","pendiente"} 
				,new String[] {"folio","fecha","clave","descripcion","solicitado","depurado","depuracion","entregado","ultimaEntrada","pendiente"} 
		);
		EventTableModel tm=new EventTableModel(getPedidosPendientes(),tf);		
		 JXTable grid=buildTable();
		 grid.setModel(tm);
		 grid.setColumnControlVisible(false);
		 grid.setFocusable(false);
		 grid.packAll();
		 JComponent c=ComponentUtils.createTablePanel(grid);
		 c.setPreferredSize(new Dimension(900,150));
		 return c;
	}
	
	 public JXTable buildTable(){		
			JXTable grid=new SXTable();
	    	grid.setColumnControlVisible(false);
			grid.setHorizontalScrollEnabled(true);
			ColorHighlighter hl = new  ColorHighlighter(Color.BLUE,Color.WHITE, new HighlightPredicate(){
				public boolean isHighlighted(Component renderer,ComponentAdapter adapter) {
					return adapter.row==getRow();
				}
			});
			grid.setRolloverEnabled(true);
			
			
			grid.setSortable(false);
			grid.getSelectionMapper().setEnabled(false);
			grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
			return grid;
	    }
	 
	 public EventList<PedidosPendientesRow>  getPedidosPendientes(){
		 
		Long suc=Services.getInstance().getConfiguracion().getSucursal().getId();
		
		
		

	
		 String sql="select C.folio,C.fecha,X.clave,x.descripcion,X.solicitado,X.depurado,X.depuracion "+
				 " ,IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0) AS entregado "+ 
				 " ,(SELECT MAX(I.FECHA) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID) AS ultimaEntrada "+
				 " ,((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0) AS pendiente "+
				 " from sx_compras2 C JOIN sx_compras2_det X  ON(C.COMPRA_ID=X.COMPRA_ID) WHERE X.CLAVE=? AND x.SUCURSAL_ID in (?) "+ 
				 " AND ((X.SOLICITADO-X.DEPURADO))-IFNULL((SELECT SUM(I.CANTIDAD) FROM sx_inventario_com I WHERE I.COMPRADET_ID=X.COMPRADET_ID),0)>0 ";
			
		pendientes.addAll(Services.getInstance().getJdbcTemplate().query(sql, new Object[]{getDetModel().getPedidoDet().getClave(),suc},new BeanPropertyRowMapper(PedidosPendientesRow.class)));
		 	 	 
 	 	// pendientes.addAll(Services.getInstance().getJdbcTemplate().query(sql, new BeanPropertyRowMapper(PedidosPendientesRow.class)));
		 	 	 
		return pendientes;
		 
	 }
	

	
	private Long row;
	
	public Long getRow(){
		if(row==null){
			String sucursal=System.getProperty("sw3.pedidos.sucursal.row","0");
			Long id=Long.valueOf(sucursal);
			row=id;
		}
		return row;
	}	
	


	

	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				final PedidoDet det=PedidoDet.getPedidoDet();
				
				PedidoDetFormModel2 model=new PedidoDetFormModel2(det);
				model.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
				PedidosPendientesForm form=new PedidosPendientesForm(model);
			//	form.setProductos(Services.getInstance().getProductosManager().getActivosAsRows());
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Ped: "+ToStringBuilder.reflectionToString(model.getBaseBean()));
				}
				System.exit(0);
				
			}
		});
	}	
	
	
}

