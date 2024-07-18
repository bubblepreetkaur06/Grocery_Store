import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// User base class
abstract class User {
    private String username;
    private String password;
    private String email;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public String getPassword() {
        return password;
    }
}

// Customer class
class Customer extends User {
    private List<Order> orders;

    public Customer(String username, String password, String email) {
        super(username, password, email);
        this.orders = new ArrayList<>();
    }

    public void placeOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrders() {
        return orders;
    }
}

// Product class
class Product {
    private String name;
    private String category;
    private double price;
    private int stock;

    public Product(String name, String category, double price, int stock) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}

// OrderItem class
class OrderItem {
    private Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }

    public void reduceQuantity(int quantity) {
        this.quantity -= quantity;
    }
}

// Order class
class Order {
    private List<OrderItem> items;
    private Customer customer;
    private double totalPrice;
    private String status;

    public Order(Customer customer) {
        this.customer = customer;
        this.items = new ArrayList<>();
        this.status = "Pending";
        this.totalPrice = 0.0;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        totalPrice += item.getTotalPrice();
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        totalPrice -= item.getTotalPrice();
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

// UserManager class for handling user data in a file
class UserManager {
    private static final String USER_FILE = "users.txt";

    public UserManager() throws IOException {
        // Ensure the user file exists
        File file = new File(USER_FILE);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public void registerUser(User user) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(user.getUsername() + "," + user.getPassword() + "," + user.getEmail());
            writer.newLine();
        }
    }

    public User authenticateUser(String username, String password) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username) && parts[1].equals(password)) {
                    return new Customer(parts[0], parts[1], parts[2]);
                }
            }
        }
        return null;
    }

    public boolean userExists(String username) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }
}

// Main class
public class GroceryPlatform {
    private List<Product> products;
    private List<Order> orders;
    private UserManager userManager;

    public GroceryPlatform() {
        this.products = new ArrayList<>();
        this.orders = new ArrayList<>();
        try {
            this.userManager = new UserManager();
        } catch (IOException e) {
            System.out.println("Error initializing UserManager: " + e.getMessage());
        }
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public List<Product> getProducts() {
        return products;
    }

    public void placeOrder(Order order) {
        orders.add(order);
        order.getCustomer().placeOrder(order);
    }

    public List<Order> getOrders() {
        return orders;
    }

    public static void main(String[] args) {
        GroceryPlatform platform = new GroceryPlatform();
        Scanner scanner = new Scanner(System.in);

        // Sample Data
        platform.addProduct(new Product("Milk", "Dairy", 35, 100));
        platform.addProduct(new Product("Cheese", "Dairy", 20, 50));
        platform.addProduct(new Product("Chips", "Snacks", 20, 200));
        platform.addProduct(new Product("Cookies", "Snacks", 50, 150));
        platform.addProduct(new Product("Apples", "Fruits", 30, 100));
        platform.addProduct(new Product("Bananas", "Fruits", 10, 120));
        platform.addProduct(new Product("Carrots", "Vegetables", 25, 80));
        platform.addProduct(new Product("Broccoli", "Vegetables", 40, 60));

        // Welcome Message
        System.out.println("======================================");
        System.out.println("        Welcome to XYZ Store!          ");
        System.out.println("======================================");

        while (true) {
            User user = null;

            while (user == null) {
                System.out.println("\n1. Login");
                System.out.println("2. Create Account");
                System.out.print("Choose an option: ");
                int choice = Integer.parseInt(scanner.nextLine());

                if (choice == 1) {
                    // Login
                    System.out.print("\nEnter username: ");
                    String username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();

                    try {
                        if (platform.userManager.userExists(username)) {
                            user = platform.userManager.authenticateUser(username, password);
                            if (user != null) {
                                System.out.println("\nLogin successful!");
                                handleCustomerActions(scanner, platform, (Customer) user);
                                // Exit after checkout to stop further actions
                                return;
                            } else {
                                System.out.println("\nLogin failed! Invalid username or password.");
                            }
                        } else {
                            System.out.println("\nNo account with this username. Please create an account.");
                            createAccount(scanner, platform);
                        }
                    } catch (IOException e) {
                        System.out.println("Error accessing user file: " + e.getMessage());
                    }
                } else if (choice == 2) {
                    // Create Account
                    createAccount(scanner, platform);
                } else {
                    System.out.println("\nInvalid choice. Please try again.");
                }
            }
        }
    }

    private static void createAccount(Scanner scanner, GroceryPlatform platform) {
        System.out.print("\nEnter new username: ");
        String username = scanner.nextLine();

        try {
            if (platform.userManager.userExists(username)) {
                System.out.println("\nUsername already exists. Please choose a different username.");
                return;
            }

            System.out.print("Enter new password: ");
            String password = scanner.nextLine();
            System.out.print("Enter email: ");
            String email = scanner.nextLine();

            User newUser = new Customer(username, password, email);
            platform.userManager.registerUser(newUser);
            System.out.println("\nAccount created successfully! Please log in.");
        } catch (IOException e) {
            System.out.println("Error writing to user file: " + e.getMessage());
        }
    }

    private static void handleCustomerActions(Scanner scanner, GroceryPlatform platform, Customer customer) {
        while (true) {
            System.out.println("\n1. View Products");
            System.out.println("2. View Cart");
            System.out.println("3. Checkout");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                // View Products by Category
                viewProductsByCategory(scanner, platform, customer);
            } else if (choice == 2) {
                // View Cart
                viewCart(scanner, customer);
            } else if (choice == 3) {
                // Checkout
                checkout(customer);
                System.out.println("\nThank you for shopping!");
                break;  // Exit after checkout
            } else if (choice == 4) {
                // Logout
                System.out.println("\nLogged out successfully!");
                break;
            } else {
                System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private static void viewProductsByCategory(Scanner scanner, GroceryPlatform platform, Customer customer) {
        System.out.println("\n1. Dairy Products");
        System.out.println("2. Snacks");
        System.out.println("3. Fruits");
        System.out.println("4. Vegetables");
        System.out.print("Choose a category: ");
        int categoryChoice = Integer.parseInt(scanner.nextLine());

        String category = switch (categoryChoice) {
            case 1 -> "Dairy";
            case 2 -> "Snacks";
            case 3 -> "Fruits";
            case 4 -> "Vegetables";
            default -> null;
        };

        if (category == null) {
            System.out.println("\nInvalid category choice.");
            return;
        }

        List<Product> categoryProducts = new ArrayList<>();
        for (Product product : platform.getProducts()) {
            if (product.getCategory().equals(category)) {
                categoryProducts.add(product);
            }
        }

        if (categoryProducts.isEmpty()) {
            System.out.println("\nNo products available in this category.");
            return;
        }

        System.out.println("\n" + category + " Products:");
        for (Product product : categoryProducts) {
            System.out.println(product.getName() + " - Rs " + product.getPrice() + " (" + product.getStock() + " in stock)");
        }

        addToCartOrContinue(scanner, platform, categoryProducts, customer);
    }

    private static void addToCartOrContinue(Scanner scanner, GroceryPlatform platform, List<Product> products, Customer customer) {
        while (true) {
            System.out.print("\nEnter product name to add to cart: ");
            String productName = scanner.nextLine();
            System.out.print("Enter quantity: ");
            int quantity = Integer.parseInt(scanner.nextLine());

            Product productToAdd = null;
            for (Product product : products) {
                if (product.getName().equals(productName)) {
                    productToAdd = product;
                    break;
                }
            }

            if (productToAdd != null && productToAdd.getStock() >= quantity) {
                Order order = new Order(customer);
                OrderItem item = new OrderItem(productToAdd, quantity);
                order.addItem(item);
                customer.placeOrder(order);
                productToAdd.setStock(productToAdd.getStock() - quantity);
                System.out.println("\nItem added to cart.");

                System.out.print("Do you want to add more items? (yes/no): ");
                String addMore = scanner.nextLine();
                if (addMore.equalsIgnoreCase("no")) {
                    viewCart(scanner, customer);
                    break; // Exit the loop and go to view cart
                } else if (addMore.equalsIgnoreCase("yes")) {
                    // Go back to view products by category
                    viewProductsByCategory(scanner, platform, customer);
                    break; // Exit the loop and go back to view products
                }
            } else {
                System.out.println("\nProduct not available or insufficient stock!");
            }
        }
    }

    private static void viewCart(Scanner scanner, Customer customer) {
        List<Order> orders = customer.getOrders();
        if (orders.isEmpty()) {
            System.out.println("\nYour cart is empty.");
            return;
        }

        while (true) {
            System.out.println("\nYour cart:");
            for (Order order : orders) {
                for (OrderItem item : order.getItems()) {
                    System.out.println(item.getProduct().getName() + " - Quantity: " + item.getQuantity() + " - Price: Rs " + item.getTotalPrice());
                }
            }

            System.out.println("\n1. Delete an item");
            System.out.println("2. Continue to checkout");
            System.out.print("Choose an option: ");
            int choice = Integer.parseInt(scanner.nextLine());

            if (choice == 1) {
                System.out.print("\nEnter product name to delete: ");
                String productName = scanner.nextLine();
                System.out.print("Enter quantity to delete: ");
                int quantityToDelete = Integer.parseInt(scanner.nextLine());

                boolean itemDeleted = false;
                for (Order order : orders) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getProduct().getName().equals(productName) && item.getQuantity() >= quantityToDelete) {
                            item.reduceQuantity(quantityToDelete);
                            item.getProduct().setStock(item.getProduct().getStock() + quantityToDelete);
                            System.out.println("\nItem quantity updated in cart.");

                            if (item.getQuantity() == 0) {
                                order.removeItem(item);
                                System.out.println("\nItem removed from cart.");
                            }

                            itemDeleted = true;
                            break;
                        }
                    }
                    if (itemDeleted) break;
                }

                if (!itemDeleted) {
                    System.out.println("\nItem not found in cart or insufficient quantity to delete.");
                }
            } else if (choice == 2) {
                break; // Exit the loop and continue to checkout
            } else {
                System.out.println("\nInvalid choice. Please try again.");
            }
        }
    }

    private static void checkout(Customer customer) {
        List<Order> orders = customer.getOrders();
        if (orders.isEmpty()) {
            System.out.println("\nYour cart is empty.");
            return;
        }
        double totalAmount = 0;
        for (Order order : orders) {
            totalAmount += order.getTotalPrice();
        }
        System.out.println("\nTotal amount to pay: Rs " + totalAmount);
        // Clear cart after checkout
        orders.clear();
    }
}
