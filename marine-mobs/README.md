# FreeLifeMarineMobs

A separate Spigot 1.21.1 plugin that adds command-only rideable shark and orca entities with 10 health.

## What is rendered

Pure Spigot cannot send an arbitrary downloaded OBJ/GLTF mesh to an unmodified vanilla client. This plugin therefore does **not** redistribute or import a third-party mesh. It builds a new low-poly approximation from vanilla `BlockDisplay` entities, so no resource pack or client mod is required.

The proportions and silhouette were visually referenced from these public 3D model pages:

- Shark by Quaternius on Poly Pizza — Public Domain (CC0): https://poly.pizza/m/YYsK3gRCBZ
- Orca by Poly by Google on Poly Pizza — Creative Commons Attribution: https://poly.pizza/m/5p9B6IebY-A

No mesh, texture, animation, or other third-party asset is included in this repository or JAR.

## Requirements

- Java 21
- Spigot 1.21.1

## Commands

```text
/marine spawn shark
/marine spawn orca
```

Permission:

```text
freelifemarine.spawn
```

The permission defaults to server operators. There is no natural spawning, spawn egg, crafting recipe, or automatic generation path.

## Behavior

- Each shark/orca starts with exactly 10 plugin-managed health.
- Right-click the entity's hitbox to mount it.
- While mounted, it moves in the direction the rider is looking.
- Sneak dismount uses Minecraft's normal passenger behavior.
- The visual body is made from vanilla display entities and follows an invisible server-side carrier.
- Marine mobs created by this plugin are removed on plugin/server shutdown rather than persisted incompletely across restarts.

## Exact imported 3D meshes

Displaying the exact downloaded Poly Pizza OBJ/GLTF mesh is not possible with a Spigot JAR alone on a vanilla client. An exact custom mesh would require a resource pack or another client-visible model system. The implementation in this module is the Spigot-only fallback: a low-poly 3D approximation made from vanilla display entities.

## Build

```bash
mvn -B -f marine-mobs/pom.xml verify
```

Output:

```text
marine-mobs/target/FreeLifeMarineMobs-1.0.0-Spigot-1.21.1.jar
```
