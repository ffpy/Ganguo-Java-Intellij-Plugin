package com.ganguo.plugin.util;

import com.intellij.openapi.util.io.FileUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;

import java.io.IOException;
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

    public static String fromResource(String path, Map<String, String> params) {
        try {
            return fromString(FileUtil.loadTextAndClose(TemplateUtils.class.getResourceAsStream(path)), params);
        } catch (IOException e) {
            throw new RuntimeException("load resource of " + path + " fail!", e);
        }
    }

    public static String fromString(String template, Map<String, String> params) {
        VelocityContext context = new VelocityContext();
        params.forEach(context::put);
        StringWriter writer = new StringWriter();
        velocityEngine.evaluate(context, writer, "", template);
        return writer.toString();
    }
}
