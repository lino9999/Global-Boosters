package com.Lino.globalBoosters.utils;

import org.bukkit.ChatColor;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientColor {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(`?#[A-Fa-f0-9]{6}`?):(`?#[A-Fa-f0-9]{6}`?)>(.*?)</gradient>");
    private static final Pattern HEX_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

    /**
     * Applies gradient colors to a string
     * @param text The text to apply gradients to
     * @return The text with gradient colors applied
     */
    public static String apply(String text) {
        if (text == null) return null;

        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1).replace("`", "");
            String endColor = matcher.group(2).replace("`", "");
            String content = matcher.group(3);

            String gradientText = createGradient(content, startColor, endColor);
            matcher.appendReplacement(result, gradientText);
        }

        matcher.appendTail(result);

        // Apply hex colors that aren't part of gradients
        return applyHexColors(result.toString());
    }

    /**
     * Creates a gradient between two colors
     * @param text The text to apply gradient to
     * @param startHex Starting hex color
     * @param endHex Ending hex color
     * @return The text with gradient applied
     */
    private static String createGradient(String text, String startHex, String endHex) {
        // Remove color codes from the text but preserve it for gradient calculation
        String cleanText = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', text));

        Color startColor = hexToColor(startHex);
        Color endColor = hexToColor(endHex);

        StringBuilder gradientText = new StringBuilder();
        int length = cleanText.length();

        for (int i = 0; i < length; i++) {
            char c = cleanText.charAt(i);

            // Skip spaces and special characters for gradient calculation
            if (c == ' ' || c == '&' || c == '§') {
                gradientText.append(c);
                continue;
            }

            float percent = (float) i / Math.max(1, length - 1);
            Color interpolated = interpolateColor(startColor, endColor, percent);
            String hex = colorToHex(interpolated);

            gradientText.append(hexToMinecraftColor(hex)).append(c);
        }

        return gradientText.toString();
    }

    /**
     * Converts a hex color string to a Color object
     * @param hex The hex color string
     * @return The Color object
     */
    private static Color hexToColor(String hex) {
        hex = hex.replace("#", "");
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        );
    }

    /**
     * Converts a Color object to a hex string
     * @param color The Color object
     * @return The hex color string
     */
    private static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /**
     * Interpolates between two colors
     * @param start Starting color
     * @param end Ending color
     * @param percent Percentage (0.0 to 1.0)
     * @return The interpolated color
     */
    private static Color interpolateColor(Color start, Color end, float percent) {
        int r = Math.round(start.getRed() + (end.getRed() - start.getRed()) * percent);
        int g = Math.round(start.getGreen() + (end.getGreen() - start.getGreen()) * percent);
        int b = Math.round(start.getBlue() + (end.getBlue() - start.getBlue()) * percent);

        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    /**
     * Converts a hex color to Minecraft color format
     * @param hex The hex color
     * @return The Minecraft color code
     */
    private static String hexToMinecraftColor(String hex) {
        // For versions 1.16+, use the hex color format
        StringBuilder builder = new StringBuilder("§x");
        hex = hex.replace("#", "");

        for (char c : hex.toCharArray()) {
            builder.append('§').append(c);
        }

        return builder.toString();
    }

    /**
     * Applies hex colors to text (for non-gradient hex colors)
     * @param text The text to process
     * @return The text with hex colors applied
     */
    private static String applyHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group();
            String replacement = hexToMinecraftColor(hex);
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }
}