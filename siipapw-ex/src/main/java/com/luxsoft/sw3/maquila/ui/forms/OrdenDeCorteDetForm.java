package com.luxsoft.sw3.maquila.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.value.ValueHolder;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.maquila.model.EntradaDeMaterialDet;
import com.luxsoft.sw3.maquila.model.OrdenDeCorteDet;





/**
 * Forma para el mantenimiento unitario de las ordenes de corte
 * 
 * @author Ruben Cancino
 *
 */
public class OrdenDeCorteDetForm extends AbstractForm{
	
	private EventList<Producto> productos=new BasicEventList<Producto>();
 	
	public OrdenDeCorteDetForm(IFormModel model) {
		super(model);
		setTitle("Corte");
		model.addBeanPropertyChangeListener(new Handler());
		model.getModel("destino").addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Producto p=(Producto)evt.getNewValue();
				if(p!=null){
					System.out.println("Actualizando destino: "+p.isMedidaEspecial());
					getControl("ancho").setEnabled(p.isMedidaEspecial());
					getControl("largo").setEnabled(p.isMedidaEspecial());
				}				
			}
		});		
		
	}	

	@Override
	protected JComponent buildFormPanel() {
		FormLayout layout=new FormLayout(
				" p,2dlu,90dlu:g(.5),2dlu" +
				",p,2dlu,90dlu:g(.5)" 
			,	"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		builder.appendSeparator("Instrucción de corte");
		builder.append("Destino",getControl("destino"),5);
		builder.nextLine();
		builder.append("Kilos",getControl("kilos"));
		builder.append("M2",getControl("metros2"));		
		
		
		builder.appendSeparator("Estimación");
		builder.append("Millares",addReadOnly("millaresEstimados"));
		builder.append("Costo",addReadOnly("costoEstimado"));
		
		builder.appendSeparator("Medida especial");
		builder.append("Ancho",getControl("ancho"));
		builder.append("Largo",getControl("largo"));
		
		builder.nextLine();
		builder.append("Comentario",getControl("comentario"),5);
		
		//ComponentUtils.decorateSpecialFocusTraversal(builder.getPanel());
		//ComponentUtils.decorateTabFocusTraversal(getControl("origen"));
		return builder.getPanel();
	}	

	private Header header;	
	
	@Override
	protected JComponent buildHeader() {
		if(header==null){
			header=new Header("","");
			updateHeader();
		}
		return header.getHeader();
	}
	
	public void updateHeader() {
		if(header!=null){
			EntradaDeMaterialDet origen=(EntradaDeMaterialDet)model.getValue("origen");
			if(origen!=null){
				Producto p=origen.getProducto();
				header.setTitulo(MessageFormat.format("Id:{0}  Bobina:{1} ({2})"
						,origen.getId()
						,p.getDescripcion()
						,p.getClave())
						);
				String pattern=
						  "Disponible Kg: {0}     Precio x Kg: {1}" +
						"\nDisponible M2: {2}     Precio x m2: {3}";
				String desc=MessageFormat.format(pattern
						,origen.getDisponibleKilos()
						,origen.getPrecioPorKilo()
						,origen.getDisponibleEnM2()
						,origen.getPrecioPorM2()
						);
				header.setDescripcion(desc);
			}
			else{
				header.setTitulo("La entrada origen es mandatoria");
				header.setDescripcion("");
			}
		}
	}
	
	private JFormattedTextField kilosField;
	private JFormattedTextField metrosField;
	
	private ValueHolder kilosModel=new ValueHolder(Double.valueOf(0));
	private ValueHolder metrosHolder=new ValueHolder(Double.valueOf(0));
	
	@Override
	protected JComponent createCustomComponent(String property) {
		if("destino".equals(property)){
			JComponent pc=createProductosControl(model.getModel(property));
			return pc;
		}else if("kilos".equals(property)){
			NumberFormat format=NumberFormat.getInstance();
			format.setGroupingUsed(true);			
			format.setMaximumFractionDigits(4);
			format.setMinimumFractionDigits(2);
			format.setParseIntegerOnly(false);
			NumberFormatter formatter=new NumberFormatter(format);
			formatter.setAllowsInvalid(true);			
			formatter.setCommitsOnValidEdit(false);
			formatter.setValueClass(BigDecimal.class);			
			kilosField=new JFormattedTextField(formatter);
			kilosField.setEnabled(!model.isReadOnly());
			kilosField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BigDecimal val=(BigDecimal)kilosField.getValue();
					if(val==null)
						val=BigDecimal.ZERO;
					if(getOrdenDet()!=null){
						getOrdenDet().setKilos(val);
						getOrdenDet().recalcularMetros();
						metrosField.setValue(getOrdenDet().getMetros2());
						//getControl("comentario").requestFocusInWindow();
					}
				}
			});
			return kilosField;
		}else if("metros2".equals(property)){
			NumberFormat format=NumberFormat.getInstance();
			format.setGroupingUsed(true);			
			format.setMaximumFractionDigits(4);
			format.setMinimumFractionDigits(2);
			format.setParseIntegerOnly(false);
			NumberFormatter formatter=new NumberFormatter(format);
			formatter.setAllowsInvalid(true);			
			formatter.setCommitsOnValidEdit(false);
			formatter.setValueClass(BigDecimal.class);			
			metrosField=new JFormattedTextField(formatter);
			metrosField.setEnabled(!model.isReadOnly());
			metrosField.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BigDecimal val=(BigDecimal)metrosField.getValue();
					if(val==null)
						val=BigDecimal.ZERO;
					if(getOrdenDet()!=null){
						getOrdenDet().setMetros2(val);
						getOrdenDet().recalcularKilos();
						kilosField.setValue(getOrdenDet().getKilos());
						//getControl("comentario").requestFocusInWindow();
					}
				}
			});
			return metrosField;
		}
		return super.createCustomComponent(property);
	}
	
	private JComponent createProductosControl(ValueModel vm){		
		final JComboBox box = new JComboBox();			
		EventList source =null;
		source=GlazedLists.eventList(getProductos());
		final TextFilterator filterator = GlazedLists.textFilterator(new String[] { "clave","descripcion" });
		AutoCompleteSupport support = AutoCompleteSupport.install(box,source, filterator);
		support.setFilterMode(TextMatcherEditor.CONTAINS);
		support.setStrict(false);
		final EventComboBoxModel model = (EventComboBoxModel) box.getModel();
		model.addListDataListener(new Bindings.WeakListDataListener(vm));
		box.setSelectedItem(vm.getValue());
		return box;
	}	
	
	public EventList<Producto> getProductos() {
		return productos;
	}

	public void setProductos(EventList<Producto> productos) {
		this.productos = productos;
	}

	private OrdenDeCorteDet getOrdenDet(){
		return (OrdenDeCorteDet)model.getBaseBean();
	}

	private class Handler implements PropertyChangeListener{
		public void propertyChange(PropertyChangeEvent evt) {
			if("origen".equals(evt.getPropertyName())){
				updateHeader();
				
			}
		}
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				
				
				OrdenDeCorteDet det=new OrdenDeCorteDet();
				det.setOrigen((EntradaDeMaterialDet) ServiceLocator2.getHibernateTemplate().get(EntradaDeMaterialDet.class,new Long(511)));
				
				DefaultFormModel model=new DefaultFormModel(det);
				OrdenDeCorteDetForm form=new OrdenDeCorteDetForm(model);
				form.getProductos().addAll(ServiceLocator2.getProductoManager().buscarProductosActivos());
				form.open();
				if(!form.hasBeenCanceled()){
					showObject(model.getBaseBean());
				}
				System.exit(0);
			}

		});
	}

}
