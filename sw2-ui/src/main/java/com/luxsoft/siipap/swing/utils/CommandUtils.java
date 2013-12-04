package com.luxsoft.siipap.swing.utils;

import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.jgoodies.uif.builder.ToolBarBuilder;
import com.luxsoft.siipap.swing.Application;
import com.luxsoft.siipap.swing.actions.DispatchingAction;

/**
 * Clase de utilerias para crear acciones comunes
 * 
 * @author Ruben Cancino
 *
 */
public final class CommandUtils {
	
	private static Logger logger=Logger.getLogger(CommandUtils.class);
	
	/**
	 * Crea un DispatchingAction apropiado para el alta de objetos 
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createInsertAction(Object model,String method,String id){
		Action insert=new DispatchingAction(model,method);
		insert.putValue("ID", id);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, id);
			insert.putValue(Action.NAME, "Nuevo");
			insert.putValue(Action.SHORT_DESCRIPTION, "Nuevo");
			insert.putValue(Action.LONG_DESCRIPTION, "Insertar un nuevo objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/new_page.gif"));
		}else{
			insert.putValue(Action.NAME, "Insertar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Nuevo");
			insert.putValue(Action.LONG_DESCRIPTION, "Insertar un nuevo objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/new_page.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para el alta de objetos 
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createInsertAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "insertAction");
		}else{
			insert.putValue(Action.NAME, "Insertar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Insertar");
			insert.putValue(Action.LONG_DESCRIPTION, "Insertar un nuevo objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/new_page.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para la eliminacion de objetos
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createDeleteAction(Object model,String method,String id){
		Action insert=new DispatchingAction(model,method);
		insert.putValue("ID", id);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, id);
			insert.putValue(Action.NAME, "Eliminar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Eliminar");
			insert.putValue(Action.LONG_DESCRIPTION, "Elimina registro");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/delete_edit.gif"));
		}else{
			insert.putValue(Action.NAME, "Eliminar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Eliminar");
			insert.putValue(Action.LONG_DESCRIPTION, "Elimina un objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/delete_edit.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para la eliminacion de objetos
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createDeleteAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "deleteAction");
		}else{
			insert.putValue(Action.NAME, "Eliminar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Eliminar");
			insert.putValue(Action.LONG_DESCRIPTION, "Elimina un objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/delete_edit.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para la edicion de objetos
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createEditAction(Object model,String method,String id){
		Action insert=new DispatchingAction(model,method);
		insert.putValue("ID", id);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, id);
			insert.putValue(Action.NAME, "Editar");
			insert.putValue(Action.SHORT_DESCRIPTION, "Editar las propiedades del objeto");
			insert.putValue(Action.LONG_DESCRIPTION, "Editar las propiedades del objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/text_edit.gif"));
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Editar");
			insert.putValue(Action.LONG_DESCRIPTION, "Editar las propiedades del objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/text_edit.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para la edicion de objetos
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createEditAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "editAction");
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Editar");
			insert.putValue(Action.LONG_DESCRIPTION, "Editar las propiedades del objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/edit/text_edit.gif"));
		}
		return insert;
	}
	
	public static Action createViewAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "viewObjectAction");
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Ver");
			insert.putValue(Action.LONG_DESCRIPTION, "Ver el objeto");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/misc/watchlist_view.gif"));
		}
		return insert;
	}
	
	public static Action createRefreshAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "refreshAction");
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Refrescar");
			insert.putValue(Action.LONG_DESCRIPTION, "Refresca los objetos desde la base de datos");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/misc/refresh_remote.gif"));
		}
		return insert;
	}
	
	public static Action createLoadAction(Object model,String method){
		return createRefreshAction(model, method);
	}
	
	public static Action createFilterAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, "filterAction");
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Filtrar");
			insert.putValue(Action.LONG_DESCRIPTION, "Filtar los objetos");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/nav/filter.gif"));
		}
		return insert;
	}
	
	public static Action createPrintAction(Object model,String method){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			//Application.instance().getActionManager().configure(insert, null);
			insert.putValue(Action.NAME, "Imprimir");
			insert.putValue(Action.SHORT_DESCRIPTION, "Imprimir");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Imprimir");
			insert.putValue(Action.LONG_DESCRIPTION, "Imprimir");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return insert;
	}
	
	public static Action createPrintAction(Object model,String method,String id){
		Action insert=new DispatchingAction(model,method);
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(insert, id);
		}else{
			insert.putValue(Action.SHORT_DESCRIPTION, "Imprimir");
			insert.putValue(Action.LONG_DESCRIPTION, "Imprimir");
			insert.putValue(Action.SMALL_ICON, getIconFromResource("images/file/printview_tsk.gif"));
		}
		return insert;
	}
	
	/**
	 * Crea un DispatchingAction apropiado para la edicion de objetos
	 * 
	 * @param model
	 * @param method
	 * @return
	 */
	public static Action createAutorizarAction(Object model,String method,String id){
		Action action=new DispatchingAction(model,method);
		action.putValue("ID", id);
		action.putValue(Action.NAME, "Autorizar");
		action.putValue(Action.SHORT_DESCRIPTION, "Autorización de operación");
		action.putValue(Action.LONG_DESCRIPTION, "Solicitar autorizacion para la operación");
		action.putValue(Action.SMALL_ICON, getIconFromResource("images/misc2/tick.png"));
		return action;
	}
	
	public static List<Action> createCommonCURD_Actions(Object model){
		List<Action> actions=new ArrayList<Action>();
		actions.add(createInsertAction(model, "insert"));
		actions.add(createDeleteAction(model,"delete"));
		actions.add(createEditAction(model,"edit"));
		actions.add(createViewAction(model,"view"));
		actions.add(createRefreshAction(model,"refresh"));
		actions.add(createFilterAction(model,"filter"));
		actions.add(createPrintAction(model,"print"));
		
		return actions;
	}
	
	public static List<Action> decorateCommonCURD_Actions(final Object model,ToolBarBuilder builder){
		List<Action> actions=createCommonCURD_Actions(model);
		for(Action a:actions){
			builder.add(a);
		}
		return actions; 
	}
	
	public static void configAction(final Action a,final String id,final String defaultIconPath){
		if(Application.isLoaded()){
			Application.instance().getActionManager().configure(a, id);
		}else{
			a.putValue(Action.NAME, id);
			a.putValue(Action.SMALL_ICON, getIconFromResource(defaultIconPath));
		}
	}
	
	public static Icon getIconFromResource(String path){
		try {
			ClassLoader cl=CommandUtils.class.getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
	
	public static Image getImageFromResource(String path){
		try {
			ClassLoader cl=CommandUtils.class.getClassLoader();
			URL url=cl.getResource(path);
			return ImageIO.read(url);			
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
	

}
