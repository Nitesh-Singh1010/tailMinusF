package com.example.browserstack.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
                return "Unsupported browser: " + browser;
            }

            System.out.println("Opening browser " + browser + " with " + url);
            processBuilder.start();

            return "Started browser " + browser + " with URL " + url;
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to start browser: " + e.getMessage();
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

            System.out.println("Trying to kill process: " + processName);

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "taskkill", "/F", "/IM", processName);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Successfully killed process: " + processName);
                return "Stopped browser: " + browser;
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                reader.close();
                return "Failed to stop browser. Error: " + errorOutput.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to stop browser: " + e.getMessage();
        }
    }

    @GetMapping("/cleanup")
    public String cleanupBrowser(@RequestParam("browser") String browser) {
        String userProfile = System.getenv("USERPROFILE");
        File profileDir;

        switch (browser.toLowerCase()) {
            case "google chrome":
                profileDir = new File(userProfile + "\\AppData\\Local\\Google\\Chrome\\User Data\\Default");
                break;
            case "edge":
                profileDir = new File(userProfile + "\\AppData\\Local\\Microsoft\\Edge\\User Data\\Default");
                break;
            default:
                return "Unsupported browser: " + browser;
        }

        try {
            if (profileDir.exists()) {
                deleteDirectory(profileDir);
                return "Cleaned up browser session for: " + browser;
            } else {
                return "No session data found for: " + browser;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to clean up browser: " + e.getMessage();
        }
    }

    public static void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            File[] entries = dir.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete: " + dir.getAbsolutePath());
        }
    }
}
