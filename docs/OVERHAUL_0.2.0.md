# Refonte 0.2.0-dev

Cible : Minecraft Java 26.2, NeoForge 26.2.0.93, Java 25.

## Architecture et matériaux

Les trois anciens modèles sont remplacés intégralement. Deux bâtiments supplémentaires sont générés dans Vestige. Les volumes de fichiers incluent leurs toitures et leurs abords :

| Structure | Dimensions | Aménagements |
| --- | --- | --- |
| Avant-poste | 49 × 35 × 49 | Vestibule, atelier, dortoir à couchettes, galerie, salle des fresques et cache d’expédition |
| Sanctuaire | 57 × 43 × 57 | Nef, dais du portail, ailes d’archives, galerie, quatre piliers et vestibules |
| Relais | 41 × 41 × 41 | Salle des machines, atelier, galerie, escalier et cabine supérieure |
| Archive | 55 × 39 × 49 | Rayonnages, tables, ailes de lecture, galerie et réserves |
| Fonderie | 57 × 41 × 49 | Quatre fours, conduits, allée technique, ateliers et réserves |

Les trois types de bâtiments de Vestige partagent une grille de placement : la séparation minimale de 144 blocs évite leurs intersections. Les avant-postes naturels évitent aussi les sanctuaires de portail.

Les salles ont des murs, toitures, vitrages, passages, éclairages et meubles. Les coffres possèdent des tables de butin distinctes. Le minerai, les lampes et les décors utilisent une palette de pierre et de métal patiné, avec des sprites originaux en 16 × 16. Les matériaux vanilla gardent leurs modèles et textures vanilla. Les vingt-cinq nouveaux blocs sont obtenus au tailleur de pierre ; les escaliers et les dalles possèdent leurs vraies formes et collisions.

L’avant-poste initial charge sa zone avant toute lecture de hauteur et reçoit des fondations. Il ignore la cime des arbres pour choisir sa hauteur, conserve le niveau de l’eau sur les côtes et remplace l’eau sous les fondations par des blocs solides. Le terrain de Vestige conserve aussi suffisamment de ciel pour les toitures les plus hautes. La création du passage de retour installe désormais un petit abri avec arche et éclairage dans le volume déjà vérifié comme libre.

## Découverte des minerais

Les trois minerais utilisent des features de milieu, après la végétation. Les lectures et écritures sont limitées au chunk décoré.

- **Pyrique** : le bloc juste au-dessus doit contenir de la lave. Des petits groupes sont répartis sous les lacs de l’Overworld, à toute altitude. Un ensemble d’au moins huit positions admissibles dans un chunk reçoit au moins un petit groupe.
- **Abyssal** : le minerai remplace le fond exposé, avec au moins huit blocs d’eau au-dessus. Tous les biomes du tag vanilla `is_ocean` sont concernés. Les fonds admissibles comprennent pierre, sable, gravier et argile. Un fond admissible suffisamment étendu reçoit au moins un petit groupe par chunk.
- **Sylvestre** : sol exposé, air au-dessus et tronc de deux blocs juste à côté. Forêts, taïgas et jungles sont incluses. Le tirage reste rare : environ une position admissible sur vingt-quatre.

Les blocs enterrés et les emplacements éloignés d’un tronc ne sont pas des candidats. Ces conditions peuvent être ajustées via les sources et les tags de datapack ; il n’y a pas de remplacement automatique du terrain déjà exploré.

## Interfaces et ambiance

Un onglet à droite des inventaires de survie et de création ouvre le menu serveur des maîtrises. Aucun livre n’est requis. Les niveaux, XP, points, rangs, coûts et prérequis restent contrôlés par le serveur. Le livre existant reste compatible avec les anciennes sauvegardes, mais il a été retiré du coffre initial.

Les interfaces des maîtrises et de l’extracteur emploient des panneaux biseautés, des fonds contrastés, des jauges et des infobulles. Les quinze identifiants sont consultables ; les arbres ayant des effets jouables restent le minage, le bûcheronnage et l’ingénierie. Les autres sont explicitement indiqués comme à venir.

Vestige utilise les attributs natifs de Minecraft 26.2 pour le brouillard entre 24 et 150 blocs, les particules blanches, le vent continu discret, les vibrations et les échos métalliques occasionnels. Les sons suivent les réglages audio vanilla. La luminosité a été relevée pour rendre l’exploration lisible.

## Validation

- Compilation et paquet JAR avec JDK 25 / NeoForge 26.2.0.93.
- Noyau : 50 033 assertions sur progression, sauvegardes, production et cycle.
- Ressources : 9 contrôles couvrant JSON, références, PNG 16 × 16 et CRC, NBT, recettes, traductions, éclairage, toitures et navigation entre les salles.
- Minecraft : 11 GameTests requis, dont 10 tests du mod et 1 test de l’environnement vanilla. Ils vérifient la dimension, l’extracteur, les portails, les trois milieux de minerais, les bâtiments et les fondations. Les tests de minerai exécutent la vraie feature dans des milieux préparés avec les biomes du moteur.
- Monde normal : minerais bleus et rouges retrouvés dans les chunks générés ; 50 minerais verts dans 121 chunks autour de la forêt de départ.
- Client Minecraft : vérification de l’inventaire et du menu des maîtrises ; inspection de bâtiments, de l’intérieur et de l’ambiance de Vestige dans un monde jetable.

Cette version est une refonte de ces systèmes. Elle n’achève pas les créatures, les douze maîtrises restantes, les réseaux industriels ou le scénario du cahier des charges initial. Une partie de longue durée et l’équilibrage multijoueur ne sont pas couverts par ces essais.

## Installation et essai

Remplacer l’ancien JAR TND par `tnd-0.2.0-dev.jar`, conserver une seule version dans `mods/`, puis lancer le profil NeoForge 26.2.0.93. Sauvegarder sa partie avant de changer de version.

Pour vérifier tous les changements de génération, utiliser un nouveau monde créatif avec commandes. ` /tnd enter ` entre dans Vestige ; ` /locate structure tnd:archive `, `tnd:foundry` ou `tnd:relay` trouvent les bâtiments naturellement générés. Les commandes vanilla `/place template tnd:outpost`, `/place template tnd:portal_site`, etc. permettent de placer un exemplaire dans une zone de test choisie ; elles modifient les blocs à cet endroit.

Les nouveaux bâtiments et minerais apparaissent dans les chunks générés après l’installation. Le mod n’écrase pas les anciens bâtiments des sauvegardes.
