import java.util.*;
import java.util.stream.Collectors;

public class Sports {
    private Set<String> activities = new TreeSet<>();
    private Map<String, Set<String>> categoryToActivities = new HashMap<>();
    private Map<String, Set<String>> activityToCategories = new HashMap<>();
    private Map<String, Product> products = new TreeMap<>();
    private Map<String, List<Rating>> productRatings = new HashMap<>();

    private static class Product {
        String name;
        String activity;
        String category;

        public Product(String name, String activity, String category) {
            this.name = name;
            this.activity = activity;
            this.category = category;
        }
    }

    private static class Rating {
        String productName;
        String userName;
        int stars;
        String comment;

        public Rating(String productName, String userName, int stars, String comment) {
            this.productName = productName;
            this.userName = userName;
            this.stars = stars;
            this.comment = comment;
        }

        @Override
        public String toString() {
            return stars + " : " + comment;
        }
    }

    public void defineActivities(String... activities) throws SportsException {
        if (activities.length == 0) {
            throw new SportsException("No activities provided");
        }

        for (String activity : activities) {
            this.activities.add(activity);
            activityToCategories.putIfAbsent(activity, new TreeSet<>());
        }
    }

    public List<String> getActivities() {
        return new ArrayList<>(activities);
    }

    public void addCategory(String name, String... linkedActivities) throws SportsException {
        for (String activity : linkedActivities) {
            if (!activities.contains(activity)) {
                throw new SportsException("Activity " + activity + " does not exist");
            }
        }

        Set<String> linkedActivitiesSet = new HashSet<>(Arrays.asList(linkedActivities));
        categoryToActivities.put(name, linkedActivitiesSet);

        for (String activity : linkedActivities) {
            activityToCategories.get(activity).add(name);
        }
    }

    public int countCategories() {
        return categoryToActivities.size();
    }

    public List<String> getCategoriesForActivity(String activity) {
        return new ArrayList<>(activityToCategories.getOrDefault(activity, new TreeSet<>()));
    }

    public void addProduct(String name, String activityName, String categoryName) throws SportsException {
        if (products.containsKey(name)) {
            throw new SportsException("Product " + name + " already exists");
        }

        products.put(name, new Product(name, activityName, categoryName));
        productRatings.putIfAbsent(name, new ArrayList<>());
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

    public void addRating(String productName, String userName, int numStars, String comment) throws SportsException {
        if (numStars < 0 || numStars > 5) {
            throw new SportsException("Star rating must be between 0 and 5");
        }

        Rating rating = new Rating(productName, userName, numStars, comment);
        productRatings.computeIfAbsent(productName, k -> new ArrayList<>()).add(rating);
    }

    public List<String> getRatingsForProduct(String productName) {
        return productRatings.getOrDefault(productName, Collections.emptyList()).stream()
                .sorted((r1, r2) -> Integer.compare(r2.stars, r1.stars))
                .map(Rating::toString)
                .collect(Collectors.toList());
    }

    public double getStarsOfProduct(String productName) {
        List<Rating> ratings = productRatings.get(productName);
        if (ratings == null || ratings.isEmpty()) return 0.0;

        return ratings.stream().mapToInt(r -> r.stars).average().orElse(0.0);
    }

    public double averageStars() {
        List<Rating> allRatings = productRatings.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return allRatings.isEmpty() ? 0.0 : allRatings.stream().mapToInt(r -> r.stars).average().orElse(0.0);
    }

    public SortedMap<String, Double> starsPerActivity() {
        SortedMap<String, Double> result = new TreeMap<>();

        Map<String, List<String>> activityProducts = new HashMap<>();
        for (Product p : products.values()) {
            activityProducts.computeIfAbsent(p.activity, k -> new ArrayList<>()).add(p.name);
        }

        for (String activity : activityProducts.keySet()) {
            List<String> names = activityProducts.get(activity);
            List<Rating> relevantRatings = names.stream()
                    .filter(productRatings::containsKey)
                    .flatMap(n -> productRatings.get(n).stream())
                    .collect(Collectors.toList());

            if (!relevantRatings.isEmpty()) {
                double avg = relevantRatings.stream().mapToInt(r -> r.stars).average().orElse(0.0);
                result.put(activity, avg);
            }
        }

        return result;
    }

    public SortedMap<Double, List<String>> getProductsPerStars() {
        SortedMap<Double, List<String>> result = new TreeMap<>(Comparator.reverseOrder());

        for (String productName : products.keySet()) {
            double stars = getStarsOfProduct(productName);
            if (stars > 0) {
                result.computeIfAbsent(stars, k -> new ArrayList<>()).add(productName);
            }
        }

        result.values().forEach(Collections::sort);
        return result;
    }
}
