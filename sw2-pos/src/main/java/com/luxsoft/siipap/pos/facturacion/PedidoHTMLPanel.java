package com.luxsoft.siipap.pos.facturacion;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

import com.jgoodies.uif.component.UIFButton;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;

/**
 * Panel que muestra los datos relevantes de un pedido en formato HTML usando Velocity Template
 * 
 * @author pato
 *
 */
public class PedidoHTMLPanel extends JPanel implements HyperlinkListener{
	
	private JEditorPane editor;
	
	public PedidoHTMLPanel(){
		setLayout(new BorderLayout());
		editor=new JEditorPane();
		editor.setContentType("text/html");
		editor.setEditable(false);
		editor.addHyperlinkListener(this);
		JScrollPane sp=new JScrollPane(editor);
		//editor.setComponentPopupMenu(buildPopupMenu());
		add(sp,BorderLayout.CENTER);
	}
	
	
	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(EventType.ACTIVATED==e.getEventType()){
			System.out.println("Link typed: "+e.getDescription()+" Type:"+e.getEventType()+ "URL: "+e.getURL());
			System.out.println("Element: "+e.getURL().getRef());
		}
		
		
	}
	public static void showInDialog(){
		SXAbstractDialog dialog=new SXAbstractDialog("Test"){

			private PedidoHTMLPanel pedidoPanel;
			
			@Override
			protected JComponent buildContent() {				
				JPanel content=new JPanel(new BorderLayout());
				content.add(buildButtonBarWithOKCancelReset(),BorderLayout.SOUTH);
				pedidoPanel=new PedidoHTMLPanel();
				pedidoPanel.setPreferredSize(new Dimension(600,400));
				content.add(pedidoPanel,BorderLayout.CENTER);
				return content;
			}
			
			public void doReset(){
				pedidoPanel.editor.setText("<h1 style=\"font-size:10px; color:red\"> RELOADING...</h1> " +
						"<p><a id=\"465\" href=\"http://rcancino:dodgers@www.w3schools.com?idd=45\">This is a link</a> </p>");
			}
			
		};
		dialog.open();
	}
	
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				com.luxsoft.siipap.swing.utils.SWExtUIManager.setup();
				showInDialog();
				System.exit(0);
			}

		});
	}

	

}
