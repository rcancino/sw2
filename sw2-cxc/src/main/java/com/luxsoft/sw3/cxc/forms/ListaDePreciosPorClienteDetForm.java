package com.luxsoft.sw3.cxc.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeProductosCxC;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.ventas.model.ListaDePreciosClienteDet;



public class ListaDePreciosPorClienteDetForm extends AbstractForm{
	
	
	
	private List<Producto> productos;
	
	public ListaDePreciosPorClienteDetForm(DefaultFormModel model) {
		super(model);
		setTitle("Lista de precios por cliente");
		model.getModel("producto").addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {				
				if(evt.getNewValue()!=null){
					getControl("precio").requestFocusInWindow();
				}
				updateHeader();
			}
			
		});
		final PropertyChangeListener precioHandler=new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				ListaDePreciosClienteDet det=(ListaDePreciosClienteDet)getModel().getBaseBean();
				det.aplicarDescuentoSobrePrecioDeLista();
				
			}
		};
		model.getModel("descuento").addValueChangeListener(precioHandler);
	}
	
	@Override
	protected JComponent buildFormPanel(){
		FormLayout layout=new FormLayout("p","p,2dlu,p,2dlu,t:p:g");
		PanelBuilder builder=new PanelBuilder(layout);
		CellConstraints cc=new CellConstraints();
		builder.add(buildFormMainPanel(),cc.xy(1, 1));
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
		builder.append("Producto",getControl("producto"),9);
		builder.nextLine();
		builder.append("descuento",getControl("descuento"));
		builder.append("Precio",getControl("precio"));
		//builder.append("Precio x Kg",getControl("precioKilo"));
		
		builder.nextLine();
		
		builder.appendSeparator("Costos");
		builder.append("Costo Prom",addReadOnly("costoPromedio"));
		builder.append("Costo Repo",addReadOnly("costo"));
		builder.append("Costo Ulti",addReadOnly("costoUltimo"));
		builder.nextLine();
		builder.append("Utilidad",addReadOnly("diferencia"));
		builder.nextLine();
		builder.append("Margen",addReadOnly("margen"));
		builder.nextLine();
		
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
		}else if("cantidad".equals(property)){
			JComponent c=Binder.createNumberBinding(model.getModel(property), 0);
			return c;
		}else if("precio".equals(property)){
			JComponent c=Bindings.createDoubleBinding(model.getModel(property), 4, 2);
			return c;
		}else if(property.startsWith("costo")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if(property.startsWith("diferencia")){
			JComponent c=Binder.createBigDecimalForMonyBinding(model.getModel(property));
			return c;
		}else if("margen".equals(property)){
			return Binder.createDescuentoBinding(model.getModel(property));
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
						Producto p=ServiceLocator2.getProductoManager().buscarPorClave(clave);
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
	
	private Header header;
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("Seleccione un producto","");
			header.setDescRows(5);
			updateHeader();
		}
		return header.getHeader();
	}
	
	
	
	@Override
	protected void onWindowOpened() {
		updateHeader();
	}
	
	 public void buscarProducto(){
		 Producto p=SelectorDeProductosCxC.seleccionar(getProductos());
		 if(p!=null){
			 model.setValue("producto", p);
			 getControl("cantidad").requestFocusInWindow();
		 }
	 }	
	
	 public void updateHeader() {
			if(header!=null){
				Producto p=(Producto)model.getValue("producto");
				if(p!=null){
					header.setTitulo(MessageFormat.format("{0} ({1})",p.getDescripcion(),p.getClave()));
					String pattern="Uni:{0}\t Ancho:{1}\tLargo:{2}\t Calibre:{3}" +
							"\nAcabado:{4}\t Caras:{5}\tPrecio:{6}" +
							"\nP. Crédito: {7,number,currency}\tP. Contado: {8,number,currency}" +
							"\nP. P x Kg(CRE): {9,number,currency} " 
							;
					String desc=MessageFormat.format(pattern
							,p.getUnidad().getNombre()						
							,p.getAncho()
							,p.getLargo()
							,p.getCalibre()
							,p.getAcabado()
							,p.getCaras()
							,p.getModoDeVenta()!=null?(p.getModoDeVenta().equals("B")?"Bruto":"Neto"):""
							,p.getPrecioCredito()
							,p.getPrecioContado()
							,p.getPrecioPorKiloCredito()
							);
					
					header.setDescripcion(desc);
				}
				else{
					header.setTitulo("Seleccione un producto");
					header.setDescripcion("");
				}
			}
		}
		

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
				
				DefaultFormModel model=new DefaultFormModel(new ListaDePreciosClienteDet());
				ListaDePreciosPorClienteDetForm form=new ListaDePreciosPorClienteDetForm(model);
				//form.setProductos(ServiceLocator2.getProductoManager().buscarProductosActivos());
				form.open();
				if(!form.hasBeenCanceled()){
					System.out.println("Ped: "+ToStringBuilder.reflectionToString(model.getBaseBean()));
				}
				System.exit(0);
				
			}
		});
	}	

}
