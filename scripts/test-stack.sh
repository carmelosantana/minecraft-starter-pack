#!/usr/bin/env bash
# Thin wrapper around the shared rig. Deliberately contains no version, port,
# or JAR path -- everything that used to rot lives in the shared rig now.
# See minecraft-plugin-docs/bin/xpfarm-test-stack for usage and the four
# silent failure modes it encodes.
exec /home/carmelo/Projects/Minecraft/Plugins/minecraft-plugin-docs/bin/xpfarm-test-stack "$@"
