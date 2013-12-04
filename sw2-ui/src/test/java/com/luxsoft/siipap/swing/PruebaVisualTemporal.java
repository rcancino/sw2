package com.luxsoft.siipap.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import com.jgoodies.looks.HeaderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.uif.util.ScreenUtils;
import com.luxsoft.siipap.swing.controls.StatusBar;
import com.luxsoft.siipap.swing.impl.InfoNodeTabbedPage;

/**
 * Pruebas visuales de componentes o funciones aisladas
 * 
 * @author Ruben Cancino
 *
 */
public class PruebaVisualTemporal extends JFrame {
	
	//private JTabbedPane tabPanel=new JTabbedPane();
	//private InfoNodePage page=new InfoNodePage();
	//private Page page=new TabbedPanePage();
	private Page page=new InfoNodeTabbedPage();
	protected Set<Action> actions=new HashSet<Action>();
		
	
	
	public PruebaVisualTemporal(){
		super("Pruebas visuales");
		configActions();
		start();
	}
	
	
	
	protected void configActions(){
		final Action showView=new AbstractAction("Show_View"){
			public void actionPerformed(ActionEvent e) {
				View v=new TestView();
				page.addView(v);
			}			
		};
		actions.add(showView);		
		
	}
	
	protected void start(){		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new Listener());
		setJMenuBar(buildMenu());
		setContentPane(buildContent());
		
		
		pack();
		ScreenUtils.locateOnScreenCenter(this);
		setVisible(true);
	}
	
	private JComponent buildContent(){
		JPanel p=new JPanel(new BorderLayout());
		
		JPanel top=new JPanel(new BorderLayout());
		//top.add(buildMenu(),BorderLayout.NORTH);
		top.add(buildToolbar(),BorderLayout.CENTER);
		
		p.add(top,BorderLayout.NORTH);
		p.add(page.getContainer(),BorderLayout.CENTER);
		StatusBar sbar=new StatusBar();
		//sbar.getStatusPanel().setMainLeftComponent(new JLabel("Applicacion de prueba general.."));
		p.add(sbar.getStatusPanel(),BorderLayout.SOUTH);
		p.setPreferredSize(new Dimension(600,500));
		return p;
		
	}
	
	
	
	private JMenuBar buildMenu(){
		JMenuBar bar=new JMenuBar();
		//bar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
		JMenu mnu=new JMenu("Vistas");
		for(Action a:actions){
			mnu.add(a);
		}
		bar.add(mnu);		
		return bar;
	}
	
	private JToolBar buildToolbar(){
		JToolBar bar=new JToolBar();
		bar.setFloatable(false);
		bar.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
		for(Action a:actions){
			bar.add(a);
		}
		return bar;
	}
	
	public static void setLook(){
		try {
			//UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
			//UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
			//UIManager.setLookAndFeel(new WindowsLookAndFeel());
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {		}
	}
	
	public static void main(String[] args) {
		setLook();		
		new PruebaVisualTemporal();
		
	}
	
	private class Listener extends WindowAdapter{

		@Override
		public void windowClosing(WindowEvent e) {
			page.close();
		}
		
	}
	
	 class TestView implements View{
		
		private final String id;
		
		public TestView(){			
			this.id="testView_";
			
		}
		
		public void close() {
			System.out.println("close en la ventana: "+getId());
		}
		public void focusGained() {
			System.out.println("Focusgained en la ventana: "+getId());
		}
		public JComponent getContent() {
			JTextArea area=new JTextArea("Test View");
			return area;
		}
		public String getId() {						
			return id;
		}
		public void open() {
			System.out.println("Abriendo la ventana: "+getId());
		}

		public VisualElement getVisualSupport() {			
			return null;
		}

		public void focusLost() {
			System.out.println("Focus lost en la ventana: "+getId());			
		}					
		
	}

}
