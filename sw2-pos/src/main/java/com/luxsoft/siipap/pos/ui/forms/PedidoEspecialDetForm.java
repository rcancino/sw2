package com.luxsoft.siipap.pos.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.PlasticFieldCaret;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ventas.PedidoDet;

public class PedidoEspecialDetForm extends AbstractForm{
	
	
	
	private List<Producto> productos;
	
	public PedidoEspecialDetForm(PedidoEspecialDetFormModel model) {
		super(model);		
		String tipo=model.isCredito()?"Crédito":"Contado";
		setTitle("Detalle de medida especial:"+tipo);
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {				
				if(evt.getNewValue()!=null){
					getControl("ancho").requestFocusInWindow();
				}
			}
			
		});
	}
	
	private PedidoEspecialDetFormModel getDetModel(){
		return (PedidoEspecialDetFormModel)getModel();
	}
	
	@Override
	protected JComponent buildFormPanel(){
		FormLayout layout=new FormLayout("p","p,2dlu,p,2dlu,t:p:g");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.add(buildFormMainPanel(),cc.xy(1, 1));
		//builder.addSeparator("Disponibilidad", cc.xy(1, 3));
		
		return builder.getPanel();
	}

	
	protected JComponent buildFormMainPanel() {
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
		
		
		builder.append("Ancho (cm)",getControl("ancho"));
		builder.append("Largo (cm)",getControl("largo"));
		builder.append("Cantidad",getControl("cantidad"));
		
		builder.nextLine();
		
		
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
			pc.setEnabled(!model.isReadOnly());
			return pc;
		}else if("cantidad".equals(property)||"backOrder".equals(property)){
			//JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			 JComponent  c=Binder.createNumberBinding(model.getModel(property), 0);
			//c.setEnabled(!model.isReadOnly());
			//return c;
			
			//System.out.println("Generando tf....");
			return c;
		}else if("precioCorte".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("precio".equals(property)){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if(property.startsWith("importe")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if(property.startsWith("subTotal")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			c.setEnabled(!model.isReadOnly());
			return c;
		}else if("largo".equals(property)||"ancho".equals(property)){
			//JComponent  c=Binder.createNumberBinding(model.getModel(property), 2);
			JComponent  c=Bindings.createDoubleBinding(model.getModel(property));
			return c;
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
		final EventList<Producto> source=GlazedLists.eventList(getProductos());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
        support.setFilterMode(TextMatcherEditor.STARTS_WITH);
        support.setSelectsTextOnFocusGain(true);
        //support.setStrict(true);
        box.getEditor().addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				if(sel instanceof Producto){
					model.setValue("producto", sel);
				}else if(sel instanceof String){
					String clave=(String)sel;
					if(!StringUtils.isBlank(clave)){
						Producto p=Services.getInstance().getProductosManager().buscarPorClave(clave);
						if(p!=null)
							model.setValue("producto", p);
					}					
				}
			}
        });        
        if(model.getValue("producto")!=null)
        	box.setSelectedItem(model.getValue("producto"));
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
	
	@Override
	protected JComponent buildHeader() {
		return getDetModel().getHeader().getHeader();
	}
	
	
	@Override
	protected void onWindowOpened() {
		getDetModel().updateHeader();
	}

	/*
	
	 public JXTable buildTable(){		
			JXTable grid=new SXTable();
	    	grid.setColumnControlVisible(false);
			grid.setHorizontalScrollEnabled(true);
			/*
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
			//grid.setHighlighters(hl,h2);
			//grid.setRolloverEnabled(true);
			//grid.setDefaultRenderer(Renderers.)
			
			grid.setSortable(false);
			grid.getSelectionMapper().setEnabled(false);
			grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
			return grid;
	    }
	*/
	 public void buscarProducto(){
		 /*Producto p=SelectorDeProductos2.seleccionar();
		 if(p!=null){
			 model.setValue("producto", p);
			 getControl("cantidad").requestFocusInWindow();
		 }*/
	 }
	
	//private Long row;
	/*
	public Long getRow(){
		if(row==null){
			String sucursal=System.getProperty("sw3.pedidos.sucursal.row","0");
			Long id=Long.valueOf(sucursal);
			row=id;
		}
		return row;
	}	
*/
	public List<Producto> getProductos() {
		return productos;
	}

	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}
	
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				final PedidoDet det=PedidoDet.getPedidoDet();
				det.setEspecial(true);
				PedidoEspecialDetFormModel model=new PedidoEspecialDetFormModel(det);
				//model.setSucursal(Services.getInstance().getConfiguracion().getSucursal());
				PedidoEspecialDetForm form=new PedidoEspecialDetForm(model);				
				
				form.setProductos(Services.getInstance().getProductosManager().getMedidasEspeciales());
				System.out.println(form.getProductos().size());
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Ped: "+ToStringBuilder.reflectionToString(model.getBaseBean()));
				}
				System.exit(0);
				
			}
		});
	}	

}
