package com.luxsoft.siipap.cxc.ui.form;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;
import ca.odell.glazedlists.swing.EventComboBoxModel;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.cxc.model.Cargo;
import com.luxsoft.siipap.cxc.model.NotaDeCargoDet;
import com.luxsoft.siipap.cxc.model.OrigenDeOperacion;
import com.luxsoft.siipap.cxc.ui.CXCUIServiceFacade;
import com.luxsoft.siipap.cxc.ui.selectores.SelectorDeCargos;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractMasterDetailForm;

public class NotaDeCargoForm extends AbstractMasterDetailForm{

	public NotaDeCargoForm(NotaDeCargoFormModel nmodel) {
		super(nmodel);
		nmodel.getImporteHabilitado().addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				getControl("importe").setEnabled((Boolean)evt.getNewValue());
			}
		});
		
	}
	
	public NotaDeCargoFormModel getCargoModel(){
		return (NotaDeCargoFormModel)model;
	}
	
	HeaderPanel header;
	
	protected JComponent buildHeader(){
		header=new HeaderPanel("Generación y mantenimiento de Nota de Cargo"
				,""); 
		return header;
	}

	@Override
	protected JComponent buildMasterForm() {
		FormLayout layout=new FormLayout("60dlu,2dlu,150dlu ,3dlu ,60dlu,2dlu,190dlu","");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		builder.append("Cliente",getControl("cliente"),5);		
		builder.append("Comentario",getControl("comentario"),5);
		builder.append("Origen",getControl("origen"));
		builder.append("Fecha",getControl("fecha"));
		builder.append("Vencimiento",getControl("vencimiento"));
		builder.append("Cargo (%)",getControl("cargo"));
		//builder.append("Folio",getControl("documento"));
		builder.append("Esp 1%",getControl("especial"));
		return builder.getPanel();
	}

	@Override
	protected TableFormat getTableFormat() {		
		return GlazedLists.tableFormat(NotaDeCargoDet.class
				,new String[]{"venta.documento","venta.sucursal.nombre","venta.fecha","venta.importe","saldo","cargo","importe"}
				, new String[]{"Factura","Sucursal","Fecha","Importe(F)","Saldo","Cargo","Importe"}
				,new boolean[]{false,false,false,false,false,true,false});
	}
	
	
	
	@Override
	public void insertPartida() {
		if(model.getValue("cliente")!=null){
			//List<Venta> res=SelectorDeFacturas.buscarVentas(getCargoModel().getCargo().getCliente());
			List<Cargo> res=null;
			if(getCargoModel().getCargo().isEspecial()){
				res=SelectorDeCargos.buscarEspecial(getCargoModel().getCargo().getCliente());
			}else
				res=SelectorDeCargos.buscar(getCargoModel().getCargo().getCliente());
			if(res.isEmpty())
				return;
			for(Cargo  v:res){
				if(v!=null)
					getCargoModel().agregarVenta(v);
			}
		}
		
	}

	protected JComponent buildTotalesPanel(){
		
		final FormLayout layout=new FormLayout(
				"p:g,5dlu,p,2dlu,f:90dlu"
				,"p,2dlu,p,2dlu,p");
		
		//final FormDebugPanel debugPanel=new FormDebugPanel(layout);
		
		final PanelBuilder builder=new PanelBuilder(layout);		
		final CellConstraints cc=new CellConstraints();
		
		if(!model.isReadOnly())			
			builder.add(buildValidationPanel(),cc.xywh(1, 1,1,5));
		
		builder.addLabel("Importe",cc.xy(3, 1));
		builder.add(getControl("importe"),cc.xy(5, 1));
		
		builder.addLabel("Impuesto",cc.xy(3, 3));
		builder.add(addReadOnly("impuesto"),cc.xy(5, 3));
		
		builder.addLabel("Total",cc.xy(3, 5));
		builder.add(addReadOnly("total"),cc.xy(5, 5));	
				
		return builder.getPanel();
	}
	
	@Override
	protected JComponent createCustomComponent(String property) {		
		if("comentario".equals(property)){
			JComponent control=Binder.createMayusculasTextField(model.getModel(property), false);
			control.setEnabled(!model.isReadOnly());
			return control;			
		}else if("cliente".equals(property)){
			JComponent box=createClienteBox(model.getModel(property));			
			return box;
		}else if("documento".equals(property)){					
			final JTextField tf=BasicComponentFactory.createLongField(model.getComponentModel(property));
			tf.setEnabled(!model.isReadOnly());
			return tf;
		}else if("cargo".equals(property)){
			return Bindings.createDescuentoEstandarBinding(model.getModel(property));
		}else if("importe".equals(property) ||"impuesto".equals(property) ||"total".equals(property) ){
			return Binder.createBigDecimalMonetaryBinding(model.getModel(property));
		}else if("origen".equals(property)){
			SelectionInList sl=new SelectionInList(OrigenDeOperacion.values(),model.getModel(property));
			return BasicComponentFactory.createComboBox(sl);
		}
		return null;
	}
	
	protected JComponent createClienteBox(final ValueModel vm){
		if(getCargoModel().getCargo().getCliente()==null){
			final JComboBox box=new JComboBox();
			final EventList source=CXCUIServiceFacade.getClientes();
			final TextFilterator filterator=GlazedLists.textFilterator(new String[]{"clave","nombre","rfc"});
			AutoCompleteSupport support = AutoCompleteSupport.install(box, source, filterator);
	        support.setFilterMode(TextMatcherEditor.CONTAINS);        
	        final EventComboBoxModel model=(EventComboBoxModel)box.getModel();
	        model.addListDataListener(new Bindings.WeakListDataListener(vm));
	        box.setSelectedItem(vm.getValue());        
			return box;
		}else{
			JTextField tf=new JTextField(20);
			tf.setText(getCargoModel().getCargo().getCliente().getNombreRazon());
			tf.setEnabled(false);
			return tf;
		}
		
	}

}
