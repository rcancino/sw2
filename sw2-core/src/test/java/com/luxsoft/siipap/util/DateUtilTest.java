package com.luxsoft.siipap.util;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import com.luxsoft.siipap.model.Periodo;

public class DateUtilTest extends TestCase {
    //~ Instance fields ========================================================

    private final Log log = LogFactory.getLog(DateUtilTest.class);

    //~ Constructors ===========================================================

    public DateUtilTest(String name) {
        super(name);
    }

    public void testGetInternationalDatePattern() {
        LocaleContextHolder.setLocale(new Locale("nl"));
        assertEquals("dd-MMM-yyyy", DateUtil.getDatePattern());
       
        LocaleContextHolder.setLocale(Locale.FRANCE);
        assertEquals("dd/MM/yyyy", DateUtil.getDatePattern());
        
        LocaleContextHolder.setLocale(Locale.GERMANY);
        assertEquals("dd.MM.yyyy", DateUtil.getDatePattern());
        
        // non-existant bundle should default to default locale
        LocaleContextHolder.setLocale(new Locale("fi"));
        String fiPattern = DateUtil.getDatePattern();
        LocaleContextHolder.setLocale(Locale.getDefault());
        String defaultPattern = DateUtil.getDatePattern();
        
        assertEquals(defaultPattern, fiPattern);
    }

    public void testGetDate() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("db date to convert: " + new Date());
        }

        String date = DateUtil.getDate(new Date());

        if (log.isDebugEnabled()) {
            log.debug("converted ui date: " + date);
        }

        assertTrue(date != null);
    }
    
    public void testGetDateTime() {
        if (log.isDebugEnabled()) {
            log.debug("entered 'testGetDateTime' method");
        }
        String now = DateUtil.getTimeNow(new Date());
        assertTrue(now != null);
        log.debug(now);
    }
    
    public void testSegmentar(){
    	Periodo p=new Periodo("01/01/2008","31/01/2008");
    	List<Periodo> segmentos=DateUtil.segmentar(p,10);
    	assertEquals(4, segmentos.size());
    	System.out.println("OK Segmentos: "+segmentos.size());
    	//Probamos dos segmentos
    	
    	Periodo p1=segmentos.get(0);    	
    	assertEquals(DateUtil.toDate("01/01/2008"), p1.getFechaInicial());
    	assertEquals(DateUtil.toDate("10/01/2008"), p1.getFechaFinal());
    
    	Periodo p4=segmentos.get(3);    	
    	assertEquals(DateUtil.toDate("31/01/2008"), p4.getFechaInicial());
    	assertEquals(DateUtil.toDate("31/01/2008"), p4.getFechaFinal());
    	
    }
}
