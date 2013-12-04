package com.luxsoft.siipap.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.Assert;

import com.luxsoft.siipap.Constants;
import com.luxsoft.siipap.model.Periodo;


/**
 * Date Utility Class used to convert Strings to Dates and Timestamps
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 *  Modified by <a href="mailto:dan@getrolling.com">Dan Kibler </a> 
 *  to correct time pattern. Minutes should be mm not MM (MM is month). 
 */
public class DateUtil {
    private static Log log = LogFactory.getLog(DateUtil.class);
    private static final String TIME_PATTERN = "HH:mm";

    /**
     * Checkstyle rule: utility classes should not have public constructor
     */
    private DateUtil() {
    }

    /**
     * Return default datePattern (MM/dd/yyyy)
     * @return a string representing the date pattern on the UI
     */
    public static String getDatePattern() {
        Locale locale = LocaleContextHolder.getLocale();
        String defaultDatePattern;
        try {
            defaultDatePattern = ResourceBundle.getBundle(Constants.BUNDLE_KEY, locale)
                .getString("date.format");
        } catch (MissingResourceException mse) {
            defaultDatePattern = "dd/MM/yyyy";
        }

        return defaultDatePattern;
    }

    public static String getDateTimePattern() {
        return DateUtil.getDatePattern() + " HH:mm:ss.S";
    }

    /**
     * This method attempts to convert an Oracle-formatted date
     * in the form dd-MMM-yyyy to mm/dd/yyyy.
     *
     * @param aDate date from database as a string
     * @return formatted string for the ui
     */
    public static String getDate(Date aDate) {
        SimpleDateFormat df;
        String returnValue = "";

        if (aDate != null) {
            df = new SimpleDateFormat(getDatePattern());
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * This method generates a string representation of a date/time
     * in the format you specify on input
     *
     * @param aMask the date pattern the string is in
     * @param strDate a string representation of a date
     * @return a converted Date object
     * @see java.text.SimpleDateFormat
     * @throws ParseException when String doesn't match the expected format
     */
    public static Date convertStringToDate(String aMask, String strDate)
      throws ParseException {
        SimpleDateFormat df;
        Date date;
        df = new SimpleDateFormat(aMask);

        if (log.isDebugEnabled()) {
            log.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");
        }

        try {
            date = df.parse(strDate);
        } catch (ParseException pe) {
            //log.error("ParseException: " + pe);
            throw new ParseException(pe.getMessage(), pe.getErrorOffset());
        }

        return (date);
    }

    /**
     * This method returns the current date time in the format:
     * MM/dd/yyyy HH:MM a
     *
     * @param theTime the current time
     * @return the current date/time
     */
    public static String getTimeNow(Date theTime) {
        return getDateTime(TIME_PATTERN, theTime);
    }

    /**
     * This method returns the current date in the format: MM/dd/yyyy
     * 
     * @return the current date
     * @throws ParseException when String doesn't match the expected format
     */
    public static Calendar getToday() throws ParseException {
        Date today = new Date();
        SimpleDateFormat df = new SimpleDateFormat(getDatePattern());

        // This seems like quite a hack (date -> string -> date),
        // but it works ;-)
        String todayAsString = df.format(today);
        Calendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(todayAsString));

        return cal;
    }

    /**
     * This method generates a string representation of a date's date/time
     * in the format you specify on input
     *
     * @param aMask the date pattern the string is in
     * @param aDate a date object
     * @return a formatted string representation of the date
     * 
     * @see java.text.SimpleDateFormat
     */
    public static String getDateTime(String aMask, Date aDate) {
        SimpleDateFormat df = null;
        String returnValue = "";

        if (aDate == null) {
            log.error("aDate is null!");
        } else {
            df = new SimpleDateFormat(aMask);
            returnValue = df.format(aDate);
        }

        return (returnValue);
    }

    /**
     * This method generates a string representation of a date based
     * on the System Property 'dateFormat'
     * in the format you specify on input
     * 
     * @param aDate A date to convert
     * @return a string representation of the date
     */
    public static String convertDateToString(Date aDate) {
        return getDateTime(getDatePattern(), aDate);
    }

    /**
     * This method converts a String to a date using the datePattern
     * 
     * @param strDate the date to convert (in format dd/MM/yyyy)
     * @return a date object
     * @throws ParseException when String doesn't match the expected format
     */
    public static Date convertStringToDate(String strDate)
      throws ParseException {
        Date aDate = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("converting date with pattern: " + getDatePattern());
            }

            aDate = convertStringToDate(getDatePattern(), strDate);
        } catch (ParseException pe) {
            log.error("Could not convert '" + strDate + "' to a date, throwing exception");
            pe.printStackTrace();
            throw new ParseException(pe.getMessage(),pe.getErrorOffset());
        }

        return aDate;
    }
    
    /**
     * This method converts a String to a date using the datePattern
     * 
     * @param strDate the date to convert (in format dd/MM/yyyy) * 
     * 
     * @return a date Object
     */
    public static Date toDate(String strDate){
    	try {
            if (log.isDebugEnabled()) {
                log.debug("converting date with pattern: " + getDatePattern());
            }

            return convertStringToDate(getDatePattern(), strDate);
        } catch (ParseException pe) {
            log.error("Could not convert '" + strDate + "' to a date, throwing exception");
            pe.printStackTrace();
            throw new RuntimeException(pe);
        }
    }
    
    
    
    public static int toYear(final Date fecha){
    	Calendar c=Calendar.getInstance();
    	c.setTime(fecha);
    	return c.get(Calendar.YEAR);
    }
    
    public static int toMes(final Date fecha){
    	Calendar c=Calendar.getInstance();
    	c.setTime(fecha);
    	return c.get(Calendar.MONTH)+1;
    }
    
    /**
     * <p>Truncate this date, leaving the field specified as the most
     * significant field.</p>
     *
     * <p>For example, if you had the datetime of 28 Mar 2002
     * 13:45:01.231, if you passed with HOUR, it would return 28 Mar
     * 2002 13:00:00.000.  If this was passed with MONTH, it would
     * return 1 Mar 2002 0:00:00.000.</p>
     * 
     * @param date  the date to work with
     * @param field  the field from <code>Calendar</code>
     *  or <code>SEMI_MONTH</code>
     * @return the rounded date
     * @throws IllegalArgumentException if the date is <code>null</code>
     * @throws ArithmeticException if the year is over 280 million
     */
    public static Date truncate(Date date, int field) {
        return DateUtils.truncate(date, field);
    }
    
    
    
    /**
	 * Encuentra la fecha mas proxima para el dia de la semana posterior a la fecha
	 * especificada, El rango de los dias es 1 al 7, el lunes es 1 y el domingo es 7
	 * Si el parametro inclusive es verdadero la fecha especificada cuenta para el calculo, es
	 * decir esta fecha puede ser la misma que la calculada, en caso de que el parametro sea
	 * falso la fecha regresada nunca sera igual a la fecha solicitada
	 * 
	 * @param fecha
	 * @param diaDeLaSemana
	 * @return
	 */
	public static final Date calcularFechaMasProxima(final Date fecha,final int diaDeLaSemana,final boolean inclusive){		
		Assert.isTrue(diaDeLaSemana>0 && diaDeLaSemana<=7,"El rando  es de 1 a 7, el valor registrado es:"+diaDeLaSemana);
		 Map<Integer, Integer> map=new HashMap<Integer, Integer>();
		 map.put(Calendar.MONDAY,1 );
		 map.put(Calendar.TUESDAY,2 );
		 map.put(Calendar.WEDNESDAY,3 );
		 map.put(Calendar.THURSDAY,4);
		 map.put(Calendar.FRIDAY,5 );
		 map.put(Calendar.SATURDAY,6 );
		 map.put(Calendar.SUNDAY,7 );
		 //Obtener el dia que corresponde al 5
		 if(diaDeLaSemana==7){
			 final Calendar c=Calendar.getInstance();
			 c.setTime(fecha);
			 c.add(Calendar.DATE, 1);
			 return c.getTime();
		 }
		 Calendar c=Calendar.getInstance();
		 c.setTime(fecha);
		 
		 Date revision=null;
		 
		 while(true){			 
			 revision=c.getTime();
			 int diaCalendario=c.get(Calendar.DAY_OF_WEEK);
			 int diaMapeo=map.get(diaCalendario);
			 if(inclusive){
				 if(diaMapeo==diaDeLaSemana  )				 
					 break;				 
			 }else{
				 if(diaMapeo==diaDeLaSemana && (!fecha.equals(revision)) )				 
					 break;
				 				 
			 }
			 c.add(Calendar.DATE, 1);
		 }
		 return revision;
	}
	
	public static List<Periodo> segmentar(final Periodo p,int dias){
		List<Periodo> periodos=new ArrayList<Periodo>();
		Date start=truncate(p.getFechaInicial(),Calendar.DATE);
		final Date end=truncate(p.getFechaFinal(),Calendar.DATE);		
		Date current=start;		
		int index=0;
		
		while(current.compareTo(end)<=0){
			
			if(++index%dias==0){				
				Periodo per=new Periodo(start,DateUtils.addDays(start, dias-1));
				start=DateUtils.addDays(current,1);
				periodos.add(per);
				//System.out.println("Corte: "+per);
			}
			current=DateUtils.addDays(current, 1);			
		}if(start.compareTo(end)<=0){
			Periodo par=new Periodo(start,end);
			periodos.add(par);
			//System.out.println("Ultimo periodo parcial: "+par);
		}		
		return periodos;
	}
	
	public static boolean isSameMonth(final Date date1,final Date date2){
		if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameMonth(cal1, cal2);
	}
	
	
	public static boolean isSameYear(final Date date1,final Date date2){
		if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameYear(cal1, cal2);
	}
	
    
    /**
     * <p>Checks if two calendar objects are on the same month ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     * 
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     * @since 2.1
     */
    public static boolean isSameMonth(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH));
    }
    
    /**
     * <p>Checks if two calendar objects are on the same month ignoring time.</p>
     *
     * <p>28 Mar 2002 13:45 and 28 Mar 2002 06:01 would return true.
     * 28 Mar 2002 13:45 and 12 Mar 2002 13:45 would return false.
     * </p>
     * 
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     * @since 2.1
     */
    public static boolean isSameYear(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) );
    }
    
    private static Calendar CALENDAR = Calendar.getInstance();
    
    
    /**
     * Returns the number of days difference between <code>t1</code> and
     * <code>t2</code>.
     *
     * @param t1 Time 1
     * @param t2 Time 2
     * @param checkOverflow indicates whether to check for overflow
     * @return Number of days between <code>start</code> and <code>end</code>
     */
    public static int getDaysDiff(long t1, long t2, boolean checkOverflow) {
        if (t1 > t2) {
            long tmp = t1;
            t1 = t2;
            t2 = tmp;
        }
        Calendar calendar = CALENDAR;
        synchronized(calendar) {
            calendar.setTimeInMillis(t1);
            int delta = 0;
            while (calendar.getTimeInMillis() < t2) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                delta++;
            }
            if (checkOverflow && (calendar.getTimeInMillis() > t2)) {
                delta--;
            }
            return delta;
        }
    }

   /**
     * Returns the number of days difference between <code>t1</code> and
     * <code>t2</code>.
     *
     * @param t1 Time 1
     * @param t2 Time 2
     * @return Number of days between <code>start</code> and <code>end</code>
     */
      public static int getDaysDiff(long t1, long t2) {
       return  getDaysDiff(t1, t2, true);
    }
      
      public static int getDaysDiff(Date t1, Date t2) {
          return  getDaysDiff(t1.getTime(), t2.getTime(), true);
       }
      
      /**
       * Returns a new Date with the hours, milliseconds, seconds and minutes
       * set to 0.
       *
       * @param date Date used in calculating start of day
       * @return Start of <code>date</code>
       */
      public static Date startOfDay(Date date) {
          Calendar calendar = CALENDAR;
          synchronized(calendar) {
              calendar.setTime(date);
              calendar.set(Calendar.HOUR_OF_DAY, 0);
              calendar.set(Calendar.MILLISECOND, 0);
              calendar.set(Calendar.SECOND, 0);
              calendar.set(Calendar.MINUTE, 0);
              return calendar.getTime();
          }
      }
      
      /**
       * Returns the last millisecond of the specified date.
       *
       * @param date Date to calculate end of day from
       * @return Last millisecond of <code>date</code>
       */
      public static Date endOfDay(Date date) {
          Calendar calendar = CALENDAR;
          synchronized(calendar) {
              calendar.setTime(date);
              calendar.set(Calendar.HOUR_OF_DAY, 23);
              calendar.set(Calendar.MILLISECOND, 999);
              calendar.set(Calendar.SECOND, 59);
              calendar.set(Calendar.MINUTE, 59);
              return calendar.getTime();
          }
      }
      
      /**
       * Returns the day of the week.
       *
       * @param date date
       * @return day of week.
       */
      public static int getDayOfWeek(long date) {
         Calendar calendar = CALENDAR;
          synchronized(calendar) {
              calendar.setTimeInMillis(date);
              return (calendar.get(Calendar.DAY_OF_WEEK));
          }
      }
	
	
	
	/**
	 * Regresa un String apropiado para el numero del mes
	 * 
	 * @param id
	 * @return
	 */
	public static String getMesAsString(int id){
		switch (id) {
		case 1:
			return "ENE";
		case 2:
			return "FEB";
		case 3:
			return "MAR";
		case 4:
			return "ABR";
		case 5:
			return "MAY";
		case 6:
			return "JUN";
		case 7:
			return "JUL";
		case 8:
			return "AGO";
		case 9:
			return "SEP";
		case 10:
			return "OCT";
		case 11:
			return "NOV";
		case 12:
			return "DIC";
		default:
			throw new RuntimeException("El id del mes debe ser de 1 a 12");
		}
	}
	
	public static final Date calcularDiaHabilPosterior(final Date fecha){
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		Date siguienteDia=DateUtils.addDays(fecha, 1);
		cal.setTime(siguienteDia);
		int diaDeLaSemana=cal.get(Calendar.DAY_OF_WEEK);
		if(diaDeLaSemana==Calendar.SUNDAY)
			siguienteDia=DateUtils.addDays(siguienteDia, 1);
		return siguienteDia;
	}
	public static final Date calcularDiaHabilAnterior(final Date fecha){
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		Date anterior=DateUtils.addDays(fecha, -1);
		cal.setTime(anterior);
		int diaDeLaSemana=cal.get(Calendar.DAY_OF_WEEK);
		if(diaDeLaSemana==Calendar.SUNDAY)
			anterior=DateUtils.addDays(anterior, -1);
		return anterior;
	}
	
	
	public static final Date calcularDiaHabilAnterior(final Date fecha,int dias){
		Calendar cal=Calendar.getInstance();
		cal.setTime(fecha);
		Date anterior=DateUtils.addDays(fecha, -dias);
		cal.setTime(anterior);
		int diaDeLaSemana=cal.get(Calendar.DAY_OF_WEEK);
		if(diaDeLaSemana==Calendar.SUNDAY)
			anterior=DateUtils.addDays(anterior, -1);
		return anterior;
	}
	
	public static void main(String[] args) {
		Date lunes=DateUtil.toDate("14/05/2013");
		System.out.println(calcularDiaHabilAnterior(lunes,2));
	}

	
}
