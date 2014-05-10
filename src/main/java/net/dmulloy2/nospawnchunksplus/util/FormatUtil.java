package net.dmulloy2.nospawnchunksplus.util;

import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;

/**
 * Util used for general formatting
 *
 * @author dmulloy2
 */

public class FormatUtil
{
	private FormatUtil() { }

	/**
	 * Formats a given string with its objects.
	 *
	 * @param format
	 *        - Base string
	 * @param objects
	 *        - Objects to format in
	 * @return Formatted string
	 * @see {@link MessageFormat#format(String, Object...)}
	 */
	public static String format(String format, Object... objects)
	{
		try
		{
			format = MessageFormat.format(format, objects);
		} catch (Exception e) { }

		return ChatColor.translateAlternateColorCodes('&', format);
	}

	/**
	 * Returns a user-friendly representation of a given Object. This is mostly
	 * used for {@link Enum} constants.
	 * <p>
	 * If the object or any of its superclasses (minus Object) do not implement
	 * a toString() method, the object's simple name will be returned.
	 *
	 * @param o
	 *        - Object to get the user-friendly representation of
	 * @return A user-friendly representation of the given Object.
	 */
	public static String getFriendlyName(Object o)
	{
		try
		{
			// Clever little method to check if the method isn't declared by a
			// class other than Object.
			Method method = o.getClass().getMethod("toString", (Class<?>[]) null);
			if (method.getDeclaringClass().getSuperclass() == null)
			{
				return o.getClass().getSimpleName();
			}
		} catch (Exception e) { }

		return getFriendlyName(o.toString());
	}

	/**
	 * Returns a user-friendly version of a given String.
	 *
	 * @param string
	 *        - String to get the user-friendly version of
	 * @return A user-friendly version of the given String.
	 */
	public static String getFriendlyName(String string)
	{
		String ret = string.toLowerCase();
		ret = ret.replaceAll("_", " ");
		return WordUtils.capitalize(ret);
	}

	/**
	 * Returns the proper article of a given string
	 *
	 * @param string
	 *        - String to get the article for
	 * @return The proper article of a given string
	 */
	public static String getArticle(String string)
	{
		string = string.toLowerCase();
		if (string.startsWith("a") || string.startsWith("e") || string.startsWith("i") || string.startsWith("o") || string.startsWith("u"))
			return "an";

		return "a";
	}

	/**
	 * Returns the proper plural of a given string
	 *
	 * @param string
	 *        - String to get the plural for
	 * @return The proper plural of a given string
	 */
	public static String getPlural(String string, int amount)
	{
		amount = Math.abs(amount);
		if (amount == 0 || amount > 1)
		{
			if (! string.toLowerCase().endsWith("s"))
				return string + "s";
		}

		return string;
	}

	/**
	 * Joins together multiple given strings with the given glue.
	 *
	 * @param glue
	 *        - String to join the args together with
	 * @param args
	 *        - Strings to join together
	 * @return Multiple strings joined together with the given glue.
	 */
	public static String join(String glue, String... args)
	{
		StringBuilder ret = new StringBuilder();
		for (String arg : args)
		{
			ret.append(arg + glue);
		}

		if (ret.lastIndexOf(glue) >= 0)
		{
			ret.delete(ret.lastIndexOf(glue), ret.length());
		}

		return ret.toString();
	}

	/**
	 * Returns the given {@link File}'s name with the extension omitted.
	 *
	 * @param file
	 *        - {@link File}
	 * @param extension
	 *        - File extension
	 * @return The file's name with the extension omitted
	 */
	public static String trimFileExtension(File file, String extension)
	{
		int index = file.getName().lastIndexOf(extension);
		return index > 0 ? file.getName().substring(0, index) : file.getName();
	}
}