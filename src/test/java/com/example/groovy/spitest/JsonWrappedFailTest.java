package com.example.groovy.spitest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.Constants.EXAM_FAIL_ON_UNRESOLVED_KEY;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.groovy.json.FastStringServiceFactory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import groovy.json.JsonOutput;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonWrappedFailTest
{
  @Inject
  private FastStringServiceFactory factory;

  @Configuration
  public Option[] config() {
    return options(
        junitBundles(),
        systemProperty(EXAM_FAIL_ON_UNRESOLVED_KEY).value("true"),
        systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
        mavenBundle("org.apache.aries.spifly", "org.apache.aries.spifly.dynamic.bundle")
          .versionAsInProject()
          .startLevel(START_LEVEL_SYSTEM_BUNDLES),
        mavenBundle("org.codehaus.groovy", "groovy-json").versionAsInProject().noStart(),
        wrappedBundle(mavenBundle("org.codehaus.groovy", "groovy").versionAsInProject())
          .instructions("overwrite=merge", "SPI-Consumer=*", "SPI-Provider=*", "Require-Capability=osgi.serviceloader")
        );
  }

  @Test
  public void test_001() throws Exception {
    jsonTester();
  }

  @Test
  public void test_002() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(factory.getClass().getClassLoader());
      jsonTester();
    }
    finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  private void jsonTester() {
    @SuppressWarnings("serial")
    final Map<String, String> m = new HashMap<String, String>() {{
      put("foo", "bar");
    }};
    assertThat(JsonOutput.toJson(m), equalTo("{\"foo\":\"bar\"}"));
  }
}
