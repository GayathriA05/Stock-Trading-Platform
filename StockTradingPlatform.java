import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;
import java.time.LocalDate;

public class StockTradingPlatform {

    static class Stock {
        private final String symbol;
        private double price;

        public Stock(String symbol, double price) {
            if (symbol == null || symbol.isEmpty()) {
                throw new IllegalArgumentException("Stock symbol cannot be null or empty.");
            }
            if (price <= 0) {
                throw new IllegalArgumentException("Stock price must be positive.");
            }
            this.symbol = symbol;
            this.price = price;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getPrice() {
            return price;
        }

        public void updatePrice() {
            this.price *= (0.95 + Math.random() * 0.1);
        }
    }

    static class Portfolio {
        private double balance;
        private final Map<String, Integer> holdings;
        private final Map<LocalDate, Double> performanceHistory;

        public Portfolio(double initialBalance) {
            if (initialBalance < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative.");
            }
            this.balance = initialBalance;
            this.holdings = new HashMap<>();
            this.performanceHistory = new HashMap<>();
        }

        public void buyStock(String symbol, int quantity, double price) {
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                return;
            }
            double cost = price * quantity;
            if (balance >= cost) {
                balance -= cost;
                holdings.put(symbol, holdings.getOrDefault(symbol, 0) + quantity);
                System.out.println("Bought " + quantity + " shares of " + symbol);
            } else {
                System.out.println("Insufficient funds to buy " + quantity + " shares of " + symbol);
            }
        }

        public void sellStock(String symbol, int quantity, double price) {
            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                return;
            }
            if (holdings.containsKey(symbol) && holdings.get(symbol) >= quantity) {
                balance += price * quantity;
                holdings.put(symbol, holdings.get(symbol) - quantity);
                if (holdings.get(symbol) == 0) {
                    holdings.remove(symbol);
                }
                System.out.println("Sold " + quantity + " shares of " + symbol);
            } else {
                System.out.println("Insufficient shares to sell " + quantity + " shares of " + symbol);
            }
        }

        public void viewPortfolio(Map<String, Stock> marketData) {
            System.out.println("Current Portfolio:");
            double totalValue = balance;
            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                String symbol = entry.getKey();
                int shares = entry.getValue();
                Stock stock = marketData.get(symbol);
                if (stock != null) {
                    double stockValue = shares * stock.getPrice();
                    totalValue += stockValue;
                    System.out.printf("%s: %d shares, Current Value: $%.2f%n", symbol, shares, stockValue);
                }
            }
            System.out.printf("Account Balance: $%.2f%n", balance);
            System.out.printf("Total Portfolio Value: $%.2f%n", totalValue);
            updatePerformanceHistory(totalValue);
        }

        private void updatePerformanceHistory(double totalValue) {
            LocalDate today = LocalDate.now();
            performanceHistory.put(today, totalValue);
        }

        public void viewPerformance() {
            System.out.println("Performance History:");
            if (performanceHistory.isEmpty()) {
                System.out.println("No performance history available.");
                return;
            }
            LocalDate firstDate = performanceHistory.keySet().iterator().next();
            double initialValue = performanceHistory.get(firstDate);
            LocalDate lastDate = performanceHistory.keySet().stream().reduce((first, second) -> second).orElse(firstDate);
            double currentValue = performanceHistory.get(lastDate);

            for (Map.Entry<LocalDate, Double> entry : performanceHistory.entrySet()) {
                System.out.printf("%s: $%.2f%n", entry.getKey(), entry.getValue());
            }

            double returnPercentage = ((currentValue - initialValue) / initialValue) * 100;
            System.out.printf("Overall Return: %.2f%%%n", returnPercentage);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map<String, Stock> marketData = new HashMap<>();
        marketData.put("APPLE", new Stock("TATA STEEL", 150.00));
        marketData.put("GOOGLE", new Stock("ICICI BANK", 2800.00));
        marketData.put("AMAZON", new Stock("WIPRO", 3400.00));
        marketData.put("MSFT", new Stock("MSFT", 300.00));

        Portfolio portfolio = new Portfolio(10000.00);

        while (true) {
            displayMenu();

            int choice = getUserChoice(scanner);
            if (choice == -1) continue;

            switch (choice) {
                case 1:
                    viewMarketData(marketData);
                    break;
                case 2:
                    handleBuyStock(scanner, marketData, portfolio);
                    break;
                case 3:
                    handleSellStock(scanner, marketData, portfolio);
                    break;
                case 4:
                    portfolio.viewPortfolio(marketData);
                    break;
                case 5:
                    portfolio.viewPerformance();
                    break;
                case 6:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("\n--- Stock Trading Platform ---");
        System.out.println("1. View Market Data");
        System.out.println("2. Buy Stock");
        System.out.println("3. Sell Stock");
        System.out.println("4. View Portfolio");
        System.out.println("5. View Performance");
        System.out.println("6. Exit");
        System.out.print("Choose an option: ");
    }

    private static int getUserChoice(Scanner scanner) {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
            return -1;
        }
    }

    private static void viewMarketData(Map<String, Stock> marketData) {
        System.out.println("Market Data:");
        for (Stock stock : marketData.values()) {
            stock.updatePrice();
            System.out.printf("%s: $%.2f%n", stock.getSymbol(), stock.getPrice());
        }
    }

    private static void handleBuyStock(Scanner scanner, Map<String, Stock> marketData, Portfolio portfolio) {
        System.out.print("Enter stock symbol to buy: ");
        String symbol = scanner.next().toUpperCase();
        if (marketData.containsKey(symbol)) {
            System.out.print("Enter quantity to buy: ");
            int quantity = getPositiveInteger(scanner);
            portfolio.buyStock(symbol, quantity, marketData.get(symbol).getPrice());
        } else {
            System.out.println("Invalid stock symbol.");
        }
    }

    private static void handleSellStock(Scanner scanner, Map<String, Stock> marketData, Portfolio portfolio) {
        System.out.print("Enter stock symbol to sell: ");
        String symbol = scanner.next().toUpperCase();
        if (portfolio.holdings.containsKey(symbol)) {
            System.out.print("Enter quantity to sell: ");
            int quantity = getPositiveInteger(scanner);
            portfolio.sellStock(symbol, quantity, marketData.get(symbol).getPrice());
        } else {
            System.out.println("You do not own any shares of " + symbol);
        }
    }

    private static int getPositiveInteger(Scanner scanner) {
        while (true) {
            try {
                int value = scanner.nextInt();
                if (value > 0) {
                    return value;
                } else {
                    System.out.print("Please enter a positive number: ");
                }
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.next();
            }
        }
    }
}
