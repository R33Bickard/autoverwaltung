# Autoverwaltungssystem

Ein Microservice-basiertes System zur Verwaltung von Autos mit Validierungslogik. Das System benutzt Quarkus, Kafka und PostgreSQL und kann mit Docker Compose gestartet werden.

## Systemarchitektur

Das System besteht aus zwei Microservices:

1. **Auto-Service**: Verwaltet Autos in einer PostgreSQL-Datenbank
2. **Validierungs-Service**: Validiert Autoinformationen (Kennzeichen, Baujahr)

Die Services kommunizieren über Kafka-Topics und werden von einem Redpanda-Kafka-Broker unterstützt.

## Voraussetzungen

- Docker und Docker Compose
- Java 17+ und Maven (für die Entwicklung)
- Git (für das Klonen des Repositories)

## Schnellstart

```bash
# Repository klonen
git clone https://github.com/[username]/autoverwaltung.git
cd autoverwaltung

# System starten
docker-compose up -d

# Überprüfen ob alle Services laufen
docker-compose ps
```

## Entwicklungsumgebung

### Auto-Service starten

```bash
cd auto-service
./mvnw compile quarkus:dev
```

### Validation-Service starten

```bash
cd validation-service
./mvnw compile quarkus:dev
```

## API-Endpunkte

### Auto-Service (http://localhost:8080)

- `GET /autos` - Alle Autos abrufen
- `GET /autos/{id}` - Ein Auto abrufen
- `POST /autos` - Ein neues Auto anlegen
- `PUT /autos/{id}` - Ein Auto aktualisieren
- `DELETE /autos/{id}` - Ein Auto löschen

### Validation-Service (http://localhost:8082)

- `GET /validation` - Status des Validation-Service
- `GET /validation/rules` - Aktuelle Validierungsregeln

## Beispiel: Auto erstellen

```bash
curl -X POST -H "Content-Type: application/json" -d '{
  "marke": "BMW",
  "modell": "X5",
  "baujahr": 2020,
  "kennzeichen": "M-AB 123",
  "farbe": "schwarz"
}' http://localhost:8080/autos
```

## Kafka-Topics

- `auto-validation-request`: Anfragen zur Validierung von Autos
- `auto-validation-response`: Antworten von Validierungen

## Monitoring

### Redpanda Console

Die Redpanda-Console ist unter http://localhost:8081 verfügbar und zeigt alle Kafka-Topics und Nachrichten.

## Datenbankzugriff

Die PostgreSQL-Datenbank ist unter localhost:5432 erreichbar. Verbindungsdaten:
- Datenbank: autodb
- Benutzer: postgres
- Passwort: postgres

## Containerisierung

Alle Services werden als Docker-Container bereitgestellt. Die Images können mit folgendem Befehl gebaut werden:

```bash
docker-compose build
```

## Fehlerbehebung

**Problem: Services können nicht mit Kafka verbinden**
- Überprüfe, ob der Redpanda-Container läuft: `docker-compose ps`
- Überprüfe die Logs: `docker-compose logs redpanda-1`

**Problem: Datenbank ist nicht erreichbar**
- Überprüfe, ob der PostgreSQL-Container läuft: `docker-compose ps`
- Überprüfe die Logs: `docker-compose logs postgres`