package com.luxsoft.siipap.cxc.ui.consultas;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXPanel;

import com.jgoodies.uif.util.Mode;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.luxsoft.siipap.swing.utils.ComponentUtils;
/**
 * An abstract superclass that minimizes the effort required to build
 * consistent Swing JWindow's  quickly. It forms a template process 
 * to build a standardized JWindow and provides convenience behavior 
 * to create frequently used JWindows
 *  
 */
public abstract class AbstractWindow extends JWindow{
	
	
	
	public AbstractWindow(){
		
	}
	
	 /**
     * Builds the dialog content, marks it as not canceled and makes it visible.
     */
    public void open() {
        build();
        setVisible(true);
    }
	
	protected HeaderPanel header;	
	
	protected JXPanel xcontent;
	
	protected void build() {
		xcontent=new JXPanel();
        xcontent.add(buildContentPane());

        // Work around the JRE's gray rect problem.
        // TODO: Remove this if fixed in the JRE.
        setBackground(xcontent.getBackground());
        
        resizeHook(xcontent);
        setContentPane(xcontent);
        pack();
        locateOnScreen();
        registerCancelOnEscapeKey();
    }
	
	
	/**
     * Builds and returns the content pane, sets the border and 
     * puts an optional header component in the dialog's north.<p>
     * 
     * Subclasses will rarely override this method.
     * 
     * @return the dialog's content pane with a border set and 
     *     optional header in the north
     */
    protected JComponent buildContentPane() {
        JComponent center = buildContent();
        center.setBorder(getDialogBorder());

        header = buildHeader();
        if (header == null)
            return center;

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }
    
    protected abstract HeaderPanel buildHeader();
	
    protected abstract  JComponent buildContent();
    
    /**
     * TODO: Move this constant to the Forms Borders class.
     */
    protected static final Border DIALOG_BORDER =
        new EmptyBorder(12, 10, 10, 10);
	
    protected Border getDialogBorder() {
        return DIALOG_BORDER;
    }
    
	/**
     * Configures the closing behavior: invokes #doCloseWindow
     * instead of just closing the dialog. This allows subclasses
     * to hook into the close process and perform a custom code sequence.
     */
    protected void configureWindowClosing() {
        addWindowListener(new WindowClosingHandler());
    }
    
    /**
     * Resizes the specified component. This method is called during the build
     * process and enables subclasses to achieve a better aspect ratio,
     * by applying a resizer, e.g. the <code>Resizer</code>.
     * 
     * @param component   the component to be resized
     */
    protected void resizeHook(JComponent component) {
      
    }
    
    
    
    /**
     * Locates the dialog on the screen. The default implementation 
     * sets the location relative to the parent.<p>
     * 
     * Subclasses may choose to center the dialog or put it in a screen corner.
     */
    protected void locateOnScreen() {
        setLocationRelativeTo(getParent());
    }
    

    /**
     * Maps the escape key to this dialog's cancel Action.
     * 
     * @see #deregisterCancelOnEscapeKey
     * @see #setEscapeCancelsMode(Mode)
     * @see #setEscapeCancelsDefaultMode(Mode)
     */
    private void registerCancelOnEscapeKey() {
        ComponentUtils.addAction(
                getRootPane(),
                getCloseAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private Action closeAction;
    
    /**
     * Lazily creates and returns the Close Action that invokes #doClose.
     * 
     * @return the lazily created Close Action
     * 
     * @see #createCloseButton
     * @see #buildButtonBarWithOKCancelApply()
     */
    public Action getCloseAction() {
        if (closeAction == null) {
            closeAction = new AbstractAction(){
				public void actionPerformed(ActionEvent e) {
					close();
				}
            	
            };
        }
        return closeAction;
    }

    /**
     * Closes the dialog: makes it invisible and disposes it, 
     * which in turn releases all required OS resources.
     */
    public void close() {
    	doReset();
        setVisible(false);
    }
    
    public void doReset() {
    	
    }
    
    private class WindowClosingHandler extends WindowAdapter {
        
        public void windowClosing(WindowEvent e) {
           close();
        }

    }
	
	

}
