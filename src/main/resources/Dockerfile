FROM alpine:3.17.2

RUN apk --no-cache add ca-certificates curl jq openjdk17-jre \
 && mkdir -p /opt/kquiet/${project.artifactId}/ext \
 && addgroup kquiet && adduser -G kquiet -D kquiet \
 && chown -R kquiet:kquiet /opt/kquiet/

WORKDIR /opt/kquiet/${project.artifactId}

COPY --chown=kquiet:kquiet ["hecate.sh", "./"]

COPY --chown=kquiet:kquiet ["${project.build.finalName}.jar", "lib", "application.yml", "logback-spring.xml", "./ext/"]

USER kquiet:kquiet

ENTRYPOINT ["./hecate.sh"]
