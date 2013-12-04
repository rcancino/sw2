package com.luxsoft.siipap.swing.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTable.PrintMode;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.SetUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.uif.builder.PopupMenuBuilder;
import com.jgoodies.uifextras.panel.HeaderPanel;
import com.jgoodies.uifextras.util.UIFactory;
import com.luxsoft.siipap.model.CantidadMonetaria;
import com.luxsoft.siipap.swing.controls.SXTable;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
* Extension a la clase de JGoodies
* 
* Consists only of static convenience methods around components.
* 
* @author Karsten Lentzsch
* @version $Revision: 1.3 $
* @since 1.4
*/
public final class ComponentUtils {
	
	/**
	 * Decora un componente JTextField tf para que transfiera el foco al dar enter
	 * 
	 * @param tf
	 */
	public static void setEnterAsTransferFocus(final JTextField tf){
		tf.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				tf.transferFocus();				
			}			
		});
	}
	
	
	/**
	 * Adds the given action to the input map of the given component 
     * for the given key stroke. The key used to register the action 
     * is the Action object itself.
     * 
	 * @param component  the component that shall hold the action
	 * @param action     the action to add
	 * @param keyStroke  the key stroke that initiates the action
	 */
	public static void addAction(
            JComponent component, 
            Action action,
			KeyStroke keyStroke) {
        addAction(component, action, keyStroke, JComponent.WHEN_FOCUSED);
	}
    

    /**
     * Adds the given action to the input map of the given component 
     * for the given key stroke. The key used to register the action 
     * is the Action object itself.
     * 
     * @param component  the component that shall hold the action
     * @param action     the action to add
     * @param keyStroke  the key stroke that initiates the action
     * @param condition one of JComponent.WHEN_IN_FOCUSED_WINDOW, 
     *     WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
     */
    public static void addAction(
            JComponent component, 
            Action action,
            KeyStroke keyStroke,
            int condition) {
        Object command = action;
        component.getInputMap(condition).put(keyStroke, command);
        component.getActionMap().put(command, action);
    }
    

	/**
	 * Adds the given action to the input map of the given component as a action
	 * on pressing the ENTER key.
	 * 
	 * @param component  the component that shall hold the action
	 * @param action     the action to add
	 */
	public static void addEnterAction(JComponent component, Action action) {
		addAction(component, 
                  action, 
                  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	}
	
	/**
	 * Adds the given action to the input map of the given component as a action
	 * on pressing the INSERT key.
	 * 
	 * @param component  the component that shall hold the action
	 * @param action     the action to add
	 */
	public static void addInsertAction(JComponent component, Action action) {
		addAction(component, 
                  action, 
                  KeyStroke.getKeyStroke("INSERT"));
	}
    
	/**
	 * Adds the given action to the input map of the given component as a action
	 * on pressing the DELETE key.
	 * 
	 * @param component  the component that shall hold the action
	 * @param action     the action to add
	 */
	public static void addDeleteAction(JComponent component, Action action) {
		addAction(component, 
                  action, 
                  KeyStroke.getKeyStroke("DELETE"));
	}
	
	/**
	 * Adds the given action to the input map of the given component as a action
	 * on pressing the F2 key.
	 * 
	 * @param component  the component that shall hold the action
	 * @param action     the action to add
	 */
	public static void addF2Action(JComponent component, Action action) {
		addAction(component, 
                  action, 
                  KeyStroke.getKeyStroke("F2"));
	}

    /**
     * Removes the given action from the input map of the given component 
     * for the given key stroke. The key used to register the action 
     * is the Action object itself.
     * 
     * @param component  the component that shall hold the action
     * @param action     the action to add
     * @param keyStroke  the key stroke that initiates the action
     * @param condition one of JComponent.WHEN_IN_FOCUSED_WINDOW, 
     *     WHEN_FOCUSED, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
     */
    public static void removeAction(
            JComponent component, 
            Action action,
            KeyStroke keyStroke,
            int condition) {
        Object actionMapKey = action;
        component.getInputMap(condition).put(keyStroke, actionMapKey);
        component.getActionMap().put(actionMapKey, action);
    }
    
    /**
     * Decora una tabla con las acciones mas comunes como sumar registros sumar los valores de una columna etc
     * 
     * @param table
     */
    public static void decorateActions(final JTable table){
		
		final Action contarAction=new AbstractAction("contar"){
			public void actionPerformed(ActionEvent e) {
				//System.out.println("Contando registros");
				int rows=table.getRowCount();
				JOptionPane.showMessageDialog(table,"Numero de registros: "+rows);
			}			
		};		
		final Action sumarAction=new AbstractAction("sumar"){
			public void actionPerformed(ActionEvent e) {
				//System.out.println("Sumando registros");
				int selectedRow=table.getSelectedRow();
				int col=table.getSelectedColumn();
				if(col==-1 || selectedRow==-1)
					return;
				Object selected=table.getValueAt(selectedRow, col);
				if(selected instanceof Number){
					String columna=table.getColumnName(col);
					BigDecimal val=BigDecimal.ZERO;
					for(int row=0;row<table.getRowCount();row++){
						try{							
							Number v=(Number)table.getValueAt(row,col);
							if(v==null) continue;
							val=val.add(BigDecimal.valueOf(v.doubleValue()));
						}catch (Exception ex) {
							MessageUtils.showError("Error sumando columna "+columna+ "En Fila: "+row, ex);
							continue;
						}
					}
					String msg=MessageFormat.format("Total de columna {0} : {1}",columna,NumberFormat.getInstance().format(val.doubleValue()));
					JOptionPane.showMessageDialog(table,msg);
				}
				
				if(selected instanceof CantidadMonetaria){
					String columna=table.getColumnName(col);
					BigDecimal val=BigDecimal.ZERO;
					for(int row=0;row<table.getRowCount();row++){
						try{							
							Number v=((CantidadMonetaria)table.getValueAt(row,col)).amount();
							if(v==null) continue;
							val=val.add(BigDecimal.valueOf(v.doubleValue()));
						}catch (Exception ex) {
							MessageUtils.showError("Error sumando columna "+columna+ "En Fila: "+row, ex);
							continue;
						}
					}
					String msg=MessageFormat.format("Total de columna {0} : {1}",columna,NumberFormat.getInstance().format(val.doubleValue()));
					JOptionPane.showMessageDialog(table,msg);
				}
				
			}
		};
		addAction(table, contarAction, KeyStroke.getKeyStroke(KeyEvent.VK_ADD,KeyEvent.CTRL_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
		addAction(table, sumarAction, KeyStroke.getKeyStroke(KeyEvent.VK_ADD,0),JComponent.WHEN_IN_FOCUSED_WINDOW);		
	}
    
    public static JXTable getStandardTable(){		
		JXTable grid=new SXTable();
    	grid.setColumnControlVisible(true);
		grid.setHorizontalScrollEnabled(true);		
		grid.setRolloverEnabled(true);
		Highlighter alternate=HighlighterFactory.createAlternateStriping();//new HighlighterPipeline();		
		grid.setHighlighters(new Highlighter[]{alternate});
		grid.setRolloverEnabled(true);
		//grid.setDefaultRenderer(Renderers.)
		grid.setSortable(false);
		grid.getSelectionMapper().setEnabled(false);
		grid.getTableHeader().setDefaultRenderer(new JTableHeader().getDefaultRenderer());
		decorateForPrinting(grid);
		return grid;
    }
    
    public static final String TITULO_PARA_IMPRIMIR_KEY="Titulo para imprimir";
    
    public static void decorateForPrinting(final JXTable table){
    	Action printAction=new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				try {
					String titulo=(String)table.getClientProperty(TITULO_PARA_IMPRIMIR_KEY);
					MessageFormat header=new MessageFormat(titulo+"Página {0}");
					MessageFormat footer=new MessageFormat(" -- {0} --");
					table.print(PrintMode.FIT_WIDTH,header,footer);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		};
    	addAction(table, printAction, KeyStroke.getKeyStroke(KeyEvent.VK_P,KeyEvent.CTRL_MASK),JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    public static JComponent buildTextFilterPanel(final JTextField filterTextField){
		return buildInputFilterPanel(filterTextField, "Filtrar:");
	}
    
    public static JComponent buildInputFilterPanel(final JTextField filterTextField,final String inputLabel){
		FormLayout layout=new FormLayout(
				"3dlu,l:50dlu,max(70dlu;p)",
				"p");
		CellConstraints cc=new CellConstraints();
		PanelBuilder builder=new PanelBuilder(layout);
		//builder.setDefaultDialogBorder();
		JLabel l=DefaultComponentFactory.getInstance().createTitle(inputLabel);
		builder.add(l,cc.xy(2,1));
		builder.add(filterTextField,cc.xy(3,1));
		return builder.getPanel();
	}
    
    /**
     * Decura una etiqueta para indicar con un icono pequeño que tiene acceso  lookup F2
     * @param l
     */
    public static void decorateF2Label(JLabel l){
		l.setHorizontalTextPosition(SwingConstants.LEADING);
		l.setIconTextGap(8);
		l.setHorizontalAlignment(SwingConstants.LEFT);
		l.setIcon(ResourcesUtils.getIconFromResource("images2/database_refresh.png"));
	}
    
    /**
     * Intala el JPopupMenu en la tabla
     *  
     * @param table
     * @param mnu
     */
    public static void decorateForPopup(final JTable table,final JPopupMenu mnu){
    	
    	table.addMouseListener(new MouseAdapter(){			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON3){
					System.out.println("Popup");
					mnu.show(table, e.getX(), e.getY());
				}
			}
		});
    }
    
    /**
     *Decora un grid para que en el dounle click se ejecuta la accion indicada
     * 
     * @param table
     * @param target
     */
    public static void decorateForDobleClick(final JTable table,final Action target){
    	table.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2)
					target.actionPerformed(null);
			}
    		
    	});
    }
    
    
    public static JXTaskPane createStandarJXTaskPane(){
    	SXTaskPane tp=new SXTaskPane();
    	//tp.setDefaultIcon(CommandUtils.getIconFromResource("images/misc/arrowGreen.gif"));
    	tp.setDefaultIcon(CommandUtils.getIconFromResource("images2/bullet_go.png"));
    	
    	return tp;
    }
    
    public static JPopupMenu createPopupMenu(final String title,final Action... actions){
    	PopupMenuBuilder builder=new PopupMenuBuilder("Opciones");    	
    	for(Action a:actions){
    		builder.add(a);
    	}
		return builder.getPopupMenu();
    }
    
    public static HeaderPanel getBigHeader(final String title,final String desc){
    	return new CustomHeader(title,desc);
    	
    }
    
    public static void decorateSpecialFocusTraversal(Container c){
    	
    	final Set fk=getNextFoculsKeys();
    	fk.addAll(c.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
    	c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, fk);
    	
    	final Set bk=getPreviousFocusKeys();
    	bk.addAll(c.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
    	c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, bk);
    	
    }
    
    public static void decorateTabFocusTraversal(Container c){
    	final Set<KeyStroke> fk=new HashSet<KeyStroke>();
    	fk.add(KeyStroke.getKeyStroke("TAB"));
    	c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, fk);
    }
    
    public static Set<KeyStroke> getNextFoculsKeys(){
    	final Set<KeyStroke> fk=new HashSet<KeyStroke>();
    	fk.add(KeyStroke.getKeyStroke("TAB"));
		fk.add(KeyStroke.getKeyStroke("ENTER"));
		//fk.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0));
		//fk.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0));
		return fk;
    }
    
    public static Set<KeyStroke> getPreviousFocusKeys(){    	
		final Set<KeyStroke> bk=new HashSet<KeyStroke>();
		//bk.add(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0));
		//bk.add(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,0));
		return bk;
    }
    
    public static Set<KeyStroke> getNextFoculsKeys(final KeyStroke...excludeStrokes){
    	final Set<KeyStroke> fk=new HashSet<KeyStroke>();
		fk.add(KeyStroke.getKeyStroke("ENTER"));
		fk.add(KeyStroke.getKeyStroke("TAB"));
		//fk.add(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0));
		//fk.add(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,0));
		
		Collection res=CollectionUtils.subtract(fk, Arrays.asList(excludeStrokes));
		return new HashSet<KeyStroke>(res);
		/*
		Set ok=SetUtils.predicatedSet(fk, new Predicate(){
			public boolean evaluate(Object object) {
				return !ArrayUtils.contains(excludeStrokes, object);
			}
			
		});
		return ok;
		*/
    }
    
    public static JLabel createBoldLabel(float increment,int alignment){
    	JLabel label=new JLabel();
		label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+increment));
		//label.setHorizontalAlignment(JLabel.LEFT);
		if(alignment!=0)
			label.setHorizontalAlignment(JLabel.LEFT);
		return label;
    }
    
    public static JLabel createTitleLabel(float increment){
    	JLabel l=new JLabel();
    	l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D()+increment));
		l.setForeground(UIManager.getColor("TitledBorder.titleColor"));
		return l;
    }
    
    public static void toTitleLabel(JLabel l,float increment){
    	l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D()+increment));
		l.setForeground(UIManager.getColor("TitledBorder.titleColor"));
    }
    
    
    public static class SXTaskPane extends JXTaskPane{
    	
    	private Icon defaultIcon;
    	
    	@Override
		public Component add(Action action) {
			if(action.getValue(Action.SMALL_ICON)==null)
				action.putValue(Action.SMALL_ICON, getDefaultIcon());
			Component c=super.add(action);				
			return c;
		}
    	
    	
		
		public Icon getDefaultIcon() {
			return defaultIcon;
		}
		public void setDefaultIcon(Icon defaultIcon) {
			this.defaultIcon = defaultIcon;
		}


    }
    
    public static class CustomHeader extends HeaderPanel{
    	
    	private float descSize=12f;
    	private int titleSize=7;
    	
    	public CustomHeader(String title, String description, Icon icon, ConstantSize minimumWidth, ConstantSize minimumHeight) {
			super(title, description, icon, minimumWidth, minimumHeight);
		}

		public CustomHeader(String title, String description, Icon icon, JComponent backgroundComponent, ConstantSize minimumWidth, ConstantSize minimumHeight) {
			super(title, description, icon, backgroundComponent, minimumWidth,
					minimumHeight);			
		}

		public CustomHeader(String title, String description, Icon icon) {
			super(title, description, icon);			
		}

		public CustomHeader(String title, String description) {
			super(title, description);
		}

		
		protected JTextComponent createDescriptionArea() {
			JTextArea area = UIFactory.createWrappedMultilineLabel("");
			area.setLineWrap(false);
			Font f = area.getFont();
			area.setFont(f.deriveFont(Font.BOLD, 12f));
			area.setForeground(Color.DARK_GRAY.brighter());			
			area.setRows(2);
			return area;
		}
		
		protected JLabel createTitleLabel() {
			return UIFactory.createBoldLabel("", 7, UIManager
					.getColor("TitledBorder.titleColor"));
		}

		public float getDescSize() {
			return descSize;
		}

		public void setDescSize(float descSize) {
			this.descSize = descSize;
		}

		public int getTitleSize() {
			return titleSize;
		}

		public void setTitleSize(int titleSize) {
			this.titleSize = titleSize;
		}
		
		
    	
    }
    
    public static void enableComponents(final JPanel container,boolean val){
    	for(Component c:container.getComponents()){
    		if(c instanceof JPanel)
    			disableComponents((JPanel)c);
    		else if(c instanceof JTextField){    			
    			JComponent jc=(JComponent)c;
    			jc.setEnabled(val);	
    		}else if(c instanceof JComboBox || c instanceof JCheckBox ){    			
    			JComponent jc=(JComponent)c;
    			jc.setEnabled(val);	
    		}
    	}
    }
    
    
    /**
     * Deshabilita todos los componentes del container
     * 
     * @param c
     */
    public static void disableComponents(final JPanel container){
    	for(Component c:container.getComponents()){
    		if(c instanceof JPanel)
    			disableComponents((JPanel)c);
    		else if(c instanceof JTextField){
    			
    			JComponent jc=(JComponent)c;
    			jc.setEnabled(false);	
    		}else if(c instanceof JComboBox || c instanceof JCheckBox ){    			
    			JComponent jc=(JComponent)c;
    			jc.setEnabled(false);	
    		}
    	}
    }
    
    public static JPanel createLookupPanel(final JTextField tf,final JButton btn){
    	final FormLayout layout=new FormLayout(
    			"p,2dlu,20dlu","c:15dlu");
    	final PanelBuilder builder=new PanelBuilder(layout);
    	final CellConstraints cc=new CellConstraints();
    	//btn.setBorder(null);
    	builder.add(tf,cc.xy(1,1));
    	builder.add(btn,cc.xy(3, 1));
    	return builder.getPanel();
    }
    
    /**
     * Salva en preferencias las dimensiones de las columnas de la
     * tabla y si estan ocultas o no
     * 
     * @param grid
     */
    public static void persistirColumnas(JXTable grid){
    	grid.getColumnModel();
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
        panel.setPreferredSize(new Dimension(220, 300));
        panel.add(scrollPane);

        return panel;
    }
    
    public static void printAllActionMap(JComponent c){
    	ActionMap map=c.getActionMap();
		for(Object key:map.allKeys()){
			Action  a=map.get(key);
			System.out.println(key+","+a.getValue(Action.NAME));
		}
		
    }
    
    /*private static KeyStroke lokkupKeyForAction(JComponent c,Object key){
    	//c.getInputMap().get(keyStroke)
    }*/
    
    public static void printAllInputMap(JComponent c){
    	InputMap map=c.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		for(KeyStroke ks:map.allKeys()){
			Object  a=map.get(ks);
			System.out.println(a+","+ks.toString());
		}
		
    }
}
