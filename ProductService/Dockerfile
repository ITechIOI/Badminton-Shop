# -------- STAGE 1: BUILD WITH MAVEN + JDK 21 --------
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml trước để cache dependency layer
COPY pom.xml .

# Tải dependencies sớm để dùng cache
RUN mvn dependency:go-offline -B

# Copy toàn bộ source sau (chỉ mất cache nếu source đổi)
COPY . .

# Build project (bỏ test để nhanh)
RUN mvn clean package -DskipTests

# -------- STAGE 2: RUNTIME WITH JDK 21 (lightweight) --------
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Copy built JAR từ builder
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]
