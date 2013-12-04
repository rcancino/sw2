package com.luxsoft.siipap.swing;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

//import com.luxsoft.luxor.swing.Application;
import com.jgoodies.uif.util.WindowUtils;
import com.luxsoft.siipap.swing.controls.AbstractFrame;
import com.luxsoft.siipap.swing.controls.Header;
import com.luxsoft.siipap.swing.controls.StatusBar;

public class DefaultApplicationWindow implements ApplicationWindow{
	
	protected AbstractFrame mainFrame;
	private Page windowPage;
	private MenuFactory menuFactory;
	private ToolbarFactory toolbarFactory;
	private StatusBar statusBar;
	private Header header;
	private ResourceLocator resourceLocator;
	private boolean confirClose=true;

	private String titulo="SiipapWin-Ex";

	public JFrame getWindow() {
		if(mainFrame==null){
			mainFrame=buildMainFrame();
			mainFrame.build();
			mainFrame.addWindowListener(new WindowAdapter(){
				@Override
				public void windowClosing(WindowEvent e) {
					close();
				}
				
				
			});
			
		}
		return mainFrame;
	}
	
	protected AbstractFrame buildMainFrame(){
		AbstractFrame frame=new AbstractFrame("mainFrame"){

			@Override
			protected JComponent buildContentPane() {
				JComponent content=buildWorkSpace();
				return content;
			}
			
		};
		if(getMenuFactory()!=null)
			frame.setJMenuBar(getMenuFactory().getMenuBar());
		if(getResourceLocator()!=null)
			frame.setResourceLocator(getResourceLocator());
		Image img=getResourceLocator().getImage("application.icon.16x16");
		//System.out.println("Image: "+img);
		frame.setIconImage(img);
		frame.setTitle(getTitulo());
		return frame;
	}
	
	protected JComponent buildWorkSpace(){
		
		//Page
		JPanel mainPanel=new JPanel(new BorderLayout());
		mainPanel.add(getWindowPage().getContainer(),BorderLayout.CENTER);
		
		//Header y Toolbar
		JPanel topPanel=new JPanel(new BorderLayout());
		if(getHeader()!=null){
			topPanel.add(getHeader().getHeader(),BorderLayout.NORTH);
		}
		if(getToolbarFactory()!=null){
			topPanel.add(getToolbarFactory().getToolbar(),BorderLayout.CENTER);
		}
		
		mainPanel.add(topPanel,BorderLayout.NORTH);
		if(getStatusBar()!=null)
			mainPanel.add(getStatusBar().getStatusPanel(),BorderLayout.SOUTH);
		return mainPanel;
	}

	public Page getWindowPage() {		
		return windowPage;
	}	


	public void open() {
		restoreWindowState();
		getWindow().setVisible(true);
	}
	
	public void close() {
		if(confirClose){
			int res=JOptionPane.showConfirmDialog(Application.instance().getMainFrame(), "Salir del sistema?","Salir",JOptionPane.OK_CANCEL_OPTION);
			if(res==JOptionPane.OK_OPTION)
				closeApplication();
		}else
			closeApplication();
		
	}
	
	protected void closeApplication(){
		storeWindowState();
		getWindowPage().close();
		getWindow().setVisible(false);
		if(Application.isLoaded()){
			Application.instance().close();
		}else			
			getWindow().dispose();
	}
	
	private void storeWindowState(){
		if(Application.isLoaded()){
			Preferences pref=Application.instance().getUserPreferences();
			if(pref==null) 
				return;
	    	WindowUtils.storeBounds(pref, getWindow());
	        WindowUtils.storeState(pref, getWindow());
		}    	
    }

    protected void restoreWindowState() {
    	if(Application.isLoaded()){
			Preferences userPrefs=Application.instance().getUserPreferences();			
			if(userPrefs==null) 
				return;
			WindowUtils.restoreBounds(getWindow(), userPrefs);
	        WindowUtils.restoreState(getWindow(), userPrefs, false);
		}        
    }    
    
    /*** Colaboradores asignados con IoC depencency injection ****/

	public MenuFactory getMenuFactory() {
		return menuFactory;
	}

	public void setMenuFactory(MenuFactory menuFactory) {
		this.menuFactory = menuFactory;
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	public void setStatusBar(StatusBar statusBar) {
		this.statusBar = statusBar;
	}

	public ToolbarFactory getToolbarFactory() {
		return toolbarFactory;
	}

	public void setToolbarFactory(ToolbarFactory toolbarFactory) {
		this.toolbarFactory = toolbarFactory;
	}

	public void setWindowPage(Page windowPage) {
		this.windowPage = windowPage;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}

	public boolean isConfirClose() {
		return confirClose;
	}

	public void setConfirClose(boolean confirClose) {
		this.confirClose = confirClose;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	
	
	

}
