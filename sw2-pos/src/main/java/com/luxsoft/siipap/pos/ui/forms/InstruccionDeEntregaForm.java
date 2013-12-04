package com.luxsoft.siipap.pos.ui.forms;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.SpinnerAdapterFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.Direccion;
import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.binding.Binder;
import com.luxsoft.siipap.swing.binding.Bindings;
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.sw3.services.Services;
import com.luxsoft.sw3.ui.selectores.LookupUtils;
import com.luxsoft.sw3.ventas.InstruccionDeEntrega;

public class InstruccionDeEntregaForm extends AbstractForm implements ActionListener{
	
	private Cliente cliente;
	private boolean foraneo=false;

	public InstruccionDeEntregaForm(IFormModel model) {
		super(model);
		
	}
	
	private JLabel clienteField;
	private JTextArea socioAdress;
	private JComboBox direcciones;
	
	private String direccionSocio;
	
	@Override
	protected JComponent buildFormPanel() {
		final FormLayout layout=new FormLayout(
				"l:p,2dlu,f:90dlu ,2dlu," +
				"l:p,2dlu,f:90dlu"
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);
		
		
		if(getCliente()!=null){
			builder.appendSeparator("Cliente");
			clienteField=new JLabel(getCliente().getNombreRazon());
			builder.append("Cliente",clienteField,5);
			builder.nextLine();			
			if(getDireccionSocio()!=null){
				socioAdress=new JTextArea(3,10);
				socioAdress.setEditable(false);
				socioAdress.setWrapStyleWord(true);
				socioAdress.setText(getDireccionSocio());
				builder.append("Socio",new JScrollPane(socioAdress),5);
			}
			if(getCliente().getDirecciones().isEmpty()){
				direcciones=new JComboBox(new Object[]{null,getCliente().getDireccionFiscal()});				
			}else{
				direcciones=new JComboBox(new Object[]{null,getCliente().getDirecciones().values()});
			}
			direcciones.addActionListener(this);
			builder.append("Direcciones",direcciones,5);
		}
		builder.appendSeparator("Dirección");
		builder.append("Calle",addMandatory("calle"),5);
		builder.append("Numero Ext",addMandatory("numero"));
		builder.append("Numero Int",getControl("interior"));
		builder.append("Colonia",getControl("colonia"),5);
		builder.append("Del/Mpio",getControl("municipio"),5);
		builder.append("Entidad",getControl("estado"));
		builder.append("C.P.",addMandatory("cp"));
		builder.append("Comentario  ",getControl("comentario"),5);
		
		if(isForaneo()){
			builder.appendSeparator("Foraneo");
			builder.append("Transporte",getControl("transporte"),5);
			builder.append("Ocurre",getControl("ocurre"));
			builder.append("Asegurado",getControl("asegurado"));
			builder.append("Recolección",getControl("recoleccion"));
			builder.nextLine();
			builder.append("Condiciones",getControl("condiciones"),5);
		}
		builder.nextLine();
		builder.append("Fecha de Entrega",getControl("fechaEntrega"));
		return builder.getPanel();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JComponent createCustomComponent(String property) {
		if("estado".equalsIgnoreCase(property)){
			return buildEstadosBox(model.getModel(property));
		}else if("municipio".equalsIgnoreCase(property)){
			return buildMunicipioBox(model.getModel(property));
		}else if("calle".equals(property) || "colonia".equals(property) ){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}else if(property.startsWith("comenta")){
			JTextField tf=Binder.createMayusculasTextField(model.getComponentModel(property),true);
			return tf;
		}else if("transporte".equals(property)){
			List values=Services.getInstance().getHibernateTemplate().find("from ServicioDeTransporte t");
			SelectionInList sl=new SelectionInList(values,model.getModel(property));
			JComboBox box=BasicComponentFactory.createComboBox(sl);
			return box;
		}else if("fechaEntrega2".equals(property)){
			JSpinner spiner=new JSpinner();
			SpinnerDateModel smodel=SpinnerAdapterFactory.createDateAdapter(model.getModel(property), new Date(), null, null, Calendar.DATE);
			spiner.setModel(smodel);
			return spiner;
			
		}
		return super.createCustomComponent(property);
	}
	
	private JComboBox buildMunicipioBox(ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(LookupUtils.getMunicipios());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setSelectsTextOnFocusGain(true);
        box.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				model.setValue("municipio", sel);	
			}
        });       
        if(model.getValue("municipio")!=null)
        	box.setSelectedItem(model.getValue("municipio"));
		return box;
	}
	
	private JComboBox buildEstadosBox(ValueModel vm){
		final JComboBox box=new JComboBox();
		final EventList source=GlazedLists.eventList(LookupUtils.getEstados());
		final AutoCompleteSupport support = AutoCompleteSupport.install(box, source);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setSelectsTextOnFocusGain(true);
        box.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				Object sel=box.getSelectedItem();
				model.setValue("estado", sel);	
			}
        });        		
        if(model.getValue("estado")!=null)
        	box.setSelectedItem(model.getValue("estado"));
		return box;
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	
	
	public String getDireccionSocio() {
		return direccionSocio;
	}

	public void setDireccionSocio(String direccionSocio) {
		this.direccionSocio = direccionSocio;
	}

	public boolean isForaneo() {
		return foraneo;
	}

	public void setForaneo(boolean foraneo) {
		this.foraneo = foraneo;
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==direcciones){			
			Direccion d=(Direccion)direcciones.getSelectedItem();
			//System.out.println("Direccion seleccionada: "+d);
			model.setValue("direccion", d);
			((JComboBox)getControl("municipio")).setSelectedItem(model.getValue("municipio"));			
			((JComboBox)getControl("estado")).setSelectedItem(model.getValue("estado"));
		}
		
	}

	public static InstruccionDeEntrega crearNueva(final Cliente c){
		return crearNueva(c, null);
	}

	public static InstruccionDeEntrega crearNueva(final Cliente c,final String direccionSocio){
		final DefaultFormModel model=new DefaultFormModel(new InstruccionDeEntrega());
		final InstruccionDeEntregaForm form=new InstruccionDeEntregaForm(model);
		form.setDireccionSocio(direccionSocio);
		form.setCliente(c);
		form.open();		
		if(!form.hasBeenCanceled()){
			return (InstruccionDeEntrega)model.getBaseBean();
		}
		return null;
	}
	
	public static InstruccionDeEntrega modificar(final Cliente c,final InstruccionDeEntrega source){
		return modificar(c, source,null);
	}
	
	public static InstruccionDeEntrega modificar(final Cliente c,final InstruccionDeEntrega source,String direccionSocio){
		InstruccionDeEntrega target=new InstruccionDeEntrega();
		BeanUtils.copyProperties(source, target,new String[]{"id"});
		final DefaultFormModel model=new DefaultFormModel(target);
		final InstruccionDeEntregaForm form=new InstruccionDeEntregaForm(model);
		form.setDireccionSocio(direccionSocio);
		form.setCliente(c);
		form.open();		
		if(!form.hasBeenCanceled()){
			return (InstruccionDeEntrega)model.getBaseBean();
		}
		return source;
	}
	
	public static InstruccionDeEntrega crearNuevaForaneo(final Cliente c){
		return crearNueva(c, null);
	}
	
	public static InstruccionDeEntrega crearNuevaForaneo(final Cliente c,String direccionSocio){
		final DefaultFormModel model=new DefaultFormModel(new InstruccionDeEntrega());
		final InstruccionDeEntregaForm form=new InstruccionDeEntregaForm(model);
		form.setDireccionSocio(direccionSocio);
		form.setForaneo(true);
		form.setCliente(c);
		form.open();		
		if(!form.hasBeenCanceled()){
			return (InstruccionDeEntrega)model.getBaseBean();
		}
		return null;
	}
	
	public static InstruccionDeEntrega modificarForaneo(final Cliente c,final InstruccionDeEntrega source,String direccionSocio){
		InstruccionDeEntrega target=new InstruccionDeEntrega();
		BeanUtils.copyProperties(source, target,new String[]{"id"});
		final DefaultFormModel model=new DefaultFormModel(target);
		final InstruccionDeEntregaForm form=new InstruccionDeEntregaForm(model);
		form.setDireccionSocio(direccionSocio);
		form.setCliente(c);
		form.setForaneo(true);
		form.open();		
		if(!form.hasBeenCanceled()){
			return (InstruccionDeEntrega)model.getBaseBean();
		}
		return source;
	}
	
	/**
	 * Prueba local en el EDT
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				Cliente c=Services.getInstance().getClientesManager().buscarPorClave("U050008");
				System.out.println("Cliente; "+c);
				showObject(crearNueva(c,null));
				System.exit(0);
			}

		});
	}

	
}
