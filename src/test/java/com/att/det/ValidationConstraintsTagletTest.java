package com.att.det;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DocumentationTool;
import javax.tools.ToolProvider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class ValidationConstraintsTagletTest {

    @Test
    public void combined() throws Exception {
        runJavadoc();
        Map<String, List<String>> propTextMap = extractPropertyMessageMap("build/javadoc/com/att/det/CombinedStuff.html");
        assertThat(propTextMap.get("anotherThing")).hasSize(1);
        assertThat(propTextMap.get("anotherThing").get(0)).contains(",");
    }

    @Test
    public void uncombined() throws Exception {
        runJavadoc();
        Map<String, List<String>> propTextMap = extractPropertyMessageMap("build/javadoc/com/att/det/UncombinedStuff.html");
        assertThat(propTextMap.get("anotherThing")).hasSize(2);
        assertThat(propTextMap.get("anotherThing").get(0)).contains("may not be null");
        assertThat(propTextMap.get("anotherThing").get(1)).contains("See class");
    }

    private void runJavadoc() {
        ByteArrayOutputStream   stdout    = new ByteArrayOutputStream();
        ByteArrayOutputStream   stderr    = new ByteArrayOutputStream();
        DocumentationTool systemDocumentationTool = ToolProvider.getSystemDocumentationTool();
        systemDocumentationTool.run(null, stdout, stderr,
                                    "-verbose",
                                    "-taglet", "com.att.det.taglet.ValidationConstraintsTaglet",
                                    "-taglet", "com.att.det.taglet.ValidationConstraintsCombinedTaglet",
                                    "-tagletpath", "build/classes/main",
                                    "-d", "build/javadoc",
                                    "-sourcepath", "src/test/java",
                                    "com.att.det");
    }
    
    private Map<String, List<String>> extractPropertyMessageMap(String filePath) throws IOException {
        File    htmlFile   = new File(filePath);
        Document    htmlDoc     = Jsoup.parse(htmlFile, "utf-8");
        Element     descDiv     = htmlDoc.select("div.description").first();
        Element     tbody       = descDiv.select("tbody").first();
        Elements    rows        = tbody.select("tr");
        Map<String, List<String>> propTextMap = new HashMap<>(); 
        rows.forEach(it -> {
            String          name    = it.select("td").first().text();
            List<String>    values  = propTextMap.get(name);
            if (values == null) {
                values  = new ArrayList<>();
                propTextMap.put(name,  values);
            }
            values.add(it.select("td").last().text());
            });
        return propTextMap;
    }
}
