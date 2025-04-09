package com.example.browserstack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;


@RestController
public class WebBrowserController {

    @GetMapping("/start")
    public String startBrowser(@RequestParam("browser") String browser,
            @RequestParam("url") String url) {
        try {
            ProcessBuilder processBuilder;

            if (browser.equalsIgnoreCase("Google Chrome")) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "start chrome " + url);
            } else if (browser.equalsIgnoreCase("Edge")) {
                processBuilder = new ProcessBuilder("cmd.exe", "/c", "start msedge " + url);
            } else {
                return "unsupported browser: " + browser;
            }

            System.out.println("opening browser " + browser + " with " + url);
            processBuilder.start();

            return "started browser " + browser + " with URL " + url;
        } catch (IOException e) {
            e.printStackTrace();
            return "failed to start browser: " + e.getMessage();
        }
    }

    @GetMapping("/stop")
    public String stopBrowser(@RequestParam("browser") String browser) {
        try {
            String processName;

            switch (browser.toLowerCase()) {
                case "google chrome":
                    processName = "chrome.exe";
                    break;
                case "edge":
                    processName = "msedge.exe";
                    break;
                default:
                    return "Unsupported browser: " + browser;
            }

            System.out.println("trying to kill process: " + processName);

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "taskkill", "/F", "/IM", processName);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("successfully killed process: " + processName);
                return "stopped browser: " + browser;
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                reader.close();
                return "failed to stop browser. error: " + errorOutput.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "failed to stop browser: " + e.getMessage();
        }
    }

    @GetMapping("/cleanup")
    public String cleanupBrowser(@RequestParam("browser") String browser) {
        try {
            String processName;

            switch (browser.toLowerCase()) {
                case "chrome":
                    processName = "chrome.exe";
                    break;
                case "edge":
                    processName = "msedge.exe";
                    break;
                default:
                    return "unsupported browser: " + browser;
            }

            // force killing any background processes of the browser
            try {
                System.out.println("ensuring all " + processName + " processes are terminated before cleanup");
                ProcessBuilder killProcessBuilder = new ProcessBuilder("cmd.exe", "/c", "taskkill", "/F", "/IM",
                        processName);
                Process killProcess = killProcessBuilder.start();
                killProcess.waitFor(); // Wait but don't check exit code (it's OK if there's nothing to kill)
            } catch (Exception e) {
                System.out.println("could not terminate processes: " + e.getMessage());
            }

            // temp directory paths based on browser
            String userHome = System.getProperty("user.home");
            String tempPath = "";

            if (browser.equalsIgnoreCase("Google Chrome")) {
                tempPath = userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache";
            } else if (browser.equalsIgnoreCase("Edge")) {
                tempPath = userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\Cache";
            }

            // adding more cleanup paths here
            String[] cleanupPaths = new String[] { tempPath };
            if (browser.equalsIgnoreCase("Google Chrome")) {
                cleanupPaths = new String[] {
                        userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Cache",
                        userHome + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default\\Code Cache"
                };
            } else if (browser.equalsIgnoreCase("Edge")) {
                cleanupPaths = new String[] {
                        userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\Cache",
                        userHome + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default\\Code Cache"
                };
            }

            // cleaning up the directories
            int deletedFiles = 0;
            for (String path : cleanupPaths) {
                if (path != null && !path.isEmpty()) {
                    File tempDir = new File(path);
                    if (tempDir.exists() && tempDir.isDirectory()) {
                        System.out.println("cleaning directory: " + path);
                        File[] cacheFiles = tempDir.listFiles();
                        if (cacheFiles != null) {
                            for (File file : cacheFiles) {
                                try {
                                    if (file.isFile() && file.delete()) {
                                        deletedFiles++;
                                    }
                                } catch (Exception e) {
                                    System.out.println(
                                            "could not delete file: " + file.getName() + " - " + e.getMessage());
                                }
                            }
                        }
                    } else {
                        System.out.println("directory does not exist or is not accessible: " + path);
                    }
                }
            }

            return "cleanup completed for " + browser + "deleted this number of files:" + deletedFiles;

        } catch (Exception e) {
            e.printStackTrace();
            return "failed to cleanup browser: " + e.getMessage();
        }
    }
}