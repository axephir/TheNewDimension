# The New Dimension

Mod d’exploration, RPG et industrie pour **Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25**.

**Version : 0.12.0-dev — Symbiose et quatrième axe.**

[Télécharger le JAR](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/tnd-0.12.0-dev.jar) · [Pack avec sources](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/TheNewDimension-0.12.0-dev.zip) · [Empreintes SHA-256](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/SHA256SUMS-0.12.0-dev) · [Captures Minecraft](https://github.com/axephir/TheNewDimension/blob/downloads/GALLERY_0.12.0.md)

Placer le JAR dans `mods/` de l’instance et retirer l’ancien JAR TND. Installer la même version côté serveur et côté client. Aucun autre mod de gameplay n’est nécessaire.

## Dans cette version

**Nacre devient une métropole entière**, d’environ 1 280 blocs de diamètre, sous un dôme traversable. Des quartiers de grandes tours, passerelles et pièces aménagées entourent une place centrale. La ville et son mobilier sont protégés ; portes, coffres et ordinateurs restent utilisables. Une couronne de pics ferrenoirs habille ses abords.

**Un seul familier par joueur**, trouvé dans le laboratoire central unique de chaque métropole. L’ordinateur libère un sujet hostile ; le battre le laisse blessé. Le nutriment caché dans l’annexe permet de le soigner et l’apprivoiser. Il possède ses propres niveaux, trois formes et quatre aptitudes. Son dossier permet de le renommer, consulter ses statistiques et le transporter en capsule. Clic droit main vide, cinquième onglet à gauche de l’inventaire ou bouton **Familier** des paramètres.

**Les usines continuent pendant les expéditions** grâce à une ancre alimentée : neuf chunks contre 16 FE/t, assemblage physique, propriétaire connecté et limites de charge. L’industrie électrique, les fermes, les carrières et l’élevage des versions précédentes restent inclus.

**La progression narrative possède une conclusion jouable** : restaurateur à reconstruire, réacteur de convergence, ruines personnelles explorables, effets de rémanence configurables, refus ou acceptation du retour, puis perception du quatrième axe. La scène écrit dans une dimension isolée. La base réelle reste intacte. Des journaux vérifiés et `/tnd recover` assurent des voies de récupération distinctes de l’interface.

Le portail adopte une surface plus claire, des contours lumineux fins et de petites particules. Le moniteur et son ouverture permanente sont conservés. Les archives expliquent progressivement le départ des habitants et les anomalies perceptives, espacées de dizaines de minutes.

[Guide complet de la 0.12](docs/SYMBIOSIS_0.12.0.md) · [Couverture du cahier des charges](docs/CAHIER_COUVERTURE_0.12.md)

## Commencer une partie

1. Rejoindre l’avant-poste ancien, généré près du premier spawn d’un nouveau monde, et lire les fresques.
2. Récupérer le noyau, puis chercher les minerais **bleus aux fonds des océans**, **rouges dans la première couche solide sous les lacs de lave**, et **verts près des racines d’arbres**.
3. Fabriquer le cristal composite et le détecteur pour trouver une vraie structure de portail rare. Accroupi, séparer le détecteur pour récupérer le noyau et le cristal.
4. Construire le moniteur près du seuil, son socle de huit charpentes et ses quatre bobines relais. Fournir **16 000 FE** et un cristal composite. L’interface indique les éléments manquants. **Inventaire → ? → Portail** détaille le montage.
5. Explorer Vestige, réactiver les consoles, développer les maîtrises et l’industrie, affronter le sanctuaire, retrouver Nacre et ses archives.

Les nouvelles métropoles, laboratoires, massifs et minerais apparaissent dans les **nouveaux chunks**. Une mise à jour ne remplace pas les constructions déjà sauvegardées. Les anciennes parties conservent leurs passages, maîtrises, machines et compagnons reconnus.

## Systèmes inclus

| Domaine | Contenu |
|---|---|
| Vestige | Six biomes naturels, métropole, reliefs, brouillard, particules, cycle indépendant de 40 minutes, 58 sons originaux |
| Exploration | Neuf grandes architectures sur massifs couronnés, sanctuaire à trois ailes et boss, observatoire, citadelle, conservatoire, tours aménagées et archives |
| RPG | Quinze maîtrises, XP manuelle, talents, synergies, perception, progression au-delà du niveau 100 et célébrations personnelles |
| Équipements | Minerais exposés, alliages, armure en relief, outils, armes, égide, marteau et objets de retour |
| Industrie | Extracteur, forge, séparateur, récolteur nocturne, dynamo, solaire, accumulateur, broyeur, four, carrière, cultivateur, éleveur, station pastorale et ancre |
| Créatures | Sentinelle, Lithogarde, Traqueur de verre, Conservateur, sécurité de Nacre, drones, Faucheurs et familier évolutif ; attaques annoncées, recharges et butins |
| Interface | Boutons à gauche de l’inventaire, maîtrises, objectifs, guide, archives, dossier du familier et interfaces des installations |
| Conclusion | Restaurateur, réacteur, projection isolée, récupération, lentille liée et volumes cachés |

Les textures des blocs et objets restent en **16 × 16**. Les créatures et armures utilisent des atlas **64 × 64**, des modèles articulés et des animations dédiées. L’automatisation ne donne pas l’XP des actions manuelles. Les sorties, carburants, ressources et récompenses restent bornés.

## Développement et vérification

Avec un JDK 25 :

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runClient
./gradlew runServer
```

Les GameTests utilisent `run-gametest/`, les parties client `run/` et le serveur normal `run-server/`. Le serveur normal demande l’acceptation de l’EULA Minecraft dans son propre répertoire.

Vérifications des calculs et des ressources :

```sh
bash scripts/test-core.sh
python3 scripts/check_resources.py
python3 scripts/check_audio.py
```

La validation automatisée comprend **131 GameTests natifs**, **51 035 assertions du noyau**, les ressources JSON/NBT et le décodage des 58 fichiers OGG. Le rendu, les interactions et les écrans sont également examinés dans le client Minecraft. Les scénarios d’erreur du dénouement conservent les journaux et vérifient l’absence de destruction du monde source. Cela ne prétend pas garantir toute panne matérielle ; voir le §14.6 du cahier et le guide de récupération.

Dans l’environnement Codex :

```sh
bash scripts/setup-cloud.sh
python3 scripts/cloud_gradle.py build
python3 scripts/cloud_gradle.py runGameTestServer
```

Le script utilise le JDK épinglé, vérifie son empreinte et conserve le proxy ainsi que les racines TLS fournis par la plateforme. [Configuration cloud](docs/CLOUD.md) · [Architecture](docs/ARCHITECTURE.md) · [Cahier original](docs/CAHIER_DES_CHARGES.md)

## Commandes et accessibilité

```text
/tnd masteries
/tnd expeditions
/tnd archives
/tnd familiar
/tnd sense mining
/tnd recover
```

La perception des maîtrises accepte leurs quinze identifiants et nécessite le talent correspondant. `/tnd recover` vise uniquement la session narrative du joueur. Les commandes `enter`, `leave` et `anomaly` sont réservées aux opérateurs avec les commandes de développement activées.

Dans la configuration client du mod, `afterimages=false` supprime la rémanence ; `animations=false` réduit les mouvements et passe la rotation cinématique. `rareAnomalies` et le volume d’ambiance sont également réglables. Le serveur règle la taille de projection et le plafond des ancres.

## Historique et licences

[0.11 : Nacre et portail](docs/NACRE_0.11.0.md) · [0.10 : industrie](docs/INDUSTRY_0.10.0.md) · [0.9 : Éveil](docs/AWAKENING_0.9.0.md) · [0.8 : expéditions](docs/FIELDWORK_0.8.0.md) · [0.7 : boss et forge](docs/EXPEDITION_0.7.0.md) · [0.6 : ambiance](docs/ATMOSPHERE_0.6.0.md) · [0.5 : sanctuaire](docs/DUNGEON_0.5.0.md) · [0.4 : rencontres](docs/ENCOUNTERS_0.4.0.md) · [0.3 : maîtrises](docs/PROGRESSION_0.3.0.md) · [0.2 : refonte](docs/OVERHAUL_0.2.0.md)

Code sous [licence MIT](LICENSE). [Ressources et dépendances](docs/THIRD_PARTY.md).
