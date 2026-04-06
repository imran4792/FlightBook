# Use Java 17
FROM openjdk:17-jdk-slim

# Copy project files
WORKDIR /app
COPY . .

# Give permission to mvnw
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Run the app
CMD ["java", "-jar", "target/*.jar"]
