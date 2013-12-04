package com.luxsoft.siipap.swing;

import javax.swing.JPanel;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class DefaultApplicationWindowTest extends MockObjectTestCase{
	
	private DefaultApplicationWindow window;
	
	public void setUp(){
		window=new DefaultApplicationWindow();
	}

	public void testClose(){
		
		Mock mockPage=mock(Page.class);	
		
		mockPage.stubs().method("getContainer").will(returnValue(new JPanel()));
		
		// Lamado aoligado
		mockPage.expects(once()).method("close");
		window.setWindowPage((Page)mockPage.proxy());
		window.close();
		
	}

	public void testGetFrameWindow() {
		assertNotNull(window.getWindow());
	}

	

}
