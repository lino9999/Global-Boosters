package com.Lino.globalBoosters.utils;

import org.bukkit.ChatColor;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientColor {

    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6})>(.*?)</gradient>");

    public static String apply(String text) {
        if (text == null) return "";

        text = ChatColor.translateAlternateColorCodes('&', text);

        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1);
            String endColor = matcher.group(2);
            String content = matcher.group(3);

            boolean bold = content.contains("§l");
            String strippedContent = ChatColor.stripColor(content);

            String gradientText = createGradient(strippedContent, startColor, endColor, bold);
            matcher.appendReplacement(result, Matcher.quoteReplacement(gradientText));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private static String createGradient(String text, String startHex, String endHex, boolean bold) {
        Color startColor = hexToColor(startHex);
        Color endColor = hexToColor(endHex);

        StringBuilder gradientText = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (c == ' ') {
                gradientText.append(c);
                continue;
            }

            float percent = (float) i / Math.max(1, length - 1);
            Color interpolated = interpolateColor(startColor, endColor, percent);
            String hex = colorToHex(interpolated);

            gradientText.append(hexToMinecraftColor(hex));
            if (bold) {
                gradientText.append("§l");
            }
            gradientText.append(c);
        }

        return gradientText.toString();
    }

    private static Color hexToColor(String hex) {
        hex = hex.replace("#", "");
        return new Color(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        );
    }

    private static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

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

    private static String hexToMinecraftColor(String hex) {
        StringBuilder builder = new StringBuilder("§x");
        hex = hex.replace("#", "");

        for (char c : hex.toCharArray()) {
            builder.append('§').append(c);
        }

        return builder.toString();
    }
}