
# Autoverwaltung – Kafka-based Microservices

Dieses Projekt beinhaltet zwei Quarkus-Microservices, die per Kafka miteinander kommunizieren. Einer der Services (Auto-Service) bindet eine Datenbank an und kann Autos verwalten; der andere Service (Validation-Service) validiert ankommende Autos und sendet ein Ergebnis zurück.

## Inhaltsverzeichnis

1.  [Überblick](#%C3%BCberblick)
2.  [Architektur](#architektur)
3.  [Voraussetzungen](#voraussetzungen)
4.  [Lokaler Start der Umgebung](#lokaler-start-der-umgebung)
    1.  [Kafka & Datenbank starten](#kafka--datenbank-starten)
    2.  [Docker Images bauen und starten](#docker-images-bauen-und-starten)
5.  [Services testen](#services-testen)
6.  [Nützliche Kommandos](#n%C3%BCtzliche-kommandos)
7.  [Wichtigste Endpunkte und Use-Case](#wichtigste-endpunkte-und-use-case)
8.  [Pushing zu GHCR](#pushing-zu-ghcr)
9.  [Lizenz & Info](#lizenz--info)

----------

## Überblick

Wir haben zwei Services:

1.  **Auto-Service** (`auto-service/`):
    
    -   Bietet REST-Endpoints an, um Autos zu erzeugen, abzufragen usw.
    -   Sendet „ValidationRequests“ per Kafka an den Validation-Service.
    -   Empfängt das „ValidationResponse“ und speichert ggf. Resultate in der Datenbank.
2.  **Validation-Service** (`validation-service/`):
    
    -   Lauscht per Kafka auf neue Anfragen zur Auto-Validierung.
    -   Prüft, ob die Daten regelkonform sind.
    -   Sendet das Validation-Ergebnis wieder per Kafka zurück.

Der _Auto-Service_ bindet eine Datenbank an (z. B. PostgreSQL oder H2). Im Code ist standardmäßig eine Konfiguration hinterlegt (siehe `auto-service/src/main/resources/application.properties`).

----------

## Architektur

-   Der Auto-Service schickt ValidationRequests an ein Kafka-Topic (z. B. `auto-validation-request`), der Validation-Service konsumiert diese.
-   Der Validation-Service schickt ein ValidationResponse an ein anderes Topic (z. B. `auto-validation-response`) zurück, Auto-Service wertet das aus und speichert Resultate in der DB.

----------

## Voraussetzungen

-   **Docker** und **Docker Compose** installiert.
-   Ein laufendes Docker-Setup (z. B. Docker Desktop).
-   Ggf. **Git** sowie (falls du lokal entwickeln möchtest) ein JDK + Maven.
-   Für das Deployment in GHCR brauchst du einen **GitHub-Account** (du bist `r33bickard`) und ein entsprechendes **Personal Access Token** (PAT) mit `write:packages`-Rechten.

----------

## Lokaler Start der Umgebung

### 1. Kafka & Datenbank starten

Option A: **Manuell** per Docker Compose  
Legen wir z. B. eine `docker-compose.yml` im Hauptverzeichnis an (Beispiel für Apache Kafka + Zookeeper + Postgres):

`version: '3.7'
services:
  zookeeper:
    image: bitnami/zookeeper:latest
    environment:
      - ZOO_ENABLE_AUTH=no
    ports:
      - "2181:2181"

  kafka:
    image: bitnami/kafka:latest
    environment:
      - KAFKA_BROKER_ID=1
      - KAFKA_ZOOKEEPER_CONNECT=zookeeper:2181
      - KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092
      - KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"

  db:
    image: postgres:15-alpine
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=mysecret
      - POSTGRES_DB=autodb
    ports:
      - "5432:5432"` 

Starte alles mit:

`docker compose up -d` 

=> Du hast nun **Zookeeper**, **Kafka** (Port `9092`) und **Postgres** (Port `5432`) am Laufen.

Option B: **Eigene Kafka-Installation** / **H2**-Datenbank

-   Du kannst natürlich jede Kafka-Instanz verwenden und im `auto-service/application.properties` auf H2 umstellen, wenn du Postgres nicht nutzen möchtest. Dann ist die Compose-Konfiguration nur für Kafka erforderlich.

### 2. Docker Images bauen und starten

In jedem Microservice-Verzeichnis (`auto-service/`, `validation-service/`) liegen Dockerfiles. Du kannst sie entweder jeweils in ihrem Ordner bauen **oder** ein Dockerfile im Projekt-Root verwenden.

#### Beispiel: Auto-Service

`cd auto-service
docker build -t ghcr.io/r33bickard/auto-service:1.0.0 .
docker run -d -p 8080:8080 --name auto-service \
  -e QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://host.docker.internal:5432/autodb" \
  -e QUARKUS_DATASOURCE_USERNAME=postgres \
  -e QUARKUS_DATASOURCE_PASSWORD=mysecret \
  ghcr.io/r33bickard/auto-service:1.0.0` 

(Den `host.docker.internal` passt du ggf. an, falls unter Linux Docker anders konfiguriert ist.)

#### Beispiel: Validation-Service

`cd ../validation-service
docker build -t ghcr.io/r33bickard/validation-service:1.0.0 .
docker run -d -p 8081:8080 --name validation-service \
  ghcr.io/r33bickard/validation-service:1.0.0` 

Jetzt läuft der Validation-Service auf `localhost:8081`, der Auto-Service auf `localhost:8080`. Kafka ist erreichbar unter `localhost:9092` (sofern Docker Compose oben läuft).

----------

## Services testen

1.  **Check**: Läuft der Auto-Service? ⇒ `http://localhost:8080/q/health/ready`
2.  **Check**: Läuft der Validation-Service? ⇒ `http://localhost:8081/q/health/ready`
3.  **Erstelle** ein Auto, das intern einen Kafka-Request anstößt (z. B. via Postman oder `curl`).
4.  **Schau** ins Log vom Validation-Service, ob eine Validierung stattgefunden hat.
5.  **Schau** ins Log vom Auto-Service, ob die Validierungsantwort angekommen ist und in der Datenbank gespeichert wurde.

----------

## Nützliche Kommandos

**Logs ansehen**:

`docker logs -f auto-service
docker logs -f validation-service` 

**Services stoppen**:

`docker stop auto-service validation-service
docker rm auto-service validation-service` 

**Kafka CLI / Producer / Consumer** (z. B. im Container `kafka`), um Topics zu inspizieren:

`docker exec -it <kafka-container> bash
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic-name> --from-beginning` 

----------

## Wichtigste Endpunkte und Use-Case

### Auto-Service

-   **Endpoint** zum Anlegen eines Autos (Beispiel):
    
    -   `POST /auto`
    -   Body (JSON):
                
        `{
          "brand": "VW",
          "model": "Golf",
          "year": 2021
        }` 
        
    -   Effekt: Auto wird in der DB gespeichert (zunächst evtl. mit `status = PENDING_VALIDATION`), anschließend geht eine `ValidationRequest` per Kafka zum Validation-Service.
-   **Endpoint** zum Abfragen aller Autos:
    
    -   `GET /auto`
    -   Liefert eine Liste aller Autos inkl. Validierungsstatus.

### Validation-Service

-   Liest automatisch vom Kafka-Topic (z. B. `auto-validation-request`).
-   Führt Validierungen aus, z. B. ob Marke/Modell/Jahr legal sind.
-   Sendet das Ergebnis (OK oder FAIL) per Kafka an `auto-validation-response`.

### Ablauf

1.  **User** ruft `/auto` (POST) auf →
2.  **Auto-Service** legt Datensatz an, schickt Kafka-Message →
3.  **Validation-Service** empfängt Kafka-Message, validiert →
4.  **Validation-Service** schickt Kafka-Response →
5.  **Auto-Service** aktualisiert das Auto in der DB mit Validation-Ergebnis.
