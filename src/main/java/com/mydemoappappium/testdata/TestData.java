package com.mydemoappappium.testdata;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydemoappappium.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Loads test data from testdata/*.json and resolves platform differences (product names, users).
 */
public final class TestData {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode PRODUCTS = read("testdata/products.json");
    private static final JsonNode USERS = read("testdata/users.json");
    private static final JsonNode CHECKOUT = read("testdata/checkout.json");
    private static final JsonNode MESSAGES = read("testdata/messages.json");

    private TestData() {
    }

    public static Product product(String key) {
        JsonNode product = required(PRODUCTS.get("products"), key, "products.json");
        List<String> colors = product.has("colors")
                ? MAPPER.convertValue(product.get("colors"), new TypeReference<List<String>>() {
                })
                : List.of();
        return new Product(platformValue(product.get("name")), new BigDecimal(product.get("price").asText()), colors);
    }

    /**
     * Base price of a product family; every colour variant of a product has its family's price.
     */
    public static BigDecimal familyPrice(String productName) {
        for (Map.Entry<String, JsonNode> family : PRODUCTS.get("familyPrices").properties()) {
            if (productName.startsWith(family.getKey())) {
                return new BigDecimal(family.getValue().asText());
            }
        }
        throw new IllegalArgumentException("No family price for '" + productName + "' in testdata/products.json");
    }

    public static User user(String key) {
        JsonNode user = required(USERS, key, "users.json");
        return new User(platformValue(user.get("username")), user.get("password").asText());
    }

    public static Address address(String key) {
        return convert(required(CHECKOUT.get("addresses"), key, "checkout.json"), Address.class);
    }

    public static PaymentCard card(String key) {
        return convert(required(CHECKOUT.get("cards"), key, "checkout.json"), PaymentCard.class);
    }

    /**
     * Expected UI message; a few differ in wording between the platforms.
     */
    public static String message(String key) {
        return platformValue(required(MESSAGES, key, "messages.json"));
    }

    public static BigDecimal deliveryFee() {
        return new BigDecimal(CHECKOUT.get("deliveryFee").asText());
    }

    private static String platformValue(JsonNode value) {
        return value.isTextual() ? value.asText() : value.get(Config.get().platform().id()).asText();
    }

    private static JsonNode required(JsonNode parent, String key, String file) {
        JsonNode node = parent.get(key);
        if (node == null) {
            throw new IllegalArgumentException("Unknown key '" + key + "' in testdata/" + file);
        }
        return node;
    }

    private static <T> T convert(JsonNode node, Class<T> type) {
        try {
            return MAPPER.treeToValue(node, type);
        } catch (IOException e) {
            throw new UncheckedIOException("Invalid " + type.getSimpleName() + " test data", e);
        }
    }

    private static JsonNode read(String resource) {
        try (InputStream in = TestData.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Test data file not found on classpath: " + resource);
            }
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + resource, e);
        }
    }
}
