package com.example;

import static spark.Spark.*;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Read port from environment (e.g., Render sets PORT). Fallback to 8080 for local testing.
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null && !portEnv.isBlank()) ? Integer.parseInt(portEnv) : 8080;

        ipAddress("0.0.0.0");
        port(port);

        // Serve static files from src/main/resources/public
        // e.g., uniform.html will be available at /uniform.html
        staticFiles.location("/public");

        // Redirect root to the UI page
        get("/", (req, res) -> {
            res.redirect("/uniform.html");
            return null;
        });

        // Enable simple CORS for all routes (so frontend on same or different origin can call)
        options("/*", (req, res) -> {
            String reqHeaders = req.headers("Access-Control-Request-Headers");
            if (reqHeaders != null) res.header("Access-Control-Allow-Headers", reqHeaders);
            String reqMethod = req.headers("Access-Control-Request-Method");
            if (reqMethod != null) res.header("Access-Control-Allow-Methods", reqMethod);
            res.header("Access-Control-Allow-Origin", "*");
            return "OK";
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
        });

        // Health check endpoint
        get("/health", (req, res) -> "OK");

        // Main generate endpoint
        get("/generate", (req, res) -> {
            String strengthS = req.queryParams("strength");
            String numInputsS = req.queryParams("numInputs");
            String paramValueS = req.queryParams("paramValue");

            // Basic validation
            if (strengthS == null || numInputsS == null || paramValueS == null) {
                res.status(400);
                return "[ERROR] Missing parameters: strength, numInputs, paramValue";
            }

            try {
                int strength = Integer.parseInt(strengthS);
                int numInputs = Integer.parseInt(numInputsS);
                int paramValue = Integer.parseInt(paramValueS);

                if (strength <= 0 || numInputs <= 0 || paramValue <= 0) {
                    res.status(400);
                    return "[ERROR] Parameters must be positive integers";
                }

                // Build the value[] array: length = numInputs, each entry = paramValue
                // (Your generator's constructor expects an int[] describing number of values per input)
                int[] value = new int[numInputs];
                Arrays.fill(value, paramValue);

                System.out.println("[DEBUG] /generate called with strength=" + strength
                        + " numInputs=" + numInputs + " paramValue=" + paramValue);
                System.out.println("[DEBUG] value[] = " + Arrays.toString(value));

                // Construct and run the generator (synchronously)
                TestSuiteGenerator generator = new TestSuiteGenerator(value, strength);

                long start = System.currentTimeMillis();
                String result = generator.generateTestSuite(); // may take time for large inputs
                long elapsed = System.currentTimeMillis() - start;

                System.out.println("[INFO] Generation finished in " + elapsed + " ms. Result length=" + (result == null ? 0 : result.length()));

                res.type("text/plain");
                return result;

            } catch (NumberFormatException nfe) {
                res.status(400);
                return "[ERROR] Invalid numeric parameters: " + nfe.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "[ERROR] Internal Server Error: " + e.toString();
            }
        });

        System.out.println("✅ Server started on port " + port + " (0.0.0.0).");
        System.out.println("✅ UI available at: http://<host>:" + port + "/uniform.html");
    }
}
