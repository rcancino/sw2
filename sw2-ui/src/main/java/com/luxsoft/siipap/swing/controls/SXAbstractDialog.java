package com.luxsoft.siipap.swing.controls;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.AbstractDialog;
import com.luxsoft.siipap.swing.Application;

public abstract class SXAbstractDialog extends AbstractDialog{
	
	protected Logger logger=Logger.getLogger(getClass());
	
	public SXAbstractDialog(String title){
		this(Application.isLoaded()?Application.instance().getMainFrame():null,title);
	}
	
	public SXAbstractDialog(String title,boolean modal){
		this(Application.isLoaded()?Application.instance().getMainFrame():null,title,modal);
	}

	public SXAbstractDialog(Dialog owner, String title, boolean modal) {
		super(owner, title, modal);		
	}

	public SXAbstractDialog(Dialog owner, String title) {
		super(owner, title);		
	}

	public SXAbstractDialog(Dialog owner) {
		super(owner);		
	}

	public SXAbstractDialog(Frame owner, String title, boolean modal) {
		super(owner, title, modal);		
	}

	public SXAbstractDialog(Frame owner, String title) {
		super(owner, title);		
	}

	public SXAbstractDialog(Frame owner) {
		this(owner,"Sin Titulo");
	}
	
	protected void build(){
		super.build();
		addWindowListener(new WindowOpeningHandler());
	}
	protected void onWindowOpened(){
		
	}

	/**
	 * Utiliza Luxsoft Application framework en lugar de JGoodies ResourceUtils
	 */
    protected String getString(String key, String defaultText) {
    	if(Application.isLoaded()){
    		return Application.instance().getMessage(key, defaultText);
    	}else
    		return defaultText;        
    }
	
    private Action okAction=null;
    private Action cancelAction=null;
    
    public Action getOKAction() {
    	if(okAction==null){
    		okAction=super.getOKAction();
    		okAction.putValue(Action.SMALL_ICON,getIconFromResource("images2/tick.png") );
    	}
    	return okAction;
    }
    
    public Action getCancelAction() {
    	if(cancelAction==null){
    		cancelAction=super.getCancelAction();
    		cancelAction.putValue(Action.SMALL_ICON,getIconFromResource("images2/cross.png"));
    		cancelAction.putValue(Action.NAME,"CANCELAR [ESC]");
    	}
    	return cancelAction;
    }
    
    public Action getCloseAction() {
    	Action a=super.getCloseAction();
    	a.putValue(Action.NAME, "Cerrar");
    	return a;
    }
    
    /**
     * Builds and returns a button bar with three buttons: OK, Cancel and Reset.
     * Uses #createOKButton, #createCancelButton and #createResetButton()
     * to create standardized buttons.
     * 
     * @return a button bar that contains: an OK, a Cancel and an Apply button
     * @see #createOKButton(boolean)
     * @see #createCancelButton()
     * @see #createApplyButton()
     */
    protected JComponent buildButtonBarWithOKCancelReset() {
        JPanel bar = ButtonBarFactory.buildOKCancelApplyBar(
            createOKButton(true),
            createCancelButton(),
            createResetButton());
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
        return bar;
    }
	
    
    public Icon getIconFromResource(String path){
		try {
			ClassLoader cl=getClass().getClassLoader();
			URL url=cl.getResource(path);
			Icon icon=new ImageIcon(url);
			return icon;
		} catch (Exception e) {
			logger.info("No pudo cargar icono: "+path+" Msg:"+e.getMessage() );
			return null;
		}		
	}
    
    
    

    @Override
	protected void setResizable() {
		
	}




	private class WindowOpeningHandler extends WindowAdapter{

		@Override
		public void windowOpened(WindowEvent e) {
			onWindowOpened();
		}
    	
    }

}
