# Les Soutes de Nacre — 0.20

Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25.

Cette version relie l’exploration des océans, le minage de l’Overworld et les combats de fin de progression à une industrie plus productive, mais coûteuse en énergie, en composants et en entretien.

## Trois filières

| Matière | Acquisition | Traitement et usage |
| --- | --- | --- |
| Ferronyte | Veines relativement courantes de l’Overworld entre Y −48 et 16 ; pioche de diamant minimum | Fonte classique ×1 ; centrifugation avec du sable puis four à pression : ×3. Machines et kits d’entretien. |
| Thalassite | Nodules lumineux exposés au fond des océans à Y ≤ 45, sous au moins 12 blocs d’eau ; environ un emplacement éligible sur 90 | Pioche ou drague. Fonte ×1 ; centrifugation avec un éclat de prismarine puis four à pression : ×3. Circuits et cellules énergétiques. |
| Astérium | Nodules des quatre arènes des Soutes, accessibles après la mort effective de leur gardien | Extraction personnelle, une fois par boss et par ville : 3, 4, 5 puis 6 bruts. Fonte ×1 ou réplication avec une matrice : ×3. Équipement et machines finales. |

Chaque matière possède une épée, une pioche, une hache, une pelle, une houe et les quatre pièces d’armure : **27 équipements**. Durabilités des outils : 2 100 / 2 500 / 3 600. Réparation native avec le lingot correspondant et enchantements normaux. Les sprites d’inventaire restent en 16 × 16 ; les armures portées utilisent des atlas 64 × 64 et des pièces articulées en relief.

Bonus de set complet : ferronyte, résistance au feu en s’accroupissant ; thalassite, respiration et nage améliorées sous l’eau ; astérium, Résistance I. Les effets demandent réellement les quatre pièces du même métal.

## Sept machines utiles

Toutes possèdent un écran astral, des entrées distinctes, un emplacement Service, cinq sorties, une réserve FE, un état explicite et une aide de construction via « ? ». Les icônes des ingrédients et du résultat montrent la recette choisie. Le bouton change de recette ou démarre volontairement une extraction.

| Machine | Production | Coût par cycle |
| --- | --- | --- |
| Hydrocentrifugeuse | 1 brut + sable / éclat de prismarine → 3 granulés | 6 000 / 10 000 FE ; 4 / 5 s ; 0,4 / 0,6 % d’intégrité |
| Four à pression | 4 granulés + charbon / quartz → 4 lingots | 6 000 / 10 000 FE ; 3 / 4 s ; 0,4 / 0,6 % |
| Drague abyssale | Récolte les vrais nodules d’un volume 17 × 17 × 3 sous elle | 16 000 FE, 3 s, 1 usage de trépan et 1 % par nodule |
| Foreuse abyssale | Excave les vrais blocs d’une colonne 17 × 17 jusqu’au fond du monde | 2 blocs/s ; 900 FE/pierre, 12 000 FE/minerai ; 1 usage de trépan et 0,2 % par bloc |
| Réplicateur d’astérium | 1 brut + 1 matrice → 3 lingots | 96 000 FE ; 16 s ; 4 % |
| Turbine à cycle fermé | 1 cellule + 1 seau d’eau → 640 000 FE + seau vide | Production sur 20 s, 1 600 FE/t ; 2 % par cellule |
| Assembleur de précision | Débris de netherite + 4 auralites purifiées → 4 matrices ; 2 ferronytes + 4 quartz → 4 kits ; thalassite + 2 blocs de charbon → cellule | Respectivement 16 000 / 6 000 / 24 000 FE ; 6 / 4 / 8 s ; 1 / 0,4 / 1,2 % |

Les fabrications manuelles de matrices, kits et cellules sont moins avantageuses que l’assemblage industriel. La foreuse, le réplicateur et la turbine exigent un **moteur disjoint**, une récompense personnelle des boss des Soutes, et des matériaux avancés. La foreuse exige également deux blocs de netherite. Il n’existe aucune recette transformant des lingots en bruts ou granulés pour multiplier une seconde fois le même métal.

### Montage physique

- Centrifugeuse, four, réplicateur, turbine et assembleur : **huit charpentes industrielles en anneau 3 × 3 à Y−1**, puis quatre piliers aux angles, hauts de deux blocs à Y et Y+1. Le centre sous le contrôleur reste disponible pour la sortie.
- Drague : même anneau inférieur, sans piliers. Contrôleur à deux blocs au-dessus du fond, en biome océan de l’Overworld, à Y ≤ 55, avec huit blocs d’eau au-dessus.
- Foreuse : bordure 7 × 7 à Y−1, quatre piliers d’angle de cinq blocs, de Y à Y+4, puis une poutre de sept blocs suivant X à Y+5.

Une pièce manquante arrête la machine. Les extractions démarrent désarmées. Le trépan renforcé possède **512 utilisations**. Un kit dans Service rétablit 100 % d’intégrité au prochain cycle exigeant un entretien. Retirer la maintenance ou le trépan ne crée aucune ressource.

Trémies : matière par le dessus, réactif/trépan et entretien par les côtés, produits par dessous. Pour la turbine, le seau d’eau arrive côté Réactif et le seau vide ressort dessous. Les câbles et accumulateurs industriels existants restent compatibles. Un signal redstone suspend le travail. Aucun travail hors chargement, aucun chunk forcé et aucun rendement calculé pendant une déconnexion.

Les sorties bloquées, l’énergie insuffisante et les terrains protégés préservent les ressources. La foreuse ignore les coffres, machines, blocs indestructibles et cibles refusées par les protections de casse. La drague ne fabrique aucun minerai : elle remplace le nodule réel par du gravier. Déplacer une foreuse permet de viser une autre zone ; une colonne épuisée reste épuisée.

## Un donjon sous chaque métropole

L’unique accès de surface se trouve sur la rue centrale, **64 blocs au sud du centre de Nacre**. Un pavillon de roche noire abrite une échelle continue vers les Soutes. Ce passage est distinct du laboratoire d’incubation. Le donjon occupe environ 530 × 270 blocs sous le socle urbain, avec huit halls de 97 blocs de côté et une voûte culminant vers Y 60.

Le parcours est continu : débarcadère, Bastion enseveli, conduits noyés, Lentille noyée, fours silencieux, Faucheur de Nacre, arsenal disjoint, Souverain des Soutes. Les portes de progression s’ouvrent après les combats. La dernière ouvre un retour vers le couloir d’entrée et la même échelle. **Aucune énigme.**

Les galeries, escaliers, grandes nervures, alcôves équipées, réservoirs, conduites et instruments suspendus donnent du relief aux intérieurs. Les lignes lumineuses au sol et l’éclairage des piliers gardent les attaques lisibles dans la roche noire métallique. Les coffres contiennent des métaux, composants, consommables et, plus rarement, des équipements des deux premières filières. Les réserves des tours, les coffres de ville et les provisions de l’Observatoire accueillent également des matériaux industriels.

**292 ennemis au total** : quatre salles de 48 gardes, et quatre arènes de 24 gardes suivis de leur boss. Douze adversaires vivants maximum par rencontre, de courtes pauses entre vagues et aucun repeuplement infini. Trois rôles : sentinelle résistante, chasseur rapide, fusilier à distance. Les quatre boss mesurent nominalement de 9 à 13 blocs, disposent de 3 600 à 6 000 points d’intégrité et de motifs télégraphiés propres. Leurs modèles articulés portent boucliers, lames, pointes et anneaux mobiles. Les familiers peuvent participer ; les attaques de zone des gardiens ciblent les joueurs et ne détruisent pas le terrain.

Les salles s’engagent à l’approche en Survie ; un terminal permet aussi de lancer le combat et de suivre les éliminations. Après chaque boss, interagir avec son nodule ou le bouton d’extraction du terminal remet une récompense personnelle unique. Chaque boss fournit aussi un moteur disjoint. Les joueurs ayant réellement participé peuvent réclamer leur lot ; une présence ultérieure ne permet pas de récupérer le butin des autres. Un abandon de dix secondes réinitialise la rencontre courante, sans effacer les secteurs précédemment achevés. Les acteurs disparus ou déchargés ne comptent jamais comme morts ; une tentative interrompue peut être relancée au terminal une fois ses acteurs absents.

La difficulté Paisible ne lance aucune rencontre. Les boss sont conçus pour un personnage avancé accompagné et équipé ; leur équilibrage reste à confronter aux parties réelles. Le nombre d’ennemis est fixe : aucune durée précise de campagne n’est promise.

## Mondes existants

Les nouveaux minerais naturels demandent des **chunks d’Overworld inédits**. Les nouvelles villes incluent les Soutes à leur génération. Les villes déjà générées disposent d’une migration progressive, limitée aux chunks chargés, un chunk toutes les cinq ticks. Chaque chunk est inspecté avant modification : un contenant ou un matériau de construction étranger dans le volume visé fait conserver le chunk entier. Aucun inventaire n’est remplacé. Les chunks conservés ne sont pas retentés automatiquement, afin d’éviter une modification différée inattendue.

Une ville ancienne occupée peut donc conserver une portion de terrain dans le nouveau souterrain. Dans ce cas, préférer **une métropole encore inexplorée** pour un parcours complet. Les anciens Observatoires, les Fournaises et les Archives ne sont pas remplacés par cette mise à jour.

## Validation

Les vérifications et les captures natives de cette version sont précisées dans le compte rendu publié avec le JAR. Les tests couvrent les transactions FE, les trémies, les consommations exactes, l’entretien, les nodules réels, les sauvegardes, l’accès unique, l’architecture, les morts effectives, les récompenses personnelles et la migration préservant les coffres. Les prises de vue de contrôle utilisent une sauvegarde isolée et des commandes ; elles ne représentent pas une campagne gagnée sans aide ni une mesure des performances sur matériel de joueur.
