package com.ganguo.plugin.util;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.StringWriter;
import java.util.Map;

public class TemplateUtils {

    private final static VelocityEngine velocityEngine;

    static {
        velocityEngine = new VelocityEngine();
        // Disable separate Velocity logging.
        velocityEngine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
                NullLogChute.class.getName());
        velocityEngine.init();
    }

    public static String fromString(String template, Map<String, Object> params) {
        VelocityContext context = new VelocityContext();
        if (params != null) {
            params.forEach(context::put);
        }
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "", template);
        return writer.toString();
    }
}
