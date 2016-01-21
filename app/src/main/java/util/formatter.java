package util;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.herenow.fase1.LineTime;

import java.util.ArrayList;
import java.util.HashMap;

public class formatter {
    HashMap<String, ArrayList<LineTime>> tableLines;

    public formatter(ArrayList<LineTime> lineTimes) {
        tableLines = organizeLines(lineTimes);
    }

    /**
     * Create a table  L1 | {lineTimes}
     *
     * @param lineTimes
     * @return
     */
    @NonNull
    private static HashMap<String, ArrayList<LineTime>> organizeLines(ArrayList<LineTime> lineTimes) {
        HashMap<String, ArrayList<LineTime>> tableLines = new HashMap<>();
        ArrayList arr;


        for (LineTime lineTime : lineTimes) {
            String lc = lineTime.lineCode;
            if (tableLines.containsKey(lc)) {
                //add a time to the line
                arr = tableLines.get(lc);
                arr.add(lineTime);
                tableLines.put(lc, arr);
            } else {
                //add a line with first time
                arr = new ArrayList();
                arr.add(lineTime);
                tableLines.put(lc, arr);
            }
        }
        return tableLines;
    }

    /**
     * Shows only the first arrival by line:  L1:10m | B3: 5m | R4:18m
     *
     * @return
     */
    public String summarizeAllLines() {
        String substring = "No info";

        StringBuilder sb = new StringBuilder();

        for (String name : tableLines.keySet()) {
            String roundedTime = tableLines.get(name).get(0).roundedTime;
            int pos = roundedTime.indexOf(" ");
            sb.append(name + ": " + roundedTime.substring(0, pos) + "m | ");
        }
        String s = sb.toString();
        substring = s.substring(0, s.length() - 2);

        return substring;
    }

    /**
     * Shows only the first arrival by line in compact version:  L1:10|B3:5|R4:18
     *
     * @return
     */
    public String summarizeAllLinesCompact() {
        return null;
    }

    /**
     * Array with strings that summarizes each line: L1: 12 min, 18 min, 35 min
     *
     * @return
     */
    public ArrayList<SpannableString> summarizeByOneLine() {
        ArrayList<SpannableString> arr = new ArrayList<>();

        for (String name : tableLines.keySet()) {
            ArrayList<LineTime> arrTimes = tableLines.get(name);
            StringBuilder sb = new StringBuilder(name + " ");

            for (LineTime lineTime : arrTimes) {
                sb.append(lineTime.roundedTime + ", ");
            }

            String s = sb.toString();
            String sub = s.substring(0, s.length() - 2);

            //add format
            SpannableString span = new SpannableString(sub);
            span.setSpan(new ForegroundColorSpan(Color.BLACK), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new RelativeSizeSpan(1.1f), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            arr.add(span);
        }

        return arr;
    }

    public HashMap<String, ArrayList<LineTime>> getTable() {
        return tableLines;
    }
}