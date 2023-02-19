#!/bin/sh

exec java -javaagent:opentelemetry-javaagent.jar -Dspring.profiles.active=jsonlog -Dotel.exporter.otlp.endpoint=${otlp_endpoint:-http://localhost:4317} -Dotel.resource.attributes=service.name=${project.artifactId},service.version=${project.version} -cp "lib/:lib/*:ext/:ext/*" org.kquiet.hecate.Launcher
