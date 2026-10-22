# 0.14 — Les tours de confinement

Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25. Remplacer l’ancien JAR TND sur le client et le serveur.

## Des tours à conquérir

Trois tours de chaque nouveau quartier de Nacre reçoivent des salles distinctes, des réserves et une rencontre de sommet : clinique de la Suture, caserne du Rempart et tour de la Traque. Les grands quartiers conservent leurs escaliers, entrées multiples, passerelles, archives et accès au toit. Les étages disposent de pièces séparées, laboratoires, postes de tir ou rayonnages de surveillance.

Une intrusion active les équipes de sécurité : quatre gardes au maximum par étage, avec lourds, traqueur et drone. Les luminaires de réserve peuvent s’éteindre huit secondes, puis se rétablissent. Les rencontres s’activent près du joueur, sans charger de chunks voisins. Les effectifs sont limités et sauvegardés ; recharger le quartier ne recrée pas ses gardes. Le toit ne produit qu’un mini-boss. Les coffres et les rencontres sont des ressources finies de l’exploration, pas une ferme à réapparitions illimitées.

Ces modifications apparaissent dans les nouveaux chunks de ville. La mise à jour ne remplace pas les bâtiments déjà sauvegardés.

## Trois adversaires et trois sets

| Tour | Mini-boss | Combat | Bonus des quatre pièces du même set |
|---|---|---|---|
| Caserne | Maréchal de Nacre | Adversaire lourd plus grand ; charge annoncée et impact de zone | Résistance I et Résistance au feu I |
| Tour de surveillance | Traqueur prismatique | Contournement entre les attaques, ruée visant une position annoncée, interruption par un coup assez fort | Vitesse I et Sauts améliorés I |
| Clinique | Chirurgien du Chœur | Visée lumineuse, tirs et soin de ses alliés visibles ; canalisation interruptible | Régénération I et Célérité I |

Le mini-boss possède une barre de vie locale, des préparations d’attaque visibles et une récupération qui laisse une ouverture. Les déplacements et les tirs respectent les murs ; aucune attaque ne détruit la ville. Les traqueurs de sécurité existants contournent également leur cible pendant leur récupération.

**Chaque victoire attribuée à un joueur a exactement 20 % de chance de donner une pièce**, choisie uniformément parmi casque, plastron, jambières et bottes du set de ce mini-boss. Une victoire ne peut donc pas donner plusieurs pièces. Pillage ne modifie pas ce tirage. Un doublon reste possible ; aucune pièce particulière n’est garantie après un nombre fixé de victoires. Chaque pièce possède **500 points de durabilité**, une protection de niveau diamant et les enchantements habituels. Les effets nécessitent les quatre bonnes pièces ; leur rémanence dure au plus six secondes après retrait.

## Un atelier pour préserver son équipement

Le **restaurateur cristallin** se fabrique avec deux alliages d’Umbra, deux circuits de commande, deux matrices inertielles, un lingot de netherite et deux charpentes. Son socle demande huit autres charpentes autour de la position située une couche sous la machine. Alimenter par les câbles, une batterie ou une autre source FE.

Placer la pièce usée en haut à gauche et les cristaux en bas à gauche. La pièce réparée sort à droite. Un cycle dure quatre secondes et consomme **2 000 FE plus un cristal** :

| Matière | Durabilité rendue |
|---|---:|
| Éclat bleu, rouge ou vert | 100 |
| Éclat résonant ou cristal de noctilite | 150 |
| Cristal composite | 250 |

La réparation est plafonnée au maximum de l’objet. Nom, enchantements et composants sont conservés. Une sortie pleine, un socle incomplet, l’absence d’énergie ou un signal redstone empêchent la consommation. Les entonnoirs peuvent alimenter les deux entrées et récupérer la sortie par-dessous. La machine ne simule pas du travail pendant le déchargement du chunk.

Les enchantements vanilla, y compris Raccommodage, restent compatibles avec ces équipements. La machine offre une réparation automatisable sans payer d’expérience. Le guide industriel accessible depuis l’inventaire explique son montage et les valeurs des cristaux.

## Des traces pour le familier

Chaque mini-boss vaincu par un joueur laisse une mémoire de son protocole :

- Rempart : +6 PV maximum au familier.
- Traque : vitesse de déplacement accrue.
- Suture : +2 points d’armure.

Utiliser la mémoire sur son familier lié et rétabli. Chaque protocole s’installe une seule fois. Un refus ou une tentative sur le compagnon d’un autre joueur ne consomme rien. Les améliorations survivent à la croissance, à la sauvegarde et au transport. Le dossier indique le nombre de protocoles installés ; les propriétés se lisent sur les objets. Un doublon et une redstone donnent quatre circuits de commande.

## Validation

Neuf nouveaux GameTests couvrent les ensembles incomplets et complets, l’enchantabilité, les 500 points de durabilité, la réparation d’un objet nommé et enchanté, les coûts, la sortie bloquée, les entonnoirs, la sauvegarde, les rencontres finies, la panne temporaire, les boss, les protocoles du familier et les modèles de tours. Les tables de butin natives sont tirées 5 000 fois par mini-boss pour vérifier la fréquence et la répartition des quatre pièces. La suite contient désormais 159 scénarios Minecraft ; le compte rendu joint au JAR indique les résultats effectifs.

Ces scénarios serveur utilisent les connexions intégrées des GameTests. Ils ne remplacent pas une longue campagne humaine ou une mesure de performances de toutes les villes avec plusieurs clients distants.
