package com.luxsoft.siipap.swing.impl;

import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.SwingWorker;

import net.infonode.gui.hover.hoverable.HoverManager;
import net.infonode.tabbedpanel.Tab;
import net.infonode.tabbedpanel.TabAdapter;
import net.infonode.tabbedpanel.TabEvent;
import net.infonode.tabbedpanel.TabRemovedEvent;
import net.infonode.tabbedpanel.TabStateChangedEvent;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.theme.TabbedPanelTitledTabTheme;
import net.infonode.tabbedpanel.titledtab.TitledTab;

import org.apache.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.jgoodies.looks.LookUtils;
import com.luxsoft.siipap.swing.Page;
import com.luxsoft.siipap.swing.View;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.TaskUtils;

/**
 * Page implementation that uses InfoNode TabbedPane as supporting container
 * of views
 * 
 * @author Ruben Cancino
 *
 */
public class InfoNodeTabbedPage implements Page{
	
	private static final Dimension PREFERRED_SIZE = LookUtils.IS_LOW_RESOLUTION? new Dimension(620, 510): new Dimension(760, 570);
            
	private TabbedPanel tabbedPanel;
    private TabbedPanelTitledTabTheme activeTheme;
    private EventList<View> vistas;
    private Logger logger=Logger.getLogger(getClass());
             

    /**
     * Agrega la vista al TabbedPanel, si la vista ya existe esta solo es activada
     * 
     */
    public void addView(View view) { 
    	
		TitledTab tab=(TitledTab)getTab(view);
		if(tab==null){
			/*
			JComponent c=view.getContent();
			//Make sure a referenco to the view is stored in the client properties of the content
			if(c.getClientProperty("view")!=view) 
				c.putClientProperty("view",view);
			
			int current=getTabbedPanel().getTabCount();
			String title=view.getVisualSupport()!=null?view.getVisualSupport().getLabel():view.getId()+current;
			Icon icon=view.getVisualSupport()!=null?view.getVisualSupport().getIcon():null;
			
			tab=new TitledTab(title,icon,c,null);
			tab.setName(view.getId());
			tab.setHighlightedStateTitleComponent(InfoNodeUtils.createCloseTabButton(tab));
			tab.getProperties().addSuperObject(activeTheme.getTitledTabProperties());
			
			getTabbedPanel().addTab(tab);
			getViews().add(current, view);
			**/
			addNewTablForView2(view);
		}else{
			System.out.println("Vista existente");
			tab.setSelected(true);
			if(logger.isDebugEnabled()){
				String msg=MessageFormat.format("View {0} ya esta agregada al Page, solo sera seleccionada", tab.getName());
				logger.debug(msg);
			}
		}
	}
    
    
    private void addNewTablForView(final View view){   
    	
    	JComponent c=view.getContent();
		//Make sure a referenco to the view is stored in the client properties of the content
		if(c.getClientProperty("view")!=view) 
			c.putClientProperty("view",view);
		
		int current=getTabbedPanel().getTabCount();
		String title=view.getVisualSupport()!=null?view.getVisualSupport().getLabel():view.getId()+current;
		Icon icon=view.getVisualSupport()!=null?view.getVisualSupport().getIcon():null;
		
		TitledTab tab=new TitledTab(title,icon,c,null);
		tab.setName(view.getId());
		tab.setHighlightedStateTitleComponent(InfoNodeUtils.createCloseTabButton(tab));
		tab.getProperties().addSuperObject(activeTheme.getTitledTabProperties());
		
		getTabbedPanel().addTab(tab);
		//final int current=getTabbedPanel().getTabCount();
		//getViews().add(view);
		//current=getTabbedPanel().getTabCount();
		//getViews().add(current-1, view);
		tab.setSelected(true);
    }
    
    private void addNewTablForView2(final View view){    	
    	SwingWorker<TitledTab, String> worker=new SwingWorker<TitledTab, String>(){

			@Override
			protected TitledTab doInBackground() throws Exception {
				JComponent c=view.getContent();
				//Make sure a referenco to the view is stored in the client properties of the content
				if(c.getClientProperty("view")!=view) 
					c.putClientProperty("view",view);
				
				int current=getTabbedPanel().getTabCount();
				String title=view.getVisualSupport()!=null?view.getVisualSupport().getLabel():view.getId()+current;
				Icon icon=view.getVisualSupport()!=null?view.getVisualSupport().getIcon():null;
				
				TitledTab tab=new TitledTab(title,icon,c,null);
				tab.setName(view.getId());
				tab.setHighlightedStateTitleComponent(InfoNodeUtils.createCloseTabButton(tab));
				tab.getProperties().addSuperObject(activeTheme.getTitledTabProperties());
				return tab;
			}

			@Override
			protected void done() {
				try {
					final TitledTab tab=get();
					getTabbedPanel().addTab(tab);
					final int current=getTabbedPanel().getTabCount();
					getViews().add(current-1, view);
					tab.setSelected(true);
				} catch (Exception e) {
					e.printStackTrace();
					MessageUtils.showError("Error agregando vista", e);
				} 
				
			}
			
    		
    	};
    	TaskUtils.executeSwingWorker(worker);
    }
    
    public TabbedPanel getTabbedPanel(){
    	return (TabbedPanel)getContainer();
    }

	public JComponent getContainer() {
		if(tabbedPanel==null){
			initComponents();
			tabbedPanel.setPreferredSize(PREFERRED_SIZE);			
		}		
		return tabbedPanel;
	}	
	
	private void initComponents() {
		activeTheme = InfoNodeUtils.getDefaultTabbedPanelTitledTheme();
        tabbedPanel=new TabbedPanel();
        HoverManager.getInstance().setEventListeningActive(false);
        tabbedPanel.getProperties().addSuperObject(activeTheme.getTabbedPanelProperties());
        tabbedPanel.addTabListener(new TabHandler());        
        //tabbedPanel.getProperties().getContentPanelProperties().getComponentProperties().setInsets(new Insets(15,5,5,5));
        //tabbedPanel.getProperties().getTabAreaComponentsProperties().getComponentProperties().setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        tabbedPanel.getProperties().getTabAreaComponentsProperties().setStretchEnabled(true);
        
    }

	public void close() {
		if(logger.isDebugEnabled()){
			logger.debug("Closing all views hosted in this TabbedPane" );
		}
		while(getTabbedPanel().getSelectedTab()!=null){
			Tab tab=tabbedPanel.getSelectedTab();
			tabbedPanel.removeTab(tab);
		}
		
	}
	
	public List<View> getViews() {
		if(vistas==null){
			vistas=new BasicEventList<View>();
		}
		return vistas;
	}


	
	
	/**
	 * Returns the tab hosting the view
	 * 
	 * @param view
	 * @return
	 */	
	private Tab getTab(final View view){
		for(int i=0;i<getTabbedPanel().getTabCount();i++){
			Tab tab=getTabbedPanel().getTabAt(i);
			JComponent content=tab.getContentComponent();
			View v=(View)content.getClientProperty("view");
			if(v!=null){
				if(v.equals(view))
					return tab;
			}
			
		}
		return null;
	}
	
	
	private class TabHandler extends TabAdapter {

		public void tabAdded(TabEvent e) {
			JComponent c=e.getTab().getContentComponent();
			View view=(View)c.getClientProperty("view");
			if(view!=null ){
				view.open();
				logger.debug("Agregando vista: "+view.getId());
			}
				
						
		}

		
		public void tabRemoved(TabRemovedEvent e) {
			JComponent c=e.getTab().getContentComponent();
			View view=(View)c.getClientProperty("view");
			
			e.getTab().removeTabListener(this);
			if(view!=null){
				view.focusLost();
				view.close();			
				boolean ok=getViews().remove(view);
				if(ok){
					if(logger.isDebugEnabled())
						logger.debug("Vista removida exitosamente: "+view.getId()+" Vistas restantes: "+getViews().size());
				}
				else
					logger.info("La vista: "+view.getId()+" No fue removida del lista de vistas del conteneor esto puede causar un memory leak");
			}
			
			
		}

		public void tabSelected(TabStateChangedEvent e) {
			if(e.getTab()!=null){
				JComponent c=e.getTab().getContentComponent();
				View view=(View)c.getClientProperty("view");
				if(view!=null)
					view.focusGained();
			}
		}
    	
    }


	
	

}
