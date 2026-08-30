# Travel Planner - Spring Boot Application

A Spring Boot REST API backend with a web-based frontend for managing travel plans, itineraries, expenses, and destination information. Data is persisted in a PostgreSQL database.

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
│       │   ├── repository/
│       │   │   └── TripRepository.java            # Spring Data JPA repository
│       │   └── model/
│       │       ├── Trip.java                      # Trip JPA entity
│       │       ├── ItineraryItem.java             # ItineraryItem JPA entity
│       │       ├── Expense.java                   # Expense JPA entity
│       │       └── DestinationInfo.java           # Embeddable destination info
│       └── resources/
│           ├── static/
│           │   ├── index.html                     # Frontend HTML (served by Spring Boot)
│           │   ├── css/styles.css                 # Frontend styles
│           │   └── js/app.js                      # Frontend JavaScript
│           └── application.properties             # Spring configuration
├── docker-compose.yml                              # PostgreSQL Docker container
└── pom.xml                                         # Maven configuration
```

## Technology Stack

- **Backend**: Spring Boot 3.4.1
- **Java Version**: 25
- **Build Tool**: Maven
- **Database**: PostgreSQL 16 (via Docker)
- **ORM**: Spring Data JPA / Hibernate
- **Frontend**: HTML, CSS, JavaScript (Vanilla)

## Getting Started

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- Docker & Docker Compose

### Running the Application

**1. Start the PostgreSQL database:**

```bash
docker-compose up -d
```

This starts PostgreSQL on port 5432. Data is persisted in a Docker volume.

**2. Build and run the application:**

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

**3. Access the application:**

Open your browser and navigate to:

```
http://localhost:8080
```

The application auto-creates database tables on first startup via Hibernate's `ddl-auto=update`.

**4. Stop the database (when done):**

```bash
docker-compose down
```

The data volume is preserved. To also delete all data:

```bash
docker-compose down -v
```

## API Endpoints

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

## Specs

Features are specified before implementation using a spec-driven development
workflow. See [`specs/README.md`](specs/README.md) for the process and
[`specs/000-template.md`](specs/000-template.md) for the template.

| # | Spec |
|---|------|
| 001 | Account & password management |
| 002 | Trip editing (full CRUD) |
| 003 | Expense analytics & category breakdown |
| 004 | Dashboard search, sort & filter |
| 005 | Packing checklist |
| 006 | Collaborative trip sharing |
| 007 | Email reminders for upcoming trips |
| 008 | Document attachments |
| 009 | Live destination information (weather & currency APIs) |
| 010 | API documentation (OpenAPI) + data import/export |

## Data Storage

The application uses **PostgreSQL** running in a Docker container with **Spring Data JPA** for persistence. All trips, itinerary items, and expenses are stored in the database and survive application restarts.

Database tables are auto-generated from JPA entity annotations. Key relationships:

- `trips` table — stores trip metadata and embedded destination info
- `itinerary_items` table — `@OneToMany` from trips, cascaded deletes
- `expenses` table — `@OneToMany` from trips, cascaded deletes

### Database Configuration

Default credentials (see `application.properties` and `docker-compose.yml`):

| Setting   | Value            |
|-----------|------------------|
| Host      | `localhost:5432` |
| Database  | `travelplanner`  |
| Username  | `travelplanner`  |
| Password  | `travelplanner`  |

## License

This project is open source and available under the MIT License.
