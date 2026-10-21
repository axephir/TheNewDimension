# 0.10.0-dev — L’âge industriel

Minecraft Java **26.2**, NeoForge **26.2.0.93**, Java **25**.

[Télécharger le JAR](https://github.com/axephir/TheNewDimension/raw/refs/heads/downloads/tnd-0.10.0-dev.jar) · [Galerie en jeu](https://github.com/axephir/TheNewDimension/blob/downloads/GALLERY_0.10.0.md)

Cette extension ajoute **neuf machines fonctionnelles**, trois blocs d’infrastructure et six composants ou produits industriels. Elle fonctionne dans les sauvegardes existantes : aucune exploration de nouveaux chunks n’est nécessaire pour construire une usine. Les contenus de la 0.9 restent inclus.

## Démarrer en survie

Obtenez du fer, du cuivre, du quartz du Nether, de la redstone, de l’or et de l’améthyste. Le cuivre débloque les recettes industrielles dans le livre de recettes. Fabriquez des charpentes, des bobines et des circuits, puis une dynamo et des câbles. L’agriculture, l’élevage, la collecte pastorale et le four électrique sont accessibles sans minerai de Vestige. Les meilleures installations exigent l’exploration : alliage trempé pour le broyeur et les trépans, auralite pour la centrale solaire, alliage ombral, bloc de diamant et lingot de netherite pour la carrière.

Dans **Inventaire → Guide → Industrie**, le manuel explique chaque machine. Le bouton **?** d’une machine ouvre directement sa page : montage, consommables, coût, portée et conditions. Chaque écran montre la réserve exacte de FE, l’activité, les entrées et les neuf emplacements de sortie.

## Énergie et distribution

| Installation | Fonction |
| --- | --- |
| Dynamo à charbon | 1 charbon ou charbon de bois produit **8 000 FE**, à **32 FE/t**, pendant 12,5 secondes d’activité. Le combustible partiellement utilisé se conserve si la réserve est pleine. |
| Contrôleur solaire | **32 FE/t**, uniquement dans l’Overworld, en journée et par temps sec. Nécessite huit panneaux, posés autour du contrôleur à la même hauteur, dans un carré 3 × 3 entièrement à ciel ouvert. |
| Accumulateur industriel | Stocke **1 000 000 FE** ; distribue jusqu’à **256 FE/t** par impulsions de dix ticks. Le comparateur mesure sa charge. |
| Câble énergétique | Relie les six faces des machines. Aucun stockage caché dans les câbles. Leur collision suit leur forme visible. |

Les consommateurs reçoivent l’énergie avant les accumulateurs. Une batterie alimente les machines, sans transférer sa charge vers une autre batterie. Plusieurs générateurs et batteries peuvent desservir une usine. Chaque source parcourt au plus **128 positions** par distribution ; construisez plusieurs circuits pour les très grands complexes. Les transferts utilisent les transactions de l’API énergétique NeoForge 26.2 : une opération annulée ne modifie aucune réserve. Les ports des machines acceptent aussi les transferts FE des autres mods utilisant cette API ; aucune compatibilité avec un mod tiers précis n’a été testée.

Un signal redstone suspend le travail et la distribution d’une source. Une batterie suspendue peut encore recevoir de l’énergie. Les nouvelles machines travaillent seulement pendant le chargement effectif de leurs chunks : aucun chargement forcé ni production rétroactive pendant une déconnexion.

## Transformation des minerais

**Broyeur et four électrique demandent chacun un socle de huit charpentes**, un bloc sous la machine. Vue du socle :

```text
CCC
C.C    C = charpente ; . = centre libre pour un entonnoir de sortie
CCC
```

| Machine | Transformation | Coût | Durée |
| --- | --- | --- | --- |
| Broyeur électrique | 1 fer, cuivre ou or brut → **2 granulés** | 2 400 FE | 4 s |
| Four électrique | 1 granulé ou métal brut → 1 lingot | 1 200 FE | 3 s |
| Four électrique | Auralite/noctilite brute ou granulée → lingot/cristal | 1 200 FE | 3 s |
| Four électrique | Sable → verre ; pavés → pierre ; pierre → pierre lisse ; boule d’argile → brique ; netherrack → brique du Nether | 1 200 FE | 3 s |

Les granulés se cuisent également dans les fours vanilla, sans XP de fusion. Les lingots et granulés ne se rebroyent pas. Un métal brut passé par le broyeur puis deux cuissons coûte donc **4 800 FE pour deux lingots**. La forge résonante garde sa filière utilisant la poussière d’écho, et le séparateur garde l’augmentation de rendement des minerais de Vestige. Le four électrique ne transforme pas le bois en charbon.

## Carrière industrielle

Son prix comprend notamment **un bloc de diamant**, **un lingot de netherite**, deux alliages ombraux et deux alliages trempés. Elle demande aussi un véritable derrick de **37 charpentes** :

1. Placez le contrôleur au centre d’un carré de 7 × 7.
2. Un bloc plus bas, construisez la **bordure du carré** : 24 charpentes.
3. Au-dessus des quatre angles, ajoutez **trois charpentes par pilier**, aux hauteurs du contrôleur, +1 et +2 : 12 charpentes.
4. Placez la dernière charpente **directement au-dessus du contrôleur**. Le dessous reste libre pour un entonnoir de sortie.
5. Branchez l’énergie, insérez des trépans par un entonnoir latéral ou l’interface et cliquez sur **Démarrer le forage**.

La carrière retire **réellement** les blocs d’une colonne de **7 × 7**, centrée sous elle, à partir de deux blocs plus bas et jusqu’au fond du monde. **Les sols et murs en pierre ou terre dans cette zone seront excavés : choisissez votre emplacement avant de démarrer.** Le bouton permet de suspendre la carrière ; la redstone et le démontage d’un élément du derrick l’arrêtent aussi.

Un trépan couvre **1 024 blocs réellement extraits**. Quatre trépans coûtent trois diamants et deux alliages trempés. La carrière consomme **480 FE par bloc ordinaire**, **4 800 FE par minerai**, avec un minimum d’une seconde par bloc. Ses neuf sorties et un réseau d’entonnoirs/coffres permettent de stocker les matières en grande quantité. Une réserve pleine laisse le bloc, l’énergie et le trépan intacts.

L’extraction utilise les tables de butin natives avec une pioche en diamant, sans Fortune et sans XP. Elle accepte les roches et sols naturels courants ainsi que les minerais ; elle ne retire ni coffre, ni autre bloc possédant une entité, ni bloc incassable, ni bloc décoratif hors de cette liste. Elle transmet un événement de casse avec le joueur technique NeoForge pour permettre aux protections compatibles de refuser la casse ; aucune intégration avec un gestionnaire de territoires tiers précis n’a été testée. Elle n’invente pas de gisements. Son curseur, son activation, sa réserve et l’usure du trépan sont sauvegardés. Démonter une machine fait tomber son inventaire ; l’énergie, le combustible déjà entamé et les charges du trépan intégrées ne sont pas transportés dans le bloc récupéré.

## Agriculture et élevage

| Machine | Utilisation |
| --- | --- |
| Cultivateur | Travaille dans un carré **9 × 9**, à la même hauteur que les plants. Récolte un plant mûr toutes les 2 s pour **800 FE**. Garde une graine ou un légume du butin pour replanter. Blé, carottes, pommes de terre et betteraves utilisent leur croissance naturelle. |
| Éleveur automatique | Nourrit deux adultes compatibles de la même espèce, proches de moins de quatre blocs, avec **deux aliments**, pour **1 600 FE** en 5 s. Rayon de cinq blocs autour de la machine, hauteur ±2. Les animaux se rejoignent et se reproduisent par leur IA native. |
| Station pastorale | Avec des cisailles dans la première entrée, tond un mouton adulte disponible pour **600 FE** en 2 s et use l’outil. Avec des seaux dans la seconde entrée, donne du lait de vache pour **400 FE** en 2 s. Ramasse les vrais objets au sol à cinq blocs pour **120 FE par pile**, à raison d’une pile par seconde. |

Le cultivateur demande des terres préparées, hydratées et éclairées. Il n’accélère pas la croissance et n’accorde pas d’XP automatique. Les récoltes peuvent alimenter l’éleveur par entonnoirs. Les quatre aliments principaux et les graines de céréales conviennent aux animaux vanilla correspondants.

L’éleveur respecte les bébés, les délais de reproduction vanilla, l’alimentation de chaque espèce et l’état amoureux. Il s’arrête à **16 animaux présents** dans sa zone. Préparez des enclos accessibles et conservez une clôture complète autour des machines ; répartissez les troupeaux : il ne téléporte pas les animaux, ne traverse pas leurs clôtures et n’invente pas de petits.

Les moutons conservent leur couleur et repoussent leur laine selon les règles vanilla. Les œufs sont ceux réellement pondus par les poules. Le délai de traite est de **60 secondes par vache**, mémorisé sur l’animal et partagé entre les stations ; remplacer la machine ne réinitialise pas ce délai. La station ne tue pas les animaux.

## Ports et interfaces

- Entrées : dessus ou côtés. La station pastorale possède deux entrées : cisailles et seaux.
- Sorties : dessous, uniquement les neuf emplacements de produits. Les matières premières ne sont pas aspirées par la sortie.
- Signal redstone : pause. Comparateur : niveau de charge énergétique.
- Stockage plein ou énergie insuffisante : aucun matériau, bloc ou animal n’est consommé.
- Sons mécaniques locaux à faible volume, étincelles pendant le fonctionnement, voyants actifs et barres animées.
- Textures originales **16 × 16**, en accord avec l’échelle des blocs vanilla.

## Validation

Les tests natifs couvrent la conservation et l’annulation des transferts FE, les câbles débranchés, les batteries sans transfert circulaire, le rendement exact du charbon, les socles, les conversions et leurs coûts, les vrais entonnoirs, le forage volontaire, les blocs protégés, les sorties pleines, les cultures et leur replantation, la reproduction par l’IA vanilla, la limite de troupeau, la tonte, la traite partagée, les sauvegardes et le montage solaire.

La compilation de la version passe avec **51 035 assertions** du noyau. La campagne complète compte **90 tests natifs**, dont **16 tests industriels** ; les **19 contrôles de ressources** valident les modèles, textures, recettes, traductions et données. Les tests industriels finaux vérifient notamment une carrière réellement alimentée par un entonnoir latéral et vidée dans un coffre par un entonnoir inférieur.

Le client normal charge une sauvegarde de la 0.9, affiche les écrans et le manuel en français dans une fenêtre de 854 × 480, et fait fonctionner une installation de démonstration dans l’Overworld. Les captures proviennent du moteur Minecraft. Le terrain, les animaux, les consommables, les minerais de démonstration et une réserve initiale d’énergie ont été préparés en Créatif ; les prises de vue aériennes utilisent aussi le mode Spectateur. Cette installation d’essai n’est ni une structure naturelle, ni une partie de survie déjà équilibrée sur plusieurs dizaines d’heures. Aucun monde local n’est distribué dans le ZIP.

Les effets sonores des machines réutilisent un événement vanilla ; les sons originaux de Vestige restent inchangés. L’environnement graphique utilise un rendu logiciel et un pilote audio nul : la lecture humaine du son en jeu n’a pas été évaluée ici.

Le cahier des charges global reste la feuille de route ; cette livraison constitue l’extension industrielle de la version de développement.
