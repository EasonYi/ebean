package io.ebean.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility String class that supports String manipulation functions.
 */
public class StringHelper {

  private static final Pattern SPLIT_NAMES = Pattern.compile("[\\s,;]+");

  private static final String[] EMPTY_STRING_ARRAY = new String[0];

  /**
   * Return true if the value is null or an empty string.
   */
  public static boolean isNull(String value) {
    return value == null || value.trim().isEmpty();
  }

  /**
   * Parses out a list of Name Value pairs that are delimited together. Will
   * always return a StringMap. If allNameValuePairs is null, or no name values
   * can be parsed out an empty StringMap is returned.
   *
   * @param source  the entire string to be parsed.
   * @param listDelimiter      (typically ';') the delimited between the list
   * @param nameValueSeparator (typically '=') the separator between the name and value
   */
  public static Map<String, String> delimitedToMap(String source, String listDelimiter, String nameValueSeparator) {
    Map<String, String> params = new HashMap<>();
    if (source == null || source.isEmpty()) {
      return params;
    }
    // trim off any leading listDelimiter...
    source = trimFront(source, listDelimiter);
    return delimitedToMap(params, source, listDelimiter, nameValueSeparator);
  }

  /**
   * Trims off recurring strings from the front of a string.
   *
   * @param source the source string
   * @param trim   the string to trim off the front
   */
  private static String trimFront(String source, String trim) {
    while (true) {
      if (source.indexOf(trim) == 0) {
        source = source.substring(trim.length());
      } else {
        return source;
      }
    }
  }

  /**
   * Recursively pulls out the key value pairs from a raw string.
   */
  private static Map<String, String> delimitedToMap(Map<String, String> map, String source, String listDelimiter, String nameValueSeparator) {
    int pos = 0;
    while (true) {
      if (pos >= source.length()) {
        return map;
      }
      int equalsPos = source.indexOf(nameValueSeparator, pos);
      int delimPos = source.indexOf(listDelimiter, pos);
      if (delimPos == -1) {
        delimPos = source.length();
      }
      if (equalsPos == -1) {
        return map;
      }
      if (delimPos == (equalsPos + 1)) {
        pos = delimPos + 1;
        continue;
      }
      if (equalsPos > delimPos) {
        // there is a key without a value?
        String key = source.substring(pos, delimPos);
        key = key.trim();
        if (!key.isEmpty()) {
          map.put(key, null);
        }
        pos = delimPos + 1;
        continue;
      }
      String key = source.substring(pos, equalsPos);
      String value = source.substring(equalsPos + 1, delimPos);
      map.put(key.trim(), value);
      pos = delimPos + 1;
    }
  }

  /**
   * This method takes a String and will replace all occurrences of the match
   * String with that of the replace String.
   *
   * @param source  the source string
   * @param match   the string used to find a match
   * @param replace the string used to replace match with
   * @return the source string after the search and replace
   */
  public static String replaceString(String source, String match, String replace) {
    if (source == null) {
      return null;
    }
    if (replace == null) {
      return source;
    }
    if (match == null) {
      throw new NullPointerException("match is null?");
    }
    if (match.equals(replace)) {
      return source;
    }
    return replaceString(source, match, replace, 30, 0, source.length());
  }

  /**
   * Additionally specify the additionalSize to add to the buffer. This will
   * make the buffer bigger so that it doesn't have to grow when replacement
   * occurs.
   */
  public static String replaceString(String source, String match, String replace,
                                     int additionalSize, int startPos, int endPos) {

    if (source == null) {
      return null;
    }

    char match0 = match.charAt(0);

    int matchLength = match.length();

    if (matchLength == 1 && replace.length() == 1) {
      char replace0 = replace.charAt(0);
      return source.replace(match0, replace0);
    }
    if (matchLength >= replace.length()) {
      additionalSize = 0;
    }

    int sourceLength = source.length();
    int lastMatch = endPos - matchLength;

    StringBuilder sb = new StringBuilder(sourceLength + additionalSize);

    if (startPos > 0) {
      sb.append(source.substring(0, startPos));
    }

    char sourceChar;
    boolean isMatch;
    int sourceMatchPos;

    for (int i = startPos; i < sourceLength; i++) {
      sourceChar = source.charAt(i);
      if (i > lastMatch || sourceChar != match0) {
        sb.append(sourceChar);

      } else {
        // check to see if this is a match
        isMatch = true;
        sourceMatchPos = i;

        // check each following character...
        for (int j = 1; j < matchLength; j++) {
          sourceMatchPos++;
          if (source.charAt(sourceMatchPos) != match.charAt(j)) {
            isMatch = false;
            break;
          }
        }
        if (isMatch) {
          i = i + matchLength - 1;
          sb.append(replace);
        } else {
          // was not a match
          sb.append(sourceChar);
        }
      }
    }

    return sb.toString();
  }

  /**
   * Return new line and carriage return with space.
   */
  public static String removeNewLines(String source) {
    source = source.replace('\n', ' ');
    return source.replace('\r', ' ');
  }

  /**
   * Splits at any whitespace "," or ";" and trims the result.
   * It does not return empty entries.
   */
  public static String[] splitNames(String names) {
    if (names == null || names.isEmpty()) {
      return EMPTY_STRING_ARRAY;
    }
    String[] result = SPLIT_NAMES.split(names);
    if (result.length == 0) {
      return EMPTY_STRING_ARRAY;
    }
    if ("".equals(result[0])) { //  input string starts with whitespace
      if (result.length == 1) { // input string contains only whitespace
        return EMPTY_STRING_ARRAY;
      } else {
        String[] ret = new String[result.length-1]; // remove first entry
        System.arraycopy(result, 1, ret, 0, ret.length);
        return ret;
      }
    } else {
      return result;
    }
  }
}
