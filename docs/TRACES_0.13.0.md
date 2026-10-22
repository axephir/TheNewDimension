# 0.13 — Les traces muettes

Minecraft Java 26.2 · NeoForge 26.2.0.93 · Java 25. Même JAR côté client et serveur ; retirer l’ancien exemplaire de TND.

## Des maîtrises qui prennent des chemins différents

Les quinze maîtrises conservent leurs cinq fondations et reçoivent chacune deux talents propres : un passif au niveau 25, puis une capacité au niveau 50. Chaque spécialisation coûte douze points. Les sept talents d’une maîtrise restent tous accessibles avant le niveau 100 ; aucun arbre n’impose une exclusion définitive.

L’écran des maîtrises comporte trois pages : **Bases**, **Spécialités** et **Distinctions**. Le survol explique les effets, le coût, le niveau et le prérequis. La capacité s’utilise depuis la deuxième page, en Survie, avec une recharge commune de soixante secondes. Ses conditions sont vérifiées sur le serveur. Les ingrédients ne sont consommés que lorsqu’une action peut aboutir ; cliquer à nouveau pendant la recharge ne consomme rien.

| Maîtrise | Passif propre | Capacité propre |
|---|---|---|
| Mine | Repérage étendu | Célérité II, pioche en main |
| Bûcheronnage | Replante une pousse de l’inventaire lors d’une coupe naturelle accroupie | Célérité II, hache en main, repérage |
| Agriculture | Replante une graine de l’inventaire lors d’une récolte accroupie | Un stade de croissance pour huit cultures maximum contre quatre poudres d’os |
| Pêche | Respiration aquatique dans l’eau | Chance II et respiration pendant trente secondes |
| Chasse | Repérage étendu | Approche invisible et rapide pendant dix secondes |
| Élevage | Descendance plus robuste | Soigne jusqu’à douze animaux blessés contre quatre blés |
| Mêlée | Bonus contre une cible isolée | Onde de recul et ralentissement des hostiles visibles |
| Distance | Bonus au-delà de douze blocs | Prochain projectile ralentissant pendant vingt secondes |
| Défense | Résistance au recul au sol | Quatre cœurs temporaires pendant dix secondes |
| Artisanat | Équipements neufs plus durables | Répare un objet contre un alliage trempé |
| Construction | Portée supplémentaire en tenant un bloc | Chute lente pendant trente secondes |
| Exploration | Allure accrue sous le ciel ouvert | Vitesse II et saut pendant vingt secondes |
| Alchimie | Potions bénéfiques prolongées | Retire trois afflictions contre une fiole de miel |
| Ingénierie | Calibration permanente de l’extracteur | Calibration de relique à 25 % contre un circuit |
| Technologie ancienne | Repérage étendu | Contre-fréquence qui affaiblit les hostiles visibles |

L’automatisation ne donne toujours pas d’XP de récolte. Les capacités n’en donnent pas non plus. Les bonus de combat spécialisés excluent les joueurs. Les cultures d’une ville protégée ne peuvent pas être modifiées par le talent de croissance.

## Une progression qui laisse une signature

Les niveaux 100, 250, 500 et 1 000 débloquent les distinctions **Initié**, **Veilleur**, **Éminence** et **Légende**, déclinées selon chaque maîtrise. Choisir un titre l’affiche discrètement en bas à gauche du jeu. Il peut être masqué librement.

Le premier choix de chaque palier remet un **sceau décoratif lumineux** : quatre nouveaux blocs en textures 16 × 16. Changer de titre ou de maîtrise ne recrée pas le sceau. Le registre des récompenses et le titre survivent à la mort et au rechargement. Ces distinctions ne renforcent pas les statistiques.

## Découvrir avant de comprendre

Le noyau ne porte plus un nom qui annonce sa fonction. Son interface distingue le signal observé et les instructions pratiques du socle. Les archives donnent des traces contradictoires, des phrases interrompues et des indices liés aux lieux ; elles ne livrent plus l’explication finale avant l’événement.

Les événements rares choisissent leur mise en scène selon les alentours : présence derrière soi, poussière au sol, réponse d’une machine ou bruit provenant d’une porte. Une porte peut s’ouvrir dans les seuls quartiers générés et protégés de la nouvelle métropole. Le système n’ouvre pas les portes des constructions personnelles et ne ferme jamais un passage sur un joueur. Les premières manifestations restent espacées de dix à trente minutes, les suivantes de trente à cent minutes. Sons, rémanence et animations restent réglables.

Le guide pratique présente désormais chaque fiche et toutes ses pages. Les textes longs ne sont plus coupés après quatre lignes.

<details>
<summary>Conclusion : fonctionnement et récupération — contient des indications sur la scène finale</summary>

La scène utilise toujours une dimension personnelle isolée ; le monde source n’est jamais détruit, réécrit ni restauré depuis une vieille copie. La taille par défaut des nouvelles configurations passe à **25 × 25 chunks, soit 400 × 400 blocs**. Un serveur possédant déjà une configuration à huit conserve ce réglage ; `narrative.projectionRadiusChunks=12` active le nouveau maximum. Les copies s’étalent sur les ticks du serveur, sans dupliquer les inventaires et l’énergie des machines.

Après la courte caméra, le clic droit permet d’écouter quatre familles de traces dans les décombres. Le serveur détermine lui-même le bloc regardé, vérifie la portée, la session personnelle et le délai entre interactions. Le noyau peut être retrouvé et utilisé sans commande. Une indication apparaît après deux minutes ; le refus laisse l’exploration accessible.

`/tnd recover` reste la voie de récupération indépendante. Les références doubles vérifiées, journaux atomiques et protections de l’inventaire de la 0.12 sont conservés. Les changements légitimes d’un autre joueur dans l’Overworld ne sont jamais annulés.

</details>

## Validation et limites

La validation de cette version associe tests de noyau, ressources, GameTests natifs et inspection des interfaces dans le client Minecraft. Le compte rendu publié avec le JAR donne les nombres et résultats effectifs. Les joueurs des GameTests utilisent de vraies entités serveur avec connexions intégrées ; ces scénarios ne sont pas une longue partie humaine ni un essai de plusieurs clients indépendants sur Internet.

Les identités sauvegardées des maîtrises, talents existants, blocs et machines sont conservées. Les textures de blocs et objets restent en 16 × 16, les atlas de personnages en 64 × 64. Les anciens exemplaires du JAR restent téléchargeables.
