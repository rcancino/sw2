/**
 * 
 */
package com.luxsoft.siipap.swing.controls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.Timer;

import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.painter.PainterIcon;

/**
 * Sub clase de JXBusyLabel para ajustar el painter
 *  
 * @author Ruben Cancino Ramos
 *
 */
public class BusyLabel extends JLabel {
    private BusyPainter busyPainter;
    private Timer busy;
    
    /** Creates a new instance of JXBusyLabel */
    public BusyLabel() {
        busyPainter = new BusyPainter();
        busyPainter.setBarLength(7f);
        busyPainter.setBarWidth(3f);
        busyPainter.setBaseColor(Color.LIGHT_GRAY);
        busyPainter.setHighlightColor(getForeground());
        Dimension dim = new Dimension(15,15);
        PainterIcon icon = new PainterIcon(dim);
        icon.setPainter(busyPainter);
        this.setIcon(icon);
    }
    
    /**
     * <p>Gets whether this <code>JXBusyLabel</code> is busy. If busy, then
     * the <code>JXBusyLabel</code> instance will indicate that it is busy,
     * generally by animating some state.</p>
     * 
     * @return true if this instance is busy
     */
    public boolean isBusy() {
        return busy != null;
    }

    /**
     * <p>Sets whether this <code>JXBusyLabel</code> instance should consider
     * itself busy. A busy component may indicate that it is busy via animation,
     * or some other means.</p>
     *
     * @param busy whether this <code>JXBusyLabel</code> instance should
     *        consider itself busy
     */
    public void setBusy(boolean busy) {
        boolean old = isBusy();
        if (!old && busy) {
            startAnimation();
            firePropertyChange("busy", old, isBusy());
        } else if (old && !busy) {
            stopAnimation();
            firePropertyChange("busy", old, isBusy());
        }
    }
    
    private void startAnimation() {
        if(busy != null) {
            stopAnimation();
        }
        
        busy = new Timer(100, new ActionListener() {
            int frame = 8;
            public void actionPerformed(ActionEvent e) {
                frame = (frame+1)%8;
                busyPainter.setFrame(frame);
                repaint();
            }
        });
        busy.start();
    }
    
    private void stopAnimation() {
        busy.stop();
        busyPainter.setFrame(-1);
        repaint();
        busy = null;
    }
}