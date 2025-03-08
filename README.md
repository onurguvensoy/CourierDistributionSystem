# Courier Distribution System

A real-time courier tracking and distribution management system with GPS integration, built using Spring Boot and React.

##  Features

- **Live Tracking**
  - Real-time GPS tracking using Leaflet maps
  - WebSocket integration for live updates
  - Interactive map interface

- **User System**
  - Secure JWT authentication
  - Role-based access control
  - User management interface

- **Distribution Management**
  - Real-time courier status
  - Package tracking
  - Delivery management

##  Tech Stack

### Backend
- Spring Boot 3.2.3
- Spring Security with JWT
- Spring WebSocket
- Spring Data JPA
- Redis for caching
- H2 Database
- Lombok
- Maven

### Frontend
- React 19
- TypeScript
- Vite 6
- Leaflet for maps
- Ant Design
- Redux Toolkit
- WebSocket (SockJS + STOMP)
- Axios
- React Bootstrap

##  Prerequisites

- Java 17 or higher
- Node.js 18+ and npm
- Redis server (for caching)
- IDE (IntelliJ IDEA recommended)

##  Getting Started

### Backend Setup

1. Clone the repository
```bash
git clone 
cd CourierDistributionSystem
```

2. Start Redis server (required for caching)

3. Navigate to backend directory and run:
```bash
cd backend
./mvnw clean install
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start development server:
```bash
npm run dev
```

Frontend will be available at `http://localhost:5173`

##  Project Structure

```
CourierDistributionSystem/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   ├── data/           # Database files
│   ├── logs/           # Application logs
│   └── pom.xml
├── frontend/
│   ├── src/
│   ├── public/
│   ├── mock/          # Mock GPS data
│   ├── package.json
│   └── vite.config.ts
└── .mvn/              # Maven wrapper
```

##  Configuration

### Backend Configuration
Application properties can be configured in `backend/src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:file:./data/courierdb
spring.jpa.hibernate.ddl-auto=update

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379

# JWT Configuration
jwt.secret=your-secret-key
jwt.expiration=86400000
```

### Frontend Configuration
Environment variables can be set in `.env`:

```env
VITE_API_URL=http://localhost:8080/api
VITE_WS_URL=http://localhost:8080/ws
```

##  Available Scripts

### Backend
- `./mvnw clean install` - Build the project
- `./mvnw spring-boot:run` - Run the application
- `./mvnw test` - Run tests

### Frontend
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run lint` - Lint code
- `npm run preview` - Preview production build

##  Security

- JWT-based authentication
- Spring Security integration
- CORS configuration
- WebSocket security
- Redis session management

##  Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
