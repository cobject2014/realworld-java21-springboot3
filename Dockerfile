# Multi-stage Dockerfile for GraalVM Native Image on Rocky Linux 9

# Stage 1: Build native image on Rocky Linux 9
FROM xik8mu0dzy0zl4.xuanyuan.run/rockylinux:9.3 AS builder

# Install required build tools
RUN dnf install -y \
    gcc \
    gcc-c++ \
    glibc-devel \
    zlib-devel \
    tar \
    gzip \
    && dnf clean all

# Install GraalVM for Java 17 from local file
ENV GRAALVM_HOME=/opt/graalvm
COPY graalvm-jdk-17.0.17_linux-x64_bin.tar.gz .
RUN mkdir -p ${GRAALVM_HOME} \
    && tar -xzf graalvm-jdk-17.0.17_linux-x64_bin.tar.gz -C ${GRAALVM_HOME} --strip-components=1 \
    && rm graalvm-jdk-17.0.17_linux-x64_bin.tar.gz

ENV JAVA_HOME=${GRAALVM_HOME}
ENV PATH=${GRAALVM_HOME}/bin:${PATH}

# Set working directory
WORKDIR /build

# Copy Gradle wrapper and build files first to leverage Docker cache
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./

# Download dependencies
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew dependencies --no-daemon

# Copy source code
COPY module ./module
COPY server ./server

# Build native image (skip tests as they were already run)
RUN ./gradlew :realworld:nativeCompile -x test --no-daemon

# Stage 2: Create minimal runtime image
FROM rockylinux:9-minimal

# Install only runtime dependencies
RUN microdnf install -y \
    glibc \
    zlib \
    libstdc++ \
    && microdnf clean all

# Create non-root user
RUN useradd -m -u 1001 -s /sbin/nologin appuser

# Set working directory
WORKDIR /app

# Copy the native executable from builder
COPY --from=builder /build/server/api/build/native/nativeCompile/realworld /app/realworld

# Change ownership
RUN chown -R appuser:appuser /app && chmod +x /app/realworld

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Health check (using direct port check since native image has fast startup)
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD timeout 2 bash -c '</dev/tcp/localhost/8080' || exit 1

# Run the native application
ENTRYPOINT ["/app/realworld"]
