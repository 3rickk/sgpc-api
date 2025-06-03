# Use a imagem oficial do OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia os arquivos do projeto
COPY sgpc-api/pom.xml .
COPY sgpc-api/src ./src

# Instala Maven e compila o projeto
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    apk del maven && \
    rm -rf /root/.m2

# Expõe a porta
EXPOSE 8000

# Executa a aplicação
CMD ["java", "-jar", "target/sgpc-api-0.0.1-SNAPSHOT.jar"]
