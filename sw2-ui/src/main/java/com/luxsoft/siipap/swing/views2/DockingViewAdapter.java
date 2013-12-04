package com.luxsoft.siipap.swing.views2;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;

import com.luxsoft.siipap.swing.browser.FilteredBrowserPanel;

public class DockingViewAdapter implements DockingWindowListener{
	
	private final DockingView dockingView;
	

	public DockingViewAdapter(DockingView view) {		
		this.dockingView = view;
	}

	public void windowAdded(DockingWindow addedToWindow,DockingWindow addedWindow) {
		System.out.println("windowAdded");
		FilteredBrowserPanel browser=findBrowser(addedWindow);
		if(browser!=null){
			browser.open();
		}
	}

	public void windowRemoved(DockingWindow removedFromWindow,DockingWindow removedWindow) {
		
	}

	public void windowShown(DockingWindow window) {
		System.out.println("windowShown");
		FilteredBrowserPanel browser=findBrowser(window);
		dockingView.setBrowser(browser);
	}

	public void windowHidden(DockingWindow window) {
		
	}

	public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
		//dockingView.vistaSeleccionada(window);
	}

	public void windowClosing(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowClosed(DockingWindow window) {
		System.out.println("windowClosed");
		FilteredBrowserPanel browser=findBrowser(window);
		if(browser!=null){
			browser.close();
		}
		
	}

	public void windowUndocking(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowUndocked(DockingWindow window) {
		
	}

	public void windowDocking(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowDocked(DockingWindow window) {
		
		Object o=window.getClientProperty(FilteredBrowserPanel.CLIENTE_PROPERTY_ID);
		if(o!=null){
			FilteredBrowserPanel browser=(FilteredBrowserPanel)o;				
			dockingView.setBrowser(browser);
			return;
		}
		FilteredBrowserPanel browser=findBrowser(window);
		dockingView.setBrowser(browser);
	}

	public void windowMinimizing(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowMinimized(DockingWindow window) {
		System.out.println("windowMinimized");
		dockingView.setBrowser(null);
	}

	public void windowMaximizing(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowMaximized(DockingWindow window) {
		//dockingView.vistaSeleccionada(window);
	}

	public void windowRestoring(DockingWindow window)throws OperationAbortedException {
		
	}

	public void windowRestored(DockingWindow window) {
		
	}
	
	public FilteredBrowserPanel findBrowser(final DockingWindow window){	
		if(window==null){
			return null;
		}		
		for(int index=0;index<window.getChildWindowCount();index++){
			
			Object o=window.getChildWindow(index).getClientProperty(FilteredBrowserPanel.CLIENTE_PROPERTY_ID);
			if(o!=null){
				FilteredBrowserPanel browser=(FilteredBrowserPanel)o;				
				return browser;
			}
		}
		return null;
	}
	

}
