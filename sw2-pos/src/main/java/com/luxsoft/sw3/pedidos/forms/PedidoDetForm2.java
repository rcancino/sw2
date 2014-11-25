package com.luxsoft.sw3.pedidos.forms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.text.NumberFormatter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventTableModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
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
import com.luxsoft.siipap.swing.controls.PlasticFieldCaret;
import com.luxsoft.siipap.swing.controls.SXTable;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.pedidos.SelectorDeProductosRow;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.PedidoDet;

public class PedidoDetForm2 extends AbstractForm{
	
	
	private final KeyEventPostProcessor keyHandler;
	private List<ProductoRow> productos;
	
	public PedidoDetForm2(PedidoDetFormModel2 model) {
		
		super(model);
		
		keyHandler=new KeyHandler();
		String tipo=model.isCredito()?"Crédito":"Contado";
		setTitle("Detalle de pedido tipo :"+tipo);
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {				
				if(evt.getNewValue()!=null){
					getControl("cantidad").requestFocusInWindow();
				}
				
			}
			
		});
	}
	
	private PedidoDetFormModel2 getDetModel(){
		return (PedidoDetFormModel2)getModel();
	}
	
	@Override
	protected JComponent buildFormPanel(){
		FormLayout layout=new FormLayout("p","p,2dlu,p,2dlu,t:p:g");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.add(buildFormMainPanel(),cc.xy(1, 1));
		builder.addSeparator("Disponibilidad", cc.xy(1, 3));
		builder.add(buildDisponibilidadPanel(),cc.xy(1,5));
		return builder.getPanel();
	}

	
	protected JComponent buildFormMainPanel() {
		JButton pendientesButton=new JButton(getbuscarPendiente());
		setDefaultButton(pendientesButton);
		
		FormLayout layout=new FormLayout(
				" p,2dlu,max(p;60dlu),2dlu" +
				",p,2dlu,max(p;60dlu),2dlu" +
				",p,2dlu,max(p;60dlu):g" 
			,	"");
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.setLineGapSize(Sizes.DLUX2);
		if(model.getValue("producto")==null){
			
		}
		builder.append("Producto",getControl("producto"),9);
		builder.append("Precio",addReadOnly("precio"));
		
		builder.append("Imp Bruto",addReadOnly("importeBruto"));
		builder.nextLine();
		
		builder.append("Cantidad",getControl("cantidad"));
		builder.append("B.O.",addReadOnly("backOrder"));
		builder.append("Cotizable",getControl("cotizable"));
		builder.append("Pedidos Pend.",pendientesButton);
		builder.nextLine();
		
		builder.appendSeparator("Instrucción de corte");
		builder.append("Comentario",getControl("instruccionesDecorte"),5);
		builder.nextLine();
		builder.append("Cantidad ",getControl("cortes"));
		builder.append("Precio",getControl("precioCorte"));
		builder.append("Imp Cortes",addReadOnly("importeCorte"));
		builder.nextLine();
		
		builder.append("");
		builder.nextColumn(6);
		builder.append("Sub Total",addReadOnly("subTotal"));
		ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		
		JPanel cc=(JPanel)getControl("producto");
		ComponentUtils.decorateTabFocusTraversal(cc);
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
		if("producto".equals(property)){
			JComponent pc=createProductosControl();
			return pc;
		}else if("cantidad".equals(property)||"backOrder".equals(property)){
			JFormattedTextField c=Binder.createNumberBinding(model.getModel(property), 0);
			//System.out.println("Generando tf....");
			return c;
		}else if("precioCorte".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if("precio".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if(property.startsWith("importe")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if(property.startsWith("subTotal")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if("cortes".equals(property)){
			JFormattedTextField tf=BasicComponentFactory.createIntegerField(model.getModel(property), 0);
			tf.setCaret(new PlasticFieldCaret());
			return tf;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent createProductosControl(){
		
		DefaultFormBuilder builder=new DefaultFormBuilder(new FormLayout("p:g,2dlu,p",""));	
		
		JButton bt1=new JButton(getLookupAction());
		bt1.setFocusable(false);
		bt1.setMnemonic('F');
		
		final JComboBox box=new JComboBox();
		final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","descripcion"});
		final EventList<ProductoRow> source=GlazedLists.eventList(getProductos());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        //support.setStrict(true);
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof ProductoRow){
					ProductoRow row=(ProductoRow)sel;
					model.setValue("producto", Services.getInstance().getProductosManager().buscarPorClave(row.getClave()));
				}else if(sel instanceof String){
					String clave=(String)sel;
					if(!StringUtils.isBlank(clave)){
						Object p=Services.getInstance().getProductosManager().buscarPorClave(clave);
						if(p!=null)
							model.setValue("producto", p);
					}					
				}
			}
        });        
        if(model.getValue("producto")!=null)
        	box.setSelectedItem(new ProductoRow((Producto)model.getValue("producto")));
		builder.append(box);
		builder.append(bt1);
		return builder.getPanel();
	}
	
	private Action lookupAction;
	
	public Action getLookupAction(){
		if(lookupAction==null){
			lookupAction=new DispatchingAction(this,"buscarProducto");
			lookupAction.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			lookupAction.putValue(Action.NAME, "F2");
		}
		return lookupAction;
	}
	
	
private Action buscarPendientes;
	
	public Action getbuscarPendiente(){
		if(buscarPendientes==null){
			buscarPendientes=new DispatchingAction(this,"buscarPedidosPendientes");
			buscarPendientes.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images/misc2/page_find.png"));
			buscarPendientes.putValue(Action.NAME, "F12");
		}
		return buscarPendientes;
	}
	
	 
	public void buscarPedidosPendientes() {
		if(getDetModel().getPedidoDet().getClave()!=null){
			
			final PedidoDet det=getDetModel().getPedidoDet();
			PedidoDetFormModel2 model=new PedidoDetFormModel2(det);
			model.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
			final PedidosPendientesForm form=new PedidosPendientesForm(model);			
			form.open();	
		}
		
	}
	
	
	
	@Override
	protected JComponent buildHeader() {
		return getDetModel().getHeader().getHeader();
	}
	
	
	@Override
	protected void onWindowOpened() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(keyHandler);
		getDetModel().updateHeader();
	}

	private JComponent buildDisponibilidadPanel(){		
		final TableFormat tf=GlazedLists.tableFormat(Existencia.class
				, new String[]{"sucursal.nombre","cantidad","recorte","pedidosPendientes","disponible","recorteComentario"} //,"modificado","clave","year","mes"}
				,new String[] {"Sucursal","Existencia","Recorte","Pedidos Pend.","Disponible","Recorte Comentario"} //,"Actualización","Clave","Año","Mes"}
		);
		EventTableModel tm=new EventTableModel(getDetModel().getExistencias(),tf);		
		 JXTable grid=buildTable();
		 grid.setModel(tm);
		 grid.setColumnControlVisible(false);
		 grid.setFocusable(false);
		 grid.packAll();
		 JComponent c=ComponentUtils.createTablePanel(grid);
		 c.setPreferredSize(new Dimension(220,135));
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
			ColorHighlighter h2 = new  ColorHighlighter(Color.WHITE, Color.RED, new HighlightPredicate(){
				public boolean isHighlighted(Component renderer,ComponentAdapter adapter) {
					if(adapter.column==1 ){
						Number val=(Number)adapter.getValue();
						return val.doubleValue()<=0.0;
					}
					return false;
				}
				
			});
			//Highlighter alternate=HighlighterFactory.createAlternateStriping();//new HighlighterPipeline();		
			grid.setHighlighters(hl,h2);
			grid.setRolloverEnabled(true);
			//grid.setDefaultRenderer(Renderers.)
			
			grid.setSortable(false);
			grid.getSelectionMapper().setEnabled(false);
			grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
			return grid;
	    }
	
	 public void buscarProducto(){
		 ProductoRow p=SelectorDeProductosRow.seleccionar();
		 if(p!=null){
			 Object prod=Services.getInstance().getProductosManager().buscarPorClave(p.getClave());
			 model.setValue("producto", prod);
			 getControl("cantidad").requestFocusInWindow();
		 }
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

	public List<ProductoRow> getProductos() {
		return productos;
	}

	public void setProductos(List<ProductoRow> productos) {
		this.productos = productos;
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				final PedidoDet det=PedidoDet.getPedidoDet();
				
				PedidoDetFormModel2 model=new PedidoDetFormModel2(det);
				model.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
				PedidoDetForm2 form=new PedidoDetForm2(model);
				form.setProductos(Services.getInstance().getProductosManager().getActivosAsRows());
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Ped: "+ToStringBuilder.reflectionToString(model.getBaseBean()));
				}
				System.exit(0);
				
								
			}
		});
	}
	
	/**
	 * Garantiza la ejecucion de ciertas tareas mediante teclas de acceso 
	 * 
	 * @author Ruben Cancino Ramos
	 *
	 */
	private class KeyHandler implements KeyEventPostProcessor{
		
		
		/**
		 * Implementacion de {@link KeyEventPostProcessor} para los accesos de teclado rpido
		 * 
		 */
		
		@Override
		public boolean postProcessKeyEvent(final  KeyEvent e) {
			
			if(KeyStroke.getKeyStroke("F12").getKeyCode()==e.getKeyCode()){
			
				if(isFocused()){
					e.consume();
					buscarPedidosPendientes();
					return true;
				}
				
			}else if(KeyStroke.getKeyStroke("F2").getKeyCode()==e.getKeyCode()){
				 if(isFocused()){
					e.consume();
					buscarProducto();
					return true;
				}
			}
			
		
				
								
			return false;
		}

		
		
		
	}

}
