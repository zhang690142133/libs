package xsg.lychee.richalpha.utils.firebase;

import com.google.firebase.perf.metrics.Trace;

import com.google.firebase.perf.FirebasePerformance;

import java.util.HashMap;
import java.util.Map;


public class WZ3Performance {
    private static Map sTraceMap;

    public static void startTrace(String tag) {
        if (sTraceMap == null) {
            sTraceMap = new HashMap();
        }

        stopTrace(tag);

        Trace myTrace = FirebasePerformance.getInstance().newTrace(tag);
        if (myTrace != null) {
            myTrace.start();
            sTraceMap.put(tag, myTrace);
        }
    }

    public static void stopTrace(String tag) {
        if (sTraceMap == null) {
            return;
        }

        Trace myTrace = (Trace) sTraceMap.get(tag);
        if (myTrace != null) {
            myTrace.stop();
            sTraceMap.remove(tag);
        }
    }
}
