package com.amastigote.templatemsg.module;

import java.util.ArrayList;

public class TemplateUtils {
    public static ArrayList<Integer> spot_placeholders(String string, String symbol) {
        int next_off = 0;
        ArrayList<Integer> int_arrlst = new ArrayList<>();
        while (true) {
            next_off = string.indexOf(symbol, next_off);
            if (next_off != -1)
                int_arrlst.add(next_off);
            else
                break;
            next_off += symbol.length();
        }
        return int_arrlst;
    }
}
