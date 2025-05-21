# syntax=docker/dockerfile:1

########################################
# 1. Build stage: install dependencies #
########################################
FROM python:3.11-slim AS builder

# Set working directory for dependency installation
WORKDIR /install

# Copy only requirements to leverage Docker cache
COPY requirements.txt .

# Install Python dependencies to a separate location
RUN pip install --prefix=/install --no-cache-dir -r requirements.txt

########################################
# 2. Final stage: package the app    #
########################################
FROM python:3.11-slim

# Create app directory
WORKDIR /app

# Copy installed dependencies from builder stage
COPY --from=builder /install /usr/local

# Copy application code
# Assumes your FastAPI app is under ./app directory
COPY ./app ./app

# Expose FastAPI default port
EXPOSE 8008

# Default command to run the application with Uvicorn
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8008"]
