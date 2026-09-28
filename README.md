# Concurrent Application Server

Implementación del framework propio para el **Repositorio 2** del taller de virtualización. No usa Spring ni dependencias de frameworks web. `MiniHttpServer` es el framework: registra rutas GET mediante una API fluida y delega cada solicitud a un handler.

## Estado actual

- `MiniHttpServer` registra rutas con `get(path, handler)` y entrega datos de consulta a través de `Request`.
- Atiende solicitudes concurrentes mediante un executor de *virtual threads* de Java 21: cada solicitud puede ejecutarse independientemente sin bloquear el hilo principal del servidor.
- Expone `GET /greeting?name=<nombre>` y `GET /health`.
- Lee el puerto de la variable de entorno `PORT`; el valor por defecto es `6000`.
- Ejecuta un apagado ordenado al recibir `SIGTERM`/`Ctrl+C`: deja terminar solicitudes en curso hasta 10 segundos y después cierra el executor.
- Se empaqueta y ejecuta en una imagen Docker basada en Amazon Corretto 21.

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop (para la ejecución en contenedor)

## Construir y ejecutar localmente

```powershell
mvn clean package
$env:PORT=6000
java -jar target/concurrent-application-server-1.0.0.jar
```

En otra terminal:

```powershell
curl "http://localhost:6000/greeting?name=Pedro"
curl "http://localhost:6000/health"
```

Respuesta esperada: `Hello, Pedro!` y `UP`.

Para usar otro puerto:

```powershell
$env:PORT=8080
java -jar target/concurrent-application-server-1.0.0.jar
```

## Docker

```powershell
docker build -t <dockerhub-user>/concurrent-application-server:1.0 .
docker run -d --name concurrent-server -e PORT=6000 -p 34000:6000 <dockerhub-user>/concurrent-application-server:1.0
curl "http://localhost:34000/greeting?name=Container"
```

Para detener el contenedor ordenadamente, Docker envía `SIGTERM`; el *shutdown hook* registra el cierre y da un periodo de gracia de 10 segundos:

```powershell
docker stop concurrent-server
docker logs concurrent-server
```

## Despliegue en EC2

En una instancia Amazon Linux 2023 con Docker instalado:

```bash
docker pull <dockerhub-user>/concurrent-application-server:1.0
docker run -d --name concurrent-server --restart unless-stopped \
  -e PORT=6000 -p 8080:6000 \
  <dockerhub-user>/concurrent-application-server:1.0
```

Abra el puerto `8080` en el security group únicamente para la red necesaria y compruebe:

```text
http://<ec2-public-dns>:8080/greeting?name=AWS
```

## Evidencia de progreso

El commit de esta extensión debe registrar la incorporación del manejo concurrente, la configuración `PORT`, el apagado ordenado y Docker. Tras publicar el repositorio, agregue aquí el hash y enlace del commit, además de capturas o video de Docker y EC2.
