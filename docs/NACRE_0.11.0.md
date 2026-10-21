# Nacre-9 — 0.11.0-dev

Minecraft Java **26.2**, NeoForge **26.2.0.93**, Java **25**. Remplacer le précédent JAR TND dans `mods/` ; un seul JAR du mod doit rester installé.

[JAR](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/tnd-0.11.0-dev.jar) · [Archive avec sources](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/TheNewDimension-0.11.0-dev.zip) · [Captures natives](https://github.com/axephir/TheNewDimension/blob/downloads/GALLERY_0.11.0.md)

## Le seuil devient une installation

Le portail possède une membrane animée, deux anneaux tournant en sens inverse, des glyphes en orbite, des particules et quatre nouveaux sons de charge, ouverture, traversée et résonance. Les options d’ambiance conservent le réglage des animations ; les sons de machines utilisent le volume des blocs de Minecraft.

**Un moniteur principal assemblé est désormais nécessaire côté Overworld.** Les anciennes destinations restent enregistrées ; il suffit d’ajouter le moniteur pour repartir. Le retour depuis Vestige reste possible même si le moniteur de départ est détruit. Une liaison déjà établie ne facture pas les voyages suivants.

1. Fabriquer le moniteur avec des charpentes industrielles, circuits de commande, bobines de cuivre, un bloc de quartz et de la redstone. Tous ces composants sont disponibles avant l’entrée dans Vestige.
2. Placer le moniteur à **8 blocs maximum** du contrôleur du portail, avec un écart vertical maximal de **3 blocs**. La distance de 8 blocs est mesurée en ligne droite. Éviter de poser deux contrôleurs à proximité : un moniteur dessert le plus proche.
3. Un bloc sous le moniteur, construire la **bordure 3 × 3 de huit charpentes industrielles**. Le centre du socle est libre.
4. Au niveau du moniteur, placer **quatre bobines de relais**, une à chacun des quatre angles. Le moniteur se trouve au centre.
5. Relier une dynamo ou un accumulateur avec des câbles énergétiques. Le moniteur stocke **32 000 FE** ; la première ouverture demande **16 000 FE** et **un cristal composite**.
6. Insérer le cristal dans l’interface ou par un entonnoir, puis cliquer sur **Ouvrir le seuil**. La synchronisation dure **120 ticks chargés, soit 6 secondes à 20 TPS**.
7. Cliquer sur le contrôleur du portail pour traverser.

```text
Niveau du moniteur     Un bloc en dessous
R . R                 C C C
. M .                 C . C
R . R                 C C C

M : moniteur   R : bobine de relais   C : charpente industrielle
```

Le moniteur affiche les pièces manquantes, la réserve d’énergie, la progression et le diagnostic. Son bouton `?` explique les trois étapes. Le même guide est accessible par **Inventaire → ? → Portail**. Cliquer sur le portail avec un cristal le transfère dans un moniteur assemblé et ouvre l’interface.

L’énergie et le cristal sont consommés seulement lorsque la liaison aboutit. Annuler, retirer une bobine, appliquer un signal redstone ou retirer le cristal interrompt la charge sans coût. Une arrivée encombrée conserve les ressources ; dégager l’espace permet de réessayer. Démonter le moniteur restitue les cristaux, mais ne transporte pas sa réserve FE. Le chargement d’une sauvegarde conserve les ressources et annule la séquence en cours ; aucun travail hors ligne.

## Districts de Nacre-9

Un **septième biome** de Vestige contient des quartiers cyberpunk de **127 × 111 × 127 blocs** : **huit tours**, six à douze étages de huit blocs, jusqu’à **96 blocs de hauteur habitée**, enseignes lumineuses, façades vitrées, passerelles, escaliers à double volée, toits techniques, mobilier, ateliers, clinique, caserne, logements et place centrale.

Le quartier et son massif de scorie ferrenoire constituent une seule structure sauvegardée. Une couronne de pics entoure le plateau et une montée pavée le relie au sol. Le terrain urbain est abaissé progressivement pour ménager la place des grands bâtiments sous le plafond de Vestige. Une grille commune aux quartiers et aux anciennes structures espace les centres d’au moins 288 blocs, davantage que le diamètre du plus grand massif.

**Explorer de nouveaux chunks** pour trouver ce biome et ces villes dans une sauvegarde existante. Les bâtiments déjà générés conservent leur géométrie. La région urbaine se repère par son brouillard froid, ses néons et son ambiance sonore dédiée. En créatif avec les commandes : `/locate biome tnd:neon_city`, puis `/locate structure tnd:neon_district`.

## Défenses hostiles

Les trois nouvelles créatures apparaissent uniquement dans Vestige, dans le biome urbain, sur les surfaces appropriées. Elles sont hostiles **le jour comme la nuit**, y compris près des néons. Une limite locale complète les règles de population de Minecraft. Les modèles articulés et textures originales **64 × 64** sont distincts ; les blocs et icônes restent en **16 × 16**.

| Ennemi | Comportement | Riposte |
| --- | --- | --- |
| **Veilleur de néon** | 54 PV, armure, bouclier et matraque ; annonce de 28 ticks puis ruée lourde vers la position marquée | Sortir de l’axe et frapper pendant la récupération |
| **Drone Suture** | 24 PV, vol réel et rotors animés ; verrouillage de 40 ticks puis trois impulsions vers une visée fixe | Bouger après le verrouillage ; les murs bloquent les tirs |
| **Faucheur de signal** | 36 PV, lames et manteau articulé ; préparation de 24 ticks puis ruée rapide | Esquiver la marque ; un coup suffisamment fort interrompt sa préparation |

Les attaques sont résolues par le serveur et respectent les collisions. Aucun tir ne détruit le terrain. Les joueurs créatifs et spectateurs ne sont pas des cibles de ces attaques. La barre de cible affiche les préparations et les ouvertures de combat.

Les gardes et faucheurs tués par un joueur livrent des **processeurs de sécurité** : un processeur se récupère en **deux circuits de commande**. Les drones donnent des **filaments ioniques** : recettes de bobines de relais et de néons plus économiques. Les coffres des étages et des toits contiennent des ressources utiles à l’industrie et à l’équipement.

## Archives et ergonomie

**Huit récits personnels** dévoilent les Architectes, la prospérité de Nacre, le Réacteur de Minuit, Iris et le Conservateur. Le premier s’obtient au moniteur ; les sept autres se lisent sur de petits terminaux dans la place, la clinique, la fonderie, la caserne, le transit, la tour d’Iris et le toit central. Les textes sont paginés en français et en anglais.

Le **quatrième onglet à gauche de l’inventaire** ouvre les archives, également accessibles via `/tnd archives` sans permission spéciale. Les effets de potion gardent leur place à droite. Les archives verrouillées indiquent où chercher. Chaque première découverte rapporte **12 XP de technologie ancienne** en survie, une seule fois. Les huit fragments débloquent **un mémorial décoratif unique** ; les découvertes et la récupération du trophée sont conservées après la mort et le rechargement.

La version ajoute **13 blocs**, dont le moniteur, les terminaux, les bobines et le mémorial, plus deux ressources de récupération, trois œufs d’apparition et **12 sons originaux**. Les neuf machines de la 0.10, leurs réseaux FE et toutes les progressions précédentes restent disponibles.

## Validation

Les contrôles de cette version couvrent les codecs natifs du biome et des structures, la hauteur du massif et des tours, les chemins d’escalier, les attaques réelles et leur esquive, l’occlusion des lasers, les coûts du portail, l’annulation, les sauvegardes et l’unicité des récompenses. Résultats locaux : **102 tests natifs requis réussis** (101 TND + le test de démarrage de Minecraft), **51 035 assertions** de progression et de sauvegarde, **20 contrôles de ressources** et **52 sons Ogg décodés** sans écrêtage. Les tests de génération vérifient les quatre rotations, les limites de chunk, les coffres et les terminaux ; les seize sous-modèles reconstruisent exactement les 634 472 blocs du quartier complet. Les escaliers de chacun des douze niveaux de la tour centrale passent la navigation native.

Le client Minecraft est également utilisé pour vérifier la charge du portail, les écrans en français, les archives, les tours et les modèles. Les captures sont prises par la fonction F2 du jeu, sans retouche. La ville présentée est générée naturellement dans une sauvegarde de contrôle ; le montage du portail et la présentation des trois modèles utilisent des scènes de test en créatif. Le serveur de tests utilise des joueurs intégrés et des terrains isolés : il ne remplace pas une longue partie de survie ni un essai sur un grand serveur multijoueur.
