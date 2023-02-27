#!/bin/sh

exec java -Dspring.profiles.active=jsonlog -cp "lib/:lib/*:ext/:ext/*" org.kquiet.hecate.Launcher
