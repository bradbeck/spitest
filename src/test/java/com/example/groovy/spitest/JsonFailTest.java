package com.example.groovy.spitest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.Constants.EXAM_FAIL_ON_UNRESOLVED_KEY;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

import groovy.json.JsonOutput;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonFailTest
{
  @Inject
  private BundleContext context;

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
        mavenBundle("org.codehaus.groovy", "groovy").versionAsInProject()
    );
  }

  @Test
  public void test_001() {
    jsonTester();
  }

  @Test
  public void test_002() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(groovyBundle().adapt(BundleWiring.class).getClassLoader());
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

  private Bundle groovyBundle() {
    for (Bundle b : context.getBundles()) {
      if ("groovy".equals(b.getSymbolicName())) {
        return b;
      }
    }
    throw new IllegalStateException("No groovy bundle found");
  }
}
