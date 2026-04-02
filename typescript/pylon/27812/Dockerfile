# Build stage
FROM node:20-bullseye AS builder

WORKDIR /app

# Copy package files
COPY package.json package-lock.json* ./
COPY tsconfig.json ./

# Install dependencies
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

# Copy source code
COPY src ./src

# Build TypeScript
RUN npm run build

# Production stage
FROM node:20-bullseye

WORKDIR /app

# Copy package files
COPY package.json package-lock.json* ./

# Install production dependencies only
RUN if [ -f package-lock.json ]; then npm ci --only=production; else npm install --only=production; fi && npm cache clean --force

# Copy built files from builder
COPY --from=builder /app/lib ./lib

# Expose any ports if needed (adjust based on your app)
# EXPOSE 3000

# Default command (can be overridden in docker-compose)
CMD ["node", "lib/worker.js"]

