/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.nospawnchunks.util;

import java.text.MessageFormat;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

/**
 * Util used for general formatting.
 *
 * @author dmulloy2
 */

public class FormatUtil
{
	private FormatUtil() { }

	/**
	 * Formats a given string with its objects.
	 *
	 * @param format Base string
	 * @param objects Objects to format in
	 * @return Formatted string
	 * @see {@link MessageFormat#format(String, Object...)}
	 */
	public static String format(String format, Object... objects)
	{
		Validate.notNull(format, "format cannot be null!");

		try
		{
			format = MessageFormat.format(format, objects);
		} catch (Throwable ex) { }

		return replaceColors(format);
	}

	private static final String[] rainbowColors = new String[]
	{
			"c", "6", "e", "a", "b", "d", "5"
	};

	/**
	 * Replaces color codes in a given string. Includes rainbow.
	 *
	 * @param message Message to replace color codes in
	 * @return Formatted chat message
	 */
	public static String replaceColors(String message)
	{
		Validate.notNull(message, "message cannot be null!");
		message = message.replaceAll("(&([zZ]))", "&z");
		if (message.contains("&z"))
		{
			StringBuilder ret = new StringBuilder();
			String[] ss = message.split("&z");
			ret.append(ss[0]);
			ss[0] = null;

			for (String s : ss)
			{
				if (s != null)
				{
					int index = 0;
					while (index < s.length() && s.charAt(index) != '&')
					{
						ret.append("&" + rainbowColors[index % rainbowColors.length]);
						ret.append(s.charAt(index));
						index++;
					}

					if (index < s.length())
					{
						ret.append(s.substring(index));
					}
				}
			}

			message = ret.toString();
		}

		// Format the colors
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}