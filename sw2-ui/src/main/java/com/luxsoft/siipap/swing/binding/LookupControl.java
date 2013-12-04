package com.luxsoft.siipap.swing.binding;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.text.Format;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swing.AutoCompleteSupport;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.ComponentUtils;

/**
 * 
 * Control que permite seleccionar un objeto (Puede ser un bean)
 * 
 * Se asume que este control carga sus elementos antes de ser presentado
 *  
 * @author Ruben Cancino
 *
 */
public class LookupControl extends JPanel {
	
	protected JComboBox box;
	protected JButton btn;
	
	protected TextFilterator filterator;
	protected Format format;
	protected AutoCompleteSupport support;
	protected EventList source;
	private Action insertAction;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	public LookupControl(final List data){
		this(data,null,null);
	}
	
	@SuppressWarnings("unchecked")
	public LookupControl(final List data,final TextFilterator filterator,final Format format){
		source=createEventList();
		source.addAll(data);
		this.filterator=filterator;
		this.format=format;	
		init();
	}
	
	/**
	 * Template method para inicializar GlazedList 
	 *
	 */
	@SuppressWarnings("unchecked")
	protected EventList createEventList(){
		return GlazedLists.threadSafeList(new BasicEventList());
	}
	
	protected FormLayout getFormLayout(){
		final FormLayout layout=new FormLayout("f:max(p;50dlu):g,p","f:p");
		return layout;
	}
	
	protected void init(){
		setLayout(getFormLayout());
		box=new JComboBox();
		//box.addAncestorListener(this);
		btn=createInsertButton();
		
		final CellConstraints cc=new CellConstraints();
		add(box,cc.xy(1, 1));
		add(btn,cc.xy(2, 1));
		
		ComponentUtils.addInsertAction(((JTextField)box.getEditor().getEditorComponent()),getInsertAction());
		installAutoComplete();
	}
	
	
	private void installAutoComplete(){		
		if(SwingUtilities.isEventDispatchThread())
			decorate();
		else{
			try {
				SwingUtilities.invokeAndWait(new Runnable() {				    
					public void run() {
						decorate();
				    }
				});
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}				
	}
	
	
	@SuppressWarnings("unchecked")
	protected void decorate(){
		support=AutoCompleteSupport.install(box, source,filterator,format);
        support.setFilterMode(TextMatcherEditor.CONTAINS);
        support.setStrict(false);
        box.addActionListener(new BindingSupport());
	}
	
	protected JButton createInsertButton(){
		JButton btn=new JButton(getInsertAction());
		btn.setBorderPainted(false);
		btn.setFocusable(false);
		//btn.setEnabled(false);
		return btn;
	}
	
	public Action getInsertAction() {
		if(insertAction==null){
			insertAction=new AbstractAction(""){
				public void actionPerformed(ActionEvent e) {
					doInsert();
				}				
			};
			insertAction.putValue(Action.SMALL_ICON,CommandUtils.getIconFromResource("images/database_add.png") );
			insertAction.setEnabled(enableInsertObject);
		}
		return insertAction;
	}
	
	@SuppressWarnings("unchecked")
	protected void doInsert(){
		Object row=newObject();
		if(row!=null){
			source.add(row);
			box.setSelectedItem(row);
		}
	}
	
	
	
	/**
	 * Template method para habilitar la creacion en linea de nuevas instancias para Lookup
	 * La implementacion por default no genera ninguna instancia. Subclases puede implementar
	 * este metodo de forma particular a las necesidades
	 * 
	 * @return
	 */
	protected Object newObject(){
		logger.debug("Generando una nueva entidad para la lista del combo");
		return null;
	}

	/**
	 * Installa AutoCompleteSupport 
	 
	public void ancestorAdded(AncestorEvent event) {		
		installAutoComplete();
	}
	
	public void ancestorRemoved(AncestorEvent event) {
				
	}

	public void ancestorMoved(AncestorEvent event) {	}
	*/
	
	/**
	 * Detecta los cambios en la seleccion y en caso de existir un {@link ValueModel}
	 * asociado (Para Binding) lo actualiza
	 * 
	 * @author Ruben Cancino
	 *
	 */
	private class BindingSupport implements ActionListener{
		public void actionPerformed(ActionEvent e) {			
			setValue(box.getSelectedItem());
		}		
	}
	
	
	private WeakReference<ValueModel> valueModelRef;
	
	public void setValue(final Object val){
		
		if(valueModelRef!=null && (valueModelRef.get()!=null)){
			if(val instanceof String){
				valueModelRef.get().setValue(null);
			}else{
				if(val!=null){
					valueModelRef.get().setValue(val);
					
				}
			}
		}
	}	
	
	public void setValueModel(ValueModel valueModel) {
		if(valueModelRef==null &&(valueModel!=null)){
			valueModelRef=new WeakReference<ValueModel>(valueModel);			
			
		}
		if(valueModel.getValue()!=null){
			int index=source.indexOf(valueModel.getValue());			
			box.setSelectedIndex(index);
		}
	}
	
	 public void setEnabled(boolean enabled) {
		 super.setEnabled(enabled);
		 box.setEnabled(enabled);
		 if(enableInsertObject)
			 btn.setEnabled(enabled);
	 }
	 
	 protected boolean enableInsertObject=false;


	public boolean isEnableInsertObject() {
		return enableInsertObject;
	}

	public void setEnableInsertObject(boolean enableInsertObject) {
		this.enableInsertObject = enableInsertObject;
	}
	 
	 

}
