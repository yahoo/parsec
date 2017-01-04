package com.yahoo.parsec.tools.mojo;

import org.apache.maven.project.MavenProject;

import java.lang.reflect.Field;

/**
 * This class provides some handy untilites for mojo testing
 */
public class MojoTestUtils {

    /**
     * Manually inject a stubbed maven project into an AbstractParsecMojo
     * @param mojo
     * @param stubProject
     * @throws IllegalAccessException
     */
    public static void injectStubMavenProj(AbstractParsecMojo mojo, MavenProject stubProject)
        throws IllegalAccessException {

        Field[] fields = mojo.getClass().getSuperclass().getDeclaredFields();
        for (Field f: fields) {
            if (f.getName().equals("project")) {
                f.setAccessible(true);
                f.set(mojo, stubProject);
                break;
            }
        }

    }
}
