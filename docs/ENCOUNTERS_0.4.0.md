# Rencontres et cycle nocturne — 0.4.0-dev

Minecraft Java 26.2 / NeoForge 26.2.0.93 / Java 25. La progression, les machines et les structures des versions précédentes sont conservées.

## Deux premières créatures originales

Les espèces et leurs noms restent des propositions de travail. Leurs modèles articulés sont originaux, avec leurs propres animations et quatre atlas de texture de **16 × 16 pixels**. Aucun modèle d’animal vanilla n’a été recoloré.

### Éclat errant

Petit assemblage minéral à quatre pattes, antennes et fragment central. Il apparaît rarement à la surface de Vestige, seul. Un joueur peut l’apprivoiser avec **un cristal composite**, consommé en survie ; l’apprivoisement est garanti. Un clic à main vide sur un individu sauvage indique cet ingrédient.

Une fois lié :

- Il suit son propriétaire ; clic à main vide pour alterner suivre et attendre.
- Un éclat résonant lui rend huit points de vie, jusqu’à son maximum de 24. L’ingrédient n’est pas consommé s’il est déjà en pleine forme.
- Il aide son propriétaire au combat avec trois dégâts de base, et n’attaque ni les joueurs ni les familiers apprivoisés.
- Toutes les dix secondes, s’il suit son propriétaire proche, il signale le minerai résonant le plus proche dans une zone de 13 × 9 × 13 blocs. Le signal est un petit chemin de particules visible par son propriétaire. Aucune extraction, aucun XP automatique et aucun chargement de chunk.
- Le propriétaire, la santé et l’ordre d’attendre survivent au rechargement. Il ne peut pas être élevé et ne donne pas de récompense de chasse.

Un familier chargé, vivant, en mode suivre et situé à moins de six blocs accompagne les changements de dimension du propriétaire, y compris les portails vanilla et les passages TND. L’Overworld, le Nether, l’End et Vestige sont compatibles. Le transfert conserve son UUID et sa propriété. Un familier en attente reste au départ. Si aucune position proche avec sol, espace libre et absence de fluide ne convient, il reste au départ et son propriétaire en est informé. Aucun bloc n’est supprimé pour le faire arriver. Seul le chunk d’arrivée est chargé si nécessaire au transport, sans ticket permanent.

### Sentinelle de faille

Mécanisme autonome quadrupède avec noyau mobile. Elle apparaît naturellement **la nuit**, dans les zones peu éclairées à ciel ouvert de Vestige. Elle reste immobile et cesse ses attaques le jour, sauf si elle est provoquée. Elle n’apparaît pas naturellement dans les dimensions vanilla et respecte le mode Paisible.

Sa difficulté repose sur un geste lisible :

1. Elle s’approche jusqu’à portée, puis fixe la position actuelle de sa cible.
2. Un cercle de particules de 2,5 blocs de rayon et un son annoncent l’impact pendant **32 ticks, soit 1,6 seconde**.
3. L’impact inflige cinq dégâts de base aux joueurs restés dans la zone et dans sa ligne de vue. Les règles natives de dégâts s’appliquent.
4. Elle récupère pendant trois secondes avant une nouvelle attaque.

Il est possible de sortir de la zone annoncée ou de lui infliger des dégâts durant la charge pour l’interrompre. Une interruption impose deux secondes de récupération. La cible d’impact ne poursuit pas le joueur pendant la charge. La sentinelle possède 24 points de vie et deux points d’armure ; il ne s’agit pas d’un boss.

Une élimination attribuée à un joueur peut donner une ou deux poussières d’écho et, avec 25 % de chance, un éclat résonant.

## Une ressource nocturne utile

Les **agrégats d’écho** se génèrent sur la pierre cendrée exposée de Vestige, sans remplacer les constructions. Leur croissance comprend quatre états visuels. Ils accumulent leur charge uniquement pendant la nuit de Vestige, selon sa véritable horloge, y compris si celle-ci a été décalée ou arrêtée par commande.

Un agrégat mûr se récolte d’un clic droit pendant la nuit, donne une ou deux poussières, puis repart de zéro. Il reste en place et peut se recharger. Le jour, la récolte est refusée sans perte de charge. Un agrégat déplacé dans l’Overworld ne charge pas et ne produit rien. Le temps de croissance utilise les ticks aléatoires natifs : la zone doit être simulée. L’XP de récolte manuelle en technologies anciennes est limitée par un budget sauvegardé.

Les agrégats peuvent être déplacés à la pioche ; leur charge est remise à zéro lorsqu’ils sont reposés.

Le **prisme d’écho** se fabrique avec trois poussières, un cristal composite et un éclat d’améthyste. Utilisé vers un espace libre, il révèle les créatures hostiles dans un rayon de 16 blocs par la surbrillance native pendant dix secondes. Il consomme une poussière dans l’inventaire et possède une recharge de vingt secondes. Sans cible, il ne consomme rien. Il ne cherche que parmi les entités chargées et n’attribue pas d’XP.

Les indices de la fresque de fusion et les infobulles expliquent ces interactions. Les nouvelles recettes rejoignent le livre de recettes du mod. Des œufs d’apparition sont disponibles en créatif pour les deux créatures.

## Préserver l’ambiance désertée

Aucun village, groupe de PNJ ou population dense n’est ajouté. Les tables de biome limitent les groupes à un individu ; les contrôles supplémentaires imposent un tirage rare et une limite locale. Il ne peut apparaître naturellement qu’un Éclat errant sauvage dans les 96 blocs alentour, ou deux sentinelles dans cette zone. Les sentinelles exigent également la nuit, l’absence d’éclairage fort, le ciel et un sol naturel de pierre cendrée.

Les apparitions utilisent le système natif, ses plafonds et ses règles de simulation. Les taux restent à équilibrer sur plusieurs longues parties et plusieurs graines ; ils ne garantissent pas une rencontre à chaque ruine.

## Validation et installation

- Java 25 : build et **50 114 assertions du domaine**.
- Ressources : **11 tests**, dont références, recettes, modèles de blocs, 54 textures en 16 × 16, tables d’apparition limitées à Vestige et traductions françaises/anglaises.
- Minecraft : **25 tests requis**, dont la liaison et sa sauvegarde native, le suivi physique, les transferts du familier entre les trois dimensions vanilla et une arrivée initialement déchargée, l’attente au départ, l’absence d’attaque contre les familiers d’autrui, le coût et la recharge du prisme, la génération réelle d’agrégats sur une surface préparée, ainsi que les dégâts, l’esquive et l’interruption des sentinelles. Les régressions de la 0.3 restent incluses.
- Le client normal sert à vérifier les modèles et animations, l’apprivoisement en survie, la liaison Overworld/Vestige, le refus de récolte le jour, la récolte nocturne et la surbrillance du prisme. Les captures publiées utilisent une partie de validation et des entités/agrégats placés par commande pour ces contrôles ; elles ne prétendent pas mesurer les taux naturels d’apparition.

Remplacer l’ancien JAR TND par `tnd-0.4.0-dev.jar`, en conservant une seule version dans `mods/`, après sauvegarde de sa partie. Les maîtrises et machines existantes sont conservées. Les créatures peuvent apparaître sur les terrains existants compatibles de Vestige ; **les nouveaux agrégats naturels demandent de nouveaux chunks**. Aucun nouveau monde n’est obligatoire.

Les donjons de combat complets, les boss, les machines industrielles supplémentaires, les événements narratifs et la conclusion du cahier des charges restent à développer. La catastrophe scénaristique n’est pas implémentée.
