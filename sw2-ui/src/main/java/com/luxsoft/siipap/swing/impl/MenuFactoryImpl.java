package com.luxsoft.siipap.swing.impl;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.jgoodies.uif.builder.MenuBuilder;
import com.luxsoft.siipap.swing.MenuFactory;
import com.luxsoft.siipap.swing.actions.ActionManager;
import com.luxsoft.siipap.swing.actions.Actions;
import com.luxsoft.siipap.swing.actions.ShowViewAction;


/**
 * Basic implementation of MenuFactory
 * 
 * @author Ruben Cancino
 *
 */
public class MenuFactoryImpl implements MenuFactory{
	
	private JMenuBar bar;
	private ActionManager actionManager;
	
	
	public MenuFactoryImpl(){
	}
	
	public JMenu getModuleMenu() {
		return null;
	}

	public JMenuBar getMenuBar(){
		if(bar==null){
			bar=buildMenuBar();
		}
		return bar;
	}
	
	protected JMenuBar buildMenuBar(){
		JMenuBar bar=new JMenuBar();
		//Set a hint so that JGoodies Looks will detect it as being in the header.
		bar.putClientProperty(Options.HEADER_STYLE_KEY, 
								  HeaderStyle.BOTH);
		// Unlike the default, use a separator border.
		bar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY, 
								  BorderStyle.SEPARATOR);
		bar.putClientProperty(PlasticLookAndFeel.BORDER_STYLE_KEY,    
								  BorderStyle.SEPARATOR);
		
		bar.add(buildFileMenu());
		List<JMenu> customMenus=new ArrayList<JMenu>();
		buildCustomMenus(customMenus);
		for(JMenu mnu:customMenus){
			bar.add(mnu);
		}
		//bar.add(buildViewMenu());
		//bar.add(buildHelpMenu());
		return bar;
	}
	
	/**
	 * Template method for custom menu creation
	 * 
	 * @param customMenus
	 */
	protected void buildCustomMenus(List<JMenu> customMenus){
		
	}
	
	protected JMenu buildFileMenu() {
        MenuBuilder builder = new MenuBuilder("Archivo", 'A');        
        builder.addSeparator();
        Action a=getActionManager().getAction(Actions.ExitApplication.toString());
        a.setEnabled(true);
        builder.add(a);
        
		return builder.getMenu();
	}
	
	/**
	 * Builds and returns the Component menu.
	 */
	protected JMenu buildViewMenu() {
		MenuBuilder builder = new MenuBuilder("Ventanas", 'V');		
		//ViewFactory.instance().load();
		//Action a1=getActionManager().getAction("welcomeView");
		//builder.add(a1);
		builder.add(getActionManager().getAction(Actions.ShowWelcomeView.toString()));
		return builder.getMenu();
	}
	

	/**
	 * Builds and returns the Help menu.
	 */
	protected JMenu buildHelpMenu() {
        MenuBuilder builder = new MenuBuilder("Ayuda", 'y');
        //builder.add(getActionManager().getAction("helpContentAction"));
        builder.addSeparator();
        //builder.add(Commons.getTipOfTheDayAction());
        //builder.add(Commons.getOpenAboutDialogAction());
        //builder.addSeparator();
        //builder.add(ActionManager.get(DLGSwingModel.OPEN_ABOUT_DIALOG_ID));        
		return builder.getMenu();
	}



	public ActionManager getActionManager() {
		return actionManager;
	}

	public void setActionManager(ActionManager actionManager) {
		this.actionManager = actionManager;
	}
	
	public ShowViewAction getViewAction(String viewId,String role){
		return getViewAction(viewId, role, true);
	}
	
	public ShowViewAction getViewAction(String viewId,String role,boolean showWhenNotGranted){
		ShowViewAction a=new ShowViewAction(viewId, role);
		getActionManager().configure(a, viewId);
		a.setShowWhenNotGranted(showWhenNotGranted);
		return a;
	}

}
