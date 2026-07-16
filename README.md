# Travel Planner - Spring Boot Application

This is a Spring Boot implementation of the Travel Planner application. It provides a REST API backend with a web-based frontend for managing travel plans, itineraries, expenses, and destination information.

## Project Structure

```
Travel Planner/
├── src/
│   └── main/
│       ├── java/com/example/travelplanner/
│       │   ├── TravelPlannerApplication.java      # Main Spring Boot Application
│       │   ├── controller/
│       │   │   └── TripController.java            # REST API endpoints
│       │   ├── service/
│       │   │   └── TripService.java               # Business logic
│       │   └── model/
│       │       ├── Trip.java                      # Trip entity
│       │       ├── ItineraryItem.java             # Itinerary item entity
│       │       ├── Expense.java                   # Expense entity
│       │       └── DestinationInfo.java           # Destination information entity
│       └── resources/
│           ├── static/
│           │   ├── index.html                     # Frontend HTML
│           │   ├── css/styles.css                 # Frontend styles
│           │   └── js/app.js                      # Frontend JavaScript
│           └── application.properties             # Spring configuration
└── pom.xml                                         # Maven configuration
```

## Technology Stack

- **Backend**: Spring Boot 3.2.7
- **Java Version**: 25
- **Build Tool**: Maven
- **Frontend**: HTML, CSS, JavaScript (Vanilla)

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+ (or use the Maven wrapper if available)

### Running the Application

1. **Navigate to the project directory:**
   ```bash
   cd "c:\Users\Dominik\Projekty Demo\Travel Planner"
   ```

2. **Build the project:**
   ```bash
   mvn clean package
   ```

3. **Run the Spring Boot application:**
   ```bash
   mvn spring-boot:run
   ```
   
   Or if a JAR file was built:
   ```bash
   java -jar target/travel-planner-0.0.1-SNAPSHOT.jar
   ```

4. **Access the application:**
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

## API Endpoints

The application provides the following REST API endpoints:

### Trips
- `GET /api/trips` - Get all trips
- `GET /api/trips/{tripId}` - Get a specific trip
- `POST /api/trips` - Create a new trip
- `DELETE /api/trips/{tripId}` - Delete a trip

### Itinerary
- `POST /api/trips/{tripId}/itinerary` - Add an itinerary item
- `DELETE /api/trips/{tripId}/itinerary/{itemId}` - Remove an itinerary item

### Expenses
- `POST /api/trips/{tripId}/expenses` - Add an expense
- `DELETE /api/trips/{tripId}/expenses/{expenseId}` - Remove an expense

### Destination Information
- `POST /api/trips/{tripId}/destination-info` - Load destination information
- `POST /api/trips/{tripId}/notes` - Save destination notes

## Features

- **Trip Management**: Create, view, and delete trips
- **Itinerary Planning**: Add activities with day and time information
- **Budget Tracking**: Monitor spending against your budget
- **Expense Management**: Track individual expenses by category
- **Destination Info**: Get weather, currency, and travel tips for popular destinations
- **Notes**: Save personal notes and tips for each destination
- **Data Export**: Export all trips as JSON

## Data Storage

Currently, the application stores data in-memory using a `LinkedHashMap`. This means data will be lost when the application restarts. For persistent storage, consider integrating a database like:
- H2 (for development)
- PostgreSQL
- MySQL
- MongoDB

## Development Notes

- The application uses Spring Boot's auto-configuration
- All business logic is encapsulated in the `TripService` class
- The frontend communicates with the backend via JSON REST APIs
- Static resources (HTML, CSS, JS) are served from `src/main/resources/static/`

## Building a JAR File

To create a standalone JAR file that can be run independently:

```bash
mvn clean package
java -jar target/travel-planner-0.0.1-SNAPSHOT.jar
```

The JAR file will include all dependencies and static resources.

## License

This project is open source and available under the MIT License.