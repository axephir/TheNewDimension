# Couverture du cahier des charges — 0.14

Ce document relie les exigences du [cahier original](CAHIER_DES_CHARGES.md) au contenu jouable et aux validations. Les demandes ultérieures de NeoForge 26.2, de métropoles sous dôme et de familier unique remplacent les propositions initiales incompatibles.

| Parties du cahier | Réalisation |
|---|---|
| 1–3 : univers, dimension, civilisation | Vestige, six biomes naturels et une métropole continue, reliefs, monuments sur scorie ferrenoire, installations réactivables, huit archives, noyau énigmatique et conclusion personnelle |
| 4 : horreur environnementale | Ambiances de biomes, couches nocturnes et intérieures, sons spatialisés ; quatre contextes d’anomalies espacées de dizaines de minutes, configurables |
| 5 : cycle distinct | Horloge native de 48 000 ticks indépendante de l’Overworld ; comportements, ressources et ambiance nocturnes |
| 6 : quinze maîtrises | XP manuelle, cinq fondations et deux spécialisations propres à chaque maîtrise, synergies, perception, progression au-delà du niveau 100, titres aux niveaux 100/250/500/1000 et quatre sceaux décoratifs, sauvegarde et synchronisation personnelle ; aucune capacité vanilla verrouillée |
| 7 : industrie et absence | Extraction, forge, séparation, récolte nocturne, neuf machines électriques, FE, câbles, accumulateur, entonnoirs, fermes, élevage, carrière à derrick ; rattrapage borné et ancres alimentées |
| 8 : compagnon | Un lien par joueur, laboratoire unique par métropole, capsule et ordinateur, défense, mise au sol, nourriture cachée, trois formes, niveau personnel, quatre aptitudes, dossier, soins et transport |
| 9 : donjons et exploration | Neuf grandes architectures de sites, sanctuaire à trois ailes, trésor fini, boss, citadelle, conservatoire, observatoire, quartiers aménagés et huit objectifs d’expédition |
| 10 : accès | Avant-poste initial, fresques, noyau, trois minerais exposés, cristal composite, détecteur de véritables structures rares ; moniteur assemblé et alimenté ; passage permanent, partagé et apparié |
| 11–12 : histoire et machine | Archives progressives, avertissements sur les rendements, plans de montage séparés des indices, reconstruction physique et noyau muet |
| 13 : conclusion | Réacteur, ciblage d’une base prouvée près du lit personnel, caméra adaptée aux obstacles, ruines explorables sur 400 × 400 blocs par défaut, quatre traces interactives, silence, rémanence GPU désactivable, indication après deux minutes, guidage, acceptation ou refus réversible, retour et révélation |
| 14 : données | Dimension isolée, source jamais cible des destructions, conteneurs visuels vides, observation native, références personnelles doubles et vérifiées, écritures atomiques, journaux conservés, récupération indépendante de l’interface |
| 15 : prolongement optionnel | Lentille de quatrième axe liée au propriétaire, volumes cachés réellement rendus et pas spatial local sûr ; aucune prétention à simuler un monde physique complet à quatre dimensions |
| 16–17 : interfaces et art | Onglets à gauche de l’inventaire, guides, menus des machines, dossiers paginés ; textures blocs/objets 16 × 16, atlas de créatures et armures 64 × 64, modèles articulés, effets et 58 sons originaux |
| 18 : multijoueur | Serveur autoritaire, propriétaires vérifiés, menus bornés, récompenses idempotentes, passages partagés, liens uniques et conclusion personnelle qui préserve les actions des autres joueurs |
| 19–20 : fondations et étapes | Minecraft 26.2 / NeoForge 26.2.0.93 / Java 25, séparation du client, génération reproductible des ressources, compilation, noyau indépendant et tests d’intégration dans un monde séparé |
| 21–23 : méthode et distribution | Choix de version imposé par la demande définitive, documentation de montage et de jeu, code source, JAR téléchargeable, empreintes et historique de versions |

## Preuves et limites des validations

Le lancement dédié vérifie les registres, les codecs, les ressources et l’absence de dépendance client dans les systèmes serveur. Les GameTests utilisent de vrais mondes, joueurs à connexion intégrée, menus, entités, inventaires, moteurs de lumière et fichiers NBT. La suite complète contient 159 scénarios ; le noyau possède 51 083 assertions. Les ressources JSON/NBT, références de modèles, traductions et dimensions d’images font l’objet d’une vérification séparée ; les 58 fichiers OGG sont décodés par ffmpeg.

La série de récupération vérifie notamment : une référence corrompue, deux références corrompues avec inventaire natif intact, absence du repère du joueur et de l’index, interruption de session, état de restauration en attente, refus puis acceptation, absence de réutilisation d’un retour terminé, erreur d’écriture, conservation de l’armure et du coffre de l’Ender, et absence de réécriture des modifications faites dans le monde source.

Les captures proviennent du client Minecraft sans retouche. Une validation automatisée ne mesure pas à elle seule la qualité artistique, l’équilibrage sur des centaines d’heures ou toutes les interactions possibles avec des mods tiers. Les volumes de ville et les images permettent de juger le résultat visuel ; les limites de configuration bornent la charge de l’industrie et de la conclusion. Aucun pourcentage ne peut garantir l’absence de toute panne matérielle ou remplacer ces preuves concrètes.

La 0.13.1 ajoute six scénarios de ciblage personnel, dont une vraie base à 512 blocs du réacteur, des équipements placés via `BlockItem`, un retour après interruption, une vue dégagée sous un toit et le passage de la cinématique. Les anciennes constructions non attribuées restent exclues de la détection jusqu’à un nouveau placement ; [fonctionnement et limites](BASE_CIBLEE_0.13.1.md).

La 0.14 ajoute trois parcours d’assaut, trois mini-boss, douze pièces à bonus d’ensemble, un restaurateur cristallin et trois protocoles de familier. Les rencontres sont finies, le tirage d’équipement est de 20 % et les constructions existantes restent inchangées. [Guide détaillé](ASSAULT_0.14.0.md).
