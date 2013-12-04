package com.luxsoft.siipap.swing.views2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXTaskPane;
import org.springframework.util.StringUtils;

import com.luxsoft.siipap.swing.actions.DispatchingAction;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Abstract implementation de InternalTaskView para facilitar la creación
 * de este tipo de vistas
 * 
 * @author Ruben Cancino
 *
 */
public abstract class AbstractInternalTaskView implements InternalTaskView{
	
	private String title="Sin Título";
	private String iconPath;
	protected Logger logger=Logger.getLogger(getClass());
	
	public Icon getIcon() {
		return ResourcesUtils.getIconFromResource(getIconPath());
	}

	public String getTitle() {
		return title;
	}

	public void instalOperacionesAction(JXTaskPane operaciones) {		
	}

	public void instalProcesosActions(JXTaskPane procesos) {
			
	}

	public void installDetallesPanel(JXTaskPane detalle) {
		
	}

	public void installFiltrosPanel(JXTaskPane filtros) {
		
	}	
	
	public void close() {
	}
	
	public void open(){}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	protected Map<String, Action> actions=new HashMap<String, Action>();
	
	protected Action getInternalTaskAction(final String id){
		Action a=actions.get(id);
		if(a==null){
			a=new DispatchingAction(this,id);
			CommandUtils.configAction(a, id, "");
			String label=(String)a.getValue(Action.SHORT_DESCRIPTION);
			if(label==null){
				a.putValue(Action.SHORT_DESCRIPTION, StringUtils.capitalize(id));
			}
		}
		return a;
	}
	
	public void load(){
		final SwingWorker<List,String> worker=new SwingWorker<List,String>(){

			@Override
			protected List doInBackground() throws Exception {
				return loadData();
			}
			@Override
			protected void done() {
				try {
					afterLoad(get());
				} catch (Exception e) {
					MessageUtils.showError("Error al cargar datos", e);
					e.printStackTrace();
				}
			}
			
		};
		TaskUtils.executeSwingWorker(worker);
	}
	
	/**
	 * Template method se ejecuta en EDT descpues de cargar datos en {@link SwingWorker}
	 *
	 */
	public void afterLoad(final Object data){
		
	}
	
	/**
	 * Template method que se ejecuta en un sub proceso para traer informacion 
	 * normalmente de la base de datos
	 * 
	 * @return
	 */
	public List loadData(){
		return new ArrayList();
	}

}
