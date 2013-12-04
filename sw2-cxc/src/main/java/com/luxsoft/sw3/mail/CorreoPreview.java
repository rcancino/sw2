/**
 * 
 */
package com.luxsoft.sw3.mail;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.uif.component.UIFButton;

import com.luxsoft.siipap.model.core.Cliente;
import com.luxsoft.siipap.swing.controls.SXAbstractDialog;
import com.luxsoft.siipap.swing.utils.CommandUtils;
import com.luxsoft.siipap.swing.utils.MessageUtils;
import com.luxsoft.siipap.swing.utils.ResourcesUtils;

public class CorreoPreview extends SXAbstractDialog {
	
	private URL url;
	private String text;
	private final Cliente cliente;
	
	public CorreoPreview(final URL url,final Cliente c) {
		super("Recordatorio de pago");
		this.url=url;
		this.cliente=c;
	}
	
	public CorreoPreview(final String text,final Cliente c) {
		super("Recordatorio de pago");
		this.text=text;
		this.cliente=c;
		
	}

	JEditorPane editor;
	//final String urlPath;

	@Override
	protected JComponent buildContent() {
		final JPanel panel=new JPanel(new BorderLayout());
		editor=new JEditorPane();
		editor.setEditable(false);
		panel.add(new JScrollPane(editor),BorderLayout.CENTER);
		panel.add(buildButtonBarWithOKCancel(),BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(800,450));
		return panel;
	}

	@Override
	protected void setResizable() {
		setResizable(true);
	}

	@Override
	protected void onWindowOpened() {
		if(url!=null)
			System.out.println("Tratando de localizar: "+"templates/"+url.getPath());
		if(url!=null){
			try {					
				editor.setPage(url);					
			} catch (IOException e) {
				e.printStackTrace();
				editor.setText("No se localizo : "+url.getPath());
			}
		}else if(text!=null){
			editor.setContentType("text/html");
			editor.setText(text);
		}
		
	}
	
	protected JComponent buildButtonBarWithOKCancel() {
		JButton[] btns=new JButton[]{
				createPrintButton(),
				createSaveButton(),
				createOKButton(true),
	            createCancelButton(),
	            
	            };
		btns[3].setText("Cancelar");
        JPanel bar = ButtonBarFactory.buildRightAlignedBar(btns);		            
        bar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);		        
        return bar;
    }
	
	protected UIFButton createPrintButton() {
		final Action a=CommandUtils.createPrintAction(this, "print");
        return new UIFButton(a);
    }
	
	protected UIFButton createSaveButton() {
		final Action a=new AbstractAction("Salvar"){
			public void actionPerformed(ActionEvent e) {
				salvar();			
			}			
		};
		a.putValue(Action.SMALL_ICON, ResourcesUtils.getIconFromResource("images2/SAVE.PNG"));
        return new UIFButton(a);
    }
	
	private void salvar(){
		System.out.println("Salvando");
		JFileChooser chooser=new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos HTML ", "html", "htm");
		
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		final File ff=new File(chooser.getCurrentDirectory(),obtenerConsecutivo());
		chooser.setSelectedFile(ff);
		
		int res=chooser.showSaveDialog(this);
		if(res==JFileChooser.APPROVE_OPTION){
			File f=chooser.getSelectedFile();
			
			try {
				f.createNewFile();
				System.out.println("File: "+f.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	private String obtenerConsecutivo(){
		final Date hoy=new Date();
		DateFormat df=new SimpleDateFormat("dd_MM_yy_HH_mm");
		return cliente.getClave()+"_"+df.format(hoy)+".html";
	}
	
	public void print(){
		try {
			editor.print();
		} catch (PrinterException e) {
			e.printStackTrace();
			MessageUtils.showError("Error imprimiendo",e);
		}
	}
	
	
}