package com.luxsoft.siipap.swing.binding;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.TableFormat;

import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.luxsoft.siipap.model.core.Producto;
import com.luxsoft.siipap.service.ServiceLocator2;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.dialog.AbstractSelector;
import com.luxsoft.siipap.swing.utils.CommandUtils;

/**
 * 
 * Control que permite seleccionar un objeto (Puede ser un bean)
 * 
 * Se asume que este control carga sus elementos antes de ser presentado
 *  
 * @author Ruben Cancino
 *
 */
public abstract class GridLookupControl extends JPanel {
	
	protected JTextField textField;
	protected JButton btn;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	
	@SuppressWarnings("unchecked")
	public GridLookupControl(){
		init();
	}
	
	protected FormLayout getFormLayout(){
		final FormLayout layout=new FormLayout("f:max(p;50dlu):g,p","f:p");
		return layout;
	}
	
	protected void init(){
		setLayout(getFormLayout());
		textField=new JTextField(15);		
		btn=createInsertButton();
		final CellConstraints cc=new CellConstraints();
		add(textField,cc.xy(1, 1));
		add(btn,cc.xy(2, 1));
	}
	
	
	protected JButton createInsertButton(){
		JButton btn=new JButton(getLookupAction());
		btn.setBorderPainted(false);
		btn.setFocusable(false);
		//btn.setEnabled(false);
		return btn;
	}
	
	private Action lookupAction;
	
	public Action getLookupAction() {
		if(lookupAction==null){
			lookupAction=new AbstractAction(""){
				public void actionPerformed(ActionEvent e) {
					doLookup();
				}				
			};
			lookupAction.putValue(Action.SMALL_ICON,CommandUtils.getIconFromResource("images2/zoom.png") );
		}
		return lookupAction;
	}
	
	
	public void doLookup(){
		AbstractSelector selector=new AbstractSelector(getBeanClass(),"Buscando..."){
			
			@Override
			public List getData() {
				return GridLookupControl.this.getData();
			}

			@Override
			public TableFormat getTableFormat() {
				return GridLookupControl.this.getTableFormat();
			}
			
		};
		selector.open();
		if(!selector.hasBeenCanceled()){			
			Object selected=selector.getSelected();
			System.out.println("Asignando valor "+selected);
			setValue(selected);
		}
	}
	
	public abstract Class getBeanClass();
	
	public abstract List getData() ;
	
	public abstract  TableFormat getTableFormat(); 
	
	protected WeakReference<ValueModel> valueModelRef;
	
	protected Object selected=null;

	public Object getSelected(){
		return selected;
	}
	
	protected void setValue(final Object val){
		
		textField.setText(format(val));
		selected=val;
		
		if(valueModelRef!=null && (valueModelRef.get()!=null)){
			if(val!=null){
				valueModelRef.get().setValue(val);
			}
		}
	}	
	
	public void setValueModel(ValueModel valueModel) {
		if(valueModelRef==null &&(valueModel!=null)){
			valueModelRef=new WeakReference<ValueModel>(valueModel);			
			
		}
		if(valueModel.getValue()!=null){
			Object obj=valueModel.getValue();		
			textField.setText(format(obj));
		}
	}
	
	/**
	 * Template method para sobre escribir lo que se quiera 
	 * en el JTextfield
	 * 
	 * @param obj
	 * @return
	 */
	public String format(Object obj){
		return obj.toString();
	}
	
	 public void setEnabled(boolean enabled) {
		 super.setEnabled(enabled);
		 textField.setEnabled(enabled);
		 btn.setEnabled(enabled);
	 }
	 
	

}
