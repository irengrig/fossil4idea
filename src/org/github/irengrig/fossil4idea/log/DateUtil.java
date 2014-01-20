package org.github.irengrig.fossil4idea.log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Irina.Chernushina
 * Date: 2/24/13
 * Time: 5:25 PM
 */
public class DateUtil {
  private final static List<DateFormat> ourFormats = new ArrayList<DateFormat>();

  static {
    ourFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    ourFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'"));
    ourFormats.add(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US));
    ourFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z (EE, d MMM yyyy)", Locale.getDefault()));
    ourFormats.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss' 'ZZZZ' ('E', 'dd' 'MMM' 'yyyy')'"));
    ourFormats.add(new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss'Z'"));
    ourFormats.add(new SimpleDateFormat("EEE' 'MMM' 'dd' 'HH:mm:ss' 'yyyy"));
    ourFormats.add(new SimpleDateFormat("MM' 'dd'  'yyyy"));
    ourFormats.add(new SimpleDateFormat("MM' 'dd'  'HH:mm"));
    ourFormats.add(new SimpleDateFormat("MM' 'dd'  'HH:mm:ss"));
  }

  public static Date parseDate(final String date) {
    for (DateFormat format : ourFormats) {
      try {
        return format.parse(date);
      }
      catch (ParseException e) {
        continue;
      }
    }
    return new Date(0);
  }
}
