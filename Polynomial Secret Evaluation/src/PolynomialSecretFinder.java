import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class PolynomialSecretFinder {

    // Helper class to store (x,y) points
    static class Point {
        BigInteger x;
        BigInteger y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Decodes a value from given base to decimal (BigInteger)
    private static BigInteger decodeValue(String valueStr, String baseStr) {
        int base = Integer.parseInt(baseStr);
        return new BigInteger(valueStr, base);
    }

    // Performs Lagrange interpolation to find the constant term (secret)
    private static BigInteger findSecret(List<Point> points, int k) {
        BigInteger secret = BigInteger.ZERO;

        // We only need first k points to reconstruct the polynomial
        for (int i = 0; i < k; i++) {
            BigInteger xi = points.get(i).x;
            BigInteger yi = points.get(i).y;

            // Calculate the Lagrange basis polynomial evaluated at x=0
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (j != i) {
                    BigInteger xj = points.get(j).x;
                    // Multiply numerator by (0 - xj) = -xj
                    numerator = numerator.multiply(xj.negate());
                    // Multiply denominator by (xi - xj)
                    denominator = denominator.multiply(xi.subtract(xj));
                }
            }

            // Add the term: yi * (numerator / denominator)
            BigInteger term = yi.multiply(numerator).divide(denominator);
            secret = secret.add(term);
        }

        return secret;
    }

    public static void main(String[] args) {
        JSONParser parser = new JSONParser();

        try {
            // Process Test Case 1
            JSONObject testCase1 = (JSONObject) parser.parse(new FileReader("test_case1.json"));
            processTestCase(testCase1, "Test Case 1");

            // Process Test Case 2
            JSONObject testCase2 = (JSONObject) parser.parse(new FileReader("test_case2.json"));
            processTestCase(testCase2, "Test Case 2");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void processTestCase(JSONObject testCase, String caseName) {
        // Extract n and k values
        JSONObject keys = (JSONObject) testCase.get("keys");
        int n = ((Long) keys.get("n")).intValue();
        int k = ((Long) keys.get("k")).intValue();

        // Collect all points
        List<Point> points = new ArrayList<>();
        for (Object key : testCase.keySet()) {
            String keyStr = (String) key;
            if (!keyStr.equals("keys")) {
                JSONObject pointData = (JSONObject) testCase.get(keyStr);
                BigInteger x = new BigInteger(keyStr);
                String base = (String) pointData.get("base");
                String value = (String) pointData.get("value");
                BigInteger y = decodeValue(value, base);
                points.add(new Point(x, y));
            }
        }

        // Find the secret using first k points
        BigInteger secret = findSecret(points, k);
        System.out.println(caseName + " Secret: " + secret);
    }
}