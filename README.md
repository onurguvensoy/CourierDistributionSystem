# Courier Distribution System

A modern full-stack application for managing courier delivery operations and optimizing distribution logistics. This system helps courier companies streamline their delivery processes, track packages in real-time, and improve overall operational efficiency.

## 🌟 Features

- **Real-time Package Tracking**
  - Live GPS tracking of couriers
  - Package status updates
  - Delivery confirmation system

- **Route Optimization**
  - Smart route planning
  - Traffic-aware delivery scheduling
  - Multi-stop journey optimization

- **User Management**
  - Customer accounts
  - Courier/Driver profiles
  - Admin dashboard

- **Analytics & Reporting**
  - Delivery performance metrics
  - Route efficiency analysis
  - Cost optimization reports

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring Security
- PostgreSQL
- Maven
- JUnit 5

### Frontend
- React
- TypeScript
- Vite
- Material-UI
- React Query
- React Router
- Axios

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- Java 17 or higher
- Node.js 18+ and npm/yarn
- PostgreSQL 14+
- Maven 3.6+

## 🚀 Getting Started

### Backend Setup

1. Clone the repository:
```bash
git clone [repository-url]
cd CourierDistributionSystem
```

2. Navigate to the backend directory:
```bash
cd backend
```

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The backend server will start at `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend application will be available at `http://localhost:5173`

## 🔧 Configuration

### Backend Configuration

Create `application.properties` in `backend/src/main/resources/`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/courier_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### Frontend Configuration

Create `.env` file in the frontend directory:

```env
VITE_API_URL=http://localhost:8080/api
```

## 📱 API Documentation

The API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## 🧪 Testing

### Backend Tests
```bash
cd backend
./mvnw test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## 📦 Project Structure

```
courier-distribution-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   └── services/
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

## 🔐 Security

- JWT-based authentication
- Role-based access control
- HTTPS encryption
- Input validation
- XSS protection
- CSRF protection

## 🌐 Deployment

### Backend Deployment
1. Build the JAR file:
```bash
./mvnw clean package
```

2. Run the application:
```bash
java -jar target/courier-distribution-system.jar
```

### Frontend Deployment
1. Build the production version:
```bash
npm run build
```

2. Deploy the contents of the `dist` directory to your web server

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- Your Name - *Initial work* - [YourGithub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Hat tip to anyone whose code was used
- Inspiration
- etc

## 📞 Support

For support, email your-email@example.com or create an issue in the repository. 