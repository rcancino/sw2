package com.luxsoft.siipap.swing.utils;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;

public class FormUtils {
	
	public static DefaultFormBuilder createStandarFormBuilder(String title,int cols){
		//FormLayout layout=new FormLayout("l:max(p;50dlu),2dlu,max(p;50dlu):g",				"");
		DefaultFormBuilder builder=new DefaultFormBuilder(getLayout(cols));
		//builder.appendSeparator(title);
		builder.setDefaultDialogBorder();
		//builder.appendTitle(title);		
		//builder.appendSeparator(title);
		//builder.nextLine();
		builder.getPanel().setOpaque(true);
		builder.setBorder(BorderFactory.createTitledBorder(title));
		return builder;
	}
	
	public static  FormLayout getLayout(int majorCols){
		switch (majorCols) {
		case 1:
			return new FormLayout(
					"l:max(p;50dlu),2dlu,min(p;100dlu)",
					"");
		case 2:
			return new FormLayout(
					"l:max(p;50dlu),2dlu,max(p;50dlu):g",
					"");
		default:
			throw new IllegalStateException("No se ha implementado para mas de 3 columnas");
		}
		
	}
	
	public static DefaultFormBuilder createBuilderForCustomBindins(final String title){
		FormLayout layout=new FormLayout(
				"l:p,2dlu,min(p;50dlu):g(.5),3dlu," +//First Column
				"l:p,2dlu,max(p;50dlu):g(.5)" 
				,"");		
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);		
		builder.setDefaultDialogBorder();		
		builder.getPanel().setOpaque(true);
		Border outsideBorder=BorderFactory.createTitledBorder(title);
		Border insideBorder=Borders.DIALOG_BORDER;
		Border b=BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
		builder.setBorder(b);
		return builder;
	}
	
	/**
	 * Panel Builder util para alinear multiples check boxes
	 * 
	 * @param title
	 * @return
	 */
	public static DefaultFormBuilder createCheckBoxSelector(final String title){
		FormLayout layout=new FormLayout(
				"l:p,2dlu,min(p;50dlu):g(.5),3dlu," + //First Column
				"l:p,2dlu,min(p;50dlu):g(.5),3dlu," +//Second Column				
				"l:p,2dlu,max(p;50dlu):g(.5)" 		//Third 
				,"");		
		DefaultFormBuilder builder=new DefaultFormBuilder(layout);	
		//builder.addSeparator(title);
		builder.setDefaultDialogBorder();		
		builder.getPanel().setOpaque(true);
		//builder.setBorder(BorderFactory.createTitledBorder(title));
		return builder;
	}

}
