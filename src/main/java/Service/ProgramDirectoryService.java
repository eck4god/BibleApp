package main.java.Service;

import java.io.File;
import java.net.URISyntaxException;

public class ProgramDirectoryService {

    private static String getJarName() {
        return new File(ProgramDirectoryService.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath())
                .getName();
    }

    public static boolean runningFromJar() {
        String jarName = getJarName();
        return jarName.contains(".jar");
    }

    public String getProgramDirectory() {
        if (runningFromJar()) {
            return getCurrentJarDirectory();
        } else {
            return getCurrentProjectDirectory();
        }
    }

    private static String getCurrentProjectDirectory() {
        return new File("").getAbsolutePath();
    }

    private static String getCurrentJarDirectory() {
        try {
            return new File(ProgramDirectoryService.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation().toURI().getPath())
                    .getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }
}
