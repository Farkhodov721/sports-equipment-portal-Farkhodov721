import java.util.*;
import java.util.stream.Collectors;

public class Sports {
    private Set<String> activities = new TreeSet<>();
    private Map<String, Set<String>> categoryToActivities = new TreeMap<>();
    private Map<String, Set<String>> activityToCategories = new TreeMap<>();
    private Map<String, Product> products = new TreeMap<>();
    private Map<String, List<Rating>> productRatings = new HashMap<>();

    // R1: Activities and Categories
    public void defineActivities(String... activities) throws SportsException {
        if (activities == null || activities.length == 0) {
            throw new SportsException("No activity provided");
        }
        this.activities.addAll(Arrays.asList(activities));
    }

    public List<String> getActivities() {
        return new ArrayList<>(activities);
    }

    public void addCategory(String name, String... linkedActivities) throws SportsException {
        for (String act : linkedActivities) {
            if (!activities.contains(act)) {
                throw new SportsException("Activity not defined: " + act);
            }
        }
        categoryToActivities.put(name, new TreeSet<>(Arrays.asList(linkedActivities)));
        for (String act : linkedActivities) {
            activityToCategories.putIfAbsent(act, new TreeSet<>());
            activityToCategories.get(act).add(name);
        }
    }

    public int countCategories() {
        return categoryToActivities.size();
    }

    public List<String> getCategoriesForActivity(String activity) {
        return activityToCategories.getOrDefault(activity, new TreeSet<>())
                                   .stream().collect(Collectors.toList());
    }

    // R2: Products
    public void addProduct(String name, String activityName, String categoryName) throws SportsException {
        if (products.containsKey(name)) {
            throw new SportsException("Duplicate product: " + name);
        }
        if (!activities.contains(activityName)) {
            throw new SportsException("Activity not defined: " + activityName);
        }
        if (!categoryToActivities.containsKey(categoryName) || 
            !categoryToActivities.get(categoryName).contains(activityName)) {
            throw new SportsException("Invalid category or not linked to activity");
        }
        products.put(name, new Product(name, activityName, categoryName));
    }

    public List<String> getProductsForCategory(String categoryName) {
        return products.values().stream()
                .filter(p -> p.category.equals(categoryName))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getProductsForActivity(String activityName) {
        return products.values().stream()
                .filter(p -> p.activity.equals(activityName))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getProducts(String activityName, String... categoryNames) {
        Set<String> categories = new HashSet<>(Arrays.asList(categoryNames));
        return products.values().stream()
                .filter(p -> p.activity.equals(activityName) && categories.contains(p.category))
                .map(p -> p.name)
                .sorted()
                .collect(Collectors.toList());
    }

    // R3: Ratings
    public void addRating(String productName, String userName, int numStars, String comment) throws SportsException {
        if (numStars < 0 || numStars > 5) {
            throw new SportsException("Stars must be between 0 and 5");
        }
        if (!products.containsKey(productName)) {
            throw new SportsException("Product does not exist");
        }
        productRatings.putIfAbsent(productName, new ArrayList<>());
        productRatings.get(productName).add(new Rating(userName, numStars, comment));
    }

    public List<String> getRatingsForProduct(String productName) {
        return productRatings.getOrDefault(productName, new ArrayList<>())
                .stream()
                .sorted((a, b) -> Integer.compare(b.stars, a.stars))
                .map(r -> r.stars + " : " + r.comment)
                .collect(Collectors.toList());
    }

    // R4: Evaluations
    public double getStarsOfProduct(String productName) {
        List<Rating> ratings = productRatings.get(productName);
        if (ratings == null || ratings.isEmpty()) return 0;
        return ratings.stream().mapToInt(r -> r.stars).average().orElse(0);
    }

    public double averageStars() {
        return productRatings.values().stream()
                .flatMap(List::stream)
                .mapToInt(r -> r.stars)
                .average().orElse(0);
    }

    // R5: Statistics
    public SortedMap<String, Double> starsPerActivity() {
        SortedMap<String, List<Integer>> actToStars = new TreeMap<>();
        for (Map.Entry<String, List<Rating>> entry : productRatings.entrySet()) {
            String activity = products.get(entry.getKey()).activity;
            List<Integer> stars = actToStars.computeIfAbsent(activity, k -> new ArrayList<>());
            stars.addAll(entry.getValue().stream().map(r -> r.stars).collect(Collectors.toList()));
        }
        SortedMap<String, Double> result = new TreeMap<>();
        for (Map.Entry<String, List<Integer>> entry : actToStars.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().mapToInt(i -> i).average().orElse(0));
        }
        return result;
    }

    public SortedMap<Double, List<String>> getProductsPerStars() {
        Map<String, Double> avgStars = new HashMap<>();
        for (String prod : productRatings.keySet()) {
            avgStars.put(prod, getStarsOfProduct(prod));
        }
        SortedMap<Double, List<String>> res = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<String, Double> entry : avgStars.entrySet()) {
            res.putIfAbsent(entry.getValue(), new ArrayList<>());
            res.get(entry.getValue()).add(entry.getKey());
        }
        for (List<String> list : res.values()) {
            Collections.sort(list);
        }
        return res;
    }

    // Inner helper classes
    private static class Product {
        String name;
        String activity;
        String category;

        Product(String name, String activity, String category) {
            this.name = name;
            this.activity = activity;
            this.category = category;
        }
    }

    private static class Rating {
        String user;
        int stars;
        String comment;

        Rating(String user, int stars, String comment) {
            this.user = user;
            this.stars = stars;
            this.comment = comment;
        }
    }
}
