package com.luxsoft.sw3.contabilidad.ui.form;

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
import com.luxsoft.siipap.swing.form2.AbstractForm;
import com.luxsoft.siipap.swing.form2.DefaultFormModel;
import com.luxsoft.siipap.swing.form2.IFormModel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.sw3.contabilidad.model.ConceptoContable;
import com.luxsoft.sw3.contabilidad.model.CuentaContable;


/**
 * 
 * @author Ruben Cancino
 *
 */
public class CuentaContableConceptosPanel extends JPanel{
	
	private final CuentaContable cuenta;
	
	private EventList<ConceptoContable> partidas;
	private EventSelectionModel selectionModel;
	
	
	
	public CuentaContableConceptosPanel(final CuentaContable cuenta){
		this.cuenta=cuenta;
		partidas=new BasicEventList<ConceptoContable>();
		final EventList<MatcherEditor<ConceptoContable>> editors=new BasicEventList<MatcherEditor<ConceptoContable>>();
		
		descripcionField=new JTextField(20);		
		TextFilterator<ConceptoContable> filter1=GlazedLists.textFilterator("clave","descripcion");
		TextComponentMatcherEditor<ConceptoContable> e1=new TextComponentMatcherEditor<ConceptoContable>(this.descripcionField, filter1);
		editors.add(e1);
		
		CompositeMatcherEditor<ConceptoContable> editor=new CompositeMatcherEditor<ConceptoContable>(editors);
		partidas=new FilterList<ConceptoContable>(partidas,editor);
		partidas=new SortedList<ConceptoContable>(partidas,null);
		partidas.addAll(cuenta.getConceptos());
		init();
	}
	
	private void init(){
		setLayout(new BorderLayout());
		add(buildFormPanel(),BorderLayout.NORTH);
		add(buildGridPanel(),BorderLayout.CENTER);
		add(ButtonBarFactory.buildRightAlignedBar(getButtons()),BorderLayout.SOUTH);
	}
	
	private JTextField descripcionField;
	
	
	private JComponent buildFormPanel(){
		
		final FormLayout layout=new FormLayout(
				"p,2dlu,f:p:g(.5), 3dlu" +
				",p,2dlu,f:p:g(.5)" 
				,"");
		final DefaultFormBuilder builder=new DefaultFormBuilder(layout);	
		builder.setDefaultDialogBorder();
		builder.appendSeparator("Filtros");
		builder.append("Concepto",descripcionField);
		return builder.getPanel();
		
	}
	
	@SuppressWarnings("unchecked")
	private JComponent buildGridPanel(){
		final TableFormat<ConceptoContable> tf=GlazedLists.tableFormat(ConceptoContable.class
			,new String[]{"clave","descripcion"}
			,new String[]{"Clave","Descripción"}
		,new boolean[]{true,true}
		);
		
		final EventTableModel<ConceptoContable> tm=new EventTableModel<ConceptoContable>(partidas,tf);
		final JXTable table=ComponentUtils.getStandardTable();
		table.setModel(tm);
		selectionModel=new EventSelectionModel(partidas);
		table.setSelectionModel(selectionModel);
		TableComparatorChooser.install(table, (SortedList<ConceptoContable>)partidas, TableComparatorChooser.MULTIPLE_COLUMN_MOUSE);
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
		final DefaultFormModel model=new DefaultFormModel(new ConceptoContable());
		final ConceptoForm form=new ConceptoForm(model);
		form.open();
		if(!form.hasBeenCanceled()){
			ConceptoContable target=(ConceptoContable)model.getBaseBean();
			boolean res=cuenta.getConceptos().contains(target);
			MessageUtils.showMessage("Concepto ya registrado", "Conceptos contables para la cuenta: "+cuenta.getClave());
			if(!res){
				cuenta.getConceptos().add(target);
				partidas.add(target);
			}
		}
	}
	
	public void eliminar(){
		if(!selectionModel.isSelectionEmpty()){
			List<ConceptoContable> selected=new ArrayList<ConceptoContable>(selectionModel.getSelected());
			for(ConceptoContable pp:selected){
				int index=partidas.indexOf(pp);
				if(index!=-1){
					if(cuenta.getConceptos().remove(pp)){
						pp.setCuenta(null);
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
	
	public static final class ConceptoForm extends AbstractForm{

		public ConceptoForm(IFormModel model) {
			super(model);
		}

		@Override
		protected JComponent buildFormPanel() {
			FormLayout layout=new FormLayout("p,2dlu,70dlu,3dlu,p,2dlu,200dlu:g","");
			DefaultFormBuilder builder=new DefaultFormBuilder(layout);
			builder.append("Clave",getControl("clave"),true);
			builder.append("Descripcion",getControl("descripcion"),5);
			return builder.getPanel();
		}
		
	}
	
	

}
