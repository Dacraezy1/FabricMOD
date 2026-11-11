AimAssist (Fabric, Minecraft Java 26.3)

BUILD: push to GitHub -> Actions tab -> "Build AimAssist JAR" -> download AimAssist-JAR artifact.
       (or locally: gradle build, jar is build/libs/aimassist-1.0.0.jar)
INSTALL: mods folder + Fabric Loader 0.19.5+ + Fabric API for 26.3.

COMMANDS (in game chat)
  /aimassist                 status
  /aimassist toggle          on/off
  /aimassist range 6         1-12 blocks
  /aimassist fov 120         10-360 degrees, targets must be inside this cone
  /aimassist speed 35        1-100, % of the remaining angle turned per tick (100 = instant)
  /aimassist hold true       true = aim only while holding attack, false = always
  /aimassist mobs true       hostile mobs
  /aimassist players false   players
  /aimassist animals false   passive animals
  /aimassist los true        require line of sight

Works great together with HoldClicker. Config: config/aimassist.json
