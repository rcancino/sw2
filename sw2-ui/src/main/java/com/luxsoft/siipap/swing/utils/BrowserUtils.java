package com.luxsoft.siipap.swing.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXTable;

import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uifextras.util.UIFactory;

/**
 * A factory class that consists only of static methods to create frequently
 * used Swing components for Browsers
 * 
 * @author Ruben Cancino
 * @version $Revision: 1.2 $
 */
public class BrowserUtils {
	
	/**
	 * Creates and returns a standard <code>JSplitPane</code> suited for a master - detail browser
	 * @param master
	 * @param detail
	 * @return
	 */
	public static JSplitPane buildMasterDetailBrowserPanel(final JComponent masterPanel,final JTable detail){	
		masterPanel.setBorder(Borders.DLU4_BORDER);		
		JComponent detailSp=UIFactory.createTablePanel(detail);
		JSplitPane sp=UIFactory.createStrippedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				masterPanel,
				detailSp,.3);
		return sp;
	}
	
	/**
	 * Creates and return a standard <code>JComponent</code> with a browser,filter and status bar
	 * 
	 * 
	 * @param filterPanel
	 * @param browserPanel
	 * @param statusPanel
	 * @return
	 */
	public static JComponent buildStandardBrowser(final JComponent filterPanel,final JComponent browserPanel,final JComponent statusPanel){	
		JPanel content=new JPanel(new BorderLayout(5,10));
		if(filterPanel!=null)
			content.add(filterPanel,BorderLayout.NORTH);
		content.add(browserPanel,BorderLayout.CENTER);
		if(statusPanel!=null)
			content.add(statusPanel,BorderLayout.SOUTH);
		return content;
	}
	
	/**
	 * Crea un browser simple 
	 * 
	 * @param grid
	 * @param inputField
	 * @param statusPanel
	 * @return
	 */
	public static JComponent buildSimpleBrowser(final JTable grid,final JTextField inputField,final JToolBar toolbar,final JComponent statusPanel){
		JPanel panel=new JPanel(new BorderLayout(5,5));
		JComponent filterPanel=buildTextFilterPanel(inputField);
		JComponent gridPanel=UIFactory.createTablePanel(grid);
		panel.add(toolbar,BorderLayout.NORTH);
		panel.add(buildStandardBrowser(filterPanel,gridPanel,statusPanel),BorderLayout.CENTER);
		return panel;
	}
	
	
	
	public JComponent buildStatusBar(){		
		return new JXStatusBar();	
	}
	
	public static JComponent buildMasterListPanel(final JTextField serchField,final JList content){
		JScrollPane sp=new JScrollPane(content);
		FormLayout layout=new FormLayout("l:100dlu:g","p,4dlu,f:p,4dlu,f:p,4dlu,f:p:g");
		CellConstraints cc=new CellConstraints();
		PanelBuilder builder=new PanelBuilder(layout);
		//builder.setDefaultDialogBorder();
		builder.addSeparator("Filtrar:");
		builder.add(serchField,cc.xy(1,3,"c,f"));
		//builder.nextRow();
		//builder.nextRow();
		builder.addSeparator("Selección:",cc.xy(1,5));
		builder.add(sp,cc.xy(1,7,"f,f"));
		return builder.getPanel();
	}
	
	public static JXList buildMasterList(){
		JXList list=new JXList();
		return list;
	}
	
	public static JComponent buildTextFilterPanel(final JTextField filterTextField){
		FormLayout layout=new FormLayout(
				"3dlu,l:50dlu,p:g",
				"p");
		CellConstraints cc=new CellConstraints();
		PanelBuilder builder=new PanelBuilder(layout);
		//builder.setDefaultDialogBorder();
		JLabel l=DefaultComponentFactory.getInstance().createTitle("Filtrar: ");
		builder.add(l,cc.xy(2,1));
		builder.add(filterTextField,cc.xy(3,1));
		return builder.getPanel();
	}
	
	public static JTextField buildFilterTextField(){		
		JTextField textFilter=new JTextField(50);
		return textFilter;
		
	}
	
	public static JXTable buildBrowserGrid(){
		JXTable grid=new JXTable();
		grid.setColumnControlVisible(true);
		grid.setHorizontalScrollEnabled(true);
		grid.setSortable(false);
		grid.setRolloverEnabled(true);
		Highlighter alternate=HighlighterFactory.createAlternateStriping();//new HighlighterPipeline();		
		grid.setHighlighters(new Highlighter[]{alternate});
		grid.setRolloverEnabled(true);
		//DateTimeTableCellRenderer dr=new DateTimeTableCellRenderer(new SimpleDateFormat("dd/MM/yyyy"));
		StdDateTableCellRenderer dr=new StdDateTableCellRenderer();
		grid.setDefaultRenderer(Date.class,dr);
		grid.setDefaultRenderer(Boolean.class,new BooleanTableCellRenderer());
		grid.getSelectionMapper().setEnabled(false);
		grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
		//grid.setSelectionModel(getSelectionModel());
		return grid;
	}
	
	 public static JComponent createTablePanel(JTable table) {
	        Color background = UIManager.getColor("window");

	        JScrollPane scrollPane = new JScrollPane(table);
	        scrollPane.getViewport().setOpaque(false);
	        scrollPane.getViewport().setBackground(background);
	        scrollPane.setOpaque(false);

	        scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER,
	                new JPanel(null));
	        scrollPane.setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER,
	                new JPanel(null));

	        JPanel panel = new JPanel(new GridLayout(0, 1)) {
	            public void updateUI() {
	                super.updateUI();
	                setBackground(UIManager.getColor("window"));
	            }
	        };
	        panel.setPreferredSize(new Dimension(280, 200));
	        panel.add(scrollPane);

	        return panel;
	}
	 
	public static TableCellRenderer getDateRenderer(){
		return new StdDateTableCellRenderer();
	}
	
	public static class StdDateTableCellRenderer extends DateTimeTableCellRenderer{
		
		public StdDateTableCellRenderer(){
			super(new SimpleDateFormat("dd/MM/yyyy"));
			
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	            int row, int column) {
			doPrepareRenderer(table, isSelected, hasFocus, row, column);
	        if (value != null) {
	        	Object o=getFormat().format(value);
	            setValue(o);
	        }
	        else {
	            setValue(null);
	        }
	        return this;
		}
		
	}
	
	public static final TableCellRenderer getPorcentageTableCellRenderer(){
		return new PorcentageCellRenderer();
	}
	
	public static class PorcentageCellRenderer extends FormatTableCellRenderer{
		
		public PorcentageCellRenderer(){
			super(NumberFormat.getPercentInstance(Locale.US));
			JLabel l=(JLabel)this;
			l.setHorizontalAlignment(JLabel.RIGHT);
			NumberFormat nf=NumberFormat.getPercentInstance(Locale.US);
			nf.setMinimumFractionDigits(2);
			nf.setMaximumFractionDigits(2);
			setFormat(nf);
		}
		
	}
	
	public static class BooleanTableCellRenderer extends OptimizedTableCellRenderer implements TableCellRenderer {

	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	            int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        Boolean b=(Boolean)value;
	        if(b)	        	
	        	this.setText("SI");
	        else
	        	this.setText("NO");
	        return this;
	    }

	    public BooleanTableCellRenderer() {
	        super();
	    }

	}
	
	/**
	 * Renders a date/time in the standard format.
	 * 
	 * @author Keith Donald
	 */
	public static class DateTimeTableCellRenderer extends FormatTableCellRenderer {
	    
	    public DateTimeTableCellRenderer() {
	        super(new SimpleDateFormat("EEE M/d/yyyy H:mm:ss"));
	    }

	    public DateTimeTableCellRenderer(DateFormat formatter) {
	        super(formatter);
	    }

	    public DateTimeTableCellRenderer(TimeZone timeZone) {
	        this();
	        getDateFormat().setTimeZone(timeZone);
	    }

	    public DateFormat getDateFormat() {
	        return (DateFormat) getFormat();
	    }

	    public void useGMTTime() {
	        setFormat(new SimpleDateFormat("EEE M/d/yyyy H:mm:ss z"));
	        getDateFormat().setTimeZone(TimeZone.getTimeZone("GMT"));
	    }

	    public void useLocalTime() {
	        setFormat(new SimpleDateFormat("EEE M/d/yyyy H:mm:ss"));
	    }
	}
	
	/**
	 * @author Oliver Hutchison
	 */
	public static class FormatTableCellRenderer extends OptimizedTableCellRenderer {
	    
	    private Format format;
	    
	    public FormatTableCellRenderer(Format format) {
	        setFormat(format);
	    }
	    
	    protected Format getFormat() {
	        return format;
	    }
	    
	    protected void setFormat(Format format) {
	        this.format = format;        
	    }
	    
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	            int row, int column) {
	        doPrepareRenderer(table, isSelected, hasFocus, row, column);
	        if (value != null) {
	            setValue(format.format(value));
	        }
	        else {
	            setValue(null);
	        }
	        return this;
	    }
	}
	
	/**
	 * A table cell renderer that has been optimized for performance
	 * 
	 * @author Keith Donald
	 */
	public static class OptimizedTableCellRenderer extends DefaultTableCellRenderer {
	    protected Border focusBorder = UIManager.getBorder("Table.focusCellHighlightBorder");

	    protected Color background = UIManager.getColor("Table.focusCellBackground");

	    protected Color foreground = UIManager.getColor("Table.focusCellForeground");

	    protected Color editableForeground;

	    protected Color editableBackground;

	    protected void doPrepareRenderer(JTable table, boolean isSelected, boolean hasFocus, int row, int column) {
	        if (isSelected) {
	            setForeground(table.getSelectionForeground());
	            setBackground(table.getSelectionBackground());
	        }
	        else {
	            setForeground(table.getForeground());
	            setBackground(table.getBackground());
	        }
	    	setFont(table.getFont());
	    	if (hasFocus) {
	    	    setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
	    	    if (table.isCellEditable(row, column)) {
	    	        super.setForeground( UIManager.getColor("Table.focusCellForeground") );
	    	        super.setBackground( UIManager.getColor("Table.focusCellBackground") );
	    	    }
	    	} else {
	    	    setBorder(noFocusBorder);
	    	}
	    }

	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
	            int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//	        doPrepareRenderer(table, isSelected, hasFocus, row, column);
	        setValue(value);
	        return this;
	    }

	    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	        // As long as you don't have any HTML text, this override is ok.
	    }

	    // This override is only appropriate if this will never contain any
	    // children AND the Graphics is not clobbered during painting.
	    public void paint(Graphics g) {
	        ui.update(g, this);
	    }

	    public void setBackground(Color c) {
	        this.background = c;
	    }

	    public Color getBackground() {
	        return background;
	    }

	    public void setForeground(Color c) {
	        this.foreground = c;
	    }

	    public Color getForeground() {
	        return foreground;
	    }

	    public boolean isOpaque() {
	        return (background != null);
	    }

	    // This is generally ok for non-Composite components (like Labels)
	    public void invalidate() {

	    }

	    // Can be ignored, we don't exist in the containment hierarchy.
	    public void repaint() {

	    }
	}

}
