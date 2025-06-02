# Use a imagem oficial do OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Define o diretório de trabalho no container
WORKDIR /app

# Copia o arquivo pom.xml e os arquivos do projeto
COPY sgpc-api/pom.xml .
COPY sgpc-api/src ./src

# Instala o Maven e compila a aplicação
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests && \
    rm -rf /root/.m2 && \
    apk del maven

# Expõe a porta 8080
EXPOSE 8080

# Define o comando para executar a aplicação
CMD ["java", "-jar", "target/sgpc-api-0.0.1-SNAPSHOT.jar"] 