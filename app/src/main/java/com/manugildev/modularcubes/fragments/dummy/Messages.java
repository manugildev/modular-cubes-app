package com.manugildev.modularcubes.fragments.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Messages {

    public static final List<CubeMessage> ITEMS = new ArrayList<CubeMessage>();

    public static final Map<String, CubeMessage> ITEM_MAP = new HashMap<String, CubeMessage>();

    private static final int COUNT = 25;

    private static void addItem(CubeMessage item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(ITEMS.size() - 1), item);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class CubeMessage {
        public final String content;

        public CubeMessage(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
