package com.luxsoft.siipap.pos.ui.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;



public final class UIUtils {
	
	
	public static void increaseFontSize(Container  panel,float delta){
		float size=panel.getFont().getSize2D();
		Font font=panel.getFont().deriveFont(size+delta);
		
		updateContainerFont(panel, font);
	}
	
	public static void increaseFontSize(JTable  grid,float delta){
		float size=grid.getFont().getSize2D();
		Font font=grid.getFont().deriveFont(size+delta);
		grid.setFont(font);
		
		/**Size the header font
		size=grid.getTableHeader().getFont().getSize2D();
		font=grid.getTableHeader().getFont().deriveFont(size+delta);
		grid.getTableHeader().setFont(font);**/
	}
	
	/**
	 * Actualiza el {@link Font} de todos los componentes recursivamente 
	 * 
	 * @param container
	 * @param font
	 */
	public static void updateContainerFont(Container container,Font font) {
        int componentCount = container.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            Component child = container.getComponent(i);
            if (child instanceof JPanel) {
            	updateContainerFont((Container)child, font);
            } else {
            	JComponent component = (JComponent) child;
                component.setFont(font);
            }
        }
    }
	
	public static Format buildToStringFormat(){
		return new ToStringFormat();
	}
	
	public static class ToStringFormat extends Format{

		@Override
		public StringBuffer format(Object obj, StringBuffer toAppendTo,
				FieldPosition pos) {
			if(obj!=null)
				toAppendTo.append(obj.toString());
			return toAppendTo;
		}

		@Override
		public Object parseObject(String source, ParsePosition pos) {
			//Not supported
			return null;
		}
		
	}

}
