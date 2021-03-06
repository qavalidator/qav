package de.qaware.qav.util;

import lombok.extern.slf4j.Slf4j;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Util methods to deal with ANTLR StringTemplates.
 *
 * @author QAware GmbH
 */
@Slf4j
public final class StringTemplateUtil {

    /**
     * util class with only static methods.
     */
    private StringTemplateUtil() {
    }

    /**
     * Loads the STG file; that should be on the classpath (i.e. could also be in the resources or jar file). The STG
     * file uses "&lt;" / "&gt;" characters.
     *
     * @param templateName the name of the template file
     * @return the {@link StringTemplateGroup}
     */
    public static StringTemplateGroup loadTemplateGroupAngleBracket(String templateName) {

        try (InputStreamReader reader = getTemplateInputStreamReader(templateName)) {
            return new StringTemplateGroup(reader);
        } catch(IOException e) {
            LOGGER.error("Error reading resource {}", templateName, e);
        }
        throw new IllegalArgumentException("Resource not found: " + templateName);
    }

    /**
     * Loads the STG file; that should be on the classpath (i.e. could also be in the resources or jar file). The STG
     * file uses "$" instead of "&lt;" / "&gt;", which is handy for HTML templates.
     *
     * @param templateName the name of the template file
     * @return the {@link StringTemplateGroup}
     */
    public static StringTemplateGroup loadTemplateGroupDollarSign(String templateName) {
        try (InputStreamReader reader = getTemplateInputStreamReader(templateName)) {
            return new StringTemplateGroup(reader, DefaultTemplateLexer.class);
        } catch(IOException e) {
            LOGGER.error("Error reading resource {}", templateName, e);
            throw new IllegalArgumentException("Resource not found: " + templateName);
        }
    }

    private static InputStreamReader getTemplateInputStreamReader(String templateName) {
        InputStream is = StringTemplateUtil.class.getResourceAsStream(templateName);

        if (is == null) {
            String msg = "Resource '" + templateName + "' not found.";
            LOGGER.error(msg);
            throw new IllegalArgumentException(msg);
        }

        return new InputStreamReader(is, StandardCharsets.UTF_8);
    }
}
