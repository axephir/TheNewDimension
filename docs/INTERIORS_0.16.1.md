# 0.16.1 — Reliefs du Premier Silence

Cette révision reprend les intérieurs des **48 chambres de l’Observatoire**, ainsi que les **neuf sprites d’objets introduits en 0.16**. Les textures restent en 16 × 16 ; les modèles d’armures portées ne changent pas.

## Architecture

Les fondations sont réellement excavées : les cours latérales descendent sur quatre niveaux, avec des marches natives et un fond minéral éclairé. Les salles de surveillance et les arènes conservent leur sol fonctionnel ; les réacteurs reposent sur des fondations pleines. Les salles ordinaires possèdent des terrasses arrière à deux marches, et les arènes des tribunes latérales en gradins.

Deux arcades monumentales, leurs colonnes, bases et chapiteaux structurent chaque salle. Les angles sont chanfreinés, les voûtes ont une courbure plus marquée et des caissons en retrait. Les galeries disposent de consoles, d’alcôves profondes, de banquettes et de pilastres saillants. Des lampes à hauteur humaine et des lignes sous les arches éclairent le relief.

Chaque acte a son mobilier : reliquaires étagés, archives ajourées avec tiroirs lumineux, réacteurs à enveloppe creuse et conduites, arbres pétrifiés ramifiés, bancs cérémoniels et instruments annulaires percés. Ces éléments remplacent les grands amas rectangulaires de la première version.

Les positions des stations, disques, stèles, relais, coffres et résonances restent compatibles avec la progression existante. Les grands gardiens disposent d’une zone centrale dégagée. La génération reste strictement locale au chunk, sans chargement forcé des voisins.

## Objets

Éclat mnémonique, cœur axiomatique, lingot de nocturne, circuit mnémonique, auralite purifiée, mémoire du Premier Silence, boussole, lame et pioche de nocturne : silhouettes distinctes, facettes, arêtes métalliques, inclusions et traces gravées. Les neuf sprites sont exportés en 16 × 16, seize couleurs maximum et transparence binaire, puis stockés sous forme canonique dans le générateur.

## Parties existantes

Remplacer l’ancien JAR TND côté client et serveur. Les nouveaux sprites s’appliquent immédiatement à tous les inventaires. **Les intérieurs rénovés apparaissent dans les Observatoires générés dans de nouveaux chunks** : les bâtiments déjà enregistrés ne sont pas réécrits. Pour voir l’ensemble cohérent, rejoindre un autre site encore inexploré avec la boussole, ou tester dans un nouveau monde. Les compétences, récompenses et familiers sauvegardés restent conservés.

## Vérification

Deux nouveaux GameTests contrôlent les accès des 48 chambres à partir des collisions natives (stations, dispositifs, coffres, escaliers et galeries), les volumes de combat et la véritable excavation des fondations. Ils complètent les tests de génération locale et de progression existants. Les captures de la galerie sont réalisées avec F2 dans le client Minecraft, sur un site inédit du monde de contrôle. Le parcours de 3 h 30–4 h reste un objectif à mesurer lors de campagnes complètes.
