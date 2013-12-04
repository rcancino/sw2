package com.luxsoft.siipap.swx.catalogos;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.JTextField;

import org.aspectj.bridge.MessageUtil;
import org.jdesktop.swingx.JXTable;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.CompositeMatcherEditor;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.cxc.ui.form.ProductoFinder;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.model.core.ProductoPorProveedor;
import com.luxsoft.siipap.model.core.Proveedor;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;


/**
 * 
 * @author Ruben Cancino
 *
 */
public class ProveedorProductosPanel2 extends JPanel{
	
	private final Proveedor proveedor;
	
	private EventList<ProductoPorProveedor> partidas;
	private EventSelectionModel selectionModel;
	
	
	
	public ProveedorProductosPanel2(final Proveedor proveedor){
		this.proveedor=proveedor;
		partidas=new BasicEventList<ProductoPorProveedor>();
		final EventList<MatcherEditor<ProductoPorProveedor>> editors=new BasicEventList<MatcherEditor<ProductoPorProveedor>>();
		
		descripcionLocal=new JTextField(20);		
		TextFilterator<ProductoPorProveedor> filter1=GlazedLists.textFilterator("producto.clave","producto.descripcion");
		TextComponentMatcherEditor<ProductoPorProveedor> e1=new TextComponentMatcherEditor<ProductoPorProveedor>(this.descripcionLocal, filter1);
		editors.add(e1);
		
		descripcionProveedor=new JTextField(20);
		TextFilterator<ProductoPorProveedor> filter2=GlazedLists.textFilterator("claveProv","descripcionProv");
		TextComponentMatcherEditor<ProductoPorProveedor> e2=new TextComponentMatcherEditor<ProductoPorProveedor>(this.descripcionProveedor, filter2);
		editors.add(e2);
		
		CompositeMatcherEditor<ProductoPorProveedor> editor=new CompositeMatcherEditor<ProductoPorProveedor>(editors);
		partidas=new FilterList<ProductoPorProveedor>(partidas,editor);
		partidas=new SortedList<ProductoPorProveedor>(partidas,null);
		partidas.addAll(proveedor.getProductos());
		init();
	}
	
	private void init(){
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
	}
	
	private JTextField descripcionLocal;
	private JTextField descripcionProveedor;
	
	private JComponent buildFormPanel(){
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:p:g(.5), 3dlu" +
				",p,2dlu,f:p:g(.5)" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);	
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Filtros");
		builder.append("Desc Siipap",descripcionLocal);
		builder.append("Desc Prov",descripcionProveedor);
		return builder.getPanel();
		
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final TableFormat<ProductoPorProveedor> tf=GlazedLists.tableFormat(ProductoPorProveedor.class
			,new String[]{"producto.descripcion","claveProv","descripcionProv"}
			,new String[]{"Producto","Clave Prov","DescripcionProv"}
		,new boolean[]{false,true,true}
		);
		
		final EventTableModel<ProductoPorProveedor> tm=new EventTableModel<ProductoPorProveedor>(partidas,tf);
		final JXTable table=ComponentUtils.getStandardTable();
		table.setModel(tm);
		selectionModel=new EventSelectionModel(partidas);
		table.setSelectionModel(selectionModel);
		TableComparatorChooser.install(table, (SortedList<ProductoPorProveedor>)partidas, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
		final JScrollPane sp=new JScrollPane(table);
		sp.setPreferredSize(new Dimension(200,250));
		return sp;
	}	
	
	private JButton[] buttons;
	
	private JButton[] getButtons(){
		if(buttons==null){
			buttons=new JButton[3];
			buttons[0]=buildAddButton();
			buttons[1]=buildRemoveButton();
			buttons[2]=buildAsingarPredeterminados();
		}
		
		return buttons;
	}
	
	public void setEnabled(boolean val){
		super.setEnabled(val);
		for(JButton b:buttons){
			b.setEnabled(val);
		}
	}
	
	public void nuevo(){
		List<Producto> selected=ProductoFinder.findWithDialog(false);
		for(Producto p:selected){
			ProductoPorProveedor pp=new ProductoPorProveedor(proveedor, p);
			boolean res=proveedor.agregarProducto(pp);
			if(res)
				partidas.add(pp);
		}
	}
	
	public void eliminar(){
		if(!selectionModel.isSelectionEmpty()){
			List<ProductoPorProveedor> selected=new ArrayList<ProductoPorProveedor>(selectionModel.getSelected());
			for(ProductoPorProveedor pp:selected){
				int index=partidas.indexOf(pp);
				if(index!=-1){
					if(proveedor.removerProducto(pp)){
						partidas.remove(index);
					}
				}
			}
		}
	}
	

	private JButton buildRemoveButton() {
		JButton delete=new JButton("Eliminar");
		delete.addActionListener(EventHandler.create(ActionListener.class, this, "eliminar"));
		return delete;
	}

	private JButton buildAddButton() {
		JButton add=new JButton("Nuevo");
		add.addActionListener(EventHandler.create(ActionListener.class, this, "nuevo"));
		return add;
	}
	
	private JButton buildAsingarPredeterminados(){
		JButton add=new JButton("Proveedor favorito");
		add.addActionListener(EventHandler.create(ActionListener.class, this, "registrarProveedorFavorito"));
		add.setToolTipText("Registrar a este proveedor como predeterminado para los productos que tiene asignados ");
		return add;
	}
	
	
	public void registrarProveedorFavorito(){
		if(!selectionModel.isSelectionEmpty()){
			if(MessageUtils.showConfirmationMessage("Asignar este proveedor como predeterminado para los productos seleccionados", "Asignación de Proveedor predeterminado")){
				List<ProductoPorProveedor> selected=new ArrayList<ProductoPorProveedor>(selectionModel.getSelected());
				
				for(ProductoPorProveedor pp:selected){
					Producto prod=ServiceLocator2.getProductoManager().buscarPorClave(pp.getProducto().getClave());
					prod.setProveedor(pp.getProveedor());
					ServiceLocator2.getProductoManager().save(prod);
				}
			}
			
		}
	}

}
