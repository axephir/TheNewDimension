# The New Dimension

Mod RPG, exploration et automatisation pour **Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25**.
Nom du mod, de la dimension « Vestige » et des ressources : propositions de travail, modifiables avant une première publication.

**Version actuelle : 0.10.0-dev — L’âge industriel.**

[Télécharger le JAR NeoForge 26.2](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/tnd-0.10.0-dev.jar).
Placer ce fichier dans `mods/` avec **Minecraft Java 26.2 et NeoForge 26.2.0.93**, en remplaçant l’ancien JAR TND.

La **0.10** ajoute **neuf machines** : dynamo, contrôleur solaire, accumulateur, broyeur, four électrique, carrière, cultivateur, éleveur et station pastorale. Un réseau FE avec câbles alimente des chaînes de production et d’élevage. Les machines avancées coûtent des matériaux de Vestige ; la carrière exige un derrick de 37 charpentes et des trépans. **Inventaire → Guide → Industrie** explique les montages. Ces ajouts fonctionnent dans les sauvegardes existantes. [Guide industriel](docs/INDUSTRY_0.10.0.md).

Les neuf bâtiments apparaissent sur des massifs de scorie ferrenoire légèrement lumineuse, entourés d’une couronne de pics et accessibles par une montée pavée. Ils sont fermés, éclairés et aménagés : avant-poste, sanctuaire du portail, relais, archive, fonderie, sanctuaire de résonance, observatoire ancien, citadelle auréole et conservatoire d’écho. Les textures des blocs et des icônes restent en **16 × 16** ; les créatures et l’armure utilisent désormais des atlas **64 × 64** et des modèles avec davantage de relief. Des blocs supplémentaires, dont six variantes d’escaliers et de dalles, sont disponibles en survie au tailleur de pierre.

Dans l’inventaire, cliquer sur les onglets à gauche pour ouvrir les maîtrises, les expéditions et le guide, sans transporter de livre. Les minerais bleus affleurent sous l’eau dans tous les océans d’au moins huit blocs de profondeur ; les rouges occupent la première couche solide sous la lave de l’Overworld ; les verts apparaissent, plus rarement, contre les troncs des forêts, taïgas et jungles.

**Créer un monde de test ou explorer de nouveaux chunks** pour découvrir les nouvelles structures et les nouveaux minerais. Les bâtiments déjà enregistrés dans une sauvegarde conservent leur géométrie ; leurs textures sont actualisées. La génération conserve les constructions existantes. La carrière de la 0.10 creuse uniquement la zone choisie lorsque le joueur démarre explicitement son forage.

Le [compte rendu de cette version](docs/INDUSTRY_0.10.0.md) décrit les fonctionnalités et leur validation. Les quinze maîtrises ont des gains d’XP, des talents et une perception active. Réparer les consoles naturelles avec un éclat résonant donne des fragments de relique et améliore la cadence des extracteurs voisins. Les fresques révèlent des indices en cliquant dessus. Les recettes se débloquent dans le livre de recettes dès la découverte d’un éclat résonant. Le [cahier des charges original](docs/CAHIER_DES_CHARGES.md) reste la feuille de route pour étoffer les créatures, les donjons, l’industrie et le scénario final.

L’**Éclat errant** est un familier rare, apprivoisable avec un cristal composite : suivi, attente, soin, aide au combat et repérage du minerai résonant. La **sentinelle de faille** annonce sa zone d’attaque avant de frapper ; il est possible d’esquiver ou d’interrompre sa charge. Les agrégats d’écho produisent uniquement la nuit de Vestige. Leur poussière alimente un prisme révélant temporairement les ennemis proches. Voir les [captures en jeu](https://github.com/axephir/TheNewDimension/blob/downloads/GALLERY_0.9.0.md).

Le **sanctuaire de résonance** propose trois ailes de combat à activer avec de la poussière d’écho, des gardiens aux attaques annoncées et un trésor partagé unique. Le cœur récupéré sert à fabriquer l’**égide résonante**, qui donne deux cœurs temporaires. La progression des ailes est sauvegardée et les issues restent ouvertes. Les nouveaux sanctuaires naturels se trouvent dans les nouveaux chunks de Vestige.

Vestige possède maintenant une **forêt de verre**, des **bassins de cendre** et des **dunes vitrifiées**, avec leurs propres sols, formations et nappes sonores. La nuit et les lieux couverts ajoutent des couches graves ; des événements spatialisés plus rares ponctuent l’exploration. Les montées de maîtrise déclenchent un carillon et un effet de particules. [Écouter un extrait des sons](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/Vestige-0.6-audio-preview.ogg).

Après les trois épreuves du sanctuaire, **poussière d’écho en main + clic sur le noyau (coût : deux poussières)** éveille le **Conservateur brisé**. Esquivez fractures et couronne, puis frappez son cœur exposé. Sa matrice permet de fabriquer la **forge résonante**, qui transforme les métaux bruts et produit l’alliage des nouvelles pioche et lame. Les sanctuaires déjà terminés sont compatibles. [Guide de cette progression](docs/EXPEDITION_0.7.0.md).

Le nouvel onglet **Expéditions**, sous les maîtrises dans l’inventaire, propose **huit objectifs personnels**, leurs récompenses et un trophée final. Un grand **observatoire** ajoute une galerie, une bibliothèque et un mécanisme céleste à découvrir. Liez un **prisme de retour** à une ancre dans Vestige : maintenir cinq secondes permet de revenir contre deux poussières d’écho, avec une arrivée vérifiée et une recharge d’une minute. [Guide de la 0.8](docs/FIELDWORK_0.8.0.md).

La **0.9** ajoute deux grandes structures (**95 × 47 × 95** et **79 × 45 × 79**), un Lithogarde massif et un Traqueur de verre aux attaques annoncées, deux minerais exposés, neuf équipements dont une armure complète et un marteau à onde frontale, un séparateur de minerais et un récolteur nocturne. **30 blocs**, **16 nouveaux sons**, des animations de combat, des panneaux animés, une carte de découverte de biome et un guide depuis le troisième onglet de l’inventaire complètent cette version. [Guide de la 0.9](docs/AWAKENING_0.9.0.md).

## Développement

Installer un **JDK 25** et définir `JAVA_HOME`. Aucun autre mod n’est nécessaire.

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

Les GameTests utilisent exclusivement `run-gametest/`, séparé des parties client dans `run/` et du serveur normal dans `run-server/`. Le premier lancement d’un serveur normal demande de lire et accepter l’EULA Minecraft dans son répertoire d’exécution. Les processus doivent être redémarrés après restauration d’un environnement cloud.

Vérifications indépendantes du téléchargement de Minecraft :

```sh
bash scripts/test-core.sh
python3 scripts/check_resources.py
java scripts/CheckJavaSyntax.java
```

Pour vérifier les fichiers audio encodés, `python3 scripts/check_audio.py` demande ffmpeg. `python3 scripts/make_audio_preview.py` recrée le montage d’écoute.

La dernière commande ne vérifie que la syntaxe Java. Elle ne remplace ni la compilation ni les GameTests. `build` exécute aussi les régressions du noyau via la tâche `coreTest`.

Dans l’environnement Codex actuel :

```sh
bash scripts/setup-cloud.sh
python3 scripts/cloud_gradle.py runGameTestServer
```

Le script installe/réutilise un JDK Temurin épinglé avec vérification SHA-256, conserve les racines de confiance TLS et configure le proxy fourni par la plateforme. Il ne modifie pas les sources ni les fichiers de dépendances. Les domaines supplémentaires nécessaires sont décrits dans [CLOUD.md](docs/CLOUD.md).

## Contenu jouable

- Deux nouveaux minerais de Vestige, neuf équipements, deux machines et guide intégré ; détails dans le compte rendu 0.9.
- Dimension procédurale distincte de l’Overworld, six biomes silencieux, matériaux propres, horloge indépendante de 48 000 ticks et ambiance sonore originale.
- Avant-poste initial à environ 1 000 blocs du premier point d’arrivée d’un nouveau monde ; fresques, coffre et noyau de localisation.
- Trois minerais exposés associés aux fonds marins, aux fonds de lave et aux racines des arbres ; cristal composite, détecteur et recherche native des vraies structures de portail.
- Détecteur démontable par utilisation accroupie ; mécanisme consommant un cristal pour établir un passage partagé et permanent, avec destination de retour persistante.
- Quinze maîtrises indépendantes : récolte, pêche, élevage, combat, construction, exploration, alchimie, artisanat et technologie. Talents, perception active, synergies, notifications de niveau et interface depuis l’inventaire.
- Quatre espèces originales et un boss de sanctuaire : familier lié au joueur et sentinelle à attaque annoncée ; transferts sûrs du familier entre dimensions, modèles et animations propres. Conservateur brisé à deux attaques annoncées, deuxième phase et fenêtres de vulnérabilité.
- Observatoire à deux niveaux et huit objectifs d’expédition personnels, notes persistantes, récompenses finies et trophée du cartographe.
- Ancres et prismes de retour dans Vestige : canalisation, annulation en cas de déplacement ou dégâts, coût fini et transport sûr du familier proche.
- Sanctuaire de combat fermé de 63 × 43 × 63 blocs : trois épreuves, reprise après abandon, butin commun unique et égide défensive.
- Agrégats à croissance nocturne, poussière et prisme d’écho pour repérer les dangers proches.
- Forge résonante : quatre transformations, poussière d’écho, entonnoirs, pause redstone et rattrapage borné. Pioche, lame et cinq progrès natifs.
- Extracteur avec cinq productions (fer, cuivre, or, redstone, diamant), charbon, modules d’efficacité ou de stabilisation, calibrage par les consoles et les talents. Production différée bornée, sans XP automatique.

La validation de cette version est détaillée dans [INDUSTRY_0.10.0.md](docs/INDUSTRY_0.10.0.md). La [0.9](docs/AWAKENING_0.9.0.md) reste incluse. Les [expéditions et le retour de la 0.8](docs/FIELDWORK_0.8.0.md) restent inclus. Le [boss et la forge de la 0.7](docs/EXPEDITION_0.7.0.md) restent inclus. L’[ambiance de la 0.6](docs/ATMOSPHERE_0.6.0.md) reste incluse. Le [sanctuaire de la 0.5](docs/DUNGEON_0.5.0.md) reste inclus. Les [rencontres de la 0.4](docs/ENCOUNTERS_0.4.0.md) restent incluses. La [progression 0.3](docs/PROGRESSION_0.3.0.md) reste incluse. La [refonte 0.2](docs/OVERHAUL_0.2.0.md) reste incluse.

## Commandes

```text
/tnd masteries
/tnd expeditions
/tnd sense mining
/tnd sense logging
/tnd sense engineering
/tnd enter
/tnd leave
```

La perception accepte les quinze identifiants de maîtrises (par exemple `/tnd sense farming` ou `/tnd sense ancient_tech`) et nécessite le talent correspondant. Le bouton « Percevoir » de l’interface fait la même chose. `enter` et `leave` sont réservées aux opérateurs et désactivables dans la configuration serveur. Elles servent au développement ; `leave` cherche actuellement une surface à X=0, Z=0 et refuse un retour aquatique. La quête doit utiliser les passages appariés.

## Configuration et données

Les options serveur `experienceMultiplier`, `extractorTicksPerItem`, `maxCatchupTicks` et `developmentCommands` sont définies dans `TndConfig`. Les taux de génération et recettes sont des données remplaçables par datapack. Les identifiants `tnd:*` et ceux des maîtrises constituent des clés de sauvegarde : les renommer nécessite une migration.

Le mod préserve les capacités vanilla des débutants. Les grands donjons à plusieurs étages, les autres boss, les machines avancées et le scénario final restent à développer. Aucune catastrophe n’est déclenchable dans ce code.

Les textures 16 × 16 des blocs et icônes, les atlas 64 × 64 des personnages, modèles, données et structures NBT sont générés par `scripts/generate_resources.py`, `scripts/pixel_art.py` et `scripts/architecture.py`. L’option `--audio` régénère les 40 sons originaux avec `ffmpeg`. Voir [ARCHITECTURE.md](docs/ARCHITECTURE.md) et [THIRD_PARTY.md](docs/THIRD_PARTY.md).
